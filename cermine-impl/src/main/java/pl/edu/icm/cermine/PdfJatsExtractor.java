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

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jdom.Element;
import pl.edu.icm.cermine.bibref.model.BibEntry;
import pl.edu.icm.cermine.content.model.BxDocContentStructure;
import pl.edu.icm.cermine.content.model.DocumentContentStructure;
import pl.edu.icm.cermine.content.model.DocumentParagraph;
import pl.edu.icm.cermine.content.references.EndReferenceMatcher;
import pl.edu.icm.cermine.content.references.InTextReference;
import pl.edu.icm.cermine.content.references.InTextReferenceStyle;
import pl.edu.icm.cermine.content.references.InTextReferenceStyleClassifier;
import pl.edu.icm.cermine.exception.ReferenceTypeException;
import pl.edu.icm.cermine.content.transformers.BxContentStructToDocContentStructConverter;
import pl.edu.icm.cermine.content.transformers.DocContentStructToJatsBodyConverter;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.exception.CermineException;
import pl.edu.icm.cermine.metadata.model.DocumentAffiliation;
import pl.edu.icm.cermine.metadata.model.DocumentMetadata;
import pl.edu.icm.cermine.metadata.transformers.DocumentMetadataToNLMElementConverter;
import pl.edu.icm.cermine.structure.model.BxDocument;
import pl.edu.icm.cermine.tools.transformers.ModelToModelConverter;

/**
 * Extracts data from a given document and converts it into a JATS XML output format.
 *
 * @author Dominika Tkaczyk
 * @author Dominik Horb <cermine@dominik.berlin>
 */
public class PdfJatsExtractor extends AbstractExtractor<InputStream, Element> {

    // @todo bitmask for enabling different extraction stages
    // http://eddmann.com/posts/using-bit-flags-and-enumsets-in-java/

    private List<BibEntry> endReferences;
    private List<InTextReference> inTextReferences;

    public PdfJatsExtractor() throws AnalysisException {
        super();
    }

    /**
     * Extracts content from PDF file and converts it into JATS format.
     *
     * @param input The input stream of the PDF document.
     * @return The extracted content in JATS XML.
     * @throws AnalysisException
     */
    @Override
    public Element extract(InputStream input) throws CermineException {
        BxDocument document = this.extractBasicStructure(input);

        Element root = new Element("article");

        this.processFront(root, document);
        this.processBody(root, document);
        this.processBack(root, document);

        return root;
    }

    /**
     * Adds the JATS <front> element to the given root filled with data from the given document.
     *
     * @param root The root element of the JATS document.
     * @param document The document to convert into JATS.
     * @throws CermineException
     */
    private void processFront(Element root, BxDocument document) throws CermineException {
        Element meta = this.extractMetadata(document);
        Element metadata = (Element) meta.getChild("front").clone();
        root.addContent(metadata);
    }

    /**
     * Adds the JATS <body> element to the given root filled with data from the given document.
     *
     * @param root The root element of the JATS document.
     * @param document The document to convert into JATS.
     * @throws CermineException
     */
    private void processBody(Element root, BxDocument document) throws CermineException {
        DocumentContentStructure contentStructure = this.extractText(document);

        this.inTextReferences = this.extractInTextReferences(document, contentStructure);

        ModelToModelConverter<DocumentContentStructure, Element> converter = new DocContentStructToJatsBodyConverter();
        Element text = converter.convert(contentStructure);
        root.addContent(text);
    }

    /**
     * Extracts full text from document's box structure.
     *
     * @param document box structure
     * @return document's full text
     * @throws AnalysisException
     */
    private DocumentContentStructure extractText(BxDocument document) throws CermineException {
        BxDocument doc = config.contentFilter.filter(document);
        BxDocContentStructure tmpContentStructure = config.contentHeaderExtractor.extractHeaders(doc);
        config.contentCleaner.cleanupContent(tmpContentStructure);
        BxContentStructToDocContentStructConverter converter = new BxContentStructToDocContentStructConverter();
        return converter.convert(tmpContentStructure);
    }

