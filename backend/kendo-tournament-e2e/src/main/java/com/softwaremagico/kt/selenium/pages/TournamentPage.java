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
    private static final int LEAGUE_PARTICIPANT_COUNT = 6;

    private final WebDriverClient webDriverClient;
    private final Table table;

    public TournamentPage(WebDriverClient webDriverClient) {
        this.webDriverClient = webDriverClient;
        this.table = new Table(webDriverClient, "tournaments-table");
    }

    public void open(String baseUrl) {
        webDriverClient.open(baseUrl + "/tournaments");
        table.getRowCount();
    }

    public void createLeague(String name) {
        table.clickAction("button-plus");
        new InputField(webDriverClient, "tournament-name").setValue(name);
        new Dropdown(webDriverClient, "tournament-type").select("League");
        new InputField(webDriverClient, "tournament-team-size").setValue("3");
        new InputField(webDriverClient, "tournament-fight-size").setValue("3");
        webDriverClient.waitUntilClickable(By.id("tournament-button-save")).click();
        new Popup(webDriverClient, "tournament-popup").waitUntilClosed();
        table.search(name);
        table.selectRowContaining(name);
    }

    public void addCompetitors(String... participantNames) {
        clickSelectedTournamentAction("card");
        for (String participantName : participantNames) {
            final By participant = By.xpath("//tournament-roles//user-card[contains(normalize-space(), "
                    + toXPathLiteral(participantName) + ")]");
            webDriverClient.dragAndDrop(webDriverClient.findVisible(participant),
                    webDriverClient.findVisible(By.cssSelector("tournament-roles .role")));
        }
        webDriverClient.waitUntilClickable(By.id("tournament-button-close")).click();
    }

    public void createTwoTeamsOfThree() {
        clickSelectedTournamentAction("team");
        webDriverClient.waitUntilClickable(By.id("tournament-team-add")).click();
        webDriverClient.waitUntilClickable(By.id("tournament-team-add")).click();
        webDriverClient.waitUntilClickable(By.cssSelector("tournament-teams button[biit-button][secondary]")).click();
        webDriverClient.waitForAtLeast(By.cssSelector("tournament-teams .team .user-card"), LEAGUE_PARTICIPANT_COUNT);
        webDriverClient.waitUntilClickable(By.id("tournament-button-close")).click();
    }

    public void openFights() {
        clickSelectedTournamentAction("fight");
    }

    private void clickSelectedTournamentAction(String icon) {
        webDriverClient.waitUntilClickable(By.xpath("//button[.//mat-icon[@svgIcon='" + icon + "']]")).click();
    }

    private String toXPathLiteral(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }
        return "concat('" + value.replace("'", "', \"'\", '") + "')";
    }
}
