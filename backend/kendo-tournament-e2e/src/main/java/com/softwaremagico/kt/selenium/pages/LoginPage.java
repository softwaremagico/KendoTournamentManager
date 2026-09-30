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
import org.openqa.selenium.By;

/**
 * Page object for the public login route.
 */
public class LoginPage {
    private final WebDriverClient webDriverClient;
    private final InputField username;
    private final InputField password;

    public LoginPage(WebDriverClient webDriverClient) {
        this.webDriverClient = webDriverClient;
        this.username = new InputField(webDriverClient, "login-username");
        this.password = new InputField(webDriverClient, "login-password");
    }

    public void open(String baseUrl) {
        webDriverClient.open(baseUrl + "/#/login");
        webDriverClient.findVisible(By.id("login"));
    }

    public void setUsername(String usernameValue) {
        username.setValue(usernameValue);
    }

    public void login(String usernameValue, String passwordValue) {
        setUsername(usernameValue);
        password.setValue(passwordValue);
        webDriverClient.findVisible(By.id("login-button")).click();
        webDriverClient.waitUntilInvisible(By.id("login"));
        webDriverClient.findVisible(By.id("navbar"));
    }

    public boolean isDisplayed() {
        return !webDriverClient.findAll(By.id("login")).isEmpty();
    }

    public String getUsername() {
        return username.getValue();
    }
}
