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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import pl.edu.icm.cermine.content.model.DocumentContentStructure;

/**
 * Determines the most likely in-text reference style that is used.
 *
 * This class is counting the occurrences of the different bracket types and then continues to guess
 * the style depending on the content of the most frequent ones.
 *
 * @author Dominik Horb <cermine@dominik.berlin>
 */
public class InTextReferenceStyleClassifier {

    /**
     * Assumed minimum average length of name-year references to determine the referencing style.
     */
    public static int NAME_YEAR_THRESHOLD = 4;

    /**
     * Determines the in-text reference style for the given document.
     *
     * Currently only name-year and numerical types are taken into account, it is also based on the
     * assumption that the in-text references are in brackets. Classification will fail if for
     * example in-text references are given in superscript.
     *
     * @param documentStructure The document for which the in-text references should be determined.
     * @return The most likely in-text reference style.
     */
    public InTextReferenceStyle classify(DocumentContentStructure documentStructure) {
        String allText = this.concatenateParagraphs(documentStructure.getAllParagraphTexts());
        BracketType mostFrequentBracketType = this.findMostLikelyBracketType(allText);
        InTextReferenceType referenceType = this.findMostLikeylReferenceType(mostFrequentBracketType, allText);

        return new InTextReferenceStyle(mostFrequentBracketType, referenceType);
    }

    private String concatenateParagraphs(List<String> allParagraphs) {
        StringBuilder allText = new StringBuilder();
        for (String paragraph : allParagraphs) {
            allText.append(paragraph);
        }
        return allText.toString();
    }

    private BracketType findMostLikelyBracketType(String allText) {
        BracketType[] bracketTypes = BracketType.values();
        
        int currentMax = Integer.MIN_VALUE;
        int maxIndex = -1;
        int currentCount;

        for (int i = 0; i < bracketTypes.length; i++) {
            currentCount = StringUtils.countMatches(allText, String.valueOf(bracketTypes[i].getOpeningBracket()));
            if (currentCount * bracketTypes[i].getCountableRatio() > currentMax) {
                currentMax = currentCount;
                maxIndex = i;
            }
        }

        return bracketTypes[maxIndex];
    }

    /**
     * For now this method is based on the assumption that in a name-year style the reference texts
     * in the brackets will be most likely a bit longer than in a numerical style. A name-year
     * reference will always have at least 4 characters for the year, likely some more for author
     * and commas. It seems safe to assume that if the average is higher than 4, that we are dealing
     * with an author-year scheme.
     */
    private InTextReferenceType findMostLikeylReferenceType(BracketType bracketType, String allText) {
        List<String> matches = findAllBracketContents(bracketType, allText);

        int totalLengthOfMatches = 0;
        for (String match : matches) {
            totalLengthOfMatches += match.length() -2; // don't count the brackets
        }

        int average = totalLengthOfMatches / matches.size();
        if (average >= NAME_YEAR_THRESHOLD) {
            return InTextReferenceType.NAME_YEAR;
        }

        return InTextReferenceType.NUMERIC;
    }

    private List<String> findAllBracketContents(BracketType bracketType, String allText) {
        String patternString = "\\" + bracketType.getOpeningBracket() + "(.*?)\\" + bracketType.getClosingBracket();
        Pattern bracketContentPattern = Pattern.compile(patternString);

        Matcher matcher = bracketContentPattern.matcher(allText);
        List<String> matches = new ArrayList<>();
        while (matcher.find()) {
            matches.add(matcher.group());
        }

        return matches;
    }

}
