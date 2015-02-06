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

import com.google.common.collect.Lists;
import java.io.*;
import java.util.Collection;
import java.util.List;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import pl.edu.icm.cermine.bibref.model.BibEntry;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.exception.TransformationException;
import pl.edu.icm.cermine.metadata.model.DocumentMetadata;
import pl.edu.icm.cermine.metadata.transformers.DocumentMetadataToNLMElementConverter;
import pl.edu.icm.cermine.structure.model.BxDocument;
import pl.edu.icm.cermine.structure.transformers.BxDocumentToTrueVizWriter;

/**
 * Content extractor from PDF files.
 * The extractor stores the results of the extraction in various formats.
 * The extraction process is performed only if the requested results 
 * is not available yet.
 *
 * @author Dominika Tkaczyk
 */
public class ContentExtractor {
    
    private ComponentConfiguration conf;
    
    /** input PDF file */
    private InputStream pdfFile;
    
    /** document's geometric structure */
    private BxDocument bxDocument;
    
    /** document's metadata */
    private DocumentMetadata metadata;
    
    /** document's metadata in NLM format */
    private Element nlmMetadata;
    
    /** document's list of references */
    private List<BibEntry> references;
    
    /** document's list of references in NLM format */
    private List<Element> nlmReferences;
    
    /** raw full text */
    private String rawFullText;
    
    /** full text in NLM format */
    private Element nlmFullText;
    
    /** extracted content in NLM format */
    private Element nlmContent;

    public ContentExtractor() throws AnalysisException {
        conf = new ComponentConfiguration();
    }

    /**
     * Stores the input PDF stream.
     * 
     * @param pdfFile PDF stream
     * @throws IOException 
     */
    public void uploadPDF(InputStream pdfFile) throws IOException {
        this.reset();
        this.pdfFile = pdfFile;
    }
    
    /**
     * Extracts geometric structure.
     * 
     * @return geometric structure
     * @throws AnalysisException 
     */
    public BxDocument getBxDocument() throws AnalysisException {
        if (pdfFile == null) {
            throw new AnalysisException("No PDF document uploaded!");
        }
        if (bxDocument == null) {
            bxDocument = ExtractionUtils.extractStructure(conf, pdfFile);
        }
        return bxDocument;
    }
    
    /**
     * Extracts the metadata.
     * 
     * @return the metadata
     * @throws AnalysisException 
     */
    public DocumentMetadata getMetadata() throws AnalysisException {
        if (metadata == null) {
            getBxDocument();
            metadata = ExtractionUtils.extractMetadata(conf, bxDocument);
        }
        return metadata;
    }
    
    /**
     * Extracts the metadata in NLM format.
     * 
     * @return the metadata in NLM format
     * @throws AnalysisException 
     */
    public Element getNLMMetadata() throws AnalysisException {
        try {
            if (nlmMetadata == null) {
                getMetadata();
                DocumentMetadataToNLMElementConverter converter = new DocumentMetadataToNLMElementConverter();
                nlmMetadata = converter.convert(metadata);
            }
            return nlmMetadata;
        } catch (TransformationException ex) {
            throw new AnalysisException("Cannot extract metadata!", ex);
        }
    }
    
    /**
     * Extracts the references.
     * 
     * @return the list of references
     * @throws AnalysisException 
     */
    public List<BibEntry> getReferences() throws AnalysisException {
        if (references == null) {
            getBxDocument();
            references = Lists.newArrayList(ExtractionUtils.extractReferences(conf, bxDocument));
        }
        return references;
    }
    
    /**
     * Extracts the references in NLM format.
     * 
     * @return the list of references
     * @throws AnalysisException 
     */
    public List<Element> getNLMReferences() throws AnalysisException {
        if (nlmReferences == null) {
            getReferences();
            nlmReferences = Lists.newArrayList(
                ExtractionUtils.convertReferences(references.toArray(new BibEntry[]{})));
        }
        return nlmReferences;
    }

    /**
     * Extracts raw text.
     * 
     * @return raw text
     * @throws AnalysisException 
     */
    public String getRawFullText() throws AnalysisException {
        if (rawFullText == null) {
            getBxDocument();
            rawFullText = ExtractionUtils.extractRawText(conf, bxDocument);
        }
        return rawFullText;
    }

