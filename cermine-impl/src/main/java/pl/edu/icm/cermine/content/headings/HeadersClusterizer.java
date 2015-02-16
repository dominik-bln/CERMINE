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

import pl.edu.icm.cermine.content.model.BxDocContentStructure;
import pl.edu.icm.cermine.structure.model.BxLine;
import pl.edu.icm.cermine.structure.model.BxPage;
import pl.edu.icm.cermine.tools.classification.clustering.Clusterizer;
import pl.edu.icm.cermine.tools.classification.clustering.FeatureVectorClusterizer;
import pl.edu.icm.cermine.tools.classification.clustering.SingleLinkageClusterizer;
import pl.edu.icm.cermine.tools.classification.features.FeatureVectorBuilder;
import pl.edu.icm.cermine.tools.classification.metrics.FeatureVectorDistanceMetric;
import pl.edu.icm.cermine.tools.classification.metrics.FeatureVectorEuclideanMetric;

/**
 *
 * @author Dominika Tkaczyk
 */
public class HeadersClusterizer {
    
    public static final double DEFAULT_MAX_HEADER_LEV_DIST = 1;
    
    private double maxHeaderLevelDistance = DEFAULT_MAX_HEADER_LEV_DIST;
    
    private FeatureVectorBuilder<BxLine, BxPage> vectorBuilder;
    
    private Clusterizer clusterizer;
    
    private FeatureVectorDistanceMetric metric;

    public HeadersClusterizer() {
        this.vectorBuilder = HeaderExtractingTools.CLUSTERING_VB;
        this.clusterizer = new SingleLinkageClusterizer();
        this.metric = new FeatureVectorEuclideanMetric();
    }
    
    public HeadersClusterizer(FeatureVectorBuilder<BxLine, BxPage> vectorBuilder, Clusterizer clusterizer, FeatureVectorDistanceMetric metric) {
        this.vectorBuilder = vectorBuilder;
        this.clusterizer = clusterizer;
        this.metric = metric;
    }
    
    
    public void clusterHeaders(BxDocContentStructure contentStructure) {
        FeatureVectorClusterizer fvClusterizer = new FeatureVectorClusterizer();
        fvClusterizer.setClusterizer(clusterizer);
        int[] clusters = fvClusterizer.clusterize(contentStructure.getFirstHeaderFeatureVectors(vectorBuilder), 
                vectorBuilder, metric, maxHeaderLevelDistance, true);
        contentStructure.setHeaderLevelIds(clusters);
    }

    public void setClusterizer(Clusterizer clusterizer) {
        this.clusterizer = clusterizer;
    }

    public void setMaxHeaderLevelDistance(double maxHeaderLevelDistance) {
        this.maxHeaderLevelDistance = maxHeaderLevelDistance;
    }

    public void setMetric(FeatureVectorDistanceMetric metric) {
        this.metric = metric;
    }

    public void setVectorBuilder(FeatureVectorBuilder<BxLine, BxPage> vectorBuilder) {
        this.vectorBuilder = vectorBuilder;
    }
    
}
