package com.softwaremagico.kt.core.controller;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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

import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.ParticipantConverter;
import com.softwaremagico.kt.core.converters.TeamConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.models.TeamConverterRequest;
import com.softwaremagico.kt.core.exceptions.TournamentNotFoundException;
import com.softwaremagico.kt.core.exceptions.ValidateBadRequestException;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.core.tournaments.SenbatsuTournamentHandler;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.GroupRepository;
import com.softwaremagico.kt.persistence.repositories.TeamRepository;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Controller
public class TeamController extends BasicInsertableController<Team, TeamDTO, TeamRepository,
        TeamProvider, TeamConverterRequest, TeamConverter> {
    private final TournamentProvider tournamentProvider;
    private final TournamentConverter tournamentConverter;
    private final ParticipantConverter participantConverter;
    private final GroupRepository groupRepository;
    private final SenbatsuTournamentHandler senbatsuTournamentHandler;


    @Autowired
    public TeamController(TeamProvider provider, TeamConverter converter, TournamentProvider tournamentProvider,
                          TournamentConverter tournamentConverter, ParticipantConverter participantConverter, GroupRepository groupRepository,
                          SenbatsuTournamentHandler senbatsuTournamentHandler) {
        super(provider, converter);
        this.tournamentProvider = tournamentProvider;
        this.tournamentConverter = tournamentConverter;
        this.participantConverter = participantConverter;
        this.groupRepository = groupRepository;
        this.senbatsuTournamentHandler = senbatsuTournamentHandler;
    }

    @Override
    protected TeamConverterRequest createConverterRequest(Team entity) {
        return new TeamConverterRequest(entity);
    }

    public List<TeamDTO> getAllByTournament(TournamentDTO tournamentDTO, String createdBy) {
        final List<TeamDTO> teams = convertAll(getProvider().getAll(tournamentConverter.reverse(tournamentDTO)));
        if (teams.isEmpty()) {
            return convertAll(getProvider().createDefaultTeams(tournamentConverter.reverse(tournamentDTO), createdBy));
        }
        return teams;
    }

    public List<TeamDTO> getAllByTournament(Integer tournamentId, String createdBy) {
        final Tournament tournament = tournamentProvider.get(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(getClass(), "No tournament found with id '" + tournamentId + "'."));
        final List<TeamDTO> teams = convertAll(getProvider().getAll(tournament));
        if (teams.isEmpty()) {
            return convertAllNotSorted(getProvider().createDefaultTeams(tournament, createdBy));
        }
        return teams;
    }

    public List<TeamDTO> getAllRemainingByTournament(Integer tournamentId, String createdBy) {
        final Tournament tournament = tournamentProvider.get(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(getClass(), "No tournament found with id '" + tournamentId + "'."));
        if (tournament.getType() != TournamentType.SENBATSU) {
            return getAllByTournament(tournamentId, createdBy);
        }
        return convertAllNotSorted(senbatsuTournamentHandler.getNextTeamsOrderedByRanks(tournament, null));
    }

    public long countByTournament(Integer tournamentId) {
        return getProvider().count(tournamentProvider.get(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(getClass(), "No tournament found with id '" + tournamentId + "'.")));
    }

    @Override
    public TeamDTO create(TeamDTO teamDTO, String username, String session) {
        if (teamDTO.getName() == null) {
            teamDTO.setName(getProvider().getNextDefaultName(tournamentConverter.reverse(teamDTO.getTournament())));
        }
        final TeamDTO storedTeamDTO = super.create(teamDTO, username, session);
        storedTeamDTO.setTournament(teamDTO.getTournament());
        return storedTeamDTO;
    }

    public List<TeamDTO> create(TournamentDTO tournamentDTO, String createdBy) {
        return convertAll(getProvider().createDefaultTeams(tournamentConverter.reverse(tournamentDTO), createdBy));
    }

    @Override
    public List<TeamDTO> create(Collection<TeamDTO> teamsDTOs, String username, String session) {
        teamsDTOs.forEach(teamDTO -> {
            if (teamDTO.getName() == null) {
                teamDTO.setName(getProvider().getNextDefaultName(tournamentConverter.reverse(teamDTO.getTournament())));
            }
        });
        return super.create(teamsDTOs, username, session);
    }

    public TeamDTO delete(TournamentDTO tournamentDTO, ParticipantDTO member) {
        final Team team = getProvider().delete(tournamentConverter.reverse(tournamentDTO), participantConverter.reverse(member)).orElse(null);
        if (team != null) {
            return convert(team);
        }
        return null;
    }

    public void delete(TournamentDTO tournamentDTO) {
        final Tournament tournament = tournamentConverter.reverse(tournamentDTO);
        //Remove teams from groups or will be a ConstraintViolationException SQL error.
        final List<Group> groups = groupRepository.findByTournamentOrderByLevelAscIndexAsc(tournament);
        groups.forEach(group -> group.setTeams(new ArrayList<>()));
        groupRepository.saveAll(groups);
        getProvider().delete(tournament);
    }

    @Override
    public TeamDTO update(TeamDTO teamDTO, String username, String session) {
        final TeamDTO dbTeam = super.update(teamDTO, username, session);
        dbTeam.setTournament(teamDTO.getTournament());
        return dbTeam;
    }

    public long count(TournamentDTO tournament) {
        return getProvider().count(tournamentConverter.reverse(tournament));
    }

    @Override
    public void validate(TeamDTO teamDTO) throws ValidateBadRequestException {
        if (teamDTO == null || teamDTO.getTournament() == null) {
            throw new ValidateBadRequestException(getClass(), "Team data is missing");
        }
    }

}
