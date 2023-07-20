package com.softwaremagico.kt.core.tournaments;

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

import com.softwaremagico.kt.core.controller.RankingController;
import com.softwaremagico.kt.core.converters.GroupConverter;
import com.softwaremagico.kt.core.exceptions.InvalidGroupException;
import com.softwaremagico.kt.core.managers.SimpleGroupFightManager;
import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.core.providers.FightProvider;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Tournament;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
public class TreeTournamentHandler extends LeagueHandler {

    private final SimpleGroupFightManager simpleGroupFightManager;
    private final FightProvider fightProvider;
    private final GroupProvider groupProvider;


    public TreeTournamentHandler(GroupProvider groupProvider, TeamProvider teamProvider, GroupConverter groupConverter, RankingController rankingController,
                                 SimpleGroupFightManager simpleGroupFightManager, FightProvider fightProvider) {
        super(groupProvider, teamProvider, groupConverter, rankingController);
        this.groupProvider = groupProvider;
        this.simpleGroupFightManager = simpleGroupFightManager;
        this.fightProvider = fightProvider;
    }

    private Map<Integer, List<Group>> orderByLevel(List<Group> groups) {
        final Map<Integer, List<Group>> sortedGroups = new HashMap<>();
        groups.forEach(group -> {
            sortedGroups.computeIfAbsent(group.getLevel(), k -> new ArrayList<>());
            sortedGroups.get(group.getLevel()).add(group);
        });
        return sortedGroups;
    }

    @Override
    public List<Group> getGroups(Tournament tournament, Integer level) {
        return groupProvider.getGroups(tournament, level);
    }

    @Override
    public Group addGroup(Tournament tournament, Group group) {
        if (group.getLevel() > 0) {
            throw new InvalidGroupException(this.getClass(), "Groups can only be added at level 0.");
        }

        final Group savedGroup = groupProvider.addGroup(tournament, group);

        //Check if inner levels must be increased on size.
        final List<Group> tournamentGroups = groupProvider.getGroups(tournament);
        final Map<Integer, List<Group>> groupsByLevel = orderByLevel(tournamentGroups);
        int previousLevelSize = 0;
        for (final Integer level : new HashSet<>(groupsByLevel.keySet())) {
            if (groupsByLevel.get(level).size() < (previousLevelSize / 2)) {
                final Group levelGroup = new Group(tournament, level, groupsByLevel.get(level).size());
                groupProvider.addGroup(tournament, levelGroup);
                groupsByLevel.get(level).add(group);
            }
            previousLevelSize = groupsByLevel.get(level).size();
        }

        //Add extra level if needed.
        if (groupsByLevel.get(groupsByLevel.size() - 1).size() > 1) {
            final Integer newLevel = groupsByLevel.size();
            final Group levelGroup = new Group(tournament, newLevel, 0);
            groupsByLevel.put(newLevel, new ArrayList<>());
            groupsByLevel.get(newLevel).add(levelGroup);
            groupProvider.addGroup(tournament, levelGroup);
        }


        return savedGroup;
    }

    @Override
    public void removeGroup(Tournament tournament, Integer groupLevel, Integer groupIndex) {
        if (groupLevel > 0) {
            throw new InvalidGroupException(this.getClass(), "Groups can only be deleted at level 0.");
        }

        groupProvider.deleteGroupByLevelAndIndex(tournament, groupLevel, groupIndex);


        //Check if inner levels must be decreased on size.
        final List<Group> tournamentGroups = groupProvider.getGroups(tournament);
        final Map<Integer, List<Group>> groupsByLevel = orderByLevel(tournamentGroups);
        int previousLevelSize = 0;
        for (final Integer level : new HashSet<>(groupsByLevel.keySet())) {
            if (groupsByLevel.get(level).size() > (previousLevelSize / 2)) {
                groupProvider.deleteGroupByLevelAndIndex(tournament, level, groupsByLevel.get(level).size() - 1);
                groupsByLevel.get(level).remove(groupsByLevel.get(level).size() - 1);
            }
            previousLevelSize = groupsByLevel.get(level).size();
        }
    }

    @Override
    public List<Fight> createFights(Tournament tournament, TeamsOrder teamsOrder, Integer level, String createdBy) {
        return null;
    }
}
