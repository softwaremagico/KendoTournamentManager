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
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Ensures tournament guest QR links open a read-only session that receives score updates.
 */
public class TournamentGuestQrTest {
    private static final String BASE_URL = System.getProperty("selenium.base-url", "http://localhost:4200");
    private static final String WINNING_TEAM = "Team 1";

    private WebDriver administratorDriver;
    private WebDriver guestDriver;

    @BeforeClass
    public void setUp() {
        final String username = System.getProperty("selenium.username", "");
        final String password = System.getProperty("selenium.password", "");
        if (username.isBlank() || password.isBlank()) {
            throw new SkipException("Set selenium.username and selenium.password to run QR E2E tests.");
        }
        administratorDriver = ChromeDriverFactory.create(Boolean.parseBoolean(System.getProperty("selenium.headless", "true")));
        final LoginPage loginPage = new LoginPage(new WebDriverClient(administratorDriver));
        loginPage.open(BASE_URL);
        loginPage.login(username, password);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        quit(administratorDriver);
        quit(guestDriver);
    }

    @Test
    public void guestQrLinkShowsLiveTournamentScoreUpdates() {
        final String identifier = Long.toString(System.currentTimeMillis());
        final String clubName = "QR Club " + identifier;
        final String firstParticipant = "QrFirst" + identifier;
        final String secondParticipant = "QrSecond" + identifier;
        final String tournamentName = "QR Tournament " + identifier;
        final WebDriverClient administratorClient = new WebDriverClient(administratorDriver);

        final ClubPage clubPage = new ClubPage(administratorClient);
        clubPage.open(BASE_URL);
        clubPage.create(clubName, "Spain", "Valéncia");

        final ParticipantPage participantPage = new ParticipantPage(administratorClient);
        participantPage.open(BASE_URL);
        participantPage.create(firstParticipant, "Player", "QRA" + identifier, clubName);
        participantPage.create(secondParticipant, "Player", "QRB" + identifier, clubName);

        final TournamentPage tournamentPage = new TournamentPage(administratorClient);
        tournamentPage.open(BASE_URL);
        tournamentPage.createLeague(tournamentName, 1);
        tournamentPage.addCompetitors(firstParticipant, secondParticipant);
        tournamentPage.createTeams(2, 2);
        final String guestLink = tournamentPage.openGuestQrLink(tournamentName);
        Assert.assertTrue(guestLink.contains("user=guest"), "The QR link must identify the guest session.");
        tournamentPage.openFights();

        final FightPage administratorFightPage = new FightPage(administratorClient);
        administratorFightPage.generateLeagueFights();

        guestDriver = ChromeDriverFactory.create(Boolean.parseBoolean(System.getProperty("selenium.headless", "true")));
        final FightPage guestFightPage = new FightPage(new WebDriverClient(guestDriver));
        guestDriver.get(guestLink);
        new WebDriverClient(guestDriver).findVisible(org.openqa.selenium.By.cssSelector("fight"));

        administratorFightPage.scoreFirstDuelFor(WINNING_TEAM);
        guestFightPage.waitUntilContainsScore("M");
        Assert.assertTrue(guestFightPage.containsScore("M"), "The guest view must receive the administrator score update.");
    }

    private void quit(WebDriver driver) {
        if (driver != null) {
            driver.quit();
        }
    }
}
