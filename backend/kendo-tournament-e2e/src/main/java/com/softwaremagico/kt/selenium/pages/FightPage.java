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
    private String resolvedWinningTeam;
    private String resolvedWinningParticipant;
    private boolean individualRanking;

    public FightPage(WebDriverClient webDriverClient) {
        this.webDriverClient = webDriverClient;
    }

    public void generateLeagueFights() {
        webDriverClient.waitUntilClickable(By.xpath("//button[.//mat-icon[@svgIcon='wand']]")).click();
        webDriverClient.clickWizardryButton(By.id("confirm-recreate-accept-button"));
        webDriverClient.clickWizardryButton(By.cssSelector("#league-generator-popup .sorted-button"));
        webDriverClient.clickWizardryButton(By.cssSelector("#league-generator-popup .fight-button"));
        webDriverClient.waitUntilInvisible(By.id("league-generator-popup"));
        webDriverClient.findVisible(By.cssSelector("fight"));
    }

    public void generateOrderedFights() {
        generateLeagueFights();
    }

    public void initializeSenbatsu() {
        webDriverClient.clickFirstVisible(By.xpath("//button[.//mat-icon[@svgIcon='wand']]"));
        webDriverClient.clickWizardryButton(By.id("confirm-recreate-accept-button"));
        webDriverClient.clickWizardryButton(By.cssSelector("#league-generator-popup .sorted-button"));
        webDriverClient.clickWizardryButton(By.cssSelector("#league-generator-popup .fight-button"));
        webDriverClient.waitUntilInvisible(By.id("league-generator-popup"));
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
        webDriverClient.findVisible(By.cssSelector("tournament-brackets .level-0 group-container"));
        for (int index = 0; index < additionalGroups; index++) {
            final By addGroup = By.cssSelector("app-tournament-generator button[biit-icon][icon='plus']");
            if (!webDriverClient.findVisible(addGroup).isEnabled()) {
                break;
            }
            final int previousGroups = webDriverClient.findAll(By.cssSelector("tournament-brackets .level-0 group-container")).size();
            webDriverClient.clickFirstVisible(addGroup);
            webDriverClient.waitForAtLeast(By.cssSelector("tournament-brackets .level-0 group-container"), previousGroups + 1);
        }
        webDriverClient.clickWizardryButton(By.id("brackets-sort-teams"));
        if (webDriverClient.isVisible(By.id("confirmation-delete-teams"))) {
            webDriverClient.clickWizardryButton(By.id("confirm-delete-accept-button"));
            webDriverClient.waitUntilInvisible(By.id("confirmation-delete-teams"));
        }
        webDriverClient.clickFirstVisible(By.xpath("//button[.//mat-icon[@svgIcon='wand']]"));
        webDriverClient.clickWizardryButton(By.id("confirm-delete-accept-button"));
        webDriverClient.findVisible(By.cssSelector("fight"));
    }

    public void resolveAllFightsFor(String winningTeam) {
        final int fightCount = getFightCount();
        for (int fightIndex = 0; fightIndex < fightCount; fightIndex++) {
            final int currentFight = fightIndex;
            final int duelCount = webDriverClient.findAll(By.cssSelector("fight")).get(currentFight)
                    .findElements(By.cssSelector("duel")).size();
            for (int duelIndex = 0; duelIndex < duelCount; duelIndex++) {
                final int currentDuel = duelIndex;
                webDriverClient.getWebDriverWait().until(driver -> {
                    final WebElement fight = driver.findElements(By.cssSelector("fight")).get(currentFight);
                    final WebElement duel = fight.findElements(By.cssSelector("duel")).get(currentDuel);
                    scoreAndFinish(duel, leftTeamWins(fight, winningTeam));
                    return true;
                });
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
        final boolean leftWins = leftTeamWins(fight, winningTeam);
        for (WebElement duel : fight.findElements(By.cssSelector("duel"))) {
            scoreAndFinish(duel, leftWins);
        }
    }

    public void scoreFirstDuelFor(String winningTeam) {
        webDriverClient.dismissSuccessNotifications();
        final WebElement fight = webDriverClient.findVisible(By.cssSelector("fight"));
        final boolean leftWins = leftTeamWins(fight, winningTeam);
        final WebElement duel = fight.findElement(By.cssSelector("duel"));
        final List<WebElement> userScores = duel.findElements(By.cssSelector("user-score"));
        userScores.get(leftWins ? 0 : 1).findElement(By.cssSelector("score .score-area")).click();
        clickMenScore();
    }

    public boolean containsScore(String score) {
        return webDriverClient.findAll(By.cssSelector("duel .point-value, duel .point")).stream()
                .anyMatch(point -> point.getText().equals(score));
    }

    public void waitUntilContainsScore(String score) {
        webDriverClient.getWebDriverWait().until(driver -> containsScore(score));
    }

    public boolean hasEditableScoreControls() {
        return !webDriverClient.findAll(By.cssSelector("duel score .score-area")).isEmpty();
    }

    public boolean hasReachedFinal() {
        return getFightCount() >= EIGHT_TEAM_CHAMPIONSHIP_FIGHT_COUNT;
    }

    public String getWinner() {
        closeRankingPopup();
        final By teamsButton = By.xpath("//button[.//mat-icon[@svgIcon='teams-classification']]");
        individualRanking = webDriverClient.findAll(teamsButton).stream().noneMatch(WebElement::isDisplayed);
        if (individualRanking) {
            if (webDriverClient.isVisible(By.id("competitors-ranking-popup"))) {
                return webDriverClient.findVisible(By.cssSelector("#competitors-ranking-popup .competitor-name")).getText()
                        .replaceAll("\\s*,\\s*", ", ");
            }
            openParticipantRanking();
            return webDriverClient.findVisible(By.cssSelector("#competitors-ranking-popup .competitor-name")).getText()
                    .replaceAll("\\s*,\\s*", ", ");
        }
        webDriverClient.getWebDriverWait().until(driver -> {
            if (webDriverClient.isVisible(By.id("teams-ranking-popup"))) {
                return true;
            }
            if (webDriverClient.isVisible(By.id("competitors-ranking-popup"))) {
                webDriverClient.clickWizardryButton(By.cssSelector("#competitors-ranking-popup .button-container > button:last-child"));
                return false;
            }
            try {
                webDriverClient.dismissSuccessNotifications();
                driver.findElement(By.xpath("//button[.//mat-icon[@svgIcon='teams-classification']]")).click();
            } catch (org.openqa.selenium.ElementClickInterceptedException exception) {
                return false;
            }
            return webDriverClient.isVisible(By.id("teams-ranking-popup"));
        });
        return webDriverClient.findVisible(By.cssSelector("#teams-ranking-popup .team-ranking-table tr .team-name")).getText();
    }

    public int getWinnerFightsWon() {
        if (individualRanking) {
            return Integer.parseInt(webDriverClient.findVisible(By.cssSelector(
                    "#competitors-ranking-popup tr:has(.competitor-name) td:nth-child(3)")).getText());
        }
        return Integer.parseInt(webDriverClient.findVisible(
                By.cssSelector("#teams-ranking-popup .team-ranking-table tr:has(.team-name) td:nth-child(3)"))
                .getText());
    }

    public String getResolvedWinningTeam() {
        return individualRanking ? resolvedWinningParticipant : resolvedWinningTeam;
    }

    public void resolveChampionshipFor(String winningTeam) {
        resolveAllFightsFor(winningTeam);
        webDriverClient.waitForAtLeast(By.cssSelector("fight"), EIGHT_TEAM_CHAMPIONSHIP_SEMIFINAL_FIGHT_COUNT);
        resolveAllFightsFor(winningTeam);
        webDriverClient.waitForAtLeast(By.cssSelector("fight"), EIGHT_TEAM_CHAMPIONSHIP_FIGHT_COUNT);
        resolveAllFightsFor(winningTeam);
    }

    private void scoreAndFinish(WebElement duel, boolean leftWins) {
        closeRankingPopup();
        webDriverClient.dismissSuccessNotifications();
        if (duel.getAttribute("class").contains("over")) {
            return;
        }
        final List<WebElement> userScores = duel.findElements(By.cssSelector("user-score"));
        final List<WebElement> scores = userScores.get(leftWins ? 0 : 1).findElements(By.cssSelector("score"));
        final boolean bye = userScores.stream().anyMatch(side -> side.findElement(By.cssSelector(".user-score"))
                .getAttribute("data-participant-name").isBlank());
        if (!bye) {
            for (int scoreIndex = 0; scoreIndex < 2; scoreIndex++) {
                if (scores.get(scoreIndex).getText().contains("M")) {
                    continue;
                }
                scores.get(scoreIndex).findElement(By.cssSelector(".score-area")).click();
                clickMenScore();
            }
        }
        webDriverClient.dismissSuccessNotifications();
        duel.click();
        webDriverClient.waitUntilClickable(By.xpath("//button[.//mat-icon[@svgIcon='check']]")).click();
        webDriverClient.getWebDriverWait().until(driver -> duel.getAttribute("class").contains("over"));
        closeRankingPopup();
    }

    private boolean leftTeamWins(WebElement fight, String winningTeam) {
        final WebElement container = fight.findElement(By.cssSelector(".fight-container"));
        final String left = container.getAttribute("data-left-team");
        final String right = container.getAttribute("data-right-team");
        final List<WebElement> participantSides = fight.findElements(By.cssSelector("user-score > .user-score"));
        if (!participantSides.isEmpty()) {
            final String leftParticipant = participantSides.get(0).getAttribute("data-participant-name");
            final String rightParticipant = participantSides.get(1).getAttribute("data-participant-name");
            if (leftParticipant.toLowerCase(java.util.Locale.ROOT).contains(winningTeam.toLowerCase(java.util.Locale.ROOT))) {
                return true;
            }
            if (rightParticipant.toLowerCase(java.util.Locale.ROOT).contains(winningTeam.toLowerCase(java.util.Locale.ROOT))) {
                return false;
            }
        }
        if (left.equalsIgnoreCase(winningTeam)) {
            resolvedWinningTeam = left;
            return true;
        }
        if (right.equalsIgnoreCase(winningTeam)) {
            resolvedWinningTeam = right;
            return false;
        }
        if (winningTeam.equals("Team 1")) {
            if (resolvedWinningTeam == null) {
                resolvedWinningTeam = left.compareToIgnoreCase(right) < 0 ? left : right;
                final List<WebElement> sides = fight.findElements(By.cssSelector("user-score > .user-score"));
                resolvedWinningParticipant = sides.get(left.equals(resolvedWinningTeam) ? 0 : 1).getAttribute("data-participant-name");
            }
            return left.equalsIgnoreCase(resolvedWinningTeam);
        }
        return false;
    }

    private void clickMenScore() {
        webDriverClient.dismissSuccessNotifications();
        final org.openqa.selenium.JavascriptExecutor scripts = (org.openqa.selenium.JavascriptExecutor) webDriverClient.getDriver();
        scripts.executeScript("""
                if (!window.e2eScoreTracking) {
                  window.e2eScoreTracking = true;
                  window.e2eScoreRequests = 0;
                  const send = XMLHttpRequest.prototype.send;
                  XMLHttpRequest.prototype.send = function(...args) {
                    this.addEventListener('loadend', () => {
                      if (this.responseURL.includes('/duels') && this.status >= 200 && this.status < 300) window.e2eScoreRequests++;
                    });
                    return send.apply(this, args);
                  };
                  const originalFetch = window.fetch;
                  window.fetch = async function(...args) {
                    const response = await originalFetch.apply(this, args);
                    if (response.url.includes('/duels') && response.ok) window.e2eScoreRequests++;
                    return response;
                  };
                }
                window.e2eScoreRequestBaseline = window.e2eScoreRequests;
                """);
        final By men = By.xpath("//div[contains(@class, 'mat-mdc-menu-panel')]//button[@mat-menu-item]"
                + "[normalize-space()='Men']");
        webDriverClient.getWebDriverWait().until(driver -> {
            final List<WebElement> options = driver.findElements(men).stream().filter(WebElement::isDisplayed).toList();
            if (options.isEmpty()) {
                return false;
            }
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", options.getFirst());
            return true;
        });
        webDriverClient.waitUntilInvisible(By.cssSelector(".mat-mdc-menu-panel"));
        webDriverClient.getWebDriverWait().until(driver -> Boolean.TRUE.equals(scripts.executeScript(
                "return window.e2eScoreRequests > window.e2eScoreRequestBaseline;")));
        webDriverClient.getWebDriverWait().until(driver -> driver.findElements(By.cssSelector("score .score-area[data-saving='true']")).isEmpty());
    }

    private void closeRankingPopup() {
        if (webDriverClient.isVisible(By.id("teams-ranking-popup"))) {
            webDriverClient.clickWizardryButton(By.id("teams-ranking-close"));
            webDriverClient.waitUntilInvisible(By.id("teams-ranking-popup"));
        }
        if (webDriverClient.isVisible(By.id("competitors-ranking-popup"))) {
            webDriverClient.clickWizardryButton(By.id("competitors-ranking-close"));
            webDriverClient.waitUntilInvisible(By.id("competitors-ranking-popup"));
        }
    }

    public void openParticipantRanking() {
        webDriverClient.getWebDriverWait().until(driver -> {
            if (webDriverClient.isVisible(By.id("competitors-ranking-popup"))) {
                return true;
            }
            try {
                driver.findElement(By.xpath("//button[.//mat-icon[@svgIcon='competitors-classification']]")).click();
            } catch (org.openqa.selenium.ElementClickInterceptedException exception) {
                return false;
            }
            return webDriverClient.isVisible(By.id("competitors-ranking-popup"));
        });
    }
}
