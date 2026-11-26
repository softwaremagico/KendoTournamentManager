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

import com.softwaremagico.kt.selenium.pages.LoginPage;
import com.softwaremagico.kt.selenium.pages.TournamentPage;
import com.softwaremagico.kt.selenium.pages.UserPage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.List;
import java.util.Map;

public class ResultCorrectionTest extends AdvancedBrowserTest {
    @Test
    public void reopeningCorrectingAndRemovingScoresRecomputesRankingsStatisticsAndSecondSession() {
        seed("League",2,1);
        fights.generateLeagueFights();
        final Map<String,Object> original = list(persistedFights().getFirst().get("duels")).getFirst();
        final int first = number(map(original.get("competitor1")),"id");
        final int second = number(map(original.get("competitor2")),"id");
        score(duel(0),0,0,"Men"); score(duel(0),0,1,"Men"); finish(duel(0));
        final String editor = "correction." + System.currentTimeMillis() + "@test.local";
        final UserPage users = new UserPage(client);
        users.open(URL); users.create(editor,PASSWORD); users.setEditorRole(editor);
        other = browser(); login(other,editor);
        final WebDriverClient remote = new WebDriverClient(other);
        final TournamentPage remoteTournaments = new TournamentPage(remote);
        remoteTournaments.open(URL); remoteTournaments.select(tournament); remoteTournaments.openFights();
        tournaments.open(URL); tournaments.select(tournament); tournaments.openFights();
        duel(0).click(); icon("uncheck");
        client.getWebDriverWait().until(browser -> !duel(0).getAttribute("class").contains("over"));
        score(duel(0),0,0,"Clear"); score(duel(0),0,1,"Clear");
        score(duel(0),1,0,"Kote"); score(duel(0),1,1,"Kote");
        finish(duel(0));
        remote.getWebDriverWait().until(browser -> browser.findElements(By.cssSelector("duel .point-value")).stream()
                .map(WebElement::getText).toList().equals(List.of("K","K")));
        reloadFights();
        Assert.assertEquals(list(persistedFights().getFirst().get("duels")).getFirst().get("competitor1Score"),List.of());
        final List<Map<String,Object>> ranking = list(api("GET","/rankings/competitors/tournaments/" + tournamentId,null));
        Assert.assertEquals(number(map(ranking.getFirst().get("competitor")),"id"),second);
        Assert.assertEquals(number(ranking.getFirst(),"wonDuels"),1);
        final Map<String,Object> firstStats = map(map(api("GET","/statistics/participants/" + first,null)).get("participantFightStatistics"));
        final Map<String,Object> secondStats = map(map(api("GET","/statistics/participants/" + second,null)).get("participantFightStatistics"));
        Assert.assertEquals(number(firstStats,"menNumber"),0);
        Assert.assertEquals(number(firstStats,"receivedKoteNumber"),2);
        Assert.assertEquals(number(secondStats,"koteNumber"),2);
        Assert.assertEquals(number(secondStats,"receivedMenNumber"),0);
    }

    @Test
    public void tiedTeamLeagueCreatesDaihyosenAndAttributesDecidingHitToRepresentative() {
        seed("League",2,2); fights.generateLeagueFights();
        finish(duel(0));
        finish(duel(1));
        client.refresh();client.findVisible(By.cssSelector("fight"));
        final List<Map<String,Object>> tiedRanking = teamRanking();
        Assert.assertEquals(tiedRanking.size(),2);
        for (Map<String,Object> row : tiedRanking) {
            Assert.assertEquals(number(row,"sortingIndex"),0);
            Assert.assertEquals(number(row,"drawFights"),1);
            Assert.assertEquals(number(row,"drawDuels"),2);
            Assert.assertEquals(number(row,"wonFights"),0);
        }
        icon("teams-classification");
        client.waitForAtLeast(By.cssSelector("#teams-ranking-popup .team-name.draw"),2);
        createRepresentativeDuel();
        final WebElement untie = client.findVisible(By.cssSelector("untie-fight duel"));
        final List<Map<String,Object>> duels = list(api("GET","/duels/tournaments/" + tournamentId + "/unties",null));
        Assert.assertEquals(duels.size(),1);
        final int representative = number(map(duels.getFirst().get("competitor1")),"id");
        final int winningTeam = representativeTeam(representative,tiedRanking);
        score(untie,0,0,"Men"); finish(untie);
        reloadFights();
        final List<Map<String,Object>> ranking = teamRanking();
        Assert.assertEquals(number(map(ranking.getFirst().get("team")),"id"),winningTeam);
        Assert.assertEquals(number(ranking.getFirst(),"sortingIndex"),0);
        Assert.assertEquals(number(ranking.get(1),"sortingIndex"),1);
        Assert.assertEquals(number(ranking.getFirst(),"untieDuels"),1);
        Assert.assertEquals(number(ranking.get(1),"untieDuels"),0);
        final Map<String,Object> stats = map(map(api("GET","/statistics/participants/" + representative,null)).get("participantFightStatistics"));
        Assert.assertEquals(number(stats,"menNumber"),1);
        Assert.assertTrue(Boolean.TRUE.equals(list(api("GET","/duels/tournaments/" + tournamentId + "/unties",null)).getFirst().get("finished")));
        Assert.assertEquals(fights.getWinner(),map(ranking.getFirst().get("team")).get("name"));
        Assert.assertTrue(driver.findElements(By.cssSelector("#teams-ranking-popup .team-name.draw")).isEmpty());
    }

