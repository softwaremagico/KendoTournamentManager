package com.softwaremagico.kt.selenium;

/*-
 * #%L
 * Kendo Tournament Manager (End-to-End Tests)
 * %%
 * Copyright (C) 2021 - 2026 SoftwareMagico
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

import com.softwaremagico.kt.selenium.components.InputField;
import com.softwaremagico.kt.selenium.components.Popup;
import com.softwaremagico.kt.selenium.components.Table;
import com.softwaremagico.kt.selenium.pages.ClubPage;
import com.softwaremagico.kt.selenium.pages.FightPage;
import com.softwaremagico.kt.selenium.pages.LoginPage;
import com.softwaremagico.kt.selenium.pages.ParticipantPage;
import com.softwaremagico.kt.selenium.pages.TournamentPage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.util.List;
import java.util.Map;

/** Real UI setup with authenticated browser HTTP reads for precise persistence assertions. */
public abstract class AdvancedBrowserTest {
    protected static final String URL = System.getProperty("selenium.base-url", "http://localhost:14200");
    protected static final String PASSWORD = System.getProperty("selenium.password", "E2e-password-123");
    protected WebDriver driver;
    protected WebDriver other;
    protected WebDriverClient client;
    protected TournamentPage tournaments;
    protected FightPage fights;
    protected String tournament;
    protected String club;
    protected String[] names;
    protected int tournamentId;

    @BeforeMethod
    public void startBrowser() {
        driver = browser();
        client = new WebDriverClient(driver);
        login(driver, System.getProperty("selenium.username", "e2e-admin@test.local"));
        tournaments = new TournamentPage(client);
        fights = new FightPage(client);
    }

    protected WebDriver browser() {
        return ChromeDriverFactory.create(Boolean.parseBoolean(System.getProperty("selenium.headless", "true")));
    }

    protected void login(WebDriver browser, String username) {
        final LoginPage login = new LoginPage(new WebDriverClient(browser));
        login.open(URL);
        login.login(username, PASSWORD);
    }

    @AfterMethod(alwaysRun = true)
    public void stopBrowser() {
        if (other != null) { other.quit(); other = null; }
        if (driver != null) { driver.quit(); }
    }

    protected void seed(String type, int teams, int size) {
        final String id = Long.toString(System.currentTimeMillis());
        club = "Advanced Club " + id;
        tournament = "Advanced " + type + " " + id;
        final ClubPage clubs = new ClubPage(client);
        clubs.open(URL);
        clubs.create(club, "Spain", "Madrid");
        final ParticipantPage participants = new ParticipantPage(client);
        participants.open(URL);
        names = new String[teams * size];
        for (int i = 0; i < names.length; i++) {
            names[i] = "Advanced" + id + "Member" + i;
            participants.create(names[i], "Player", "ADV" + id + i, club);
        }
        tournaments.open(URL);
        if (type.equals("Senbatsu")) { tournaments.createSenbatsu(tournament); }
        else { tournaments.createTournament(tournament, type, size); }
        tournaments.addCompetitors(names);
        tournaments.createTeams(teams, names.length);
        tournamentId = 0;
        // Resolve through the public listing rather than depending on a storage key.
        for (Map<String, Object> row : list(api("GET", "/tournaments", null))) {
            if (tournament.equals(row.get("name"))) { tournamentId = number(row, "id"); }
        }
        Assert.assertTrue(tournamentId > 0);
        tournaments.openFights();
    }

    protected void icon(String name) {
        closeRankings();
        client.clickFirstVisible(By.xpath("//button[.//mat-icon[@svgIcon='" + name + "']]"));
    }

    protected void closeRankings() {
        for (String id : List.of("teams-ranking-close", "competitors-ranking-close")) {
            client.getWebDriverWait().until(browser -> {
                final List<WebElement> buttons=browser.findElements(By.id(id));
                if(buttons.isEmpty() || !buttons.getFirst().isDisplayed()) {return true;}
                ((JavascriptExecutor)browser).executeScript("arguments[0].click();",buttons.getFirst().findElement(By.cssSelector(".button-base")));
                return true;
            });
        }
    }

