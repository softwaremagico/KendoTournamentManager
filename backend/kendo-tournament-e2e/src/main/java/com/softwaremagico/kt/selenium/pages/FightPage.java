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
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page object for generating and resolving tournament fights.
 */
public class FightPage {
    private final WebDriverClient webDriverClient;

    public FightPage(WebDriverClient webDriverClient) {
        this.webDriverClient = webDriverClient;
    }

    public void generateLeagueFights() {
        webDriverClient.waitUntilClickable(By.xpath("//button[.//mat-icon[@svgIcon='wand']]")).click();
        webDriverClient.waitUntilClickable(By.cssSelector("#league-generator-popup .sorted-button")).click();
        webDriverClient.waitUntilClickable(By.cssSelector("#league-generator-popup .fight-button")).click();
        webDriverClient.waitUntilInvisible(By.id("league-generator-popup"));
        webDriverClient.findVisible(By.cssSelector("fight"));
    }

    public void resolveAllFightsFor(String winningTeam) {
        for (WebElement fight : webDriverClient.findAll(By.cssSelector("fight"))) {
            final boolean leftWins = fight.findElement(By.cssSelector(".left-team")).getText().equals(winningTeam);
            for (WebElement duel : fight.findElements(By.cssSelector("duel"))) {
                scoreAndFinish(duel, leftWins);
            }
        }
    }

    public String getWinner() {
        webDriverClient.waitUntilClickable(By.xpath("//button[.//mat-icon[@svgIcon='teams-classification']]")).click();
        return webDriverClient.findVisible(By.cssSelector("#teams-ranking-popup .team-ranking-table tbody tr:first-child .team-name"))
                .getText();
    }

    private void scoreAndFinish(WebElement duel, boolean leftWins) {
        final List<WebElement> userScores = duel.findElements(By.cssSelector("user-score"));
        final List<WebElement> scores = userScores.get(leftWins ? 0 : 1).findElements(By.cssSelector("score"));
        for (int scoreIndex = 0; scoreIndex < 2; scoreIndex++) {
            scores.get(scoreIndex).findElement(By.cssSelector(".score-area")).click();
            webDriverClient.waitUntilClickable(By.cssSelector(".mat-mdc-menu-panel button[mat-menu-item]")).click();
        }
        duel.click();
        webDriverClient.waitUntilClickable(By.xpath("//button[.//mat-icon[@svgIcon='check']]")).click();
    }
}
