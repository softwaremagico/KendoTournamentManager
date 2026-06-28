package com.softwaremagico.kt.core.tests.tournament;

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

import com.softwaremagico.kt.core.controller.ClubController;
import com.softwaremagico.kt.core.controller.DuelController;
import com.softwaremagico.kt.core.controller.FightController;
import com.softwaremagico.kt.core.controller.GroupController;
import com.softwaremagico.kt.core.controller.ParticipantController;
import com.softwaremagico.kt.core.controller.RoleController;
import com.softwaremagico.kt.core.controller.TeamController;
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.TournamentExtraPropertyController;
import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.controller.models.TournamentExtraPropertyDTO;
import com.softwaremagico.kt.core.converters.FightConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.models.FightConverterRequest;
import com.softwaremagico.kt.core.providers.RankingProvider;
import com.softwaremagico.kt.core.score.ScoreOfTeam;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.SwissTieBreakRule;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@SpringBootTest
@Test(groups = {"swissTournament16TieBreakRulesTest"})
public class SwissTournament16TeamsTieBreakRulesTest extends AbstractTestNGSpringContextTests {

	// This test builds a deterministic 16-team/4-round Swiss tournament to force
	// ties in match points and validate that ranking changes according to the
	// selected tie-break rule.

	private static final String CLUB_NAME = "Swiss16TieBreakClub";
	private static final String CLUB_CITY = "Swiss16TieBreakCity";
	private static final int MEMBERS = 3;
	private static final int TEAMS = 16;
	private static final int ROUNDS = 4;
	private static final int FIGHTS_PER_ROUND = 8;
	private static final String TOURNAMENT_NAME = "SwissTournament16TeamsTieBreakRulesTest";

	@Autowired
	private TournamentController tournamentController;

	@Autowired
	private TournamentConverter tournamentConverter;

	@Autowired
	private ParticipantController participantController;

	@Autowired
	private RoleController roleController;

	@Autowired
	private TeamController teamController;

	@Autowired
	private ClubController clubController;

	@Autowired
	private RankingProvider rankingProvider;

	@Autowired
	private GroupController groupController;

	@Autowired
	private FightController fightController;

	@Autowired
	private DuelController duelController;

	@Autowired
	private FightConverter fightConverter;

	@Autowired
	private TournamentExtraPropertyController tournamentExtraPropertyController;

	private ClubDTO clubDTO;
	private TournamentDTO tournamentDTO;

	@Test
	public void shouldExposeValidSwissTieBreakRules() {
		// Enum contract: only these rules are valid for SWISS_TIE_BREAK_RULE.
		// If this list changes, validation and ranking tests must be updated.
		Assert.assertEquals(List.of(SwissTieBreakRule.values()),
				List.of(SwissTieBreakRule.BUCHHOLZ, SwissTieBreakRule.MEDIAN_BUCHHOLZ,
						SwissTieBreakRule.SONNEBORN_BERGER, SwissTieBreakRule.DIRECT_ENCOUNTER,
						SwissTieBreakRule.POINT_DIFFERENTIAL));
		// getType is case-insensitive to simplify text-based configuration.
		Assert.assertEquals(SwissTieBreakRule.getType("buchholz"), SwissTieBreakRule.BUCHHOLZ);
		Assert.assertEquals(SwissTieBreakRule.getType("sonneborn_berger"), SwissTieBreakRule.SONNEBORN_BERGER);
	}

	@Test(dependsOnMethods = "shouldExposeValidSwissTieBreakRules")
	public void addClub() {
		this.clubDTO = this.clubController.create(new ClubDTO(CLUB_NAME, CLUB_CITY), null, null);
	}

	@Test(dependsOnMethods = "addClub")
	public void addParticipants() {
		for (int i = 0; i < MEMBERS * TEAMS; i++) {
			this.participantController.create(new ParticipantDTO(String.format("S16TB-%04d", i),
					String.format("name%s", i), String.format("lastname%s", i), this.clubDTO), null, null);
		}
	}

	@Test(dependsOnMethods = "addParticipants")
	public void addTournament() {
		Assert.assertEquals(this.tournamentController.count(), 0);
		this.tournamentDTO = this.tournamentController
				.create(new TournamentDTO(TOURNAMENT_NAME, 1, MEMBERS, TournamentType.SWISS), null, null);
		Assert.assertEquals(this.tournamentController.count(), 1);
	}

