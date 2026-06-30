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

import com.softwaremagico.kt.persistence.entities.Fight;
import org.testng.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class SwissTestAssertions {

    private SwissTestAssertions() {
    }

    static void assertAdjacentBracketFloatsOnly(List<Fight> fightsInRound,
                                                Map<String, Integer> winsBeforeRound,
                                                int roundLevel) {
        for (final Fight fight : fightsInRound) {
            final int team1Wins = winsBeforeRound.getOrDefault(fight.getTeam1().getName(), 0);
            final int team2Wins = winsBeforeRound.getOrDefault(fight.getTeam2().getName(), 0);
            Assert.assertTrue(Math.abs(team1Wins - team2Wins) <= 1,
                    "Non-adjacent float at level " + roundLevel + ": "
                            + fight.getTeam1().getName() + "(" + team1Wins + ") vs "
                            + fight.getTeam2().getName() + "(" + team2Wins + ")");
        }
    }

    static int countCrossBracketPairings(List<Fight> fightsInRound, Map<String, Integer> winsBeforeRound) {
        return (int) fightsInRound.stream()
                .filter(fight -> winsBeforeRound.getOrDefault(fight.getTeam1().getName(), 0).intValue()
                        != winsBeforeRound.getOrDefault(fight.getTeam2().getName(), 0).intValue())
                .count();
    }

    static int getMinimumCrossBracketPairings(Map<String, Integer> winsBeforeRound, Integer byeWins, String messageCtx) {
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
                Assert.assertEquals(effectiveSize % 2, 0,
                        "Bottom bracket must have even effective size " + messageCtx);
            } else if (effectiveSize % 2 != 0) {
                minimumCrossPairings++;
                incomingFloater = 1;
            } else {
                incomingFloater = 0;
            }
        }

        return minimumCrossPairings;
    }

    static void assertNoRematchAndSingleAppearance(List<Fight> fightsInRound,
                                                    Set<String> playedPairs,
                                                    int roundLevel) {
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
}

