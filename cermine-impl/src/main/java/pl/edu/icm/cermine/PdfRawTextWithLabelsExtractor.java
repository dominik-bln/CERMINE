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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.exception.CermineException;
import pl.edu.icm.cermine.structure.model.BxDocument;

/**
 * Text extractor from PDF files. Extracted text includes 
 * all text strings found in the document in correct reading order
 * as well as labels for text fragments.
 *
 * @author Dominika Tkaczyk
 */
public class PdfRawTextWithLabelsExtractor extends AbstractExtractor<InputStream, Element>{
    
    public PdfRawTextWithLabelsExtractor() throws AnalysisException {
        super();
    }
    
    
    @Override
    public Element extract(InputStream input) throws CermineException {
        return this.extractRawText(input);
    }
    
    /**
     * Extracts content of a PDF with labels.
     * 
     * @param stream input stream
     * @return pdf's content as plain text
     * @throws AnalysisException 
     */
    public Element extractRawText(InputStream stream) throws AnalysisException {
        return ExtractionUtils.extractRawTextWithLabels(config, stream);
    }
    
    /**
     * Extracts content of a PDF with labels.
     * 
     * @param document document's structure
     * @return pdf's content as plain text
     * @throws AnalysisException 
     */
    public Element extractRawText(BxDocument document) throws AnalysisException {
        return ExtractionUtils.extractRawTextWithLabels(config, document);
    }
    
    public static void main(String[] args) throws ParseException, IOException {
        CommandLineOptionsParser parser = new CommandLineOptionsParser();
        if (!parser.parse(args)) {
            System.err.println(
                    "Usage: PdfRawTextWithLabelsExtractor -path <path> [optional parameters]\n\n"
                  + "Tool for extracting labelled full text in the right reading order from PDF files.\n\n"
                  + "Arguments:\n"
                  + "  -path <path>              path to a PDF file or directory containing PDF files\n"
                  + "  -ext <extension>          (optional) the extension of the resulting text file;\n"
                  + "                            default: \"cermtxt\"; used only if passed path is a directory\n"
                  + "  -modelmeta <path>         (optional) the path to the metadata classifier model file\n"
                  + "  -modelinit <path>         (optional) the path to the initial classifier model file\n"
                  + "  -threads <num>            number of threads for parallel processing\n");
            System.exit(1);
        }
        
        String path = parser.getPath();
        String extension = parser.getTextExtension();
        Cermine.THREADS_NUMBER = parser.getThreadsNumber();
 
        File file = new File(path);
        if (file.isFile()) {
            try {
                PdfRawTextWithLabelsExtractor extractor = new PdfRawTextWithLabelsExtractor();
                parser.updateMetadataModel(extractor.getConfiguration());
                parser.updateInitialModel(extractor.getConfiguration());
                InputStream in = new FileInputStream(file);
                Element result = extractor.extractRawText(in);
                XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
                System.out.println(outputter.outputString(result));
            } catch (AnalysisException ex) {
                ex.printStackTrace(System.out);
            }
        } else {
        
            Collection<File> files = FileUtils.listFiles(file, new String[]{"pdf"}, true);
    
            int i = 0;
            for (File pdf : files) {
                File xmlF = new File(pdf.getPath().replaceAll("pdf$", extension));
                if (xmlF.exists()) {
                    i++;
                    continue;
                }
 
                long start = System.currentTimeMillis();
                float elapsed = 0;
            
                System.out.println(pdf.getPath());
 
                try {
                    PdfRawTextWithLabelsExtractor extractor = new PdfRawTextWithLabelsExtractor();
                    parser.updateMetadataModel(extractor.getConfiguration());
                    parser.updateInitialModel(extractor.getConfiguration());

                    InputStream in = new FileInputStream(pdf);
                    BxDocument doc = extractor.extractBasicStructure(in);
                    Element result = extractor.extractRawText(doc);

                    long end = System.currentTimeMillis();
                    elapsed = (end - start) / 1000F;
            
                    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
                    if (!xmlF.createNewFile()) {
                        System.out.println("Cannot create new file!");
                    }
                    FileUtils.writeStringToFile(xmlF, outputter.outputString(result));            
                } catch (CermineException ex) {
                   ex.printStackTrace(System.out);
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
    
}
