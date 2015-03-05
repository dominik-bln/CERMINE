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
 * Represents a type of bracket.
 * 
 * @note The countable ratios are "magical" for now but seem to make it possible to account for 
 * the usual frequency of the bracket types. A more exact version would be preferable however,
 * in case anyone got time to create a corpus of scientific documents and count the brackets.
 * 
 * @author Dominik Horb <cermine@dominik.berlin>
 */
public enum BracketType {

    SQUARE_BRACKETS('[', ']', 1.0), 
    PARENTHESES('(', ')', 0.1), 
    CURLY_BRACES('{', '}', 0.0),
    ANGLE_BRACKETS('<', '>', 0.0);
    
    private final char opening;
    private final char closing;
    private final double countableRatio;
    
    BracketType(char opening, char closing, double countableRatio){
        this.opening = opening;
        this.closing = closing;
        this.countableRatio = countableRatio;
    }
    
    public char getOpeningBracket(){
        return this.opening;
    }
    
    public char getClosingBracket(){
        return this.closing;
    }
    
    public double getCountableRatio(){
        return this.countableRatio;
    }
}
