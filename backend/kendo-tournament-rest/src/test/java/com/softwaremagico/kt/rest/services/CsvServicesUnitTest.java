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

import com.softwaremagico.kt.core.controller.CsvController;
import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.core.controller.models.GroupLinkDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.exceptions.InvalidCsvRowException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test(groups = "csvServicesUnit")
public class CsvServicesUnitTest {

	@Mock
	private CsvController csvController;

	@Mock
	private Authentication authentication;

	private CsvServices csvServices;

	@BeforeMethod(alwaysRun = true)
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		csvServices = new CsvServices(csvController);
		when(authentication.getName()).thenReturn("tester");
	}

	private MockMultipartFile file() {
		return new MockMultipartFile("file", "file.csv", "text/csv", "content".getBytes());
	}

	@Test
	public void shouldAddClubsSuccessfully() throws Exception {
		when(csvController.addClubs(anyString(), eq("tester"))).thenReturn(List.of());

		final List<ClubDTO> result = csvServices.addClubs(file(), authentication, mock(jakarta.servlet.http.HttpServletRequest.class));

		assertTrue(result.isEmpty());
	}

	@Test(expectedExceptions = InvalidCsvRowException.class)
	public void shouldThrowWhenClubsFail() throws Exception {
		when(csvController.addClubs(anyString(), eq("tester"))).thenReturn(List.of(new ClubDTO()));

		csvServices.addClubs(file(), authentication, mock(jakarta.servlet.http.HttpServletRequest.class));
	}

	@Test
	public void shouldAddParticipantsSuccessfully() throws Exception {
		when(csvController.addParticipants(anyString(), eq("tester"))).thenReturn(List.of());

		final List<ParticipantDTO> result = csvServices.addParticipants(file(), authentication, mock(jakarta.servlet.http.HttpServletRequest.class));

		assertTrue(result.isEmpty());
	}

	@Test(expectedExceptions = InvalidCsvRowException.class)
	public void shouldThrowWhenParticipantsFail() throws Exception {
		when(csvController.addParticipants(anyString(), eq("tester"))).thenReturn(List.of(new ParticipantDTO()));

		csvServices.addParticipants(file(), authentication, mock(jakarta.servlet.http.HttpServletRequest.class));
	}

	@Test
	public void shouldAddTeamsWithoutTournamentSuccessfully() throws Exception {
		when(csvController.addTeams(anyString(), isNull(), eq("tester"))).thenReturn(List.of());

		final List<TeamDTO> result = csvServices.addTeams(file(), authentication, mock(jakarta.servlet.http.HttpServletRequest.class));

		assertTrue(result.isEmpty());
	}

	@Test(expectedExceptions = InvalidCsvRowException.class)
	public void shouldThrowWhenTeamsWithoutTournamentFail() throws Exception {
		when(csvController.addTeams(anyString(), isNull(), eq("tester"))).thenReturn(List.of(new TeamDTO()));

		csvServices.addTeams(file(), authentication, mock(jakarta.servlet.http.HttpServletRequest.class));
	}

	@Test
	public void shouldAddTeamsForTournamentSuccessfully() throws Exception {
		when(csvController.addTeams(anyString(), eq(5), eq("tester"))).thenReturn(List.of());

		final List<TeamDTO> result = csvServices.addTeams(5, file(), authentication, mock(jakarta.servlet.http.HttpServletRequest.class));

		assertTrue(result.isEmpty());
	}

	@Test(expectedExceptions = InvalidCsvRowException.class)
	public void shouldThrowWhenTeamsForTournamentFail() throws Exception {
		when(csvController.addTeams(anyString(), eq(5), eq("tester"))).thenReturn(List.of(new TeamDTO()));

		csvServices.addTeams(5, file(), authentication, mock(jakarta.servlet.http.HttpServletRequest.class));
	}

	@Test
	public void shouldAddGroupLinksSuccessfully() throws Exception {
		when(csvController.addGroupLinks(eq(5), anyString(), eq("tester"))).thenReturn(List.of());

		final List<GroupLinkDTO> result = csvServices.addGroupLinks(5, file(), authentication, mock(jakarta.servlet.http.HttpServletRequest.class));

		assertTrue(result.isEmpty());
	}

	@Test(expectedExceptions = InvalidCsvRowException.class)
	public void shouldThrowWhenGroupLinksFail() throws Exception {
		when(csvController.addGroupLinks(eq(5), anyString(), eq("tester"))).thenReturn(List.of(new GroupLinkDTO()));

		csvServices.addGroupLinks(5, file(), authentication, mock(jakarta.servlet.http.HttpServletRequest.class));
	}
}

