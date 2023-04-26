package com.softwaremagico.kt.core.providers;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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

import com.softwaremagico.kt.core.exceptions.NotFoundException;
import com.softwaremagico.kt.logger.ExceptionType;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class GroupProvider extends CrudProvider<Group, Integer, GroupRepository> {

    @Autowired
    public GroupProvider(GroupRepository repository) {
        super(repository);
    }

    public List<Group> getGroups(Tournament tournament) {
        return repository.findByTournamentOrderByLevelAscIndexAsc(tournament);
    }

    public Group getGroup(Fight fight) {
        return repository.findByFightsId(fight.getId()).orElse(null);
    }

    public List<Group> getGroupsByLevel(Tournament tournament, Integer level) {
        return repository.findByTournamentAndLevelOrderByLevelAscIndexAsc(tournament, level);
    }

    public Group getGroupByLevelAndIndex(Tournament tournament, Integer level, Integer index) {
        if (level == null) {
            level = 0;
        }
        if (index == null) {
            index = 0;
        }
        return repository.findByTournamentAndLevelAndIndex(tournament, level, index);
    }

    public boolean deleteGroupByLevelAndIndex(Tournament tournament, Integer level, Integer index) {
        if (level == null) {
            level = 0;
        }
        if (index == null) {
            index = 0;
        }
        return repository.deleteByTournamentAndLevelAndIndex(tournament, level, index) > 0;
    }

    public Group getGroup(Integer groupId) {
        return repository.getById(groupId);
    }

    public List<Group> getGroups(Collection<Fight> fights) {
        return repository.findDistinctByFightsIdIn(fights.stream().map(Fight::getId).collect(Collectors.toList()));
    }

    public List<Group> getGroupsByShiaijo(Tournament tournament, Integer shiaijo) {
        return repository.findByTournamentAndShiaijoOrderByLevelAscIndexAsc(tournament, shiaijo);
    }

    public Group addGroup(Tournament tournament, Group group) {
        group.setTournament(tournament);
        return repository.save(group);
    }

    public long delete(Tournament tournament) {
        return repository.deleteByTournament(tournament);
    }

    public void delete(Tournament tournament, Group group) {
        if (Objects.equals(group.getTournament(), tournament)) {
            repository.delete(group);
        }
    }

    public long delete(Tournament tournament, Integer level) {
        return repository.deleteByTournamentAndLevel(tournament, level);
    }

    public Group addTeams(Integer groupId, List<Team> teams, String username) {
        final Group group = get(groupId).orElseThrow(() -> new NotFoundException(getClass(), "Entity with id '" + groupId + "' not found.",
                ExceptionType.INFO));
        group.getTeams().addAll(teams.stream().filter(team -> !group.getTeams().contains(team)).collect(Collectors.toList()));
        group.setUpdatedBy(username);
        return repository.save(group);
    }

    public Group deleteTeams(Integer groupId, List<Team> teams, String username) {
        final Group group = get(groupId).orElseThrow(() -> new NotFoundException(getClass(), "Entity with id '" + groupId + "' not found.",
                ExceptionType.INFO));
        group.getTeams().removeAll(teams);
        group.setUpdatedBy(username);
        return repository.save(group);
    }

    public long count(Tournament tournament) {
        return repository.countByTournament(tournament);
    }
}
