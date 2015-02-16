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
package pl.edu.icm.cermine.web;

import java.io.ByteArrayInputStream;
import java.util.logging.Level;
import javax.servlet.http.HttpServletRequest;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import pl.edu.icm.cermine.bibref.CRFBibReferenceParser;
import pl.edu.icm.cermine.bibref.model.BibEntry;
import pl.edu.icm.cermine.bibref.transformers.BibEntryToNLMElementConverter;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.exception.TransformationException;
import pl.edu.icm.cermine.metadata.affiliation.CRFAffiliationParser;
import pl.edu.icm.cermine.web.service.model.ExtractionResult;
import pl.edu.icm.cermine.web.service.exceptions.ServiceException;

/**
 * Provides the RESTful interface of CERMINE.
 *
 * @author Dominik Horb <cermine@dominik.berlin>
 */
@Controller
//@RequestMapping("api")
public class RestController extends CermineController {

    @RequestMapping(value = "/extract.do", method = RequestMethod.POST)
    public ResponseEntity<String> extractSync(@RequestBody byte[] content,
        HttpServletRequest request,
        Model model) {
        try {
            logger.debug("content length: {}", content.length);

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.APPLICATION_XML);
            ExtractionResult result = extractorService.extractNLM(new ByteArrayInputStream(content));
            String nlm = result.getNlm();
            return new ResponseEntity<>(nlm, responseHeaders, HttpStatus.OK);
        } catch (AnalysisException | ServiceException ex) {
            java.util.logging.Logger.getLogger(HtmlController.class.getName()).log(Level.SEVERE, null, ex);
            return new ResponseEntity<>("Exception: " + ex.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/parse.do", method = RequestMethod.POST)
    public ResponseEntity<String> parseSync(HttpServletRequest request, Model model) {
        try {
            String refText = request.getParameter("reference");
            if (refText == null) {
                refText = request.getParameter("ref");
            }
            String affText = request.getParameter("affiliation");
            if (affText == null) {
                affText = request.getParameter("aff");
            }

            if (refText == null && affText == null) {
                return new ResponseEntity<>(
                    "Exception: \"reference\" or \"affiliation\" parameter has to be passed!\n", null,
                    HttpStatus.INTERNAL_SERVER_ERROR);
            }

            HttpHeaders responseHeaders = new HttpHeaders();
            String response;

            if (refText != null) {

                String format = request.getParameter("format");
                if (format == null) {
                    format = "bibtex";
                }
                format = format.toLowerCase();
                if (!format.equals("nlm") && !format.equals("bibtex")) {
                    return new ResponseEntity<>(
                        "Exception: format must be \"bibtex\" or \"nlm\"!\n", null,
                        HttpStatus.INTERNAL_SERVER_ERROR);
                }

                CRFBibReferenceParser parser = CRFBibReferenceParser.getInstance();
                BibEntry reference = parser.parseBibReference(refText);
                if (format.equals("bibtex")) {
                    responseHeaders.setContentType(MediaType.TEXT_PLAIN);
                    response = reference.toBibTeX();
                } else {
                    responseHeaders.setContentType(MediaType.APPLICATION_XML);
                    BibEntryToNLMElementConverter converter = new BibEntryToNLMElementConverter();
                    Element element = converter.convert(reference);
                    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
                    response = outputter.outputString(element);
                }
            } else {
                CRFAffiliationParser parser = new CRFAffiliationParser();
                Element parsedAff = parser.parse(affText);
                XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
                response = outputter.outputString(parsedAff);
            }

            return new ResponseEntity<>(response + "\n", responseHeaders, HttpStatus.OK);
        } catch (AnalysisException | TransformationException ex) {
            java.util.logging.Logger.getLogger(HtmlController.class.getName()).log(Level.SEVERE, null, ex);
            return new ResponseEntity<>("Exception: " + ex.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