    protected void options() {
        tournaments.open(URL);
        tournaments.select(tournament);
        new Table(client, "tournaments-table").clickAction("button-edit");
        client.clickFirstVisible(By.id("edit-options-button"));
        client.findVisible(By.id("custom-properties-popup"));
    }

    protected void closeOptions() {
        client.clickWizardryButton(By.cssSelector("#custom-properties-popup #tournament-button-close"));
        new Popup(client, "tournament-popup").close();
        tournaments.openFights();
    }

    protected WebElement duel(int index) {
        client.waitForAtLeast(By.cssSelector("fight duel:not(.substitute)"),index+1);
        return driver.findElements(By.cssSelector("fight duel:not(.substitute)")).get(index);
    }

    protected void score(WebElement duel, int side, int slot, String label) {
        final JavascriptExecutor script=(JavascriptExecutor)driver;
        script.executeScript("""
            if(!window.advancedTracking) {
              window.advancedTracking=true;window.advancedSaves=0;
              const fetchOriginal=window.fetch;
              window.fetch=async function(...args) {const r=await fetchOriginal.apply(this,args);if(r.url.includes('/duels')&&r.ok)window.advancedSaves++;return r;};
              const send=XMLHttpRequest.prototype.send;
              XMLHttpRequest.prototype.send=function(...args){this.addEventListener('loadend',()=>{if(this.responseURL.includes('/duels')&&this.status<300)window.advancedSaves++;});return send.apply(this,args);};
            }
            window.advancedBaseline=window.advancedSaves;
            """);
        client.dismissSuccessNotifications();
        duel.findElements(By.cssSelector("user-score")).get(side).findElements(By.cssSelector("score .score-area")).stream()
                .filter(WebElement::isDisplayed).toList().get(slot).click();
        final By menuOption=By.xpath("//div[contains(@class,'mat-mdc-menu-panel')]//button[normalize-space()='" + label + "']");
        client.getWebDriverWait().until(browser -> {
            final List<WebElement> options=browser.findElements(menuOption).stream().filter(WebElement::isDisplayed).toList();
            if(options.isEmpty()) {return false;}
            script.executeScript("arguments[0].click();",options.getLast());
            return true;
        });
        client.waitUntilInvisible(By.cssSelector(".mat-mdc-menu-panel"));
        client.getWebDriverWait().until(browser -> browser.findElements(By.cssSelector("score [data-saving='true']")).isEmpty());
        client.getWebDriverWait().until(browser -> Boolean.TRUE.equals(script.executeScript("return window.advancedSaves>window.advancedBaseline;")));
    }

    protected void finish(WebElement duel) {
        client.dismissSuccessNotifications();
        duel.click();
        icon("check");
        client.getWebDriverWait().until(browser -> duel.getAttribute("class").contains("over"));
        closeRankings();
    }

    protected void reloadFights() {
        closeRankings();
        client.refresh();
        client.findVisible(By.cssSelector("fight"));
    }

    protected Object api(String method, String path, Object body) {
        return request(driver, method, path, body).get("body");
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> request(WebDriver browser, String method, String path, Object body) {
        return (Map<String, Object>) ((JavascriptExecutor) browser).executeAsyncScript("""
            const [method, path, body, done] = arguments;
            const config = window.__config || __config;
            fetch(config.backendUrl + path, {method, headers: {
              Authorization: 'Bearer ' + localStorage.getItem('.jwt'), 'Content-Type': 'application/json'
            }, body: body == null ? undefined : JSON.stringify(body)})
            .then(async r => {const text = await r.text(); let data; try {data=JSON.parse(text)} catch {data=text}; done({status:r.status,body:data});})
            .catch(e => done({status:0,body:String(e)}));
            """, method, path, body);
    }

    @SuppressWarnings("unchecked")
    protected static List<Map<String, Object>> list(Object value) { return (List<Map<String, Object>>) value; }
    @SuppressWarnings("unchecked")
    protected static Map<String, Object> map(Object value) { return (Map<String, Object>) value; }
    protected static int number(Map<String, Object> value, String field) { return ((Number) value.get(field)).intValue(); }
    protected List<Map<String, Object>> persistedFights() { return list(api("GET", "/fights/tournaments/" + tournamentId, null)); }
}
