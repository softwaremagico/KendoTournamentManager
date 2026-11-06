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
 * %%
 * #L%
 */

import com.softwaremagico.kt.selenium.components.Dropdown;
import com.softwaremagico.kt.selenium.components.InputField;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

public class TournamentTypePropertiesTest extends AdvancedBrowserTest {
    private static final List<String> PROPERTY_FIELD_IDS = List.of(
            "tournament-maximize-fights",
            "tournament-avoid-duplicate-fights",
            "tournament-league-fifo",
            "tournament-resolve-odd-fights-asap",
            "tournament-draw-resolutions",
            "tournament-rung",
            "tournament-swiss-avoid-repeated-pairings",
            "tournament-swiss-tie-break",
            "swiss-rounds"
    );

    @Test
    public void showsAndSavesPropertiesForEachApplicableTournamentType() {
        final String suffix = Long.toString(System.currentTimeMillis());
        tournaments.open(URL);

        assertProperties(suffix, "Championship",
                List.of("tournament-maximize-fights", "tournament-league-fifo", "tournament-resolve-odd-fights-asap"),
                Map.of(
                        "MAXIMIZE_FIGHTS", "false",
                        "LEAGUE_FIGHTS_ORDER_GENERATION", "FIFO",
                        "ODD_FIGHTS_RESOLVED_ASAP", "true"
                ));
        assertProperties(suffix, "League (Saotori shiai)",
                List.of("tournament-league-fifo"),
                Map.of(
                        "MAXIMIZE_FIGHTS", "false",
                        "LEAGUE_FIGHTS_ORDER_GENERATION", "FIFO"
                ));
        assertProperties(suffix, "Loop (Saotori shiai without rest)",
                List.of("tournament-avoid-duplicate-fights"),
                Map.of("AVOID_DUPLICATES", "true"));
        assertProperties(suffix, "Kachinuki",
                List.of("tournament-draw-resolutions"),
                Map.of("KING_DRAW_RESOLUTION", "BOTH_ELIMINATED"));
        assertProperties(suffix, "Dethrone the King Tournament",
                List.of("tournament-draw-resolutions"),
                Map.of("KING_DRAW_RESOLUTION", "BOTH_ELIMINATED"));
        assertProperties(suffix, "Senbatsu",
                List.of("tournament-rung"),
                Map.of("SENBATSU_CHALLENGE_DISTANCE", "3"));
        assertProperties(suffix, "Swiss System",
                List.of("tournament-swiss-avoid-repeated-pairings", "tournament-swiss-tie-break", "swiss-rounds"),
                Map.of(
                        "SWISS_AVOID_REPEATED_PAIRINGS", "true",
                        "SWISS_TIE_BREAK_RULE", "BUCHHOLZ"
                ));
        assertProperties(suffix, "Custom", List.of(), Map.of());
    }

    private void assertProperties(String suffix, String type, List<String> visibleFields, Map<String, String> expectedProperties) {
        final String name = "P " + type + " " + suffix;
        client.clickFirstVisible(By.id("button-plus"));
        new InputField(client, "tournament-name").setValue(name);
        new Dropdown(client, "tournament-type").select(type);

        for (String fieldId : PROPERTY_FIELD_IDS) {
            final List<WebElement> fields = driver.findElements(By.id(fieldId));
            final boolean isVisible = fields.stream().anyMatch(WebElement::isDisplayed);
            Assert.assertEquals(isVisible, visibleFields.contains(fieldId),
                    "Unexpected visibility of " + fieldId + " for tournament type " + type);
        }

        final String propertyDropdown = List.of(
                        "tournament-draw-resolutions",
                        "tournament-rung",
                        "tournament-swiss-tie-break"
                ).stream()
                .filter(visibleFields::contains)
                .findFirst()
                .orElse(null);
        if (propertyDropdown != null) {
            final org.openqa.selenium.JavascriptExecutor script = (org.openqa.selenium.JavascriptExecutor) driver;
            final Number propertyWidth = (Number) script.executeScript(
                    "return document.getElementById(arguments[0]).getBoundingClientRect().width", propertyDropdown);
            final Number durationWidth = (Number) script.executeScript(
                    "return document.getElementById('tournament-duration').getBoundingClientRect().width");
            Assert.assertEquals(Math.round(propertyWidth.doubleValue()), Math.round(durationWidth.doubleValue()),
                    "Tournament property dropdown should align with the main form for " + type);
        }

        client.clickWizardryButton(By.id("tournament-button-save"));
        client.getWebDriverWait()
                .withMessage(() -> driver.findElement(By.id("tournament-popup")).getText())
                .until(browser -> browser.findElements(By.id("tournament-popup")).stream().noneMatch(WebElement::isDisplayed));
        new com.softwaremagico.kt.selenium.components.Table(client, "tournaments-table").waitUntilContainsText(name);

        final Map<String, Object> tournamentRow = list(api("GET", "/tournaments", null)).stream()
                .filter(row -> name.equals(row.get("name"))).findFirst().orElseThrow();
        final int id = number(tournamentRow, "id");
        final List<Map<String, Object>> savedProperties =
                list(api("GET", "/tournaments/properties/tournaments/" + id, null));
        final Map<String, String> actualProperties = savedProperties.stream().collect(
                java.util.stream.Collectors.toMap(
                        property -> (String) property.get("propertyKey"),
                        property -> (String) property.get("propertyValue")
                ));

        Assert.assertEquals(actualProperties, expectedProperties,
                "Saved properties for tournament type " + type);
    }
}
