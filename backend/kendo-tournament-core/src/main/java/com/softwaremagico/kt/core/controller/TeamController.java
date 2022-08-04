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

import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.ParticipantConverter;
import com.softwaremagico.kt.core.converters.TeamConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.models.TeamConverterRequest;
import com.softwaremagico.kt.core.exceptions.TournamentNotFoundException;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.repositories.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class TeamController extends BasicInsertableController<Team, TeamDTO, TeamRepository,
        TeamProvider, TeamConverterRequest, TeamConverter> {
    private final TournamentProvider tournamentProvider;
    private final TournamentConverter tournamentConverter;
    private final ParticipantConverter participantConverter;


    @Autowired
    public TeamController(TeamProvider provider, TeamConverter converter, TournamentProvider tournamentProvider,
                          TournamentConverter tournamentConverter, ParticipantConverter participantConverter) {
        super(provider, converter);
        this.tournamentProvider = tournamentProvider;
        this.tournamentConverter = tournamentConverter;
        this.participantConverter = participantConverter;
    }

    @Override
    protected TeamConverterRequest createConverterRequest(Team entity) {
        return new TeamConverterRequest(entity);
    }

    public List<TeamDTO> getAllByTournament(TournamentDTO tournamentDTO) {
        return converter.convertAll(provider.getAll(tournamentConverter.reverse(tournamentDTO)).stream()
                .map(this::createConverterRequest).collect(Collectors.toList()));
    }

    public List<TeamDTO> getAllByTournament(Integer tournamentId) {
        return converter.convertAll(provider.getAll(tournamentProvider.get(tournamentId)
                        .orElseThrow(() -> new TournamentNotFoundException(getClass(), "No tournamnet found with id '" + tournamentId + "'.")))
                .stream().map(this::createConverterRequest).collect(Collectors.toList()));
    }

    public long countByTournament(Integer tournamentId) {
        return provider.count(tournamentProvider.get(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(getClass(), "No tournamnet found with id '" + tournamentId + "'.")));
    }

    @Override
    public TeamDTO create(TeamDTO teamDTO) {
        if (teamDTO.getGroup() == null) {
            teamDTO.setGroup(1);
        }
        if (teamDTO.getName() == null) {
            teamDTO.setName(provider.getNextDefaultName(tournamentConverter.reverse(teamDTO.getTournament())));
        }
        return super.create(teamDTO);
    }

    public TeamDTO delete(TournamentDTO tournamentDTO, ParticipantDTO member) {
        final Team team = provider.delete(tournamentConverter.reverse(tournamentDTO), participantConverter.reverse(member)).orElse(null);
        if (team != null) {
            return converter.convert(new TeamConverterRequest(team));
        }
        return null;
    }

    public void delete(TournamentDTO tournamentDTO) {
        provider.delete(tournamentConverter.reverse(tournamentDTO));
    }

    @Override
    public TeamDTO update(TeamDTO teamDTO) {
        if (teamDTO.getGroup() == null) {
            teamDTO.setGroup(1);
        }
        validate(teamDTO);
        final Team dbTeam = super.provider.save(converter.reverse(teamDTO));
        dbTeam.setTournament(tournamentConverter.reverse(teamDTO.getTournament()));
        return converter.convert(createConverterRequest(dbTeam));
    }

}
