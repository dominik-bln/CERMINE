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
package pl.edu.icm.cermine.service;

import pl.edu.icm.cermine.service.exceptions.NoSuchTaskException;
import java.util.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Manages the storage of tasks in the session of the current user.
 *
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 * @author Dominik Horb <cermine@dominik.berlin>
 */
@Service
@Scope(value = "session")
public class TaskManagerImpl implements TaskManager{

    private static int currentId = 1;
    private final List<ExtractionTask> tasks = new ArrayList<>();
    
    public TaskManagerImpl(){
        
    }
    
    /**
     * Stores the given task in the list of tasks.
     *
     * @param task The task to register.
     * @return The id the task was stored under.
     */
    @Override
    public long registerTask(ExtractionTask task) {
        if (task.getId() == 0) {
            task.setId(currentId);
            currentId++;
        }
        tasks.add(task);
        return task.getId();
    }

    /**
     * Finds the task with the given ID.
     *
     * @param id The ID of the task to find.
     * @return The task with the matching ID.
     * @throws NoSuchTaskException
     */
    @Override
    public ExtractionTask getTask(long id) throws NoSuchTaskException {
        for (ExtractionTask task : tasks) {
            if (task.getId() == id) {
                return task;
            }
        }
        throw new NoSuchTaskException(id);
    }

    /**
     * Returns a list of all registered tasks.
     *
     * @return The list of registered tasks in the order they have been registered.
     */
    @Override
    public List<ExtractionTask> taskList() {
        return Collections.unmodifiableList(this.tasks);
    }

    /**
     * Generates a unique filename in case another file with the same name was already uploaded
     *
     * @param filename The original filename.
     * @return A unique version of the given filename.
     */
    @Override
    public String getProperFilename(String filename) {
        String fbase = filename;
        if (filename == null || filename.isEmpty()) {
            fbase = "input.pdf";
        }

        int sameName = this.countTasksWithSameFilename(fbase);
        if (sameName > 0) {
            return fbase + "#" + sameName;
        }
        return fbase;
    }

    private int countTasksWithSameFilename(String fbase) {
        int sameName = 0;
        for (ExtractionTask task : tasks) {
            if (fbase.equals(task.getFileName())) {
                sameName++;
            }
        }

        return sameName;
    }
}
