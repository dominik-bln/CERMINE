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

package pl.edu.icm.cermine;

import java.io.InputStream;
import org.jdom.Element;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.exception.CermineException;
import pl.edu.icm.cermine.exception.TransformationException;
import pl.edu.icm.cermine.metadata.transformers.DocumentMetadataToNLMElementConverter;
import pl.edu.icm.cermine.structure.model.BxDocument;


/**
 * NLM-based metadata extractor from PDF files.
 *
 * @author Dominika Tkaczyk
 */
public class PdfNLMMetadataExtractor extends AbstractExtractor<InputStream, Element>{
    
    public PdfNLMMetadataExtractor() throws AnalysisException {
        super();
    }
    
    /**
     * Extracts NLM metadata from input stream.
     * 
     * @param input PDF stream
     * @return document's metadata in NLM format
     * @throws AnalysisException 
     * @throws TransformationException 
     */
    @Override
    public Element extract(InputStream input) throws CermineException {
        BxDocument doc = this.extractBasicStructure(input);
        DocumentMetadataToNLMElementConverter converter = new DocumentMetadataToNLMElementConverter();
        return converter.convert(ExtractionUtils.extractMetadata(this.config, doc));
    }
    
}