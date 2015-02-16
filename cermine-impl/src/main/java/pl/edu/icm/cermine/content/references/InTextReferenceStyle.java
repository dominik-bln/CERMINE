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
 * Represents the style of an in-text reference, i. e. some variation of [1] or (Shotton, 2009), 
 * by the brackets that are used and if it is numerical or in author-year style.
 * 
 * @author Dominik Horb <cermine@dominik.berlin>
 */
public class InTextReferenceStyle {

    private final BracketType bracketType;
    private final InTextReferenceType inTextReferenceType;
    
    public InTextReferenceStyle(BracketType bracketType, InTextReferenceType inTextReferenceType){
        this.bracketType = bracketType;
        this.inTextReferenceType = inTextReferenceType;
    }
    
    public BracketType getBracketType(){
        return this.bracketType;
    }
    
    public InTextReferenceType getInTextReferenceType(){
        return inTextReferenceType;
    }
}
