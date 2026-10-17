package com.softwaremagico.kt.selenium;

/*-
 * #%L
 * Kendo Tournament Manager (End-to-End Tests)
 * %%
 * Copyright (C) 2021 - 2026 Softwaremagico
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
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
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Opens the personal QR destination in a separate, initially unauthenticated browser.
 */
public class ParticipantQrAccessTest {
    private static final String BASE_URL = System.getProperty("selenium.base-url", "http://localhost:14200");
    private WebDriver administrator;
    private WebDriver participant;

    @BeforeClass
    public void setUp() {
        administrator = ChromeDriverFactory.create(Boolean.parseBoolean(System.getProperty("selenium.headless", "true")));
        final LoginPage login = new LoginPage(new WebDriverClient(administrator));
        login.open(BASE_URL);
        login.login(System.getProperty("selenium.username", "e2e-admin@test.local"),
                System.getProperty("selenium.password", "E2e-password-123"));
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (participant != null) {
            participant.quit();
        }
        if (administrator != null) {
            administrator.quit();
        }
    }

    @Test
    public void personalQrAuthenticatesParticipantAndShowsOwnPersistedStatistics() {
        final String identifier = Long.toString(System.currentTimeMillis());
        final String winner = "QrWinner" + identifier;
        final String opponent = "QrOpponent" + identifier;
        final String clubName = "Participant QR Club " + identifier;
        final WebDriverClient adminClient = new WebDriverClient(administrator);
        final ClubPage clubs = new ClubPage(adminClient);
        clubs.open(BASE_URL);
        clubs.create(clubName, "Spain", "Madrid");
        final ParticipantPage participants = new ParticipantPage(adminClient);
        participants.open(BASE_URL);
        participants.create(winner, "Player", "PQW" + identifier, clubName);
        participants.create(opponent, "Player", "PQO" + identifier, clubName);
        final TournamentPage tournaments = new TournamentPage(adminClient);
        tournaments.open(BASE_URL);
        tournaments.createLeague("Participant QR Tournament " + identifier, 1);
        tournaments.addCompetitors(winner, opponent);
        tournaments.createTeams(2, 2);
        tournaments.openFights();
        final FightPage fights = new FightPage(adminClient);
        fights.generateLeagueFights();
        fights.resolveFirstFightFor(winner);
        participants.open(BASE_URL);
        final String link = participants.getQrLink(winner);
        Assert.assertTrue(link.contains("/#/participants/statistics?participantId="));
        Assert.assertTrue(link.contains("temporalToken="), "The personal QR must include its temporary access token.");

        participant = ChromeDriverFactory.create(Boolean.parseBoolean(System.getProperty("selenium.headless", "true")));
        final WebDriverClient participantClient = new WebDriverClient(participant);
        participantClient.open(link);
        participantClient.getWebDriverWait().until(browser -> browser.findElements(By.cssSelector(".statistics-view .player .name"))
                .stream().anyMatch(element -> element.getText().equalsIgnoreCase(winner + " Player")));
        Assert.assertTrue(new ParticipantPage(participantClient).containsStatisticValue("2"),
                "The QR session must show the two persisted MEN scored by this participant.");
        final JavascriptExecutor storage = (JavascriptExecutor) participant;
        Assert.assertEquals(storage.executeScript("return localStorage.getItem('account');"), "participant");
        Assert.assertNotNull(storage.executeScript("return localStorage.getItem('.jwt');"));
        Assert.assertTrue(participantClient.findAll(By.id("login")).isEmpty(), "Personal QR access must not require administrator credentials.");

        participantClient.refresh();
        participantClient.getWebDriverWait().until(browser -> browser.findElements(By.cssSelector(".statistics-view .player .name"))
                .stream().anyMatch(element -> element.getText().equalsIgnoreCase(winner + " Player")));
        Assert.assertTrue(new ParticipantPage(participantClient).containsStatisticValue("2"),
                "Reloading the personal QR view must retain access to the same participant statistics.");
    }
}
