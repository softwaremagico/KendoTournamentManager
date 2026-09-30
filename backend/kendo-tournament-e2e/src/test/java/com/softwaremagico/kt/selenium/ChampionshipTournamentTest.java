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
 * Browser equivalent of the eight-team tree tournament progression test.
 */
public class ChampionshipTournamentTest {
    private static final int TEAM_COUNT = 8;
    private static final int TEAM_SIZE = 3;
    private static final int PARTICIPANT_COUNT = TEAM_COUNT * TEAM_SIZE;
    private static final int EXPECTED_FIGHT_COUNT = TEAM_COUNT - 1;
    private static final String WINNING_TEAM = "Team 1";
    private static final String BASE_URL = System.getProperty("selenium.base-url", "http://localhost:4200");

    private WebDriver driver;
    private ClubPage clubPage;
    private ParticipantPage participantPage;
    private TournamentPage tournamentPage;
    private FightPage fightPage;

    @BeforeClass
    public void setUp() {
        final String username = System.getProperty("selenium.username", "");
        final String password = System.getProperty("selenium.password", "");
        if (username.isBlank() || password.isBlank()) {
            throw new SkipException("Set selenium.username and selenium.password to run authenticated E2E tests.");
        }
        driver = ChromeDriverFactory.create(Boolean.parseBoolean(System.getProperty("selenium.headless", "true")));
        final WebDriverClient webDriverClient = new WebDriverClient(driver);
        final LoginPage loginPage = new LoginPage(webDriverClient);
        loginPage.open(BASE_URL);
        loginPage.login(username, password);
        clubPage = new ClubPage(webDriverClient);
        participantPage = new ParticipantPage(webDriverClient);
        tournamentPage = new TournamentPage(webDriverClient);
        fightPage = new FightPage(webDriverClient);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void resolvesEightTeamsChampionshipAndRanksWinner() {
        final String identifier = Long.toString(System.currentTimeMillis());
        final String clubName = "E2E Championship Club " + identifier;
        final String tournamentName = "E2E Championship " + identifier;
        final String[] competitors = competitors(identifier);

        clubPage.open(BASE_URL);
        clubPage.create(clubName, "Spain", "Valéncia");
        participantPage.open(BASE_URL);
        for (int index = 0; index < PARTICIPANT_COUNT; index++) {
            participantPage.create(competitors[index], "Player", "C" + identifier + index, clubName);
        }

        tournamentPage.open(BASE_URL);
        tournamentPage.createChampionship(tournamentName, TEAM_SIZE);
        tournamentPage.addCompetitors(competitors);
        tournamentPage.createTeams(TEAM_COUNT, PARTICIPANT_COUNT);
        tournamentPage.openFights();

        fightPage.generateChampionshipFights();
        fightPage.resolveChampionshipFor(WINNING_TEAM);
        Assert.assertTrue(fightPage.hasReachedFinal(), "The championship must generate the final round.");
        Assert.assertEquals(fightPage.getFightCount(), EXPECTED_FIGHT_COUNT);
        Assert.assertEquals(fightPage.getWinner(), WINNING_TEAM);
    }

    private String[] competitors(String identifier) {
        final String[] competitors = new String[PARTICIPANT_COUNT];
        for (int index = 0; index < PARTICIPANT_COUNT; index++) {
            competitors[index] = "Champion" + identifier + index;
        }
        return competitors;
    }
}
