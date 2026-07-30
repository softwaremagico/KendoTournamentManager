package com.softwaremagico.kt.core.csv;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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

import com.softwaremagico.kt.core.exceptions.InvalidCsvFieldException;
import com.softwaremagico.kt.persistence.entities.Club;
import com.softwaremagico.kt.utils.StringUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

@Test(groups = "clubCsvTests")
public class ClubCsvTest {

    private static final String HEADER = "name;country;city;address;email;phone;web";

    private ClubCsv clubCsv;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        this.clubCsv = new ClubCsv();
    }

    @Test
    public void shouldReadSingleClub() {
        final String csv = HEADER + "\nMyClub;MyCountry;MyCity;MyAddress;my@email.com;123456;http://web.com";

        final List<Club> clubs = this.clubCsv.readCSV(csv);

        assertEquals(clubs.size(), 1);
        assertEquals(clubs.get(0).getName(), StringUtils.setCase("MyClub"));
        assertEquals(clubs.get(0).getCountry(), StringUtils.setCase("MyCountry"));
        assertEquals(clubs.get(0).getCity(), StringUtils.setCase("MyCity"));
        assertEquals(clubs.get(0).getAddress(), StringUtils.setCase("MyAddress"));
        assertEquals(clubs.get(0).getEmail(), "my@email.com");
        assertEquals(clubs.get(0).getPhone(), "123456");
        assertEquals(clubs.get(0).getWeb(), "http://web.com");
    }

    @Test
    public void shouldReadMultipleClubLines() {
        final String csv = HEADER + "\nClub0;C0;City0;Add0;e0@e.com;000;w0"
                + "\nClub1;C1;City1;Add1;e1@e.com;111;w1";

        final List<Club> clubs = this.clubCsv.readCSV(csv);

        assertEquals(clubs.size(), 2);
        assertEquals(clubs.get(0).getName(), "Club0");
        assertEquals(clubs.get(1).getName(), "Club1");
    }

    @Test
    public void shouldThrowExceptionOnInvalidHeader() {
        final String csv = "name;country;city;address;email;phone;invalid\nMyClub;C;City;Add;e@e.com;123;w";

        assertThrows(InvalidCsvFieldException.class, () -> this.clubCsv.readCSV(csv));
    }
}

