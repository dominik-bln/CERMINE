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
        this.processSingleReferences(contentToProcess, matchingReferences);

        return matchingReferences;
    }

    private String processRanges(String contentToProcess, Set<BibEntry> matches) throws ParseException {
        Matcher matcher = RANGE_PATTERN.matcher(contentToProcess);

        Matcher hyphenMatcher;
        String currentFind;
        int hyphenIndex, rangeStart, rangeEnd;
        while (matcher.find()) {
            currentFind = matcher.group();
            hyphenMatcher = HYPHEN_PATTERN.matcher(currentFind);
            hyphenMatcher.find();
            hyphenIndex = hyphenMatcher.start();
            rangeStart = Integer.parseInt(currentFind.substring(0, hyphenIndex).trim());
            rangeEnd = Integer.parseInt(currentFind.substring(hyphenIndex + 1, currentFind.length()).trim());

            if (rangeStart < rangeEnd) {
                for (int i = rangeStart; i <= rangeEnd; i++) {
                    this.addSingleMatch(i, matches);
                }
            } else {
                throw new ParseException("Problem while parsing a range", matcher.regionStart());
            }
        }

        return matcher.replaceAll("");
    }

    private void processSingleReferences(String contentToProcess, Set<BibEntry> matches) throws ParseException {
        Matcher matcher = NUMBER_PATTERN.matcher(contentToProcess);

        int currentFind;
        while (matcher.find()) {
            currentFind = Integer.parseInt(matcher.group());
            this.addSingleMatch(currentFind, matches);
        }

    }

    private void addSingleMatch(int match, Set<BibEntry> matches) throws ParseException {
        if (this.getEndReferences().size() >= match) {
            // -1 to account for zero based collection
            matches.add(this.getEndReferences().get(match - 1));
        } else {
            throw new ParseException("No reference for this match.", 0);
        }
    }

}
