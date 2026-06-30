package com.softwaremagico.kt.core.providers;

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


import com.softwaremagico.kt.core.exceptions.NotFoundException;
import com.softwaremagico.kt.core.exceptions.TeamNotFoundException;
import com.softwaremagico.kt.logger.ExceptionType;
import com.softwaremagico.kt.logger.KendoTournamentLogger;
import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.DuelRepository;
import com.softwaremagico.kt.persistence.repositories.FightRepository;
import com.softwaremagico.kt.persistence.repositories.GroupLinkRepository;
import com.softwaremagico.kt.persistence.repositories.GroupRepository;
import com.softwaremagico.kt.utils.GroupUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class GroupProvider extends CrudProvider<Group, Integer, GroupRepository> {
    private static final String NOT_FOUND_SUFFIX = "' not found.";

    private final FightRepository fightRepository;
    private final DuelRepository duelRepository;
    private final GroupLinkRepository groupLinkRepository;

    @Autowired
    public GroupProvider(GroupRepository repository, FightRepository fightRepository, DuelRepository duelRepository,
                         GroupLinkRepository groupLinkRepository) {
        super(repository);
        this.fightRepository = fightRepository;
        this.duelRepository = duelRepository;
        this.groupLinkRepository = groupLinkRepository;
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
        groupLinkRepository.deleteByTournament(tournament);
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
        groupLinkRepository.deleteByTournament(tournament);
        group.setTournament(tournament);
        if (group.getId() != null) {
            final Group existing = getRepository().findById(group.getId()).orElse(null);
            if (existing != null) {
                // Keep optimistic-lock version from the managed row and copy mutable fields only.
                existing.setTournament(tournament);
                existing.setTeams(group.getTeams());
                existing.setFights(group.getFights());
                existing.setUnties(group.getUnties());
                existing.setShiaijo(group.getShiaijo());
                existing.setLevel(group.getLevel());
                existing.setIndex(group.getIndex());
                existing.setNumberOfWinners(group.getNumberOfWinners());
                if (group.getUpdatedBy() != null) {
                    existing.setUpdatedBy(group.getUpdatedBy());
                }
                return getRepository().save(existing);
            }
            // The old row may have been deleted before re-adding the group (league regeneration path).
            // Persist as new row instead of merging a stale detached instance.
            group.setId(null);
            group.setVersion(null);
        }
        return getRepository().save(group);
    }

    public List<Group> addGroups(Tournament tournament, Collection<Group> groups) {
        groupLinkRepository.deleteByTournament(tournament);
        groups.forEach(group -> group.setTournament(tournament));
        return getRepository().saveAll(groups);
    }

    public long delete(Tournament tournament) {
        groupLinkRepository.deleteByTournament(tournament);
        return getRepository().deleteByTournament(tournament);
    }

    public void delete(Tournament tournament, Group group) {
        groupLinkRepository.deleteByTournament(tournament);
        if (Objects.equals(group.getTournament(), tournament)) {
            deleteGroupByLevelAndIndex(tournament, group.getLevel(), group.getIndex());
        }
    }

    public long delete(Tournament tournament, Integer level) {
        final List<Group> groups = getGroups(tournament);
        final Map<Integer, List<Group>> groupsByLevel = GroupUtils.orderByLevel(groups);
        long deleted = 0;
        groupLinkRepository.deleteByTournament(tournament);
        if (!groups.isEmpty()) {
            if (!groupsByLevel.get(0).isEmpty()) {
                deleted += deleteGroupsFromLevel(groups, groupsByLevel, level);
            } else {
                deleted += getRepository().deleteByTournamentAndLevel(tournament, 0);
            }
        }
        KendoTournamentLogger.warning(this.getClass(), "Deleted '{}' groups.", deleted);
        return deleted;
    }

    private long deleteGroupsFromLevel(List<Group> groups, Map<Integer, List<Group>> groupsByLevel, int fromLevel) {
        long deleted = 0;
        final int maxLevel = groups.get(groups.size() - 1).getLevel();
        final int numberOfWinnersAtLevelZero = groupsByLevel.get(0).get(0).getNumberOfWinners();

        for (int i = fromLevel; i <= maxLevel; i++) {
            if (i > 1 || numberOfWinnersAtLevelZero == 1) {
                deleted += deleteExcessRegularGroups(groupsByLevel, i);
            } else if (i == 1 && numberOfWinnersAtLevelZero == 2 && !groupsByLevel.get(i).isEmpty()) {
                deleted += deleteExcessTwoWinnersGroups(groupsByLevel);
            }
        }
        return deleted;
    }

    private long deleteExcessRegularGroups(Map<Integer, List<Group>> groupsByLevel, int level) {
        long deleted = 0;
        final List<Group> levelGroups = groupsByLevel.get(level);
        if (levelGroups == null || levelGroups.isEmpty()) {
            return 0;
        }
        final List<Group> prevLevelGroups = groupsByLevel.get(level - 1);

        while ((prevLevelGroups.size() + 1) / 2 < levelGroups.size()) {
            deleted += removeLastGroupFromLevel(levelGroups);
        }
        if (prevLevelGroups.size() == 1 && !levelGroups.isEmpty()) {
            deleted += removeLastGroupFromLevel(levelGroups);
        }
        return deleted;
    }

    private long deleteExcessTwoWinnersGroups(Map<Integer, List<Group>> groupsByLevel) {
        long deleted = 0;
        final List<Group> level1Groups = groupsByLevel.get(1);
        final List<Group> level0Groups = groupsByLevel.get(0);

        while ((level0Groups.size() + 1) < level1Groups.size()) {
            deleted += removeLastGroupFromLevel(level1Groups);
        }
        return deleted;
    }

    private long removeLastGroupFromLevel(List<Group> levelGroups) {
        getRepository().delete(levelGroups.get(levelGroups.size() - 1));
        levelGroups.remove(levelGroups.size() - 1);
        return 1;
    }
    public Group addTeams(Integer groupId, List<Team> teams, String username) {
        final Group group = get(groupId).orElseThrow(() -> new NotFoundException(getClass(), "Entity with id '" + groupId + NOT_FOUND_SUFFIX,
                ExceptionType.INFO));
        group.getTeams().addAll(teams.stream().filter(team -> !group.getTeams().contains(team)).toList());
        group.setUpdatedBy(username);
        return getRepository().save(group);
    }

    public Group deleteTeams(Integer groupId, List<Team> teams, String username) {
        final Group group = get(groupId).orElseThrow(() -> new NotFoundException(getClass(), "Entity with id '" + groupId + NOT_FOUND_SUFFIX,
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

    public Group setTeams(Integer groupId, List<Team> teams, String username) {
        if (teams.isEmpty()) {
            throw new TeamNotFoundException(this.getClass(), "No teams found!");
        }

        Group group = get(groupId).orElseThrow(() -> new NotFoundException(getClass(), "Group with id '" + groupId + NOT_FOUND_SUFFIX,
                ExceptionType.INFO));

        final List<Fight> fights = new ArrayList<>(group.getFights());
        group.getFights().clear();

        final List<Duel> unties = new ArrayList<>(group.getUnties());
        group.getUnties().clear();

        group.getTeams().clear();
        group = save(group);
        //Unties are unlinked from groups. Can be removed.
        duelRepository.deleteAll(unties);
        fightRepository.deleteAll(fights);
        group.setTeams(teams);
        group.setUpdatedBy(username);
        return save(group);
    }
}
