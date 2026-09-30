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
        getRows().stream().filter(row -> row.getText().contains(text)).findFirst()
                .orElseThrow(() -> new IllegalStateException("No table row contains '" + text + "'.")).click();
    }

    public void clickAction(String actionId) {
        webDriverClient.waitUntilClickable(By.id(actionId)).click();
    }

    public boolean containsText(String text) {
        return getRows().stream().anyMatch(row -> row.getText().contains(text));
    }

    public void search(String text) {
        final WebElement search = webDriverClient.findVisible(tableLocator).findElement(By.id("search"))
                .findElement(By.cssSelector("input"));
        search.clear();
        search.sendKeys(text);
        search.sendKeys(Keys.ENTER);
    }

    private List<WebElement> getRows() {
        return webDriverClient.findVisible(tableLocator).findElements(By.cssSelector(".datatable-body-row"));
    }
}
