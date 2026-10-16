package com.softwaremagico.kt.selenium.pages;

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

import com.softwaremagico.kt.selenium.WebDriverClient;
import com.softwaremagico.kt.selenium.components.Dropdown;
import com.softwaremagico.kt.selenium.components.InputField;
import com.softwaremagico.kt.selenium.components.Popup;
import com.softwaremagico.kt.selenium.components.Table;
import org.openqa.selenium.By;

/**
 * Page object for tournament administration and its roles and teams dialogs.
 */
public class TournamentPage {
    private static final int THREE_MEMBER_TEAM_SIZE = 3;
    private static final int TWO_THREE_MEMBER_TEAMS = 2;
    private static final int TWO_THREE_MEMBER_TEAM_PARTICIPANTS = 6;

    private final WebDriverClient webDriverClient;
    private final Table table;
    private String selectedTournamentName;

    public TournamentPage(WebDriverClient webDriverClient) {
        this.webDriverClient = webDriverClient;
        this.table = new Table(webDriverClient, "tournaments-table");
    }

    public void open(String baseUrl) {
        webDriverClient.open(baseUrl + "/#/tournaments");
        table.getRowCount();
    }

    public void createLeague(String name) {
        createLeague(name, THREE_MEMBER_TEAM_SIZE);
    }

    public void createLeague(String name, int teamSize) {
        createTournament(name, "League", teamSize);
    }

    public void createLeagueWithScoreRule(String name, String scoreRule) {
        selectedTournamentName = name;
        table.clickAction("button-plus");
        new InputField(webDriverClient, "tournament-name").setValue(name);
        new Dropdown(webDriverClient, "tournament-type").select("League");
        new Dropdown(webDriverClient, "tournament-score-rules").select(scoreRule);
        webDriverClient.clickWizardryButton(By.id("tournament-button-save"));
        table.waitUntilContainsText(name);
        new Popup(webDriverClient, "tournament-popup").close();
    }

    public void createChampionship(String name, int teamSize) {
        createTournament(name, "Championship", teamSize);
    }

    public void createTournament(String name, String type, int teamSize) {
        createTournamentForm(name, type, teamSize);
    }

    public void createSenbatsu(String name) {
        selectedTournamentName = name;
        table.clickAction("button-plus");
        new InputField(webDriverClient, "tournament-name").setValue(name);
        new Dropdown(webDriverClient, "tournament-type").select("Senbatsu");
        webDriverClient.clickWizardryButton(By.id("tournament-button-save"));
        table.waitUntilContainsText(name);
        new Popup(webDriverClient, "tournament-popup").close();
        table.search(name);
        table.selectRowContaining(name);
    }

    private void createTournamentForm(String name, String type, int teamSize) {
        selectedTournamentName = name;
        table.clickAction("button-plus");
        new InputField(webDriverClient, "tournament-name").setValue(name);
        new Dropdown(webDriverClient, "tournament-type").select(type);
        new InputField(webDriverClient, "tournament-team-size").setValue(Integer.toString(teamSize));
        new InputField(webDriverClient, "tournament-fight-size").setValue(Integer.toString(teamSize));
        webDriverClient.clickWizardryButton(By.id("tournament-button-save"));
        table.waitUntilContainsText(name);
        new Popup(webDriverClient, "tournament-popup").close();
        table.search(name);
        table.selectRowContaining(name);
    }

