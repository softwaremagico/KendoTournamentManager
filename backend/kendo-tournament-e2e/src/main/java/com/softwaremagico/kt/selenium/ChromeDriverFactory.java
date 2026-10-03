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

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

/**
 * Creates browsers with the same deterministic viewport in local and CI runs.
 */
public final class ChromeDriverFactory {
    private static final Duration PAGE_LOAD_TIMEOUT = Duration.ofSeconds(30);

    private ChromeDriverFactory() {
    }

    /**
     * Creates a Chrome driver using Selenium Manager to resolve the driver binary.
     *
     * @param headless whether Chrome should run without a visible window.
     * @return configured browser driver.
     */
    public static WebDriver create(boolean headless) {
        final ChromeOptions options = new ChromeOptions();
        final String chromeBinary = System.getProperty("selenium.chrome.binary");
        if (chromeBinary != null && !chromeBinary.isBlank()) {
            options.setBinary(chromeBinary);
        }
        options.addArguments("--window-size=1920,1080", "--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu",
                "--disable-renderer-backgrounding", "--disable-background-timer-throttling", "--disable-features=CalculateNativeWinOcclusion");
        if (headless) {
            options.addArguments("--headless=new");
        }
        final WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(PAGE_LOAD_TIMEOUT);
        return driver;
    }
}
