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
import com.softwaremagico.kt.selenium.pages.TenantPage;
import com.softwaremagico.kt.selenium.pages.TournamentPage;
import com.softwaremagico.kt.selenium.pages.FightPage;
import com.softwaremagico.kt.selenium.pages.UserPage;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Validates tenant-scoped registries with two independently authenticated administrators.
 */
public class TenantIsolationTest {
    private static final String BASE_URL = System.getProperty("selenium.base-url", "http://localhost:4200");
    private static final String PASSWORD = "Tenant-admin-password-123";
    private static final String WINNING_TEAM = "Team 1";

    private WebDriver platformDriver;
    private WebDriver tenantADriver;
    private WebDriver tenantBDriver;
    private String tenantAClub;
    private String tenantBClub;
    private String tenantAParticipant;
    private String tenantBParticipant;
    private String tenantATournament;
    private String tenantBTournament;
    private String tenantAUser;
    private String tenantBUser;

    @BeforeClass
    public void setUp() {
        final String username = System.getProperty("selenium.username", "e2e-admin@test.local");
        final String password = System.getProperty("selenium.password", "E2e-password-123");
        platformDriver = ChromeDriverFactory.create(Boolean.parseBoolean(System.getProperty("selenium.headless", "true")));
        final WebDriverClient platformClient = new WebDriverClient(platformDriver);
        final LoginPage platformLogin = new LoginPage(platformClient);
        platformLogin.open(BASE_URL);
        platformLogin.login(username, password);

        final String identifier = Long.toString(System.currentTimeMillis());
        final String tenantAName = "E2E Tenant A " + identifier;
        final String tenantBName = "E2E Tenant B " + identifier;
        final String tenantAAdmin = "e2e.tenant.a." + identifier + "@test.local";
        final String tenantBAdmin = "e2e.tenant.b." + identifier + "@test.local";
        tenantAClub = "Tenant A Club " + identifier;
        tenantBClub = "Tenant B Club " + identifier;
        tenantAParticipant = "TenantAPlayer" + identifier;
        tenantBParticipant = "TenantBPlayer" + identifier;
        tenantATournament = "Tenant A Tournament " + identifier;
        tenantBTournament = "Tenant B Tournament " + identifier;
        tenantAUser = "e2e.tenant.a.user." + identifier + "@test.local";
        tenantBUser = "e2e.tenant.b.user." + identifier + "@test.local";

        final TenantPage tenantPage = new TenantPage(platformClient);
        tenantPage.open(BASE_URL);
        tenantPage.create(tenantAName, tenantAAdmin, PASSWORD);
        tenantPage.create(tenantBName, tenantBAdmin, PASSWORD);

        tenantADriver = login(tenantAAdmin);
        tenantBDriver = login(tenantBAdmin);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        quit(platformDriver);
        quit(tenantADriver);
        quit(tenantBDriver);
    }

    @Test
    public void tenantAdministratorsOnlySeeTheirOwnDataAndTournaments() {
        createTenantData(tenantADriver, tenantAClub, tenantAParticipant, tenantATournament, tenantAUser, "TA");
        createTenantData(tenantBDriver, tenantBClub, tenantBParticipant, tenantBTournament, tenantBUser, "TB");

        assertTenantIsolation(tenantADriver, tenantAClub, tenantAParticipant, tenantATournament,
                tenantBClub, tenantBParticipant, tenantBTournament);
        assertTenantIsolation(tenantBDriver, tenantBClub, tenantBParticipant, tenantBTournament,
                tenantAClub, tenantAParticipant, tenantATournament);
    }

    private WebDriver login(String username) {
        final WebDriver driver = ChromeDriverFactory.create(Boolean.parseBoolean(System.getProperty("selenium.headless", "true")));
        final LoginPage loginPage = new LoginPage(new WebDriverClient(driver));
        loginPage.open(BASE_URL);
        loginPage.login(username, PASSWORD);
        return driver;
    }

    private void createTenantData(WebDriver driver, String clubName, String participantName, String tournamentName,
                                  String userName, String idPrefix) {
        final WebDriverClient client = new WebDriverClient(driver);
        final ClubPage clubPage = new ClubPage(client);
        clubPage.open(BASE_URL);
        clubPage.create(clubName, "Spain", "Valéncia");

        final ParticipantPage participantPage = new ParticipantPage(client);
        participantPage.open(BASE_URL);
        participantPage.create(participantName, "Player", idPrefix + Long.toString(System.currentTimeMillis()), clubName);
        participantPage.create(participantName + "B", "Player", idPrefix + "B" + Long.toString(System.currentTimeMillis()), clubName);

        final TournamentPage tournamentPage = new TournamentPage(client);
        tournamentPage.open(BASE_URL);
        tournamentPage.createLeague(tournamentName, 1);
        tournamentPage.addCompetitors(participantName, participantName + "B");
        tournamentPage.createTeams(2, 2);
        tournamentPage.openFights();
        final FightPage fightPage = new FightPage(client);
        fightPage.generateLeagueFights();
        fightPage.resolveAllFightsFor(WINNING_TEAM);
        Assert.assertEquals(fightPage.getWinner(), WINNING_TEAM, "Each tenant must retain its own resolved ranking.");

        final UserPage userPage = new UserPage(client);
        userPage.open(BASE_URL);
        userPage.create(userName, PASSWORD);
    }

    private void assertTenantIsolation(WebDriver driver, String ownClub, String ownParticipant, String ownTournament,
                                       String foreignClub, String foreignParticipant, String foreignTournament) {
        final WebDriverClient client = new WebDriverClient(driver);
        final ClubPage clubPage = new ClubPage(client);
        clubPage.open(BASE_URL);
        Assert.assertTrue(clubPage.contains(ownClub));
        Assert.assertFalse(clubPage.contains(foreignClub), "A tenant must not see another tenant's club.");

        final ParticipantPage participantPage = new ParticipantPage(client);
        participantPage.open(BASE_URL);
        Assert.assertTrue(participantPage.contains(ownParticipant));
        Assert.assertFalse(participantPage.contains(foreignParticipant), "A tenant must not see another tenant's participant.");

        final TournamentPage tournamentPage = new TournamentPage(client);
        tournamentPage.open(BASE_URL);
        Assert.assertTrue(tournamentPage.contains(ownTournament));
        Assert.assertFalse(tournamentPage.contains(foreignTournament), "A tenant must not see another tenant's tournament.");
        tournamentPage.openStatistics(ownTournament);
        Assert.assertEquals(tournamentPage.getStatisticsTournamentName(), ownTournament,
                "A tenant must only access statistics for its own tournament.");
    }

    private void quit(WebDriver driver) {
        if (driver != null) {
            driver.quit();
        }
    }
}
