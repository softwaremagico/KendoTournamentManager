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
import com.softwaremagico.kt.selenium.components.Table;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page object for platform tenant administration.
 */
public class TenantPage {
    private static final int TENANT_INPUT = 0;
    private static final int USERNAME_INPUT = 1;
    private static final int PASSWORD_INPUT = 2;
    private static final int NAME_INPUT = 3;
    private static final int LASTNAME_INPUT = 4;
    private final WebDriverClient webDriverClient;
    private final Table table;

    public TenantPage(WebDriverClient webDriverClient) {
        this.webDriverClient = webDriverClient;
        this.table = new Table(webDriverClient, "tenants-table");
    }

    public void open(String baseUrl) {
        webDriverClient.open(baseUrl + "/#/administration/tenants");
        table.getRowCount();
    }

    public void create(String tenantName, String username, String password) {
        table.clickAction("tenant-add");
        final List<WebElement> inputs = webDriverClient.findVisible(By.id("tenant-create-popup"))
                .findElements(By.cssSelector("biit-input-text input"));
        setInput(inputs.get(TENANT_INPUT), tenantName);
        setInput(inputs.get(USERNAME_INPUT), username);
        setInput(inputs.get(PASSWORD_INPUT), password);
        setInput(inputs.get(NAME_INPUT), "Tenant");
        setInput(inputs.get(LASTNAME_INPUT), "Administrator");
        webDriverClient.clickByScript(By.cssSelector("#tenant-create-popup button[biit-button][primary] .button-base"));
        table.waitUntilContainsText(tenantName);
    }

    private void setInput(WebElement input, String value) {
        input.clear();
        input.sendKeys(value);
    }
}
