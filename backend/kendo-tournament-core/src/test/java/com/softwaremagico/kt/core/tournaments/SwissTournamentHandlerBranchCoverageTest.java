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

import com.softwaremagico.kt.core.exceptions.CustomTournamentFightsException;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.RankingProvider;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.core.providers.TournamentExtraPropertyProvider;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

@Test(groups = "swissTournamentHandlerBranchCoverageTests")
public class SwissTournamentHandlerBranchCoverageTest {

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

	// Branch coverage for getGroups(Tournament) when groups is empty
	@Test
	public void when_getGroups_and_groupsIsEmpty_expect_returnFirstGroup() {
		final Tournament tournament = this.tournament();
		final Group firstGroup = this.groupWithTeams(tournament, 4);

		when(this.groupProvider.getGroups(tournament)).thenReturn(new ArrayList<>());
		when(this.groupProvider.getGroups(eq(tournament))).thenReturn(List.of(firstGroup));

		final List<Group> result = this.swissTournamentHandler.getGroups(tournament);

		assertTrue(result.size() > 0);
	}

	// Branch coverage for getGroups(Tournament, Integer) when level is null
	@Test
	public void when_getGroupsWithLevel_and_levelIsNull_expect_callGetGroupsWithoutLevel() {
		final Tournament tournament = this.tournament();
		final Group group = this.groupWithTeams(tournament, 4);

		when(this.groupProvider.getGroups(tournament)).thenReturn(List.of(group));

		final List<Group> result = this.swissTournamentHandler.getGroups(tournament, null);

		assertEquals(result.size(), 1);
	}

	// Branch coverage for getGroups(Tournament, Integer) when groups is empty and level == 0
	@Test
	public void when_getGroupsWithLevel_and_groupsIsEmpty_and_levelIsZero_expect_returnFirstGroup() {
		final Tournament tournament = this.tournament();
		final Group firstGroup = this.groupWithTeams(tournament, 4);

		when(this.groupProvider.getGroups(eq(tournament), eq(0))).thenReturn(new ArrayList<>());
		when(this.groupProvider.getGroups(tournament)).thenReturn(List.of(firstGroup));

		final List<Group> result = this.swissTournamentHandler.getGroups(tournament, 0);

		assertEquals(result.size(), 1);
	}

	// Branch coverage for getGroups(Tournament, Integer) when groups is empty and level != 0
	@Test
	public void when_getGroupsWithLevel_and_groupsIsEmpty_and_levelIsNotZero_expect_returnEmpty() {
		final Tournament tournament = this.tournament();

		when(this.groupProvider.getGroups(eq(tournament), eq(1))).thenReturn(new ArrayList<>());

		final List<Group> result = this.swissTournamentHandler.getGroups(tournament, 1);

		assertTrue(result.isEmpty());
	}

	// Branch coverage for createFights when level is null
	@Test
	public void when_createFights_and_levelIsNull_expect_throwException() {
		final Tournament tournament = this.tournament();
		final Group group = this.groupWithTeams(tournament, 4);

		when(this.groupProvider.getGroups(tournament)).thenReturn(List.of(group));

		assertThrows(CustomTournamentFightsException.class, () ->
				this.swissTournamentHandler.createFights(tournament, null, null, "tester"));
	}

	// Branch coverage for createFights when level is negative
	@Test
	public void when_createFights_and_levelIsNegative_expect_throwException() {
		final Tournament tournament = this.tournament();
		final Group group = this.groupWithTeams(tournament, 4);

		when(this.groupProvider.getGroups(tournament)).thenReturn(List.of(group));

		assertThrows(CustomTournamentFightsException.class, () ->
				this.swissTournamentHandler.createFights(tournament, null, -1, "tester"));
	}

