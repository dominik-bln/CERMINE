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
package pl.edu.icm.cermine.content.references;

import java.text.ParseException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pl.edu.icm.cermine.bibref.model.BibEntry;

/**
 * Tries to match the possible reference as a name-year reference, i. e. something like:
 *
 * <ul>
 * <li>[1]</li>
 * <li>[1,3,5]</li>
 * <li>[1-7, 9]</li>
 * <ul>
 *
 * @author Dominik Horb <cermine@dominik.berlin>
 */
class NumericEndReferenceMatcher extends EndReferenceMatcher {

    /**
     * Heuristic (magic) number that denotes how much additional characters are allowed in a numeric
     * in-text reference before it is left out of consideration, i. e. [3-methyl-diphosphate] is left
     * out, although it contains the number 3.
     */
    private final static int LEFTOVER_THRESHOLD = 10;

    private final static Pattern RANGE_PATTERN;
    private final static Pattern NUMBER_PATTERN;
    private final static Pattern HYPHEN_PATTERN;

    static {
        RANGE_PATTERN = Pattern.compile("\\d+\\s*\\p{Pd}\\s*\\d+");
        NUMBER_PATTERN = Pattern.compile("\\d+");
        HYPHEN_PATTERN = Pattern.compile("\\p{Pd}");
    }

    protected NumericEndReferenceMatcher() {
    }

    @Override
    protected Set<BibEntry> doMatching(InTextReference possibleReference) throws ParseException {
        Set<BibEntry> matchingReferences = new HashSet<>();
        
        String contentToProcess = this.retrieveReferenceContent(possibleReference);
        contentToProcess = this.processRanges(contentToProcess, matchingReferences);
        contentToProcess = this.processSingleReferences(contentToProcess, matchingReferences);

        if (this.isOversteppingLeftoverThreshold(contentToProcess)) {
            return Collections.<BibEntry>emptySet();
        }
        return matchingReferences;
    }

    /**
     * Extracts ranges like 5-7 from the given input and adds the matches to the collection.
     * 
     * @param contentToProcess The string possibly containing a range.
     * @param matches The collection of matches the ranges should be added to.
     * @return The input string without the extracted ranges.
     * @throws ParseException In case something is wrong with a range (e. g. 5-3).
     */
    private String processRanges(String contentToProcess, Set<BibEntry> matches) throws ParseException {
        Matcher matcher = RANGE_PATTERN.matcher(contentToProcess);

        String currentFind;
        int hyphenIndex, rangeStart, rangeEnd;
        while (matcher.find()) {
            currentFind = matcher.group();

            hyphenIndex = this.findHyphenIndex(currentFind);
            rangeStart = this.findRangeStart(currentFind, hyphenIndex);
            rangeEnd = this.findRangeEnd(currentFind, hyphenIndex);

            if (rangeStart < rangeEnd) {
                addRangeMatches(rangeStart, rangeEnd, matches);
            } else {
                throw new ParseException("Problem while parsing a range", matcher.regionStart());
            }
        }

        return matcher.replaceAll("");
    }

    /**
     * Extracts all numbers from the input and tries to find the matching end reference to add to 
     * the collection of matches.
     * 
     * @param contentToProcess The string possibly containing a reference.
     * @param matches The collection of matches the ranges should be added to.
     * @return The input string without the extracted ranges.
     * @throws ParseException In case something is wrong with a found number.
     */
    private String processSingleReferences(String contentToProcess, Set<BibEntry> matches) throws ParseException {
        Matcher matcher = NUMBER_PATTERN.matcher(contentToProcess);

        int currentFind;
        while (matcher.find()) {
            currentFind = Integer.parseInt(matcher.group());
            this.addSingleMatch(currentFind, matches);
        }

        return matcher.replaceAll("");
    }

    private int findRangeStart(String currentFind, int hyphenIndex) {
        return this.prepareInt(currentFind.substring(0, hyphenIndex));
    }

    private int findRangeEnd(String currentFind, int hyphenIndex) {
        return this.prepareInt(currentFind.substring(hyphenIndex + 1, currentFind.length()));
    }

    private int prepareInt(String number) {
        return Integer.parseInt(number.trim());
    }

    private int findHyphenIndex(String currentFind) {
        Matcher hyphenMatcher = HYPHEN_PATTERN.matcher(currentFind);
        //there should be only one hyphen in there from the regex before, so no find loop needed
        hyphenMatcher.find();
        return hyphenMatcher.start();
    }

    private void addRangeMatches(int rangeStart, int rangeEnd, Set<BibEntry> matches) throws ParseException {
        for (int i = rangeStart; i <= rangeEnd; i++) {
            this.addSingleMatch(i, matches);
        }
    }

    private void addSingleMatch(int match, Set<BibEntry> matches) throws ParseException {
        if (this.getEndReferences().size() >= match) {
            // -1 to account for zero based collection and 1 based reference counting
            matches.add(this.getEndReferences().get(match - 1));
        } else {
            throw new ParseException("No reference for this match.", 0);
        }
    }

    private boolean isOversteppingLeftoverThreshold(String contentToProcess) {
        //remove commas as they could be leftover separators from actual references
        contentToProcess = contentToProcess.replaceAll(",", "");
        return contentToProcess.length() >= LEFTOVER_THRESHOLD;
    }

}
