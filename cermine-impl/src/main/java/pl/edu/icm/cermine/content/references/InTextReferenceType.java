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
package pl.edu.icm.cermine.content.references;

/**
 * A list of all different in-text reference styles.
 * 
 * Name-Year: (Author, 2015)
 * Numeric: [1]
 * 
 * Please note that the most important part about the type is the content of the bracket. Although
 * the above examples are the most commonly used bracket types, these could possibly differ.
 * 
 * @author Dominik Horb <cermine@dominik.berlin>
 */
public enum InTextReferenceType {

    NAME_YEAR, NUMERIC;
}
