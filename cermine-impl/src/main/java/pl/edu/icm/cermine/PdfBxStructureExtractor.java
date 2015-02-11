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

import java.io.*;
import java.util.Collection;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.exception.TransformationException;
import pl.edu.icm.cermine.structure.model.BxDocument;
import pl.edu.icm.cermine.structure.transformers.BxDocumentToTrueVizWriter;

/**
 * Document geometric structure extractor. Extracts the geometric hierarchical structure
 * (pages, zones, lines, words and characters) from a PDF file and stores it as a BxDocument object.
 *
 * @author Dominika Tkaczyk
 */
public class PdfBxStructureExtractor {

    private ComponentConfiguration conf;

    public PdfBxStructureExtractor() throws AnalysisException {
        conf = new ComponentConfiguration();
    }
    
    /**
     * Extracts the geometric structure from a PDF file and stores it as BxDocument.
     * 
     * @param stream PDF stream
     * @return BxDocument object storing the geometric structure
     * @throws AnalysisException 
     */
    public BxDocument extractStructure(InputStream stream) throws AnalysisException {
        return ExtractionUtils.extractStructure(conf, stream);
    }

    public ComponentConfiguration getConf() {
        return conf;
    }

    public void setConf(ComponentConfiguration conf) {
        this.conf = conf;
    }
    
    public static void main(String[] args) throws ParseException, IOException {
        CommandLineOptionsParser parser = new CommandLineOptionsParser();
        if (!parser.parse(args)) {
            System.err.println(
                    "Usage: PdfBxStructureExtractor -path <path> [optional parameters]\n\n"
                  + "Tool for extracting structured content from PDF files.\n\n"
                  + "Arguments:\n"
                  + "  -path <path>              path to a PDF file or directory containing PDF files\n"
                  + "  -modelmeta <path>         (optional) the path to the metadata classifier model file\n"
                  + "  -modelinit <path>         (optional) the path to the initial classifier model file\n"
                  + "  -strext <extension>       (optional) the extension of the structure (TrueViz) file;\n"
                  + "                            default: \"cxml\"; used only if passed path is a directory\n"
                  + "  -threads <num>            number of threads for parallel processing\n");
            System.exit(1);
        }
        
        String path = parser.getPath();
        String strExtension = parser.getBxExtension();
        PdfNLMContentExtractor.THREADS_NUMBER = parser.getThreadsNumber();
 
        File file = new File(path);
        Collection<File> files = FileUtils.listFiles(file, new String[]{"pdf"}, true);
    
        int i = 0;
        for (File pdf : files) {
            File strF = new File(pdf.getPath().replaceAll("pdf$", strExtension));
            if (strF.exists()) {
                i++;
                continue;
            }
 
            long start = System.currentTimeMillis();
            float elapsed = 0;
            
            System.out.println(pdf.getPath());
 
            try {
                PdfBxStructureExtractor extractor = new PdfBxStructureExtractor();
                parser.updateMetadataModel(extractor.getConf());
                parser.updateInitialModel(extractor.getConf());

                InputStream in = new FileInputStream(pdf);
                BxDocument doc = ExtractionUtils.extractStructure(extractor.getConf(), in);
                doc = extractor.getConf().getMetadataClassifier().classifyZones(doc);

                long end = System.currentTimeMillis();
                elapsed = (end - start) / 1000F;
            
                BxDocumentToTrueVizWriter writer = new BxDocumentToTrueVizWriter();
                writer.write(new FileWriter(strF), doc.getPages());
            } catch (AnalysisException | TransformationException ex) {
               ex.printStackTrace();
            }
                
            i++;
            int percentage = i*100/files.size();
            if (elapsed == 0) {
                elapsed = (System.currentTimeMillis() - start) / 1000F;
            }
            System.out.println("Extraction time: " + Math.round(elapsed) + "s");
            System.out.println(percentage + "% done (" + i +" out of " + files.size() + ")");
            System.out.println("");
        }
    }
    
}