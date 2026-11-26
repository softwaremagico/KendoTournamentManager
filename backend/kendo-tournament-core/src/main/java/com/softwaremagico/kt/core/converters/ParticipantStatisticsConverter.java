package com.softwaremagico.kt.core.converters;

/*-
 * #%L
 * Kendo Participant Manager (Core)
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

import com.softwaremagico.kt.core.controller.models.ParticipantStatisticsDTO;
import com.softwaremagico.kt.core.converters.models.ParticipantFightStatisticsConverterRequest;
import com.softwaremagico.kt.core.converters.models.ParticipantStatisticsConverterRequest;
import com.softwaremagico.kt.core.statistics.ParticipantStatistics;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class ParticipantStatisticsConverter extends ElementConverter<ParticipantStatistics, ParticipantStatisticsDTO,
        ParticipantStatisticsConverterRequest> {

    private final ParticipantFightStatisticsConverter participantFightStatisticsConverter;

    public ParticipantStatisticsConverter(ParticipantFightStatisticsConverter participantFightStatisticsConverter) {
        this.participantFightStatisticsConverter = participantFightStatisticsConverter;
    }

    @Override
    protected ParticipantStatisticsDTO convertElement(ParticipantStatisticsConverterRequest from) {
        if (from == null) {
            return null;
        }
        final ParticipantStatisticsDTO participantStatisticsDTO = new ParticipantStatisticsDTO();
        BeanUtils.copyProperties(from.getEntity(), participantStatisticsDTO, ConverterUtils.getNullPropertyNames(from.getEntity()));
        participantStatisticsDTO.setParticipantFightStatistics(participantFightStatisticsConverter.convertElement(
                new ParticipantFightStatisticsConverterRequest(from.getEntity().getFightStatistics())));
        return participantStatisticsDTO;
    }

    @Override
    public ParticipantStatistics reverse(ParticipantStatisticsDTO to) {
        if (to == null) {
            return null;
        }
        final ParticipantStatistics participantStatistics = new ParticipantStatistics();
        BeanUtils.copyProperties(to, participantStatistics, ConverterUtils.getNullPropertyNames(to));
        participantStatistics.setFightStatistics(participantFightStatisticsConverter.reverse(to.getParticipantFightStatistics()));
        return participantStatistics;
    }
}
