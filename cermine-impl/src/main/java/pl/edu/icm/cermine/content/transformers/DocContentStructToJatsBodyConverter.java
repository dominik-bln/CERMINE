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
package pl.edu.icm.cermine.content.transformers;

import com.google.common.collect.Lists;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import pl.edu.icm.cermine.content.model.DocumentContentStructure;
import pl.edu.icm.cermine.content.model.DocumentHeading;
import pl.edu.icm.cermine.content.model.DocumentParagraph;
import pl.edu.icm.cermine.content.references.InTextReference;
import pl.edu.icm.cermine.exception.TransformationException;
import pl.edu.icm.cermine.tools.transformers.ModelToModelConverter;

/**
 * Writes DocumentContentStructure model to NLM format.
 *
 * @author Dominika Tkaczyk
 */
public class DocContentStructToJatsBodyConverter implements ModelToModelConverter<DocumentContentStructure, Element> {

    private int paragraphCount = 0;

    @Override
    public Element convert(DocumentContentStructure contentStructure, Object... hints) throws TransformationException {
        Element body = new Element("body");

        List<Element> articleJats;
        try {
            articleJats = this.toJats(contentStructure);
        } catch (JDOMException | IOException ex) {

            throw new TransformationException(ex);
        }

        body.addContent(articleJats);
        this.addSectionIds(body);
        return body;
    }

    private List<Element> toJats(DocumentContentStructure contentStructure) throws JDOMException, IOException {
        List<Element> elements = new ArrayList<>();

        if (contentStructure.getHeading() == null) {
            processAsDocumentPart(contentStructure, elements);
        } else {
            processAsSection(contentStructure, elements);
        }

        return elements;
    }

    private void processAsSection(DocumentContentStructure contentStructure, List<Element> elements) throws JDOMException, IOException {
        Element element = new Element("sec");
        element.addContent(this.headingToJats(contentStructure.getHeading()));

        for (DocumentParagraph paragraph : contentStructure.getParagraphs()) {
            element.addContent(paragraphToJats(paragraph));
        }

        for (DocumentContentStructure part : contentStructure.getParts()) {
            element.addContent(toJats(part));
        }
        elements.add(element);
    }

    private void processAsDocumentPart(DocumentContentStructure contentStructure, List<Element> elements) throws JDOMException, IOException {
        for (DocumentContentStructure part : contentStructure.getParts()) {
            elements.addAll(this.toJats(part));
        }
    }

    private Element headingToJats(DocumentHeading header) {
        Element element = new Element("title");
        element.setText(header.getText() + "\n");
        return element;
    }

    private Element paragraphToJats(DocumentParagraph paragraph) throws JDOMException, IOException {
        this.paragraphCount++;
        Element textWithXRef = this.addXRefElements(paragraph);

        return textWithXRef;
    }

    private Element addXRefElements(DocumentParagraph paragraph) {
        StringBuilder adaptedText = new StringBuilder(paragraph.getText());

        // move through the original text in reverse so that the indices to insert elements
        // don't shift
        for (InTextReference reference : Lists.reverse(paragraph.getInTextReferences())) {
            adaptedText.insert(reference.getPosition() + reference.getLength() - 1, "</xref>");
            adaptedText.insert(reference.getPosition() + 1, "<xref ref-type='bibr' rid=''>");
        }

        adaptedText.insert(0, "\n<p>");
        adaptedText.append("</p>");

        SAXBuilder builder = new SAXBuilder();
        try {
            Document doc;
            doc = builder.build(new ByteArrayInputStream(adaptedText.toString().getBytes("UTF-8")));
            Element element = doc.getRootElement();
            return (Element) element.detach();
        } catch (JDOMException | IOException ex) {
            System.out.println(ex.getMessage());
        }

        Element element = new Element("p");
        return element;
    }

    private void addSectionIds(Element element) {
        List<Element> sections = element.getChildren("sec");
        int index = 1;
        for (Element section : sections) {
            addSectionIds(section, "", index++);
        }
    }

    private void addSectionIds(Element element, String prefix, int index) {
        if (!prefix.isEmpty()) {
            prefix += "-";
        }
        String id = prefix + index;
        element.setAttribute("id", id);
        List<Element> sections = element.getChildren("sec");
        int i = 1;
        for (Element section : sections) {
            addSectionIds(section, id, i++);
        }
    }

    @Override
    public List<Element> convertAll(List<DocumentContentStructure> source, Object... hints) throws TransformationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
