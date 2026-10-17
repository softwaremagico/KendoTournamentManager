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
import com.softwaremagico.kt.selenium.components.Popup;
import com.softwaremagico.kt.selenium.components.Table;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.List;
import java.util.Map;

public class TimerAndShiaijoTest extends AdvancedBrowserTest {
    private String time() { return client.findVisible(By.cssSelector("popup-timer .time")).getText(); }

    @Test
    public void timerStartsPausesResumesAndPersistsOnlyForSelectedDuel() {
        seed("League",3,1); fights.generateLeagueFights();
        duel(0).click(); icon("timer");
        final String initial = time();
        client.clickFirstVisible(By.cssSelector("popup-timer button.play"));
        client.getWebDriverWait().until(browser -> !time().equals(initial));
        client.clickFirstVisible(By.cssSelector("popup-timer button.pause"));
        final String paused = time();
        final long pausedAt=System.nanoTime();
        new org.openqa.selenium.support.ui.WebDriverWait(driver,java.time.Duration.ofSeconds(3))
                .until(browser -> { Assert.assertEquals(time(),paused); return System.nanoTime()-pausedAt>java.time.Duration.ofSeconds(2).toNanos(); });
        client.clickFirstVisible(By.cssSelector("popup-timer button.play"));
        client.getWebDriverWait().until(browser -> !time().equals(paused));
        client.clickFirstVisible(By.cssSelector("popup-timer button.pause"));
        client.clickFirstVisible(By.cssSelector("popup-timer button.stop"));
        closeRankings();
        reloadFights();
        final List<Map<String,Object>> all = persistedFights();
        final Map<String,Object> timed = list(all.getFirst().get("duels")).getFirst();
        Assert.assertTrue(number(timed,"duration") >= 2);
        Assert.assertTrue(Boolean.TRUE.equals(timed.get("finished")));
        for (int i=1;i<all.size();i++) {
            final Object duration = list(all.get(i).get("duels")).getFirst().get("duration");
            Assert.assertTrue(duration == null || ((Number)duration).intValue()==0,"Another duel must not inherit elapsed time.");
        }
        final int participant = number(map(timed.get("competitor1")),"id");
        final Map<String,Object> statistics = map(map(api("GET","/statistics/participants/"+participant,null)).get("participantFightStatistics"));
        Assert.assertEquals(number(statistics,"duelsNumber"),2,"Participant has two scheduled league duels.");
        Assert.assertTrue(number(statistics,"totalDuelsTime")>=2,"Finished draw time must reach participant statistics.");
    }

    @Test
    public void multipleShiaijosFilterIndependentFightsAndKeepScoresOnTheirAssignedCourt() {
        seed("Custom",4,1);
        tournaments.open(URL); tournaments.select(tournament);
        new Table(client,"tournaments-table").clickAction("button-edit");
        new InputField(client,"tournament-shiaijos").setValue("2");
        client.clickWizardryButton(By.id("tournament-button-save"));
        new Popup(client,"tournament-popup").close(); tournaments.openFights();
        final List<Map<String,Object>> teams = list(api("GET","/teams/tournaments/"+tournamentId,null));
        // Court allocation uses the same authenticated REST contract as the fight creator.
        for(int i=0;i<2;i++) {
            final Map<String,Object> fight = new java.util.HashMap<>();
            fight.put("tournament",api("GET","/tournaments/"+tournamentId,null));
            fight.put("team1",teams.get(i*2)); fight.put("team2",teams.get(i*2+1)); fight.put("shiaijo",i); fight.put("level",0);
            final Map<String,Object> generated = map(api("PUT","/fights/duels",fight));
            final Object draftId=generated.get("id");
            final List<Map<String,Object>> groups = list(api("GET","/groups/tournaments/"+tournamentId,null));
            final Map<String,Object> group=groups.getFirst();
            final List<Map<String,Object>> groupFights = new java.util.ArrayList<>(list(group.get("fights"))); groupFights.add(generated); group.put("fights",groupFights);
            api("PUT","/groups",group);
            api("DELETE","/fights/"+draftId,null);
        }
        reloadFights();
        final String editor="court."+System.currentTimeMillis()+"@test.local";
        final com.softwaremagico.kt.selenium.pages.UserPage users=new com.softwaremagico.kt.selenium.pages.UserPage(client);
        users.open(URL);users.create(editor,PASSWORD);users.setEditorRole(editor);
        tournaments.open(URL);tournaments.select(tournament);tournaments.openFights();
        other=browser();login(other,editor);
        final WebDriverClient remote=new WebDriverClient(other);
        final com.softwaremagico.kt.selenium.pages.TournamentPage view=new com.softwaremagico.kt.selenium.pages.TournamentPage(remote);
        view.open(URL);view.select(tournament);view.openFights();
        selectCourt(client,"A");selectCourt(remote,"B");
        final String teamA=client.findVisible(By.cssSelector("fight .fight-container")).getAttribute("data-left-team");
        final String teamB=remote.findVisible(By.cssSelector("fight .fight-container")).getAttribute("data-left-team");
        Assert.assertNotEquals(teamA,teamB);
        Assert.assertEquals(driver.findElements(By.cssSelector("fight")).size(),1);
        Assert.assertEquals(other.findElements(By.cssSelector("fight")).size(),1);
        score(duel(0),0,0,"Men");
        Assert.assertTrue(remote.findAll(By.cssSelector("fight .point-value")).isEmpty(),"Court B must not inherit court A score.");
        remote.clickFirstVisible(By.cssSelector("button.expand-button"));remote.findVisible(By.cssSelector(".project-mode"));
        Assert.assertEquals(remote.findVisible(By.cssSelector("fight .fight-container")).getAttribute("data-left-team"),teamB);
        final List<Map<String,Object>> stored=persistedFights();
        Assert.assertEquals(((List<?>)list(stored.stream().filter(f->map(f.get("team1")).get("name").equals(teamA)).findFirst().orElseThrow().get("duels")).getFirst().get("competitor1Score")).stream().filter(java.util.Objects::nonNull).toList(),List.of("MEN"));
        Assert.assertTrue(list(list(stored.stream().filter(f->map(f.get("team1")).get("name").equals(teamB)).findFirst().orElseThrow().get("duels")).getFirst().get("competitor1Score")).isEmpty());
    }

    private void selectCourt(WebDriverClient browser,String court) {
        for(int i=0;i<3;i++) {
            final String label=browser.findVisible(By.cssSelector(".shiaijo-name")).getText();
            if(label.equals(court)) {return;}
            browser.clickFirstVisible(By.cssSelector(".shiaijo-name"));
        }
        Assert.fail("Court not available: "+court);
    }
}
