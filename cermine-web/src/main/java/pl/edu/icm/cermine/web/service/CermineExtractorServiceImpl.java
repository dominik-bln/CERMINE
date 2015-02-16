/**
 * This file is part of CERMINE project. Copyright (c) 2011-2013 ICM-UW
 *
 * CERMINE is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Affero General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * CERMINE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with CERMINE. If
 * not, see <http://www.gnu.org/licenses/>.
 */
package pl.edu.icm.cermine.web.service;

import pl.edu.icm.cermine.web.service.model.ExtractionResult;
import pl.edu.icm.cermine.web.service.model.ExtractionTask;
import pl.edu.icm.cermine.web.service.model.TaskStatus;
import pl.edu.icm.cermine.web.service.model.ArticleMeta;
import pl.edu.icm.cermine.web.service.exceptions.ServiceException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.edu.icm.cermine.AbstractExtractor;
import pl.edu.icm.cermine.PdfJatsExtractor;
import pl.edu.icm.cermine.content.transformers.NLMElementToHTMLWriter;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.exception.CermineException;

/**
 * This class handles the threads for extracting content from files.
 *
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 * @author Dominik Horb <cermine@dominik.berlin>
 */
@Component
public class CermineExtractorServiceImpl implements CermineExtractorService {

    private static final int DEFAULT_POOL_SIZE = 4;
    private static final int DEFAULT_QUEUE_SIZE = 10000;

    private int threadPoolSize;
    private int maxQueueForBatch;
    private final Logger log = LoggerFactory.getLogger(CermineExtractorServiceImpl.class);

    private final List<AbstractExtractor> extractors;

    private ExecutorService processingExecutor;
    private ExecutorService batchProcessingExecutor;

    @Autowired
    TaskManager taskManager;

    /**
     * Initializes the class with defaults for thread pool and batch queue size.
     */
    public CermineExtractorServiceImpl() {
        this(DEFAULT_POOL_SIZE, DEFAULT_QUEUE_SIZE);
    }

    /**
     * Initializes the class with the given thread pool and batch queue size.
     *
     * @param threadPoolSize
     * @param maxQueueForBatch
     */
    public CermineExtractorServiceImpl(int threadPoolSize, int maxQueueForBatch) {
        this.threadPoolSize = threadPoolSize;

        this.maxQueueForBatch = maxQueueForBatch;
        if (this.maxQueueForBatch <= 0) {
            this.maxQueueForBatch = DEFAULT_QUEUE_SIZE;
        }

        this.createExecutorServices();

        this.extractors = new ArrayList<>();
        this.createExtractors();
    }

    /**
     * Processes the provided input stream without storing the result in the session.
     *
     * @param is
     * @return
     * @throws AnalysisException
     * @throws ServiceException
     */
    @Override
    public ExtractionResult extractNLM(InputStream is) throws AnalysisException, ServiceException {
        log.debug("Entering extractNLM method...");
        ExtractionResult res = new ExtractionResult();
        try {
            log.debug("Submitting extractNLM task...");
            Future<ExtractionResult> future = batchProcessingExecutor.submit(new SimpleExtractionCallable(is));

            Thread.yield();
            log.debug("waiting for extractNLM task...");
            res = future.get();
        } catch (RejectedExecutionException rje) {
            throw new ServiceException("Queue size exceeded.", rje);
        } catch (InterruptedException | ExecutionException ex) {
            log.error("Exception while executing extraction task...", ex);
            throw new RuntimeException(ex);
        }

        log.debug("Leaving extractNLM method...");
        return res;
    }

    /**
     * Processes an extraction task and registers it with the task manager for later retrieval.
     *
     * @param pdf
     * @param fileName
     * @return
     */
    @Override
    public long initExtractionTask(byte[] pdf, String fileName) {
        ExtractionTask task = this.taskManager.createTask(pdf, fileName);

        processingExecutor.submit(new ExtractingTaskExecution(task, this.log));
        task.setStatus(TaskStatus.QUEUED);

        return task.getId();
    }

