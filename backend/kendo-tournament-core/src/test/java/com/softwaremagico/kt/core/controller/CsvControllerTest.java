package com.softwaremagico.kt.core.controller;

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

import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.core.controller.models.GroupLinkDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.converters.ClubConverter;
import com.softwaremagico.kt.core.converters.GroupLinkConverter;
import com.softwaremagico.kt.core.converters.ParticipantConverter;
import com.softwaremagico.kt.core.converters.TeamConverter;
import com.softwaremagico.kt.core.converters.models.ClubConverterRequest;
import com.softwaremagico.kt.core.converters.models.GroupLinkConverterRequest;
import com.softwaremagico.kt.core.converters.models.ParticipantConverterRequest;
import com.softwaremagico.kt.core.converters.models.TeamConverterRequest;
import com.softwaremagico.kt.core.csv.ClubCsv;
import com.softwaremagico.kt.core.csv.GroupLinkCsv;
import com.softwaremagico.kt.core.csv.ParticipantCsv;
import com.softwaremagico.kt.core.csv.TeamCsv;
import com.softwaremagico.kt.core.exceptions.InvalidCsvFieldException;
import com.softwaremagico.kt.core.providers.ClubProvider;
import com.softwaremagico.kt.core.providers.GroupLinkProvider;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.core.providers.RoleProvider;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.Club;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.GroupLink;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test(groups = {"csvReader"})
public class CsvControllerTest {

    @Mock
    private ClubCsv clubCsv;
    @Mock
    private ClubProvider clubProvider;
    @Mock
    private ClubConverter clubConverter;

    @Mock
    private ParticipantCsv participantCsv;
    @Mock
    private ParticipantProvider participantProvider;
    @Mock
    private ParticipantConverter participantConverter;

    @Mock
    private TeamCsv teamCsv;
    @Mock
    private TeamProvider teamProvider;
    @Mock
    private TeamConverter teamConverter;

    @Mock
    private GroupLinkCsv groupLinkCsv;
    @Mock
    private GroupLinkProvider groupLinkProvider;
    @Mock
    private GroupLinkConverter groupLinkConverter;

    @Mock
    private RoleProvider roleProvider;
    @Mock
    private TournamentProvider tournamentProvider;
    @Mock
    private GroupProvider groupProvider;