	@Test(dependsOnMethods = "addTournament")
	public void configureSwissProperties() {
		// 4 rounds for 16 teams (no byes) and repeated pairings allowed so pairing
		// remains stable and fully reproducible in this test scenario.
		this.tournamentExtraPropertyController.update(new TournamentExtraPropertyDTO(this.tournamentDTO,
				TournamentExtraPropertyKey.SWISS_ROUNDS, String.valueOf(ROUNDS)), null, null);
		this.tournamentExtraPropertyController.update(
				new TournamentExtraPropertyDTO(this.tournamentDTO,
						TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS, Boolean.FALSE.toString()),
				null, null);
		// Buchholz is used first; then each case overrides it via DataProvider.
		this.tournamentExtraPropertyController.update(new TournamentExtraPropertyDTO(this.tournamentDTO,
				TournamentExtraPropertyKey.SWISS_TIE_BREAK_RULE, SwissTieBreakRule.BUCHHOLZ.name()), null, null);

		Assert.assertEquals(this.tournamentExtraPropertyController
				.getByTournamentAndProperty(this.tournamentDTO.getId(), TournamentExtraPropertyKey.SWISS_ROUNDS)
				.getPropertyValue(), String.valueOf(ROUNDS));
		Assert.assertEquals(
				this.tournamentExtraPropertyController.getByTournamentAndProperty(this.tournamentDTO.getId(),
						TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS).getPropertyValue(),
				Boolean.FALSE.toString());
	}

	@Test(dependsOnMethods = "configureSwissProperties")
	public void addRoles() {
		for (final ParticipantDTO competitor : this.participantController.get()) {
			this.roleController.create(new RoleDTO(this.tournamentDTO, competitor, RoleType.COMPETITOR), null, null);
		}
		Assert.assertEquals(this.roleController.count(this.tournamentDTO), this.participantController.count());
	}

	@Test(dependsOnMethods = "addRoles")
	public void addTeams() {
		int teamIndex = 0;
		TeamDTO team = null;
		int teamMember = 0;

		final List<Group> groups = this.groupController.getGroups(this.tournamentDTO, 0);
		Assert.assertEquals(groups.size(), 1);

		for (final ParticipantDTO competitor : this.participantController.get()) {
			if (team == null) {
				teamIndex++;
				team = new TeamDTO("Team" + String.format("%02d", teamIndex), this.tournamentDTO);
				teamMember = 0;
			}

			team.addMember(competitor);
			team = this.teamController.create(team, null, null);

			if (teamMember == 0) {
				this.groupController.addTeams(groups.getFirst().getId(), Collections.singletonList(team), null, null);
			}

			teamMember++;
			if (teamMember >= MEMBERS) {
				team = null;
			}
		}

		Assert.assertEquals(this.teamController.count(this.tournamentDTO), TEAMS);
		Assert.assertEquals(this.groupController.getGroups(this.tournamentDTO, 0).getFirst().getTeams().size(), TEAMS);
	}

