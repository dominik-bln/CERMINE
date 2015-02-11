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

import java.io.InputStream;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.service.exceptions.ServiceException;

/**
 * Abstraction of the service used to extract metadata from submitted files. The service is
 * responsible for extraction and proper scheduling and resource allocation.
 *
 * IMPORTANT: This interface needs to be present in order for spring autowire to work.
 *
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 */
public interface CermineExtractorService {

    /**
     * Method to extract metadata from the given pdf file. File is represented as a InputStream,
     * which will be passed to the extractor itself.
     *
     * @param ii
     * @return result of the extraction, including basic request stats.
     * @throws AnalysisException
     * @throws pl.edu.icm.cermine.service.exceptions.ServiceException
     */
    public ExtractionResult extractNLM(InputStream ii) throws AnalysisException, ServiceException;

    public long initExtractionTask(byte[] pdf, String fileName);
}
