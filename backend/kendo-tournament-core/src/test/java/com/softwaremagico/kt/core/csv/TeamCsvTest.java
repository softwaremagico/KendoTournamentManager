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

import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessResourceFailureException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

@Test(groups = "teamCsvTests")
public class TeamCsvTest {

    private static final String HEADER = "name;tournament;member1;member2;member3;member4;member5;member6;member7;member8;member9";

    @Mock
    private TournamentProvider tournamentProvider;

    @Mock
    private ParticipantProvider participantProvider;

    private TeamCsv teamCsv;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.teamCsv = new TeamCsv(this.tournamentProvider, this.participantProvider);
    }

    private Participant participant(String idCard) {
        final Participant participant = new Participant();
        participant.setIdCard(idCard);
        return participant;
    }

    @Test
    public void shouldReadTeamWithSingleMember() {
        final Tournament tournament = new Tournament();
        tournament.setName("MyTournament");
        when(this.tournamentProvider.findByName("MyTournament")).thenReturn(Optional.of(tournament));
        when(this.participantProvider.findByIdCard("ID-1")).thenReturn(Optional.of(this.participant("ID-1")));

        final String csv = HEADER + "\nTeam0;MyTournament;ID-1;;;;;;;;";
        final List<Team> teams = this.teamCsv.readCSV(csv);

        assertEquals(teams.size(), 1);
        assertEquals(teams.get(0).getName(), "Team0");
        assertEquals(teams.get(0).getTournament(), tournament);
        assertEquals(teams.get(0).getMembers().size(), 1);
        assertEquals(teams.get(0).getMembers().get(0).getIdCard(), "ID1");
    }

    @Test
    public void shouldReadTeamWithMultipleMembers() {
        final Tournament tournament = new Tournament();
        tournament.setName("MyTournament");
        when(this.tournamentProvider.findByName("MyTournament")).thenReturn(Optional.of(tournament));
        when(this.participantProvider.findByIdCard("ID-1")).thenReturn(Optional.of(this.participant("ID-1")));
        when(this.participantProvider.findByIdCard("ID-2")).thenReturn(Optional.of(this.participant("ID-2")));
        when(this.participantProvider.findByIdCard("ID-3")).thenReturn(Optional.of(this.participant("ID-3")));

        final String csv = HEADER + "\nTeam0;MyTournament;ID-1;ID-2;ID-3;;;;;;";
        final List<Team> teams = this.teamCsv.readCSV(csv);

        assertEquals(teams.get(0).getMembers().size(), 3);
    }

    @Test
    public void shouldSetTournamentToNullWhenNotFound() {
        when(this.tournamentProvider.findByName(anyString())).thenReturn(Optional.empty());

        final String csv = HEADER + "\nTeam0;Unknown;;;;;;;;;";
        final List<Team> teams = this.teamCsv.readCSV(csv);

        assertNull(teams.get(0).getTournament());
    }

    @Test
    public void shouldHandleDataAccessExceptionWhenResolvingTournament() {
        when(this.tournamentProvider.findByName(anyString()))
                .thenThrow(new DataAccessResourceFailureException("db down"));

        final String csv = HEADER + "\nTeam0;MyTournament;;;;;;;;;";
        final List<Team> teams = this.teamCsv.readCSV(csv);

        assertEquals(teams.size(), 1);
        assertNull(teams.get(0).getTournament());
    }

    @Test
    public void shouldSkipMemberWhenIdCardBlank() {
        when(this.tournamentProvider.findByName(anyString())).thenReturn(Optional.empty());

        final String csv = HEADER + "\nTeam0;MyTournament;;;;;;;;;";
        final List<Team> teams = this.teamCsv.readCSV(csv);

        assertTrue(teams.get(0).getMembers().isEmpty());
    }

    @Test
    public void shouldLogAndSkipMemberWhenParticipantNotFound() {
        when(this.tournamentProvider.findByName(anyString())).thenReturn(Optional.empty());
        when(this.participantProvider.findByIdCard("MISSING")).thenReturn(Optional.empty());

        final String csv = HEADER + "\nTeam0;MyTournament;MISSING;;;;;;;;";
        final List<Team> teams = this.teamCsv.readCSV(csv);

        // A null placeholder is added and then trimmed since it's the last member.
        assertTrue(teams.get(0).getMembers().isEmpty());
    }

    @Test
    public void shouldReadMultipleTeamLines() {
        when(this.tournamentProvider.findByName(anyString())).thenReturn(Optional.empty());
        when(this.participantProvider.findByIdCard(anyString())).thenReturn(Optional.empty());

        final String csv = HEADER + "\nTeam0;MyTournament;;;;;;;;;\nTeam1;MyTournament;;;;;;;;;";
        final List<Team> teams = this.teamCsv.readCSV(csv);

        assertEquals(teams.size(), 2);
        assertEquals(teams.get(0).getName(), "Team0");
        assertEquals(teams.get(1).getName(), "Team1");
    }
}

