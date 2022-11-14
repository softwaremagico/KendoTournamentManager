package com.softwaremagico.kt.core.controller;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2022 Softwaremagico
 * %%
 * This software is designed by Jorge Hortelano Otero. Jorge Hortelano Otero
 * <softwaremagico@gmail.com> Valencia (Spain).
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.softwaremagico.kt.core.controller.models.DuelDTO;
import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.DuelConverter;
import com.softwaremagico.kt.core.converters.GroupConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.models.DuelConverterRequest;
import com.softwaremagico.kt.core.converters.models.GroupConverterRequest;
import com.softwaremagico.kt.core.exceptions.TournamentNotFoundException;
import com.softwaremagico.kt.core.exceptions.ValidateBadRequestException;
import com.softwaremagico.kt.core.providers.DuelProvider;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.logger.ExceptionType;
import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.repositories.DuelRepository;
import com.softwaremagico.kt.persistence.values.Score;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.stream.Collectors;

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
        if (dto.getCompetitor1Score().contains(Score.EMPTY) ||
                dto.getCompetitor1Score().contains(Score.DRAW) ||
                dto.getCompetitor1Score().contains(Score.FAULT)) {
            throw new ValidateBadRequestException(this.getClass(), "Invalid score on duel '" + dto + "'");
        }
        if (dto.getCompetitor2Score().contains(Score.EMPTY) ||
                dto.getCompetitor2Score().contains(Score.DRAW) ||
                dto.getCompetitor2Score().contains(Score.FAULT)) {
            throw new ValidateBadRequestException(this.getClass(), "Invalid score on duel '" + dto + "'");
        }
    }

    public List<DuelDTO> getUntiesFromGroup(Integer groupId) {
        final Group group = groupProvider.getGroup(groupId);
        final GroupDTO groupDTO = groupConverter.convert(new GroupConverterRequest(group));
        return groupDTO.getUnties();
    }

    public List<DuelDTO> getUntiesFromTournament(Integer tournamentId) {
        final List<Group> groups = groupProvider.getGroups(tournamentProvider.get(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(getClass(), "No tournament found with id '" + tournamentId + "',",
                        ExceptionType.INFO)));
        final List<GroupDTO> groupDTO = groupConverter.convertAll(groups.stream()
                .map(GroupConverterRequest::new).collect(Collectors.toList()));
        return groupDTO.stream().flatMap(group -> group.getUnties().stream()).collect(Collectors.toList());
    }

    public long count(TournamentDTO tournament) {
        return provider.count(tournamentConverter.reverse(tournament));
    }

}
