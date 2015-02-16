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
package pl.edu.icm.cermine.web.service;

import pl.edu.icm.cermine.web.service.model.ExtractionTask;
import pl.edu.icm.cermine.web.service.exceptions.NoSuchTaskException;
import java.util.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import pl.edu.icm.cermine.web.service.model.TaskStatus;

/**
 * Manages the storage of tasks in the session of the current user.
 *
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 * @author Dominik Horb <cermine@dominik.berlin>
 */
@Service
@Scope(value = "session")
public class TaskManagerImpl implements TaskManager{

    private int currentId;
    private final List<ExtractionTask> tasks;
    
    public TaskManagerImpl(){
        this.currentId = 0;
        this.tasks = new ArrayList<>();
    }
    
    @Override
    public ExtractionTask createTask(byte[] pdf, String fileName) {
        this.currentId++;
        ExtractionTask task = new ExtractionTask(this.currentId, pdf, fileName, TaskStatus.CREATED);
        tasks.add(task);
        return task;
    }

    @Override
    public ExtractionTask getTask(long id) throws NoSuchTaskException {
        for (ExtractionTask task : tasks) {
            if (task.getId() == id) {
                return task;
            }
        }
        throw new NoSuchTaskException(id);
    }

    @Override
    public List<ExtractionTask> getTaskList() {
        // unmodifiable list to make sure the interface of this class
        // is used to manipulate the list of registered tasks
        return Collections.unmodifiableList(this.tasks);
    }

    @Override
    public String getUniqueFilename(String filename) {
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
