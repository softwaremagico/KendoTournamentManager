package com.softwaremagico.kt.core.csv;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2025 SoftwareMagico
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

import com.softwaremagico.kt.persistence.entities.Club;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ClubCsv extends CsvReader<Club> {
    private static final String NAME_HEADER = "name";
    private static final String COUNTRY_HEADER = "country";
    private static final String CITY_HEADER = "city";
    private static final String ADDRESS_HEADER = "address";
    private static final String EMAIL_HEADER = "email";
    private static final String PHONE_HEADER = "phone";
    private static final String WEB_HEADER = "web";

    @Override
    public List<Club> readCSV(String csvContent) {
        final String[] headers = getHeaders(csvContent);
        checkHeaders(headers, NAME_HEADER, COUNTRY_HEADER, CITY_HEADER, ADDRESS_HEADER, EMAIL_HEADER, PHONE_HEADER, WEB_HEADER);
        final String[] content = getContent(csvContent);
        final List<Club> clubs = new ArrayList<>();

        final int nameIndex = getHeaderIndex(headers, NAME_HEADER);
        final int countryIndex = getHeaderIndex(headers, COUNTRY_HEADER);
        final int cityIndex = getHeaderIndex(headers, CITY_HEADER);
        final int addressIndex = getHeaderIndex(headers, ADDRESS_HEADER);
        final int emailIndex = getHeaderIndex(headers, EMAIL_HEADER);
        final int phoneIndex = getHeaderIndex(headers, PHONE_HEADER);
        final int webIndex = getHeaderIndex(headers, WEB_HEADER);


        for (String clubLine : content) {
            final Club club = new Club();
            club.setName(getField(clubLine, nameIndex));
            club.setCountry(getField(clubLine, countryIndex));
            club.setCity(getField(clubLine, cityIndex));
            club.setAddress(getField(clubLine, addressIndex));
            club.setEmail(getField(clubLine, emailIndex));
            club.setPhone(getField(clubLine, phoneIndex));
            club.setWeb(getField(clubLine, webIndex));
            clubs.add(club);
        }
        return clubs;
    }

}
