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
 * Ensures a king winner remains on the court for the next challenger.
 */
public class KingOfTheMountainTournamentTest extends AbstractTournamentTypeTest {
    private static final int TEAM_COUNT = 3;
    private static final int FIRST_FIGHT_COUNT = 1;
    private static final int SECOND_FIGHT_COUNT = 2;

    @Test
    public void createsNextFightAfterKingWins() {
        final String[] competitors = createIndividualTeams("King", TEAM_COUNT);
        createTournamentWithIndividualTeams("King", "King of the Mountain", competitors);

        fightPage.generateOrderedFights();
        Assert.assertEquals(fightPage.getFightCount(), FIRST_FIGHT_COUNT);
        fightPage.resolveFirstFightFor(WINNING_TEAM);
        fightPage.waitForFightCount(SECOND_FIGHT_COUNT);
        Assert.assertEquals(fightPage.getFightCount(), SECOND_FIGHT_COUNT);
    }
}
