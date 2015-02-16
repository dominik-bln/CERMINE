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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import pl.edu.icm.cermine.web.service.CermineExtractorService;
import pl.edu.icm.cermine.web.service.TaskManager;

/**
 * Gives access to common setup for controllers of the CERMINE application.
 *
 * @author Dominik Horb <cermine@dominik.berlin>
 */
public abstract class CermineController {

    @Autowired
    protected CermineExtractorService extractorService;
    @Autowired
    protected TaskManager taskManager;
    protected final Logger logger = LoggerFactory.getLogger(HtmlController.class);
}
