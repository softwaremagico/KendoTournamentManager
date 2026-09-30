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
import com.softwaremagico.kt.selenium.pages.LoginPage;
import com.softwaremagico.kt.selenium.pages.ParticipantPage;
import com.softwaremagico.kt.selenium.pages.TournamentPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Ensures locking a tournament revokes the guest access advertised by its QR code.
 */
public class LockedTournamentGuestAccessTest {
    private static final String BASE_URL = System.getProperty("selenium.base-url", "http://localhost:14200");
    private static final String USERNAME = System.getProperty("selenium.username", "e2e-admin@test.local");
    private static final String PASSWORD = System.getProperty("selenium.password", "E2e-password-123");

    private WebDriver administratorDriver;
    private WebDriver guestDriver;

    @BeforeClass
    public void setUp() {
        administratorDriver = ChromeDriverFactory.create(Boolean.parseBoolean(System.getProperty("selenium.headless", "true")));
        final LoginPage loginPage = new LoginPage(new WebDriverClient(administratorDriver));
        loginPage.open(BASE_URL);
        loginPage.login(USERNAME, PASSWORD);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        quit(administratorDriver);
        quit(guestDriver);
    }

    @Test
    public void lockedTournamentRejectsGuestQrAccess() {
        final String identifier = Long.toString(System.currentTimeMillis());
        final String clubName = "Locked QR Club " + identifier;
        final String firstParticipant = "LockedQrFirst" + identifier;
        final String secondParticipant = "LockedQrSecond" + identifier;
        final String tournamentName = "Locked QR Tournament " + identifier;
        final WebDriverClient administratorClient = new WebDriverClient(administratorDriver);

        final ClubPage clubPage = new ClubPage(administratorClient);
        clubPage.open(BASE_URL);
        clubPage.create(clubName, "Spain", "Valéncia");

        final ParticipantPage participantPage = new ParticipantPage(administratorClient);
        participantPage.open(BASE_URL);
        participantPage.create(firstParticipant, "Player", "LQA" + identifier, clubName);
        participantPage.create(secondParticipant, "Player", "LQB" + identifier, clubName);

        final TournamentPage tournamentPage = new TournamentPage(administratorClient);
        tournamentPage.open(BASE_URL);
        tournamentPage.createLeague(tournamentName, 1);
        tournamentPage.addCompetitors(firstParticipant, secondParticipant);
        tournamentPage.createTeams(2, 2);
        final String guestLink = tournamentPage.openGuestQrLink(tournamentName);
        tournamentPage.lock(tournamentName);

        guestDriver = ChromeDriverFactory.create(Boolean.parseBoolean(System.getProperty("selenium.headless", "true")));
        guestDriver.get(guestLink);
        final WebDriverClient guestClient = new WebDriverClient(guestDriver);
        guestClient.findVisible(By.id("login"));
        Assert.assertTrue(guestClient.isVisible(By.id("login")), "A locked tournament must redirect QR guests to login.");
    }

    private void quit(WebDriver driver) {
        if (driver != null) {
            driver.quit();
        }
    }
}
