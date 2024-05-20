package com.softwaremagico.kt.core.converters;

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

import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.converters.models.ParticipantConverterRequest;
import com.softwaremagico.kt.core.converters.models.TeamConverterRequest;
import com.softwaremagico.kt.core.converters.models.TournamentConverterRequest;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.repositories.TournamentRepository;
import org.hibernate.LazyInitializationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class TeamConverter extends ElementConverter<Team, TeamDTO, TeamConverterRequest> {
    private final TournamentConverter tournamentConverter;
    private final ParticipantReducedConverter participantReducedConverter;
    private final ParticipantConverter participantConverter;
    private final TournamentRepository tournamentRepository;

    @Autowired
    public TeamConverter(TournamentConverter tournamentConverter, ParticipantReducedConverter participantReducedConverter,
                         ParticipantConverter participantConverter, TournamentRepository tournamentRepository) {
        this.tournamentConverter = tournamentConverter;
        this.participantReducedConverter = participantReducedConverter;
        this.participantConverter = participantConverter;
        this.tournamentRepository = tournamentRepository;
    }


    @Override
    protected TeamDTO convertElement(TeamConverterRequest from) {
        final TeamDTO teamDTO = new TeamDTO();
        BeanUtils.copyProperties(from.getEntity(), teamDTO, ConverterUtils.getNullPropertyNames(from.getEntity()));
        teamDTO.setMembers(new ArrayList<>());

        try {
            if (from.getTournamentDTO() != null) {
                teamDTO.setTournament(from.getTournamentDTO());
            } else if (from.getTournament() != null) {
                //Converter can have the tournament defined already.
                teamDTO.setTournament(tournamentConverter.convert(
                        new TournamentConverterRequest(from.getTournament())));
            } else {
                teamDTO.setTournament(tournamentConverter.convert(
                        new TournamentConverterRequest(from.getEntity().getTournament())));
            }
        } catch (LazyInitializationException | FatalBeanException e) {
            teamDTO.setTournament(tournamentConverter.convert(
                    new TournamentConverterRequest(tournamentRepository.findById(from.getEntity().getTournament().getId()).orElse(null))));
        }

        from.getEntity().getMembers().forEach(member ->
                teamDTO.getMembers().add(participantReducedConverter.convert(new ParticipantConverterRequest(member))));
        return teamDTO;
    }

    @Override
    public Team reverse(TeamDTO to) {
        if (to == null) {
            return null;
        }
        final Team team = new Team();
        BeanUtils.copyProperties(to, team, ConverterUtils.getNullPropertyNames(to));
        team.setTournament(tournamentConverter.reverse(to.getTournament()));
        team.setMembers(new ArrayList<>());
        to.getMembers().forEach(member -> team.getMembers().add(participantConverter.reverse(member)));
        return team;
    }
}
