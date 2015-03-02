/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.edu.icm.cermine.content.references;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import static junit.framework.Assert.assertEquals;
import org.junit.Test;
import pl.edu.icm.cermine.bibref.model.BibEntry;
import pl.edu.icm.cermine.content.model.DocumentParagraph;

/**
 * @author Dominik Horb <cermine@dominik.berlin>
 */
public class NumericEndReferenceMatcherTest {

    @Test
    public void testReferenceRangesAreParsedProperly() throws Exception {
        List<BibEntry> endReferences = this.prepareSimpleEndReferences(); 
        EndReferenceMatcher instance = EndReferenceMatcher.create(InTextReferenceType.NUMERIC, endReferences);
        InTextReferenceStyle style = new InTextReferenceStyle(BracketType.SQUARE_BRACKETS, InTextReferenceType.NUMERIC);
        DocumentParagraph parentParagraph = this.prepareParentParagraph();
        
        InTextReference referenceToCheck = new InTextReference(parentParagraph, 6, 9, style);
        
        Set<BibEntry> result = instance.match(referenceToCheck);
        
        assertEquals(result.size(), 3);
    }
    
    private DocumentParagraph prepareParentParagraph(){
        DocumentParagraph parentParagraph = new DocumentParagraph("Claim[1–3]", null);
        return parentParagraph;   
    }
    
    private List<BibEntry> prepareSimpleEndReferences(){
        List<BibEntry> endReferences = new ArrayList<>();
        endReferences.add(new BibEntry("ref1"));
        endReferences.add(new BibEntry("ref2"));
        endReferences.add(new BibEntry("ref3"));
        
        return endReferences;
    }
    
}
