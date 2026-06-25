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

import com.softwaremagico.kt.core.controller.FightStatisticsController;
import com.softwaremagico.kt.core.controller.ParticipantController;
import com.softwaremagico.kt.core.controller.ParticipantStatisticsController;
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.TournamentStatisticsController;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantStatisticsDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.controller.models.TournamentFightStatisticsDTO;
import com.softwaremagico.kt.core.controller.models.TournamentStatisticsDTO;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.rest.exceptions.InvalidRequestException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class StatisticsServicesTest {

	@Mock
	private TournamentController mockTournamentController;

	@Mock
	private FightStatisticsController mockFightStatisticsController;

	@Mock
	private TournamentStatisticsController mockTournamentStatisticsController;

	@Mock
	private ParticipantStatisticsController mockParticipantStatisticsController;

	@Mock
	private ParticipantController mockParticipantController;

	@Mock
	private ParticipantProvider mockParticipantProvider;

	private StatisticsServices service;

	@BeforeMethod(alwaysRun = true)
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		this.service = new StatisticsServices(this.mockTournamentController, this.mockFightStatisticsController,
				this.mockTournamentStatisticsController, this.mockParticipantStatisticsController,
				this.mockParticipantController, this.mockParticipantProvider);
	}

	@Test(groups = "statisticsServices")
	public void getStatisticsFromTournament_shouldReturnFightStatistics() {
		final Integer tournamentId = 1;
		final TournamentDTO tournamentDTO = new TournamentDTO();
		final TournamentFightStatisticsDTO expectedStats = new TournamentFightStatisticsDTO();
		expectedStats.setFightsNumber(10L);

		when(this.mockTournamentController.get(tournamentId)).thenReturn(tournamentDTO);
		when(this.mockFightStatisticsController.estimate(tournamentDTO)).thenReturn(expectedStats);

		final TournamentFightStatisticsDTO result = this.service.getStatisticsFromTournament(tournamentId,
				Optional.empty(), Optional.empty(), null);

		assertNotNull(result);
		assertEquals(result.getFightsNumber(), 10L);
		verify(this.mockTournamentController).get(tournamentId);
		verify(this.mockFightStatisticsController).estimate(tournamentDTO);
	}

	@Test(groups = "statisticsServices")
	public void getStatisticsFromTournament_shouldCalculateByTeamsWhenRequested() {
		final Integer tournamentId = 1;
		final TournamentDTO tournamentDTO = new TournamentDTO();
		final TournamentFightStatisticsDTO expectedStats = new TournamentFightStatisticsDTO();
		expectedStats.setFightsByTeam(5);

		when(this.mockTournamentController.get(tournamentId)).thenReturn(tournamentDTO);
		when(this.mockFightStatisticsController.estimateByTeams(tournamentDTO)).thenReturn(expectedStats);

		final TournamentFightStatisticsDTO result = this.service.getStatisticsFromTournament(tournamentId,
				Optional.of(true), Optional.empty(), null);

		assertNotNull(result);
		assertEquals((long) result.getFightsByTeam(), 5L);
		verify(this.mockFightStatisticsController).estimateByTeams(tournamentDTO);
	}

	@Test(groups = "statisticsServices")
	public void getStatisticsFromTournament_shouldCalculateByMembersWhenRequested() {
		final Integer tournamentId = 1;
		final TournamentDTO tournamentDTO = new TournamentDTO();
		final TournamentFightStatisticsDTO expectedStats = new TournamentFightStatisticsDTO();
		expectedStats.setDuelsNumber(30L);

		when(this.mockTournamentController.get(tournamentId)).thenReturn(tournamentDTO);
		when(this.mockFightStatisticsController.estimateByMembers(tournamentDTO)).thenReturn(expectedStats);

		final TournamentFightStatisticsDTO result = this.service.getStatisticsFromTournament(tournamentId,
				Optional.empty(), Optional.of(true), null);

		assertNotNull(result);
		assertEquals(result.getDuelsNumber(), 30L);
		verify(this.mockFightStatisticsController).estimateByMembers(tournamentDTO);
	}

	@Test(groups = "statisticsServices")
	public void getStatisticsFromTournament_shouldPrioritizeMembersOverTeams() {
		final Integer tournamentId = 1;
		final TournamentDTO tournamentDTO = new TournamentDTO();
		final TournamentFightStatisticsDTO expectedStats = new TournamentFightStatisticsDTO();

		when(this.mockTournamentController.get(tournamentId)).thenReturn(tournamentDTO);
		when(this.mockFightStatisticsController.estimateByMembers(tournamentDTO)).thenReturn(expectedStats);

		this.service.getStatisticsFromTournament(tournamentId, Optional.of(true), Optional.of(true), null);

		verify(this.mockFightStatisticsController).estimateByMembers(tournamentDTO);
	}

	@Test(groups = "statisticsServices")
	public void getTournamentStatistics_shouldReturnAggregatedStats() {
		final Integer tournamentId = 1;
		final TournamentDTO tournamentDTO = new TournamentDTO();
		final TournamentStatisticsDTO expectedStats = new TournamentStatisticsDTO();
		expectedStats.setNumberOfTeams(4L);

		final Authentication auth = mock(Authentication.class);
		when(auth.getName()).thenReturn("testuser");
		when(this.mockTournamentController.get(tournamentId)).thenReturn(tournamentDTO);
		when(this.mockTournamentStatisticsController.get(tournamentDTO)).thenReturn(expectedStats);

		final TournamentStatisticsDTO result = this.service.getStatisticsFromTournament(tournamentId, auth, null);

		assertNotNull(result);
		assertEquals(result.getNumberOfTeams(), 4L);
		assertEquals(result.getCreatedBy(), "testuser");
		assertNotNull(result.getCreatedAt());
		verify(this.mockTournamentStatisticsController).get(tournamentDTO);
	}

	@Test(groups = "statisticsServices")
	public void getPreviousTournaments_shouldReturnStatisticsWithDefaultNumber() {
		final Integer tournamentId = 1;
		final TournamentDTO tournamentDTO = new TournamentDTO();
		final List<TournamentDTO> previousTournaments = List.of(new TournamentDTO());
		final TournamentStatisticsDTO stats = new TournamentStatisticsDTO();

		final Authentication auth = mock(Authentication.class);
		when(auth.getName()).thenReturn("testuser");
		when(this.mockTournamentController.get(tournamentId)).thenReturn(tournamentDTO);
		when(this.mockTournamentController.getPreviousTo(tournamentDTO, 1)).thenReturn(previousTournaments);
		when(this.mockTournamentStatisticsController.get(previousTournaments.get(0))).thenReturn(stats);

		final List<TournamentStatisticsDTO> result = this.service.getStatisticsFromPreviousTournament(tournamentId,
				null, auth, null);

		assertNotNull(result);
		assertEquals(result.size(), 1);
		verify(this.mockTournamentController).getPreviousTo(tournamentDTO, 1);
	}

	@Test(groups = "statisticsServices")
	public void getPreviousTournaments_shouldReturnMultipleStatistics() {
		final Integer tournamentId = 1;
		final Integer number = 5;
		final TournamentDTO tournamentDTO = new TournamentDTO();
		final List<TournamentDTO> previousTournaments = new ArrayList<>();
		for (int i = 0; i < number; i++) {
			final TournamentDTO previousTournament = new TournamentDTO();
			previousTournament.setName("Tournament " + i);
			previousTournaments.add(previousTournament);
		}

		final Authentication auth = mock(Authentication.class);
		when(auth.getName()).thenReturn("testuser");
		when(this.mockTournamentController.get(tournamentId)).thenReturn(tournamentDTO);
		when(this.mockTournamentController.getPreviousTo(tournamentDTO, number)).thenReturn(previousTournaments);

		for (final TournamentDTO tournamentDTO1 : previousTournaments) {
			final TournamentStatisticsDTO stats = new TournamentStatisticsDTO();
			when(this.mockTournamentStatisticsController.get(tournamentDTO1)).thenReturn(stats);
		}

		final List<TournamentStatisticsDTO> result = this.service.getStatisticsFromPreviousTournament(tournamentId,
				number, auth, null);

		assertNotNull(result);
		assertEquals(result.size(), number);
	}

	@Test(groups = "statisticsServices")
	public void getParticipantStatistics_shouldReturnStatisticsForRequester() {
		final Integer participantId = 1;
		final ParticipantDTO participantDTO = new ParticipantDTO();
		final Participant participant = new Participant();
		participant.setId(participantId);
		final ParticipantStatisticsDTO expectedStats = new ParticipantStatisticsDTO();

		final Authentication auth = mock(Authentication.class);
		when(auth.getName()).thenReturn("testuser");
		when(this.mockParticipantProvider.findByTokenUsername("testuser")).thenReturn(Optional.of(participant));
		when(this.mockParticipantController.get(participantId)).thenReturn(participantDTO);
		when(this.mockParticipantStatisticsController.get(participantDTO)).thenReturn(expectedStats);

		final ParticipantStatisticsDTO result = this.service.getStatisticsFromParticipant(participantId, auth, null);

		assertNotNull(result);
		assertEquals(result.getCreatedBy(), "testuser");
		verify(this.mockParticipantStatisticsController).get(participantDTO);
	}

	@Test(groups = "statisticsServices")
	public void getParticipantStatistics_shouldThrowExceptionWhenAccessingOtherUserStats() {
		final Integer participantId = 2;
		final Participant authenticatedParticipant = new Participant();
		authenticatedParticipant.setId(1);

		final Authentication auth = mock(Authentication.class);
		when(auth.getName()).thenReturn("testuser");
		when(this.mockParticipantProvider.findByTokenUsername("testuser"))
				.thenReturn(Optional.of(authenticatedParticipant));

		assertThrows(InvalidRequestException.class,
				() -> this.service.getStatisticsFromParticipant(participantId, auth, null));
	}

	@Test(groups = "statisticsServices")
	public void getParticipantStatistics_shouldAllowNullAuthenticationForPublicAccess() {
		final Integer participantId = 1;
		final ParticipantDTO participantDTO = new ParticipantDTO();
		final ParticipantStatisticsDTO expectedStats = new ParticipantStatisticsDTO();

		when(this.mockParticipantController.get(participantId)).thenReturn(participantDTO);
		when(this.mockParticipantStatisticsController.get(participantDTO)).thenReturn(expectedStats);

		final ParticipantStatisticsDTO result = this.service.getStatisticsFromParticipant(participantId, null, null);

		assertNotNull(result);
		assertNotNull(result.getCreatedAt());
		verify(this.mockParticipantStatisticsController).get(participantDTO);
	}

	@Test(groups = "statisticsServices")
	public void getYourWorstNightmare_shouldReturnNightmareList() {
		final Integer participantId = 1;
		final ParticipantDTO participantDTO = new ParticipantDTO();
		final List<ParticipantDTO> nightmares = List.of(new ParticipantDTO(), new ParticipantDTO());
		final Participant participant = new Participant();
		participant.setId(participantId);

		final Authentication auth = mock(Authentication.class);
		when(auth.getName()).thenReturn("testuser");
		when(this.mockParticipantProvider.findByTokenUsername("testuser")).thenReturn(Optional.of(participant));
		when(this.mockParticipantController.get(participantId)).thenReturn(participantDTO);
		when(this.mockParticipantController.getYourWorstNightmare(participantDTO)).thenReturn(nightmares);

		final List<ParticipantDTO> result = this.service.getYourWorstNightmareFromParticipant(participantId, auth,
				null);

		assertNotNull(result);
		assertEquals(result.size(), 2);
		verify(this.mockParticipantController).getYourWorstNightmare(participantDTO);
	}

	@Test(groups = "statisticsServices")
	public void getWorstNightmareOf_shouldReturnNightmareOfList() {
		final Integer participantId = 1;
		final ParticipantDTO participantDTO = new ParticipantDTO();
		final List<ParticipantDTO> worstOf = List.of(new ParticipantDTO());
		final Participant participant = new Participant();
		participant.setId(participantId);

		final Authentication auth = mock(Authentication.class);
		when(auth.getName()).thenReturn("testuser");
		when(this.mockParticipantProvider.findByTokenUsername("testuser")).thenReturn(Optional.of(participant));
		when(this.mockParticipantController.get(participantId)).thenReturn(participantDTO);
		when(this.mockParticipantController.getYouAreTheWorstNightmareOf(participantDTO)).thenReturn(worstOf);

		final List<ParticipantDTO> result = this.service.getWorstNightmareOf(participantId, auth, null);

		assertNotNull(result);
		assertEquals(result.size(), 1);
		verify(this.mockParticipantController).getYouAreTheWorstNightmareOf(participantDTO);
	}

	@Test(groups = "statisticsServices")
	public void getYourWorstNightmare_shouldThrowExceptionWhenAccessingOtherUserStats() {
		final Integer participantId = 2;
		final Participant authenticatedParticipant = new Participant();
		authenticatedParticipant.setId(1);

		final Authentication auth = mock(Authentication.class);
		when(auth.getName()).thenReturn("testuser");
		when(this.mockParticipantProvider.findByTokenUsername("testuser"))
				.thenReturn(Optional.of(authenticatedParticipant));

		assertThrows(InvalidRequestException.class,
				() -> this.service.getYourWorstNightmareFromParticipant(participantId, auth, null));
	}

	@Test(groups = "statisticsServices")
	public void getWorstNightmareOf_shouldThrowExceptionWhenAccessingOtherUserStats() {
		final Integer participantId = 2;
		final Participant authenticatedParticipant = new Participant();
		authenticatedParticipant.setId(1);

		final Authentication auth = mock(Authentication.class);
		when(auth.getName()).thenReturn("testuser");
		when(this.mockParticipantProvider.findByTokenUsername("testuser"))
				.thenReturn(Optional.of(authenticatedParticipant));

		assertThrows(InvalidRequestException.class, () -> this.service.getWorstNightmareOf(participantId, auth, null));
	}
}
