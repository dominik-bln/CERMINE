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

import java.text.ParseException;
import pl.edu.icm.cermine.exception.ReferenceTypeException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import pl.edu.icm.cermine.bibref.model.BibEntry;

/**
 * An implementing class finds all end references that are likely referenced by the current in-text
 * reference.
 *
 * @author Dominik Horb <cermine@dominik.berlin>
 */
public abstract class EndReferenceMatcher {

    private List<BibEntry> endReferences;
    private InTextReferenceType inTextReferenceType;

    /**
     * Tries to find all end references that would fit the given possible reference.
     *
     * Returns an empty list if no matching references were found.
     * 
     * @param possibleReference A possible in-text reference.
     * @return The list of all matching end references for the given reference.
     * @throws ReferenceTypeException In case the type of the in-text reference is not supported.
     */
    public final Set<BibEntry> match(InTextReference possibleReference) throws ReferenceTypeException {
        if (possibleReference.getInTextReferenceStyle().getInTextReferenceType() == this.inTextReferenceType) {
            try {
                return this.doMatching(possibleReference);
            } catch (ParseException ex) {
                return new HashSet<>();
            }
        }

        throw new ReferenceTypeException("The reference type of the given reference is not supported by this class.");
    }

    /**
     * Implementation of the actual matching.
     *
     * @param possibleReference A possible in-text reference.
     * @return The list of all matching end references for the given reference.
     * @throws java.text.ParseException Thrown when something happened during parsing
     */
    protected abstract Set<BibEntry> doMatching(InTextReference possibleReference) throws ParseException;

    /**
     * Creates an instance of an EndReferenceMatcher for the given reference type.
     *
     * @param referenceType The type to create a matcher for.
     * @param endReferences The end references that should be matched to.
     * @return An instance of a matcher.
     * @throws ReferenceTypeException In the case that no matcher for the given type could be
     * created.
     */
    public final static EndReferenceMatcher create(InTextReferenceType referenceType, List<BibEntry> endReferences) throws ReferenceTypeException {
        EndReferenceMatcher instance;
        switch (referenceType) {
            case NUMERIC:
                instance = new NumericEndReferenceMatcher();
                break;
            case NAME_YEAR:
                instance = new NameYearEndReferenceMatcher();
                break;
            default:
                throw new ReferenceTypeException("No implementation for the given reference type.");
        }

        instance.endReferences = Collections.unmodifiableList(endReferences);
        instance.inTextReferenceType = referenceType;
        return instance;
    }

    /**
     * 
     * 
     * @return 
     */
    protected List<BibEntry> getEndReferences() {
        return this.endReferences;
    }

    /**
     * Helper method to retrieve the content of the brackets in the possible reference from it's
     * parent paragraph.
     * 
     * @param possibleReference An assumed reference.
     * @return The content of the brackets of the given reference.
     */
    protected String retrieveReferenceContent(InTextReference possibleReference) {
        return possibleReference.getParentParagraph().getText().substring(possibleReference.getStartPosition(), possibleReference.getEndPosition());
    }

}
