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
package pl.edu.icm.cermine;

import java.io.InputStream;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.exception.CermineException;
import pl.edu.icm.cermine.structure.model.BxDocument;

/**
 * Provides a common interface and functionality for all extractor classes.
 * 
 * An implementing class should have the purpose of extracting exactly from one input format
 * into one output format.
 * 
 * @param <InputType>
 * @param <OutputType>
 * 
 * @author Dominik Horb <cermine@dominik.berlin>
 */
public abstract class AbstractExtractor<InputType, OutputType> {

    protected ComponentConfiguration configuration = new ComponentConfiguration();

    public AbstractExtractor() throws AnalysisException {
        this.configuration = new ComponentConfiguration();
    }

    public ComponentConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ComponentConfiguration conf) {
        this.configuration = conf;
    }
    
    /**
     * Performs the task of extracting data from the given InputType and answers with the 
     * defined OutputType.
     * 
     * @param input An initial representation of the input.
     * @return A representation of the input in the OutputType format.
     * @throws CermineException 
     */
    public abstract OutputType extract(InputType input) throws CermineException;
    
    /**
     * Creates an initial document from the given input stream.
     * 
     * @param stream
     * @return The representation of the given input stream.
     * @throws CermineException 
     * 
     * @todo this shouldn't stay here, but for now during refactoring it makes sense
     */
    protected BxDocument extractBasicStructure(InputStream stream) throws CermineException{
        BxDocument doc = configuration.characterExtractor.extractCharacters(stream);
        doc = configuration.documentSegmenter.segmentDocument(doc);
        doc = configuration.readingOrderResolver.resolve(doc);
        return configuration.initialClassifier.classifyZones(doc);
    }
}
