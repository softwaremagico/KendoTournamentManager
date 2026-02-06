package com.softwaremagico.kt.core.controller;

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

import com.softwaremagico.kt.core.controller.models.TournamentExtraPropertyDTO;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.TournamentExtraPropertyConverter;
import com.softwaremagico.kt.core.converters.models.TournamentExtraPropertyConverterRequest;
import com.softwaremagico.kt.core.exceptions.TournamentNotFoundException;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.TournamentExtraPropertyProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.core.tournaments.SenbatsuTournamentHandler;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.repositories.TournamentExtraPropertyRepository;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class TournamentExtraPropertyController extends BasicInsertableController<TournamentExtraProperty, TournamentExtraPropertyDTO,
        TournamentExtraPropertyRepository, TournamentExtraPropertyProvider, TournamentExtraPropertyConverterRequest, TournamentExtraPropertyConverter> {

    private final TournamentProvider tournamentProvider;
    private final TournamentConverter tournamentConverter;
    private final GroupProvider groupProvider;


    @Autowired
    protected TournamentExtraPropertyController(TournamentExtraPropertyProvider provider, TournamentExtraPropertyConverter converter,
                                                TournamentProvider tournamentProvider, TournamentConverter tournamentConverter,
                                                GroupProvider groupProvider) {
        super(provider, converter);
        this.tournamentProvider = tournamentProvider;
        this.tournamentConverter = tournamentConverter;
        this.groupProvider = groupProvider;
    }

    @Override
    protected TournamentExtraPropertyConverterRequest createConverterRequest(TournamentExtraProperty tournamentExtraProperty) {
        return new TournamentExtraPropertyConverterRequest(tournamentExtraProperty);
    }

    @Override
    public TournamentExtraPropertyDTO update(TournamentExtraPropertyDTO dto, String username, String session) {
        final TournamentExtraProperty tournamentExtraProperty = getProvider()
                .getByTournamentAndProperty(tournamentConverter.reverse(dto.getTournament()), dto.getPropertyKey());
        if (tournamentExtraProperty != null) {
            dto.setId(tournamentExtraProperty.getId());
        }
        //Remove any existing group if changed.
        if (dto.getPropertyKey() == TournamentExtraPropertyKey.ODD_FIGHTS_RESOLVED_ASAP) {
            groupProvider.delete(tournamentConverter.reverse(dto.getTournament()));
        }
        return super.update(dto, username, session);
    }

    public List<TournamentExtraPropertyDTO> getByTournamentId(Integer tournamentId) {
        return convertAll(getProvider().getAll(tournamentProvider.get(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(getClass(), "No tournament found with id '" + tournamentId + "'."))));
    }

    public TournamentExtraPropertyDTO getByTournamentAndProperty(Integer tournamentId, TournamentExtraPropertyKey key) {
        if (key == TournamentExtraPropertyKey.SENBATSU_CHALLENGE_DISTANCE) {
            return convert(getProvider().getByTournamentAndProperty(tournamentProvider.get(tournamentId)
                            .orElseThrow(() -> new TournamentNotFoundException(getClass(), "No tournament found with id '" + tournamentId + "'.")), key,
                    SenbatsuTournamentHandler.DEFAULT_CHALLENGE_DISTANCE));
        }
        return convert(getProvider().getByTournamentAndProperty(tournamentProvider.get(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(getClass(), "No tournament found with id '" + tournamentId + "'.")), key));
    }

    public List<TournamentExtraPropertyDTO> getLatest(String username) {
        return convertAll(getProvider().getLatestPropertiesByCreatedBy(username));
    }

}
