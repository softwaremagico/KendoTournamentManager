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
import com.softwaremagico.kt.selenium.pages.UserPage;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Validates that a fight score changed by one authenticated user reaches another user's open fight view via STOMP.
 */
public class WebSocketScoreUpdateTest {
    private static final String BASE_URL = System.getProperty("selenium.base-url", "http://localhost:14200");
    private static final String ADMIN_USERNAME = System.getProperty("selenium.username", "e2e-admin@test.local");
    private static final String ADMIN_PASSWORD = System.getProperty("selenium.password", "E2e-password-123");
    private static final String EDITOR_PASSWORD = "E2e-editor-password-123";
    private static final String WINNING_TEAM = "Team 1";

    private WebDriver administratorDriver;
    private WebDriver editorDriver;

    @BeforeClass
    public void setUp() {
        administratorDriver = ChromeDriverFactory.create(Boolean.parseBoolean(System.getProperty("selenium.headless", "true")));
        final LoginPage loginPage = new LoginPage(new WebDriverClient(administratorDriver));
        loginPage.open(BASE_URL);
        loginPage.login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        quit(administratorDriver);
        quit(editorDriver);
    }

    @Test
    public void scoreUpdateIsDeliveredToSecondAuthenticatedSession() {
        final String identifier = Long.toString(System.currentTimeMillis());
        final String clubName = "WebSocket Club " + identifier;
        final String firstParticipant = "WsFirst" + identifier;
        final String secondParticipant = "WsSecond" + identifier;
        final String tournamentName = "WebSocket Tournament " + identifier;
        final String editorUsername = "e2e.websocket.editor." + identifier + "@test.local";
        final WebDriverClient administratorClient = new WebDriverClient(administratorDriver);

        final UserPage userPage = new UserPage(administratorClient);
        userPage.open(BASE_URL);
        userPage.create(editorUsername, EDITOR_PASSWORD);
        userPage.setEditorRole(editorUsername);

        final ClubPage clubPage = new ClubPage(administratorClient);
        clubPage.open(BASE_URL);
        clubPage.create(clubName, "Spain", "Valéncia");

        final ParticipantPage participantPage = new ParticipantPage(administratorClient);
        participantPage.open(BASE_URL);
        participantPage.create(firstParticipant, "Player", "WSA" + identifier, clubName);
        participantPage.create(secondParticipant, "Player", "WSB" + identifier, clubName);

        final TournamentPage tournamentPage = new TournamentPage(administratorClient);
        tournamentPage.open(BASE_URL);
        tournamentPage.createLeague(tournamentName, 1);
        tournamentPage.addCompetitors(firstParticipant, secondParticipant);
        tournamentPage.createTeams(2, 2);
        tournamentPage.openFights();

        final FightPage administratorFightPage = new FightPage(administratorClient);
        administratorFightPage.generateLeagueFights();

        editorDriver = ChromeDriverFactory.create(Boolean.parseBoolean(System.getProperty("selenium.headless", "true")));
        final WebDriverClient editorClient = new WebDriverClient(editorDriver);
        final LoginPage editorLogin = new LoginPage(editorClient);
        editorLogin.open(BASE_URL);
        editorLogin.login(editorUsername, EDITOR_PASSWORD);
        final TournamentPage editorTournamentPage = new TournamentPage(editorClient);
        editorTournamentPage.open(BASE_URL);
        editorTournamentPage.openFights();
        final FightPage editorFightPage = new FightPage(editorClient);
        editorFightPage.waitForFightCount(1);

        administratorFightPage.scoreFirstDuelFor(WINNING_TEAM);
        editorFightPage.waitUntilContainsScore("M");
        Assert.assertTrue(editorFightPage.containsScore("M"), "The second authenticated session must receive the score update by WebSocket.");
    }

    private void quit(WebDriver driver) {
        if (driver != null) {
            driver.quit();
        }
    }
}
