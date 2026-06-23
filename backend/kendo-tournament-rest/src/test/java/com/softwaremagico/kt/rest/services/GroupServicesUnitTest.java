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

import com.softwaremagico.kt.core.controller.GroupController;
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.models.DuelDTO;
import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.pdf.EmptyPdfBodyException;
import com.softwaremagico.kt.pdf.InvalidXmlElementException;
import com.softwaremagico.kt.pdf.controller.PdfController;
import com.softwaremagico.kt.pdf.lists.GroupList;
import com.softwaremagico.kt.rest.exceptions.BadRequestException;
import com.softwaremagico.kt.rest.security.KendoSecurityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.expectThrows;

@Test(groups = "restServicesUnit")
public class GroupServicesUnitTest {

    @Mock
    private GroupController groupController;

    @Mock
    private KendoSecurityService kendoSecurityService;

    @Mock
    private PdfController pdfController;

    @Mock
    private TournamentController tournamentController;

    @Mock
    private Authentication authentication;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private GroupServices groupServices;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(authentication.getName()).thenReturn("editor");
        groupServices = new GroupServices(groupController, kendoSecurityService, pdfController, tournamentController);
    }

    @Test
    public void shouldGetAllGroupsFromTournament() {
        when(groupController.getFromTournament(3)).thenReturn(List.of(new GroupDTO()));

        final List<GroupDTO> result = groupServices.getAll(3, request);

        assertEquals(result.size(), 1);
        verify(groupController).getFromTournament(3);
    }

    @Test
    public void shouldGetGroupByTournamentLevelAndIndex() {
        final GroupDTO groupDTO = new GroupDTO();
        when(groupController.getFromTournament(1, 0, 0)).thenReturn(groupDTO);

        final GroupDTO result = groupServices.get(1, 0, 0, request);

        assertNotNull(result);
        verify(groupController).getFromTournament(1, 0, 0);
    }

    @Test
    public void shouldThrowBadRequestWhenLevelIsNull() {
        expectThrows(BadRequestException.class, () -> groupServices.get(1, null, 0, request));
    }

    @Test
    public void shouldThrowBadRequestWhenIndexIsNull() {
        expectThrows(BadRequestException.class, () -> groupServices.get(1, 0, null, request));
    }

    @Test
    public void shouldUpdateTeamInGroup() {
        final GroupDTO groupDTO = new GroupDTO();
        final List<TeamDTO> teams = List.of(new TeamDTO());
        when(groupController.setTeams(5, teams, "editor", "s1")).thenReturn(groupDTO);

        final GroupDTO result = groupServices.updateTeam(5, teams, authentication, "s1", request);

        assertNotNull(result);
        verify(groupController).setTeams(5, teams, "editor", "s1");
    }

    @Test
    public void shouldAddTeamsToGroup() {
        final GroupDTO groupDTO = new GroupDTO();
        final List<TeamDTO> teams = List.of(new TeamDTO());
        when(groupController.addTeams(5, teams, "editor", "s1")).thenReturn(groupDTO);

        final GroupDTO result = groupServices.addTeam(5, teams, authentication, "s1", request);

        assertNotNull(result);
        verify(groupController).addTeams(5, teams, "editor", "s1");
    }

    @Test
    public void shouldDeleteTeamFromGroup() {
        final GroupDTO groupDTO = new GroupDTO();
        final List<TeamDTO> teams = List.of(new TeamDTO());
        when(groupController.deleteTeams(5, teams, "editor", "s1")).thenReturn(groupDTO);

        final GroupDTO result = groupServices.deleteTeamFromGroup(5, teams, authentication, "s1", request);

        assertNotNull(result);
        verify(groupController).deleteTeams(5, teams, "editor", "s1");
    }

    @Test
    public void shouldDeleteTeamsFromTournament() {
        final List<TeamDTO> teams = List.of(new TeamDTO());
        when(groupController.deleteTeamsFromTournament(3, teams, "editor", "s1")).thenReturn(List.of(new GroupDTO()));

        final List<GroupDTO> result = groupServices.deleteTeam(3, teams, authentication, "s1", request);

        assertEquals(result.size(), 1);
        verify(groupController).deleteTeamsFromTournament(3, teams, "editor", "s1");
    }

    @Test
    public void shouldDeleteAllTeamsFromTournament() {
        when(groupController.deleteTeamsFromTournament(3, "editor", "s1")).thenReturn(List.of(new GroupDTO()));

        final List<GroupDTO> result = groupServices.deleteAllTeam(3, authentication, "s1", request);

        assertEquals(result.size(), 1);
        verify(groupController).deleteTeamsFromTournament(3, "editor", "s1");
    }

    @Test
    public void shouldSetTeamsAcrossGroups() {
        final List<TeamDTO> teams = List.of(new TeamDTO());
        final GroupDTO groupDTO = new GroupDTO();
        when(groupController.setTeams(teams, "editor", "s1")).thenReturn(groupDTO);

        final GroupDTO result = groupServices.updateTeam(teams, authentication, "s1", request);

        assertNotNull(result);
        verify(groupController).setTeams(teams, "editor", "s1");
    }

    @Test
    public void shouldAddUntiesToGroup() {
        final GroupDTO groupDTO = new GroupDTO();
        final List<DuelDTO> duels = List.of(new DuelDTO());
        when(groupController.addUnties(5, duels, "editor", "s1")).thenReturn(groupDTO);

        final GroupDTO result = groupServices.addUnties(5, duels, "s1", authentication, request);

        assertNotNull(result);
        verify(groupController).addUnties(5, duels, "editor", "s1");
    }

    @Test
    public void shouldGeneratePdfAndSetHeader() throws InvalidXmlElementException, EmptyPdfBodyException {
        final TournamentDTO tournamentDTO = new TournamentDTO();
        tournamentDTO.setName("Cup");
        final GroupList groupList = org.mockito.Mockito.mock(GroupList.class);
        when(tournamentController.get(4)).thenReturn(tournamentDTO);
        when(pdfController.generateGroupList(any(Locale.class), eq(tournamentDTO))).thenReturn(groupList);
        when(groupList.generate()).thenReturn(new byte[]{1, 2, 3});

        final byte[] bytes = groupServices.getAllFromTournamentAsPdf(4, Locale.ENGLISH, response, request);

        assertEquals(bytes, new byte[]{1, 2, 3});
        verify(response).setHeader(eq(HttpHeaders.CONTENT_DISPOSITION), any());
    }

    @Test
    public void shouldWrapPdfErrorAsBadRequest() throws InvalidXmlElementException, EmptyPdfBodyException {
        final TournamentDTO tournamentDTO = new TournamentDTO();
        tournamentDTO.setName("Cup");
        final GroupList groupList = org.mockito.Mockito.mock(GroupList.class);
        when(tournamentController.get(4)).thenReturn(tournamentDTO);
        when(pdfController.generateGroupList(any(Locale.class), eq(tournamentDTO))).thenReturn(groupList);
        when(groupList.generate()).thenThrow(new EmptyPdfBodyException("empty"));

        expectThrows(BadRequestException.class, () -> groupServices.getAllFromTournamentAsPdf(4, Locale.ENGLISH, response, request));
    }

    @Test
    public void shouldRefreshNonStartedGroups() {
        doNothing().when(groupController).refreshGroupContent(2, 0);

        groupServices.refreshNonStartedGroups(2, null, response, request);

        verify(groupController).refreshGroupContent(2, 0);
    }

    @Test
    public void shouldRefreshNonStartedGroupsWithLevel() {
        doNothing().when(groupController).refreshGroupContent(2, 3);

        groupServices.refreshNonStartedGroups(2, 3, response, request);

        verify(groupController).refreshGroupContent(2, 3);
    }
}

