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

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import com.softwaremagico.kt.selenium.pages.ClubPage;
import com.softwaremagico.kt.selenium.pages.FightPage;
import com.softwaremagico.kt.selenium.pages.LoginPage;
import com.softwaremagico.kt.selenium.pages.ParticipantPage;
import com.softwaremagico.kt.selenium.pages.TournamentPage;
import com.softwaremagico.kt.selenium.components.Table;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipInputStream;
import java.io.ByteArrayInputStream;

/** Verifies real generated export bytes and club ranking using browser actions. */
public class GeneratedExportsTest {
    private static final String URL = System.getProperty("selenium.base-url", "http://localhost:14200");
    private WebDriver driver;
    private WebDriverClient client;
    private String club;
    private String tournament;
    private String first;
    private String second;
    private String outsider;
    private TournamentPage tournaments;

    @BeforeMethod
    public void setUp() {
        driver = ChromeDriverFactory.create(Boolean.parseBoolean(System.getProperty("selenium.headless", "true")));
        client = new WebDriverClient(driver);
        final LoginPage login = new LoginPage(client);
        login.open(URL);
        login.login(System.getProperty("selenium.username", "e2e-admin@test.local"), System.getProperty("selenium.password", "E2e-password-123"));
        final String id = Long.toString(System.currentTimeMillis());
        club = "Export Club " + id;
        tournament = "Export League " + id;
        first = "ExportWinner" + id;
        second = "ExportPartner" + id;
        outsider = "ExportOther" + id;
        final ClubPage clubs = new ClubPage(client);
        clubs.open(URL);
        clubs.create(club, "Spain", "Madrid");
        clubs.create("Other Club " + id, "Spain", "Madrid");
        final ParticipantPage participants = new ParticipantPage(client);
        participants.open(URL);
        participants.create(first, "Player", "EA" + id, club);
        participants.create(second, "Player", "EB" + id, club);
        participants.create(outsider, "Player", "EC" + id, "Other Club " + id);
        participants.create("ExportFourth" + id, "Player", "ED" + id, "Other Club " + id);
        tournaments = new TournamentPage(client);
        tournaments.open(URL);
        tournaments.createLeague(tournament, 2);
        tournaments.addCompetitors(first, second, outsider, "ExportFourth" + id);
        tournaments.createTeams(2, 4);
        tournaments.openFights();
        final FightPage fights = new FightPage(client);
        fights.generateLeagueFights();
        fights.resolveAllFightsFor("Team 1");
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() { if (driver != null) { driver.quit(); } }

    /** Capture the actual browser-generated download Blob; HTTP requests are not mocked. */
    private void capture() {
        ((JavascriptExecutor) driver).executeScript("""
            window.e2eExports = [];
            if (!window.e2eDownloadInstalled) {
              window.e2eDownloadInstalled = true;
              const create = URL.createObjectURL;
              URL.createObjectURL = function(blob) {
                if (blob instanceof Blob && /pdf|zip|plain/.test(blob.type)) {
                  const reader = new FileReader();
                  reader.onload = () => window.e2eExports.push({name: '', data: reader.result.split(',')[1]});
                  reader.readAsDataURL(blob);
                }
                return create.call(this, blob);
              };
              const click = HTMLAnchorElement.prototype.click;
              HTMLAnchorElement.prototype.click = function() {
                if (!this.download) return click.call(this);
                const name = this.download;
                fetch(this.href).then(r => r.blob()).then(blob => {
                  const reader = new FileReader();
                  reader.onload = () => window.e2eExports.push({name, data: reader.result.split(',')[1]});
                  reader.readAsDataURL(blob);
                }).catch(e => window.e2eExportError = String(e));
              };
            }
            """);
    }

    private byte[] export(Runnable click) {
        capture();
        click.run();
        client.getWebDriverWait().until(browser -> ((Number) ((JavascriptExecutor) browser)
                .executeScript("return window.e2eExports.length;")).intValue() > 0);
        return Base64.getDecoder().decode((String) ((JavascriptExecutor) driver).executeScript("return window.e2eExports[0].data;"));
    }

    private String pdf(byte[] bytes) throws Exception {
        Assert.assertTrue(bytes.length > 500, "Generated PDF must contain document data.");
        Assert.assertEquals(new String(bytes, 0, 5, StandardCharsets.US_ASCII), "%PDF-");
        final PdfReader reader = new PdfReader(bytes);
        try {
            Assert.assertTrue(reader.getNumberOfPages() > 0);
            final StringBuilder text = new StringBuilder();
            final PdfTextExtractor extractor = new PdfTextExtractor(reader);
            for (int page = 1; page <= reader.getNumberOfPages(); page++) { text.append(extractor.getTextFromPage(page)); }
            Assert.assertFalse(text.toString().isBlank(), "PDF must contain readable content, not just an empty page.");
            return text.toString();
        } finally { reader.close(); }
    }

    private void icon(String icon) {
        client.clickFirstVisible(By.xpath("//button[.//mat-icon[@svgIcon='" + icon + "']]"));
    }

    private void select() { tournaments.open(URL); tournaments.select(tournament); }

    @Test
    public void clubRankingContainsOnlyMembersAndTheirActualDuelStatistics() {
        final ClubPage clubs = new ClubPage(client);
        clubs.open(URL);
        final Table table = new Table(client, "clubs-table");
        table.search(club);
        table.selectRowContaining(club);
        client.clickFirstVisible(By.id("club-ranking"));
        client.getWebDriverWait().until(browser -> browser.findElements(By.cssSelector("#ranking-popup .competitor-name")).size() == 2);
        final String ranking = client.findVisible(By.id("ranking-popup")).getText().toLowerCase(Locale.ROOT);
        Assert.assertTrue(ranking.contains(first.toLowerCase(Locale.ROOT)));
        Assert.assertTrue(ranking.contains(second.toLowerCase(Locale.ROOT)));
        Assert.assertFalse(ranking.contains(outsider.toLowerCase(Locale.ROOT)));
        for (WebElement row : driver.findElements(By.cssSelector("#ranking-popup tr:has(.competitor-name)"))) {
            final List<WebElement> cells = row.findElements(By.tagName("td"));
            Assert.assertEquals(cells.get(5).getText(), "1", "Each club member has fought exactly one duel.");
            Assert.assertTrue(Set.of("0", "2").contains(cells.get(4).getText()), "Hits must reflect the scored duel.");
        }
        Assert.assertEquals(driver.findElements(By.cssSelector("#ranking-popup tr:has(.competitor-name)")).stream()
                .mapToInt(row -> Integer.parseInt(row.findElements(By.tagName("td")).get(4).getText())).sum(), 2);
    }

    @Test
    public void generatesRoleTeamFightRankingQrDiplomaAndAccreditationPdfs() throws Exception {
        select();
        icon("card");
        Assert.assertTrue(pdf(export(() -> client.clickWizardryButton(By.id("roles-button-download")))).contains(tournament));
        client.clickWizardryButton(By.id("tournament-button-close"));
        tournaments.select(tournament);
        icon("team");
        pdf(export(() -> client.clickWizardryButton(By.xpath("//tournament-teams//button[normalize-space()='Download']"))));
        client.clickWizardryButton(By.id("tournament-button-close"));
        tournaments.openFights();
        Assert.assertTrue(pdf(export(() -> client.clickFirstVisible(By.cssSelector("button.download-button")))).contains(tournament));
        icon("teams-classification");
        pdf(export(() -> client.clickWizardryButton(By.xpath("//*[@id='teams-ranking-popup']//button[normalize-space()='Download']"))));
        client.clickWizardryButton(By.id("teams-ranking-close"));
        icon("competitors-classification");
        pdf(export(() -> client.clickWizardryButton(By.xpath("//*[@id='competitors-ranking-popup']//button[normalize-space()='Download']"))));
        select();
        icon("qr_code");
        pdf(export(() -> client.clickWizardryButton(By.id("tournament-qr-download"))));
        client.clickWizardryButton(By.id("tournament-qr-close"));
        for (String type : List.of("badge", "diplomas")) {
            tournaments.select(tournament);
            icon(type);
            client.clickFirstVisible(By.cssSelector("role-selector biit-checkbox"));
            final String text = pdf(export(() -> client.clickWizardryButton(By.cssSelector("role-selector button[primary]"))));
            Assert.assertTrue(text.toLowerCase(Locale.ROOT).contains("player"), "Personal documents must identify participants.");
        }
        select();
        final byte[] zip = export(() -> icon("zip-file"));
        final Set<String> documents = new HashSet<>();
        try (ZipInputStream stream = new ZipInputStream(new ByteArrayInputStream(zip))) {
            java.util.zip.ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                documents.add(entry.getName().split(" - ")[0]);
                if (entry.getName().endsWith(".pdf")) { pdf(stream.readAllBytes()); }
            }
        }
        Assert.assertTrue(documents.containsAll(Set.of("Role List", "Team List", "Fight List", "Team Ranking", "Competitors Ranking", "Wordpress code")),
                "ZIP must include each report, not silently omit a failed PDF: " + documents);
    }

