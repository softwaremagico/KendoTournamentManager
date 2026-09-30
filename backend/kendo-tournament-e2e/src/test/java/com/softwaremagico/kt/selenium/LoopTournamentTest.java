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
 * Covers the home-and-away fixture generation of {@code LoopLeagueTest}.
 */
public class LoopTournamentTest extends AbstractTournamentTypeTest {
    private static final int TEAM_COUNT = 3;
    private static final int EXPECTED_FIGHTS = TEAM_COUNT * (TEAM_COUNT - 1);

    @Test
    public void createsAndResolvesLoopTournament() {
        final String[] competitors = createIndividualTeams("Loop", TEAM_COUNT);
        createTournamentWithIndividualTeams("Loop", "Loop", competitors);

        fightPage.generateOrderedFights();
        Assert.assertEquals(fightPage.getFightCount(), EXPECTED_FIGHTS);
        fightPage.resolveAllFightsFor(WINNING_TEAM);
        Assert.assertEquals(fightPage.getWinner(), WINNING_TEAM);
    }
}
