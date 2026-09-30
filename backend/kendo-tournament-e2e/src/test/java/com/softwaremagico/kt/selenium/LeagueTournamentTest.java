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
 * Browser equivalent of the simple team league fixture in {@code SimpleLeagueTest}.
 */
public class LeagueTournamentTest {
    private static final String BASE_URL = System.getProperty("selenium.base-url", "http://localhost:4200");

    private WebDriver driver;
    private ClubPage clubPage;
    private ParticipantPage participantPage;
    private TournamentPage tournamentPage;
    private FightPage fightPage;

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

    @Test
    public void resolvesThreeMemberTeamLeagueAndRanksWinner() {
        final String identifier = Long.toString(System.currentTimeMillis());
        final String clubName = "E2E League Club " + identifier;
        final String tournamentName = "E2E League " + identifier;
        final String[] competitors = {"LeagueA" + identifier, "LeagueB" + identifier, "LeagueC" + identifier,
            "LeagueD" + identifier, "LeagueE" + identifier, "LeagueF" + identifier};

        clubPage.open(BASE_URL);
        clubPage.create(clubName, "Spain", "Valéncia");

        participantPage.open(BASE_URL);
        for (int index = 0; index < competitors.length; index++) {
            participantPage.create(competitors[index], "Player", "L" + identifier + index, clubName);
        }

        tournamentPage.open(BASE_URL);
        tournamentPage.createLeague(tournamentName);
        tournamentPage.addCompetitors(competitors);
        tournamentPage.createTwoTeamsOfThree();
        tournamentPage.openFights();

        fightPage.generateLeagueFights();
        fightPage.resolveAllFightsFor("Team 1");
        Assert.assertEquals(fightPage.getWinner(), "Team 1");
    }
}
