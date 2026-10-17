package com.softwaremagico.kt.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.time.Duration;
import java.util.List;

public class ManualTournamentProgressionTest extends AdvancedBrowserTest {
    private void drag(WebElement source, WebElement target) {
        new Actions(driver).moveToElement(source).pause(Duration.ofMillis(150)).clickAndHold()
                .pause(Duration.ofMillis(200)).moveByOffset(8,0).moveToElement(target)
                .pause(Duration.ofMillis(400)).release().perform();
    }

    @Test
    public void customTournamentCreatesManualFightAndRanksTheRecordedWinner() {
        seed("Custom", 2, 1);
        fights.openCustomFightCreator();
        final By cards = By.cssSelector("#fight-creator team-list team-card mat-card");
        client.findVisible(cards);
        drag(driver.findElements(cards).getFirst(), driver.findElements(By.cssSelector("#fight-creator .team-selected")).getFirst());
        drag(driver.findElements(cards).getFirst(), driver.findElements(By.cssSelector("#fight-creator .team-selected")).get(1));
        client.clickWizardryButton(By.cssSelector("#fight-creator button[primary]"));
        client.waitUntilInvisible(By.id("fight-creator"));
        client.findVisible(By.cssSelector("fight"));
        final String winner = client.findVisible(By.cssSelector("fight .fight-container")).getAttribute("data-left-team");
        fights.resolveFirstFightFor(winner);
        Assert.assertEquals(persistedFights().size(),1);
        Assert.assertTrue(list(persistedFights().getFirst().get("duels")).stream().allMatch(d -> Boolean.TRUE.equals(d.get("finished"))));
        final List<java.util.Map<String,Object>> ranking = list(api("GET", "/rankings/teams/tournaments/" + tournamentId, null));
        Assert.assertEquals(map(ranking.getFirst().get("team")).get("name"), winner);
        Assert.assertEquals(number(ranking.getFirst(),"hits"),2);
    }

    @Test
    public void senbatsuRejectsOutOfRangeChallengeAndEliminatesLosersUntilOneRemains() {
        seed("Senbatsu", 5, 1);
        fights.initializeSenbatsu();
        fights.openSenbatsuFightCreator();
        client.findVisible(By.cssSelector("#senbatsu-fight-creator team-list team-card"));
        final WebElement disabled = client.findVisible(By.cssSelector("#senbatsu-fight-creator team-card mat-card.cdk-drag-disabled"));
        drag(disabled, driver.findElements(By.cssSelector("#senbatsu-fight-creator .team-selected")).get(1));
        Assert.assertTrue(driver.findElements(By.cssSelector("#senbatsu-fight-creator .team-selected")).get(1).findElements(By.cssSelector("team-card")).isEmpty());
        Assert.assertTrue(persistedFights().isEmpty(), "Invalid challenge must not persist a fight.");
        client.clickWizardryButton(By.cssSelector("#senbatsu-fight-creator button[secondary]"));
        fights.openSenbatsuFightCreator();
        for (int round = 0; round < 4; round++) {
            final By eligible = By.cssSelector("#senbatsu-fight-creator team-list mat-card:not(.cdk-drag-disabled)");
            client.getWebDriverWait().withMessage(() -> "Senbatsu popup: "+driver.findElement(By.id("senbatsu-fight-creator")).getText()
                    +"; remaining: "+api("GET","/teams/remaining/tournaments/"+tournamentId,null)).until(browser -> !browser.findElements(eligible).isEmpty());
            drag(client.findVisible(eligible), driver.findElements(By.cssSelector("#senbatsu-fight-creator .team-selected")).get(1));
            client.clickWizardryButton(By.cssSelector("#senbatsu-fight-creator button[primary]"));
            client.waitUntilInvisible(By.id("senbatsu-fight-creator"));
            final int duelIndex = round;
            client.getWebDriverWait().until(browser -> browser.findElements(By.cssSelector("fight duel")).size() > duelIndex);
            score(duel(duelIndex),0,0,"Men"); score(duel(duelIndex),0,1,"Men");
            finish(duel(duelIndex));
            if (round < 3) {
                client.findVisible(By.id("senbatsu-fight-creator"));
                client.refresh();
                client.findVisible(By.cssSelector("fight"));
                fights.openSenbatsuFightCreator();
            }
        }
        Assert.assertEquals(persistedFights().size(),4);
        Assert.assertEquals(list(api("GET", "/teams/remaining/tournaments/" + tournamentId, null)).size(),1);
    }
}
