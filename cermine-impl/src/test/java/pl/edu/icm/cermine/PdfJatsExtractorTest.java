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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import static junit.framework.Assert.assertEquals;
import org.custommonkey.xmlunit.Diff;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import pl.edu.icm.cermine.content.references.InTextReference;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.exception.CermineException;

/**
 *
 * @author Dominika Tkaczyk
 */
public class PdfJatsExtractorTest {

    static final private String TEST_FILE = "/pl/edu/icm/cermine/test2.pdf";
    static final private String TEST_FILE2 = "/pl/edu/icm/cermine/test4.pdf";
    static final private String EXAMPLE3 = "/pl/edu/icm/cermine/example3.pdf";
    static final private String EXP_FILE = "/pl/edu/icm/cermine/test2-cont_with_xref.xml";

    private PdfJatsExtractor extractor;

    @Before
    public void setUp() throws AnalysisException, IOException {
        extractor = new PdfJatsExtractor();
    }

    @Test
    public void testContentExtractionIsSimilarToPreparedXML() throws Exception {
        try {
            Element testContent = this.callExtractForFile(TEST_FILE);
            Element expContent = this.loadXMLFromFile(EXP_FILE);
            Diff diff = this.prepareXMLDiff(expContent, testContent);
            System.out.println(diff.toString());

            assertTrue(diff.similar());
        } catch (CermineException | IOException | JDOMException | SAXException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Test
    public void testCorrectAmountOfInTextReferencesIsExtractedForTestPDF() throws CermineException, IOException {
        this.callExtractForFile(TEST_FILE);

        List<InTextReference> references = extractor.getInTextReferences();
        assertEquals(references.size(), 17);
    }

    @Test
    public void testNameYearReferencesGetExtracted() throws CermineException, IOException {
        this.callExtractForFile(TEST_FILE2);
        List<InTextReference> references = extractor.getInTextReferences();

        // 15 is probably not final, but is what gets extracted correctly right now.
        assertTrue(references.size() >= 15);
    }

    @Test
    public void testNumericReferenceRangesInPDFAreFound() throws CermineException, IOException {
        this.callExtractForFile(EXAMPLE3);

        InTextReference reference = extractor.getInTextReferences().get(1);
        assertEquals(4, reference.getEndReferences().size());
    }
    
    private Element callExtractForFile(String file) throws CermineException, IOException {
        try (InputStream testStream = this.getClass().getResourceAsStream(file)) {
            // call extract so that the in-text references are accessible
            return extractor.extract(testStream);
        }
    }

    private Element loadXMLFromFile(String xmlFile) throws JDOMException, IOException {
        SAXBuilder saxBuilder = new SAXBuilder("org.apache.xerces.parsers.SAXParser");
        Document dom;
        try (InputStream expStream = this.getClass().getResourceAsStream(xmlFile);
            InputStreamReader expReader = new InputStreamReader(expStream);) {
            dom = saxBuilder.build(expReader);
        }
        return dom.getRootElement();
    }

    private Diff prepareXMLDiff(Element expContent, Element testContent) throws SAXException, IOException {
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        return new Diff(outputter.outputString(expContent), outputter.outputString(testContent));
    }
}
