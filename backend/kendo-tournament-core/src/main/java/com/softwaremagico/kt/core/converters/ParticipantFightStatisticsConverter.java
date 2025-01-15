package com.softwaremagico.kt.core.converters;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2025 Softwaremagico
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
import com.softwaremagico.kt.core.converters.models.ParticipantFightStatisticsConverterRequest;
import com.softwaremagico.kt.core.statistics.ParticipantFightStatistics;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class ParticipantFightStatisticsConverter extends ElementConverter<ParticipantFightStatistics, ParticipantFightStatisticsDTO,
        ParticipantFightStatisticsConverterRequest> {
    @Override
    protected ParticipantFightStatisticsDTO convertElement(ParticipantFightStatisticsConverterRequest from) {
        final ParticipantFightStatisticsDTO participantFightStatisticsDTO = new ParticipantFightStatisticsDTO();
        BeanUtils.copyProperties(from.getEntity(), participantFightStatisticsDTO, ConverterUtils.getNullPropertyNames(from.getEntity()));
        return participantFightStatisticsDTO;
    }

    @Override
    public ParticipantFightStatistics reverse(ParticipantFightStatisticsDTO to) {
        if (to == null) {
            return null;
        }
        final ParticipantFightStatistics participantFightStatistics = new ParticipantFightStatistics();
        BeanUtils.copyProperties(to, participantFightStatistics, ConverterUtils.getNullPropertyNames(to));
        return participantFightStatistics;
    }
}
