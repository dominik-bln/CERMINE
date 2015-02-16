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

import pl.edu.icm.cermine.web.service.model.ExtractionTask;
import pl.edu.icm.cermine.web.service.exceptions.NoSuchTaskException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

/**
 * Handles requests for pages in the CERMINE web interface.
 * 
 * @author bart
 * @author axnow
 */
@Controller
public class HtmlController extends CermineController {

    @RequestMapping(value = "/index.html")
    public String showHome(Model model) {
        return "home";
    }

    @RequestMapping(value = "/about.html")
    public String showAbout(Model model) {
        return "about";
    }

    @RequestMapping(value = "/download.html")
    public ResponseEntity<String> downloadXML(@RequestParam("task") long taskId,
        @RequestParam("type") String resultType, Model model) throws NoSuchTaskException {
        ExtractionTask task = taskManager.getTask(taskId);
        if ("nlm".equals(resultType)) {
            String nlm = task.getResult().getNlm();
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Content-Type", "application/xml;charset=utf-8");
            return new ResponseEntity<>(nlm, responseHeaders, HttpStatus.OK);
        } else {
            throw new RuntimeException("Unknown request type: " + resultType);
        }
    }

    /**
     * Checks if the requested example PDF exists and sends it back.
     * 
     * @param filename The name of the example file.
     * @param request The incoming request.
     * @param response The prepared response to send back to the user later on.
     */
    @RequestMapping(value = "/examplepdf.html", method = RequestMethod.GET)
    public void getExamplePDF(@RequestParam("file") String filename, HttpServletRequest request, HttpServletResponse response) {
        try (InputStream in = this.loadExampleFile(filename);
            OutputStream out = response.getOutputStream();) {
            
            response.setContentType("application/pdf");
            this.sendRequestedFile(in, out);
            
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private InputStream loadExampleFile(String filename) {
        if (filename.matches("^example\\d+\\.pdf$")) {
            return HtmlController.class.getResourceAsStream("/examples/" + filename);
        }
        throw new RuntimeException("No such example file!");
    }

    private void sendRequestedFile(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
    }

    @RequestMapping(value = "/uploadexample.do", method = RequestMethod.GET)
    public String uploadExampleFileStream(@RequestParam("file") String filename, HttpServletRequest request, Model model) {
        if (!filename.matches("^example\\d+\\.pdf$")) {
            throw new RuntimeException("No such example file!");
        }
        logger.info("Got an upload request.");
        try {
            InputStream in = HtmlController.class.getResourceAsStream("/examples/" + filename);
            if (in == null) {
                throw new RuntimeException("No such example file!");
            }

            byte[] content = IOUtils.toByteArray(in);
            if (content.length == 0) {
                model.addAttribute("warning", "An empty or no file sent.");
                return "home";
            }
            logger.debug("Original filename is: " + filename);
            filename = taskManager.getUniqueFilename(filename);
            logger.debug("Created filename: " + filename);
            long taskId = extractorService.initExtractionTask(content, filename);
            logger.debug("Task manager is: " + taskManager);
            return "redirect:/task.html?task=" + taskId;

        } catch (IOException | RuntimeException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Takes a file via POST and initiates an extraction task for it.
     *
     * @param file
     * @param request
     * @param model
     * @return A string that specifies the next page to load.
     */
    @RequestMapping(value = "/upload.do", method = RequestMethod.POST)
    public String uploadFileStream(@RequestParam("files") MultipartFile file, HttpServletRequest request, Model model) {
        logger.info("Got an upload request.");
        try {
            byte[] content = file.getBytes();
            if (content.length > 0) {
                String filename = file.getOriginalFilename();
                logger.debug("Original filename is: " + filename);
                filename = taskManager.getUniqueFilename(filename);
                logger.debug("Created filename: " + filename);
                long taskId = extractorService.initExtractionTask(content, filename);
                logger.debug("Task manager is: " + taskManager);

                return "redirect:/task.html?task=" + taskId;
            } else {
                model.addAttribute("warning", "An empty or no file sent.");
                return "home";
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Displays the results of a single task execution.
     *
     * @param id The ID of the task to display.
     * @return Data about the page to show.
     * @throws NoSuchTaskException If no task with the given ID is found.
     */
    @RequestMapping(value = "/task.html", method = RequestMethod.GET)
    public ModelAndView showTask(@RequestParam("task") long id) throws NoSuchTaskException {
        ExtractionTask task = taskManager.getTask(id);

        HashMap<String, Object> model = new HashMap<>();
        model.put("task", task);
        if (task.getStatus().getFinalState()) {
            model.put("result", task.getResult());
            model.put("meta", task.getResult().getMeta());
            model.put("inTextReferences", task.getResult().getInTextReferences());
            String nlmHtml = StringEscapeUtils.escapeHtml(task.getResult().getNlm());
            model.put("html", task.getResult().getHtml());
            model.put("nlm", nlmHtml);
        }
        return new ModelAndView("task", model);
    }

    /**
     * Displays the list of tasks that have been started during the current session.
     *
     * @return Data about the page to show.
     */
    @RequestMapping(value = "/tasks.html")
    public ModelAndView showTasks() {
        return new ModelAndView("tasks", "tasks", taskManager.getTaskList());
    }
    
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = NoSuchTaskException.class)
    public ModelAndView taskNotFoundHandler(NoSuchTaskException ex) {
        return new ModelAndView("error", "errorMessage", ex.getMessage());
    }
}