	@Test(dependsOnMethods = "addTeams")
	public void createAndAdvanceSwissRoundsWithTieScenarios() {
		final List<Integer> expectedGroupsByLevel = List.of(1, 2, 3, 4);
		// expectedPairingsByRound ensures we ALWAYS test the same pairing topology.
		// scoresByFight defines concrete results that create Swiss-point ties.
		final Map<Integer, List<String>> expectedPairingsByRound = this.getExpectedPairingsByRound();
		final Map<String, int[]> scoresByFight = this.getScoresByFight();

		for (int level = 0; level < ROUNDS; level++) {
			final List<FightDTO> createdFights = this.fightController.createNextFights(this.tournamentDTO.getId(), null,
					null);
			Assert.assertEquals(createdFights.size(), FIGHTS_PER_ROUND);

			final List<Group> roundGroups = this.groupController.getGroups(this.tournamentDTO, level);
			Assert.assertEquals(roundGroups.size(), (int) expectedGroupsByLevel.get(level));
			Assert.assertEquals(roundGroups.stream().map(Group::getIndex).sorted().toList(),
					IntStream.range(0, expectedGroupsByLevel.get(level)).boxed().toList());

			final List<Fight> fightsInRound = roundGroups.stream().flatMap(group -> group.getFights().stream()).toList();
			Assert.assertEquals(fightsInRound.size(), FIGHTS_PER_ROUND);
			final Map<String, Integer> winsBeforeRound = this.getSwissWinsBeforeRoundWithoutByes(level);
			SwissTestAssertions.assertAdjacentBracketFloatsOnly(fightsInRound, winsBeforeRound, level);
			final int minimumCrossBracketPairings = SwissTestAssertions.getMinimumCrossBracketPairings(
					winsBeforeRound,
					null,
					"without byes at level " + level);
			final int actualCrossBracketPairings = SwissTestAssertions.countCrossBracketPairings(fightsInRound, winsBeforeRound);
			Assert.assertTrue(actualCrossBracketPairings >= minimumCrossBracketPairings,
					"Cross-bracket pairings at level " + level + " cannot be below theoretical minimum. "
							+ "actual=" + actualCrossBracketPairings + ", min=" + minimumCrossBracketPairings);
			Assert.assertTrue(actualCrossBracketPairings <= minimumCrossBracketPairings + 2,
					"Cross-bracket pairings at level " + level + " should stay near minimum. "
							+ "actual=" + actualCrossBracketPairings + ", min=" + minimumCrossBracketPairings);
			Assert.assertEquals(fightsInRound.stream().map(this::fightKey).toList(),
					expectedPairingsByRound.get(level));

			for (final Fight fight : fightsInRound) {
				final int[] configuredScore = scoresByFight.get(level + ":" + this.fightKey(fight));
				Assert.assertNotNull(configuredScore,
						"Missing configured score for fight " + level + ":" + this.fightKey(fight));
				this.applyResult(fight, configuredScore[0], configuredScore[1]);
			}
		}
	}

	@Test(dependsOnMethods = "createAndAdvanceSwissRoundsWithTieScenarios")
	public void checkFinalSwissScoreDistribution() {
		// Expected distribution after 4 rounds (match points):
		// 4W: Team01
		// 3W: Team09, Team05, Team15, Team14 (group reordered by tie-break)
		// 2W: Team02, Team03, Team07, Team10, Team11, Team12
		// 1W: Team04, Team06, Team08, Team13
		// 0W: Team16
		// This test fixes the tournament baseline; the next one validates only the
		// internal order in the 3W group.
		final List<ScoreOfTeam> ranking = this.rankingProvider
				.getTeamsScoreRanking(this.tournamentConverter.reverse(this.tournamentDTO));
		Assert.assertEquals(ranking.size(), TEAMS);
		Assert.assertEquals(ranking.getFirst().getTeam().getName(), "Team01");
		Assert.assertEquals(this.getTeamsWithWins(ranking, 4), List.of("Team01"));
		Assert.assertEquals(this.getTeamsWithWins(ranking, 3), List.of("Team09", "Team05", "Team15", "Team14"));
		Assert.assertEquals(this.getTeamsWithWinsSorted(ranking, 2),
				List.of("Team02", "Team03", "Team07", "Team10", "Team11", "Team12"));
		Assert.assertEquals(this.getTeamsWithWinsSorted(ranking, 1), List.of("Team04", "Team06", "Team08", "Team13"));
		Assert.assertEquals(this.getTeamsWithWins(ranking, 0), List.of("Team16"));
	}

	@DataProvider(name = "swissTieBreakRules")
	public Object[][] swissTieBreakRules() {
		// Explanation of expected order inside the 3-win group:
		// - BUCHHOLZ / MEDIAN_BUCHHOLZ / SONNEBORN_BERGER -> Team09, Team05, Team15,
		// Team14
		// In this dataset Team09 and Team05 accumulate stronger opposition; Team14
		// remains last.
		// - DIRECT_ENCOUNTER -> Team15, Team09, Team05, Team14
		// Within tied teams, Team15 wins the head-to-head against Team09 (R2:
		// Team09-Team15, 1-2)
		// and therefore moves ahead.
		// - POINT_DIFFERENTIAL -> Team14, Team05, Team09, Team15
		// Team14 gets the best total point differential and moves to the top of 3W.
		return new Object[][]{{SwissTieBreakRule.BUCHHOLZ, List.of("Team09", "Team05", "Team15", "Team14")},
				{SwissTieBreakRule.MEDIAN_BUCHHOLZ, List.of("Team09", "Team05", "Team15", "Team14")},
				{SwissTieBreakRule.SONNEBORN_BERGER, List.of("Team09", "Team05", "Team15", "Team14")},
				{SwissTieBreakRule.DIRECT_ENCOUNTER, List.of("Team15", "Team09", "Team05", "Team14")},
				{SwissTieBreakRule.POINT_DIFFERENTIAL, List.of("Team14", "Team05", "Team09", "Team15")}};
	}

