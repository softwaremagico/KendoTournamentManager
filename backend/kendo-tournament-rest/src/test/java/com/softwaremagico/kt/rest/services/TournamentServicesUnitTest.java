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
import org.springframework.security.core.Authentication;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

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
		when(this.authentication.getName()).thenReturn("editor");
		when(this.kendoSecurityService.getGuestPrivilege()).thenReturn("GUEST");
		when(this.kendoSecurityService.getParticipantPrivilege()).thenReturn("PARTICIPANT");
		when(this.kendoSecurityService.getViewerPrivilege()).thenReturn("VIEWER");
		when(this.kendoSecurityService.getEditorPrivilege()).thenReturn("EDITOR");
		when(this.kendoSecurityService.getAdminPrivilege()).thenReturn("ADMIN");
        this.tournamentServices = new TournamentServices(this.tournamentController, this.kendoSecurityService, this.pdfController);
	}

	@Test
	public void shouldReturnRequiredRolesIncludingGuest() {
		final String[] roles = this.tournamentServices.requiredRoleForEntityById();
		assertNotNull(roles);
		assertEquals(roles.length, 5);
	}

	@Test
	public void shouldAddTournamentWithBasicInfo() {
		final TournamentDTO tournamentDTO = new TournamentDTO();
		tournamentDTO.setName("Spring Cup");
		when(this.tournamentController.create("Spring Cup", 2, 3, TournamentType.LEAGUE, "editor"))
				.thenReturn(tournamentDTO);

		final TournamentDTO result = this.tournamentServices.add("Spring Cup", 2, 3, TournamentType.LEAGUE, this.authentication,
            this.request);

		assertNotNull(result);
		verify(this.tournamentController).create("Spring Cup", 2, 3, TournamentType.LEAGUE, "editor");
	}

	@Test
	public void shouldGenerateAccreditationsAndSetHeader()
			throws InvalidXmlElementException, EmptyPdfBodyException, NoContentException {
		final TournamentDTO tournamentDTO = this.tournamentWithName("Autumn Cup");
		final TournamentAccreditationCards cards = mock(TournamentAccreditationCards.class);
		when(this.tournamentController.get(5)).thenReturn(tournamentDTO);
		when(this.pdfController.generateTournamentAccreditations(any(Locale.class), eq(tournamentDTO), eq(false),
				eq("editor"), isNull(), any())).thenReturn(cards);
		when(cards.generate()).thenReturn(new byte[]{1, 2, 3});

		final byte[] bytes = this.tournamentServices.getAllAccreditationsFromTournamentAsPdf(5, null, null, null,
				Locale.ENGLISH, this.response, this.authentication, this.request);

		assertEquals(bytes, new byte[]{1, 2, 3});
		verify(this.response).setHeader(anyString(), anyString());
	}

	@Test
	public void shouldWrapAccreditationsInvalidXmlAsBadRequest()
			throws InvalidXmlElementException, EmptyPdfBodyException {
		final TournamentDTO tournamentDTO = this.tournamentWithName("Autumn Cup");
		final TournamentAccreditationCards cards = mock(TournamentAccreditationCards.class);
		when(this.tournamentController.get(5)).thenReturn(tournamentDTO);
		when(this.pdfController.generateTournamentAccreditations(any(Locale.class), eq(tournamentDTO), eq(false),
				eq("editor"), isNull(), any())).thenReturn(cards);
		when(cards.generate()).thenThrow(new InvalidXmlElementException("invalid"));

		expectThrows(BadRequestException.class, () -> this.tournamentServices.getAllAccreditationsFromTournamentAsPdf(5,
				null, null, null, Locale.ENGLISH, this.response, this.authentication, this.request));
	}

	@Test
	public void shouldGenerateParticipantAccreditationAndSetHeader()
			throws InvalidXmlElementException, EmptyPdfBodyException {
		final TournamentDTO tournamentDTO = this.tournamentWithName("Cup");
		final ParticipantDTO participantDTO = new ParticipantDTO();
		final TournamentAccreditationCards cards = mock(TournamentAccreditationCards.class);
		when(this.tournamentController.get(6)).thenReturn(tournamentDTO);
		when(this.pdfController.generateTournamentAccreditations(any(Locale.class), eq(tournamentDTO), eq(participantDTO),
				eq(RoleType.COMPETITOR), eq("editor"), isNull())).thenReturn(cards);
		when(cards.generate()).thenReturn(new byte[]{4, 5, 6});

		final byte[] bytes = this.tournamentServices.getParticipantAccreditationFromTournamentAsPdf(6, RoleType.COMPETITOR,
				participantDTO, null, Locale.ENGLISH, this.response, this.authentication, this.request);

		assertEquals(bytes, new byte[]{4, 5, 6});
		verify(this.response).setHeader(anyString(), anyString());
	}

	@Test
	public void shouldThrowWhenParticipantIsNullForAccreditation() {
		expectThrows(InvalidRequestException.class,
				() -> this.tournamentServices.getParticipantAccreditationFromTournamentAsPdf(6, RoleType.COMPETITOR, null,
						null, Locale.ENGLISH, this.response, this.authentication, this.request));
	}

	@Test
	public void shouldGenerateDiplomasAndSetHeader()
			throws InvalidXmlElementException, EmptyPdfBodyException, NoContentException {
		final TournamentDTO tournamentDTO = this.tournamentWithName("Winter Cup");
		final DiplomaPDF diplomaPDF = mock(DiplomaPDF.class);
		when(this.tournamentController.get(7)).thenReturn(tournamentDTO);
		when(this.pdfController.generateTournamentDiplomas(eq(tournamentDTO), eq(false), eq("editor"), isNull(), any()))
				.thenReturn(diplomaPDF);
		when(diplomaPDF.generate()).thenReturn(new byte[]{7, 8, 9});

		final byte[] bytes = this.tournamentServices.getAllDiplomasFromTournamentAsPdf(7, null, null, null, Locale.ENGLISH,
            this.response, this.authentication, this.request);

		assertEquals(bytes, new byte[]{7, 8, 9});
		verify(this.response).setHeader(anyString(), anyString());
	}

	@Test
	public void shouldWrapDiplomasEmptyPdfAsBadRequest() throws InvalidXmlElementException, EmptyPdfBodyException {
		final TournamentDTO tournamentDTO = this.tournamentWithName("Winter Cup");
		final DiplomaPDF diplomaPDF = mock(DiplomaPDF.class);
		when(this.tournamentController.get(7)).thenReturn(tournamentDTO);
		when(this.pdfController.generateTournamentDiplomas(eq(tournamentDTO), eq(true), eq("editor"), isNull(), any()))
				.thenReturn(diplomaPDF);
		when(diplomaPDF.generate()).thenThrow(new EmptyPdfBodyException("empty"));

		expectThrows(BadRequestException.class, () -> this.tournamentServices.getAllDiplomasFromTournamentAsPdf(7, null,
				true, null, Locale.ENGLISH, this.response, this.authentication, this.request));
	}

	@Test
	public void shouldGenerateParticipantDiplomaAndSetHeader()
			throws InvalidXmlElementException, EmptyPdfBodyException {
		final TournamentDTO tournamentDTO = this.tournamentWithName("Summer Cup");
		final ParticipantDTO participantDTO = new ParticipantDTO();
		participantDTO.setName("Miyamoto");
		participantDTO.setLastname("Musashi");
		final DiplomaPDF diplomaPDF = mock(DiplomaPDF.class);
		when(this.tournamentController.get(8)).thenReturn(tournamentDTO);
		when(this.pdfController.generateTournamentDiploma(tournamentDTO, participantDTO)).thenReturn(diplomaPDF);
		when(diplomaPDF.generate()).thenReturn(new byte[]{10, 11, 12});

		final byte[] bytes = this.tournamentServices.getParticipantDiplomaFromTournamentAsPdf(8, participantDTO,
				Locale.ENGLISH, this.response, this.request);

		assertEquals(bytes, new byte[]{10, 11, 12});
		verify(this.response).setHeader(anyString(), anyString());
	}

	@Test
	public void shouldThrowWhenParticipantIsNullForDiploma() {
		expectThrows(InvalidRequestException.class, () -> this.tournamentServices.getParticipantDiplomaFromTournamentAsPdf(8,
				null, Locale.ENGLISH, this.response, this.request));
	}

	@Test
	public void shouldDelegateCloneAndNumberOfWinners() {
		final TournamentDTO cloned = this.tournamentWithName("Cloned Cup");
		when(this.tournamentController.clone(9, "editor")).thenReturn(cloned);
		doNothing().when(this.tournamentController).setNumberOfWinners(9, 3, "editor");

		final TournamentDTO result = this.tournamentServices.clone(9, this.authentication, this.request);
        this.tournamentServices.numberOfWinners(9, 3, this.authentication, this.request);

		assertNotNull(result);
		verify(this.tournamentController).clone(9, "editor");
		verify(this.tournamentController).setNumberOfWinners(9, 3, "editor");
	}

	@Test
	public void shouldGetLastUnlockedTournament() {
		final TournamentDTO tournamentDTO = this.tournamentWithName("Unlocked Cup");
		when(this.tournamentController.getLatestUnlocked()).thenReturn(tournamentDTO);

		final TournamentDTO result = this.tournamentServices.getLastUnlockedTournament();

		assertNotNull(result);
		verify(this.tournamentController).getLatestUnlocked();
	}

	@Test
	public void shouldGetTournamentById() {
		final TournamentDTO tournamentDTO = this.tournamentWithName("T1");
		when(this.tournamentController.get(1)).thenReturn(tournamentDTO);

		final TournamentDTO result = this.tournamentServices.get(1, this.request);

		assertNotNull(result);
		verify(this.tournamentController).get(1);
	}

	private TournamentDTO tournamentWithName(String name) {
		final TournamentDTO dto = new TournamentDTO();
		dto.setName(name);
		return dto;
	}
}
