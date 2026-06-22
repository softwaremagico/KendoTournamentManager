package com.softwaremagico.kt.rest.services;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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

import com.softwaremagico.kt.core.controller.TeamController;
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantInTournamentDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantsInTournamentDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.pdf.EmptyPdfBodyException;
import com.softwaremagico.kt.pdf.InvalidXmlElementException;
import com.softwaremagico.kt.pdf.controller.PdfController;
import com.softwaremagico.kt.pdf.lists.TeamList;
import com.softwaremagico.kt.persistence.values.TournamentType;
import com.softwaremagico.kt.rest.exceptions.BadRequestException;
import com.softwaremagico.kt.rest.security.KendoSecurityService;
import jakarta.servlet.http.HttpServletResponse;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.expectThrows;

@Test(groups = "restServicesUnit")
public class TeamServicesUnitTest {

    @Mock
    private TeamController teamController;

    @Mock
    private KendoSecurityService kendoSecurityService;

    @Mock
    private TournamentController tournamentController;

    @Mock
    private PdfController pdfController;

    @Mock
    private Authentication authentication;

    @Mock
    private HttpServletResponse response;

    private TeamServices teamServices;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(authentication.getName()).thenReturn("tester");
        teamServices = new TeamServices(teamController, kendoSecurityService, tournamentController, pdfController);
    }

    @Test
    public void shouldDelegateTeamQueriesAndCommands() {
        final TournamentDTO tournament = tournament();
        final TeamDTO team = team(tournament, "A");

        when(teamController.getAllByTournament(7, "tester")).thenReturn(List.of(team));
        when(teamController.getAllRemainingByTournament(7, "tester")).thenReturn(List.of(team));
        when(teamController.countByTournament(7)).thenReturn(1L);
        when(teamController.getAllByTournament(tournament, "tester")).thenReturn(List.of(team));
        when(teamController.create(tournament, "tester")).thenReturn(List.of(team));
        when(teamController.create(any(List.class), eq("tester"), eq("s1"))).thenReturn(List.of(team));
        when(teamController.delete(eq(tournament), any(ParticipantDTO.class))).thenReturn(team);
        doNothing().when(teamController).delete(tournament);

        assertEquals(teamServices.getAll(7, authentication, null).size(), 1);
        assertEquals(teamServices.getAllRemaining(7, authentication, null).size(), 1);
        assertEquals(teamServices.countByTournamentId(7, authentication, null), 1L);
        assertEquals(teamServices.getAll(tournament, authentication, null).size(), 1);
        assertEquals(teamServices.create(tournament, authentication, null).size(), 1);

        assertEquals(teamServices.set(List.of(team), authentication, "s1", null).size(), 1);

        final ParticipantInTournamentDTO participantInTournamentDTO = new ParticipantInTournamentDTO();
        participantInTournamentDTO.setTournament(tournament);
        participantInTournamentDTO.setParticipant(new ParticipantDTO());
        assertSame(teamServices.delete(participantInTournamentDTO, null), team);

        final ParticipantsInTournamentDTO participantsInTournamentDTO = new ParticipantsInTournamentDTO();
        participantsInTournamentDTO.setTournament(tournament);
        participantsInTournamentDTO.setParticipant(List.of(new ParticipantDTO(), new ParticipantDTO()));
        teamServices.delete(participantsInTournamentDTO, null);

        verify(teamController).delete(tournament);
    }

    @Test
    public void shouldValidateMissingTeamDataOnSet() {
        final TeamDTO invalidTeam = new TeamDTO();

        expectThrows(BadRequestException.class, () -> teamServices.set(null, authentication, "s2", null));
        expectThrows(BadRequestException.class, () -> teamServices.set(List.of(), authentication, "s2", null));
        expectThrows(BadRequestException.class, () -> teamServices.set(List.of(invalidTeam), authentication, "s2", null));
    }

    @Test
    public void shouldGenerateTeamsPdfAndSetHeader() throws InvalidXmlElementException, EmptyPdfBodyException {
        final TournamentDTO tournament = tournament();
        final TeamList teamList = org.mockito.Mockito.mock(TeamList.class);
        final byte[] expected = new byte[]{1, 2, 3};

        when(tournamentController.get(9)).thenReturn(tournament);
        when(pdfController.generateTeamList(tournament)).thenReturn(teamList);
        when(teamList.generate()).thenReturn(expected);

        final byte[] result = teamServices.getAllFromTournamentAsPdf(9, Locale.ENGLISH, response, null);

        assertEquals(result, expected);
        verify(response).setHeader(eq("Content-Disposition"), any(String.class));
    }

    @Test
    public void shouldWrapPdfErrorsAsBadRequest() throws InvalidXmlElementException, EmptyPdfBodyException {
        final TournamentDTO tournament = tournament();
        final TeamList teamList = org.mockito.Mockito.mock(TeamList.class);

        when(tournamentController.get(9)).thenReturn(tournament);
        when(pdfController.generateTeamList(tournament)).thenReturn(teamList);
        when(teamList.generate()).thenThrow(new InvalidXmlElementException("invalid"));

        final BadRequestException exception = expectThrows(BadRequestException.class,
                () -> teamServices.getAllFromTournamentAsPdf(9, Locale.ENGLISH, response, null));

        assertEquals(exception.getMessage(), "invalid");
    }

    private TournamentDTO tournament() {
        final TournamentDTO tournamentDTO = new TournamentDTO("Tournament", 1, 3, TournamentType.LEAGUE);
        tournamentDTO.setId(9);
        return tournamentDTO;
    }

    private TeamDTO team(TournamentDTO tournamentDTO, String name) {
        final TeamDTO teamDTO = new TeamDTO();
        teamDTO.setName(name);
        teamDTO.setTournament(tournamentDTO);
        return teamDTO;
    }
}

