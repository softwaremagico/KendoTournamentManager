package com.softwaremagico.kt.core.tournaments;

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

import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.RankingProvider;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.core.providers.TournamentExtraPropertyProvider;
import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

@Test(groups = "swissTournamentHandlerTests")
public class SwissTournamentHandlerTest {

	@Mock
	private GroupProvider groupProvider;
	@Mock
	private TeamProvider teamProvider;
	@Mock
	private RankingProvider rankingProvider;
	@Mock
	private TournamentExtraPropertyProvider tournamentExtraPropertyProvider;

	private SwissTournamentHandler swissTournamentHandler;

	@BeforeMethod(alwaysRun = true)
	public void setUp() {
		MockitoAnnotations.openMocks(this);
        this.swissTournamentHandler = new SwissTournamentHandler(this.groupProvider, this.teamProvider, this.rankingProvider,
                this.tournamentExtraPropertyProvider);
	}

	@Test
	public void shouldCalculateDefaultRoundsFromParticipants() {
		assertEquals(this.swissTournamentHandler.getDefaultRounds(0), 1);
		assertEquals(this.swissTournamentHandler.getDefaultRounds(1), 1);
		assertEquals(this.swissTournamentHandler.getDefaultRounds(2), 1);
		assertEquals(this.swissTournamentHandler.getDefaultRounds(3), 2);
		assertEquals(this.swissTournamentHandler.getDefaultRounds(4), 2);
		assertEquals(this.swissTournamentHandler.getDefaultRounds(5), 3);
	}

