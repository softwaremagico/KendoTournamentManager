package com.softwaremagico.kt.selenium;

import com.softwaremagico.kt.selenium.components.InputField;
import com.softwaremagico.kt.selenium.components.Popup;
import com.softwaremagico.kt.selenium.components.Table;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class SubstitutionTest extends AdvancedBrowserTest {
    @Test
    public void substituteReplacesMemberOnlyInFutureFightsAndOwnsLaterStatistics() {
        seed("League",3,2);
        // Two team members, one fighting slot: the second slot is a genuine substitute.
        final Map<String,Object> definition=map(api("GET","/tournaments/"+tournamentId,null));
        definition.put("fightSize",1);
        Assert.assertEquals(number(request(driver,"PUT","/tournaments",definition),"status"),200);
        client.refresh(); fights.generateLeagueFights();
        final Map<String,Object> firstFight=persistedFights().getFirst();
        final int original=number(map(list(firstFight.get("duels")).getFirst().get("competitor1")),"id");
        score(duel(0),0,0,"Men");score(duel(0),0,1,"Men");finish(duel(0));
        final List<Map<String,Object>> before=persistedFights();
        final int next=1;
        icon("member-order");
        final WebElement fight=driver.findElements(By.cssSelector("fight")).get(next);
        final List<WebElement> rows=fight.findElements(By.cssSelector("duel"));
        final String substitute=rows.get(1).findElements(By.cssSelector("user-score > .user-score")).get(0).getAttribute("data-participant-name");
        final WebElement source=rows.get(1).findElements(By.cssSelector("user-score")).get(0).findElement(By.cssSelector(".drag-handle"));
        final WebElement target=rows.get(0).findElements(By.cssSelector("user-score")).get(0);
        new Actions(driver).moveToElement(source).clickAndHold().pause(Duration.ofMillis(200)).moveByOffset(0,-8)
                .moveToElement(target).pause(Duration.ofMillis(400)).release().perform();
        client.getWebDriverWait().until(browser -> driver.findElements(By.cssSelector("fight")).get(next)
                .findElements(By.cssSelector("duel user-score > .user-score")).getFirst().getAttribute("data-participant-name").equals(substitute));
        icon("member-order-disable");reloadFights();
        final Map<String,Object> future=list(persistedFights().get(1).get("duels")).getFirst();
        final int replacement=number(map(future.get("competitor1")),"id");
        Assert.assertEquals(list(persistedFights().getFirst().get("duels")).getFirst().get("competitor1Score"),List.of("MEN","MEN"));
        score(duel(1),0,0,"Kote");score(duel(1),0,1,"Kote");finish(duel(1));
        final Map<String,Object> stats=map(map(api("GET","/statistics/participants/"+replacement,null)).get("participantFightStatistics"));
        Assert.assertEquals(number(stats,"koteNumber"),2);
        final Map<String,Object> old=map(map(api("GET","/statistics/participants/"+original,null)).get("participantFightStatistics"));
        Assert.assertEquals(number(old,"menNumber"),2);
    }
}
