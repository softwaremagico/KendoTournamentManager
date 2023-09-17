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

import com.softwaremagico.kt.core.controller.models.TournamentExtraPropertyDTO;
import com.softwaremagico.kt.core.converters.models.TournamentConverterRequest;
import com.softwaremagico.kt.core.converters.models.TournamentExtraPropertyConverterRequest;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import org.hibernate.LazyInitializationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.stereotype.Component;

@Component
public class TournamentExtraPropertyConverter extends ElementConverter<TournamentExtraProperty, TournamentExtraPropertyDTO,
        TournamentExtraPropertyConverterRequest> {
    private final TournamentConverter tournamentConverter;
    private final TournamentProvider tournamentProvider;

    public TournamentExtraPropertyConverter(TournamentConverter tournamentConverter, TournamentProvider tournamentProvider) {
        this.tournamentConverter = tournamentConverter;
        this.tournamentProvider = tournamentProvider;
    }

    @Override
    protected TournamentExtraPropertyDTO convertElement(TournamentExtraPropertyConverterRequest from) {
        final TournamentExtraPropertyDTO tournamentExtraPropertyDTO = new TournamentExtraPropertyDTO();
        BeanUtils.copyProperties(from.getEntity(), tournamentExtraPropertyDTO, ConverterUtils.getNullPropertyNames(from.getEntity()));
        try {
            tournamentExtraPropertyDTO.setTournament(tournamentConverter.convert(
                    new TournamentConverterRequest(from.getEntity().getTournament())));
        } catch (LazyInitializationException | InvalidPropertyException e) {
            tournamentExtraPropertyDTO.setTournament(tournamentConverter.convert(
                    new TournamentConverterRequest(tournamentProvider.get(from.getEntity().getTournament().getId()).orElse(null))));
        }
        return tournamentExtraPropertyDTO;
    }

    @Override
    public TournamentExtraProperty reverse(TournamentExtraPropertyDTO to) {
        if (to == null) {
            return null;
        }
        final TournamentExtraProperty tournamentExtraProperty = new TournamentExtraProperty();
        BeanUtils.copyProperties(to, tournamentExtraProperty, ConverterUtils.getNullPropertyNames(to));
        tournamentExtraProperty.setTournament(tournamentConverter.reverse(to.getTournament()));
        tournamentExtraProperty.setPropertyKey(to.getPropertyKey());
        return tournamentExtraProperty;
    }
}