	@Test(dataProvider = "swissTieBreakRules", dependsOnMethods = "checkFinalSwissScoreDistribution")
	public void checkSwissTieBreakRuleReordersTiedTeams(SwissTieBreakRule rule,
			List<String> expectedThreeWinTeamsOrder) {
		// For each rule: update the tournament property, recalculate ranking, and
		// verify that ONLY the order of tied 3-win teams changes according to the
		// selected criterion.
		this.tournamentExtraPropertyController.update(new TournamentExtraPropertyDTO(this.tournamentDTO,
				TournamentExtraPropertyKey.SWISS_TIE_BREAK_RULE, rule.name()), null, null);

		final TournamentExtraPropertyDTO storedProperty = this.tournamentExtraPropertyController
				.getByTournamentAndProperty(this.tournamentDTO.getId(),
						TournamentExtraPropertyKey.SWISS_TIE_BREAK_RULE);
		Assert.assertEquals(storedProperty.getPropertyValue(), rule.name());

		final List<ScoreOfTeam> ranking = this.rankingProvider
				.getTeamsScoreRanking(this.tournamentConverter.reverse(this.tournamentDTO));
		Assert.assertEquals(ranking.getFirst().getTeam().getName(), "Team01");
		Assert.assertEquals(this.getTeamsWithWins(ranking, 3), expectedThreeWinTeamsOrder,
				"Unexpected 3-win teams order for tie-break rule " + rule);
	}

	private Map<Integer, List<String>> getExpectedPairingsByRound() {
		final Map<Integer, List<String>> pairings = new LinkedHashMap<>();
		// Expected pairings for the deterministic scenario
		// (SWISS_AVOID_REPEATED_PAIRINGS=false).
		// Any change here means the pairing algorithm changed and this test must be
		// reviewed.
		pairings.put(0, List.of("Team01-Team02", "Team03-Team04", "Team05-Team06", "Team07-Team08", "Team09-Team10",
				"Team11-Team12", "Team13-Team14", "Team15-Team16"));
		pairings.put(1, List.of("Team01-Team03", "Team05-Team07", "Team09-Team11", "Team13-Team15", "Team02-Team04",
				"Team06-Team08", "Team10-Team12", "Team14-Team16"));
		pairings.put(2, List.of("Team01-Team05", "Team09-Team15", "Team02-Team03", "Team06-Team07", "Team10-Team11",
				"Team13-Team14", "Team04-Team08", "Team12-Team16"));
		pairings.put(3, List.of("Team01-Team15", "Team02-Team05", "Team07-Team09", "Team10-Team14", "Team03-Team04",
				"Team06-Team11", "Team12-Team13", "Team08-Team16"));
		return pairings;
	}

