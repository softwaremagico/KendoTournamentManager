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
import com.softwaremagico.kt.selenium.components.InputField;
import com.softwaremagico.kt.selenium.components.Popup;
import com.softwaremagico.kt.selenium.components.Table;

/**
 * Page object for the club registry.
 */
public class ClubPage {
    private final WebDriverClient webDriverClient;
    private final Table table;

    public ClubPage(WebDriverClient webDriverClient) {
        this.webDriverClient = webDriverClient;
        this.table = new Table(webDriverClient, "clubs-table");
    }

    public void open(String baseUrl) {
        webDriverClient.open(baseUrl + "/registry/clubs");
        table.getRowCount();
    }

    public void create(String name, String country, String city) {
        table.clickAction("button-plus");
        new InputField(webDriverClient, "club-name").setValue(name);
        new InputField(webDriverClient, "club-country").setValue(country);
        new InputField(webDriverClient, "club-city").setValue(city);
        webDriverClient.waitUntilClickable(org.openqa.selenium.By.id("club-button-save")).click();
        new Popup(webDriverClient, "club-popup").waitUntilClosed();
    }

    public boolean contains(String clubName) {
        table.search(clubName);
        return table.containsText(clubName);
    }
}
