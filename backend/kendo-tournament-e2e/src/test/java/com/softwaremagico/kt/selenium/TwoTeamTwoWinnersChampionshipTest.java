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

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Covers the smallest championship tree configured with two first-round qualifiers.
 */
public class TwoTeamTwoWinnersChampionshipTest extends AbstractTournamentTypeTest {
    private static final int TEAM_COUNT = 2;
    private static final int INITIAL_GROUPS_ALREADY_CREATED = 0;
    private static final int EXPECTED_FIGHT_COUNT = 1;

    @Test
    public void twoTeamsWithTwoWinnersStillGenerateFinalFight() {
        final String[] competitors = createIndividualTeams("TwoWinnerMin", TEAM_COUNT);
        createTournamentWithIndividualTeams("TwoWinnerMin", "Championship", competitors);

        fightPage.generateChampionshipWithTwoFirstRoundWinners(INITIAL_GROUPS_ALREADY_CREATED);
        Assert.assertEquals(fightPage.getFightCount(), EXPECTED_FIGHT_COUNT,
                "The two-team championship must retain its only final fight when two winners are selected.");
    }
}
