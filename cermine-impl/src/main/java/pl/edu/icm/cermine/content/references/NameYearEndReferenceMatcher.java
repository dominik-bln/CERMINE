/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.edu.icm.cermine.content.references;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pl.edu.icm.cermine.bibref.model.BibEntry;
import pl.edu.icm.cermine.content.model.DocumentParagraph;

/**
 * Tries to match the possible reference as a name-year reference, i. e. something like:
 *
 * <ul>
 * <li>(Author, 2015)</li>
 * <li>Author (2015)</li>
 * <li>(Author1, 2015; Author2, 2014)</li>
 * <ul>
 *
 * @author Dominik Horb <cermine@dominik.berlin>
 */
class NameYearEndReferenceMatcher extends EndReferenceMatcher {

    private final static Pattern YEAR_PATTERN;
    private final static int YEAR_PATTERN_FLAGS;

    private final static String[] NO_DATE = {"no date", "n. d.", "n.d.", "unknown date"};

    static {
        YEAR_PATTERN_FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        YEAR_PATTERN = Pattern.compile("(?:[^0-9]|^)(\\d{4}+)(?:[^0-9]|$)");
    }

    protected NameYearEndReferenceMatcher() {
    }

    @Override
    protected List<BibEntry> doMatching(InTextReference possibleReference) {
        String referenceContent = this.retrieveReferenceContent(possibleReference);

        List<Integer> years = this.extractYearsFromContent(referenceContent);
        List<BibEntry> matchingYears = this.findByYears(years);
        List<BibEntry> matchingNames = this.matchNames(possibleReference, matchingYears);

        return matchingNames;
    }

    private List<BibEntry> matchNames(InTextReference possibleReference, List<BibEntry> matchingYears) {
        List<BibEntry> matchingNames = new ArrayList<>();
        String searchableContent = this.findSearchableContent(possibleReference);

        Matcher matcher;
        Pattern pattern;

        for (BibEntry yearMatch : matchingYears) {
            List<String> authorValues = yearMatch.getAllFieldValues(BibEntry.FIELD_AUTHOR);
            for (String author : authorValues) {
                String lastname = author.split(",")[0];

                // only look from the last reference up to the end of
                // the current reference ) .. (author, 2014) ...
                pattern = Pattern.compile(Pattern.quote(lastname), YEAR_PATTERN_FLAGS);
                matcher = pattern.matcher(searchableContent);

                if (matcher.find()) {
                    matchingNames.add(yearMatch);
                }
            }
        }

        return matchingNames;
    }

    private String findSearchableContent(InTextReference possibleReference) {
        String paragraphContent = possibleReference.getParentParagraph().getText();
        int searchStart = this.findSearchStartIndex(possibleReference.getParentParagraph());
        int searchEnd = possibleReference.getEndPosition();
        return paragraphContent.substring(searchStart, searchEnd);
    }

    private int findSearchStartIndex(DocumentParagraph parentParagraph) {
        List<InTextReference> currentParagraphReferences = parentParagraph.getInTextReferences();
        int startIndex = 0;
        if (currentParagraphReferences.size() > 0) {
            // the assumption would be that the last added reference is right before the current possibility
            InTextReference priorReference = currentParagraphReferences.get(currentParagraphReferences.size()-1);
            startIndex = priorReference.getEndPosition();
        }

        return startIndex;
    }

    private List<Integer> extractYearsFromContent(String referenceContent) {
        List<Integer> extractedYears = new ArrayList<>();

        Matcher matcher = YEAR_PATTERN.matcher(referenceContent);

        while (matcher.find()) {
            extractedYears.add(Integer.parseInt(matcher.group(1)));
        }

        return extractedYears;
    }

    private List<BibEntry> findByYears(List<Integer> years) {
        List<BibEntry> foundEndReferences = new ArrayList<>();
        for (Integer year : years) {
            foundEndReferences.addAll(this.findByYear(year));
        }

        return foundEndReferences;
    }

    private List<BibEntry> findByYear(int year) {
        List<BibEntry> yearReferences = new ArrayList<>();

        for (BibEntry endReference : this.getEndReferences()) {
            if (endReference.getFirstFieldValue(BibEntry.FIELD_YEAR).equals(String.valueOf(year))) {
                yearReferences.add(endReference);
            }
        }

        return yearReferences;
    }

}
