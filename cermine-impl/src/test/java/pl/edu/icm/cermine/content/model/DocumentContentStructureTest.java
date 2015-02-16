/**
 * This file is part of CERMINE project.
 * Copyright (c) 2011-2013 ICM-UW
 *
 * CERMINE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CERMINE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with CERMINE. If not, see <http://www.gnu.org/licenses/>.
 */

package pl.edu.icm.cermine.content.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import org.jdom.JDOMException;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import pl.edu.icm.cermine.content.transformers.HTMLToDocContentStructReader;
import pl.edu.icm.cermine.exception.TransformationException;
import pl.edu.icm.cermine.tools.transformers.FormatToModelReader;

/**
 *
 * @author Dominika Tkaczyk
 */
public class DocumentContentStructureTest {
    
    String modelFilePath = "/pl/edu/icm/cermine/content/model/model.xml";

    FormatToModelReader<DocumentContentStructure> reader;
    DocumentContentStructure structure;
    
    @Before
    public void setUp() throws JDOMException, IOException, TransformationException, URISyntaxException {
        reader = new HTMLToDocContentStructReader();
        
        InputStream is = this.getClass().getResourceAsStream(modelFilePath);
        InputStreamReader isr = new InputStreamReader(is);
        
        structure = reader.read(isr);
    }
    
    @Test
    public void topLevelStructureTest() {
        assertNull(structure.getHeading());
        assertNull(structure.getParent());
        assertEquals(4, structure.getParts().size());
        
        assertEquals(11, structure.getAllParagraphs().size());
        assertEquals(11, structure.getAllParagraphCount());
        assertEquals(11, structure.getAllParagraphTexts().size());
       
        assertEquals(10, structure.getHeadings().size());
        assertEquals(10, structure.getAllHeadingTexts().size());
        assertEquals("1. BACKGROUND", structure.getAllHeadingTexts().get(0));
        assertEquals(10, structure.getAllHeadingsCount());
        
        assertTrue(structure.containsHeadingText("3.1 OAI-PMH Data Provider"));
        assertTrue(structure.containsHeadingFirstLineText("3.1 OAI-PMH Data Provider"));
        assertFalse(structure.containsHeadingText("false"));
        assertFalse(structure.containsHeadingFirstLineText("false"));
    }
    
    @Test
    public void firstLevelStructureTest() {
        DocumentContentStructure firstLevelStruct = structure.getParts().get(1);
        assertNotNull(firstLevelStruct.getHeading());
        
        assertEquals(1, firstLevelStruct.getHeading().getLevel());
        assertEquals("2. DATA MODELING AND MAPPING", firstLevelStruct.getHeading().getText());
        assertEquals(firstLevelStruct, firstLevelStruct.getHeading().getContentStructure());
        assertEquals(firstLevelStruct.getParent(), structure);
        
        assertEquals(3, firstLevelStruct.getParts().size());
        assertEquals(3, firstLevelStruct.getAllParagraphs().size());
        assertEquals(3, firstLevelStruct.getAllParagraphCount());
        assertEquals(3, firstLevelStruct.getAllParagraphTexts().size());
        
        assertEquals(4, firstLevelStruct.getHeadings().size());
        assertEquals(4, firstLevelStruct.getAllHeadingTexts().size());
        assertEquals("2.1 Lined Data", firstLevelStruct.getAllHeadingTexts().get(1));
        assertEquals(4, firstLevelStruct.getAllHeadingsCount());
        
        assertTrue(firstLevelStruct.containsHeadingText("2.3 Dublin Core"));
        assertTrue(firstLevelStruct.containsHeadingFirstLineText("2.3 Dublin Core"));
        
        assertFalse(firstLevelStruct.containsHeadingText("test"));
        assertFalse(firstLevelStruct.containsHeadingFirstLineText("test"));
        
        assertNotNull(structure.getPreviousHeading(firstLevelStruct.getHeading()));
        assertEquals("1. BACKGROUND", structure.getPreviousHeading(firstLevelStruct.getHeading()).getText());
    }

}
