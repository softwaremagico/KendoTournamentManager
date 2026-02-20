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

import com.softwaremagico.kt.core.exceptions.TournamentNotFoundException;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.logger.KendoTournamentLogger;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Team;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TeamCsv extends CsvReader<Team> {
    private static final String NAME_HEADER = "name";
    private static final String TOURNAMENT_HEADER = "tournament";
    private static final String MEMBER1_HEADER = "member1";
    private static final String MEMBER2_HEADER = "member2";
    private static final String MEMBER3_HEADER = "member3";
    private static final String MEMBER4_HEADER = "member4";
    private static final String MEMBER5_HEADER = "member5";
    private static final String MEMBER6_HEADER = "member6";
    private static final String MEMBER7_HEADER = "member7";
    private static final String MEMBER8_HEADER = "member8";
    private static final String MEMBER9_HEADER = "member9";

    private final TournamentProvider tournamentProvider;
    private final ParticipantProvider participantProvider;

    public TeamCsv(TournamentProvider tournamentProvider, ParticipantProvider participantProvider) {
        this.tournamentProvider = tournamentProvider;
        this.participantProvider = participantProvider;
    }


    @Override
    public List<Team> readCSV(String csvContent) {
        final String[] headers = getHeaders(csvContent);
        checkHeaders(headers, NAME_HEADER, TOURNAMENT_HEADER, MEMBER1_HEADER, MEMBER2_HEADER, MEMBER3_HEADER, MEMBER4_HEADER, MEMBER5_HEADER, MEMBER6_HEADER,
                MEMBER7_HEADER, MEMBER8_HEADER, MEMBER9_HEADER);
        final String[] content = getContent(csvContent);
        final List<Team> teams = new ArrayList<>();

        final int nameIndex = getHeaderIndex(headers, NAME_HEADER);
        final int tournamentIndex = getHeaderIndex(headers, TOURNAMENT_HEADER);
        final int member1Index = getHeaderIndex(headers, MEMBER1_HEADER);
        final int member2Index = getHeaderIndex(headers, MEMBER2_HEADER);
        final int member3Index = getHeaderIndex(headers, MEMBER3_HEADER);
        final int member4Index = getHeaderIndex(headers, MEMBER4_HEADER);
        final int member5Index = getHeaderIndex(headers, MEMBER5_HEADER);
        final int member6Index = getHeaderIndex(headers, MEMBER6_HEADER);
        final int member7Index = getHeaderIndex(headers, MEMBER7_HEADER);
        final int member8Index = getHeaderIndex(headers, MEMBER8_HEADER);
        final int member9Index = getHeaderIndex(headers, MEMBER9_HEADER);

        for (String teamLine : content) {
            final Team team = new Team();
            team.setName(getField(teamLine, nameIndex));
            try {
                team.setTournament(tournamentProvider.findByName(getField(teamLine, tournamentIndex)).orElseThrow(()
                        -> new TournamentNotFoundException(this.getClass(), "No tournament with name '"
                        + getField(teamLine, tournamentIndex) + "' exists.")));
            } catch (Exception e) {
                KendoTournamentLogger.errorMessage(this.getClass(), e);
            }
            addMember(teamLine, team, member1Index);
            addMember(teamLine, team, member2Index);
            addMember(teamLine, team, member3Index);
            addMember(teamLine, team, member4Index);
            addMember(teamLine, team, member5Index);
            addMember(teamLine, team, member6Index);
            addMember(teamLine, team, member7Index);
            addMember(teamLine, team, member8Index);
            addMember(teamLine, team, member9Index);

            //Remove latest null members
            for (int i = team.getMembers().size() - 1; i >= 0; i--) {
                if (team.getMembers().get(i) != null) {
                    break;
                }
                team.getMembers().remove(i);
            }

            teams.add(team);
        }
        return teams;
    }

    private void addMember(String teamLine, Team team, int memberIndex) {
        if (memberIndex >= 0) {
            final String idCard = getField(teamLine, memberIndex);
            if (idCard != null && !idCard.isBlank()) {
                final Participant participant = participantProvider.findByIdCard(idCard).orElse(null);
                if (participant != null) {
                    team.addMember(participant);
                } else {
                    KendoTournamentLogger.severe(this.getClass().getName(), "Error when inserting CSV from '" + teamLine + "'.");
                    KendoTournamentLogger.errorMessage(this.getClass(), "No member with id '" + getField(teamLine, memberIndex) + "' on team '"
                            + team.getName() + "'.");
                }
            } else {
                team.addMember(null);
            }
        }
    }
}
