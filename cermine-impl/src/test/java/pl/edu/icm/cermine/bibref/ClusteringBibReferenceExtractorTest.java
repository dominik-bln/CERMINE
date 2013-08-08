package pl.edu.icm.cermine.bibref;

import org.junit.Before;

/**
 *
 * @author Dominika Tkaczyk
 */
public class ClusteringBibReferenceExtractorTest extends AbstractBibReferenceExtractorTest {
    
    private BibReferenceExtractor extractor;
    
    @Before
    @Override
    public void setUp() {
        super.setUp();
        extractor = new ClusteringBibReferenceExtractor();
    }


    @Override
    protected BibReferenceExtractor getExtractor() {
        return extractor;
    }
}