    /**
     * Extracts full text.
     * 
     * @return full text in NLM format
     * @throws AnalysisException 
     */
    public Element getNLMText() throws AnalysisException {
        if (nlmFullText == null) {
            getBxDocument();
            nlmFullText = ExtractionUtils.extractTextAsNLM(conf, bxDocument);
        }
        return nlmFullText;
    }
    
    /**
     * Extracts full content in NLM format.
     * 
     * @return full content in NLM format
     * @throws AnalysisException 
     */
    public Element getNLMContent() throws AnalysisException {
        if (nlmContent == null) {
            getNLMMetadata();
            getNLMReferences();
            getNLMText();
            
            nlmContent = new Element("article");
            
            Element meta = (Element) nlmMetadata.getChild("front").clone();
            nlmContent.addContent(meta);
            
            nlmContent.addContent(nlmFullText);
            
            Element back = new Element("back");
            Element refList = new Element("ref-list");
            this.addRefElements(refList, nlmReferences.toArray(new Element[0]));
            
            back.addContent(refList);
            nlmContent.addContent(back);
        }
        return nlmContent;
    }
    
    /**
     * Takes the given array of references and adds them to the given list element.
     * @param refList
     * @param references 
     * 
     * @todo remove code duplication between ContentExtractor and PdfNlmContentExtractor
     */
    private void addRefElements(Element refList, Element[] references) {
        int index = 1;
        for (Element ref : references) {
            Element r = new Element("ref");
            r.setAttribute("id", "R" + index);
            r.addContent(ref);
            refList.addContent(r);
            index++;
        }
    }
    
    /**
     * Resets the extraction results.
     * 
     * @throws IOException 
     */
    public void reset() throws IOException {
        bxDocument = null;
        metadata = null;
        nlmMetadata = null;
        references = null;
        nlmReferences = null;
        rawFullText = null;
        nlmFullText = null;
        nlmContent = null;
        if (pdfFile != null) {
            pdfFile.close();
        }
        pdfFile = null;
    }

    public ComponentConfiguration getConf() {
        return conf;
    }

    public void setConf(ComponentConfiguration conf) {
        this.conf = conf;
    }
    
    public static void main(String[] args) throws ParseException, AnalysisException, IOException, TransformationException {
        CommandLineOptionsParser parser = new CommandLineOptionsParser();
        if (!parser.parse(args)) {
            System.err.println(
                    "Usage: ContentExtractor -path <path> [optional parameters]\n\n"
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
            System.exit(1);
        }
        
        String path = parser.getPath();
        String extension = parser.getNLMExtension();
        boolean extractStr = parser.extractStructure();
        String strExtension = parser.getBxExtension();
        PdfNLMContentExtractor.THREADS_NUMBER = parser.getThreadsNumber();
 
        File file = new File(path);
        if (file.isFile()) {
            try {
                ContentExtractor extractor = new ContentExtractor();
                parser.updateMetadataModel(extractor.getConf());
                parser.updateInitialModel(extractor.getConf());
                InputStream in = new FileInputStream(file);
                extractor.uploadPDF(in);
                Element result = extractor.getNLMContent();
                XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
                System.out.println(outputter.outputString(result));
            } catch (AnalysisException ex) {
                ex.printStackTrace();
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
                    ContentExtractor extractor = new ContentExtractor();
                    parser.updateMetadataModel(extractor.getConf());
                    parser.updateInitialModel(extractor.getConf());
                    InputStream in = new FileInputStream(pdf);
                    extractor.uploadPDF(in);
                    BxDocument doc = extractor.getBxDocument();
                    Element result = extractor.getNLMContent();

                    long end = System.currentTimeMillis();
                    elapsed = (end - start) / 1000F;
            
                    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
                    if (!xmlF.createNewFile()) {
                        System.out.println("Cannot create new file!");
                    }
                    FileUtils.writeStringToFile(xmlF, outputter.outputString(result));            
                
                    if (extractStr) {
                        BxDocumentToTrueVizWriter writer = new BxDocumentToTrueVizWriter();
                        File strF = new File(pdf.getPath().replaceAll("pdf$", strExtension));
                        writer.write(new FileWriter(strF), doc.getPages());
                    }
                } catch (AnalysisException ex) {
                   ex.printStackTrace();
                } catch (TransformationException ex) {
                   ex.printStackTrace();
                }
                
                i++;
                int percentage = i*100/files.size();
                System.out.println("Extraction time: " + Math.round(elapsed) + "s");
                System.out.println(percentage + "% done (" + i +" out of " + files.size() + ")");
                System.out.println("");
            }
        }
    }
    
}
