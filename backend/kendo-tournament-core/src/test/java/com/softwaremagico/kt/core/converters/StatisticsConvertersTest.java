package com.softwaremagico.kt.core.converters;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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

import com.softwaremagico.kt.core.controller.models.ParticipantFightStatisticsDTO;
import com.softwaremagico.kt.core.controller.models.TournamentFightStatisticsDTO;
import com.softwaremagico.kt.core.statistics.ParticipantFightStatistics;
import com.softwaremagico.kt.core.statistics.TournamentFightStatistics;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Test(groups = {"converterTests"})
public class StatisticsConvertersTest {

    private final ParticipantFightStatisticsConverter participantFightStatisticsConverter =
            new ParticipantFightStatisticsConverter();

    private final TournamentFightStatisticsConverter tournamentFightStatisticsConverter =
            new TournamentFightStatisticsConverter();

    @Test
    public void testParticipantFightStatisticsReverseReturnsNullOnNullInput() {
        assertThat(participantFightStatisticsConverter.reverse(null)).isNull();
    }

    @Test
    public void testParticipantFightStatisticsReverseCopiesValuesOnNonNullInput() {
        final ParticipantFightStatisticsDTO dto = new ParticipantFightStatisticsDTO();
        dto.setDuelsNumber(12L);
        dto.setMenNumber(4L);
        dto.setKoteNumber(3L);

        final ParticipantFightStatistics result = participantFightStatisticsConverter.reverse(dto);

        assertThat(result).isNotNull();
        assertThat(result.getDuelsNumber()).isEqualTo(12L);
        assertThat(result.getMenNumber()).isEqualTo(4L);
        assertThat(result.getKoteNumber()).isEqualTo(3L);
    }

    @Test
    public void testTournamentFightStatisticsReverseReturnsNullOnNullInput() {
        assertThat(tournamentFightStatisticsConverter.reverse(null)).isNull();
    }

    @Test
    public void testTournamentFightStatisticsReverseCopiesValuesOnNonNullInput() {
        final TournamentFightStatisticsDTO dto = new TournamentFightStatisticsDTO();
        dto.setFightsNumber(21L);
        dto.setDuelsNumber(63L);
        dto.setMenNumber(18L);

        final TournamentFightStatistics result = tournamentFightStatisticsConverter.reverse(dto);

        assertThat(result).isNotNull();
        assertThat(result.getFightsNumber()).isEqualTo(21L);
        assertThat(result.getDuelsNumber()).isEqualTo(63L);
        assertThat(result.getMenNumber()).isEqualTo(18L);
    }
}