    protected AbstractExtractor obtainExtractor() {
        log.debug("Obtaining an extractor from the pool");
        AbstractExtractor res;
        try {
            synchronized (extractors) {
                while (extractors.isEmpty()) {
                    log.debug("Extractor pool is empty, going to sleep...");
                    extractors.wait();
                }
                res = extractors.remove(0);
            }
            return res;
        } catch (InterruptedException ire) {
            log.error("Unexpected exception while waiting for extractor...", ire);
            throw new RuntimeException(ire);
        }
    }

    protected void returnExtractor(AbstractExtractor e) {
        log.debug("Returning extractor to the pool...");
        synchronized (extractors) {
            try {
                e = new PdfJatsExtractor();
                extractors.add(e);
            } catch (AnalysisException ex) {
                throw new RuntimeException("Cannot create extractor!", ex);
            }
            extractors.notify();
        }
    }

    /**
     * Method to perform real extraction.
     *
     * @param result
     * @param input
     * @return The result of the extraction on the input stream.
     */
    private ExtractionResult performExtraction(ExtractionResult result, InputStream input) {
        AbstractExtractor<InputStream, Element> extractor = null;
        try {
            extractor = obtainExtractor();
            result.setProcessingStart(new Date());
            log.debug("Starting extraction on the input stream...");
            Element resEl = extractor.extract(input);
            log.debug("Extraction ok..");
            
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            
            Document doc = new Document(resEl);
            String res = outputter.outputString(doc);
            result.setNlm(res);
            String html = new NLMElementToHTMLWriter().write(resEl);
            result.setHtml(html);

            result.setInTextReferences(Integer.toString(((PdfJatsExtractor) extractor).getInTextReferences().size()));
            log.debug("Article meta extraction start:");
            result.setMeta(ArticleMeta.extractNLM(doc));
            log.debug("Article meta extraction succeeded");
            result.setSucceeded(true);
        } catch (CermineException ex) {
            log.debug("Exception from analysis: ", ex);
            result.setError(ex);
            result.setSucceeded(false);
        } finally {
            if (extractor != null) {
                returnExtractor(extractor);
            }
            result.setProcessingEnd(new Date());
        }
        return result;
    }

    /**
     * Thread for handling one extraction task from the queue.
     */
    private class ExtractingTaskExecution implements Runnable {

        private final ExtractionTask task;
        private final Logger log;

        public ExtractingTaskExecution(ExtractionTask task, Logger log) {
            this.task = task;
            this.log = log;
        }

        @Override
        public void run() {
            this.log.debug("Starting processing task: " + this.task.getId());
            this.task.setStatus(TaskStatus.PROCESSING);

            ExtractionResult result = new ExtractionResult();
            result.setProcessingStart(new Date());

            log.debug("Running extraction: " + task.getId());
            performExtraction(result, new ByteArrayInputStream(task.getPdf()));
            task.setResult(result);

            log.debug("Processing finished: " + task.getId());
            if (result.isSucceeded()) {
                task.setStatus(TaskStatus.FINISHED);
            } else {
                task.setStatus(TaskStatus.FAILED);
            }
            //clean up memory, we will overflow after few request without it...
            task.setPdf(null);
            log.debug("finishing task: " + task.getId());
        }
    }

    private class SimpleExtractionCallable implements Callable<ExtractionResult> {

        private final InputStream input;

        public SimpleExtractionCallable(InputStream input) {
            this.input = input;
        }

        @Override
        public ExtractionResult call() throws CermineException {
            return performExtraction(new ExtractionResult(), input);
        }
    }

    private void createExecutorServices() {
        this.processingExecutor = Executors.newFixedThreadPool(threadPoolSize);
        ArrayBlockingQueue<Runnable> q = new ArrayBlockingQueue<>(maxQueueForBatch);
        this.batchProcessingExecutor = new ThreadPoolExecutor(threadPoolSize, threadPoolSize, 1, TimeUnit.DAYS, q);
    }

    private void createExtractors() {
        try {
            for (int i = 0; i < threadPoolSize; i++) {
                extractors.add(new PdfJatsExtractor());
            }
        } catch (AnalysisException ex) {
            log.error("Failed to init content extractor", ex);
            throw new RuntimeException(ex);
        }
    }
}
