package com.softwaremagico.kt.selenium;

/*-
 * #%L
 * Kendo Tournament Manager (End-to-End Tests)
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

import com.softwaremagico.kt.selenium.pages.ClubPage;
import com.softwaremagico.kt.selenium.pages.FightPage;
import com.softwaremagico.kt.selenium.pages.LoginPage;
import com.softwaremagico.kt.selenium.pages.ParticipantPage;
import com.softwaremagico.kt.selenium.pages.TournamentPage;
import com.softwaremagico.kt.selenium.pages.UserPage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Browser regressions for projection and participant attribution after team reordering. */
public class ScoringPanelTest {
    private static final String URL = System.getProperty("selenium.base-url", "http://localhost:14200");
    private WebDriver driver;
    private WebDriver projector;
    private WebDriverClient client;
    private FightPage fights;
    private String tournament;
    private String club;
    private final Map<String, int[]> statistics = new LinkedHashMap<>();

    @BeforeMethod
    public void setUp() {
        statistics.clear();
        driver = ChromeDriverFactory.create(Boolean.parseBoolean(System.getProperty("selenium.headless", "true")));
        client = new WebDriverClient(driver);
        final LoginPage login = new LoginPage(client);
        login.open(URL);
        login.login(System.getProperty("selenium.username", "e2e-admin@test.local"), System.getProperty("selenium.password", "E2e-password-123"));
        fights = new FightPage(client);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (projector != null) {
            projector.quit();
            projector = null;
        }
        if (driver != null) {
            driver.quit();
        }
    }

    private void league(int teams, int size) {
        final String id = Long.toString(System.currentTimeMillis());
        club = "Panel Club " + id;
        tournament = "Panel League " + id;
        final ClubPage clubs = new ClubPage(client);
        clubs.open(URL);
        clubs.create(club, "Spain", "Madrid");
        final ParticipantPage participants = new ParticipantPage(client);
        participants.open(URL);
        final String[] names = new String[teams * size];
        for (int i = 0; i < names.length; i++) {
            names[i] = "Panel" + id + "Member" + i;
            participants.create(names[i], "Player", "P" + id + i, club);
        }
        final TournamentPage tournaments = new TournamentPage(client);
        tournaments.open(URL);
        tournaments.createLeague(tournament, size);
        tournaments.addCompetitors(names);
        tournaments.createTeams(teams, names.length);
        tournaments.openFights();
        fights.generateLeagueFights();
    }

    @Test
    public void projectorShowsLiveScoresWithoutEditingAndReturnsWithEscape() {
        league(2, 1);
        final String editor = "projector." + System.currentTimeMillis() + "@test.local";
        final UserPage users = new UserPage(client);
        users.open(URL);
        users.create(editor, "Projector-password-123");
        users.setEditorRole(editor);
        final TournamentPage control = new TournamentPage(client);
        control.open(URL);
        control.select(tournament);
        control.openFights();
        projector = ChromeDriverFactory.create(Boolean.parseBoolean(System.getProperty("selenium.headless", "true")));
        final WebDriverClient screen = new WebDriverClient(projector);
        final LoginPage login = new LoginPage(screen);
        login.open(URL);
        login.login(editor, "Projector-password-123");
        final TournamentPage tournaments = new TournamentPage(screen);
        tournaments.open(URL);
        tournaments.select(tournament);
        tournaments.openFights();
        screen.clickFirstVisible(By.cssSelector("button.expand-button"));
        screen.findVisible(By.cssSelector(".fights-container.project-mode"));
        Assert.assertTrue(screen.findAll(By.cssSelector(".filter-fights")).isEmpty());
        screen.findVisible(By.cssSelector("score .score-area")).click();
        Assert.assertTrue(screen.findAll(By.cssSelector(".mat-mdc-menu-panel")).isEmpty());
        fights.scoreFirstDuelFor("Team 1");
        new FightPage(screen).waitUntilContainsScore("M");
        fights.resolveFirstFightFor("Team 1");
        screen.findVisible(By.cssSelector(".projector-empty-state"));
        Assert.assertTrue(screen.findAll(By.cssSelector("fight")).isEmpty());
        new Actions(projector).sendKeys(Keys.ESCAPE).perform();
        screen.waitUntilInvisible(By.cssSelector(".fights-container.project-mode"));
        screen.findVisible(By.cssSelector(".filter-fights"));
        Assert.assertTrue(new FightPage(screen).containsScore("M"));
    }

    private WebElement duel(int fight, int index) {
        return driver.findElements(By.cssSelector("fight")).get(fight).findElements(By.cssSelector("duel:not(.substitute)")).get(index);
    }

    private String member(int fight, int index, int side) {
        return duel(fight, index).findElements(By.cssSelector("user-score > .user-score")).get(side).getAttribute("data-participant-name");
    }

