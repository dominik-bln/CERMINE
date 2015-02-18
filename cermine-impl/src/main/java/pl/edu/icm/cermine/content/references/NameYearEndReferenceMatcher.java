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

    private final static Pattern yearPattern;
    
    private final static String[] NO_DATE = {"no date", "n. d.", "n.d.", "unknown date"};
    
    static{
        yearPattern = Pattern.compile("(?:[^0-9]|^)(\\d{4}+)(?:[^0-9]|$)");
    }
    
    protected NameYearEndReferenceMatcher(){}
    
    @Override
    protected List<BibEntry> doMatching(InTextReference possibleReference) {
        String referenceContent = this.retrieveReferenceContent(possibleReference);
        
        List<Integer> years = this.extractYearsFromContent(referenceContent);
        List<BibEntry> matchingYears = this.findByYears(years);
        List<BibEntry> matchingNames = this.matchNames(referenceContent, matchingYears);
        
        return matchingNames;
    }
    
    private List<BibEntry> matchNames(String referenceContent, List<BibEntry> matchingYears){
        List<BibEntry> matchingNames = new ArrayList<>();
        
        for(BibEntry yearMatch : matchingYears){
            List<String> authorValues = yearMatch.getAllFieldValues(BibEntry.FIELD_AUTHOR);
            
            for(String author : authorValues){
                System.out.println(author);
            }
        }
        
        return matchingNames;
    }
    
    
    private List<Integer> extractYearsFromContent(String referenceContent){
        List<Integer> extractedYears = new ArrayList<>();
        
        Matcher matcher = yearPattern.matcher(referenceContent);
        
        while(matcher.find()){
            extractedYears.add(Integer.parseInt(matcher.group()));
        }
        
        return extractedYears;
    }
    
    
    private List<BibEntry> findByYears(List<Integer> years){
        List<BibEntry> foundEndReferences = new ArrayList<>();
        for(Integer year : years){
            foundEndReferences.addAll(this.findByYear(year));
        }
        
        return foundEndReferences;
    }
    
    private List<BibEntry> findByYear(int year){
        List<BibEntry> yearReferences = new ArrayList<>();
        
        for(BibEntry endReference : this.getEndReferences()){
            if(endReference.getFirstFieldValue(BibEntry.FIELD_YEAR).equals(String.valueOf(year))){
                yearReferences.add(endReference);
            }
        }
        
        return yearReferences;
    }

    
}
