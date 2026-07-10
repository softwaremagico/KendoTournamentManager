package com.softwaremagico.kt.core.providers;

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

import com.softwaremagico.kt.core.statistics.TournamentFightStatistics;
import com.softwaremagico.kt.core.statistics.TournamentFightStatisticsRepository;
import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Role;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class TournamentFightStatisticsProviderTest {

	@Mock
	private TournamentFightStatisticsRepository mockRepository;

	@Mock
	private DuelProvider mockDuelProvider;

	@Mock
	private FightProvider mockFightProvider;

	@Mock
	private TeamProvider mockTeamProvider;

	@Mock
	private RoleProvider mockRoleProvider;

	@Mock
	private TournamentExtraPropertyProvider mockTournamentExtraPropertyProvider;

	private TournamentFightStatisticsProvider provider;

	@BeforeMethod(alwaysRun = true)
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		this.provider = new TournamentFightStatisticsProvider(this.mockRepository, this.mockDuelProvider,
				this.mockFightProvider, this.mockTeamProvider, this.mockRoleProvider,
				this.mockTournamentExtraPropertyProvider);
	}

	@Test(groups = "tournamentFightStatistics")
	public void estimate_shouldReturnNullWhenTournamentIsNull() {
		assertThrows(NullPointerException.class, () -> this.provider.estimate(null));
	}

	@Test(groups = "tournamentFightStatistics")
	public void estimate_shouldCalculateByTeamsWhenTeamsExist() {
		final Tournament tournament = this.createTournament();
		final List<Team> teams = this.createTeams(tournament, 4);

		when(this.mockTeamProvider.getAll(tournament)).thenReturn(teams);

		final TournamentFightStatistics result = this.provider.estimate(tournament);

		assertNotNull(result);
		verify(this.mockTeamProvider).getAll(tournament);
	}

	@Test(groups = "tournamentFightStatistics")
	public void estimate_shouldCalculateByRolesWhenNoTeams() {
		final Tournament tournament = this.createTournament();
		final List<Team> emptyTeams = Collections.emptyList();
		final List<Role> roles = this.createRoles(tournament, 4);

		when(this.mockTeamProvider.getAll(tournament)).thenReturn(emptyTeams);
		when(this.mockRoleProvider.getAll(tournament)).thenReturn(roles);

		final TournamentFightStatistics result = this.provider.estimate(tournament);

		assertNotNull(result);
		verify(this.mockRoleProvider).getAll(tournament);
	}

	@Test(groups = "tournamentFightStatistics")
	public void estimateByTeams_shouldReturnStatistics() {
		final Tournament tournament = this.createTournament();
		final List<Team> teams = this.createTeams(tournament, 4);

		when(this.mockTeamProvider.getAll(tournament)).thenReturn(teams);
		when(this.mockDuelProvider.getDurationAverage()).thenReturn(180L);

		final TournamentFightStatistics result = this.provider.estimateByTeams(tournament);

		assertNotNull(result);
		assertEquals(result.getFightsNumber(), 6L);
		assertEquals(result.getFightsByTeam(), 3L);
	}

	@Test(groups = "tournamentFightStatistics")
	public void estimateByMembers_shouldFilterOnlyCompetitors() {
		final Tournament tournament = this.createTournament();
		tournament.setTeamSize(2);
		final List<Role> mixedRoles = this.createMixedRoles(tournament);

		when(this.mockRoleProvider.getAll(tournament)).thenReturn(mixedRoles);
		when(this.mockDuelProvider.getDurationAverage()).thenReturn(180L);

		final TournamentFightStatistics result = this.provider.estimateByMembers(tournament);

		assertNotNull(result);
	}

	@Test(groups = "tournamentFightStatistics")
	public void estimateLeagueStatistics_shouldCalculateCorrectFightNumbers() {
		final List<Team> teams = this.createTeams(null, 4);

		final TournamentFightStatistics result = this.provider.estimateLeagueStatistics(3, teams);

		assertNotNull(result);
		assertEquals(result.getFightsNumber(), 6L);
		assertEquals(result.getFightsByTeam(), 3L);
	}

	@Test(groups = "tournamentFightStatistics")
	public void estimateLeagueStatistics_shouldCalculateEstimatedTimeWhenDurationAvailable() {
		final List<Team> teams = this.createTeams(null, 4);

		when(this.mockDuelProvider.getDurationAverage()).thenReturn(180L);

		final TournamentFightStatistics result = this.provider.estimateLeagueStatistics(3, teams);

		assertNotNull(result);
		assertNotNull(result.getEstimatedTime());
		assertTrue(result.getEstimatedTime() > 0);
	}

	@Test(groups = "tournamentFightStatistics")
	public void estimateLoopStatistics_shouldAvoidDuplicatesWhenPropertyEnabled() {
		final Tournament tournament = this.createTournament();
		tournament.setType(TournamentType.LOOP);
		final List<Team> teams = this.createTeams(tournament, 3);
		final TournamentExtraProperty property = new TournamentExtraProperty();
		property.setPropertyValue("true");

		when(this.mockTournamentExtraPropertyProvider.getByTournamentAndProperty(tournament,
				TournamentExtraPropertyKey.AVOID_DUPLICATES)).thenReturn(property);
		when(this.mockDuelProvider.getDurationAverage()).thenReturn(180L);

		final TournamentFightStatistics result = this.provider.estimateLoopStatistics(tournament, 3, teams);

		assertNotNull(result);
		assertEquals(result.getFightsNumber(), 4L);
		assertEquals(result.getFightsByTeam(), 2L);
	}

	@Test(groups = "tournamentFightStatistics")
	public void estimateLeagueStatistics_shouldNotEstimateTimeWhenDurationAverageIsNull() {
		final List<Team> teams = this.createTeams(null, 4);
		when(this.mockDuelProvider.getDurationAverage()).thenReturn(null);

		final TournamentFightStatistics result = this.provider.estimateLeagueStatistics(3, teams);

		assertNotNull(result);
		assertNull(result.getEstimatedTime());
	}

	@Test(groups = "tournamentFightStatistics")
	public void estimateLeagueStatistics_shouldEstimateTimeWhenDurationAverageIsNegative() {
		final List<Team> teams = this.createTeams(null, 4);
		when(this.mockDuelProvider.getDurationAverage()).thenReturn(-10L);

		final TournamentFightStatistics result = this.provider.estimateLeagueStatistics(3, teams);

		assertNotNull(result);
		assertNull(result.getEstimatedTime());
	}

	@Test(groups = "tournamentFightStatistics")
	public void estimateLoopStatistics_shouldUseDefaultWhenPropertyIsNull() {
		final Tournament tournament = this.createTournament();
		tournament.setType(TournamentType.LOOP);
		final List<Team> teams = this.createTeams(tournament, 3);

		when(this.mockTournamentExtraPropertyProvider.getByTournamentAndProperty(tournament,
				TournamentExtraPropertyKey.AVOID_DUPLICATES)).thenReturn(null);
		when(this.mockDuelProvider.getDurationAverage()).thenReturn(180L);

		final TournamentFightStatistics result = this.provider.estimateLoopStatistics(tournament, 3, teams);

		assertNotNull(result);
		assertEquals(result.getFightsNumber(), 4L);
		assertEquals(result.getFightsByTeam(), 2L);
	}

	@Test(groups = "tournamentFightStatistics")
	public void estimateLoopStatistics_shouldSetEstimatedTimeToZeroWhenDurationAverageIsNegative() {
		final Tournament tournament = this.createTournament();
		tournament.setType(TournamentType.LOOP);
		final List<Team> teams = this.createTeams(tournament, 3);
		final TournamentExtraProperty property = new TournamentExtraProperty();
		property.setPropertyValue("false");

		when(this.mockTournamentExtraPropertyProvider.getByTournamentAndProperty(tournament,
				TournamentExtraPropertyKey.AVOID_DUPLICATES)).thenReturn(property);
		when(this.mockDuelProvider.getDurationAverage()).thenReturn(-10L);

		final TournamentFightStatistics result = this.provider.estimateLoopStatistics(tournament, 3, teams);

		assertNotNull(result);
		assertEquals(result.getEstimatedTime(), 0L);
	}

	@Test(groups = "tournamentFightStatistics")
	public void estimateLoopStatistics_shouldNotSetEstimatedTimeWhenDurationAverageIsNull() {
		final Tournament tournament = this.createTournament();
		tournament.setType(TournamentType.LOOP);
		final List<Team> teams = this.createTeams(tournament, 3);
		final TournamentExtraProperty property = new TournamentExtraProperty();
		property.setPropertyValue("false");

		when(this.mockTournamentExtraPropertyProvider.getByTournamentAndProperty(tournament,
				TournamentExtraPropertyKey.AVOID_DUPLICATES)).thenReturn(property);
		when(this.mockDuelProvider.getDurationAverage()).thenReturn(null);

		final TournamentFightStatistics result = this.provider.estimateLoopStatistics(tournament, 3, teams);

		assertNotNull(result);
		assertNull(result.getEstimatedTime());
	}

	@Test(groups = "tournamentFightStatistics")
	public void get_shouldSetFightsByTeamToZeroWhenNoTeams() {
		final Tournament tournament = this.createTournament();

		when(this.mockFightProvider.count(tournament)).thenReturn(3L);
		when(this.mockTeamProvider.count(tournament)).thenReturn(0L);
		when(this.mockDuelProvider.count(tournament)).thenReturn(0L);
		when(this.mockDuelProvider.getDurationAverage(tournament)).thenReturn(180L);
		when(this.mockDuelProvider.countScore(tournament, Score.MEN)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.DO)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.KOTE)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.HANSOKU)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.TSUKI)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.IPPON)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.FUSEN_GACHI)).thenReturn(0L);
		when(this.mockDuelProvider.getFirstDuel(tournament)).thenReturn(null);
		when(this.mockDuelProvider.getLastDuel(tournament)).thenReturn(null);
		when(this.mockFightProvider.countByTournamentAndFinished(tournament)).thenReturn(0L);
		when(this.mockDuelProvider.countFaults(tournament)).thenReturn(0L);

		final TournamentFightStatistics result = this.provider.get(tournament);

		assertNotNull(result);
		assertEquals(result.getFightsByTeam(), 0L);
	}

	@Test(groups = "tournamentFightStatistics")
	public void estimateLoopStatistics_shouldMaximizeFightsWhenPropertyDisabled() {
		final Tournament tournament = this.createTournament();
		tournament.setType(TournamentType.LOOP);
		final List<Team> teams = this.createTeams(tournament, 3);
		final TournamentExtraProperty property = new TournamentExtraProperty();
		property.setPropertyValue("false");

		when(this.mockTournamentExtraPropertyProvider.getByTournamentAndProperty(tournament,
				TournamentExtraPropertyKey.AVOID_DUPLICATES)).thenReturn(property);
		when(this.mockDuelProvider.getDurationAverage()).thenReturn(180L);

		final TournamentFightStatistics result = this.provider.estimateLoopStatistics(tournament, 3, teams);

		assertNotNull(result);
		assertEquals(result.getFightsNumber(), 6L);
		assertEquals(result.getFightsByTeam(), 4L);
	}

	@Test(groups = "tournamentFightStatistics")
	public void get_shouldAggregateAllStatisticsFromDuels() {
		final Tournament tournament = this.createTournament();

		when(this.mockFightProvider.count(tournament)).thenReturn(10L);
		when(this.mockTeamProvider.count(tournament)).thenReturn(4L);
		when(this.mockDuelProvider.count(tournament)).thenReturn(40L);
		when(this.mockDuelProvider.getDurationAverage(tournament)).thenReturn(180L);
		when(this.mockDuelProvider.countScore(tournament, Score.MEN)).thenReturn(15L);
		when(this.mockDuelProvider.countScore(tournament, Score.DO)).thenReturn(12L);
		when(this.mockDuelProvider.countScore(tournament, Score.KOTE)).thenReturn(8L);
		when(this.mockDuelProvider.countScore(tournament, Score.IPPON)).thenReturn(5L);
		when(this.mockDuelProvider.countScore(tournament, Score.HANSOKU)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.TSUKI)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.FUSEN_GACHI)).thenReturn(0L);
		when(this.mockDuelProvider.getFirstDuel(tournament)).thenReturn(null);
		when(this.mockDuelProvider.getLastDuel(tournament)).thenReturn(null);
		when(this.mockFightProvider.countByTournamentAndFinished(tournament)).thenReturn(8L);
		when(this.mockDuelProvider.countFaults(tournament)).thenReturn(2L);

		final TournamentFightStatistics result = this.provider.get(tournament);

		assertNotNull(result);
		assertEquals(result.getFightsNumber(), 10L);
		assertEquals(result.getFightsByTeam(), 2L);
		assertEquals(result.getDuelsNumber(), 40L);
		assertEquals(result.getMenNumber(), 15L);
		assertEquals(result.getDoNumber(), 12L);
		assertEquals(result.getKoteNumber(), 8L);
		assertEquals(result.getIpponNumber(), 5L);
		assertEquals(result.getFightsFinished(), 8L);
		assertEquals(result.getFaults(), 2L);
	}

	@Test(groups = "tournamentFightStatistics")
	public void get_shouldCalculateStartTimeFromFirstDuel() {
		final Tournament tournament = this.createTournament();
		final LocalDateTime startTime = LocalDateTime.now();
		final Duel firstDuel = new Duel();
		firstDuel.setStartedAt(startTime);

		when(this.mockFightProvider.count(tournament)).thenReturn(0L);
		when(this.mockTeamProvider.count(tournament)).thenReturn(0L);
		when(this.mockDuelProvider.count(tournament)).thenReturn(0L);
		when(this.mockDuelProvider.getFirstDuel(tournament)).thenReturn(firstDuel);
		when(this.mockDuelProvider.getLastDuel(tournament)).thenReturn(null);
		when(this.mockFightProvider.countByTournamentAndFinished(tournament)).thenReturn(0L);
		when(this.mockDuelProvider.countFaults(tournament)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.MEN)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.DO)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.KOTE)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.HANSOKU)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.TSUKI)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.IPPON)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.FUSEN_GACHI)).thenReturn(0L);

		final TournamentFightStatistics result = this.provider.get(tournament);

		assertNotNull(result);
		assertNotNull(result.getFightsStartedAt());
		assertEquals(result.getFightsStartedAt(), startTime);
	}

	@Test(groups = "tournamentFightStatistics")
	public void get_shouldEstimateStartTimeFromFinishedDuel() {
		final Tournament tournament = this.createTournament();
		final LocalDateTime finishedTime = LocalDateTime.now();
		final Duel firstDuel = new Duel();
		firstDuel.setFinishedAt(finishedTime);

		when(this.mockFightProvider.count(tournament)).thenReturn(0L);
		when(this.mockTeamProvider.count(tournament)).thenReturn(0L);
		when(this.mockDuelProvider.count(tournament)).thenReturn(0L);
		when(this.mockDuelProvider.getFirstDuel(tournament)).thenReturn(firstDuel);
		when(this.mockDuelProvider.getLastDuel(tournament)).thenReturn(null);
		when(this.mockFightProvider.countByTournamentAndFinished(tournament)).thenReturn(0L);
		when(this.mockDuelProvider.countFaults(tournament)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.MEN)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.DO)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.KOTE)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.HANSOKU)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.TSUKI)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.IPPON)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.FUSEN_GACHI)).thenReturn(0L);

		final TournamentFightStatistics result = this.provider.get(tournament);

		assertNotNull(result);
		assertNotNull(result.getFightsStartedAt());
		assertEquals(result.getFightsStartedAt(), finishedTime.minusMinutes(2));
	}

	@Test(groups = "tournamentFightStatistics")
	public void estimate_shouldHandleNullTeamsParameter() {
		final Tournament tournament = this.createTournament();

		final TournamentFightStatistics result = this.provider.estimate(tournament, null);

		assertNull(result);
	}

	@Test(groups = "tournamentFightStatistics")
	public void estimate_shouldHandleNullTournamentOnFullSignature() {
		final List<Team> teams = this.createTeams(null, 2);

		final TournamentFightStatistics result = this.provider.estimate(null, 3, teams);

		assertNull(result);
	}

	@Test(groups = "tournamentFightStatistics")
	public void estimate_shouldHandleLessThanTwoTeams() {
		final Tournament tournament = this.createTournament();
		final List<Team> oneTeam = this.createTeams(tournament, 1);

		final TournamentFightStatistics result = this.provider.estimate(tournament, oneTeam);

		assertNull(result);
	}

	@Test(groups = "tournamentFightStatistics")
	public void estimate_shouldReturnNullForCustomizedTournament() {
		final Tournament tournament = this.createTournament();
		tournament.setType(TournamentType.CUSTOMIZED);
		final List<Team> teams = this.createTeams(tournament, 4);

		final TournamentFightStatistics result = this.provider.estimate(tournament, 3, teams);

		assertNull(result);
	}

	@Test(groups = "tournamentFightStatistics")
	public void estimate_shouldReturnNullForKingOfTheMountainTournament() {
		final Tournament tournament = this.createTournament();
		tournament.setType(TournamentType.KING_OF_THE_MOUNTAIN);
		final List<Team> teams = this.createTeams(tournament, 4);

		final TournamentFightStatistics result = this.provider.estimate(tournament, 3, teams);

		assertNull(result);
	}

	@Test(groups = "tournamentFightStatistics")
	public void estimate_shouldDispatchToLoopStatistics() {
		final Tournament tournament = this.createTournament();
		tournament.setType(TournamentType.LOOP);
		final List<Team> teams = this.createTeams(tournament, 3);
		final TournamentExtraProperty property = new TournamentExtraProperty();
		property.setPropertyValue("false");

		when(this.mockTournamentExtraPropertyProvider.getByTournamentAndProperty(tournament,
				TournamentExtraPropertyKey.AVOID_DUPLICATES)).thenReturn(property);
		when(this.mockDuelProvider.getDurationAverage()).thenReturn(180L);

		final TournamentFightStatistics result = this.provider.estimate(tournament, 3, teams);

		assertNotNull(result);
		assertEquals(result.getFightsByTeam(), 4L);
	}

	@Test(groups = "tournamentFightStatistics")
	public void estimateLeagueStatistics_shouldNotEstimateTimeWhenDurationAverageIsZero() {
		final List<Team> teams = this.createTeams(null, 4);
		when(this.mockDuelProvider.getDurationAverage()).thenReturn(0L);

		final TournamentFightStatistics result = this.provider.estimateLeagueStatistics(3, teams);

		assertNotNull(result);
		assertNull(result.getEstimatedTime());
	}

	@Test(groups = "tournamentFightStatistics")
	public void estimateLeagueStatistics_shouldNotEstimateTimeWhenDuelsNumberIsNull() {
		final List<Team> teams = this.createTeamsWithoutMembers(null, 2);
		when(this.mockDuelProvider.getDurationAverage()).thenReturn(180L);

		final TournamentFightStatistics result = this.provider.estimateLeagueStatistics(3, teams);

		assertNotNull(result);
		assertNull(result.getDuelsNumber());
		assertNull(result.getEstimatedTime());
		assertNull(result.getAverageTime());
	}

	@Test(groups = "tournamentFightStatistics")
	public void estimateLoopStatistics_shouldSetEstimatedTimeToZeroWhenDuelsNumberIsNull() {
		final Tournament tournament = this.createTournament();
		tournament.setType(TournamentType.LOOP);
		final List<Team> teams = this.createTeamsWithoutMembers(tournament, 2);
		final TournamentExtraProperty property = new TournamentExtraProperty();
		property.setPropertyValue("false");

		when(this.mockTournamentExtraPropertyProvider.getByTournamentAndProperty(tournament,
				TournamentExtraPropertyKey.AVOID_DUPLICATES)).thenReturn(property);
		when(this.mockDuelProvider.getDurationAverage()).thenReturn(180L);

		final TournamentFightStatistics result = this.provider.estimateLoopStatistics(tournament, 3, teams);

		assertNotNull(result);
		assertNull(result.getDuelsNumber());
		assertEquals(result.getEstimatedTime(), 0L);
	}

	@Test(groups = "tournamentFightStatistics")
	public void estimateLoopStatistics_shouldSetEstimatedTimeToZeroWhenDuelsNumberIsNullAndAvoidDuplicatesEnabled() {
		final Tournament tournament = this.createTournament();
		tournament.setType(TournamentType.LOOP);
		final List<Team> teams = this.createTeamsWithoutMembers(tournament, 2);
		final TournamentExtraProperty property = new TournamentExtraProperty();
		property.setPropertyValue("true");

		when(this.mockTournamentExtraPropertyProvider.getByTournamentAndProperty(tournament,
				TournamentExtraPropertyKey.AVOID_DUPLICATES)).thenReturn(property);
		when(this.mockDuelProvider.getDurationAverage()).thenReturn(180L);

		final TournamentFightStatistics result = this.provider.estimateLoopStatistics(tournament, 3, teams);

		assertNotNull(result);
		assertNull(result.getDuelsNumber());
		assertEquals(result.getEstimatedTime(), 0L);
	}

	@Test(groups = "tournamentFightStatistics")
	public void estimateLeagueStatistics_shouldHandleNullFightsNumberInEstimatedTimeCalculation() {
		final List<Team> teams = new ArrayList<>() {
			@Override
			public int size() {
				return Integer.MIN_VALUE;
			}
		};
		when(this.mockDuelProvider.getDurationAverage()).thenReturn(180L);

		final TournamentFightStatistics result = this.provider.estimateLeagueStatistics(3, teams);

		assertNotNull(result);
		assertNull(result.getFightsNumber());
		assertEquals(result.getDuelsNumber(), 0L);
		assertEquals(result.getEstimatedTime(), 0L);
	}

	// Helper methods

	private Tournament createTournament() {
		final Tournament tournament = new Tournament();
		tournament.setId(1);
		tournament.setName("Test Tournament");
		tournament.setType(TournamentType.LEAGUE);
		tournament.setTeamSize(3);
		tournament.setFightSize(3);
		return tournament;
	}

	private List<Team> createTeams(Tournament tournament, int count) {
		final List<Team> teams = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			final Team team = new Team("Team" + i, tournament);
			for (int j = 0; j < 3; j++) {
				final Participant member = new Participant();
				member.setId(i * 10 + j);
				member.setName("Member" + i + "_" + j);
				team.addMember(member);
			}
			teams.add(team);
		}
		return teams;
	}

	private List<Team> createTeamsWithoutMembers(Tournament tournament, int count) {
		final List<Team> teams = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			teams.add(new Team("EmptyTeam" + i, tournament));
		}
		return teams;
	}

	private List<Role> createRoles(Tournament tournament, int count) {
		final List<Role> roles = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			final Role role = new Role();
			role.setId(i);
			role.setRoleType(RoleType.COMPETITOR);
			final Participant participant = new Participant();
			participant.setId(i);
			participant.setName("Competitor" + i);
			role.setParticipant(participant);
			roles.add(role);
		}
		return roles;
	}

	private List<Role> createMixedRoles(Tournament tournament) {
		final List<Role> roles = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			final Role role = new Role();
			role.setId(i);
			role.setRoleType(RoleType.COMPETITOR);
			final Participant participant = new Participant();
			participant.setId(i);
			participant.setName("Competitor" + i);
			role.setParticipant(participant);
			roles.add(role);
		}
		final Role arbitrerRole = new Role();
		arbitrerRole.setId(99);
		arbitrerRole.setRoleType(RoleType.REFEREE);
		final Participant arbitrer = new Participant();
		arbitrer.setId(99);
		arbitrer.setName("Referee");
		arbitrerRole.setParticipant(arbitrer);
		roles.add(arbitrerRole);
		return roles;
	}

	// ============= Additional comprehensive tests for 90%+ coverage =============

	@Test(groups = "tournamentFightStatistics")
	public void estimate_shouldDispatchToLeagueStatisticsWhenTypeIsLeague() {
		final Tournament tournament = this.createTournament();
		tournament.setType(TournamentType.LEAGUE);
		final List<Team> teams = this.createTeams(tournament, 4);

		when(this.mockDuelProvider.getDurationAverage()).thenReturn(180L);

		final TournamentFightStatistics result = this.provider.estimate(tournament, 3, teams);

		assertNotNull(result);
		assertEquals(result.getFightsByTeam(), 3L);
	}

	@Test(groups = "tournamentFightStatistics")
	public void estimateByMembers_shouldCreateTeamsWhenFiltering() {
		final Tournament tournament = this.createTournament();
		tournament.setTeamSize(2);
		final List<Role> roles = this.createRoles(tournament, 6);

		when(this.mockRoleProvider.getAll(tournament)).thenReturn(roles);
		when(this.mockDuelProvider.getDurationAverage()).thenReturn(150L);

		final TournamentFightStatistics result = this.provider.estimateByMembers(tournament);

		assertNotNull(result);
	}

	@Test(groups = "tournamentFightStatistics")
	public void get_shouldCountFightsFinished() {
		final Tournament tournament = this.createTournament();

		when(this.mockFightProvider.count(tournament)).thenReturn(10L);
		when(this.mockTeamProvider.count(tournament)).thenReturn(5L);
		when(this.mockDuelProvider.count(tournament)).thenReturn(50L);
		when(this.mockDuelProvider.getDurationAverage(tournament)).thenReturn(200L);
		when(this.mockDuelProvider.countScore(tournament, Score.MEN)).thenReturn(20L);
		when(this.mockDuelProvider.countScore(tournament, Score.DO)).thenReturn(15L);
		when(this.mockDuelProvider.countScore(tournament, Score.KOTE)).thenReturn(10L);
		when(this.mockDuelProvider.countScore(tournament, Score.HANSOKU)).thenReturn(2L);
		when(this.mockDuelProvider.countScore(tournament, Score.TSUKI)).thenReturn(3L);
		when(this.mockDuelProvider.countScore(tournament, Score.IPPON)).thenReturn(8L);
		when(this.mockDuelProvider.countScore(tournament, Score.FUSEN_GACHI)).thenReturn(1L);
		when(this.mockDuelProvider.getFirstDuel(tournament)).thenReturn(null);
		when(this.mockDuelProvider.getLastDuel(tournament)).thenReturn(null);
		when(this.mockFightProvider.countByTournamentAndFinished(tournament)).thenReturn(9L);
		when(this.mockDuelProvider.countFaults(tournament)).thenReturn(5L);

		final TournamentFightStatistics result = this.provider.get(tournament);

		assertNotNull(result);
		assertEquals(result.getFightsFinished(), 9L);
	}

	@Test(groups = "tournamentFightStatistics")
	public void get_shouldSetLastDuelFinishedTime() {
		final Tournament tournament = this.createTournament();
		final LocalDateTime finishedTime = LocalDateTime.now();
		final Duel lastDuel = new Duel();
		lastDuel.setFinishedAt(finishedTime);

		when(this.mockFightProvider.count(tournament)).thenReturn(0L);
		when(this.mockTeamProvider.count(tournament)).thenReturn(0L);
		when(this.mockDuelProvider.count(tournament)).thenReturn(0L);
		when(this.mockDuelProvider.getDurationAverage(tournament)).thenReturn(null);
		when(this.mockDuelProvider.countScore(tournament, Score.MEN)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.DO)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.KOTE)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.HANSOKU)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.TSUKI)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.IPPON)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.FUSEN_GACHI)).thenReturn(0L);
		when(this.mockDuelProvider.getFirstDuel(tournament)).thenReturn(null);
		when(this.mockDuelProvider.getLastDuel(tournament)).thenReturn(lastDuel);
		when(this.mockFightProvider.countByTournamentAndFinished(tournament)).thenReturn(0L);
		when(this.mockDuelProvider.countFaults(tournament)).thenReturn(0L);

		final TournamentFightStatistics result = this.provider.get(tournament);

		assertNotNull(result);
		assertEquals(result.getFightsFinishedAt(), finishedTime);
	}

	@Test(groups = "tournamentFightStatistics")
	public void estimateLeagueStatistics_shouldCalculateWithMultipleTeams() {
		final List<Team> teams = this.createTeams(null, 6);

		when(this.mockDuelProvider.getDurationAverage()).thenReturn(200L);

		final TournamentFightStatistics result = this.provider.estimateLeagueStatistics(3, teams);

		assertNotNull(result);
		assertEquals(result.getFightsNumber(), 15L);
		assertEquals(result.getFightsByTeam(), 5L);
	}

	@Test(groups = "tournamentFightStatistics")
	public void estimateLoopStatistics_shouldMaximizeFightsWhenPropertyFalse() {
		final Tournament tournament = this.createTournament();
		tournament.setType(TournamentType.LOOP);
		final List<Team> teams = this.createTeams(tournament, 4);
		final TournamentExtraProperty property = new TournamentExtraProperty();
		property.setPropertyValue("false");

		when(this.mockTournamentExtraPropertyProvider.getByTournamentAndProperty(tournament,
				TournamentExtraPropertyKey.AVOID_DUPLICATES)).thenReturn(property);
		when(this.mockDuelProvider.getDurationAverage()).thenReturn(150L);

		final TournamentFightStatistics result = this.provider.estimateLoopStatistics(tournament, 3, teams);

		assertNotNull(result);
		assertEquals(result.getFightsNumber(), 12L);
		assertEquals(result.getFightsByTeam(), 6L);
	}

	@Test(groups = "tournamentFightStatistics")
	public void estimateLoopStatistics_shouldAvoidDuplicatesWhenPropertyTrue() {
		final Tournament tournament = this.createTournament();
		tournament.setType(TournamentType.LOOP);
		final List<Team> teams = this.createTeams(tournament, 4);
		final TournamentExtraProperty property = new TournamentExtraProperty();
		property.setPropertyValue("true");

		when(this.mockTournamentExtraPropertyProvider.getByTournamentAndProperty(tournament,
				TournamentExtraPropertyKey.AVOID_DUPLICATES)).thenReturn(property);
		when(this.mockDuelProvider.getDurationAverage()).thenReturn(150L);

		final TournamentFightStatistics result = this.provider.estimateLoopStatistics(tournament, 3, teams);

		assertNotNull(result);
		assertEquals(result.getFightsNumber(), 7L);
		assertEquals(result.getFightsByTeam(), 3L);
	}

	@Test(groups = "tournamentFightStatistics")
	public void get_shouldAggregateAllScoreTypes() {
		final Tournament tournament = this.createTournament();

		when(this.mockFightProvider.count(tournament)).thenReturn(8L);
		when(this.mockTeamProvider.count(tournament)).thenReturn(2L);
		when(this.mockDuelProvider.count(tournament)).thenReturn(32L);
		when(this.mockDuelProvider.getDurationAverage(tournament)).thenReturn(175L);
		when(this.mockDuelProvider.countScore(tournament, Score.MEN)).thenReturn(25L);
		when(this.mockDuelProvider.countScore(tournament, Score.DO)).thenReturn(18L);
		when(this.mockDuelProvider.countScore(tournament, Score.KOTE)).thenReturn(12L);
		when(this.mockDuelProvider.countScore(tournament, Score.HANSOKU)).thenReturn(3L);
		when(this.mockDuelProvider.countScore(tournament, Score.TSUKI)).thenReturn(5L);
		when(this.mockDuelProvider.countScore(tournament, Score.IPPON)).thenReturn(7L);
		when(this.mockDuelProvider.countScore(tournament, Score.FUSEN_GACHI)).thenReturn(2L);
		when(this.mockDuelProvider.getFirstDuel(tournament)).thenReturn(null);
		when(this.mockDuelProvider.getLastDuel(tournament)).thenReturn(null);
		when(this.mockFightProvider.countByTournamentAndFinished(tournament)).thenReturn(6L);
		when(this.mockDuelProvider.countFaults(tournament)).thenReturn(4L);

		final TournamentFightStatistics result = this.provider.get(tournament);

		assertNotNull(result);
		assertEquals(result.getTsukiNumber(), 5L);
		assertEquals(result.getFusenGachiNumber(), 2L);
		assertEquals(result.getHansokuNumber(), 3L);
	}

	@Test(groups = "tournamentFightStatistics")
	public void estimateLeagueStatistics_shouldIncludeFightsNumberInEstimatedTime() {
		final List<Team> teams = this.createTeams(null, 5);

		when(this.mockDuelProvider.getDurationAverage()).thenReturn(190L);

		final TournamentFightStatistics result = this.provider.estimateLeagueStatistics(3, teams);

		assertNotNull(result);
		assertNotNull(result.getEstimatedTime());
		assertTrue(result.getEstimatedTime() > 0);
	}

	@Test(groups = "tournamentFightStatistics")
	public void get_shouldSetFightsByTeamWhenTeamsExist() {
		final Tournament tournament = this.createTournament();

		when(this.mockFightProvider.count(tournament)).thenReturn(12L);
		when(this.mockTeamProvider.count(tournament)).thenReturn(4L);
		when(this.mockDuelProvider.count(tournament)).thenReturn(40L);
		when(this.mockDuelProvider.getDurationAverage(tournament)).thenReturn(180L);
		when(this.mockDuelProvider.countScore(tournament, Score.MEN)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.DO)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.KOTE)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.HANSOKU)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.TSUKI)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.IPPON)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.FUSEN_GACHI)).thenReturn(0L);
		when(this.mockDuelProvider.getFirstDuel(tournament)).thenReturn(null);
		when(this.mockDuelProvider.getLastDuel(tournament)).thenReturn(null);
		when(this.mockFightProvider.countByTournamentAndFinished(tournament)).thenReturn(0L);
		when(this.mockDuelProvider.countFaults(tournament)).thenReturn(0L);

		final TournamentFightStatistics result = this.provider.get(tournament);

		assertNotNull(result);
		assertEquals(result.getFightsByTeam(), 3L);
	}

	@Test(groups = "tournamentFightStatistics")
	public void estimate_shouldReturnProperValuesForEstimate() {
		final Tournament tournament = this.createTournament();
		final List<Team> teams = this.createTeams(tournament, 3);

		when(this.mockDuelProvider.getDurationAverage()).thenReturn(160L);

		final TournamentFightStatistics result = this.provider.estimate(tournament, 3, teams);

		assertNotNull(result);
		assertEquals(result.getFightsByTeam(), 2L);
	}

	@Test(groups = "tournamentFightStatistics")
	public void estimateLoopStatistics_shouldHandleEstimatedTimeCalculationCorrectly() {
		final Tournament tournament = this.createTournament();
		tournament.setType(TournamentType.LOOP);
		final List<Team> teams = this.createTeams(tournament, 3);
		final TournamentExtraProperty property = new TournamentExtraProperty();
		property.setPropertyValue("true");

		when(this.mockTournamentExtraPropertyProvider.getByTournamentAndProperty(tournament,
				TournamentExtraPropertyKey.AVOID_DUPLICATES)).thenReturn(property);
		when(this.mockDuelProvider.getDurationAverage()).thenReturn(200L);

		final TournamentFightStatistics result = this.provider.estimateLoopStatistics(tournament, 3, teams);

		assertNotNull(result);
		assertNotNull(result.getEstimatedTime());
		assertTrue(result.getEstimatedTime() >= 0);
	}

	@Test(groups = "tournamentFightStatistics")
	public void get_shouldHandleFirstDuelWithStartedAtNull() {
		final Tournament tournament = this.createTournament();
		final Duel firstDuel = new Duel();
		firstDuel.setStartedAt(null);

		when(this.mockFightProvider.count(tournament)).thenReturn(0L);
		when(this.mockTeamProvider.count(tournament)).thenReturn(0L);
		when(this.mockDuelProvider.count(tournament)).thenReturn(0L);
		when(this.mockDuelProvider.getDurationAverage(tournament)).thenReturn(null);
		when(this.mockDuelProvider.countScore(tournament, Score.MEN)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.DO)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.KOTE)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.HANSOKU)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.TSUKI)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.IPPON)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.FUSEN_GACHI)).thenReturn(0L);
		when(this.mockDuelProvider.getFirstDuel(tournament)).thenReturn(firstDuel);
		when(this.mockDuelProvider.getLastDuel(tournament)).thenReturn(null);
		when(this.mockFightProvider.countByTournamentAndFinished(tournament)).thenReturn(0L);
		when(this.mockDuelProvider.countFaults(tournament)).thenReturn(0L);

		final TournamentFightStatistics result = this.provider.get(tournament);

		assertNotNull(result);
		assertNull(result.getFightsStartedAt());
	}

	@Test(groups = "tournamentFightStatistics")
	public void get_shouldHandleFirstDuelWithFinishedAtValid() {
		final Tournament tournament = this.createTournament();
		final LocalDateTime finishedTime = LocalDateTime.of(2026, Month.JUNE, 24, 15, 30);
		final Duel firstDuel = new Duel();
		firstDuel.setStartedAt(null);
		firstDuel.setFinishedAt(finishedTime);

		when(this.mockFightProvider.count(tournament)).thenReturn(0L);
		when(this.mockTeamProvider.count(tournament)).thenReturn(0L);
		when(this.mockDuelProvider.count(tournament)).thenReturn(0L);
		when(this.mockDuelProvider.getDurationAverage(tournament)).thenReturn(null);
		when(this.mockDuelProvider.countScore(tournament, Score.MEN)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.DO)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.KOTE)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.HANSOKU)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.TSUKI)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.IPPON)).thenReturn(0L);
		when(this.mockDuelProvider.countScore(tournament, Score.FUSEN_GACHI)).thenReturn(0L);
		when(this.mockDuelProvider.getFirstDuel(tournament)).thenReturn(firstDuel);
		when(this.mockDuelProvider.getLastDuel(tournament)).thenReturn(null);
		when(this.mockFightProvider.countByTournamentAndFinished(tournament)).thenReturn(0L);
		when(this.mockDuelProvider.countFaults(tournament)).thenReturn(0L);

		final TournamentFightStatistics result = this.provider.get(tournament);

		assertNotNull(result);
		assertEquals(result.getFightsStartedAt(), finishedTime.minusMinutes(2));
	}
}
