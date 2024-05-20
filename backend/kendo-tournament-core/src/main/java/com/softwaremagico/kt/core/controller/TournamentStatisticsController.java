package com.softwaremagico.kt.core.controller;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2024 Softwaremagico
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

import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.controller.models.TournamentStatisticsDTO;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.TournamentStatisticsConverter;
import com.softwaremagico.kt.core.converters.models.TournamentStatisticsConverterRequest;
import com.softwaremagico.kt.core.providers.TournamentStatisticsProvider;
import com.softwaremagico.kt.core.statistics.TournamentStatistics;
import com.softwaremagico.kt.core.statistics.TournamentStatisticsRepository;
import org.springframework.stereotype.Controller;

@Controller
public class TournamentStatisticsController extends BasicInsertableController<TournamentStatistics, TournamentStatisticsDTO, TournamentStatisticsRepository,
        TournamentStatisticsProvider, TournamentStatisticsConverterRequest, TournamentStatisticsConverter> {

    private final TournamentConverter tournamentConverter;

    protected TournamentStatisticsController(TournamentStatisticsProvider provider, TournamentStatisticsConverter converter,
                                             TournamentConverter tournamentConverter) {
        super(provider, converter);
        this.tournamentConverter = tournamentConverter;
    }

    @Override
    protected TournamentStatisticsConverterRequest createConverterRequest(TournamentStatistics tournamentStatistics) {
        return new TournamentStatisticsConverterRequest(tournamentStatistics);
    }

    public TournamentStatisticsDTO get(TournamentDTO tournamentDTO) {
        return convert(getProvider().get(tournamentConverter.reverse(tournamentDTO)));
    }

    public TournamentStatisticsDTO getPrevious(TournamentDTO tournamentDTO) {
        return convert(getProvider().get(tournamentConverter.reverse(tournamentDTO)));
    }
}
