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

/**
 * Represents the status of a task.
 */
public enum TaskStatus {
    CREATED("queue", "SUBMITTED"),
    QUEUED("queue", "QUEUED"),
    PROCESSING("processing", "PROCESSING"),
    FINISHED("success", "SUCCESS", true),
    FAILED("failure", "FAILURE", true);
    
    private String css;
    private String text;
    private boolean finalState;

    TaskStatus(String css, String text) {
        this(css, text, false);
    }

    TaskStatus(String css, String text, boolean finalState) {
        this.css = css;
        this.text = text;
        this.finalState = finalState;
    }

    public String getCss() {
        return this.css;
    }

    public String getText() {
        return this.text;
    }

    public boolean getFinalState() {
        return this.finalState;
    }
}
