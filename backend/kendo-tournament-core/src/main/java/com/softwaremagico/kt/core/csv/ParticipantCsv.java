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

import com.softwaremagico.kt.core.providers.ClubProvider;
import com.softwaremagico.kt.logger.KendoTournamentLogger;
import com.softwaremagico.kt.persistence.entities.Participant;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ParticipantCsv extends CsvReader<Participant> {
    private static final String NAME_HEADER = "name";
    private static final String LASTNAME_HEADER = "lastname";
    private static final String ID_CARD_HEADER = "idCard";
    private static final String CLUB_HEADER = "club";
    private static final String CLUB_CITY_HEADER = "clubCity";

    private final ClubProvider clubProvider;

    public ParticipantCsv(ClubProvider clubProvider) {
        this.clubProvider = clubProvider;
    }

    @Override
    public List<Participant> readCSV(String csvContent) {
        final String[] headers = getHeaders(csvContent);
        checkHeaders(headers, NAME_HEADER, LASTNAME_HEADER, ID_CARD_HEADER, CLUB_HEADER, CLUB_CITY_HEADER);
        final String[] content = getContent(csvContent);
        final List<Participant> participants = new ArrayList<>();

        final int nameIndex = getHeaderIndex(headers, NAME_HEADER);
        final int lastnameIndex = getHeaderIndex(headers, LASTNAME_HEADER);
        final int idCardIndex = getHeaderIndex(headers, ID_CARD_HEADER);
        final int clubIndex = getHeaderIndex(headers, CLUB_HEADER);
        final int clubCityIndex = getHeaderIndex(headers, CLUB_CITY_HEADER);

        for (String participantLine : content) {
            final Participant participant = new Participant();
            participant.setName(getField(participantLine, nameIndex));
            participant.setLastname(getField(participantLine, lastnameIndex));
            participant.setIdCard(getField(participantLine, idCardIndex));

            final String clubName = getField(participantLine, clubIndex);
            final String clubCity = getField(participantLine, clubCityIndex);
            if (clubName != null && clubCity != null) {
                try {
                    participant.setClub(clubProvider.findBy(clubName, clubCity).orElse(null));
                } catch (Exception e) {
                    KendoTournamentLogger.severe(this.getClass().getName(), "Error when inserting CSV from '" + participantLine + "'.");
                    KendoTournamentLogger.errorMessage(this.getClass(), e);
                }
            }

            participants.add(participant);
        }
        return participants;
    }
}
