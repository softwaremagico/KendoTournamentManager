package com.softwaremagico.kt.core.tests.tournament;

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

import com.softwaremagico.kt.core.controller.GroupController;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.score.ScoreOfTeam;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.values.SwissTieBreakRule;
import org.testng.Assert;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class SwissTestAssertions {

	private static final double EPSILON = 0.0001d;

	private SwissTestAssertions() {
	}

	static void assertAdjacentBracketFloatsOnly(List<Fight> fightsInRound, Map<String, Integer> winsBeforeRound,
			int roundLevel) {
		for (final Fight fight : fightsInRound) {
			final int team1Wins = winsBeforeRound.getOrDefault(fight.getTeam1().getName(), 0);
			final int team2Wins = winsBeforeRound.getOrDefault(fight.getTeam2().getName(), 0);
			Assert.assertTrue(Math.abs(team1Wins - team2Wins) <= 1,
					"Non-adjacent float at level " + roundLevel + ": " + fight.getTeam1().getName() + "(" + team1Wins
							+ ") vs " + fight.getTeam2().getName() + "(" + team2Wins + ")");
		}
	}

	static int countCrossBracketPairings(List<Fight> fightsInRound, Map<String, Integer> winsBeforeRound) {
		return (int) fightsInRound.stream().filter(fight -> winsBeforeRound.getOrDefault(fight.getTeam1().getName(), 0)
				.intValue() != winsBeforeRound.getOrDefault(fight.getTeam2().getName(), 0).intValue()).count();
	}

	static int getMinimumCrossBracketPairings(Map<String, Integer> winsBeforeRound, Integer byeWins,
			String messageCtx) {
		final Map<Integer, Integer> bracketSizes = new HashMap<>();
		winsBeforeRound.values().forEach(wins -> bracketSizes.merge(wins, 1, Integer::sum));

		if (byeWins != null) {
			bracketSizes.computeIfPresent(byeWins, (ignored, count) -> count - 1);
		}

		final int maxWins = bracketSizes.keySet().stream().max(Integer::compareTo).orElse(0);
		int incomingFloater = 0;
		int minimumCrossPairings = 0;

		for (int wins = maxWins; wins >= 0; wins--) {
			final int bracketSize = bracketSizes.getOrDefault(wins, 0);
			final int effectiveSize = bracketSize + incomingFloater;
			if (wins == 0) {
				Assert.assertEquals(effectiveSize % 2, 0, "Bottom bracket must have even effective size " + messageCtx);
			} else if (effectiveSize % 2 != 0) {
				minimumCrossPairings++;
				incomingFloater = 1;
			} else {
				incomingFloater = 0;
			}
		}

		return minimumCrossPairings;
	}

	static void assertNoRematchAndSingleAppearance(List<Fight> fightsInRound, Set<String> playedPairs, int roundLevel) {
		final Map<String, Integer> appearancesInRound = new HashMap<>();
		for (final Fight fight : fightsInRound) {
			appearancesInRound.merge(fight.getTeam1().getName(), 1, Integer::sum);
			appearancesInRound.merge(fight.getTeam2().getName(), 1, Integer::sum);
			final String pairKey = toPairKey(fight.getTeam1().getName(), fight.getTeam2().getName());
			Assert.assertTrue(playedPairs.add(pairKey),
					"Swiss rematch is not expected in this scenario at level " + roundLevel + ": " + pairKey);
		}

		Assert.assertTrue(appearancesInRound.values().stream().allMatch(count -> count == 1),
				"Each team must appear at most once per round");
	}

	private static String toPairKey(String teamA, String teamB) {
		if (teamA.compareTo(teamB) < 0) {
			return teamA + "::" + teamB;
		}
		return teamB + "::" + teamA;
	}

	static int getTeamPosition(List<ScoreOfTeam> ranking, String teamName) {
		for (int i = 0; i < ranking.size(); i++) {
			if (ranking.get(i).getTeam().getName().equals(teamName)) {
				return i;
			}
		}
		Assert.fail("Team not found in ranking: " + teamName);
		return Integer.MAX_VALUE;
	}

	static List<Fight> getAllRoundFightsWithoutDuplicates(GroupController groupController, TournamentDTO tournament,
			int rounds) {
		final Map<String, Fight> fightsById = new LinkedHashMap<>();
		for (int level = 0; level < rounds; level++) {
			groupController.getGroups(tournament, level).stream().flatMap(group -> group.getFights().stream())
					.forEach(fight -> {
						final String key = fight.getId() != null
								? String.valueOf(fight.getId())
								: fight.getLevel() + ":" + fight.getTeam1().getName() + "-"
										+ fight.getTeam2().getName();
						fightsById.putIfAbsent(key, fight);
					});
		}
		return fightsById.values().stream().toList();
	}

	static Map<String, Integer> getSwissPointsByTeam(List<Fight> fights) {
		final Set<String> teams = new HashSet<>();
		fights.forEach(fight -> {
			teams.add(fight.getTeam1().getName());
			teams.add(fight.getTeam2().getName());
		});
		return getSwissPointsByTeam(fights, teams);
	}

	static Map<String, Integer> getSwissPointsByTeam(List<Fight> fights, Set<String> allTeams) {
		final Map<String, Integer> pointsByTeam = new HashMap<>();
		allTeams.forEach(team -> pointsByTeam.put(team, 0));
		fights.forEach(fight -> {
			pointsByTeam.putIfAbsent(fight.getTeam1().getName(), 0);
			pointsByTeam.putIfAbsent(fight.getTeam2().getName(), 0);
		});
		for (final Fight fight : fights) {
			if (fight.getWinner() != null) {
				pointsByTeam.computeIfPresent(fight.getWinner().getName(), (ignoredTeam, points) -> points + 3);
			} else if (fight.isDrawFight()) {
				pointsByTeam.computeIfPresent(fight.getTeam1().getName(), (ignoredTeam, points) -> points + 1);
				pointsByTeam.computeIfPresent(fight.getTeam2().getName(), (ignoredTeam, points) -> points + 1);
			}
		}

		final Map<Integer, Set<String>> teamsByRound = new HashMap<>();
		fights.forEach(fight -> teamsByRound.computeIfAbsent(fight.getLevel(), ignored -> new HashSet<>()));
		fights.forEach(fight -> {
			teamsByRound.get(fight.getLevel()).add(fight.getTeam1().getName());
			teamsByRound.get(fight.getLevel()).add(fight.getTeam2().getName());
		});
		teamsByRound.values().forEach(teamsInRound -> {
			final Set<String> byeTeams = new HashSet<>(allTeams);
			byeTeams.removeAll(teamsInRound);
			if (byeTeams.size() == 1) {
				pointsByTeam.computeIfPresent(byeTeams.iterator().next(), (ignoredTeam, points) -> points + 3);
			}
		});
		return pointsByTeam;
	}

	static TieBreakExpectation findTieBreakExpectationAtDepth(List<String> teamNames,
											 List<Fight> fights,
											 Map<String, Integer> pointsByTeam,
											 SwissTieBreakRule selectedRule,
											 int depth) {
		final List<SwissTieBreakRule> chain = getTieBreakChain(selectedRule);
		if (depth < 1 || depth > chain.size()) {
			throw new IllegalArgumentException("depth must be in [1," + chain.size() + "]");
		}

		final List<String> sortedTeamNames = teamNames.stream().sorted().toList();
		for (int i = 0; i < sortedTeamNames.size(); i++) {
			final String teamA = sortedTeamNames.get(i);
			for (int j = i + 1; j < sortedTeamNames.size(); j++) {
				final String teamB = sortedTeamNames.get(j);
				if (!pointsByTeam.getOrDefault(teamA, 0).equals(pointsByTeam.getOrDefault(teamB, 0))) {
					continue;
				}

				boolean allPreviousTied = true;
				for (int metricIndex = 0; metricIndex < depth - 1; metricIndex++) {
					final SwissTieBreakRule metricRule = chain.get(metricIndex);
					final double metricA = getTieBreakMetric(metricRule, teamA, fights, pointsByTeam);
					final double metricB = getTieBreakMetric(metricRule, teamB, fights, pointsByTeam);
					if (!equalsMetric(metricA, metricB)) {
						allPreviousTied = false;
						break;
					}
				}
				if (!allPreviousTied) {
					continue;
				}

				final SwissTieBreakRule decidingRule = chain.get(depth - 1);
				final double decidingA = getTieBreakMetric(decidingRule, teamA, fights, pointsByTeam);
				final double decidingB = getTieBreakMetric(decidingRule, teamB, fights, pointsByTeam);
				if (equalsMetric(decidingA, decidingB)) {
					continue;
				}

				final String expectedHigherTeam = decidingA > decidingB ? teamA : teamB;
				final String expectedLowerTeam = decidingA > decidingB ? teamB : teamA;
				return new TieBreakExpectation(selectedRule, depth, decidingRule, expectedHigherTeam, expectedLowerTeam,
						teamA, teamB);
			}
		}
		return null;
	}

	static double getBuchholz(String teamName, List<Fight> fights, Map<String, Integer> pointsByTeam) {
		return getPlayedFights(teamName, fights).stream().map(fight -> getOpponentName(teamName, fight))
				.mapToInt(pointsByTeam::get).sum();
	}

	static double getMedianBuchholz(String teamName, List<Fight> fights, Map<String, Integer> pointsByTeam) {
		final List<Integer> opponentsPoints = getPlayedFights(teamName, fights).stream()
				.map(fight -> getOpponentName(teamName, fight)).map(pointsByTeam::get).sorted().toList();
		if (opponentsPoints.size() <= 2) {
			return opponentsPoints.stream().mapToInt(Integer::intValue).sum();
		}
		return opponentsPoints.subList(1, opponentsPoints.size() - 1).stream().mapToInt(Integer::intValue).sum();
	}

	static double getSonnebornBerger(String teamName, List<Fight> fights, Map<String, Integer> pointsByTeam) {
		double score = 0;
		for (final Fight fight : getPlayedFights(teamName, fights)) {
			final String opponent = getOpponentName(teamName, fight);
			if (opponent == null) {
				continue;
			}
			if (fight.getWinner() != null && fight.getWinner().getName().equals(teamName)) {
				score += pointsByTeam.get(opponent);
			} else if (fight.isDrawFight()) {
				score += pointsByTeam.get(opponent) / 2.0;
			}
		}
		return score;
	}

	static double getDirectEncounter(String teamName, List<Fight> fights, Map<String, Integer> pointsByTeam) {
		final Integer teamPoints = pointsByTeam.get(teamName);
		final List<String> tiedTeams = pointsByTeam.entrySet().stream()
				.filter(entry -> entry.getValue().equals(teamPoints)).map(Map.Entry::getKey).toList();
		int score = 0;
		for (final Fight fight : getPlayedFights(teamName, fights)) {
			final String opponent = getOpponentName(teamName, fight);
			if (opponent == null || !tiedTeams.contains(opponent)) {
				continue;
			}
			if (fight.getWinner() != null && fight.getWinner().getName().equals(teamName)) {
				score += 3;
			} else if (fight.isDrawFight()) {
				score += 1;
			}
		}
		return score;
	}

	static double getPointDifferential(String teamName, List<Fight> fights) {
		return getPlayedFights(teamName, fights).stream().mapToInt(fight -> {
			final com.softwaremagico.kt.persistence.entities.Team team = teamName.equals(fight.getTeam1().getName())
					? fight.getTeam1()
					: fight.getTeam2();
			return fight.getScore(team) - fight.getScoreAgainst(team);
		}).sum();
	}

	private static List<Fight> getPlayedFights(String teamName, List<Fight> fights) {
		return fights.stream().filter(
				fight -> fight.getTeam1().getName().equals(teamName) || fight.getTeam2().getName().equals(teamName))
				.toList();
	}

	private static String getOpponentName(String teamName, Fight fight) {
		if (fight.getTeam1().getName().equals(teamName)) {
			return fight.getTeam2().getName();
		}
		if (fight.getTeam2().getName().equals(teamName)) {
			return fight.getTeam1().getName();
		}
		return null;
	}

	private static List<SwissTieBreakRule> getTieBreakChain(SwissTieBreakRule selectedRule) {
		return switch (selectedRule) {
			case BUCHHOLZ -> List.of(SwissTieBreakRule.BUCHHOLZ, SwissTieBreakRule.MEDIAN_BUCHHOLZ,
					SwissTieBreakRule.SONNEBORN_BERGER, SwissTieBreakRule.DIRECT_ENCOUNTER,
					SwissTieBreakRule.POINT_DIFFERENTIAL);
			case MEDIAN_BUCHHOLZ -> List.of(SwissTieBreakRule.MEDIAN_BUCHHOLZ, SwissTieBreakRule.BUCHHOLZ,
					SwissTieBreakRule.SONNEBORN_BERGER, SwissTieBreakRule.DIRECT_ENCOUNTER,
					SwissTieBreakRule.POINT_DIFFERENTIAL);
			case SONNEBORN_BERGER -> List.of(SwissTieBreakRule.SONNEBORN_BERGER, SwissTieBreakRule.BUCHHOLZ,
					SwissTieBreakRule.MEDIAN_BUCHHOLZ, SwissTieBreakRule.DIRECT_ENCOUNTER,
					SwissTieBreakRule.POINT_DIFFERENTIAL);
			case DIRECT_ENCOUNTER -> List.of(SwissTieBreakRule.DIRECT_ENCOUNTER, SwissTieBreakRule.BUCHHOLZ,
					SwissTieBreakRule.MEDIAN_BUCHHOLZ, SwissTieBreakRule.SONNEBORN_BERGER,
					SwissTieBreakRule.POINT_DIFFERENTIAL);
			case POINT_DIFFERENTIAL -> List.of(SwissTieBreakRule.POINT_DIFFERENTIAL, SwissTieBreakRule.BUCHHOLZ,
					SwissTieBreakRule.MEDIAN_BUCHHOLZ, SwissTieBreakRule.SONNEBORN_BERGER,
					SwissTieBreakRule.DIRECT_ENCOUNTER);
		};
	}

	private static double getTieBreakMetric(SwissTieBreakRule rule,
							 String teamName,
							 List<Fight> fights,
							 Map<String, Integer> pointsByTeam) {
		return switch (rule) {
			case BUCHHOLZ -> getBuchholz(teamName, fights, pointsByTeam);
			case MEDIAN_BUCHHOLZ -> getMedianBuchholz(teamName, fights, pointsByTeam);
			case SONNEBORN_BERGER -> getSonnebornBerger(teamName, fights, pointsByTeam);
			case DIRECT_ENCOUNTER -> getDirectEncounter(teamName, fights, pointsByTeam);
			case POINT_DIFFERENTIAL -> getPointDifferential(teamName, fights);
		};
	}

	private static boolean equalsMetric(double metricA, double metricB) {
		return Math.abs(metricA - metricB) < EPSILON;
	}

	static final class TieBreakExpectation {
		private final SwissTieBreakRule selectedRule;
		private final int decidingDepth;
		private final SwissTieBreakRule decidingRule;
		private final String expectedHigherTeam;
		private final String expectedLowerTeam;
		private final String firstTeam;
		private final String secondTeam;

		private TieBreakExpectation(SwissTieBreakRule selectedRule,
								 int decidingDepth,
								 SwissTieBreakRule decidingRule,
								 String expectedHigherTeam,
								 String expectedLowerTeam,
								 String firstTeam,
								 String secondTeam) {
			this.selectedRule = selectedRule;
			this.decidingDepth = decidingDepth;
			this.decidingRule = decidingRule;
			this.expectedHigherTeam = expectedHigherTeam;
			this.expectedLowerTeam = expectedLowerTeam;
			this.firstTeam = firstTeam;
			this.secondTeam = secondTeam;
		}

		SwissTieBreakRule getSelectedRule() {
			return selectedRule;
		}

		int getDecidingDepth() {
			return decidingDepth;
		}

		SwissTieBreakRule getDecidingRule() {
			return decidingRule;
		}

		String getExpectedHigherTeam() {
			return expectedHigherTeam;
		}

		String getExpectedLowerTeam() {
			return expectedLowerTeam;
		}

		String describePair() {
			return firstTeam + " vs " + secondTeam;
		}
	}
}
