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

package pl.edu.icm.cermine.web.service;

import pl.edu.icm.cermine.web.service.model.ExtractionTask;
import java.util.List;
import pl.edu.icm.cermine.web.service.exceptions.NoSuchTaskException;

/**
 * An implementing class is able to manage tasks that need to be processed in some way.
 * 
 * IMPORTANT: This interface needs to be present in order for spring autowire to work.
 * 
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 * @author Dominik Horb <cermine@dominik.berlin>
 */
public interface TaskManager {

    /**
     * Retrieves the task with the given identifier.
     * 
     * @param id The id of the task.
     * @return The task with the given id if it was found.
     * @throws NoSuchTaskException Thrown if no task with the given id was found.
     */
    public ExtractionTask getTask(long id) throws NoSuchTaskException;

    /**
     * Creates and stores a task for later retrieval.
     * 
     * @param pdf The data of the PDF to store in the task.
     * @param fileName The filename of the PDF.
     * @return The this task is stored with.
     */
    public ExtractionTask createTask(byte[] pdf, String fileName);
    
    
    /**
     * Gives the list of all registered tasks.
     * 
     * @return The list of all registered tasks.
     */
    public List<ExtractionTask> getTaskList();

    /**
     * Checks the registered tasks and returns a filename that is unique among them.
     * 
     * How this is handled is up to the implementing class, simply enumerating files
     * with the same name should be sufficient.
     * 
     * @param filename The current filename.
     * @return A possibly altered version of the filename to ensure uniqueness.
     */
    public String getUniqueFilename(String filename);
}