package com.softwaremagico.kt.core.controller;

/*-
 * #%L
 * Kendo Participant Manager (Core)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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

import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantStatisticsDTO;
import com.softwaremagico.kt.core.converters.ParticipantConverter;
import com.softwaremagico.kt.core.converters.ParticipantStatisticsConverter;
import com.softwaremagico.kt.core.converters.models.ParticipantStatisticsConverterRequest;
import com.softwaremagico.kt.core.providers.ParticipantStatisticsProvider;
import com.softwaremagico.kt.core.statistics.ParticipantStatistics;
import com.softwaremagico.kt.core.statistics.ParticipantStatisticsRepository;
import org.springframework.stereotype.Controller;

@Controller
public class ParticipantStatisticsController extends BasicInsertableController<ParticipantStatistics, ParticipantStatisticsDTO,
        ParticipantStatisticsRepository, ParticipantStatisticsProvider, ParticipantStatisticsConverterRequest,
        ParticipantStatisticsConverter> {


    private final ParticipantConverter participantConverter;

    protected ParticipantStatisticsController(ParticipantStatisticsProvider provider, ParticipantStatisticsConverter converter,
                                              ParticipantConverter participantConverter) {
        super(provider, converter);
        this.participantConverter = participantConverter;
    }

    @Override
    protected ParticipantStatisticsConverterRequest createConverterRequest(ParticipantStatistics participantStatistics) {
        return new ParticipantStatisticsConverterRequest(participantStatistics);
    }

    public ParticipantStatisticsDTO get(ParticipantDTO participantDTO) {
        return getConverter().convert(new ParticipantStatisticsConverterRequest(getProvider().get(participantConverter.reverse(participantDTO))));
    }

    public ParticipantStatisticsDTO getPrevious(ParticipantDTO participantDTO) {
        return getConverter().convert(new ParticipantStatisticsConverterRequest(getProvider().get(participantConverter.reverse(participantDTO))));
    }
}
