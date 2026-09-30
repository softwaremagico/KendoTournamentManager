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
import com.softwaremagico.kt.selenium.components.Table;
import org.openqa.selenium.By;

/**
 * Page object for authenticated user administration.
 */
public class UserPage {
    private final WebDriverClient webDriverClient;
    private final Table table;

    public UserPage(WebDriverClient webDriverClient) {
        this.webDriverClient = webDriverClient;
        this.table = new Table(webDriverClient, "users-table");
    }

    public void open(String baseUrl) {
        webDriverClient.open(baseUrl + "/#/administration/users");
        table.getRowCount();
    }

    public void create(String username, String password) {
        table.clickAction("button-plus");
        new InputField(webDriverClient, "username").setValue(username);
        new InputField(webDriverClient, "name").setValue("Tenant");
        new InputField(webDriverClient, "lastname").setValue("User");
        new InputField(webDriverClient, "password").setValue(password);
        new InputField(webDriverClient, "repeat-password").setValue(password);
        webDriverClient.clickWizardryButton(By.id("user-button-save"));
        table.waitUntilContainsText(username);
    }
}