    public void addCompetitors(String... participantNames) {
        clickSelectedTournamentAction("card");
        webDriverClient.waitForAtLeast(By.cssSelector("tournament-roles user-list user-card"), participantNames.length);
        for (String participantName : participantNames) {
            final By participant = By.xpath("//tournament-roles//user-list//user-card[contains(translate(normalize-space(), "
                    + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), " + toXPathLiteral(participantName.toLowerCase()) + ")]");
            webDriverClient.waitUntilClickable(participant).click();
        }
        webDriverClient.waitForAtLeast(By.cssSelector("tournament-roles .role user-card"), participantNames.length);
        webDriverClient.waitUntilClickable(By.id("tournament-button-close")).click();
    }

    public void createTwoTeamsOfThree() {
        createTeams(TWO_THREE_MEMBER_TEAMS, TWO_THREE_MEMBER_TEAM_PARTICIPANTS);
    }

    public void createTeams(int teamCount, int participantCount) {
        clickSelectedTournamentAction("team");
        final boolean individualTeams = teamCount == participantCount;
        if (!individualTeams) {
            webDriverClient.findVisible(By.cssSelector("tournament-teams .team"));
            final int existingTeams = webDriverClient.findAll(By.cssSelector("tournament-teams .team")).size();
            for (int index = existingTeams; index < teamCount; index++) {
                webDriverClient.clickWizardryButton(By.id("tournament-team-add"));
            }
        }
        webDriverClient.waitForAtLeast(By.cssSelector("tournament-teams user-list user-card"), participantCount);
        webDriverClient.clickWizardryButton(By.id("tournament-teams-generate"));
        webDriverClient.waitForAtLeast(By.cssSelector("tournament-teams .team .user-card"), participantCount);
        webDriverClient.waitUntilClickable(By.id("tournament-button-close")).click();
    }

    public void openFights() {
        clickSelectedTournamentAction("fight");
    }

    public void select(String tournamentName) {
        selectedTournamentName = tournamentName;
        table.search(tournamentName);
        table.selectRowContaining(tournamentName);
    }

    public void lock(String tournamentName) {
        if (webDriverClient.isVisible(By.id("tournament-qr-close"))) {
            webDriverClient.clickWizardryButton(By.id("tournament-qr-close"));
            webDriverClient.waitUntilInvisible(By.id("qr-code"));
        }
        table.search(tournamentName);
        table.selectRowContaining(tournamentName);
        webDriverClient.waitUntilClickable(By.cssSelector("button[biit-icon][icon='lock']")).click();
        table.waitUntilContainsText(tournamentName);
        table.selectRowContaining(tournamentName);
    }

    public void cloneTournament(String tournamentName) {
        table.search(tournamentName);
        table.selectRowContaining(tournamentName);
        webDriverClient.waitUntilClickable(By.xpath("//button[.//mat-icon[@svgIcon='clone']]")).click();
        webDriverClient.clickWizardryButton(By.id("clone-confirm-button"));
        table.search("");
        table.search(tournamentName + " - Copy");
        table.waitUntilContainsText(tournamentName + " - Copy");
        selectedTournamentName = tournamentName + " - Copy";
    }

    public boolean contains(String tournamentName) {
        table.search(tournamentName);
        return table.containsText(tournamentName);
    }

    public void openStatistics(String tournamentName) {
        table.search(tournamentName);
        table.selectRowContaining(tournamentName);
        webDriverClient.clickFirstVisible(By.xpath("//button[.//mat-icon[@svgIcon='bar_chart']]"));
        webDriverClient.getWebDriverWait().withMessage(() -> "Statistics URL: " + webDriverClient.getDriver().getCurrentUrl()
                + "; body: " + webDriverClient.getDriver().findElement(By.tagName("body")).getText())
                .until(driver -> !driver.findElements(By.cssSelector(".statistics-view .name")).isEmpty());
    }

    public String getStatisticsTournamentName() {
        return webDriverClient.findVisible(By.cssSelector(".statistics-view .name")).getText();
    }

    public String openGuestQrLink(String tournamentName) {
        table.search(tournamentName);
        table.selectRowContaining(tournamentName);
        webDriverClient.waitUntilClickable(By.xpath("//button[.//mat-icon[@svgIcon='qr_code']]")).click();
        final String link = webDriverClient.findVisible(By.cssSelector("#qr-code .link")).getAttribute("href");
        webDriverClient.clickWizardryButton(By.id("tournament-qr-close"));
        webDriverClient.waitUntilInvisible(By.id("qr-code"));
        return link;
    }

    public String getScoreRule(String tournamentName) {
        table.search(tournamentName);
        table.selectRowContaining(tournamentName);
        table.clickAction("button-edit");
        final String scoreRule = webDriverClient.findVisible(By.id("tournament-score-rules"))
                .findElement(By.cssSelector("input")).getAttribute("value");
        new Popup(webDriverClient, "tournament-popup").close();
        return scoreRule;
    }

    public void openCustomScoreRules() {
        select(selectedTournamentName);
        table.clickAction("button-edit");
        webDriverClient.waitUntilClickable(By.id("edit-score-button")).click();
        webDriverClient.findVisible(By.id("score-rules-popup"));
    }

    private void clickSelectedTournamentAction(String icon) {
        if (selectedTournamentName != null) {
            select(selectedTournamentName);
        }
        webDriverClient.clickFirstVisible(By.xpath("//button[.//mat-icon[@svgIcon='" + icon + "']]"));
    }

    private String toXPathLiteral(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }
        return "concat('" + value.replace("'", "', \"'\", '") + "')";
    }
}
