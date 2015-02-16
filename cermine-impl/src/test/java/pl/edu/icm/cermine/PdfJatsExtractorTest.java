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
import org.custommonkey.xmlunit.Diff;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import pl.edu.icm.cermine.content.references.InTextReference;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.exception.CermineException;

/**
 *
 * @author Dominika Tkaczyk
 */
public class PdfJatsExtractorTest {

    static final private String TEST_FILE = "/pl/edu/icm/cermine/test2.pdf";
    static final private String EXP_FILE = "/pl/edu/icm/cermine/test2-cont_with_xref.xml";

    private PdfJatsExtractor extractor;

    @Before
    public void setUp() throws AnalysisException, IOException {
        extractor = new PdfJatsExtractor();
    }

    @Test
    public void contentExtractionTest() throws Exception {
        Element testContent;
        try (InputStream testStream = this.getClass().getResourceAsStream(TEST_FILE)) {
            testContent = extractor.extract(testStream);
        }

        SAXBuilder saxBuilder = new SAXBuilder("org.apache.xerces.parsers.SAXParser");
        Document dom;
        try (InputStream expStream = this.getClass().getResourceAsStream(EXP_FILE);
            InputStreamReader expReader = new InputStreamReader(expStream);) {
            dom = saxBuilder.build(expReader);
        }
        Element expContent = dom.getRootElement();

        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        Diff diff = new Diff(outputter.outputString(expContent), outputter.outputString(testContent));
        System.out.println(outputter.outputString(testContent));
        assertTrue(diff.similar());
    }

    @Test
    public void inTextReferenceExtractionTest() throws CermineException, IOException {
        try (InputStream testStream = this.getClass().getResourceAsStream(TEST_FILE)) {
            // call extract so that the in-text references are accessible
            extractor.extract(testStream);
        }

        List<InTextReference> references = extractor.getInTextReferences();
        String result = "";
        for (InTextReference reference : references) {
            result += reference.getParentParagraph().getText().substring(
                reference.getPosition(), reference.getPosition() + reference.getLength());
        }

        System.out.println(result);
    }
}
