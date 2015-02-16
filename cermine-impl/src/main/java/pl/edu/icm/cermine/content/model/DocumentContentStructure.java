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

import java.util.ArrayList;
import java.util.List;

/**
 * A representation of the semantic structure of a document.
 * 
 * @author Dominika Tkaczyk
 */
public class DocumentContentStructure {

    private DocumentHeading header;
    private final List<DocumentParagraph> paragraphs;
    private final List<DocumentContentStructure> parts;
    private DocumentContentStructure parent;
    
    public DocumentContentStructure() {
        parts = new ArrayList<>();
        paragraphs = new ArrayList<>();
    }

    public DocumentContentStructure getParent() {
        return parent;
    }
    
    public void setParents() {
        for (DocumentContentStructure part : parts) {
            part.parent = this;
            part.setParents();
        }
    }
    
    public List<DocumentContentStructure> getParts() {
        return parts;
    }
    
    public void addPart(DocumentContentStructure part) {
        parts.add(part);
    }
    
    public DocumentHeading getHeading() {
        return header;
    }

    public void setHeading(DocumentHeading header) {
        this.header = header;
    }
    
    public List<String> getAllHeadingTexts() {
        List<String> headers = new ArrayList<>();
        if (header != null) {
            headers.add(header.getText());
        }
        for (DocumentContentStructure part : parts) {
            headers.addAll(part.getAllHeadingTexts());
        }
        return headers;
    }
    
    public int getAllHeadingsCount() {
        int sum = (header == null) ? 0 : 1;
        for (DocumentContentStructure part : parts) {
            sum += part.getAllHeadingsCount();
        }
        return sum;
    }
        
    public boolean containsHeadingText(String headerText) {
        if (header != null && headerText.equals(header.getText())) {
            return true;
        }
        for (DocumentContentStructure part : parts) {
            if (part.containsHeadingText(headerText)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsHeadingFirstLineText(String lineText) {
        if (header != null) {
            String[] lines = header.getText().split("\n");
            if (lineText.equals(lines[0])) {
                return true;
            }
        }
        for (DocumentContentStructure part : parts) {
            if (part.containsHeadingFirstLineText(lineText)) {
                return true;
            }
        }
        return false;
    }
  
    public DocumentHeading getPreviousHeading(DocumentHeading header) {
        DocumentContentStructure prev = null;
        for (DocumentContentStructure part : parts) {
            if (part.containsHeading(header)) {
                return prev == null ? null : prev.getHeading();
            }
            prev = part;
        }
        return null;
    }
    
    public boolean containsHeading(DocumentHeading header) {
        if (header.equals(this.header)) {
            return true;
        }
        for (DocumentContentStructure part : parts) {
            if (part.containsHeading(header)) {
                return true;
            }
        }
        return false;
    }

    public List<DocumentHeading> getHeadings() {
        List<DocumentHeading> headers = new ArrayList<>();
        if (header != null) {
            headers.add(header);
        }
        for (DocumentContentStructure part : parts) {
            headers.addAll(part.getHeadings());
        }
        return headers;
    }
    
    //paragraphs

    public List<DocumentParagraph> getParagraphs() {
        return paragraphs;
    }
    
    public List<String> getAllParagraphTexts() {
        List<String> pars = new ArrayList<>();
        for (DocumentParagraph p : paragraphs) {
            pars.add(p.getText());
        }

        for (DocumentContentStructure part : parts) {
            pars.addAll(part.getAllParagraphTexts());
        }
        return pars;
    }
    
    public List<DocumentParagraph> getAllParagraphs() {
        List<DocumentParagraph> pars = new ArrayList<>();
        if (paragraphs != null) {
            pars.addAll(this.paragraphs);
        }

        for (DocumentContentStructure part : parts) {
            pars.addAll(part.getAllParagraphs());
        }
        return pars;
    }
    
    public int getAllParagraphCount() {
        int sum = paragraphs.size();
        for (DocumentContentStructure part : parts) {
            sum += part.getAllParagraphCount();
        }
        return sum;
    }
    
    public void addParagraph(DocumentParagraph paragraph) {
        paragraphs.add(paragraph);
    }
    
    //printing

    public void printHeaders() {
        if (header != null) {
            for (int i = 1; i < header.getLevel(); i++) {
                System.out.print("\t");
            }
            System.out.println(header.getLevel() + " " + header.getText());
        }
        for (DocumentContentStructure dcp : parts) {
            dcp.printHeaders();
        }
        System.out.println("");
    }

    public void print() {
        if (header != null) {
            for (int i = 1; i < header.getLevel(); i++) {
                System.out.print("\t");
            }
            System.out.println(header.getLevel() + " " + header.getText());
        }
        System.out.println("");
        for (DocumentParagraph p : paragraphs) {
            System.out.println("[" + p.getText() + "]");
        }
        System.out.println("");
        for (DocumentContentStructure dcp : parts) {
            dcp.print();
        }
        System.out.println("");
    }
    
}
