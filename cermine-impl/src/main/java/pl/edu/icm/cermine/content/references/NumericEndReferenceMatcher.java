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
import java.util.ArrayList;
import java.util.List;
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

    private final static Pattern rangePattern;
    private final static Pattern numberPattern;

    static {
        rangePattern = Pattern.compile("\\d+\\s*-\\s*\\d");
        numberPattern = Pattern.compile("\\d+");
    }

    protected NumericEndReferenceMatcher() {
    }

    @Override
    protected List<BibEntry> doMatching(InTextReference possibleReference) throws ParseException {
        List<BibEntry> matchingReferences = new ArrayList<>();
        String contentToProcess = this.retrieveReferenceContent(possibleReference);

        contentToProcess = this.processRanges(contentToProcess, matchingReferences);
        this.processSingleReferences(contentToProcess, matchingReferences);

        return matchingReferences;
    }

    private String processRanges(String contentToProcess, List<BibEntry> listForMatches) throws ParseException {
        Matcher matcher = rangePattern.matcher(contentToProcess);

        String currentFind;
        int hyphenIndex, rangeStart, rangeEnd;
        while (matcher.find()) {
            currentFind = matcher.group();
            hyphenIndex = currentFind.indexOf("-");
            rangeStart = Integer.parseInt(currentFind.substring(0, hyphenIndex));
            rangeEnd = Integer.parseInt(currentFind.substring(hyphenIndex + 1, currentFind.length()));

            if (rangeStart < rangeEnd) {
                for (int i = rangeStart; i <= rangeEnd; i++) {
                    this.addSingleMatch(i, listForMatches);
                }
            } else {
                throw new ParseException("Problem while parsing a range", matcher.regionStart());
            }
        }

        return contentToProcess.replaceAll(rangePattern.pattern(), "");
    }

    private void processSingleReferences(String contentToProcess, List<BibEntry> listForMatches) throws ParseException {
        Matcher matcher = numberPattern.matcher(contentToProcess);

        int currentFind;
        while (matcher.find()) {
            currentFind = Integer.parseInt(matcher.group());
            this.addSingleMatch(currentFind, listForMatches);
        }

    }

    private void addSingleMatch(int match, List<BibEntry> listForMatches) throws ParseException {
        if (this.getEndReferences().size() >= match) {
            listForMatches.add(this.getEndReferences().get(match - 1));
        } else {
            throw new ParseException("No reference for this match.", 0);
        }
    }

}
