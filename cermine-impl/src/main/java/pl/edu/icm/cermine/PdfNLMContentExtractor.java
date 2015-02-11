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
package pl.edu.icm.cermine;

import java.io.*;
import java.util.Collection;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import pl.edu.icm.cermine.content.model.BxDocContentStructure;
import pl.edu.icm.cermine.content.model.DocumentContentStructure;
import pl.edu.icm.cermine.content.transformers.BxContentStructToDocContentStructConverter;
import pl.edu.icm.cermine.content.transformers.DocContentStructToNLMElementConverter;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.exception.CermineException;
import pl.edu.icm.cermine.exception.TransformationException;
import pl.edu.icm.cermine.structure.model.BxDocument;
import pl.edu.icm.cermine.structure.transformers.BxDocumentToTrueVizWriter;
import pl.edu.icm.cermine.tools.transformers.ModelToModelConverter;

/**
 * NLM-based content extractor from PDF files.
 *
 * @author Dominika Tkaczyk
 */
public class PdfNLMContentExtractor extends AbstractExtractor<InputStream, Element> {

    public static final int DEFAULT_THREAD_NUMBER = 3;

    public static int THREADS_NUMBER = DEFAULT_THREAD_NUMBER;

    private final boolean extractMetadata;
    private final boolean extractReferences;
    private final boolean extractText;
    private final boolean extractInTextReferences;

    public PdfNLMContentExtractor() throws AnalysisException {
        this(true, true, true, true);
    }

    public PdfNLMContentExtractor(boolean extractMetadata, boolean extractText, boolean extractInTextReferences, boolean extractReferences) throws AnalysisException {
        super();
        this.extractMetadata = extractMetadata;
        this.extractReferences = extractReferences;
        this.extractText = extractText;
        this.extractInTextReferences = extractInTextReferences;
    }

    /**
     * Extracts content from PDF file and converts it to JATS format.
     *
     * @param input The input stream of the PDF document.
     * @return The extracted content in JATS XML.
     * @throws AnalysisException
     */
    @Override
    public Element extract(InputStream input) throws CermineException {
        BxDocument document = this.extractBasicStructure(input);
        Element root = new Element("article");
        
        this.extractMetadata(root, document);
        // we need the end references before the text in order to use the information
        // for in-text reference extraction
        this.extractEndReferences(root, document);
        this.extractText(root, document);

        return root;
    }

    private void extractText(Element root, BxDocument document) throws AnalysisException {
        if (extractText) {
            try {
                ModelToModelConverter<DocumentContentStructure, Element> converter
                    = new DocContentStructToNLMElementConverter();
                DocumentContentStructure struct = this.extractText(this.getConfiguration(), document);
                Element text = converter.convert(struct);
                root.addContent(text);
            } catch (TransformationException ex) {
                throw new AnalysisException("Cannot extract text from document!", ex);
            }
        }
    }

    /**
     * Extracts full text from document's box structure.
     *
     * @param conf extraction configuration
     * @param document box structure
     * @return document's full text
     * @throws AnalysisException
     */
    public DocumentContentStructure extractText(ComponentConfiguration conf, BxDocument document)
        throws AnalysisException {
        try {
            BxDocument doc = conf.contentFilter.filter(document);
            BxDocContentStructure tmpContentStructure = conf.contentHeaderExtractor.extractHeaders(doc);
            conf.contentCleaner.cleanupContent(tmpContentStructure);
            BxContentStructToDocContentStructConverter converter
                = new BxContentStructToDocContentStructConverter();
            return converter.convert(tmpContentStructure);
        } catch (TransformationException ex) {
            throw new AnalysisException("Cannot extract content from the document!", ex);
        }
    }

    private void extractMetadata(Element root, BxDocument document) throws AnalysisException {
        // front is a required element, so we always add it
        Element metadata = new Element("front");
        if (extractMetadata) {
            Element meta = ExtractionUtils.extractMetadataAsNLM(configuration, document);
            metadata = (Element) meta.getChild("front").clone();
        }
        root.addContent(metadata);
    }

    private void extractEndReferences(Element root, BxDocument document) throws AnalysisException {
        if (this.extractReferences) {
            Element back = new Element("back");
            root.addContent(back);

            Element refList = new Element("ref-list");
            back.addContent(refList);

            Element[] references = ExtractionUtils.convertReferences(ExtractionUtils.extractReferences(configuration, document));
            this.addEndReferenceElements(refList, references);
        }
    }

