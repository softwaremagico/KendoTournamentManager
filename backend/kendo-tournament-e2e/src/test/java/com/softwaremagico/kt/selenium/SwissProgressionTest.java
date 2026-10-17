package com.softwaremagico.kt.selenium;

/*-
 * #%L
 * Kendo Tournament Manager (End-to-End Tests)
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

import com.softwaremagico.kt.selenium.components.InputField;
import com.softwaremagico.kt.selenium.components.Dropdown;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.*;

/** Computes expected Swiss points and Buchholz independently from application rankings. */
public class SwissProgressionTest extends AdvancedBrowserTest {
    @Test
    public void completesThreeSwissRoundsWithoutRepeatedOpponentsAndRanksByBuchholz() { runSwiss(8, 3); }

    @Test
    public void oddSwissFieldRotatesByesWithoutInventingParticipantHits() { runSwiss(5, 3); }

    private void runSwiss(int count, int rounds) {
        seed("Swiss System", count, 1);
        final Map<String,Object> definition=map(api("GET","/tournaments/"+tournamentId,null));
        final Map<String,Object> scoring=map(definition.get("tournamentScore"));
        scoring.put("pointsByVictory",3);scoring.put("pointsByDraw",1);
        Assert.assertEquals(number(request(driver,"PUT","/tournaments",definition),"status"),200);
        options();
        new InputField(client, "swiss-rounds").setValue(Integer.toString(rounds));
        new Dropdown(client, "tournament-swiss-tie-break").select("Buchholz");
        closeOptions();
        fights.generateLeagueFights();
        final Set<String> pairings = new HashSet<>();
        final Set<Integer> previousByes = new HashSet<>();
        final Map<Integer, Integer> points = new HashMap<>();
        final Map<Integer, Integer> expectedHits = new HashMap<>();
        final Map<Integer, List<Integer>> opponents = new HashMap<>();
        final List<Map<String, Object>> teams = list(api("GET", "/teams/tournaments/" + tournamentId, null));
        teams.forEach(team -> points.put(number(team, "id"), 0));
        for (int round = 0; round < rounds; round++) {
            final int level = round;
            client.getWebDriverWait().withMessage(() -> "Swiss fights: " + persistedFights()).until(browser -> persistedFights().stream().filter(f -> number(f, "level") == level).count() == count / 2);
            final List<Map<String, Object>> current = persistedFights().stream().filter(f -> number(f, "level") == level).toList();
            final Set<Integer> played = new HashSet<>();
            for (Map<String, Object> fight : current) {
                final int a = number(map(fight.get("team1")), "id");
                final int b = number(map(fight.get("team2")), "id");
                Assert.assertTrue(played.add(a) && played.add(b), "A team may play only once per round.");
                Assert.assertTrue(pairings.add(Math.min(a,b) + ":" + Math.max(a,b)), "Avoidable repeated pairing.");
                opponents.computeIfAbsent(a, key -> new ArrayList<>()).add(b);
                opponents.computeIfAbsent(b, key -> new ArrayList<>()).add(a);
                points.merge(a, 3, Integer::sum);
                final Map<String,Object> participant = map(list(fight.get("duels")).getFirst().get("competitor1"));
                expectedHits.merge(number(participant, "id"), 2, Integer::sum);
            }
            if (count % 2 == 1) {
                final int bye = points.keySet().stream().filter(id -> !played.contains(id)).findFirst().orElseThrow();
                Assert.assertTrue(previousByes.add(bye), "A bye must rotate while unassigned teams remain.");
                points.merge(bye, 3, Integer::sum);
            }
            for (Map<String,Object> fight : current) {
                final String left = (String) map(fight.get("team1")).get("name");
                final String right = (String) map(fight.get("team2")).get("name");
                final String xpath = "//fight[.//*[@data-left-team='" + left + "' and @data-right-team='" + right + "']]//duel[not(contains(@class,'substitute'))]";
                score(client.findVisible(By.xpath(xpath)), 0, 0, "Men");
                score(client.findVisible(By.xpath(xpath)), 0, 1, "Men");
                finish(client.findVisible(By.xpath(xpath)));
            }
        }
        reloadFights();
        Assert.assertEquals(persistedFights().size(), count / 2 * rounds, "Configured round limit must stop generation.");
        Assert.assertTrue(persistedFights().stream().allMatch(f -> number(f, "level") < rounds));
        final List<Map<String,Object>> ranking = list(api("GET", "/rankings/teams/tournaments/" + tournamentId, null));
        final Map<Integer,Integer> configuredPoints = new HashMap<>();
        ranking.forEach(row -> configuredPoints.put(number(map(row.get("team")),"id"),points.get(number(map(row.get("team")),"id"))));
        int previousPoints = Integer.MAX_VALUE;
        double previousBuchholz = Double.MAX_VALUE;
        for (Map<String,Object> row : ranking) {
            final int id = number(map(row.get("team")), "id");
            final int expected = points.get(id);
            Assert.assertEquals(number(row,"wonFights") * 3 + number(row,"drawFights"), expected);
            final int buchholz = opponents.getOrDefault(id, List.of()).stream().mapToInt(configuredPoints::get).sum();
            // Bye treatment is checked through actual Swiss points; even-field Buchholz has no virtual opponents.
            if (count % 2 == 0) {
                Assert.assertEquals(((Number) row.get("swissTieBreakValue")).doubleValue(), (double) buchholz);
                if (expected == previousPoints) { Assert.assertTrue(buchholz <= previousBuchholz); }
            }
            Assert.assertTrue(expected <= previousPoints);
            previousPoints = expected; previousBuchholz = buchholz;
        }
        for (Map<String,Object> team : teams) {
            final int participant = number(map(((List<?>) team.get("members")).getFirst()), "id");
            final Map<String,Object> stats = map(map(api("GET", "/statistics/participants/" + participant, null)).get("participantFightStatistics"));
            Assert.assertEquals(number(stats,"menNumber"), expectedHits.getOrDefault(participant,0).intValue(), "Byes must not fabricate hits.");
        }
    }
}
