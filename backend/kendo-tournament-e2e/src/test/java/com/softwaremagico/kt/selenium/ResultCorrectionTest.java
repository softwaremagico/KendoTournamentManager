package com.softwaremagico.kt.selenium;

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
        icon("teams-classification");
        client.clickWizardryButton(By.xpath("//*[@id='teams-ranking-popup']//button[normalize-space()='Undraw']"));
        client.findVisible(By.id("untie-teams-popup"));
        for (WebElement selector : driver.findElements(By.cssSelector("#untie-teams-popup member-selector"))) {
            selector.findElements(By.cssSelector("button.user-selectable")).getFirst().click();
        }
        client.clickWizardryButton(By.cssSelector("#untie-teams-popup button[primary]"));
        closeRankings();
        client.refresh(); client.findVisible(By.cssSelector("fight"));
        final WebElement untie = client.findVisible(By.cssSelector("untie-fight duel"));
        final List<Map<String,Object>> duels = list(api("GET","/duels/tournaments/" + tournamentId + "/unties",null));
        Assert.assertEquals(duels.size(),1);
        final int representative = number(map(duels.getFirst().get("competitor1")),"id");
        score(untie,0,0,"Men"); finish(untie);
        final List<Map<String,Object>> ranking = list(api("GET","/rankings/teams/tournaments/" + tournamentId,null));
        Assert.assertEquals(number(ranking.getFirst(),"untieDuels"),1);
        final Map<String,Object> stats = map(map(api("GET","/statistics/participants/" + representative,null)).get("participantFightStatistics"));
        Assert.assertEquals(number(stats,"menNumber"),1);
        Assert.assertTrue(Boolean.TRUE.equals(list(api("GET","/duels/tournaments/" + tournamentId + "/unties",null)).getFirst().get("finished")));
    }
}
