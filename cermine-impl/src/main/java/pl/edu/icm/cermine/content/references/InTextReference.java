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
package pl.edu.icm.cermine.content.references;

import java.util.HashSet;
import java.util.Set;
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
    private Set<BibEntry> endReferences;

    /**
     * 
     * @param parentParagraph The paragraph this reference is found in.
     * @param startPosition The starting index in the parent paragraph. Should be pointing to the content.
     * @param endPosition The end position in the parent paragraph, exclusive (point at the closing bracket).
     * @param inTextReferenceStyle The style of this in-text reference.
     */
    public InTextReference(DocumentParagraph parentParagraph, int startPosition, int endPosition, InTextReferenceStyle inTextReferenceStyle) {
        this.endReferences = new HashSet<>();
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

    public final Set<BibEntry> getEndReferences() {
        return this.endReferences;
    }
    
    public final void setEndReferences(Set<BibEntry> endReferences){
        this.endReferences = endReferences;
    }
}
