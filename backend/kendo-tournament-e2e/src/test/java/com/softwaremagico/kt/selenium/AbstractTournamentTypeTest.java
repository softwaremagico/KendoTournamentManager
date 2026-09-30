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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/**
 * Shared authenticated setup for tournament type browser scenarios.
 */
public abstract class AbstractTournamentTypeTest {
    protected static final String BASE_URL = System.getProperty("selenium.base-url", "http://localhost:4200");
    protected static final String WINNING_TEAM = "Team 1";

    protected WebDriver driver;
    protected ClubPage clubPage;
    protected ParticipantPage participantPage;
    protected TournamentPage tournamentPage;
    protected FightPage fightPage;

    @BeforeClass
    public void setUp() {
        final String username = System.getProperty("selenium.username", "e2e-admin@test.local");
        final String password = System.getProperty("selenium.password", "E2e-password-123");
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

    protected String[] createIndividualTeams(String prefix, int teamCount) {
        final String identifier = Long.toString(System.currentTimeMillis());
        final String clubName = prefix + " Club " + identifier;
        final String[] competitors = new String[teamCount];
        clubPage.open(BASE_URL);
        clubPage.create(clubName, "Spain", "Valéncia");
        participantPage.open(BASE_URL);
        for (int index = 0; index < teamCount; index++) {
            competitors[index] = prefix + identifier + index;
            participantPage.create(competitors[index], "Player", prefix.substring(0, 1) + identifier + index, clubName);
        }
        return competitors;
    }

    protected void createTournamentWithIndividualTeams(String prefix, String type, String[] competitors) {
        final String tournamentName = prefix + " Tournament " + System.currentTimeMillis();
        tournamentPage.open(BASE_URL);
        tournamentPage.createTournament(tournamentName, type, 1);
        tournamentPage.addCompetitors(competitors);
        tournamentPage.createTeams(competitors.length, competitors.length);
        tournamentPage.openFights();
    }
}
