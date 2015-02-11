/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.edu.icm.cermine.content.model;

import pl.edu.icm.cermine.bibref.model.BibEntry;

/**
 * This class represents the starting
 *
 * @author Dominik Horb <cermine@dominik.berlin>
 */
public class InTextReference implements Comparable {

    private final int position;
    private final int length;
    private final DocumentParagraph parentParagraph;
    private final BibEntry endReference;

    public InTextReference(DocumentParagraph parentParagraph, int position, int length, BibEntry endReference) {
        this.length = length;
        this.position = position;
        this.endReference = endReference;
        this.parentParagraph = parentParagraph;
    }

    public int getLength() {
        return this.length;
    }

    public DocumentParagraph getParentParagraph() {
        return this.parentParagraph;
    }

    public BibEntry getEndReference() {
        return this.endReference;
    }

    public int getPosition() {
        return this.position;
    }

    @Override
    /**
     * Compares the
     *
     * Note: this class has a natural ordering that is inconsistent with equals.
     */
    public int compareTo(Object o) {
        if (o == null) {
            throw new NullPointerException();
        }

        if (o.getClass().equals(this.getClass())) {
            InTextReference compare = (InTextReference) o;
            if (this.getParentParagraph().equals(compare.getParentParagraph())) {
                return new Integer(this.getPosition()).compareTo(compare.getPosition());
            }
            throw new IllegalStateException("The two references are not from the same paragraph and can't be compared.");
        }

        throw new ClassCastException("The compared object doesn't have the correct type.");
    }
}
