package com.softwaremagico.kt.core.converters;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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

import com.softwaremagico.kt.core.controller.models.TournamentStatisticsDTO;
import com.softwaremagico.kt.core.converters.models.TournamentFightStatisticsConverterRequest;
import com.softwaremagico.kt.core.converters.models.TournamentStatisticsConverterRequest;
import com.softwaremagico.kt.core.statistics.TournamentStatistics;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class TournamentStatisticsConverter extends ElementConverter<TournamentStatistics, TournamentStatisticsDTO, TournamentStatisticsConverterRequest> {

    private final TournamentFightStatisticsConverter tournamentFightStatisticsConverter;

    public TournamentStatisticsConverter(TournamentFightStatisticsConverter tournamentFightStatisticsConverter) {
        this.tournamentFightStatisticsConverter = tournamentFightStatisticsConverter;
    }

    @Override
    protected TournamentStatisticsDTO convertElement(TournamentStatisticsConverterRequest from) {
        if (from == null) {
            return null;
        }
        final TournamentStatisticsDTO tournamentStatisticsDTO = new TournamentStatisticsDTO();
        BeanUtils.copyProperties(from.getEntity(), tournamentStatisticsDTO, ConverterUtils.getNullPropertyNames(from.getEntity()));
        tournamentStatisticsDTO.setTournamentFightStatistics(tournamentFightStatisticsConverter.convertElement(
                new TournamentFightStatisticsConverterRequest(from.getEntity().getFightStatistics())));
        return tournamentStatisticsDTO;
    }

    @Override
    public TournamentStatistics reverse(TournamentStatisticsDTO to) {
        if (to == null) {
            return null;
        }
        final TournamentStatistics tournamentStatistics = new TournamentStatistics();
        BeanUtils.copyProperties(to, tournamentStatistics, ConverterUtils.getNullPropertyNames(to));
        tournamentStatistics.setFightStatistics(tournamentFightStatisticsConverter.reverse(to.getTournamentFightStatistics()));
        return tournamentStatistics;
    }
}
