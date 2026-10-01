package com.softwaremagico.kt.selenium.components;

/*-
 * #%L
 * Kendo Tournament Manager (End-to-End Tests)
 * %%
 * Copyright (C) 2021 - 2026 Softwaremagico
 * %%
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
import org.openqa.selenium.Keys;

import java.util.List;

/**
 * Provides stable table access independently from test-specific selectors.
 */
public class Table {
    private final WebDriverClient webDriverClient;
    private final By tableLocator;

    public Table(WebDriverClient webDriverClient, String tableId) {
        this.webDriverClient = webDriverClient;
        this.tableLocator = By.id(tableId);
    }

    public int getRowCount() {
        return getRows().size();
    }

    public String getCellText(int row, int column) {
        return getRows().get(row).findElements(By.cssSelector(".datatable-body-cell")).get(column).getText();
    }

    public void selectRow(int row) {
        getRows().get(row).click();
    }

    public void selectRowContaining(String text) {
        webDriverClient.getWebDriverWait().until(driver -> {
            final List<WebElement> rows = getTable().findElements(By.cssSelector(".datatable-body-row"));
            for (WebElement row : rows) {
                if (row.getText().contains(text)) {
                    row.click();
                    return true;
                }
            }
            return false;
        });
    }

    public void clickAction(String actionId) {
        webDriverClient.clickFirstVisible(By.id(actionId));
    }

    public boolean containsText(String text) {
        return webDriverClient.getDriver().getPageSource().contains(text);
    }

    public void waitUntilContainsText(String text) {
        webDriverClient.getWebDriverWait().until(driver -> containsText(text));
    }

    public void search(String text) {
        final WebElement search = getTable().findElement(By.id("search"))
                .findElement(By.cssSelector("input"));
        search.clear();
        search.sendKeys(text);
        search.sendKeys(Keys.ENTER);
    }

    private List<WebElement> getRows() {
        return getTable().findElements(By.cssSelector(".datatable-body-row"));
    }

    private WebElement getTable() {
        webDriverClient.findVisible(tableLocator);
        return webDriverClient.findAll(tableLocator).stream().filter(WebElement::isDisplayed).findFirst()
                .orElseThrow(() -> new IllegalStateException("No visible table found for '" + tableLocator + "'."));
    }
}
