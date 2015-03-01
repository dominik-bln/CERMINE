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
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import pl.edu.icm.cermine.content.model.DocumentContentStructure;
import pl.edu.icm.cermine.content.model.DocumentHeading;
import pl.edu.icm.cermine.content.model.DocumentParagraph;
import pl.edu.icm.cermine.exception.TransformationException;
import pl.edu.icm.cermine.tools.transformers.FormatToModelReader;

/**
 * Reads DocumentContentStructure model from HTML format.
 *
 * @author Dominika Tkaczyk
 */
public class HTMLToDocContentStructReader implements FormatToModelReader<DocumentContentStructure> {

    @Override
    public DocumentContentStructure read(String string, Object... hints) throws TransformationException {
        return read(new StringReader(string), hints);
    }

    @Override
    public DocumentContentStructure read(Reader reader, Object... hints) throws TransformationException {
        try {
            Element root = getRoot(reader);
            
            DocumentContentStructure dcs = createDocContentStruct(root.getChildren(), 0);
            dcs.setParents();
            
            return dcs;
        } catch (JDOMException | IOException ex) {
            throw new TransformationException(ex);
        }
    }
   
    private Element getRoot(Reader reader) throws JDOMException, IOException {
        SAXBuilder saxBuilder = new SAXBuilder("org.apache.xerces.parsers.SAXParser");
        org.jdom.Document dom = saxBuilder.build(reader);
        return dom.getRootElement();
    }
    
    private DocumentContentStructure createDocContentStruct(List<Element> elements, int level) {
        DocumentContentStructure dcs = new DocumentContentStructure();
        
        if (elements.isEmpty()) {
            return dcs;
        }
        
        int index = 0;
        Element current = elements.get(0);
        
        if (level > 0 && isHeader(current)) {
            DocumentHeading header = new DocumentHeading(level, current.getValue(), dcs);
            dcs.setHeading(header);
            current = getNext(++index, elements);
        }
        
        while (current != null && isParagraph(current)) {
            DocumentParagraph paragraph = new DocumentParagraph(current.getValue(), dcs);
            dcs.addParagraph(paragraph);
            current = getNext(++index, elements);
        }
        
        if (current == null) {
            return dcs;
        }
        
        int nextLevel = getHeaderLevel(current);
        List<Element> els = new ArrayList<>();
        while (current != null) {
            if (isHeader(current) && nextLevel == getHeaderLevel(current) && !els.isEmpty()) {
                DocumentContentStructure n = createDocContentStruct(els, level+1);
                dcs.addPart(n);
                els.clear();
            }
            els.add(current);
            current = getNext(++index, elements);
        }
        
        if (!els.isEmpty()) {
            DocumentContentStructure n = createDocContentStruct(els, level+1);
            dcs.addPart(n);
        }
        
        return dcs;
    }
    
    private boolean isHeader(Element element) {
        return element.getName().toLowerCase().startsWith("h");
    }
    
    private boolean isParagraph(Element element) {
        return element.getName().equals("p");
    }
    
    private int getHeaderLevel(Element element) {
        return Integer.parseInt(element.getName().replaceAll("[^0-9]+", ""));
    }
    
    private Element getNext(int index, List<Element> elements) {
        if (index < elements.size()) {
            return elements.get(index);
        }
        return null;
    }

    @Override
    public List<DocumentContentStructure> readAll(String string, Object... hints) throws TransformationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<DocumentContentStructure> readAll(Reader reader, Object... hints) throws TransformationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