    @Test
    public void wordpressExportContainsTournamentCompetitorsAndScoreTables() {
        select();
        tournaments.lock(tournament);
        final String html = new String(export(() -> icon("tournament-blog")), StandardCharsets.UTF_8);
        Assert.assertTrue(html.contains("<h2>" + tournament + "</h2>"));
        Assert.assertTrue(html.toLowerCase(Locale.ROOT).contains(first.toLowerCase(Locale.ROOT)));
        Assert.assertTrue(html.toLowerCase(Locale.ROOT).contains(outsider.toLowerCase(Locale.ROOT)));
        Assert.assertTrue(html.contains("<table"));
        Assert.assertTrue(html.contains("Team 1") && html.contains("Team 2"));
        Assert.assertFalse(html.contains("undefined"));
    }

    @Test
    public void generatesChampionshipGroupListAndVisualBracketPdfs() throws Exception {
        select();
        final String championship = "Bracket " + tournament;
        final TournamentPage brackets = new TournamentPage(client);
        brackets.createChampionship(championship, 1);
        brackets.addCompetitors(first, second);
        brackets.createTeams(2, 2);
        brackets.openFights();
        new FightPage(client).generateChampionshipFights();
        icon("brackets");
        client.findVisible(By.cssSelector("app-tournament-generator"));
        final byte[] groups = export(() -> client.clickFirstVisible(By.cssSelector("app-tournament-generator .upper-menu button:has(mat-icon[svgIcon='download'])")));
        Assert.assertTrue(pdf(groups).contains(championship));
        client.clickFirstVisible(By.cssSelector("tournament-brackets-editor biit-action-button button[biit-icon]"));
        final byte[] bracket = export(() -> client.clickFirstVisible(By.cssSelector("tournament-brackets-editor button:has(mat-icon[svgIcon='download'])")));
        Assert.assertEquals(new String(bracket, 0, 5, StandardCharsets.US_ASCII), "%PDF-");
        final PdfReader reader = new PdfReader(bracket);
        try {
            Assert.assertTrue(reader.getNumberOfPages() > 0);
            Assert.assertTrue(reader.getPageContent(1).length > 100, "Visual bracket PDF must contain drawing/image content.");
        } finally { reader.close(); }
    }
}
