package com.softwaremagico.kt.core.controller;

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

import com.softwaremagico.kt.core.controller.models.DuelDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.DuelConverter;
import com.softwaremagico.kt.core.converters.GroupConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.models.DuelConverterRequest;
import com.softwaremagico.kt.core.exceptions.ValidateBadRequestException;
import com.softwaremagico.kt.core.providers.DuelProvider;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.repositories.DuelRepository;
import com.softwaremagico.kt.persistence.values.Score;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class DuelController extends BasicInsertableController<Duel, DuelDTO, DuelRepository,
        DuelProvider, DuelConverterRequest, DuelConverter> {

    private final GroupProvider groupProvider;

    private final GroupConverter groupConverter;

    private final TournamentProvider tournamentProvider;

    private final TournamentConverter tournamentConverter;

    @Autowired
    public DuelController(DuelProvider provider, DuelConverter converter, GroupProvider groupProvider, GroupConverter groupConverter,
                          TournamentProvider tournamentProvider, TournamentConverter tournamentConverter) {
        super(provider, converter);
        this.groupProvider = groupProvider;
        this.groupConverter = groupConverter;
        this.tournamentProvider = tournamentProvider;
        this.tournamentConverter = tournamentConverter;
    }

    @Override
    protected DuelConverterRequest createConverterRequest(Duel entity) {
        return new DuelConverterRequest(entity);
    }

    @Override
    public void validate(DuelDTO dto) throws ValidateBadRequestException {
        if (dto.getCompetitor1Score().contains(Score.EMPTY)
                || dto.getCompetitor1Score().contains(Score.DRAW)
                || dto.getCompetitor1Score().contains(Score.FAULT)) {
            throw new ValidateBadRequestException(this.getClass(), "Invalid score on duel '" + dto + "'");
        }
        if (dto.getCompetitor2Score().contains(Score.EMPTY)
                || dto.getCompetitor2Score().contains(Score.DRAW)
                || dto.getCompetitor2Score().contains(Score.FAULT)) {
            throw new ValidateBadRequestException(this.getClass(), "Invalid score on duel '" + dto + "'");
        }
    }

    @CacheEvict(allEntries = true, value = {"ranking", "competitors-ranking"})
    public DuelDTO update(DuelDTO duel, String username) {
        return super.update(duel, username);
    }

    public List<DuelDTO> getUntiesFromGroup(Integer groupId) {
        return convertAll(getProvider().getUntiesFromGroup(groupId));
    }

    public List<DuelDTO> getUntiesFromTournament(Integer tournamentId) {
        return convertAll(getProvider().getUntiesFromTournament(tournamentId));
    }

    public long count(TournamentDTO tournament) {
        return getProvider().count(tournamentConverter.reverse(tournament));
    }

    public void delete(TournamentDTO tournamentDTO) {
        getProvider().delete(tournamentConverter.reverse(tournamentDTO));
    }

}
