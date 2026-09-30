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

import org.testng.annotations.Test;

/**
 * Verifies that Senbatsu tournaments use their challenge-constrained creator.
 */
public class SenbatsuTournamentTest extends AbstractTournamentTypeTest {
    private static final int TEAM_COUNT = 3;

    @Test
    public void opensSenbatsuChallengeCreator() {
        final String[] competitors = createIndividualTeams("Senbatsu", TEAM_COUNT);
        final String tournamentName = "Senbatsu Tournament " + System.currentTimeMillis();
        tournamentPage.open(BASE_URL);
        tournamentPage.createSenbatsu(tournamentName);
        tournamentPage.addCompetitors(competitors);
        tournamentPage.createTeams(TEAM_COUNT, TEAM_COUNT);
        tournamentPage.openFights();

        fightPage.openSenbatsuFightCreator();
    }
}
