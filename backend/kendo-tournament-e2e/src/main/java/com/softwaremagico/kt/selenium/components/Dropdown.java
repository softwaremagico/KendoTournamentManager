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
import org.openqa.selenium.interactions.Actions;

/**
 * Represents a WizardryTheme dropdown and its filterable options.
 */
public class Dropdown {
    private final WebDriverClient webDriverClient;
    private final By componentLocator;
    private final String componentId;

    public Dropdown(WebDriverClient webDriverClient, String componentId) {
        this.webDriverClient = webDriverClient;
        this.componentId = componentId;
        this.componentLocator = By.id(componentId);
    }

    public void select(String label) {
        webDriverClient.findVisible(componentLocator);
        final WebElement component = webDriverClient.findAll(componentLocator).stream().filter(WebElement::isDisplayed)
                .reduce((first, second) -> second)
                .orElseThrow(() -> new IllegalStateException("No visible dropdown component found for '" + componentLocator + "'."));
        final WebElement input = component.findElement(By.cssSelector("input"));
        new Actions(webDriverClient.getDriver()).moveToElement(input).click().perform();
        webDriverClient.getWebDriverWait().until(driver -> component.findElement(By.cssSelector(".dropdown-list"))
                .getAttribute("class").contains("dropdown-open"));
        final By optionLocator = By.xpath("//button[@role='option'][contains(normalize-space(), "
                + toXPathLiteral(label) + ")]");
        webDriverClient.waitUntilClickable(optionLocator).click();
    }

    private String toXPathLiteral(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }
        return "concat('" + value.replace("'", "', \"'\", '") + "')";
    }

}
