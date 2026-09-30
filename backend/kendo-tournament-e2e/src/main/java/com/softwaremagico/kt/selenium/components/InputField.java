package com.softwaremagico.kt.selenium.components;

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
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Represents a Wizardry text input wrapped by its component id.
 */
public class InputField {
    private final WebDriverClient webDriverClient;
    private final By componentLocator;

    public InputField(WebDriverClient webDriverClient, String componentId) {
        this.webDriverClient = webDriverClient;
        this.componentLocator = By.id(componentId);
    }

    public void setValue(String value) {
        final WebElement input = getInput();
        input.clear();
        input.sendKeys(value);
    }

    public String getValue() {
        return getInput().getAttribute("value");
    }

    private WebElement getInput() {
        return webDriverClient.findVisible(componentLocator).findElement(By.cssSelector("input"));
    }
}
