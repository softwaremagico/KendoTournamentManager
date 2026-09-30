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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;

/**
 * Covers the club and participant CSV import actions exposed by their popups.
 */
public class CsvImportTest {
    private static final String BASE_URL = System.getProperty("selenium.base-url", "http://localhost:14200");
    private static final String USERNAME = System.getProperty("selenium.username", "e2e-admin@test.local");
    private static final String PASSWORD = System.getProperty("selenium.password", "E2e-password-123");
    private static final String CLUB_NAME = "E2E CSV Kendo Club";
    private static final String VALID_PARTICIPANT = "CsvParticipantOne";
    private static final String INVALID_PARTICIPANT = "MissingName";

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
    public void importsClubAndParticipantsAndRejectsInvalidParticipant() throws URISyntaxException {
        final WebDriverClient client = new WebDriverClient(driver);
        final ClubPage clubPage = new ClubPage(client);
        clubPage.open(BASE_URL);
        clubPage.importCsv(resourcePath("csv/e2e-clubs.csv"), CLUB_NAME);
        Assert.assertTrue(clubPage.contains(CLUB_NAME));

        final ParticipantPage participantPage = new ParticipantPage(client);
        participantPage.open(BASE_URL);
        participantPage.importCsv(resourcePath("csv/e2e-participants.csv"), VALID_PARTICIPANT);
        Assert.assertTrue(participantPage.contains(VALID_PARTICIPANT));

        participantPage.importCsv(resourcePath("csv/e2e-invalid-participants.csv"), VALID_PARTICIPANT);
        Assert.assertFalse(participantPage.contains(INVALID_PARTICIPANT), "Invalid CSV rows must not create participants.");
    }

    private String resourcePath(String resource) throws URISyntaxException {
        return Path.of(getClass().getClassLoader().getResource(resource).toURI()).toString();
    }
}