	@Test
	public void shouldReturnConfiguredRoundsWhenValidOverrideExists() {
		final Tournament tournament = this.tournament();
		final Group group = this.groupWithTeams(tournament, 5);

		when(this.groupProvider.getGroups(tournament)).thenReturn(List.of(group));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_ROUNDS), any()))
				.thenReturn(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.SWISS_ROUNDS, "6"));

		assertEquals(this.swissTournamentHandler.getConfiguredRounds(tournament), 6);
	}

	@Test
	public void shouldFallbackToDefaultRoundsWhenOverrideIsNotNumeric() {
		final Tournament tournament = this.tournament();
		final Group group = this.groupWithTeams(tournament, 5);

		when(this.groupProvider.getGroups(tournament)).thenReturn(List.of(group));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_ROUNDS), any())).thenReturn(
						new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.SWISS_ROUNDS, "invalid"));

		assertEquals(this.swissTournamentHandler.getConfiguredRounds(tournament), 3);
	}

	@Test
	public void shouldFallbackToDefaultRoundsWhenOverrideIsLessThanOne() {
		final Tournament tournament = this.tournament();
		final Group group = this.groupWithTeams(tournament, 4);

		when(this.groupProvider.getGroups(tournament)).thenReturn(List.of(group));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_ROUNDS), any()))
				.thenReturn(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.SWISS_ROUNDS, "0"));

		assertEquals(this.swissTournamentHandler.getConfiguredRounds(tournament), 2);
	}

	@Test
	public void shouldNotGenerateFightsWhenDefaultSwissRoundsAreCompleted() {
		final Tournament tournament = this.tournament();
		final Group group = this.groupWithTeams(tournament, 4);
		group.setId(30);
		group.setFights(new ArrayList<>());
		group.getFights().add(this.finishedFight(tournament, group.getTeams().get(0), group.getTeams().get(1), 0, true));
		group.getFights().add(this.finishedFight(tournament, group.getTeams().get(2), group.getTeams().get(3), 0, true));
		group.getFights().add(this.finishedFight(tournament, group.getTeams().get(0), group.getTeams().get(2), 1, true));
		group.getFights().add(this.finishedFight(tournament, group.getTeams().get(1), group.getTeams().get(3), 1, true));

		when(this.groupProvider.getGroups(tournament)).thenReturn(List.of(group));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_ROUNDS), any()))
				.thenReturn(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.SWISS_ROUNDS, "2"));

		final List<com.softwaremagico.kt.persistence.entities.Fight> fights = this.swissTournamentHandler
				.createFights(tournament, null, 2, "tester");

		assertTrue(fights.isEmpty());
		verify(this.groupProvider, never()).addGroup(eq(tournament), any(Group.class));
	}

	@Test
	public void shouldGenerateExtraSwissRoundWhenConfiguredRoundsAreHigherThanDefault() {
		final Tournament tournament = this.tournament();
		final Group group = this.groupWithTeams(tournament, 4);
		group.setId(31);
		group.setFights(new ArrayList<>());
		group.getFights().add(this.finishedFight(tournament, group.getTeams().get(0), group.getTeams().get(1), 0, true));
		group.getFights().add(this.finishedFight(tournament, group.getTeams().get(2), group.getTeams().get(3), 0, true));
		group.getFights().add(this.finishedFight(tournament, group.getTeams().get(0), group.getTeams().get(2), 1, true));
		group.getFights().add(this.finishedFight(tournament, group.getTeams().get(1), group.getTeams().get(3), 1, true));

		when(this.groupProvider.getGroups(tournament)).thenReturn(List.of(group));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_ROUNDS), any()))
				.thenReturn(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.SWISS_ROUNDS, "3"));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS), any()))
				.thenReturn(new TournamentExtraProperty(tournament,
						TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS, "true"));
		when(this.groupProvider.addGroup(eq(tournament), any(Group.class))).thenReturn(group);

		final List<com.softwaremagico.kt.persistence.entities.Fight> fights = this.swissTournamentHandler
				.createFights(tournament, null, 2, "tester");

		assertEquals(fights.size(), 2);
		assertTrue(fights.stream().allMatch(fight -> fight.getLevel() == 2));
		verify(this.groupProvider, atLeastOnce()).addGroup(eq(tournament), any(Group.class));
	}

	@Test
	public void shouldGenerateNextRoundWhenCurrentRoundIsOver() {
		final Tournament tournament = this.tournament();
		final Group group = this.groupWithTeams(tournament, 4);
		group.setId(401);
		group.setFights(new ArrayList<>());
		group.getFights().add(this.finishedFight(tournament, group.getTeams().get(0), group.getTeams().get(1), 0, true));
		group.getFights().add(this.finishedFight(tournament, group.getTeams().get(2), group.getTeams().get(3), 0, false));

		when(this.groupProvider.getGroups(tournament)).thenReturn(List.of(group));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_ROUNDS), any()))
				.thenReturn(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.SWISS_ROUNDS, "3"));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS), any()))
				.thenReturn(new TournamentExtraProperty(tournament,
						TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS, "true"));
		when(this.groupProvider.addGroup(eq(tournament), any(Group.class))).thenReturn(group);

		final List<com.softwaremagico.kt.persistence.entities.Fight> fights = this.swissTournamentHandler
				.generateNextFights(tournament, "tester");

		assertEquals(fights.size(), 2);
		assertTrue(fights.stream().allMatch(fight -> fight.getLevel() == 1));
	}

	@Test
	public void shouldNotGenerateNextRoundWhenCurrentRoundIsNotOver() {
		final Tournament tournament = this.tournament();
		final Group group = this.groupWithTeams(tournament, 4);
		group.setId(402);
		group.setFights(new ArrayList<>());
		group.getFights().add(this.finishedFight(tournament, group.getTeams().get(0), group.getTeams().get(1), 0, true));
		group.getFights().add(new com.softwaremagico.kt.persistence.entities.Fight(tournament, group.getTeams().get(2),
				group.getTeams().get(3), 0, 0, "tester"));

		when(this.groupProvider.getGroups(tournament)).thenReturn(List.of(group));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_ROUNDS), any()))
				.thenReturn(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.SWISS_ROUNDS, "3"));

		final List<com.softwaremagico.kt.persistence.entities.Fight> fights = this.swissTournamentHandler
				.generateNextFights(tournament, "tester");

		assertTrue(fights.isEmpty());
		verify(this.groupProvider, never()).addGroup(eq(tournament), any(Group.class));
	}

	@Test
	public void shouldNotGenerateFightsWhenConfiguredRoundsAreLowerThanDefault() {
		final Tournament tournament = this.tournament();
		final Group group = this.groupWithTeams(tournament, 8);
		group.setId(32);
		group.setFights(new ArrayList<>());
		group.getFights().add(this.finishedFight(tournament, group.getTeams().get(0), group.getTeams().get(1), 0, true));
		group.getFights().add(this.finishedFight(tournament, group.getTeams().get(2), group.getTeams().get(3), 0, true));
		group.getFights().add(this.finishedFight(tournament, group.getTeams().get(4), group.getTeams().get(5), 0, true));
		group.getFights().add(this.finishedFight(tournament, group.getTeams().get(6), group.getTeams().get(7), 0, true));
		group.getFights().add(this.finishedFight(tournament, group.getTeams().get(0), group.getTeams().get(2), 1, true));
		group.getFights().add(this.finishedFight(tournament, group.getTeams().get(1), group.getTeams().get(3), 1, true));
		group.getFights().add(this.finishedFight(tournament, group.getTeams().get(4), group.getTeams().get(6), 1, true));
		group.getFights().add(this.finishedFight(tournament, group.getTeams().get(5), group.getTeams().get(7), 1, true));

		when(this.groupProvider.getGroups(tournament)).thenReturn(List.of(group));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_ROUNDS), any()))
				.thenReturn(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.SWISS_ROUNDS, "2"));

		final List<com.softwaremagico.kt.persistence.entities.Fight> fights = this.swissTournamentHandler
				.createFights(tournament, null, 2, "tester");

		assertTrue(fights.isEmpty());
		verify(this.groupProvider, never()).addGroup(eq(tournament), any(Group.class));
	}

	@Test
	public void shouldGenerateInitialSwissRoundFights() {
		final Tournament tournament = this.tournament();
		final Group group = this.groupWithTeams(tournament, 4);
		group.setId(10);

		when(this.groupProvider.getGroups(tournament)).thenReturn(List.of(group));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_ROUNDS), any()))
				.thenReturn(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.SWISS_ROUNDS, "3"));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS), any()))
				.thenReturn(new TournamentExtraProperty(tournament,
						TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS, "true"));
		when(this.groupProvider.addGroup(eq(tournament), eq(group))).thenReturn(group);

		final List<Fight> fights = this.swissTournamentHandler.createFights(tournament, null, 0, "tester");

		assertEquals(fights.size(), 2);
		assertEquals(fights.get(0).getLevel().intValue(), 0);
		assertEquals(fights.get(1).getLevel().intValue(), 0);
		assertEquals(fights.get(0).getTeam1().getName(), "Team0");
		assertEquals(fights.get(0).getTeam2().getName(), "Team1");
		assertEquals(fights.get(1).getTeam1().getName(), "Team2");
		assertEquals(fights.get(1).getTeam2().getName(), "Team3");
		verify(this.groupProvider).addGroup(eq(tournament), any(Group.class));
	}

	@Test
	public void shouldGeneratePairsWithByeWhenOddNumberOfTeams() {
		final Tournament tournament = this.tournament();
		final Group group = this.groupWithTeams(tournament, 5);
		group.setId(11);

		when(this.groupProvider.getGroups(tournament)).thenReturn(List.of(group));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_ROUNDS), any()))
				.thenReturn(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.SWISS_ROUNDS, "3"));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS), any()))
				.thenReturn(new TournamentExtraProperty(tournament,
						TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS, "true"));
		when(this.groupProvider.addGroup(eq(tournament), eq(group))).thenReturn(group);

		final List<com.softwaremagico.kt.persistence.entities.Fight> fights = this.swissTournamentHandler
				.createFights(tournament, null, 0, "tester");

		assertEquals(fights.size(), 2);
		final List<String> pairedTeams = fights.stream()
				.flatMap(fight -> List.of(fight.getTeam1().getName(), fight.getTeam2().getName()).stream()).toList();
        assertFalse(pairedTeams.contains("Team4"));
	}

	@Test
	public void shouldNotRepeatByeWhenThereAreTeamsWithoutBye() {
		final Tournament tournament = this.tournament();
		final Group group = this.groupWithTeams(tournament, 5);
		group.setId(111);
		group.setFights(new ArrayList<>());

		// R0 finished fights imply Team4 received the initial bye (it does not appear
		// in level 0 fights).
		group.getFights().add(this.finishedFight(tournament, group.getTeams().get(0), group.getTeams().get(1), 0, true));
		group.getFights().add(this.finishedFight(tournament, group.getTeams().get(2), group.getTeams().get(3), 0, true));

		when(this.groupProvider.getGroups(tournament)).thenReturn(List.of(group));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_ROUNDS), any()))
				.thenReturn(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.SWISS_ROUNDS, "4"));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS), any()))
				.thenReturn(new TournamentExtraProperty(tournament,
						TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS, "true"));
		when(this.groupProvider.addGroup(eq(tournament), eq(group))).thenReturn(group);

		final List<com.softwaremagico.kt.persistence.entities.Fight> fights = this.swissTournamentHandler
				.createFights(tournament, null, 1, "tester");

		assertEquals(fights.size(), 2);
		final List<String> pairedTeams = fights.stream()
				.flatMap(fight -> List.of(fight.getTeam1().getName(), fight.getTeam2().getName()).stream()).toList();
		assertTrue(pairedTeams.contains("Team4"));
        assertFalse(pairedTeams.contains("Team3"));
	}

	@Test
	public void shouldNotGenerateSameRoundTwice() {
		final Tournament tournament = this.tournament();
		final Group group = this.groupWithTeams(tournament, 4);
		group.setId(12);
		group.setFights(new ArrayList<>());
		group.getFights().add(new com.softwaremagico.kt.persistence.entities.Fight(tournament, group.getTeams().get(0),
				group.getTeams().get(1), 0, 1, "tester"));

		when(this.groupProvider.getGroups(tournament)).thenReturn(List.of(group));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_ROUNDS), any()))
				.thenReturn(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.SWISS_ROUNDS, "3"));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS), any()))
				.thenReturn(new TournamentExtraProperty(tournament,
						TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS, "true"));

		final List<com.softwaremagico.kt.persistence.entities.Fight> fights = this.swissTournamentHandler
				.createFights(tournament, null, 1, "tester");

		assertTrue(fights.isEmpty());
		verify(this.groupProvider, never()).addGroup(eq(tournament), any(Group.class));
	}

	@Test
	public void shouldGenerateSecondRoundUsingAccumulatedScoreOrdering() {
		final Tournament tournament = this.tournament();
		final Group group = this.groupWithTeams(tournament, 4);
		group.setId(13);
		group.setFights(new ArrayList<>());

		final com.softwaremagico.kt.persistence.entities.Fight first = this.finishedFight(tournament,
				group.getTeams().get(0), group.getTeams().get(1), 0, true);
		final com.softwaremagico.kt.persistence.entities.Fight second = this.finishedFight(tournament,
				group.getTeams().get(2), group.getTeams().get(3), 0, false);
		group.getFights().add(first);
		group.getFights().add(second);

		when(this.groupProvider.getGroups(tournament)).thenReturn(List.of(group));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_ROUNDS), any()))
				.thenReturn(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.SWISS_ROUNDS, "3"));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS), any()))
				.thenReturn(new TournamentExtraProperty(tournament,
						TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS, "true"));
		when(this.groupProvider.addGroup(eq(tournament), eq(group))).thenReturn(group);

		final List<com.softwaremagico.kt.persistence.entities.Fight> fights = this.swissTournamentHandler
				.createFights(tournament, null, 1, "tester");

		assertEquals(fights.size(), 2);
		assertEquals(fights.get(0).getTeam1().getName(), "Team0");
		assertEquals(fights.get(0).getTeam2().getName(), "Team3");
		assertEquals(fights.get(1).getTeam1().getName(), "Team1");
		assertEquals(fights.get(1).getTeam2().getName(), "Team2");
	}

	@Test
	public void shouldAvoidRepeatedPairingsWhenConfigured() {
		final Tournament tournament = this.tournament();
		final Group group = this.repeatedPairingsScenario(tournament);

		when(this.groupProvider.getGroups(tournament)).thenReturn(List.of(group));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_ROUNDS), any()))
				.thenReturn(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.SWISS_ROUNDS, "4"));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS), any()))
				.thenReturn(new TournamentExtraProperty(tournament,
						TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS, "true"));
		when(this.groupProvider.addGroup(eq(tournament), eq(group))).thenReturn(group);

		final List<com.softwaremagico.kt.persistence.entities.Fight> fights = this.swissTournamentHandler
				.createFights(tournament, null, 2, "tester");

		assertEquals(fights.size(), 2);
		assertEquals(fights.get(0).getTeam1().getName(), "Team0");
		assertEquals(fights.get(0).getTeam2().getName(), "Team3");
		assertEquals(fights.get(1).getTeam1().getName(), "Team1");
		assertEquals(fights.get(1).getTeam2().getName(), "Team2");
	}

	@Test
	public void shouldAllowRepeatedPairingsWhenConfiguredFalse() {
		final Tournament tournament = this.tournament();
		final Group group = this.repeatedPairingsScenario(tournament);

		when(this.groupProvider.getGroups(tournament)).thenReturn(List.of(group));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_ROUNDS), any()))
				.thenReturn(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.SWISS_ROUNDS, "4"));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS), any()))
				.thenReturn(new TournamentExtraProperty(tournament,
						TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS, "false"));
		when(this.groupProvider.addGroup(eq(tournament), eq(group))).thenReturn(group);

		final List<com.softwaremagico.kt.persistence.entities.Fight> fights = this.swissTournamentHandler
				.createFights(tournament, null, 2, "tester");

		assertEquals(fights.size(), 2);
		assertEquals(fights.get(0).getTeam1().getName(), "Team0");
		assertEquals(fights.get(0).getTeam2().getName(), "Team1");
		assertEquals(fights.get(1).getTeam1().getName(), "Team2");
		assertEquals(fights.get(1).getTeam2().getName(), "Team3");
	}

	@Test
	public void shouldGenerateDifferentPairingsWhenAvoidRepeatedPairingsIsToggled() {
		final Tournament tournamentWithAvoidedRepeats = this.tournament();
		tournamentWithAvoidedRepeats.setId(101);
		final Group groupWithAvoidedRepeats = this.repeatedPairingsScenario(tournamentWithAvoidedRepeats);

		final Tournament tournamentWithAllowedRepeats = this.tournament();
		tournamentWithAllowedRepeats.setId(102);
		final Group groupWithAllowedRepeats = this.repeatedPairingsScenario(tournamentWithAllowedRepeats);

		when(this.groupProvider.getGroups(tournamentWithAvoidedRepeats)).thenReturn(List.of(groupWithAvoidedRepeats));
		when(this.groupProvider.getGroups(tournamentWithAllowedRepeats)).thenReturn(List.of(groupWithAllowedRepeats));

		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournamentWithAvoidedRepeats),
				eq(TournamentExtraPropertyKey.SWISS_ROUNDS), any()))
				.thenReturn(new TournamentExtraProperty(tournamentWithAvoidedRepeats,
						TournamentExtraPropertyKey.SWISS_ROUNDS, "4"));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournamentWithAllowedRepeats),
				eq(TournamentExtraPropertyKey.SWISS_ROUNDS), any()))
				.thenReturn(new TournamentExtraProperty(tournamentWithAllowedRepeats,
						TournamentExtraPropertyKey.SWISS_ROUNDS, "4"));

		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournamentWithAvoidedRepeats),
				eq(TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS), any()))
				.thenReturn(new TournamentExtraProperty(tournamentWithAvoidedRepeats,
						TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS, "true"));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournamentWithAllowedRepeats),
				eq(TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS), any()))
				.thenReturn(new TournamentExtraProperty(tournamentWithAllowedRepeats,
						TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS, "false"));

		when(this.groupProvider.addGroup(eq(tournamentWithAvoidedRepeats), eq(groupWithAvoidedRepeats)))
				.thenReturn(groupWithAvoidedRepeats);
		when(this.groupProvider.addGroup(eq(tournamentWithAllowedRepeats), eq(groupWithAllowedRepeats)))
				.thenReturn(groupWithAllowedRepeats);

		final List<com.softwaremagico.kt.persistence.entities.Fight> fightsWithAvoidedRepeats = this.swissTournamentHandler
				.createFights(tournamentWithAvoidedRepeats, null, 2, "tester");
		final List<com.softwaremagico.kt.persistence.entities.Fight> fightsWithAllowedRepeats = this.swissTournamentHandler
				.createFights(tournamentWithAllowedRepeats, null, 2, "tester");

		final List<String> pairingsWithAvoidedRepeats = fightsWithAvoidedRepeats.stream()
				.map(fight -> fight.getTeam1().getName() + "-" + fight.getTeam2().getName()).toList();
		final List<String> pairingsWithAllowedRepeats = fightsWithAllowedRepeats.stream()
				.map(fight -> fight.getTeam1().getName() + "-" + fight.getTeam2().getName()).toList();

		assertEquals(pairingsWithAvoidedRepeats, List.of("Team0-Team3", "Team1-Team2"));
		assertEquals(pairingsWithAllowedRepeats, List.of("Team0-Team1", "Team2-Team3"));
        assertFalse(pairingsWithAvoidedRepeats.equals(pairingsWithAllowedRepeats));
	}

	@Test
	public void shouldAvoidRepeatedPairingsWhenPropertyUsesDefaultValue() {
		final Tournament tournament = this.tournament();
		final Group group = this.repeatedPairingsScenario(tournament);

		when(this.groupProvider.getGroups(tournament)).thenReturn(List.of(group));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_ROUNDS), any()))
				.thenReturn(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.SWISS_ROUNDS, "4"));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS), any()))
				.thenReturn(new TournamentExtraProperty(tournament,
						TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS, "true"));
		when(this.groupProvider.addGroup(eq(tournament), eq(group))).thenReturn(group);

		final List<com.softwaremagico.kt.persistence.entities.Fight> fights = this.swissTournamentHandler
				.createFights(tournament, null, 2, "tester");

		final List<String> pairings = fights.stream()
				.map(fight -> fight.getTeam1().getName() + "-" + fight.getTeam2().getName()).toList();
		assertEquals(pairings, List.of("Team0-Team3", "Team1-Team2"));
	}

	@Test
	public void shouldAllowRepeatedPairingsWhenPropertyValueIsInvalid() {
		final Tournament tournament = this.tournament();
		final Group group = this.repeatedPairingsScenario(tournament);

		when(this.groupProvider.getGroups(tournament)).thenReturn(List.of(group));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_ROUNDS), any()))
				.thenReturn(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.SWISS_ROUNDS, "4"));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS), any()))
				.thenReturn(new TournamentExtraProperty(tournament,
						TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS, "not-a-boolean"));
		when(this.groupProvider.addGroup(eq(tournament), eq(group))).thenReturn(group);

		final List<com.softwaremagico.kt.persistence.entities.Fight> fights = this.swissTournamentHandler
				.createFights(tournament, null, 2, "tester");

		final List<String> pairings = fights.stream()
				.map(fight -> fight.getTeam1().getName() + "-" + fight.getTeam2().getName()).toList();
		assertEquals(pairings, List.of("Team0-Team1", "Team2-Team3"));
	}

	@Test
	public void shouldFallbackToRepeatedPairingsWhenNoAlternativeExists() {
		final Tournament tournament = this.tournament();
		final Group group = this.repeatedPairingsScenario(tournament);
		group.getFights().add(this.finishedFight(tournament, group.getTeams().get(0), group.getTeams().get(3), 2, true));
		group.getFights().add(this.finishedFight(tournament, group.getTeams().get(1), group.getTeams().get(2), 2, true));

		when(this.groupProvider.getGroups(tournament)).thenReturn(List.of(group));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_ROUNDS), any()))
				.thenReturn(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.SWISS_ROUNDS, "5"));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS), any()))
				.thenReturn(new TournamentExtraProperty(tournament,
						TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS, "true"));
		when(this.groupProvider.addGroup(eq(tournament), eq(group))).thenReturn(group);

		final List<com.softwaremagico.kt.persistence.entities.Fight> fights = this.swissTournamentHandler
				.createFights(tournament, null, 3, "tester");

		assertEquals(fights.size(), 2);
		assertEquals(fights.get(0).getTeam1().getName(), "Team0");
		assertEquals(fights.get(0).getTeam2().getName(), "Team1");
		assertEquals(fights.get(1).getTeam1().getName(), "Team2");
		assertEquals(fights.get(1).getTeam2().getName(), "Team3");
	}

	private Tournament tournament() {
		return new Tournament("Swiss", 1, 3, TournamentType.SWISS, "tester");
	}

	private Group groupWithTeams(Tournament tournament, int teams) {
		final Group group = new Group(tournament, 0, 0);
		group.setFights(new ArrayList<>());
		group.setTeams(
				java.util.stream.IntStream.range(0, teams).mapToObj(i -> this.teamWithSingleMember(tournament, i)).toList());
		return group;
	}

	private Team teamWithSingleMember(Tournament tournament, int index) {
		final Team team = new Team("Team" + index, tournament);
		final Participant participant = new Participant();
		participant.setIdCard("ID-" + index);
		participant.setName("Name" + index);
		participant.setLastname("Lastname" + index);
		team.setMembers(new ArrayList<>(List.of(participant)));
		return team;
	}

	private Group repeatedPairingsScenario(Tournament tournament) {
		final Group group = this.groupWithTeams(tournament, 4);
		group.setId(20);
		group.getFights().add(this.finishedFight(tournament, group.getTeams().get(0), group.getTeams().get(1), 0, true));
		group.getFights().add(this.finishedFight(tournament, group.getTeams().get(2), group.getTeams().get(3), 0, true));
		group.getFights().add(this.finishedFight(tournament, group.getTeams().get(0), group.getTeams().get(2), 1, true));
		group.getFights().add(this.finishedFight(tournament, group.getTeams().get(1), group.getTeams().get(3), 1, true));
		return group;
	}

	private com.softwaremagico.kt.persistence.entities.Fight finishedFight(Tournament tournament, Team team1,
			Team team2, int level, boolean team1Wins) {
		final com.softwaremagico.kt.persistence.entities.Fight fight = new com.softwaremagico.kt.persistence.entities.Fight(
				tournament, team1, team2, 0, level, "tester");
		final Duel duel = fight.getDuels().getFirst();
		if (team1Wins) {
			duel.addCompetitor1Score(Score.DO);
			duel.addCompetitor1Score(Score.DO);
		} else {
			duel.addCompetitor2Score(Score.DO);
			duel.addCompetitor2Score(Score.DO);
		}
		duel.setFinished(true);
		return fight;
	}
}