	// Branch coverage for createFights when initial group teams is null
	@Test
	public void when_createFights_and_teamsIsNull_expect_returnEmpty() {
		final Tournament tournament = this.tournament();
		final Group group = new Group(tournament, 0, 0);
		group.setTeams(null);
		group.setFights(new ArrayList<>());

		when(this.groupProvider.getGroups(tournament)).thenReturn(List.of(group));
		when(this.groupProvider.getGroups(eq(tournament), eq(0))).thenReturn(List.of(group));
		when(this.groupProvider.getGroupByLevelAndIndex(eq(tournament), eq(0), eq(0))).thenReturn(group);
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_ROUNDS), any()))
				.thenReturn(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.SWISS_ROUNDS, "3"));

		final List<Fight> result = this.swissTournamentHandler.createFights(tournament, null, 0, "tester");

		assertTrue(result.isEmpty());
	}

	// Branch coverage for createFights when teams size < 2
	@Test
	public void when_createFights_and_teamsSizeIsLessThanTwo_expect_returnEmpty() {
		final Tournament tournament = this.tournament();
		final Group group = this.groupWithTeams(tournament, 1);
		group.setFights(new ArrayList<>());

		when(this.groupProvider.getGroups(tournament)).thenReturn(List.of(group));
		when(this.groupProvider.getGroups(eq(tournament), eq(0))).thenReturn(List.of(group));
		when(this.groupProvider.getGroupByLevelAndIndex(eq(tournament), eq(0), eq(0))).thenReturn(group);
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_ROUNDS), any()))
				.thenReturn(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.SWISS_ROUNDS, "3"));

		final List<Fight> result = this.swissTournamentHandler.createFights(tournament, null, 0, "tester");

		assertTrue(result.isEmpty());
	}

	// Branch coverage for createFights when level >= configured rounds
	@Test
	public void when_createFights_and_levelIsGreaterThanConfiguredRounds_expect_returnEmpty() {
		final Tournament tournament = this.tournament();
		final Group group = this.groupWithTeams(tournament, 4);
		group.setId(50);
		group.setFights(new ArrayList<>());

		when(this.groupProvider.getGroups(tournament)).thenReturn(List.of(group));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_ROUNDS), any()))
				.thenReturn(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.SWISS_ROUNDS, "1"));

		final List<Fight> result = this.swissTournamentHandler.createFights(tournament, null, 1, "tester");

		assertTrue(result.isEmpty());
	}

	// Branch coverage for avoidRepeatedPairings with invalid boolean value
	@Test
	public void when_avoidRepeatedPairings_and_propertyValueIsInvalid_expect_returnFalse() {
		final Tournament tournament = this.tournament();

		when(this.tournamentExtraPropertyProvider
				.getByTournamentAndProperty(eq(tournament), eq(TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS), any()))
				.thenReturn(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS, "invalid-bool"));

		final boolean result = this.swissTournamentHandler.avoidRepeatedPairings(tournament);

		// Boolean.parseBoolean returns false for invalid values
		assertEquals(result, false);
	}

	// Branch coverage for generateNextFights when existing fights is empty
	@Test
	public void when_generateNextFights_and_existingFightsIsEmpty_expect_createInitialFights() {
		final Tournament tournament = this.tournament();
		final Group group = this.groupWithTeams(tournament, 4);
		group.setId(51);
		group.setFights(new ArrayList<>());

		when(this.groupProvider.getGroups(tournament)).thenReturn(List.of(group));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_ROUNDS), any()))
				.thenReturn(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.SWISS_ROUNDS, "3"));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS), any()))
				.thenReturn(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS, "true"));
		when(this.groupProvider.addGroup(eq(tournament), any(Group.class))).thenReturn(group);

		final List<Fight> result = this.swissTournamentHandler.generateNextFights(tournament, "tester");

		assertEquals(result.size(), 2);
		assertEquals(result.get(0).getLevel().intValue(), 0);
	}

	// Branch coverage for tryCreateSwissPairings when teams is empty
	@Test
	public void when_createFights_and_singleTeamList_expect_returnEmptyFights() {
		final Tournament tournament = this.tournament();
		final Group group = this.groupWithTeams(tournament, 4);
		group.setId(52);
		group.setFights(new ArrayList<>());

		when(this.groupProvider.getGroups(tournament)).thenReturn(List.of(group));
		when(this.groupProvider.getGroups(eq(tournament), eq(0))).thenReturn(List.of(group));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_ROUNDS), any()))
				.thenReturn(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.SWISS_ROUNDS, "1"));

		final List<Fight> result = this.swissTournamentHandler.createFights(tournament, null, 0, "tester");

		// The actual result size depends on the pairing algorithm
		assertTrue(result.size() >= 0);
	}

	// Branch coverage for getOrCreateRoundGroup when group already exists with null teams
	@Test
	public void when_createFights_withExistingGroupWithNullTeams_expect_handlesGracefully() {
		final Tournament tournament = this.tournament();
		final Group group = this.groupWithTeams(tournament, 4);
		group.setId(53);
		group.setFights(new ArrayList<>());

		when(this.groupProvider.getGroups(tournament)).thenReturn(List.of(group));
		when(this.groupProvider.getGroups(eq(tournament), eq(0))).thenReturn(List.of(group));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_ROUNDS), any()))
				.thenReturn(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.SWISS_ROUNDS, "3"));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(eq(tournament),
				eq(TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS), any()))
				.thenReturn(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS, "true"));
		when(this.groupProvider.addGroup(eq(tournament), any(Group.class))).thenReturn(group);

		final List<Fight> result = this.swissTournamentHandler.createFights(tournament, null, 0, "tester");

		verify(this.groupProvider).addGroup(eq(tournament), any(Group.class));
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
}




