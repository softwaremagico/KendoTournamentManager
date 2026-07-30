package com.softwaremagico.kt.persistence.entities;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
 * %%
 * Copyright (C) 2021 - 2026 Softwaremagico
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

@Test(groups = "clubEntity")
public class ClubTest {

    @Test
    public void constructor_expectFieldsSetWithCaseNormalization() {
        final Club club = new Club("john's DOJO", "spain", "madrid");

        assertEquals(club.getName(), "John's Dojo");
        assertEquals(club.getCountry(), "Spain");
        assertEquals(club.getCity(), "Madrid");
        assertEquals(club.toString(), "John's Dojo");
    }

    @Test
    public void setRepresentative_withThreeArguments_expectAllFieldsSet() {
        final Club club = new Club("Dojo", "Spain", "Madrid");
        club.setRepresentative("ID1", "mail@example.com", "123456789");

        assertEquals(club.getRepresentativeId(), "ID1");
        assertEquals(club.getEmail(), "mail@example.com");
        assertEquals(club.getPhone(), "123456789");
    }

    @Test
    public void setRepresentative_withSingleArgument_expectOnlyIdSet() {
        final Club club = new Club("Dojo", "Spain", "Madrid");
        club.setRepresentative("ID2");

        assertEquals(club.getRepresentativeId(), "ID2");
    }

    @Test
    public void storeWeb_expectTrimmedValue() {
        final Club club = new Club("Dojo", "Spain", "Madrid");
        club.storeWeb("  https://example.com  ");

        assertEquals(club.getWeb(), "https://example.com");
    }

    @Test
    public void mailAccessors_expectDelegateToEmail() {
        final Club club = new Club("Dojo", "Spain", "Madrid");
        club.setMail("contact@example.com");

        assertEquals(club.getMail(), "contact@example.com");
        assertEquals(club.getEmail(), "contact@example.com");
    }

    @Test
    public void addressSetter_expectCaseNormalized() {
        final Club club = new Club("Dojo", "Spain", "Madrid");
        club.setAddress("MAIN street 12");

        assertEquals(club.getAddress(), "Main Street 12");
    }

    @Test
    public void compareTo_byNameThenCity_expectOrderedResult() {
        final Club alpha = new Club("Alpha", "Spain", "Madrid");
        final Club beta = new Club("Beta", "Spain", "Madrid");

        assertTrue(alpha.compareTo(beta) < 0);
        assertTrue(beta.compareTo(alpha) > 0);
    }

    @Test
    public void compareTo_ignoringCase_expectEqualOrdering() {
        final Club lower = new Club("kendo", "Spain", "alcala");
        final Club upper = new Club("KENDO", "Spain", "ALCALA");

        // Club names/cities go through StringUtils.setCase() (title-case) on assignment,
        // so this mainly verifies the Collator SECONDARY strength ignores residual case differences.
        assertEquals(lower.compareTo(upper), 0);
    }

    @Test
    public void equals_delegatesToElementEquals() {
        final Club club1 = new Club("Dojo", "Spain", "Madrid");
        club1.setId(1);
        final Club club2 = new Club("Other", "France", "Paris");
        club2.setId(1);

        assertEquals(club1, club2);
        assertEquals(club1.hashCode(), club2.hashCode());
    }

    @Test
    public void equals_withDistinctTransientInstances_expectFalse() {
        final Club club1 = new Club("Dojo", "Spain", "Madrid");
        final Club club2 = new Club("Dojo", "Spain", "Madrid");

        assertNotEquals(club1, club2);
    }

    @Test
    public void equals_withNull_expectFalse() {
        final Club club = new Club("Dojo", "Spain", "Madrid");
        assertFalse(club.equals(null));
    }
}