    @Test
    public void tiedChampionshipGroupBlocksFinalUntilRepresentativeWins() {
        seed("Championship",4,2);
        fights.generateChampionshipFights();
        Assert.assertEquals(persistedFights().size(),2);
        // Resolve the other semifinal first so that only the tied group blocks the final.
        score(duel(2),0,0,"Men"); finish(duel(2));
        score(duel(3),0,0,"Men"); finish(duel(3));
        reloadFights();
        finish(duel(0)); finish(duel(1));
        closeRankings();
        Assert.assertEquals(persistedFights().size(),2,"A tied semifinal must not generate the final.");
        Assert.assertTrue(persistedFights().stream().flatMap(fight -> list(fight.get("duels")).stream())
                .allMatch(duel -> Boolean.TRUE.equals(duel.get("finished"))),"All semifinal duels must be finished: " + persistedFights());
        final int tiedFightId = number(persistedFights().getFirst(),"id");
        final List<Map<String,Object>> groups = list(api("GET","/groups/tournaments/" + tournamentId,null));
        final Map<String,Object> tiedGroup = groups.stream().filter(group -> number(group,"level") == 0
                && list(group.get("fights")).stream().anyMatch(fight -> number(fight,"id") == tiedFightId))
                .findFirst().orElseThrow();
        final String groupRankingPath = "/rankings/teams/groups/" + number(tiedGroup,"id");
        final List<Map<String,Object>> tiedRanking = list(api("GET",groupRankingPath,null));
        Assert.assertEquals(tiedRanking.size(),2);
        Assert.assertTrue(tiedRanking.stream().allMatch(row -> number(row,"sortingIndex") == 0));
        // Knockout groups automatically create their critical daihyosen.
        reloadFights();
        client.findVisible(By.cssSelector("untie-fight duel"));
        Assert.assertEquals(persistedFights().size(),2,"An unfinished daihyosen must keep the final blocked.");
        final List<Map<String,Object>> unties = list(api("GET","/duels/tournaments/" + tournamentId + "/unties",null));
        Assert.assertEquals(unties.size(),1);
        Assert.assertEquals(unties.getFirst().get("finished"),Boolean.FALSE);
        // Choose the second representative to catch progression based on the original row order.
        final int winner = representativeTeam(number(map(unties.getFirst().get("competitor2")),"id"),tiedRanking);
        final int loser = number(map(tiedRanking.stream().filter(row -> number(map(row.get("team")),"id") != winner)
                .findFirst().orElseThrow().get("team")),"id");
        final WebElement untie = client.findVisible(By.cssSelector("untie-fight duel"));
        score(untie,1,0,"Kote"); finish(untie);
        client.getWebDriverWait().until(browser -> persistedFights().size() == 3);
        reloadFights();
        final Map<String,Object> finalFight = persistedFights().stream().filter(fight -> number(fight,"level") == 1).findFirst().orElseThrow();
        final List<Integer> finalists = List.of(number(map(finalFight.get("team1")),"id"),number(map(finalFight.get("team2")),"id"));
        Assert.assertTrue(finalists.contains(winner),"The daihyosen winner must advance.");
        Assert.assertFalse(finalists.contains(loser),"The tied group's losing team must be eliminated.");
        final List<Map<String,Object>> resolvedRanking = list(api("GET",groupRankingPath,null));
        Assert.assertEquals(number(map(resolvedRanking.getFirst().get("team")),"id"),winner);
        Assert.assertEquals(number(resolvedRanking.getFirst(),"untieDuels"),1);
        Assert.assertEquals(number(resolvedRanking.get(1),"sortingIndex"),1);
        fights.resolveAllFightsFor((String) map(resolvedRanking.getFirst().get("team")).get("name"));
        Assert.assertEquals(fights.getWinner(),map(resolvedRanking.getFirst().get("team")).get("name"));
        Assert.assertEquals(persistedFights().size(),3,"Resolving the final must not duplicate bracket fights.");
    }

    private List<Map<String,Object>> teamRanking() {
        return list(api("GET","/rankings/teams/tournaments/" + tournamentId,null));
    }

    private int representativeTeam(int participant, List<Map<String,Object>> ranking) {
        return ranking.stream().map(row -> map(row.get("team")))
                .filter(team -> list(team.get("members")).stream().anyMatch(member -> number(member,"id") == participant))
                .mapToInt(team -> number(team,"id")).findFirst().orElseThrow();
    }

    private void createRepresentativeDuel() {
        client.clickWizardryButton(By.xpath("//*[@id='teams-ranking-popup']//button[normalize-space()='Undraw']"));
        client.findVisible(By.id("untie-teams-popup"));
        for (WebElement selector : driver.findElements(By.cssSelector("#untie-teams-popup member-selector"))) {
            selector.findElements(By.cssSelector("button.user-selectable")).getFirst().click();
        }
        client.clickWizardryButton(By.cssSelector("#untie-teams-popup button[primary]"));
        closeRankings();
        client.refresh(); client.findVisible(By.cssSelector("fight"));
    }
}
