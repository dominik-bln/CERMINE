/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.edu.icm.cermine.content.references;

import java.util.ArrayList;
import java.util.List;
import pl.edu.icm.cermine.bibref.model.BibEntry;
import pl.edu.icm.cermine.content.model.DocumentParagraph;

/**
 * This class represents an in-text reference in a paragraph.
 * 
 * An in-text reference is an annotation that points to an end reference with further information
 * about the cited source.
 * 
 * In some cases one in-text reference can point to several end references (i. e. [5-8]), therefore 
 * the end references are stored in a list.
 *
 * @author Dominik Horb <cermine@dominik.berlin>
 */
public final class InTextReference {

    private final int startPosition;
    private final int endPosition;
    private final DocumentParagraph parentParagraph;
    private final InTextReferenceStyle inTextReferenceStyle;
    private List<BibEntry> endReferences;

    /**
     * 
     * @param parentParagraph The paragraph this reference is found in.
     * @param startPosition The starting index in the parent paragraph. Should be pointing to the content.
     * @param endPosition The end position in the parent paragraph, exclusive (point at the closing bracket).
     * @param inTextReferenceStyle The style of this in-text reference.
     */
    public InTextReference(DocumentParagraph parentParagraph, int startPosition, int endPosition, InTextReferenceStyle inTextReferenceStyle) {
        this.endReferences = new ArrayList<>();
        this.parentParagraph = parentParagraph;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.inTextReferenceStyle = inTextReferenceStyle;
    }

    public final DocumentParagraph getParentParagraph() {
        return this.parentParagraph;
    }

    public final int getStartPosition() {
        return this.startPosition;
    }

    public final int getEndPosition() {
        return this.endPosition;
    }

    public final InTextReferenceStyle getInTextReferenceStyle(){
        return this.inTextReferenceStyle;
    }

    public final List<BibEntry> getEndReferences() {
        return this.endReferences;
    }
    
    public final void setEndReferences(List<BibEntry> endReferences){
        this.endReferences = endReferences;
    }
}
