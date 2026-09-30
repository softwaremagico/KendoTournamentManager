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

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Small waiting wrapper shared by page objects.
 */
public class WebDriverClient {
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration DRAG_START_DELAY = Duration.ofMillis(200);
    private static final Duration DRAG_END_DELAY = Duration.ofMillis(400);

    private final WebDriver driver;
    private final WebDriverWait wait;

    public WebDriverClient(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, WAIT_TIMEOUT);
    }

    public WebDriver getDriver() {
        return driver;
    }

    public WebDriverWait getWebDriverWait() {
        return wait;
    }

    public WebElement findVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public void waitUntilInvisible(By locator) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public WebElement waitUntilClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public void waitForAtLeast(By locator, int count) {
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(locator, count - 1));
    }

    public boolean isVisible(By locator) {
        return !driver.findElements(locator).isEmpty() && driver.findElement(locator).isDisplayed();
    }

    public List<WebElement> findAll(By locator) {
        return driver.findElements(locator);
    }

    public void open(String url) {
        driver.get(url);
    }

    public void dragAndDrop(WebElement source, WebElement target) {
        new Actions(driver).moveToElement(source).pause(DRAG_START_DELAY).clickAndHold()
                .moveToElement(target).pause(DRAG_END_DELAY).release().perform();
    }

    public void clickWizardryButton(By locator) {
        final WebElement button = findVisible(locator);
        final List<WebElement> buttonBases = button.findElements(By.cssSelector(".button-base"));
        if (buttonBases.isEmpty()) {
            waitUntilClickable(locator).click();
        } else {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", buttonBases.getFirst());
        }
    }
}
