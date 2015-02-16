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
package pl.edu.icm.cermine.web.service.model;

import java.util.Date;

/**
 * This class represents the task of an extraction with it's current state and results.
 * 
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 */
public class ExtractionTask {

    private final long id;
    private final String fileName;
    private final Date creationDate;
    private Date finishedDate;
    private byte[] pdf;
    private TaskStatus status;
    private ExtractionResult result;

    public ExtractionTask(long id, byte[] pdf, String fileName, TaskStatus status){
        this.id = id;
        this.pdf = pdf; 
        this.fileName = fileName;
        this.status = status;
        this.creationDate = new Date();
    }
    
    public long getId() {
        return this.id;
    }

    public String getFileName() {
        return this.fileName;
    }
    
    public byte[] getPdf() {
        return this.pdf;
    }

    public void setPdf(byte[] pdf) {
        this.pdf = pdf;
    }

    public TaskStatus getStatus() {
        return this.status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Date getCreationDate() {
        return this.creationDate;
    }
    
    public Date getFinishedDate(){
        return this.finishedDate;
    }

    public ExtractionResult getResult() {
        return this.result;
    }

    public void setResult(ExtractionResult result) {
        this.result = result;
        this.finishedDate = new Date();
    }
}
