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

package pl.edu.icm.cermine.content.headings;

import java.io.IOException;
import java.util.List;
import libsvm.svm_parameter;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.exception.TransformationException;
import pl.edu.icm.cermine.structure.model.BxZoneLabel;
import pl.edu.icm.cermine.tools.classification.general.TrainingSample;
import pl.edu.icm.cermine.tools.classification.svm.SVMZoneClassifier;

/**
 *
 * @author Dominika Tkaczyk
 */
public final class SVMHeaderLinesTraining {
    
    public static final double BEST_GAMMA = 0.5;
    
    public static final double BEST_C = 32.0;
    
    public static void trainClassifier(List<TrainingSample<BxZoneLabel>> trainingElements, String output) 
            throws AnalysisException, IOException {
        SVMHeaderLinesClassifier contentFilter = new SVMHeaderLinesClassifier();
        svm_parameter param = SVMZoneClassifier.getDefaultParam();
        param.gamma = BEST_GAMMA;
        param.C = BEST_C;
        param.kernel_type = svm_parameter.RBF;
        
        contentFilter.setParameter(param);
        contentFilter.buildClassifier(trainingElements);
        contentFilter.saveModel(output);
    }
    
    public static void main(String[] args) throws AnalysisException, IOException, TransformationException {
        if (args.length < 2) {
            System.out.println("Usage: SVMContentHeaderTraining <input dir> <output>");
            return;
        }
        List<TrainingSample<BxZoneLabel>> trainingElements = HeaderExtractingTools.toTrainingSamples(args[0]);
        trainClassifier(trainingElements, args[1]);
    }

    private SVMHeaderLinesTraining() {
    }
   
}
