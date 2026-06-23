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

import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.exceptions.NoContentException;
import com.softwaremagico.kt.pdf.EmptyPdfBodyException;
import com.softwaremagico.kt.pdf.InvalidXmlElementException;
import com.softwaremagico.kt.pdf.accreditations.TournamentAccreditationCards;
import com.softwaremagico.kt.pdf.controller.PdfController;
import com.softwaremagico.kt.pdf.diplomas.DiplomaPDF;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.TournamentType;
import com.softwaremagico.kt.rest.exceptions.BadRequestException;
import com.softwaremagico.kt.rest.exceptions.InvalidRequestException;
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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.expectThrows;

@Test(groups = "restServicesUnit")
public class TournamentServicesUnitTest {

    @Mock
    private TournamentController tournamentController;

    @Mock
    private KendoSecurityService kendoSecurityService;

    @Mock
    private PdfController pdfController;

    @Mock
    private Authentication authentication;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private TournamentServices tournamentServices;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(authentication.getName()).thenReturn("editor");
        when(kendoSecurityService.getGuestPrivilege()).thenReturn("GUEST");
        when(kendoSecurityService.getParticipantPrivilege()).thenReturn("PARTICIPANT");
        when(kendoSecurityService.getViewerPrivilege()).thenReturn("VIEWER");
        when(kendoSecurityService.getEditorPrivilege()).thenReturn("EDITOR");
        when(kendoSecurityService.getAdminPrivilege()).thenReturn("ADMIN");
        tournamentServices = new TournamentServices(tournamentController, kendoSecurityService, pdfController);
    }

    @Test
    public void shouldReturnRequiredRolesIncludingGuest() {
        final String[] roles = tournamentServices.requiredRoleForEntityById();
        assertNotNull(roles);
        assertEquals(roles.length, 5);
    }

    @Test
    public void shouldAddTournamentWithBasicInfo() {
        final TournamentDTO tournamentDTO = new TournamentDTO();
        tournamentDTO.setName("Spring Cup");
        when(tournamentController.create("Spring Cup", 2, 3, TournamentType.LEAGUE, "editor")).thenReturn(tournamentDTO);

        final TournamentDTO result = tournamentServices.add("Spring Cup", 2, 3, TournamentType.LEAGUE, authentication, request);

        assertNotNull(result);
        verify(tournamentController).create("Spring Cup", 2, 3, TournamentType.LEAGUE, "editor");
    }

    @Test
    public void shouldGenerateAccreditationsAndSetHeader() throws InvalidXmlElementException, EmptyPdfBodyException, NoContentException {
        final TournamentDTO tournamentDTO = tournamentWithName("Autumn Cup");
        final TournamentAccreditationCards cards = org.mockito.Mockito.mock(TournamentAccreditationCards.class);
        when(tournamentController.get(5)).thenReturn(tournamentDTO);
        when(pdfController.generateTournamentAccreditations(any(Locale.class), eq(tournamentDTO), eq(false), eq("editor"), isNull(), any()))
                .thenReturn(cards);
        when(cards.generate()).thenReturn(new byte[]{1, 2, 3});

        final byte[] bytes = tournamentServices.getAllAccreditationsFromTournamentAsPdf(5, null, null, null,
                Locale.ENGLISH, response, authentication, request);

        assertEquals(bytes, new byte[]{1, 2, 3});
        verify(response).setHeader(eq(HttpHeaders.CONTENT_DISPOSITION), any());
    }

    @Test
    public void shouldWrapAccreditationsInvalidXmlAsBadRequest() throws InvalidXmlElementException, EmptyPdfBodyException {
        final TournamentDTO tournamentDTO = tournamentWithName("Autumn Cup");
        final TournamentAccreditationCards cards = org.mockito.Mockito.mock(TournamentAccreditationCards.class);
        when(tournamentController.get(5)).thenReturn(tournamentDTO);
        when(pdfController.generateTournamentAccreditations(any(Locale.class), eq(tournamentDTO), eq(false), eq("editor"), isNull(), any()))
                .thenReturn(cards);
        when(cards.generate()).thenThrow(new InvalidXmlElementException("invalid"));

        expectThrows(BadRequestException.class, () ->
                tournamentServices.getAllAccreditationsFromTournamentAsPdf(5, null, null, null,
                        Locale.ENGLISH, response, authentication, request));
    }

    @Test
    public void shouldGenerateParticipantAccreditationAndSetHeader() throws InvalidXmlElementException, EmptyPdfBodyException {
        final TournamentDTO tournamentDTO = tournamentWithName("Cup");
        final ParticipantDTO participantDTO = new ParticipantDTO();
        final TournamentAccreditationCards cards = org.mockito.Mockito.mock(TournamentAccreditationCards.class);
        when(tournamentController.get(6)).thenReturn(tournamentDTO);
        when(pdfController.generateTournamentAccreditations(any(Locale.class), eq(tournamentDTO), eq(participantDTO),
                eq(RoleType.COMPETITOR), eq("editor"), isNull())).thenReturn(cards);
        when(cards.generate()).thenReturn(new byte[]{4, 5, 6});

        final byte[] bytes = tournamentServices.getParticipantAccreditationFromTournamentAsPdf(6, RoleType.COMPETITOR,
                participantDTO, null, Locale.ENGLISH, response, authentication, request);

        assertEquals(bytes, new byte[]{4, 5, 6});
        verify(response).setHeader(eq(HttpHeaders.CONTENT_DISPOSITION), any());
    }

    @Test
    public void shouldThrowWhenParticipantIsNullForAccreditation() {
        expectThrows(InvalidRequestException.class, () ->
                tournamentServices.getParticipantAccreditationFromTournamentAsPdf(6, RoleType.COMPETITOR,
                        null, null, Locale.ENGLISH, response, authentication, request));
    }

    @Test
    public void shouldGenerateDiplomasAndSetHeader() throws InvalidXmlElementException, EmptyPdfBodyException, NoContentException {
        final TournamentDTO tournamentDTO = tournamentWithName("Winter Cup");
        final DiplomaPDF diplomaPDF = org.mockito.Mockito.mock(DiplomaPDF.class);
        when(tournamentController.get(7)).thenReturn(tournamentDTO);
        when(pdfController.generateTournamentDiplomas(eq(tournamentDTO), eq(false), eq("editor"), isNull(), any()))
                .thenReturn(diplomaPDF);
        when(diplomaPDF.generate()).thenReturn(new byte[]{7, 8, 9});

        final byte[] bytes = tournamentServices.getAllDiplomasFromTournamentAsPdf(7, null, null, null,
                Locale.ENGLISH, response, authentication, request);

        assertEquals(bytes, new byte[]{7, 8, 9});
        verify(response).setHeader(eq(HttpHeaders.CONTENT_DISPOSITION), any());
    }

    @Test
    public void shouldWrapDiplomasEmptyPdfAsBadRequest() throws InvalidXmlElementException, EmptyPdfBodyException {
        final TournamentDTO tournamentDTO = tournamentWithName("Winter Cup");
        final DiplomaPDF diplomaPDF = org.mockito.Mockito.mock(DiplomaPDF.class);
        when(tournamentController.get(7)).thenReturn(tournamentDTO);
        when(pdfController.generateTournamentDiplomas(eq(tournamentDTO), eq(true), eq("editor"), isNull(), any()))
                .thenReturn(diplomaPDF);
        when(diplomaPDF.generate()).thenThrow(new EmptyPdfBodyException("empty"));

        expectThrows(BadRequestException.class, () ->
                tournamentServices.getAllDiplomasFromTournamentAsPdf(7, null, true, null,
                        Locale.ENGLISH, response, authentication, request));
    }

    @Test
    public void shouldGenerateParticipantDiplomaAndSetHeader() throws InvalidXmlElementException, EmptyPdfBodyException {
        final TournamentDTO tournamentDTO = tournamentWithName("Summer Cup");
        final ParticipantDTO participantDTO = new ParticipantDTO();
        participantDTO.setName("Miyamoto");
        participantDTO.setLastname("Musashi");
        final DiplomaPDF diplomaPDF = org.mockito.Mockito.mock(DiplomaPDF.class);
        when(tournamentController.get(8)).thenReturn(tournamentDTO);
        when(pdfController.generateTournamentDiploma(tournamentDTO, participantDTO)).thenReturn(diplomaPDF);
        when(diplomaPDF.generate()).thenReturn(new byte[]{10, 11, 12});

        final byte[] bytes = tournamentServices.getParticipantDiplomaFromTournamentAsPdf(8, participantDTO,
                Locale.ENGLISH, response, request);

        assertEquals(bytes, new byte[]{10, 11, 12});
        verify(response).setHeader(eq(HttpHeaders.CONTENT_DISPOSITION), any());
    }

    @Test
    public void shouldThrowWhenParticipantIsNullForDiploma() {
        expectThrows(InvalidRequestException.class, () ->
                tournamentServices.getParticipantDiplomaFromTournamentAsPdf(8, null, Locale.ENGLISH, response, request));
    }

    @Test
    public void shouldDelegateCloneAndNumberOfWinners() {
        final TournamentDTO cloned = tournamentWithName("Cloned Cup");
        when(tournamentController.clone(9, "editor")).thenReturn(cloned);
        doNothing().when(tournamentController).setNumberOfWinners(9, 3, "editor");

        final TournamentDTO result = tournamentServices.clone(9, authentication, request);
        tournamentServices.numberOfWinners(9, 3, authentication, request);

        assertNotNull(result);
        verify(tournamentController).clone(9, "editor");
        verify(tournamentController).setNumberOfWinners(9, 3, "editor");
    }

    @Test
    public void shouldGetLastUnlockedTournament() {
        final TournamentDTO tournamentDTO = tournamentWithName("Unlocked Cup");
        when(tournamentController.getLatestUnlocked()).thenReturn(tournamentDTO);

        final TournamentDTO result = tournamentServices.getLastUnlockedTournament();

        assertNotNull(result);
        verify(tournamentController).getLatestUnlocked();
    }

    @Test
    public void shouldGetTournamentById() {
        final TournamentDTO tournamentDTO = tournamentWithName("T1");
        when(tournamentController.get(1)).thenReturn(tournamentDTO);

        final TournamentDTO result = tournamentServices.get(1, request);

        assertNotNull(result);
        verify(tournamentController).get(1);
    }

    private TournamentDTO tournamentWithName(String name) {
        final TournamentDTO dto = new TournamentDTO();
        dto.setName(name);
        return dto;
    }
}