	private Map<String, int[]> getScoresByFight() {
		final Map<String, int[]> scores = new LinkedHashMap<>();
		// Scores chosen to create a 4-team block tied at 3 wins with non-trivial
		// tie-break outcomes across Buchholz/DirectEncounter/PointDifferential.
		scores.put("0:Team01-Team02", new int[]{2, 1});
		scores.put("0:Team03-Team04", new int[]{2, 0});
		scores.put("0:Team05-Team06", new int[]{2, 0});
		scores.put("0:Team07-Team08", new int[]{2, 1});
		scores.put("0:Team09-Team10", new int[]{2, 1});
		scores.put("0:Team11-Team12", new int[]{2, 1});
		scores.put("0:Team13-Team14", new int[]{2, 1});
		scores.put("0:Team15-Team16", new int[]{2, 1});

		scores.put("1:Team01-Team03", new int[]{2, 0});
		scores.put("1:Team05-Team07", new int[]{2, 0});
		scores.put("1:Team09-Team11", new int[]{2, 1});
		scores.put("1:Team13-Team15", new int[]{1, 2});
		scores.put("1:Team02-Team04", new int[]{2, 0});
		scores.put("1:Team06-Team08", new int[]{2, 0});
		scores.put("1:Team10-Team12", new int[]{2, 0});
		scores.put("1:Team14-Team16", new int[]{2, 0});

		scores.put("2:Team01-Team05", new int[]{2, 1});
		scores.put("2:Team09-Team15", new int[]{1, 2});
		scores.put("2:Team02-Team03", new int[]{2, 0});
		scores.put("2:Team06-Team07", new int[]{1, 2});
		scores.put("2:Team10-Team11", new int[]{2, 1});
		scores.put("2:Team13-Team14", new int[]{0, 2});
		scores.put("2:Team04-Team08", new int[]{2, 1});
		scores.put("2:Team12-Team16", new int[]{2, 0});

		scores.put("3:Team01-Team15", new int[]{2, 1});
		scores.put("3:Team02-Team05", new int[]{1, 2});
		scores.put("3:Team07-Team09", new int[]{1, 2});
		scores.put("3:Team10-Team14", new int[]{0, 2});
		scores.put("3:Team03-Team04", new int[]{2, 1});
		scores.put("3:Team06-Team11", new int[]{0, 2});
		scores.put("3:Team12-Team13", new int[]{2, 1});
		scores.put("3:Team08-Team16", new int[]{2, 0});
		return scores;
	}

	private String fightKey(Fight fight) {
		return fight.getTeam1().getName() + "-" + fight.getTeam2().getName();
	}

	private void applyResult(Fight fight, int team1Score, int team2Score) {
		for (int i = 0; i < team1Score; i++) {
			fight.getDuels().getFirst().addCompetitor1Score(Score.MEN);
		}
		for (int i = 0; i < team2Score; i++) {
			fight.getDuels().getFirst().addCompetitor2Score(Score.MEN);
		}
		fight.getDuels().forEach(duel -> duel.setFinished(true));
		this.fightController.update(this.fightConverter.convert(new FightConverterRequest(fight)), null, null);
	}

	private List<String> getTeamsWithWins(List<ScoreOfTeam> ranking, int wins) {
		return ranking.stream().filter(score -> score.getWonFights() == wins).map(score -> score.getTeam().getName())
				.toList();
	}

	private List<String> getTeamsWithWinsSorted(List<ScoreOfTeam> ranking, int wins) {
		return this.getTeamsWithWins(ranking, wins).stream().sorted().toList();
	}

	private Map<String, Integer> getSwissWinsBeforeRoundWithoutByes(int roundLevel) {
		final Map<String, Integer> winsByTeam = new HashMap<>();
		for (int level = 0; level < roundLevel; level++) {
			final List<Fight> fightsAtLevel = this.groupController.getGroups(this.tournamentDTO, level).stream()
					.flatMap(group -> group.getFights().stream()).toList();
			for (final Fight fight : fightsAtLevel) {
				winsByTeam.putIfAbsent(fight.getTeam1().getName(), 0);
				winsByTeam.putIfAbsent(fight.getTeam2().getName(), 0);
				if (fight.getWinner() != null) {
					winsByTeam.computeIfPresent(fight.getWinner().getName(), (ignored, value) -> value + 1);
				}
			}
		}
		return winsByTeam;
	}

	@AfterClass(alwaysRun = true)
	public void deleteTournament() {
		if (this.tournamentDTO != null) {
			this.groupController.delete(this.tournamentDTO);
			this.fightController.delete(this.tournamentDTO);
			this.duelController.delete(this.tournamentDTO);
			this.teamController.delete(this.tournamentDTO);
			this.roleController.delete(this.tournamentDTO);
			this.tournamentController.delete(this.tournamentDTO, null, null);
		}
		this.participantController.deleteAll();
		if (this.clubDTO != null) {
			this.clubController.delete(this.clubDTO, null, null);
		}
		Assert.assertEquals(this.fightController.count(), 0);
		Assert.assertEquals(this.duelController.count(), 0);
	}
}
