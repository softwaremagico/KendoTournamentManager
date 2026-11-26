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
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.softwaremagico.kt.selenium.components.Dropdown;
import com.softwaremagico.kt.selenium.components.InputField;
import com.softwaremagico.kt.selenium.components.Popup;
import com.softwaremagico.kt.selenium.components.Table;
import com.softwaremagico.kt.selenium.pages.ClubPage;
import com.softwaremagico.kt.selenium.pages.FightPage;
import com.softwaremagico.kt.selenium.pages.LoginPage;
import com.softwaremagico.kt.selenium.pages.ParticipantPage;
import com.softwaremagico.kt.selenium.pages.TournamentPage;
import com.softwaremagico.kt.selenium.pages.UserPage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Rejected operations must preserve persisted state and allow a valid subsequent operation.
 */
public class NegativeWorkflowsTest {
    private static final String BASE_URL = System.getProperty("selenium.base-url", "http://localhost:14200");
    private static final String USERNAME = System.getProperty("selenium.username", "e2e-admin@test.local");
    private static final String PASSWORD = System.getProperty("selenium.password", "E2e-password-123");
    private WebDriver driver;
    private WebDriverClient client;
    private LoginPage login;

    @BeforeMethod
    public void setUp() {
        driver = ChromeDriverFactory.create(Boolean.parseBoolean(System.getProperty("selenium.headless", "true")));
        client = new WebDriverClient(driver);
        login = new LoginPage(client);
        login.open(BASE_URL);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void wrongPasswordDoesNotCreateSessionAndValidRetrySucceeds() {
        login.submitCredentials(USERNAME, "Definitely-wrong-password-123");
        client.findVisible(By.cssSelector("#biit-notification.error"));
        Assert.assertTrue(login.isDisplayed(), "Rejected credentials must leave the login form available.");
        Assert.assertNull(((JavascriptExecutor) driver).executeScript("return localStorage.getItem('.jwt');"));
        login.login(USERNAME, PASSWORD);
        client.findVisible(By.id("tournaments-table"));
    }

    @Test
    public void anonymousDeepLinkRequiresLoginAndDoesNotExposeRegistry() {
        client.open(BASE_URL + "/#/registry/participants");
        client.findVisible(By.id("login"));
        Assert.assertTrue(client.findAll(By.id("participants-table")).isEmpty());
        Assert.assertTrue(client.findAll(By.id("participant-button-save")).isEmpty());
        login.login(USERNAME, PASSWORD);
        new ParticipantPage(client).open(BASE_URL);
        client.findVisible(By.id("participants-table"));
    }

    @Test
    public void missingParticipantLastnameIsRejectedAndCorrectedFormPersistsExactlyOnce() {
        login.login(USERNAME, PASSWORD);
        final String name = "Validation" + System.currentTimeMillis();
        final String club = createClub(name);
        final ParticipantPage participants = new ParticipantPage(client);
        participants.open(BASE_URL);
        final Table table = new Table(client, "participants-table");
        table.clickAction("button-plus");
        new InputField(client, "participant-name").setValue(name);
        new InputField(client, "participant-id-card").setValue(name);
        new Dropdown(client, "participant-club").select(club);
        client.clickWizardryButton(By.id("participant-button-save"));
        client.findVisible(By.cssSelector("#participant-lastname input.error"));
        Assert.assertTrue(client.isVisible(By.id("participant-popup")));
        new Popup(client, "participant-popup").close();
        participants.open(BASE_URL);
        client.refresh();
        client.findVisible(By.id("participants-table"));
        Assert.assertFalse(participants.contains(name), "An invalid form must not create a participant.");
        participants.create(name, "Corrected", name, club);
        participants.open(BASE_URL);
        client.refresh();
        client.findVisible(By.id("participants-table"));
        Assert.assertTrue(participants.contains(name));
        Assert.assertEquals(table.getRowCount(), 1, "The corrected form must create exactly one persisted row.");
    }

    @Test
    public void cancellingParticipantEditPreservesOriginalValuesAfterReload() {
        login.login(USERNAME, PASSWORD);
        final String name = "Cancel" + System.currentTimeMillis();
        final String club = createClub(name);
        final ParticipantPage participants = new ParticipantPage(client);
        participants.open(BASE_URL);
        participants.create(name, "Original", name, club);
        final Table table = new Table(client, "participants-table");
        table.selectRowContaining(name);
        table.clickAction("button-edit");
        new InputField(client, "participant-lastname").setValue("Unsaved");
        new Popup(client, "participant-popup").close();
        participants.open(BASE_URL);
        client.refresh();
        client.findVisible(By.id("participants-table"));
        Assert.assertTrue(participants.contains(name));
        table.selectRowContaining(name);
        table.clickAction("button-edit");
        Assert.assertEquals(new InputField(client, "participant-lastname").getValue().toLowerCase(java.util.Locale.ROOT), "original");
    }

    @Test
    public void mismatchedUserPasswordsDoNotCreateAccountAndCorrectionAllowsLogin() {
        login.login(USERNAME, PASSWORD);
        final String username = "negative." + System.currentTimeMillis() + "@test.local";
        final UserPage users = new UserPage(client);
        users.open(BASE_URL);
        final Table table = new Table(client, "users-table");
        table.clickAction("button-plus");
        new InputField(client, "username").setValue(username);
        new InputField(client, "name").setValue("Negative");
        new InputField(client, "lastname").setValue("Validation");
        new InputField(client, "password").setValue(PASSWORD);
        new InputField(client, "repeat-password").setValue("Different-password-123");
        client.clickWizardryButton(By.id("user-button-save"));
        client.findVisible(By.cssSelector("#repeat-password input.error"));
        new Popup(client, "user-popup").close();
        users.open(BASE_URL);
        client.refresh();
        client.findVisible(By.id("users-table"));
        table.search(username);
        Assert.assertFalse(table.containsText(username), "Password mismatch must not persist an account.");
        users.create(username, PASSWORD);
        login.logoutAndOpen(BASE_URL);
        login.login(username, PASSWORD);
        client.findVisible(By.id("navbar"));
    }

    @Test
    public void viewerCannotEditScoresAndExistingScoreSurvivesAttemptAndReload() {
        login.login(USERNAME, PASSWORD);
        final String identifier = Long.toString(System.currentTimeMillis());
        final String username = "viewer.negative." + identifier + "@test.local";
        final UserPage users = new UserPage(client);
        users.open(BASE_URL);
        users.create(username, PASSWORD);
        users.setRole(username, "Viewer");
        final String club = createClub(identifier);
        final ParticipantPage participants = new ParticipantPage(client);
        participants.open(BASE_URL);
        participants.create("First" + identifier, "Negative", "NA" + identifier, club);
        participants.create("Second" + identifier, "Negative", "NB" + identifier, club);
        final String tournament = "Read only " + identifier;
        final TournamentPage tournaments = new TournamentPage(client);
        tournaments.open(BASE_URL);
        tournaments.createLeague(tournament, 1);
        tournaments.addCompetitors("First" + identifier, "Second" + identifier);
        tournaments.createTeams(2, 2);
        tournaments.openFights();
        final FightPage fights = new FightPage(client);
        fights.generateLeagueFights();
        fights.scoreFirstDuelFor("Team 1");
        final String fightUrl = driver.getCurrentUrl();
        login.logoutAndOpen(BASE_URL);
        login.login(username, PASSWORD);
        tournaments.open(BASE_URL);
        tournaments.select(tournament);
        tournaments.openFights();
        fights.waitUntilContainsScore("M");
        final String scores = scoreSnapshot();
        client.findVisible(By.cssSelector("duel score .score-area")).click();
        Assert.assertTrue(client.findAll(By.cssSelector(".mat-mdc-menu-panel button[mat-menu-item]")).isEmpty(),
                "A viewer must not be offered score mutations.");
        Assert.assertTrue(client.findAll(By.cssSelector("button mat-icon[svgIcon='check']")).isEmpty());
        client.open(fightUrl);
        fights.waitUntilContainsScore("M");
        Assert.assertEquals(scoreSnapshot(), scores, "The attempted edit must not change persisted scores.");
    }

    @Test
    public void editorCannotOpenSuperAdministratorTenantManagement() {
        login.login(USERNAME, PASSWORD);
        final String username = "editor.negative." + System.currentTimeMillis() + "@test.local";
        final UserPage users = new UserPage(client);
        users.open(BASE_URL);
        users.create(username, PASSWORD);
        users.setEditorRole(username);
        login.logoutAndOpen(BASE_URL);
        login.login(username, PASSWORD);
        client.open(BASE_URL + "/#/administration/tenants");
        client.getWebDriverWait().until(browser -> browser.getCurrentUrl().contains("#/tournaments"));
        client.findVisible(By.id("tournaments-table"));
        Assert.assertTrue(client.findAll(By.cssSelector("app-tenant-list")).isEmpty());
        Assert.assertTrue(client.findAll(By.id("tenant-name")).isEmpty());
    }

    @Test
    public void cancellingParticipantDeletionKeepsRecordAndAllowsSubsequentEditing() {
        login.login(USERNAME, PASSWORD);
        final String name = "Keep" + System.currentTimeMillis();
        final String club = createClub(name);
        final ParticipantPage participants = new ParticipantPage(client);
        participants.open(BASE_URL);
        participants.create(name, "Preserved", name, club);
        final Table table = new Table(client, "participants-table");
        table.selectRowContaining(name);
        table.clickAction("button-minus");
        client.findVisible(By.id("confirmation-delete-popup"));
        client.clickWizardryButton(By.id("confirm-cancel-button"));
        client.waitUntilInvisible(By.id("confirmation-delete-popup"));
        client.refresh();
        client.findVisible(By.id("participants-table"));
        Assert.assertTrue(participants.contains(name), "Cancelling deletion must preserve the persisted participant.");
        Assert.assertEquals(table.getRowCount(), 1);
        table.selectRowContaining(name);
        table.clickAction("button-edit");
        Assert.assertEquals(new InputField(client, "participant-lastname").getValue().toLowerCase(java.util.Locale.ROOT), "preserved");
    }

    @Test
    public void loggedOutSessionCannotReopenProtectedDeepLink() {
        login.login(USERNAME, PASSWORD);
        new ParticipantPage(client).open(BASE_URL);
        final String protectedUrl = driver.getCurrentUrl();
        login.logoutAndOpen(BASE_URL);
        client.open(protectedUrl);
        client.findVisible(By.id("login"));
        Assert.assertTrue(client.findAll(By.id("participants-table")).isEmpty());
        Assert.assertNull(((JavascriptExecutor) driver).executeScript("return localStorage.getItem('.jwt');"));
        login.login(USERNAME, PASSWORD);
        new ParticipantPage(client).open(BASE_URL);
        client.findVisible(By.id("participants-table"));
    }

    private String createClub(String identifier) {
        final String name = "Negative Club " + identifier;
        final ClubPage clubs = new ClubPage(client);
        clubs.open(BASE_URL);
        clubs.create(name, "Spain", "Madrid");
        return name;
    }

    private String scoreSnapshot() {
        return client.findAll(By.cssSelector("duel score .score-area")).stream()
                .map(element -> element.getText().trim()).collect(java.util.stream.Collectors.joining("|"));
    }
}
