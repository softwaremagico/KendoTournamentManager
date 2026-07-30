package com.softwaremagico.kt.core.converters;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.softwaremagico.kt.core.controller.models.ParticipantFightStatisticsDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantStatisticsDTO;
import com.softwaremagico.kt.core.converters.models.ParticipantStatisticsConverterRequest;
import com.softwaremagico.kt.core.statistics.ParticipantFightStatistics;
import com.softwaremagico.kt.core.statistics.ParticipantStatistics;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

@Test(groups = "participantStatisticsConverter")
public class ParticipantStatisticsConverterTest {

    @Mock
    private ParticipantFightStatisticsConverter mockParticipantFightStatisticsConverter;

    private ParticipantStatisticsConverter converter;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        converter = new ParticipantStatisticsConverter(mockParticipantFightStatisticsConverter);
    }

    @Test
    public void convert_withNullRequest_expectNull() {
        assertNull(converter.convert(null));
    }

    @Test
    public void convert_expectFightStatisticsConverted() {
        final ParticipantStatistics participantStatistics = new ParticipantStatistics();
        participantStatistics.setFightStatistics(new ParticipantFightStatistics());

        final ParticipantFightStatisticsDTO fightStatisticsDTO = new ParticipantFightStatisticsDTO();
        when(mockParticipantFightStatisticsConverter.convertElement(any())).thenReturn(fightStatisticsDTO);

        final ParticipantStatisticsDTO result = converter.convert(new ParticipantStatisticsConverterRequest(participantStatistics));

        assertSame(result.getParticipantFightStatistics(), fightStatisticsDTO);
    }

    @Test
    public void reverse_withNull_expectNull() {
        assertNull(converter.reverse(null));
    }

    @Test
    public void reverse_expectFightStatisticsReversed() {
        final ParticipantStatisticsDTO dto = new ParticipantStatisticsDTO();
        final ParticipantFightStatisticsDTO fightStatisticsDTO = new ParticipantFightStatisticsDTO();
        dto.setParticipantFightStatistics(fightStatisticsDTO);

        final ParticipantFightStatistics fightStatistics = new ParticipantFightStatistics();
        when(mockParticipantFightStatisticsConverter.reverse(fightStatisticsDTO)).thenReturn(fightStatistics);

        final ParticipantStatistics result = converter.reverse(dto);

        assertSame(result.getFightStatistics(), fightStatistics);
    }
}

