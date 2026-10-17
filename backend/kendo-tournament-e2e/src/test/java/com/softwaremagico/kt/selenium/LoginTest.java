package com.softwaremagico.kt.selenium;

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

import com.softwaremagico.kt.selenium.pages.LoginPage;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Smoke test for the login form served by a running frontend instance.
 */
public class LoginTest {
    private WebDriver driver;
    private LoginPage loginPage;

    @BeforeClass
    public void setUp() {
        driver = ChromeDriverFactory.create(Boolean.parseBoolean(System.getProperty("selenium.headless", "true")));
        loginPage = new LoginPage(new WebDriverClient(driver));
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void loginFormRenders() {
        loginPage.open(System.getProperty("selenium.base-url", "http://localhost:4200"));
        loginPage.setUsername("invalid@example.test");

        Assert.assertTrue(loginPage.isDisplayed(), "The login page must be visible.");
        Assert.assertEquals(loginPage.getUsername(), "invalid@example.test");
    }
}
