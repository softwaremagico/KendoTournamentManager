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
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Creates a club and participants through the same forms used by operators.
 */
public class ClubParticipantTest {
    private static final String BASE_URL = System.getProperty("selenium.base-url", "http://localhost:4200");

    private WebDriver driver;
    private ClubPage clubPage;
    private ParticipantPage participantPage;

    @BeforeClass
    public void setUp() {
        final String username = System.getProperty("selenium.username", "");
        final String password = System.getProperty("selenium.password", "");
        if (username.isBlank() || password.isBlank()) {
            throw new SkipException("Set selenium.username and selenium.password to run authenticated E2E tests.");
        }
        driver = ChromeDriverFactory.create(Boolean.parseBoolean(System.getProperty("selenium.headless", "true")));
        final WebDriverClient webDriverClient = new WebDriverClient(driver);
        new LoginPage(webDriverClient).open(BASE_URL);
        new LoginPage(webDriverClient).login(username, password);
        clubPage = new ClubPage(webDriverClient);
        participantPage = new ParticipantPage(webDriverClient);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void createsClubAndParticipantsFromForms() {
        final String identifier = Long.toString(System.currentTimeMillis());
        final String clubName = "Técnicos de Investigación Aeroterráquea " + identifier;

        clubPage.open(BASE_URL);
        clubPage.create(clubName, "Spain", "Valéncia");
        Assert.assertTrue(clubPage.contains(clubName), "The new club must appear in the club registry.");

        participantPage.open(BASE_URL);
        createAndAssertParticipant("Mengano", "López", "E2E" + identifier, clubName);
        createAndAssertParticipant("Fulano", "Férnandez", "E2F" + identifier, clubName);
    }

    private void createAndAssertParticipant(String name, String lastname, String idCard, String club) {
        participantPage.create(name, lastname, idCard, club);
        Assert.assertTrue(participantPage.contains(name), "The new participant must appear in the participant registry.");
    }
}