    private void reorder(int fight, int source, int target, int side) {
        client.clickFirstVisible(By.xpath("//button[.//mat-icon[@svgIcon='member-order']]"));
        final String moved = member(fight, source, side);
        final WebElement handle = duel(fight, source).findElements(By.cssSelector("user-score")).get(side)
                .findElement(By.cssSelector(".drag-handle"));
        final WebElement destination = duel(fight, target).findElements(By.cssSelector("user-score")).get(side);
        new Actions(driver).moveToElement(handle).pause(java.time.Duration.ofMillis(200)).clickAndHold()
                .pause(java.time.Duration.ofMillis(200)).moveByOffset(0, 8).moveToElement(destination)
                .pause(java.time.Duration.ofMillis(400)).release().perform();
        client.getWebDriverWait().until(browser -> member(fight, target, side).equals(moved));
        client.clickFirstVisible(By.xpath("//button[.//mat-icon[@svgIcon='member-order-disable']]"));
        client.refresh();
        client.findVisible(By.cssSelector("fight"));
        Assert.assertEquals(member(fight, target, side), moved, "Member order must be persisted.");
    }

    private void finish(int fight, int index, String score) {
        final String winner = member(fight, index, 0);
        final String loser = member(fight, index, 1);
        for (int point = 0; point < 2; point++) {
            client.dismissSuccessNotifications();
            duel(fight, index).findElements(By.cssSelector("user-score")).get(0)
                    .findElements(By.cssSelector("score .score-area")).get(point).click();
            final WebElement option = client.findVisible(By.xpath("//div[contains(@class,'mat-mdc-menu-panel')]//button[normalize-space()='" + score + "']"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", option);
            client.waitUntilInvisible(By.cssSelector(".mat-mdc-menu-panel"));
            client.getWebDriverWait().until(browser -> browser.findElements(By.cssSelector("score [data-saving='true']")).isEmpty());
        }
        client.dismissSuccessNotifications();
        duel(fight, index).click();
        client.clickFirstVisible(By.xpath("//button[.//mat-icon[@svgIcon='check']]"));
        client.getWebDriverWait().until(browser -> duel(fight, index).getAttribute("class").contains("over"));
        statistics.computeIfAbsent(winner, key -> new int[4])[score.equals("Men") ? 0 : 1] += 2;
        statistics.computeIfAbsent(loser, key -> new int[4])[score.equals("Men") ? 2 : 3] += 2;
    }

    @Test
    public void repeatedMidLeagueMemberReorderingKeepsScoresAndParticipantStatistics() {
        league(3, 2);
        final Map<String, String> finished = new LinkedHashMap<>();
        for (int i = 0; i < 2; i++) { finish(0, i, "Men"); }
        for (int i = 0; i < 2; i++) { finished.put(member(0, i, 0), duel(0, i).getText()); }
        reorder(1, 0, 1, 0);
        reorder(1, 0, 1, 0);
        reorder(1, 0, 1, 0);
        reorder(1, 0, 1, 1);
        for (int i = 0; i < 2; i++) {
            Assert.assertEquals(duel(0, i).getText(), finished.get(member(0, i, 0)), "Completed fight must be immutable under later reorders.");
            finish(1, i, "Kote");
        }
        reorder(2, 0, 1, 0);
        for (int i = 0; i < 2; i++) { finish(2, i, "Men"); }
        client.refresh();
        client.findVisible(By.cssSelector("fight"));
        for (int fight = 0; fight < 3; fight++) {
            for (int index = 0; index < 2; index++) {
                Assert.assertTrue(duel(fight, index).getAttribute("class").contains("over"));
                final String tag = fight == 1 ? "K" : "M";
                final List<WebElement> sides = duel(fight, index).findElements(By.cssSelector("user-score"));
                Assert.assertEquals(sides.get(0).findElements(By.cssSelector(".point-value")).stream().map(WebElement::getText).toList(), List.of(tag, tag));
                Assert.assertTrue(sides.get(1).findElements(By.cssSelector(".point-value")).isEmpty());
            }
        }
        for (Map.Entry<String, int[]> entry : statistics.entrySet()) {
            final ParticipantPage participants = new ParticipantPage(client);
            participants.open(URL);
            participants.openStatistics(entry.getKey().split(", ")[1]);
            Assert.assertTrue(participants.containsStatisticValue(Integer.toString(entry.getValue()[0])), "MEN must belong to " + entry.getKey());
            final WebElement hits = driver.findElement(By.id("participant-men-count")).findElement(By.xpath("ancestor::table"));
            final List<WebElement> tables = driver.findElements(By.cssSelector(".box-statistics table"));
            Assert.assertEquals(hits.findElement(By.cssSelector("tr:nth-child(2) .right-column")).getText(), Integer.toString(entry.getValue()[1]), "KOTE attribution: " + entry.getKey());
            final WebElement received = tables.get(tables.indexOf(hits) + 2);
            Assert.assertEquals(received.findElement(By.cssSelector("tr:nth-child(1) .right-column")).getText(), Integer.toString(entry.getValue()[2]), "Received MEN attribution: " + entry.getKey());
            Assert.assertEquals(received.findElement(By.cssSelector("tr:nth-child(2) .right-column")).getText(), Integer.toString(entry.getValue()[3]), "Received KOTE attribution: " + entry.getKey());
        }
    }
}
