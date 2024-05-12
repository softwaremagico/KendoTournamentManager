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

import com.softwaremagico.kt.core.controller.models.DuelDTO;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.DuelConverter;
import com.softwaremagico.kt.core.converters.FightConverter;
import com.softwaremagico.kt.core.converters.GroupConverter;
import com.softwaremagico.kt.core.converters.TeamConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.models.GroupConverterRequest;
import com.softwaremagico.kt.core.converters.models.TournamentConverterRequest;
import com.softwaremagico.kt.core.exceptions.GroupNotFoundException;
import com.softwaremagico.kt.core.exceptions.TournamentNotFoundException;
import com.softwaremagico.kt.core.providers.DuelProvider;
import com.softwaremagico.kt.core.providers.FightProvider;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.core.tournaments.TournamentHandlerSelector;
import com.softwaremagico.kt.logger.ExceptionType;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.repositories.GroupRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class GroupController extends BasicInsertableController<Group, GroupDTO, GroupRepository, GroupProvider, GroupConverterRequest, GroupConverter> {
    private final TournamentConverter tournamentConverter;
    private final TournamentProvider tournamentProvider;
    private final FightProvider fightProvider;
    private final FightConverter fightConverter;
    private final DuelProvider duelProvider;
    private final DuelConverter duelConverter;
    private final TeamConverter teamConverter;
    private final TournamentHandlerSelector tournamentHandlerSelector;

    private final Set<GroupsUpdatedListener> groupsUpdatedListeners = new HashSet<>();


    public interface GroupsUpdatedListener {
        void updated(TournamentDTO tournament, String actor);
    }


    @Autowired
    public GroupController(GroupProvider provider, GroupConverter converter, TournamentConverter tournamentConverter,
                           TournamentProvider tournamentProvider, FightProvider fightProvider, FightConverter fightConverter,
                           DuelProvider duelProvider, DuelConverter duelConverter, TeamConverter teamConverter,
                           TournamentHandlerSelector tournamentHandlerSelector) {
        super(provider, converter);
        this.tournamentConverter = tournamentConverter;
        this.tournamentProvider = tournamentProvider;
        this.fightProvider = fightProvider;
        this.fightConverter = fightConverter;
        this.duelProvider = duelProvider;
        this.duelConverter = duelConverter;
        this.teamConverter = teamConverter;
        this.tournamentHandlerSelector = tournamentHandlerSelector;
    }


    public void addGroupUpdatedListeners(GroupsUpdatedListener listener) {
        groupsUpdatedListeners.add(listener);
    }


    @Override
    protected GroupConverterRequest createConverterRequest(Group group) {
        return new GroupConverterRequest(group);
    }


    public List<GroupDTO> getFromTournament(Integer tournamentId) {
        return get(tournamentConverter.convert(new TournamentConverterRequest(tournamentProvider.get(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(getClass(), "No tournament found with id '" + tournamentId + "',",
                        ExceptionType.INFO)))));
    }


    public GroupDTO getFromTournament(Integer tournamentId, Integer level, Integer index) {
        return convert(getProvider().getGroupByLevelAndIndex(tournamentProvider.get(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(getClass(), "No tournament found with id '" + tournamentId + "',",
                        ExceptionType.INFO)), level, index));
    }


    public List<Group> getGroups(TournamentDTO tournament, Integer level) {
        return getProvider().getGroups(tournamentConverter.reverse(tournament), level);
    }


    @Override
    public GroupDTO create(GroupDTO groupDTO, String username) {
        try {
            return convert(tournamentHandlerSelector.selectManager(groupDTO.getTournament().getType()).addGroup(
                    tournamentConverter.reverse(groupDTO.getTournament()), reverse(groupDTO)));
        } finally {
            new Thread(() ->
                    groupsUpdatedListeners.forEach(groupsUpdatedListener -> groupsUpdatedListener.updated(groupDTO.getTournament(), username))
            ).start();
        }
    }


    @Override
    public void deleteById(Integer id, String username) {
        delete(get(id), username);
    }


    @Override
    public void delete(GroupDTO groupDTO, String username) {
        try {
            tournamentHandlerSelector.selectManager(groupDTO.getTournament().getType()).removeGroup(tournamentConverter.reverse(groupDTO.getTournament()),
                    groupDTO.getLevel(), groupDTO.getIndex());
        } finally {
            new Thread(() ->
                    groupsUpdatedListeners.forEach(groupsUpdatedListener -> groupsUpdatedListener.updated(groupDTO.getTournament(), username))
            ).start();
        }
    }


    @Override
    public void delete(Collection<GroupDTO> groupDTOs, String username) {
        groupDTOs.forEach(groupDTO -> delete(groupDTOs, username));
    }


    public List<GroupDTO> get(TournamentDTO tournament) {
        return convertAll(getProvider().getGroups(tournamentConverter.reverse(tournament)));
    }


    @Transactional
    public GroupDTO update(GroupDTO groupDTO, String username) {
        validate(groupDTO);
        final GroupDTO oldGroupDTO = get(groupDTO.getId());
        final List<FightDTO> fights = new ArrayList<>(oldGroupDTO.getFights());
        oldGroupDTO.getFights().clear();
        fightProvider.delete(fightConverter.reverseAll(fights));

        final List<DuelDTO> unties = new ArrayList<>(oldGroupDTO.getUnties());
        oldGroupDTO.getUnties().clear();
        duelProvider.delete(duelConverter.reverseAll(unties));

        //Remove all fights and duels from the group. Will be added on the update.
        convert(getProvider().save(reverse(oldGroupDTO)));

        //Ensure that the group contains the teams of the fight.
        groupDTO.getFights().forEach(fightDTO -> {
            if (fightDTO != null) {
                if (!groupDTO.getTeams().contains(fightDTO.getTeam1())) {
                    groupDTO.getTeams().add(fightDTO.getTeam1());
                }
                if (!groupDTO.getTeams().contains(fightDTO.getTeam2())) {
                    groupDTO.getTeams().add(fightDTO.getTeam2());
                }
            }
        });

        try {
            return super.update(groupDTO, username);
        } finally {
            new Thread(() ->
                    groupsUpdatedListeners.forEach(groupsUpdatedListener -> groupsUpdatedListener.updated(groupDTO.getTournament(), username))
            ).start();
        }
    }


    public GroupDTO addTeams(Integer groupId, List<TeamDTO> teams, String username) {
        try {
            return convert(getProvider().addTeams(groupId, teamConverter.reverseAll(teams), username));
        } finally {
            new Thread(() -> {
                final GroupDTO groupDTO = get(groupId);
                groupsUpdatedListeners.forEach(groupsUpdatedListener -> groupsUpdatedListener.updated(groupDTO.getTournament(), username));
            }).start();
        }
    }


    public GroupDTO deleteTeams(Integer groupId, List<TeamDTO> teams, String username) {
        try {
            return convert(getProvider().deleteTeams(groupId, teamConverter.reverseAll(teams), username));
        } finally {
            new Thread(() -> {
                final GroupDTO groupDTO = get(groupId);
                groupsUpdatedListeners.forEach(groupsUpdatedListener -> groupsUpdatedListener.updated(groupDTO.getTournament(), username));
            }).start();
        }
    }


    public List<GroupDTO> deleteTeamsFromTournament(Integer tournamentId, String username) {
        try {
            return convertAll(getProvider().deleteTeams(tournamentProvider.get(tournamentId).orElseThrow(() ->
                            new TournamentNotFoundException(this.getClass(), "Tournament with id" + tournamentId + " not found!")),
                    username));
        } finally {
            new Thread(() -> {
                final TournamentDTO tournamentDTO = tournamentConverter.convert(new TournamentConverterRequest(tournamentProvider.get(tournamentId)
                        .orElseThrow(() -> new TournamentNotFoundException(getClass(), "No tournament found with id '" + tournamentId + "',",
                                ExceptionType.INFO))));
                groupsUpdatedListeners.forEach(groupsUpdatedListener -> groupsUpdatedListener.updated(tournamentDTO, username));
            }).start();
        }
    }


    public List<GroupDTO> deleteTeamsFromTournament(Integer tournamentId, List<TeamDTO> teams, String username) {
        try {
            return convertAll(getProvider().deleteTeams(tournamentProvider.get(tournamentId).orElseThrow(() ->
                            new TournamentNotFoundException(this.getClass(), "Tournament with id" + tournamentId + " not found!")),
                    teamConverter.reverseAll(teams), username));
        } finally {
            new Thread(() -> {
                final TournamentDTO tournamentDTO = tournamentConverter.convert(new TournamentConverterRequest(tournamentProvider.get(tournamentId)
                        .orElseThrow(() -> new TournamentNotFoundException(getClass(), "No tournament found with id '" + tournamentId + "',",
                                ExceptionType.INFO))));
                groupsUpdatedListeners.forEach(groupsUpdatedListener -> groupsUpdatedListener.updated(tournamentDTO, username));
            }).start();
        }
    }


    public GroupDTO setTeams(Integer groupId, List<TeamDTO> teams, String username) {
        try {
            return convert(getProvider().setTeams(groupId, teamConverter.reverseAll(teams), username));
        } finally {
            new Thread(() -> {
                final GroupDTO groupDTO = get(groupId);
                groupsUpdatedListeners.forEach(groupsUpdatedListener -> groupsUpdatedListener.updated(groupDTO.getTournament(), username));
            }).start();
        }
    }


    public GroupDTO setTeams(List<TeamDTO> teams, String username) {
        final GroupDTO groupDTO = get(teams.get(0).getTournament()).stream().findAny().orElseThrow(() ->
                new GroupNotFoundException(this.getClass(), "No groups found!"));
        try {
            return convert(getProvider().setTeams(groupDTO.getId(), teamConverter.reverseAll(teams), username));
        } finally {
            new Thread(() -> groupsUpdatedListeners.forEach(groupsUpdatedListener ->
                    groupsUpdatedListener.updated(groupDTO.getTournament(), username))).start();
        }
    }


    public GroupDTO addUnties(Integer groupId, List<DuelDTO> duelDTOS, String username) {
        final GroupDTO groupDTO = get(groupId);
        duelDTOS.forEach(duelDTO -> {
            duelDTO.setCreatedBy(username);
            duelDTO.setTournament(groupDTO.getTournament());
        });
        groupDTO.getUnties().addAll(duelDTOS);
        groupDTO.setUpdatedBy(username);
        return convert(getProvider().save(reverse(groupDTO)));
    }


    public long count(TournamentDTO tournament) {
        return getProvider().count(tournamentConverter.reverse(tournament));
    }


    public long delete(TournamentDTO tournamentDTO) {
        return getProvider().delete(tournamentConverter.reverse(tournamentDTO));
    }

}
