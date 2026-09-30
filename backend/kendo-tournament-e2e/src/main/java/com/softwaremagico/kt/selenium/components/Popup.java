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

/**
 * Represents a WizardryTheme popup identified by its host element id.
 */
public class Popup {
    private final WebDriverClient webDriverClient;
    private final By popupLocator;

    public Popup(WebDriverClient webDriverClient, String popupId) {
        this.webDriverClient = webDriverClient;
        this.popupLocator = By.id(popupId);
    }

    public void waitUntilClosed() {
        webDriverClient.waitUntilInvisible(popupLocator);
    }

    public void close() {
        webDriverClient.waitUntilClickable(By.cssSelector("#" + popupId() + " #popup-x-button")).click();
    }

    private String popupId() {
        return popupLocator.toString().replace("By.id: ", "");
    }
}