    private CsvController controller;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new CsvController(clubCsv, clubProvider, clubConverter,
                participantCsv, participantProvider, participantConverter,
                teamCsv, teamProvider, teamConverter,
                groupLinkCsv, groupLinkProvider, groupLinkConverter,
                roleProvider, tournamentProvider, groupProvider);
    }

    @Test
    public void shouldAddNewClubWhenValid() {
        final Club club = new Club("ClubA", "ES", "Madrid");
        when(clubCsv.readCSV("csv")).thenReturn(List.of(club));
        when(clubProvider.findBy("ClubA", "Madrid")).thenReturn(Optional.empty());

        final List<ClubDTO> failed = controller.addClubs("csv", "uploader");

        assertTrue(failed.isEmpty());
        verify(clubProvider).save(club);
    }

    @Test
    public void shouldUpdateExistingClubWhenAlreadyPresent() {
        final Club club = new Club("ClubA", "ES", "Madrid");
        final Club stored = new Club("ClubA", "ES", "Madrid");
        stored.setId(77);
        when(clubCsv.readCSV("csv")).thenReturn(List.of(club));
        when(clubProvider.findBy("ClubA", "Madrid")).thenReturn(Optional.of(stored));

        final List<ClubDTO> failed = controller.addClubs("csv", "uploader");

        assertTrue(failed.isEmpty());
        verify(clubProvider).save(club);
    }

    @Test
    public void shouldReturnFailedClubWhenInvalidNameOrCity() {
        final Club invalid = new Club("ClubA", "ES", "Madrid");
        invalid.setName(null);
        invalid.setCity(null);
        final ClubDTO converted = new ClubDTO();
        when(clubCsv.readCSV("csv")).thenReturn(List.of(invalid));
        when(clubConverter.convert(any(ClubConverterRequest.class))).thenReturn(converted);

        final List<ClubDTO> failed = controller.addClubs("csv", "uploader");

        assertEquals(failed.size(), 1);
        assertEquals(failed.get(0), converted);
    }

    @Test
    public void shouldReturnFailedClubWhenSaveThrows() {
        final Club club = new Club("ClubA", "ES", "Madrid");
        final ClubDTO converted = new ClubDTO();
        when(clubCsv.readCSV("csv")).thenReturn(List.of(club));
        when(clubProvider.findBy("ClubA", "Madrid")).thenReturn(Optional.empty());
        when(clubConverter.convert(any(ClubConverterRequest.class))).thenReturn(converted);
        when(clubProvider.save(club)).thenThrow(new RuntimeException("db"));

        final List<ClubDTO> failed = controller.addClubs("csv", "uploader");

        assertEquals(failed.size(), 1);
        assertEquals(failed.get(0), converted);
    }

    @Test
    public void shouldAddNewParticipantWhenIdCardNotExists() {
        final Participant participant = new Participant();
        participant.setIdCard("ABC");
        when(participantCsv.readCSV("csv")).thenReturn(List.of(participant));
        when(participantProvider.findByIdCard("ABC")).thenReturn(Optional.empty());

        final List<ParticipantDTO> failed = controller.addParticipants("csv", "uploader");

        assertTrue(failed.isEmpty());
        verify(participantProvider).save(participant);
    }

    @Test
    public void shouldReturnFailedParticipantWhenAlreadyExists() {
        final Participant participant = new Participant();
        participant.setIdCard("ABC");
        final ParticipantDTO converted = new ParticipantDTO();
        when(participantCsv.readCSV("csv")).thenReturn(List.of(participant));
        when(participantProvider.findByIdCard("ABC")).thenReturn(Optional.of(new Participant()));
        when(participantConverter.convert(any(ParticipantConverterRequest.class))).thenReturn(converted);

        final List<ParticipantDTO> failed = controller.addParticipants("csv", "uploader");

        assertEquals(failed.size(), 1);
        assertEquals(failed.get(0), converted);
    }

    @Test
    public void shouldSetTournamentFromParamWhenTeamHasNoTournament() {
        final Tournament tournament = new Tournament();
        tournament.setId(10);
        tournament.setTeamSize(3);
        tournament.setName("T");
        final Team team = new Team();
        team.setName("TeamA");
        team.setMembers(new ArrayList<>());

        when(teamCsv.readCSV("csv")).thenReturn(List.of(team));
        when(tournamentProvider.get(10)).thenReturn(Optional.of(tournament));
        when(teamProvider.get(tournament, "TeamA")).thenReturn(Optional.empty());

        final List<TeamDTO> failed = controller.addTeams("csv", 10, "uploader");

        assertTrue(failed.isEmpty());
        assertEquals(team.getTournament(), tournament);
        verify(teamProvider).save(team);
    }

    @Test
    public void shouldFailTeamWhenTournamentMissingEverywhere() {
        final Team team = new Team();
        team.setName("TeamA");
        team.setMembers(new ArrayList<>());
        final TeamDTO converted = new TeamDTO();

        when(teamCsv.readCSV("csv")).thenReturn(List.of(team));
        when(tournamentProvider.get(10)).thenReturn(Optional.empty());
        when(teamConverter.convert(any(TeamConverterRequest.class))).thenReturn(converted);

        final List<TeamDTO> failed = controller.addTeams("csv", 10, "uploader");

        assertEquals(failed.size(), 1);
        assertEquals(failed.get(0), converted);
    }

    @Test
    public void shouldThrowWhenTeamSizeIsGreaterThanTournamentLimit() {
        final Tournament tournament = new Tournament();
        tournament.setId(10);
        tournament.setTeamSize(1);

        final Team team = new Team();
        team.setName("TeamA");
        team.setTournament(tournament);
        team.setMembers(new ArrayList<>(List.of(new Participant(), new Participant())));

        when(teamCsv.readCSV("csv")).thenReturn(List.of(team));

        org.testng.Assert.expectThrows(InvalidCsvFieldException.class,
                () -> controller.addTeams("csv", null, "uploader"));
    }

    @Test
    public void shouldCreateCustomTreeAndStoreGroupLinks() {
        final Tournament tournament = new Tournament();
        tournament.setId(1);
        tournament.setName("T");

        final GroupLink link = new GroupLink();

        when(tournamentProvider.get(1)).thenReturn(Optional.of(tournament));
        when(groupLinkCsv.getSourceGroupSize("csv")).thenReturn(2);
        when(groupLinkCsv.getDestinationGroupSize("csv")).thenReturn(1);
        when(groupLinkCsv.readCSV(tournament, "csv")).thenReturn(List.of(link));
        when(groupProvider.addGroup(any(Tournament.class), any(Group.class))).thenAnswer(inv -> inv.getArgument(1));
        when(groupProvider.getGroups(tournament)).thenReturn(List.of(new Group(tournament, 0, 0)));
        when(groupLinkProvider.generateLinks(anyList(), anyInt(), anyInt(), anyInt())).thenReturn(List.of());
        when(groupLinkProvider.save(anyList())).thenReturn(List.of());
        when(groupLinkProvider.save(any(GroupLink.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(groupLinkProvider).deleteByTournament(tournament);

        final List<GroupLinkDTO> failed = controller.addGroupLinks(1, "csv", "uploader");

        assertTrue(failed.isEmpty());
        verify(groupLinkProvider).deleteByTournament(tournament);
    }

    @Test
    public void shouldReturnFailedGroupLinkWhenSavingOneLinkThrows() {
        final Tournament tournament = new Tournament();
        tournament.setId(1);
        tournament.setName("T");

        final GroupLink link = new GroupLink();
        final GroupLinkDTO converted = new GroupLinkDTO();

        when(tournamentProvider.get(1)).thenReturn(Optional.of(tournament));
        when(groupLinkCsv.getSourceGroupSize("csv")).thenReturn(1);
        when(groupLinkCsv.getDestinationGroupSize("csv")).thenReturn(1);
        when(groupLinkCsv.readCSV(tournament, "csv")).thenReturn(List.of(link));
        when(groupProvider.addGroup(any(Tournament.class), any(Group.class))).thenAnswer(inv -> inv.getArgument(1));
        when(groupProvider.getGroups(tournament)).thenReturn(List.of(new Group(tournament, 0, 0)));
        when(groupLinkProvider.generateLinks(anyList(), anyInt(), anyInt(), anyInt())).thenReturn(List.of());
        when(groupLinkProvider.save(anyList())).thenReturn(List.of());
        when(groupLinkProvider.save(any(GroupLink.class))).thenThrow(new RuntimeException("db"));
        when(groupLinkConverter.convert(any(GroupLinkConverterRequest.class))).thenReturn(converted);

        final List<GroupLinkDTO> failed = controller.addGroupLinks(1, "csv", "uploader");

        assertEquals(failed.size(), 1);
        assertEquals(failed.get(0), converted);
    }
}