    /**
     * Applies the configured processors to extract metadata of the document.
     *
     * @param document
     * @return A JATS <front> element containing the extracted metadata.
     * @throws CermineException In case processing fails.
     */
    private Element extractMetadata(BxDocument document) throws CermineException {
        BxDocument doc = config.metadataClassifier.classifyZones(document);
        DocumentMetadata metadata = config.metadataExtractor.extractMetadata(doc);

        // @todo does this actually do anything? 
        // Looks like something should be added after parsing
        for (DocumentAffiliation aff : metadata.getAffiliations()) {
            config.affiliationParser.parse(aff);
        }

        DocumentMetadataToNLMElementConverter converter = new DocumentMetadataToNLMElementConverter();

        return converter.convert(metadata);
    }

    /**
     * Adds the JATS <back> element to the given root filled with data from the given document.
     *
     * @param root The root element of the JATS document.
     * @param document The document to convert into JATS.
     * @throws CermineException
     */
    private void processBack(Element root, BxDocument document) throws AnalysisException {
        Element back = new Element("back");
        root.addContent(back);

        Element refList = new Element("ref-list");
        back.addContent(refList);

        List<BibEntry> bibReferences = this.getEndReferences(document);

        Element[] references = ExtractionUtils.convertReferences(bibReferences.toArray(new BibEntry[0]));
        this.addEndReferenceElements(refList, references);
    }

    /**
     * Attempts to extract the in-text references.
     */
    private List<InTextReference> extractInTextReferences(BxDocument document, DocumentContentStructure documentStructure) throws AnalysisException, ReferenceTypeException {
        List<DocumentParagraph> paragraphs = documentStructure.getAllParagraphs();

        InTextReferenceStyleClassifier styleClassifier = new InTextReferenceStyleClassifier();
        InTextReferenceStyle inTextStyle = styleClassifier.classify(documentStructure);

        Pattern bracketContentPattern = Pattern.compile("\\" + inTextStyle.getBracketType().getOpeningBracket() + "(.*?)\\" + inTextStyle.getBracketType().getClosingBracket());

        Matcher matcher;

        List<InTextReference> possibleReferences = new ArrayList<>();
        InTextReference currentPossibility;

        for (DocumentParagraph paragraph : paragraphs) {
            matcher = bracketContentPattern.matcher(paragraph.getText());
            while (matcher.find()) {
                // +1/-1 to don't get the bracket but the start/end of the contained string
                currentPossibility = new InTextReference(paragraph, matcher.start() + 1, matcher.end() - 1, inTextStyle);
                possibleReferences.add(currentPossibility);
            }
        }

        return this.filterInTextReferences(inTextStyle, possibleReferences, this.getEndReferences(document));
    }

    private List<InTextReference> filterInTextReferences(InTextReferenceStyle referenceStyle, List<InTextReference> possibleReferences, List<BibEntry> endReferences) throws ReferenceTypeException {
        EndReferenceMatcher referenceMatcher = EndReferenceMatcher.create(referenceStyle.getInTextReferenceType(), endReferences);

        List<InTextReference> actualReferences = new ArrayList<>();

        for (InTextReference reference : possibleReferences) {
            List<BibEntry> matchingEndReferences = referenceMatcher.match(reference);
            if (!matchingEndReferences.isEmpty()) {
                reference.setEndReferences(matchingEndReferences);
                reference.getParentParagraph().addInTextReference(reference);
                actualReferences.add(reference);
            }
        }

        return actualReferences;
    }

    public List<InTextReference> getInTextReferences() {
        return this.inTextReferences;
    }

    /**
     * @return The list of end references found in the given document.
     */
    private List<BibEntry> getEndReferences(BxDocument document) throws AnalysisException {
        if (this.endReferences == null) {
            this.endReferences = new ArrayList<>();
            String[] refs = config.bibReferenceExtractor.extractBibReferences(document);

            for (String currentReferenceString : refs) {
                BibEntry currentReference = config.bibReferenceParser.parseBibReference(currentReferenceString);
                // @todo this should probably happen at extraction stage
                currentReference.setId("R" + (this.endReferences.size() + 1));
                this.endReferences.add(currentReference);
            }
        }
        return this.endReferences;
    }

    /**
     * Creates JATS elements from the given references and adds them to the given element.
     *
     * @todo remove code duplication between ContentExtractor and PdfNlmContentExtractor
     */
    private void addEndReferenceElements(Element refListElement, Element[] references) {
        Element refElement;
        for (int i = 0; i < references.length; i++) {
            refElement = new Element("ref");
            refElement.setAttribute("id", "R" + (i + 1));
            refElement.addContent(references[i]);
            refListElement.addContent(refElement);
        }
    }
}
