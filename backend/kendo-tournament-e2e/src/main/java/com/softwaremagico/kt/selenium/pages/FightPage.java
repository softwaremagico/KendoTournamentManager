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
    private static final int CHAMPIONSHIP_ADDITIONAL_GROUPS = 3;
    private static final int EIGHT_TEAM_CHAMPIONSHIP_FIGHT_COUNT = 7;
    private static final int EIGHT_TEAM_CHAMPIONSHIP_SEMIFINAL_FIGHT_COUNT = 6;

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

    public void generateOrderedFights() {
        generateLeagueFights();
    }

    public void generateChampionshipFights() {
        generateChampionshipFights(CHAMPIONSHIP_ADDITIONAL_GROUPS, false);
    }

    public void generateChampionshipWithTwoFirstRoundWinners(int additionalGroups) {
        generateChampionshipFights(additionalGroups, true);
    }

    private void generateChampionshipFights(int additionalGroups, boolean twoFirstRoundWinners) {
        webDriverClient.waitUntilClickable(By.xpath("//button[.//mat-icon[@svgIcon='brackets']]")).click();
        if (twoFirstRoundWinners) {
            webDriverClient.waitUntilClickable(By.xpath("//button[.//mat-icon[@svgIcon='one-winner']]")).click();
        }
        for (int index = 0; index < additionalGroups; index++) {
            webDriverClient.waitUntilClickable(By.xpath("//button[.//mat-icon[@svgIcon='plus']]")).click();
        }
        webDriverClient.waitUntilClickable(By.cssSelector("tournament-brackets-editor button[biit-button][secondary]")).click();
        webDriverClient.waitUntilClickable(By.xpath("//button[.//mat-icon[@svgIcon='wand']]")).click();
        webDriverClient.waitUntilClickable(By.id("confirm-delete-accept-button")).click();
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

    public int getFightCount() {
        return webDriverClient.findAll(By.cssSelector("fight")).size();
    }

    public void waitForFightCount(int count) {
        webDriverClient.waitForAtLeast(By.cssSelector("fight"), count);
    }

    public void openCustomFightCreator() {
        webDriverClient.waitUntilClickable(By.id("button-plus")).click();
        webDriverClient.findVisible(By.id("fight-creator"));
    }

    public void openSenbatsuFightCreator() {
        webDriverClient.waitUntilClickable(By.id("button-plus")).click();
        webDriverClient.findVisible(By.id("senbatsu-fight-creator"));
    }

    public void resolveFirstFightFor(String winningTeam) {
        final WebElement fight = webDriverClient.findVisible(By.cssSelector("fight"));
        final boolean leftWins = fight.findElement(By.cssSelector(".left-team")).getText().equals(winningTeam);
        for (WebElement duel : fight.findElements(By.cssSelector("duel"))) {
            scoreAndFinish(duel, leftWins);
        }
    }

    public void scoreFirstDuelFor(String winningTeam) {
        final WebElement fight = webDriverClient.findVisible(By.cssSelector("fight"));
        final boolean leftWins = fight.findElement(By.cssSelector(".left-team")).getText().equals(winningTeam);
        final WebElement duel = fight.findElement(By.cssSelector("duel"));
        final List<WebElement> userScores = duel.findElements(By.cssSelector("user-score"));
        userScores.get(leftWins ? 0 : 1).findElement(By.cssSelector("score .score-area")).click();
        webDriverClient.waitUntilClickable(By.cssSelector(".mat-mdc-menu-panel button[mat-menu-item]")).click();
    }

    public boolean containsScore(String score) {
        return webDriverClient.findAll(By.cssSelector("duel .point-value")).stream()
                .anyMatch(point -> point.getText().equals(score));
    }

    public void waitUntilContainsScore(String score) {
        webDriverClient.getWebDriverWait().until(driver -> containsScore(score));
    }

    public boolean hasReachedFinal() {
        return getFightCount() >= EIGHT_TEAM_CHAMPIONSHIP_FIGHT_COUNT;
    }

    public String getWinner() {
        webDriverClient.waitUntilClickable(By.xpath("//button[.//mat-icon[@svgIcon='teams-classification']]")).click();
        return webDriverClient.findVisible(By.cssSelector("#teams-ranking-popup .team-ranking-table tbody tr:first-child .team-name"))
                .getText();
    }

    public int getWinnerFightsWon() {
        return Integer.parseInt(webDriverClient.findVisible(
                By.cssSelector("#teams-ranking-popup .team-ranking-table tbody tr:first-child td:nth-child(3)"))
                .getText());
    }

    public void resolveChampionshipFor(String winningTeam) {
        resolveAllFightsFor(winningTeam);
        webDriverClient.waitForAtLeast(By.cssSelector("fight"), EIGHT_TEAM_CHAMPIONSHIP_SEMIFINAL_FIGHT_COUNT);
        resolveAllFightsFor(winningTeam);
        webDriverClient.waitForAtLeast(By.cssSelector("fight"), EIGHT_TEAM_CHAMPIONSHIP_FIGHT_COUNT);
        resolveAllFightsFor(winningTeam);
    }

    private void scoreAndFinish(WebElement duel, boolean leftWins) {
        if (duel.getAttribute("class").contains("over")) {
            return;
        }
        final List<WebElement> userScores = duel.findElements(By.cssSelector("user-score"));
        final List<WebElement> scores = userScores.get(leftWins ? 0 : 1).findElements(By.cssSelector("score"));
        for (int scoreIndex = 0; scoreIndex < 2; scoreIndex++) {
            scores.get(scoreIndex).findElement(By.cssSelector(".score-area")).click();
            webDriverClient.waitUntilClickable(By.cssSelector(".mat-mdc-menu-panel button[mat-menu-item]")).click();
        }
        duel.click();
        webDriverClient.waitUntilClickable(By.xpath("//button[.//mat-icon[@svgIcon='check']]")).click();
        closeRankingPopup();
    }

    private void closeRankingPopup() {
        if (webDriverClient.isVisible(By.id("teams-ranking-popup"))) {
            webDriverClient.waitUntilClickable(By.cssSelector("#teams-ranking-popup button[biit-button][primary]")).click();
        }
    }
}
