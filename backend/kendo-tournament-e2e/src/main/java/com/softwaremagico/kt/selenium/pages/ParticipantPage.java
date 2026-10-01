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
 * Page object for the participant registry.
 */
public class ParticipantPage {
    private final WebDriverClient webDriverClient;
    private final Table table;

    public ParticipantPage(WebDriverClient webDriverClient) {
        this.webDriverClient = webDriverClient;
        this.table = new Table(webDriverClient, "participants-table");
    }

    public void open(String baseUrl) {
        webDriverClient.open(baseUrl + "/#/registry/participants");
        table.getRowCount();
    }

    public void create(String name, String lastname, String idCard, String clubName) {
        table.clickAction("button-plus");
        new InputField(webDriverClient, "participant-name").setValue(name);
        new InputField(webDriverClient, "participant-lastname").setValue(lastname);
        new InputField(webDriverClient, "participant-id-card").setValue(idCard);
        new Dropdown(webDriverClient, "participant-club").select(clubName);
        webDriverClient.clickWizardryButton(By.id("participant-button-save"));
        webDriverClient.waitUntilInvisible(By.id("participant-popup"));
        table.waitUntilContainsText(name);
        new Popup(webDriverClient, "participant-popup").close();
    }

    public boolean contains(String participantName) {
        table.search(participantName);
        return table.containsText(participantName);
    }

    public void importCsv(String filePath, String expectedParticipantName) {
        table.clickAction("button-plus");
        webDriverClient.findVisible(By.id("participant-csv-file-input")).sendKeys(filePath);
        table.waitUntilContainsText(expectedParticipantName);
    }

    public void openStatistics(String participantName) {
        table.search(participantName);
        table.selectRowContaining(participantName);
        webDriverClient.waitUntilClickable(By.xpath("//button[.//mat-icon[@svgIcon='bar_chart']]")).click();
        webDriverClient.findVisible(By.cssSelector(".statistics-view .player .name"));
    }

    public boolean containsStatisticValue(String value) {
        return webDriverClient.findAll(By.cssSelector(".statistics-view .right-column")).stream()
                .anyMatch(column -> column.getText().equals(value));
    }
}
