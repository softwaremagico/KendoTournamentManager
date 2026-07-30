package com.softwaremagico.kt.rest.services;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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

import com.softwaremagico.kt.core.controller.RoleController;
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantInTournamentDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.pdf.EmptyPdfBodyException;
import com.softwaremagico.kt.pdf.InvalidXmlElementException;
import com.softwaremagico.kt.pdf.controller.PdfController;
import com.softwaremagico.kt.pdf.lists.RoleList;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.rest.exceptions.BadRequestException;
import com.softwaremagico.kt.rest.security.KendoSecurityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Locale;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;

@Test(groups = "roleServices")
public class RoleServicesTest {

    @Mock
    private RoleController mockRoleController;

    @Mock
    private KendoSecurityService mockSecurityService;

    @Mock
    private PdfController mockPdfController;

    @Mock
    private TournamentController mockTournamentController;

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private RoleList mockRoleList;

    private RoleServices services;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        services = new RoleServices(mockRoleController, mockSecurityService, mockPdfController, mockTournamentController);
    }

    @Test
    public void getAllFromTournament_byId_expectDelegation() {
        final List<RoleDTO> expected = List.of(new RoleDTO());
        when(mockRoleController.getByTournamentId(10)).thenReturn(expected);

        final List<RoleDTO> result = services.getAllFromTournament(10, mockRequest);

        assertEquals(result, expected);
    }

    @Test
    public void getAllFromTournament_byIdAndRoleTypes_expectDelegation() {
        final List<RoleDTO> expected = List.of(new RoleDTO());
        when(mockRoleController.get(10, List.of(RoleType.COMPETITOR))).thenReturn(expected);

        final List<RoleDTO> result = services.getAllFromTournament(10, List.of(RoleType.COMPETITOR), mockRequest);

        assertEquals(result, expected);
    }

    @Test
    public void delete_expectControllerDelete() {
        final ParticipantInTournamentDTO participantInTournament = new ParticipantInTournamentDTO();
        final ParticipantDTO participant = new ParticipantDTO();
        final TournamentDTO tournament = new TournamentDTO();
        participantInTournament.setParticipant(participant);
        participantInTournament.setTournament(tournament);

        services.delete(participantInTournament, mockRequest);

        verify(mockRoleController).delete(participant, tournament);
    }

    @Test
    public void getAllFromTournamentAsPdf_expectPdfBytesAndContentDisposition() throws Exception {
        final TournamentDTO tournament = new TournamentDTO();
        tournament.setName("My Tournament");
        final byte[] pdfBytes = new byte[]{1, 2, 3};

        when(mockTournamentController.get(10)).thenReturn(tournament);
        when(mockPdfController.generateClubList(Locale.ENGLISH, tournament)).thenReturn(mockRoleList);
        when(mockRoleList.generate()).thenReturn(pdfBytes);

        final byte[] result = services.getAllFromTournamentAsPdf(10, Locale.ENGLISH, mockResponse, mockRequest);

        assertNotNull(result);
        assertEquals(result, pdfBytes);
        verify(mockResponse).setHeader("Content-Disposition", "attachment; filename=\"My Tournament - club list.pdf\"");
    }

    @Test
    public void getAllFromTournamentAsPdf_withInvalidXmlElementException_expectBadRequestException() throws Exception {
        final TournamentDTO tournament = new TournamentDTO();
        tournament.setName("My Tournament");

        when(mockTournamentController.get(10)).thenReturn(tournament);
        when(mockPdfController.generateClubList(Locale.ENGLISH, tournament)).thenReturn(mockRoleList);
        when(mockRoleList.generate()).thenThrow(new InvalidXmlElementException("invalid xml"));

        assertThrows(BadRequestException.class,
                () -> services.getAllFromTournamentAsPdf(10, Locale.ENGLISH, mockResponse, mockRequest));
    }

    @Test
    public void getAllFromTournamentAsPdf_withEmptyPdfBodyException_expectBadRequestException() throws Exception {
        final TournamentDTO tournament = new TournamentDTO();
        tournament.setName("My Tournament");

        when(mockTournamentController.get(10)).thenReturn(tournament);
        when(mockPdfController.generateClubList(Locale.ENGLISH, tournament)).thenReturn(mockRoleList);
        when(mockRoleList.generate()).thenThrow(new EmptyPdfBodyException("empty body"));

        assertThrows(BadRequestException.class,
                () -> services.getAllFromTournamentAsPdf(10, Locale.ENGLISH, mockResponse, mockRequest));
    }
}


