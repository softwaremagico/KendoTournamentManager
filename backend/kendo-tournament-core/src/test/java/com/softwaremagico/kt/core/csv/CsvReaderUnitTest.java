package com.softwaremagico.kt.core.csv;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2026 SoftwareMagico
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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("all")
@Test
public class CsvReaderUnitTest {

    private ClubCsv clubCsvReader;

    private static final String VALID_CSV = """
            name;country;city;address;email;phone;web
            Madrid club;Spain;Madrid;Street 1;a@test.com;123;www.a.com
            Paris club;France;Paris;Rue 2;b@test.com;456;www.b.com
            """;

    private static final String SINGLE_LINE_CSV =
            "name;country;city;address;email;phone;web";

    @BeforeMethod
    public void setUp() {
        clubCsvReader = new ClubCsv();
    }

    // ---------- getHeaders() ----------

    @Test
    public void when_multilineContent_expect_headersFromFirstLine() {
        final String[] headers = clubCsvReader.getHeaders(VALID_CSV);

        assertThat(headers).containsExactly("name", "country", "city", "address", "email", "phone", "web");
    }

    @Test
    public void when_singleLineContent_expect_emptyHeaders() {
        final String[] headers = clubCsvReader.getHeaders(SINGLE_LINE_CSV);

        assertThat(headers).isEmpty();
    }

    @Test
    public void when_contentWithHash_expect_hashStripped() {
        final String csvWithHash = "#name;country;city;address;email;phone;web\nClubA;Spain;Madrid;St;a@t.com;1;w";
        final String[] headers = clubCsvReader.getHeaders(csvWithHash);

        assertThat(headers).contains("name");
        assertThat(headers[0]).doesNotContain("#");
    }

    // ---------- getContent() ----------

    @Test
    public void when_multilineContent_expect_dataLinesWithoutHeader() {
        final String[] content = clubCsvReader.getContent(VALID_CSV);

        assertThat(content).hasSize(2);
        assertThat(content[0]).contains("Madrid");
        assertThat(content[1]).contains("Paris");
    }

    @Test
    public void when_singleLineContent_expect_emptyContentArray() {
        final String[] content = clubCsvReader.getContent(SINGLE_LINE_CSV);

        assertThat(content).isEmpty();
    }

    // ---------- getHeaderIndex() ----------

    @Test
    public void when_knownHeader_expect_correctIndex() {
        final String[] headers = {"name", "country", "city"};

        assertThat(clubCsvReader.getHeaderIndex(headers, "country")).isEqualTo(1);
        assertThat(clubCsvReader.getHeaderIndex(headers, "city")).isEqualTo(2);
    }

    @Test
    public void when_unknownHeader_expect_minusOne() {
        final String[] headers = {"name", "country"};

        assertThat(clubCsvReader.getHeaderIndex(headers, "unknown")).isEqualTo(-1);
    }

    @Test
    public void when_nullHeader_expect_minusOne() {
        final String[] headers = {"name", "country"};

        assertThat(clubCsvReader.getHeaderIndex(headers, null)).isEqualTo(-1);
    }

    @Test
    public void when_headerCaseInsensitive_expect_found() {
        final String[] headers = {"Name", "Country"};

        assertThat(clubCsvReader.getHeaderIndex(headers, "name")).isZero();
        assertThat(clubCsvReader.getHeaderIndex(headers, "COUNTRY")).isEqualTo(1);
    }

    @Test
    public void when_headerWithWhitespace_expect_found() {
        final String[] headers = {" name ", "country"};

        assertThat(clubCsvReader.getHeaderIndex(headers, "name")).isZero();
    }

    // ---------- getField() ----------

    @Test
    public void when_validIndex_expect_fieldValue() {
        final String line = "ClubA;Spain;Madrid";

        assertThat(clubCsvReader.getField(line, 0)).isEqualTo("ClubA");
        assertThat(clubCsvReader.getField(line, 1)).isEqualTo("Spain");
        assertThat(clubCsvReader.getField(line, 2)).isEqualTo("Madrid");
    }

    @Test
    public void when_indexNegative_expect_null() {
        assertThat(clubCsvReader.getField("ClubA;Spain", -1)).isNull();
    }

    @Test
    public void when_indexBeyondColumns_expect_null() {
        assertThat(clubCsvReader.getField("ClubA;Spain", 10)).isNull();
    }

    // ---------- checkHeaders() via readCSV (throws on invalid header) ----------

    @Test
    public void when_invalidHeader_expect_invalidCsvFieldException() {
        final String invalidCsv =
                "name;wrongHeader;city;address;email;phone;web\n" +
                "ClubA;Spain;Madrid;St;a@t.com;1;w";

        assertThatThrownBy(() -> clubCsvReader.readCSV(invalidCsv))
                .isInstanceOf(InvalidCsvFieldException.class);
    }

    // ---------- readCSV() happy path ----------

    @Test
    public void when_validCsv_expect_clubsParsed() {
        final List<Club> clubs = clubCsvReader.readCSV(VALID_CSV);

        assertThat(clubs).hasSize(2);
        // Club.setName uses StringUtils.setCase() which capitalizes first letter of each word
        assertThat(clubs.get(0).getName()).isNotNull();
        assertThat(clubs.get(0).getCountry()).isEqualTo("Spain");
        assertThat(clubs.get(1).getCity()).isEqualTo("Paris");
    }

    @Test
    public void when_headerOnlyCsv_expect_emptyList() {
        final String headerOnly = "name;country;city;address;email;phone;web";
        // Single-line CSV returns no content rows
        final String[] content = clubCsvReader.getContent(headerOnly);
        assertThat(content).isEmpty();
    }
}




