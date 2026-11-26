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

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** A tied championship final must be resolved by the selected representatives. */
public class ChampionshipDrawTest extends AdvancedBrowserTest {

    @Test
    public void tiedChampionshipFinalRequiresDaihyosenAndRanksItsWinner() {
        seed("Championship", 2, 2);
        fights.generateChampionshipFights();
        client.waitForAtLeast(By.cssSelector("fight"), 1);
        Assert.assertEquals(persistedFights().size(), 1);
        final Map<String, Object> semifinal = persistedFights().getFirst();
        final int leftTeam = number(map(semifinal.get("team1")), "id");
        final int rightTeam = number(map(semifinal.get("team2")), "id");

        // A non-zero tied score exercises both ippon and duel-win tie criteria.
        score(duel(0), 0, 0, "Men");
        score(duel(0), 0, 1, "Men");
        finish(duel(0));
        score(duel(1), 1, 0, "Kote");
        score(duel(1), 1, 1, "Kote");
        finish(duel(1));
        reloadFights();
        reloadFights();

        final Map<String, Object> tiedGroup = list(api("GET", "/groups/tournaments/" + tournamentId, null)).stream()
                .filter(group -> list(group.get("fights")).stream().anyMatch(fight -> number(fight, "id") == number(semifinal, "id")))
                .findFirst().orElseThrow();
        final int groupId = number(tiedGroup, "id");
        final List<Map<String, Object>> tiedRanking = list(api("GET", "/rankings/teams/groups/" + groupId, null));
        Assert.assertEquals(tiedRanking.size(), 2);
        Assert.assertEquals(number(tiedRanking.get(0), "sortingIndex"), number(tiedRanking.get(1), "sortingIndex"));
        Assert.assertTrue(persistedFights().stream().noneMatch(fight -> number(fight, "level") > 0),
                "A two-team championship must retain its single final.");

        duel(0).click();
        icon("teams-classification");
        client.clickWizardryButton(By.xpath("//*[@id='teams-ranking-popup']//button[normalize-space()='Undraw']"));
        client.findVisible(By.id("untie-teams-popup"));
        for (WebElement selector : driver.findElements(By.cssSelector("#untie-teams-popup member-selector"))) {
            selector.findElements(By.cssSelector("button.user-selectable")).getFirst().click();
        }
        client.clickWizardryButton(By.cssSelector("#untie-teams-popup button[primary]"));
        closeRankings();
        reloadFights();
        final List<Map<String, Object>> unties = list(api("GET", "/duels/tournaments/" + tournamentId + "/unties", null));
        Assert.assertEquals(unties.size(), 1);
        final int representative = number(map(unties.getFirst().get("competitor1")), "id");
        final Map<String, Object> before = statistics(representative);
        final Set<Integer> winningMembers = list(map(semifinal.get("team1")).get("members")).stream()
                .map(member -> number(member, "id")).collect(Collectors.toSet());
        Assert.assertTrue(winningMembers.contains(representative));

        score(client.findVisible(By.cssSelector("untie-fight duel")), 0, 0, "Men");
        finish(client.findVisible(By.cssSelector("untie-fight duel")));
        reloadFights();
        Assert.assertEquals(persistedFights().size(), 1, "The tiebreak must be a separate duel, not a duplicated final.");
        final List<Map<String, Object>> ranking = list(api("GET", "/rankings/teams/groups/" + groupId, null));
        Assert.assertEquals(number(map(ranking.getFirst().get("team")), "id"), leftTeam);
        Assert.assertEquals(number(ranking.getFirst(), "untieDuels"), 1);
        Assert.assertEquals(number(map(ranking.get(1).get("team")), "id"), rightTeam);
        Assert.assertTrue(number(ranking.getFirst(), "sortingIndex") < number(ranking.get(1), "sortingIndex"));
        Assert.assertEquals(number(statistics(representative), "menNumber"), number(before, "menNumber") + 1);
        Assert.assertTrue(Boolean.TRUE.equals(list(api("GET", "/duels/tournaments/" + tournamentId + "/unties", null))
                .getFirst().get("finished")));
    }

    private Map<String, Object> statistics(int participant) {
        return map(map(api("GET", "/statistics/participants/" + participant, null)).get("participantFightStatistics"));
    }
}
