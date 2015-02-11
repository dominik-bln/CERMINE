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
package pl.edu.icm.cermine.content.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class represents an immutable paragraph in a document and the in-text references it contains.
 *
 * @author Dominika Tkaczyk
 * @author Dominik Horb <cermine@dominik.berlin>
 */
public class DocumentParagraph {

    private final String text;
    private final DocumentContentStructure contentStructure;
    private final List<InTextReference> inTextReferences;

    public DocumentParagraph(String text, DocumentContentStructure contentStructure) {
        this.text = text;
        this.contentStructure = contentStructure;
        this.inTextReferences = new ArrayList<>();
    }

    public String getText() {
        return text;
    }

    public DocumentContentStructure getContentStructure() {
        return contentStructure;
    }

    /**
     * Adds a InTextReference to this paragraphs list if it is in range.
     *
     * @param inTextReference The in-text reference to add this paragraphs list.
     */
    public void addInTextReference(InTextReference inTextReference) {
        if (this.checkValidPosition(inTextReference)) {
            this.inTextReferences.add(inTextReference);
            Collections.sort(this.inTextReferences);
        }

        throw new IllegalArgumentException("The given in-text reference has an invalid position for this paragraph.");
    }
    
    public int getInTextReferenceCount(){
        return this.inTextReferences.size();
    }

    private boolean checkValidPosition(InTextReference inTextReference) {
        return inTextReference.getPosition() >= 0 && 
            inTextReference.getPosition() < this.text.length() - inTextReference.getLength();
    }

    /**
     * @return The list of registered in-text references of this paragraph as an umodifiable view.
     */
    public List<InTextReference> getInTextReferences() {
        return Collections.unmodifiableList(this.inTextReferences);
    }
}
