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

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    public void shouldGetScoreByKnownCharAbbreviation() {
        assertThat(Score.getScore('M')).isEqualTo(Score.MEN);
        assertThat(Score.getScore('K')).isEqualTo(Score.KOTE);
        assertThat(Score.getScore('D')).isEqualTo(Score.DO);
        assertThat(Score.getScore('T')).isEqualTo(Score.TSUKI);
        assertThat(Score.getScore('I')).isEqualTo(Score.IPPON);
        assertThat(Score.getScore('F')).isEqualTo(Score.FUSEN_GACHI);
        assertThat(Score.getScore('H')).isEqualTo(Score.HANSOKU);
        assertThat(Score.getScore(' ')).isEqualTo(Score.EMPTY);
    }

    @Test
    public void shouldGetScoreByUnknownChar_expect_empty() {
        assertThat(Score.getScore('Z')).isEqualTo(Score.EMPTY);
        assertThat(Score.getScore('0')).isEqualTo(Score.EMPTY);
    }

    @Test
    public void shouldGetScoreByKnownName() {
        assertThat(Score.getScore("Men")).isEqualTo(Score.MEN);
        assertThat(Score.getScore("Kote")).isEqualTo(Score.KOTE);
        assertThat(Score.getScore("Do")).isEqualTo(Score.DO);
        assertThat(Score.getScore("Tsuki")).isEqualTo(Score.TSUKI);
        assertThat(Score.getScore("Ippon")).isIn(Score.IPPON, Score.FUSEN_GACHI);
        assertThat(Score.getScore("Hansoku")).isEqualTo(Score.HANSOKU);
    }

    @Test
    public void shouldGetScoreByUnknownName_expect_empty() {
        assertThat(Score.getScore("unknown")).isEqualTo(Score.EMPTY);
        assertThat(Score.getScore((String) null)).isEqualTo(Score.EMPTY);
    }

    @Test
    public void shouldDetermineValidPoints() {
        assertThat(Score.isValidPoint(Score.MEN)).isTrue();
        assertThat(Score.isValidPoint(Score.KOTE)).isTrue();
        assertThat(Score.isValidPoint(Score.DO)).isTrue();
        assertThat(Score.isValidPoint(Score.TSUKI)).isTrue();
        assertThat(Score.isValidPoint(Score.IPPON)).isTrue();
        assertThat(Score.isValidPoint(Score.FUSEN_GACHI)).isTrue();
        assertThat(Score.isValidPoint(Score.HANSOKU)).isTrue();
        assertThat(Score.isValidPoint(Score.EMPTY)).isFalse();
        assertThat(Score.isValidPoint(Score.DRAW)).isFalse();
        assertThat(Score.isValidPoint(Score.FAULT)).isFalse();
    }

    @Test
    public void shouldReturnValidPointsList() {
        assertThat(Score.getValidPoints()).hasSize(7);
    }

    @Test
    public void shouldGetScoreAbbreviationsAndName() {
        assertThat(Score.MEN.getAbbreviation()).isEqualTo('M');
        assertThat(Score.MEN.getEnhancedAbbreviation()).isEqualTo('M');
        assertThat(Score.MEN.getPdfAbbreviation()).isEqualTo('M');
        assertThat(Score.MEN.getName()).isEqualTo("Men");

        assertThat(Score.FAULT.getAbbreviation()).isEqualTo('^');
        assertThat(Score.FAULT.getEnhancedAbbreviation()).isEqualTo('\u25B2');
        assertThat(Score.FAULT.getPdfAbbreviation()).isEqualTo('^');

        assertThat(Score.FUSEN_GACHI.getPdfAbbreviation()).isEqualTo(' ');
    }
}


