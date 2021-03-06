/**
 * This file is part of CERMINE project. 
 * Copyright (c) 2011-2013 ICM-UW
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
package pl.edu.icm.cermine.web.service.model;

import java.util.Date;
import org.slf4j.LoggerFactory;

/**
 * This class represents the result of the extraction done on a PDF.
 * 
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 */
public class ExtractionResult {

    private final Date submit;
    private Date processingStart;
    private Date processingEnd;
    
    private boolean succeeded = true;
    private String jats = "";
    private String inTextReferences = "";
    private String html = "";
    private ArticleMeta meta;
    private Throwable error;
   
    public ExtractionResult(){
        this.submit = new Date();
    }
    
    public Date getSubmit() {
        return submit;
    }

    public void setProcessingStart(Date processingStart) {
        this.processingStart = processingStart;
    }

    public void setProcessingEnd(Date processingEnd) {
        this.processingEnd = processingEnd;
    }

    public String getNlm() {
        return jats;
    }

    public void setNlm(String nlm) {
        this.jats = nlm;
    }

    public ArticleMeta getMeta() {
        return meta;
    }

    public void setMeta(ArticleMeta meta) {
        this.meta = meta;
    }

    public String getInTextReferences() {
        return this.inTextReferences;
    }

    public void setInTextReferences(String inTextReferences) {
        this.inTextReferences = inTextReferences;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public void setSucceeded(boolean succeeded) {
        this.succeeded = succeeded;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public double getQueueTimeSec() {
        return (processingStart.getTime() - submit.getTime()) / 1000.;
    }

    public double getProcessingTimeSec() {
        return (processingEnd.getTime() - processingStart.getTime()) / 1000.;
    }

    public String getErrorMessage() {
        String res = "Unknown error";
        if (error != null) {
            res = error.getMessage();
            if (res == null || res.isEmpty()) {
                res = "Exception is: " + error.getClass().toString();
            }
        } else {
            LoggerFactory.getLogger(ExtractionResult.class).warn("Unexpected question for error message while no exception. Wazzup?");
        }
        return res;
    }
}
