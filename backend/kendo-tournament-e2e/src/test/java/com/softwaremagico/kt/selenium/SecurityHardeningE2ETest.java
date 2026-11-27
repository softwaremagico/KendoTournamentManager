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

import com.softwaremagico.kt.selenium.pages.ClubPage;
import com.softwaremagico.kt.selenium.pages.FightPage;
import com.softwaremagico.kt.selenium.pages.LoginPage;
import com.softwaremagico.kt.selenium.pages.ParticipantPage;
import com.softwaremagico.kt.selenium.pages.TournamentPage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * End-to-end coverage for the security hardening introduced after the audit:
 * exact CORS origins, WebSocket origin checks and signed guest capabilities.
 */
public class SecurityHardeningE2ETest {
    private static final String BASE_URL = System.getProperty("selenium.base-url", "http://localhost:14200");
    private static final String USERNAME = System.getProperty("selenium.username", "e2e-admin@test.local");
    private static final String PASSWORD = System.getProperty("selenium.password", "E2e-password-123");

    private WebDriver administratorDriver;
    private final List<WebDriver> temporaryDrivers = new ArrayList<>();

    @BeforeClass
    public void setUp() {
        administratorDriver = createDriver();
        final LoginPage loginPage = new LoginPage(new WebDriverClient(administratorDriver));
        loginPage.open(BASE_URL);
        loginPage.login(USERNAME, PASSWORD);
        new WebDriverClient(administratorDriver).findVisible(By.id("navbar"));
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        quit(administratorDriver);
        temporaryDrivers.forEach(this::quit);
        temporaryDrivers.clear();
    }

    @Test
    public void onlyConfiguredOriginCanCallBackendFromBrowser() {
        final String allowedResult = probeCrossOriginFetch(BASE_URL);
        Assert.assertTrue(allowedResult.startsWith("status:200"),
                "The configured frontend origin must be allowed by CORS, got: " + allowedResult);

        final String unconfiguredUrl = BASE_URL.replace("localhost", "127.0.0.1");
        Assert.assertNotEquals(unconfiguredUrl, BASE_URL, "The test needs a second origin with a different host.");
        final String blockedResult = probeCrossOriginFetch(unconfiguredUrl);
        Assert.assertTrue(blockedResult.startsWith("error:"),
                "An origin absent from SERVER_CORS_DOMAINS must be rejected, got: " + blockedResult);
    }

    @Test
    public void websocketHandshakeRejectsUnconfiguredOrigin() {
        final String allowedResult = probeWebSocket(BASE_URL);
        Assert.assertEquals(allowedResult, "open",
                "The configured frontend origin must complete the WebSocket handshake, got: " + allowedResult);

        final String unconfiguredUrl = BASE_URL.replace("localhost", "127.0.0.1");
        final String blockedResult = probeWebSocket(unconfiguredUrl);
        Assert.assertTrue(blockedResult.equals("error") || blockedResult.startsWith("close:"),
                "A WebSocket handshake from an unknown origin must fail, got: " + blockedResult);
    }

    @Test
    public void tamperedGuestTicketCannotOpenTournament() {
        final String identifier = Long.toString(System.currentTimeMillis());
        final String clubName = "Security Club " + identifier;
        final String firstParticipant = "SecurityFirst" + identifier;
        final String secondParticipant = "SecuritySecond" + identifier;
        final String tournamentName = "Security Tournament " + identifier;
        final WebDriverClient administratorClient = new WebDriverClient(administratorDriver);

        final ClubPage clubPage = new ClubPage(administratorClient);
        clubPage.open(BASE_URL);
        clubPage.create(clubName, "Spain", "Valéncia");

        final ParticipantPage participantPage = new ParticipantPage(administratorClient);
        participantPage.open(BASE_URL);
        participantPage.create(firstParticipant, "Player", "SFA" + identifier, clubName);
        participantPage.create(secondParticipant, "Player", "SFB" + identifier, clubName);

        final TournamentPage tournamentPage = new TournamentPage(administratorClient);
        tournamentPage.open(BASE_URL);
        tournamentPage.createLeague(tournamentName, 1);
        tournamentPage.addCompetitors(firstParticipant, secondParticipant);
        tournamentPage.createTeams(2, 2);
        final String guestLink = tournamentPage.openGuestQrLink(tournamentName);
        Assert.assertTrue(guestLink.contains("ticket="), "The QR link must carry a signed guest ticket.");
        tournamentPage.openFights();
        new FightPage(administratorClient).generateLeagueFights();

        final WebDriver guestDriver = createDriver();
        temporaryDrivers.add(guestDriver);
        guestDriver.get(guestLink);
        new WebDriverClient(guestDriver).findVisible(By.cssSelector("fight"));

        final String tamperedLink = guestLink.replaceFirst("([?&]ticket=)([^&]+)", "$1tampered-$2");
        Assert.assertNotEquals(tamperedLink, guestLink, "The ticket must be modifiable for this test.");
        final WebDriver attackerDriver = createDriver();
        temporaryDrivers.add(attackerDriver);
        attackerDriver.get(tamperedLink);
        final WebDriverClient attackerClient = new WebDriverClient(attackerDriver);
        attackerClient.findVisible(By.id("login"));
        Assert.assertTrue(attackerClient.findAll(By.cssSelector("fight")).isEmpty(),
                "A tampered ticket must not open a guest session.");
    }

    private String probeCrossOriginFetch(String pageUrl) {
        final WebDriver driver = createDriver();
        temporaryDrivers.add(driver);
        driver.get(pageUrl);
        new WebDriverClient(driver).findVisible(By.id("login"));
        return (String) ((JavascriptExecutor) driver).executeAsyncScript(
                "const done = arguments[arguments.length - 1];" +
                        "try {" +
                        "  fetch(window.__config.backendUrl + '/info/health-check', {credentials: 'include'})" +
                        "    .then(response => done('status:' + response.status))" +
                        "    .catch(error => done('error:' + error));" +
                        "} catch (error) {" +
                        "  done('error:' + error);" +
                        "}");
    }

    private String probeWebSocket(String pageUrl) {
        final WebDriver driver = createDriver();
        temporaryDrivers.add(driver);
        driver.get(pageUrl);
        new WebDriverClient(driver).findVisible(By.id("login"));
        return (String) ((JavascriptExecutor) driver).executeAsyncScript(
                "const done = arguments[arguments.length - 1];" +
                        "let finished = false;" +
                        "let timer;" +
                        "const finish = result => {" +
                        "  if (!finished) { finished = true; clearTimeout(timer); done(result); }" +
                        "};" +
                        "try {" +
                        "  const socket = new WebSocket(window.__config.websocketsUrl);" +
                        "  timer = setTimeout(() => { try { socket.close(); } catch (ignored) {} finish('timeout'); }, 5000);" +
                        "  socket.onopen = () => { finish('open'); try { socket.close(); } catch (ignored) {} };" +
                        "  socket.onerror = () => finish('error');" +
                        "  socket.onclose = event => finish('close:' + event.code);" +
                        "} catch (error) {" +
                        "  finish('error:' + error);" +
                        "}");
    }

    private WebDriver createDriver() {
        return ChromeDriverFactory.create(Boolean.parseBoolean(System.getProperty("selenium.headless", "true")));
    }

    private void quit(WebDriver driver) {
        if (driver != null) {
            driver.quit();
        }
    }
}
