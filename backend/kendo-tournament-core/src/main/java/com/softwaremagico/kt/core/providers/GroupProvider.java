package com.softwaremagico.kt.core.providers;

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

import com.softwaremagico.kt.core.exceptions.NotFoundException;
import com.softwaremagico.kt.logger.ExceptionType;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class GroupProvider extends CrudProvider<Group, Integer, GroupRepository> {

    @Autowired
    public GroupProvider(GroupRepository repository) {
        super(repository);
    }

    private List<Group> sort(List<Group> groups) {
        groups.sort(Comparator.comparing(Group::getLevel).thenComparing(Group::getIndex));
        return groups;
    }

    public List<Group> getGroups(Tournament tournament) {
        return sort(getRepository().findByTournamentOrderByLevelAscIndexAsc(tournament));
    }

    public Group getGroup(Fight fight) {
        return getRepository().findByFightsId(fight.getId()).orElse(null);
    }

    public List<Group> getGroups(Tournament tournament, Integer level) {
        return sort(getRepository().findByTournamentAndLevelOrderByLevelAscIndexAsc(tournament, level));
    }

    public Group getGroupByLevelAndIndex(Tournament tournament, Integer level, Integer index) {
        if (level == null) {
            level = 0;
        }
        if (index == null) {
            index = 0;
        }
        return getRepository().findByTournamentAndLevelAndIndex(tournament, level, index);
    }

    public boolean deleteGroupByLevelAndIndex(Tournament tournament, Integer level, Integer index) {
        if (level == null) {
            level = 0;
        }
        if (index == null) {
            index = 0;
        }
        return getRepository().deleteByTournamentAndLevelAndIndex(tournament, level, index) > 0;
    }

    public Group getGroup(Integer groupId) {
        return getRepository().findById(groupId).orElse(null);
    }

    public List<Group> getGroups(Collection<Fight> fights) {
        return sort(getRepository().findDistinctByFightsIdIn(fights.stream().map(Fight::getId).toList()));
    }

    public List<Group> getGroupsByShiaijo(Tournament tournament, Integer shiaijo) {
        return getRepository().findByTournamentAndShiaijoOrderByLevelAscIndexAsc(tournament, shiaijo);
    }

    public Group addGroup(Tournament tournament, Group group) {
        group.setTournament(tournament);
        return getRepository().save(group);
    }

    public List<Group> addGroups(Tournament tournament, Collection<Group> groups) {
        groups.forEach(group -> group.setTournament(tournament));
        return getRepository().saveAll(groups);
    }

    public long delete(Tournament tournament) {
        return getRepository().deleteByTournament(tournament);
    }

    public void delete(Tournament tournament, Group group) {
        if (Objects.equals(group.getTournament(), tournament)) {
            deleteGroupByLevelAndIndex(tournament, group.getLevel(), group.getIndex());
        }
    }

    public long delete(Tournament tournament, Integer level) {
        return getRepository().deleteByTournamentAndLevel(tournament, level);
    }

    public Group addTeams(Integer groupId, List<Team> teams, String username) {
        final Group group = get(groupId).orElseThrow(() -> new NotFoundException(getClass(), "Entity with id '" + groupId + "' not found.",
                ExceptionType.INFO));
        group.getTeams().addAll(teams.stream().filter(team -> !group.getTeams().contains(team)).toList());
        group.setUpdatedBy(username);
        return getRepository().save(group);
    }

    public Group deleteTeams(Integer groupId, List<Team> teams, String username) {
        final Group group = get(groupId).orElseThrow(() -> new NotFoundException(getClass(), "Entity with id '" + groupId + "' not found.",
                ExceptionType.INFO));
        group.getTeams().removeAll(teams);
        group.setUpdatedBy(username);
        return getRepository().save(group);
    }

    public List<Group> deleteTeams(Tournament tournament, List<Team> teams, String username) {
        final List<Group> groups = getGroups(tournament);
        final List<Group> groupsUpdated = new ArrayList<>();
        groups.forEach(group -> {
            if (group.getTeams().removeAll(teams)) {
                group.setUpdatedBy(username);
                groupsUpdated.add(getRepository().save(group));
            }
        });
        return groupsUpdated;
    }

    public List<Group> deleteTeams(Tournament tournament, String username) {
        final List<Group> groups = getGroups(tournament);
        final List<Group> groupsUpdated = new ArrayList<>();
        groups.forEach(group -> {
            if (!group.getTeams().isEmpty()) {
                group.getTeams().clear();
                group.setUpdatedBy(username);
                groupsUpdated.add(getRepository().save(group));
            }
        });
        return groupsUpdated;
    }

    public long count(Tournament tournament) {
        return getRepository().countByTournament(tournament);
    }
}
