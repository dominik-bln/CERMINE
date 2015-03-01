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

package pl.edu.icm.cermine.content.transformers;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import pl.edu.icm.cermine.exception.TransformationException;
import pl.edu.icm.cermine.tools.transformers.ModelToFormatWriter;

/**
 * Writes DocumentContentStructure model to NLM format.
 *
 * @author Dominika Tkaczyk
 */
public class NLMElementToHTMLWriter implements ModelToFormatWriter<Element> {

    @Override
    public String write(Element object, Object... hints) throws TransformationException {
        StringWriter sw = new StringWriter();
        write(sw, object, hints);
        return sw.toString();
    }

    @Override
    public void write(Writer writer, Element object, Object... hints) throws TransformationException {
        Element html = new Element("html");
        Element body = object.getChild("body");
        if (body != null) {
            List<Element> sections = body.getChildren("sec");
            for (Element section : sections) {
                for (Element el : toHTML(section, 1)) {
                    html.addContent(el);
                }
            }
        }
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        try {
            outputter.output(html, writer);
        } catch (IOException ex) {
            throw new TransformationException("", ex);
        }
    }
    
    private List<Element> toHTML(Element section, int level) {
        List<Element> elements = new ArrayList<>();
        List<Element> children = section.getChildren();
        for (Element child : children) {
            switch (child.getName()) {
                case "title":
                    Element element = new Element("H"+level);
                    element.setText(child.getText());
                    elements.add(element);
                    break;
                case "p":
                    Element el = new Element("p");
                    el.addContent(this.convertXRefToTooltipElements(child));
                    elements.add(el);
                    break;
                case "sec":
                    elements.addAll(toHTML(child, level+1));
                    break;
            }
        }
        return elements;
    }
    
    private Element convertXRefToTooltipElements(Element element) {
        Element copyToAdd = (Element) element.clone();
        List<Element> xrefChildren = copyToAdd.getChildren("xref");

        for(Element xref : xrefChildren){
            xref.setName("em");
            xref.removeAttribute("rid");
            xref.removeAttribute("ref-type");
            xref.setAttribute("title", xref.getAttributeValue("alt"));
            xref.setAttribute("class", "in-text-reference");
            
        }

        return copyToAdd;
    }

    @Override
    public String writeAll(List<Element> objects, Object... hints) throws TransformationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void writeAll(Writer writer, List<Element> objects, Object... hints) throws TransformationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
