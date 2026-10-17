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

import com.softwaremagico.kt.selenium.pages.LoginPage;
import com.softwaremagico.kt.selenium.pages.TournamentPage;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Covers score-rule persistence for the standard and custom ranking models.
 */
public class TournamentScoreRulesTest {
    private static final String BASE_URL = System.getProperty("selenium.base-url", "http://localhost:14200");
    private static final String USERNAME = System.getProperty("selenium.username", "e2e-admin@test.local");
    private static final String PASSWORD = System.getProperty("selenium.password", "E2e-password-123");

    private WebDriver driver;
    private TournamentPage tournamentPage;

    @BeforeClass
    public void setUp() {
        driver = ChromeDriverFactory.create(Boolean.parseBoolean(System.getProperty("selenium.headless", "true")));
        final WebDriverClient client = new WebDriverClient(driver);
        final LoginPage loginPage = new LoginPage(client);
        loginPage.open(BASE_URL);
        loginPage.login(USERNAME, PASSWORD);
        tournamentPage = new TournamentPage(client);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void persistsEuropeanInternationalAndCustomScoreRules() {
        final String identifier = Long.toString(System.currentTimeMillis());
        assertScoreRule("European Score " + identifier, "European");
        assertScoreRule("International Score " + identifier, "International");

        final String customTournament = "Custom Score " + identifier;
        tournamentPage.open(BASE_URL);
        tournamentPage.createLeagueWithScoreRule(customTournament, "Custom");
        Assert.assertEquals(tournamentPage.getScoreRule(customTournament), "Custom");
        tournamentPage.openCustomScoreRules();
    }

    private void assertScoreRule(String tournamentName, String scoreRule) {
        tournamentPage.open(BASE_URL);
        tournamentPage.createLeagueWithScoreRule(tournamentName, scoreRule);
        Assert.assertEquals(tournamentPage.getScoreRule(tournamentName), scoreRule);
    }
}
