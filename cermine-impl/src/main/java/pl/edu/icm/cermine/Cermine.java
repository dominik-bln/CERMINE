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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import pl.edu.icm.cermine.exception.CermineException;
import pl.edu.icm.cermine.structure.model.BxDocument;
import pl.edu.icm.cermine.structure.transformers.BxDocumentToTrueVizWriter;

/**
 * This class provides the general API and main class for interacting with CERMINE.
 *
 * @author Dominik Horb <cermine@dominik.berlin>
 */
public class Cermine {

    private static final int DEFAULT_THREAD_COUNT = 3;
    public static int THREADS_NUMBER = DEFAULT_THREAD_COUNT;

    public Cermine() {
        this(DEFAULT_THREAD_COUNT);
    }

    public Cermine(int threadCount) {
        
    }

    public static void main(String[] args) throws ParseException, IOException {
        CommandLineOptionsParser parser = new CommandLineOptionsParser();
        if (!parser.parse(args)) {
            Cermine.printHelp();
            System.exit(1);
        } else {
            Cermine cermine = new Cermine(parser.getThreadsNumber());
            cermine.processCommandLineCall(parser);
        }

    }
    
    private void processCommandLineCall(CommandLineOptionsParser parser) throws ParseException, IOException {
        File file = new File(parser.getPath());
        try {
            PdfJatsExtractor extractor = new PdfJatsExtractor();
            if (file.isFile()) {
                InputStream in = new FileInputStream(file);
                parser.updateMetadataModel(extractor.getConfiguration());
                parser.updateInitialModel(extractor.getConfiguration());

                Element result = extractor.extract(in);
                XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
                System.out.println(outputter.outputString(result));
            } else {

                Collection<File> files = FileUtils.listFiles(file, new String[]{"pdf"}, true);

                int i = 0;
                for (File pdf : files) {
                    long start = System.currentTimeMillis();

                    File xmlF = new File(pdf.getPath().replaceAll("pdf$", parser.getNLMExtension()));
                    if (xmlF.exists()) {
                        i++;
                        continue;
                    }

                    System.out.println(pdf.getPath());

                    InputStream in = new FileInputStream(pdf);
                    parser.updateMetadataModel(extractor.getConfiguration());
                    parser.updateInitialModel(extractor.getConfiguration());

                    Element result = extractor.extract(in);

                    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
                    if (!xmlF.createNewFile()) {
                        System.out.println("Cannot create new file!");
                    }
                    FileUtils.writeStringToFile(xmlF, outputter.outputString(result));

                    if (parser.extractStructure()) {
                        BxDocumentToTrueVizWriter writer = new BxDocumentToTrueVizWriter();
                        File strF = new File(pdf.getPath().replaceAll("pdf$", parser.getBxExtension()));

                        BxDocument doc = extractor.extractBasicStructure(in);
                        writer.write(new FileWriter(strF), doc.getPages());
                    }

                    long end = System.currentTimeMillis();

                    i++;
                    this.printStatistic(i, files.size(), end, start);
                }
            }
        } catch (CermineException ex) {
            ex.printStackTrace(System.out);
        }
    }

    private void printStatistic(int currentFile, int totalFiles, float end, float start) {
        float elapsed = (end - start) / 1000F;
        int percentage = currentFile * 100 / totalFiles;

        System.out.println("Extraction time: " + Math.round(elapsed) + "s");
        System.out.println(percentage + "% done (" + currentFile + " out of " + totalFiles + ")");
        System.out.println("");
    }

    private static void printHelp() {
        System.err.println(
            "Usage: Cermine -path <path> [optional parameters]\n\n"
            + "Tool for extracting metadata and content from PDF files.\n\n"
            + "Arguments:\n"
            + "  -path <path>              path to a PDF file or directory containing PDF files\n"
            + "  -ext <extension>          (optional) the extension of the resulting metadata file;\n"
            + "                            default: \"cermxml\"; used only if passed path is a directory\n"
            + "  -modelmeta <path>         (optional) the path to the metadata classifier model file\n"
            + "  -modelinit <path>         (optional) the path to the initial classifier model file\n"
            + "  -str                      whether to store structure (TrueViz) files as well;\n"
            + "                            used only if passed path is a directory\n"
            + "  -strext <extension>       (optional) the extension of the structure (TrueViz) file;\n"
            + "                            default: \"cxml\"; used only if passed path is a directory\n"
            + "  -threads <num>            number of threads for parallel processing\n");
    }
}
