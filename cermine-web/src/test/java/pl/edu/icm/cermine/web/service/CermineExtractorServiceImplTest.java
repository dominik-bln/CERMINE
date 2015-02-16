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
import pl.edu.icm.cermine.web.service.exceptions.ServiceException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import static org.junit.Assert.*;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.icm.cermine.AbstractExtractor;
import pl.edu.icm.cermine.exception.AnalysisException;

/**
 *
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 */
public class CermineExtractorServiceImplTest {

    private final Logger log = LoggerFactory.getLogger(CermineExtractorServiceImplTest.class);

    private boolean sleeping = true;

    private CermineExtractorServiceImpl instance;
    
    @Before
    public void setUp(){
        instance = new CermineExtractorServiceImpl();
    }
    
    @Test
    public void testExtractNLMReturnsResultsForPDF() throws Exception {
        try (InputStream is = this.getClass().getResourceAsStream("/pdf/test1.pdf")) {
            log.debug("Input stream is: {}", is);

            ExtractionResult result = instance.extractNLM(is);
            assertNotNull(result);
            assertTrue(result.isSucceeded());
        }
    }

    @Test
    public void testQueueOverflow() throws Exception {
        System.out.println("Queue overflow");
        final CermineExtractorServiceImpl instance = new CermineExtractorServiceImpl(1, 1);
        final Map<Integer, Boolean> succ = new HashMap<>();

        //run immediately two, then 
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            final int k = i;
            threads.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    int idx = k;
                    try {
                        succ.put(idx, true);
                        instance.extractNLM(this.getClass().getResourceAsStream("/pdf/test1.pdf"));
                        log.debug("Extraction succeeeded...");
                    } catch (ServiceException ex) {
                        succ.put(idx, false);
                        log.debug("Service exception");
                    } catch (AnalysisException ex) {
                        java.util.logging.Logger.getLogger(CermineExtractorServiceImplTest.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }));

        }
        for (Thread t : threads) {
            t.start();
            Thread.sleep(500);
        }
        for (Thread t : threads) {
            if (t.isAlive()) {
                t.join();
            }
        }

        for (int i = 0; i < 2; i++) {
            assertTrue(succ.get(i));
        }
        assertFalse(succ.get(2));
        assertFalse(succ.get(3));

    }

    @Test
    public void testObtainExtractor() throws Exception {
        System.out.println("obtainExtractor");
        final CermineExtractorServiceImpl instance = new CermineExtractorServiceImpl(3, 0);
        List<AbstractExtractor> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            list.add(instance.obtainExtractor());
        }
        sleeping = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                AbstractExtractor res = instance.obtainExtractor();
                sleeping = false;
            }
        }).start();
        assertTrue(sleeping);
        Thread.sleep(100);
        assertTrue(sleeping);
        instance.returnExtractor(list.remove(0));
        Thread.sleep(100);
        assertFalse(sleeping);
    }
}
