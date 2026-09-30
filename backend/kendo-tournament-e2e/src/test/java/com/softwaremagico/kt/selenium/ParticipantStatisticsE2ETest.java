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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Verifies that recorded scores feed the participant statistics dashboard.
 */
public class ParticipantStatisticsE2ETest {
    private static final String BASE_URL = System.getProperty("selenium.base-url", "http://localhost:14200");
    private static final String USERNAME = System.getProperty("selenium.username", "e2e-admin@test.local");
    private static final String PASSWORD = System.getProperty("selenium.password", "E2e-password-123");
    private static final String WINNING_TEAM = "Team 1";

    private WebDriver driver;

    @BeforeClass
    public void setUp() {
        driver = ChromeDriverFactory.create(Boolean.parseBoolean(System.getProperty("selenium.headless", "true")));
        final LoginPage loginPage = new LoginPage(new WebDriverClient(driver));
        loginPage.open(BASE_URL);
        loginPage.login(USERNAME, PASSWORD);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void scoredMenAppearsInWinnerParticipantStatistics() {
        final String identifier = Long.toString(System.currentTimeMillis());
        final String clubName = "Statistics Club " + identifier;
        final String winner = "StatisticsWinner" + identifier;
        final String opponent = "StatisticsOpponent" + identifier;
        final String tournamentName = "Statistics Tournament " + identifier;
        final WebDriverClient client = new WebDriverClient(driver);

        final ClubPage clubPage = new ClubPage(client);
        clubPage.open(BASE_URL);
        clubPage.create(clubName, "Spain", "Valéncia");

        final ParticipantPage participantPage = new ParticipantPage(client);
        participantPage.open(BASE_URL);
        participantPage.create(winner, "Player", "SWA" + identifier, clubName);
        participantPage.create(opponent, "Player", "SOA" + identifier, clubName);

        final TournamentPage tournamentPage = new TournamentPage(client);
        tournamentPage.open(BASE_URL);
        tournamentPage.createLeague(tournamentName, 1);
        tournamentPage.addCompetitors(winner, opponent);
        tournamentPage.createTeams(2, 2);
        tournamentPage.openFights();

        final FightPage fightPage = new FightPage(client);
        fightPage.generateLeagueFights();
        fightPage.scoreFirstDuelFor(WINNING_TEAM);
        fightPage.resolveFirstFightFor(WINNING_TEAM);

        participantPage.open(BASE_URL);
        participantPage.openStatistics(winner);
        Assert.assertTrue(participantPage.containsStatisticValue("1"),
                "The winner participant statistics must include the scored MEN hit.");
    }
}
