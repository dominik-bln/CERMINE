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
package pl.edu.icm.cermine.web.service.model;

import java.util.ArrayList;
import java.util.List;

public class ContributorMeta {
    private String name;
    private String givennames;
    private String surname;
    private final List<String> affiliations = new ArrayList<>();
    private final List<String> emails = new ArrayList<>();

    public ContributorMeta() {
    }

    public ContributorMeta(String name) {
        this.name = name;
    }

    public String getName() {
        if (name == null) {
            if (givennames == null) {
                return surname;
            } else if (surname == null) {
                return givennames;
            } else {
                return surname + ", " + givennames;
            }
        }
        return name;
    }

    public String getGivennames() {
        return givennames;
    }

    public void setGivennames(String givennames) {
        this.givennames = givennames;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getAffiliations() {
        return affiliations;
    }

    public void addAffiliations(String affiliation) {
        affiliations.add(affiliation);
    }

    public List<String> getEmails() {
        return emails;
    }

    public void addEmail(String email) {
        emails.add(email);
    }
}
