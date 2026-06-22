package com.softwaremagico.kt.persistence.values;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
 * %%
 * Copyright (C) 2021 - 2026 SoftwareMagico
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

@Test(groups = "persistenceValues")
public class PersistenceValuesCoverageTest {

    @Test
    public void shouldExposeEnumValues() {
        assertTrue(AchievementGrade.values().length > 0);
        assertTrue(AchievementType.values().length > 0);
        assertTrue(ImageCompression.values().length > 0);
        assertTrue(ImageFormat.values().length > 0);
        assertTrue(LeagueFightsOrder.values().length > 0);
        assertTrue(RoleType.values().length > 0);
        assertTrue(Score.values().length > 0);
        assertTrue(ScoreType.values().length > 0);
        assertTrue(TournamentExtraPropertyKey.values().length > 0);
        assertTrue(TournamentImageType.values().length > 0);
        assertTrue(TournamentType.values().length > 0);
    }

    @Test
    public void shouldResolveSomeEnumsByName() {
        Score.valueOf("IPPON");
        TournamentType.valueOf("LEAGUE");
        RoleType.valueOf("COMPETITOR");
    }
}