    /**
     * Takes the given array of references and adds them to the given list element.
     *
     * @param refList
     * @param references
     *
     * @todo remove code duplication between ContentExtractor and PdfNlmContentExtractor
     */
    private void addEndReferenceElements(Element refList, Element[] references) {
        int index = 1;
        for (Element ref : references) {
            Element r = new Element("ref");
            r.setAttribute("id", "R" + index);
            r.addContent(ref);
            refList.addContent(r);
            index++;
        }
    }

    public static void main(String[] args) throws ParseException, IOException {
        CommandLineOptionsParser parser = new CommandLineOptionsParser();
        if (!parser.parse(args)) {
            System.err.println(
                "Usage: PdfNLMContentExtractor -path <path> [optional parameters]\n\n"
                + "Tool for extracting metadata and content from PDF files.\n\n"
                + "Arguments:\n"
                + "  -path <path>              path to a PDF file or directory containing PDF files\n"
                + "  -ext <extension>          (optional) the extension of the resulting metadata file;\n"
                + "                            default: \"cermxml\"; used only if passed path is a directory\n"
                + "  -modelmeta <path>         (optional) the path to the metadata classifier model file\n"
                + "  -modelinit <path>         (optional) the path to the initial classifier model file\n"
                + "  -str                      whether to store structure (TrueViz) files as well;\n"
                + "                            used only if passed path is a directory\n"
                + "  -strext <extension>       (optional) the extension of the structure (TrueViz) file;\n"
                + "                            default: \"cxml\"; used only if passed path is a directory\n"
                + "  -threads <num>            number of threads for parallel processing\n");
            System.exit(1);
        }

        String path = parser.getPath();
        String extension = parser.getNLMExtension();
        boolean extractStr = parser.extractStructure();
        String strExtension = parser.getBxExtension();
        PdfNLMContentExtractor.THREADS_NUMBER = parser.getThreadsNumber();

        File file = new File(path);
        if (file.isFile()) {
            try {
                PdfNLMContentExtractor extractor = new PdfNLMContentExtractor();
                parser.updateMetadataModel(extractor.getConfiguration());
                parser.updateInitialModel(extractor.getConfiguration());
                InputStream in = new FileInputStream(file);
                Element result = extractor.extract(in);
                XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
                System.out.println(outputter.outputString(result));
            } catch (CermineException ex) {
                ex.printStackTrace();
            }
        } else {

            Collection<File> files = FileUtils.listFiles(file, new String[]{"pdf"}, true);

            int i = 0;
            for (File pdf : files) {
                File xmlF = new File(pdf.getPath().replaceAll("pdf$", extension));
                if (xmlF.exists()) {
                    i++;
                    continue;
                }

                long start = System.currentTimeMillis();
                float elapsed = 0;

                System.out.println(pdf.getPath());

                try {
                    PdfNLMContentExtractor extractor = new PdfNLMContentExtractor();
                    parser.updateMetadataModel(extractor.getConfiguration());
                    parser.updateInitialModel(extractor.getConfiguration());

                    InputStream in = new FileInputStream(pdf);

                    BxDocument doc = extractor.extractBasicStructure(in);
                    Element result = extractor.extract(in);

                    long end = System.currentTimeMillis();
                    elapsed = (end - start) / 1000F;

                    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
                    if (!xmlF.createNewFile()) {
                        System.out.println("Cannot create new file!");
                    }
                    FileUtils.writeStringToFile(xmlF, outputter.outputString(result));

                    if (extractStr) {
                        BxDocumentToTrueVizWriter writer = new BxDocumentToTrueVizWriter();
                        File strF = new File(pdf.getPath().replaceAll("pdf$", strExtension));
                        writer.write(new FileWriter(strF), doc.getPages());
                    }
                } catch (CermineException ex) {
                    ex.printStackTrace();
                }

                i++;
                int percentage = i * 100 / files.size();
                if (elapsed == 0) {
                    elapsed = (System.currentTimeMillis() - start) / 1000F;
                }
                System.out.println("Extraction time: " + Math.round(elapsed) + "s");
                System.out.println(percentage + "% done (" + i + " out of " + files.size() + ")");
                System.out.println("");
            }
        }
    }

}
