package com.softwaremagico.kt.core.tournaments;

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

import com.softwaremagico.kt.core.controller.RankingController;
import com.softwaremagico.kt.core.converters.GroupConverter;
import com.softwaremagico.kt.core.exceptions.InvalidGroupException;
import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.core.providers.TournamentExtraPropertyProvider;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
public class TreeTournamentHandler extends LeagueHandler {
    private final GroupProvider groupProvider;

    private final TournamentExtraPropertyProvider tournamentExtraPropertyProvider;


    public TreeTournamentHandler(GroupProvider groupProvider, TeamProvider teamProvider, GroupConverter groupConverter, RankingController rankingController,
                                 TournamentExtraPropertyProvider tournamentExtraPropertyProvider) {
        super(groupProvider, teamProvider, groupConverter, rankingController);
        this.groupProvider = groupProvider;
        this.tournamentExtraPropertyProvider = tournamentExtraPropertyProvider;
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

    private int getNumberOfWinners(Tournament tournament) {
        final TournamentExtraProperty numberOfWinnersProperty = tournamentExtraPropertyProvider.getByTournamentAndProperty(tournament,
                TournamentExtraPropertyKey.NUMBER_OF_WINNERS);

        if (numberOfWinnersProperty != null) {
            try {
                return Integer.parseInt(numberOfWinnersProperty.getPropertyValue());
            } catch (Exception ignore) {

            }
        }
        return 1;
    }

    @Override
    public Group addGroup(Tournament tournament, Group group) {
        if (group.getLevel() > 0) {
            throw new InvalidGroupException(this.getClass(), "Groups can only be added at level 0.");
        }

        final int numberOfWinners = getNumberOfWinners(tournament);

        final Group savedGroup = groupProvider.addGroup(tournament, group);

        //Check if inner levels must be increased on size.
        final List<Group> tournamentGroups = groupProvider.getGroups(tournament);
        final Map<Integer, List<Group>> groupsByLevel = orderByLevel(tournamentGroups);
        int previousLevelSize = 0;
        for (final Integer level : new HashSet<>(groupsByLevel.keySet())) {
            if (groupsByLevel.get(level).size() < (((previousLevelSize + 1) / 2) * (level == 1 ? numberOfWinners : 1))) {
                final Group levelGroup = new Group(tournament, level, groupsByLevel.get(level).size());
                groupProvider.addGroup(tournament, levelGroup);
                groupsByLevel.get(level).add(group);
            }
            previousLevelSize = groupsByLevel.get(level).size();
        }

        //Add extra level if needed.
        if (groupsByLevel.get(groupsByLevel.size() - 1).size() > 1 || (groupsByLevel.size() == 1 && numberOfWinners > 1)) {
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
        final int numberOfWinners = getNumberOfWinners(tournament);


        //Check if inner levels must be decreased on size.
        final List<Group> tournamentGroups = groupProvider.getGroups(tournament);
        final Map<Integer, List<Group>> groupsByLevel = orderByLevel(tournamentGroups);
        int previousLevelSize = Integer.MAX_VALUE - 1;
        for (final Integer level : new HashSet<>(groupsByLevel.keySet())) {
            // Normal levels, the number of groups must be the half rounded up that the previous one.
            if ((numberOfWinners == 1 || level > 1)
                    && (previousLevelSize == 1 || groupsByLevel.get(level).size() > ((previousLevelSize + 1) / 2))) {
                groupProvider.deleteGroupByLevelAndIndex(tournament, level, groupsByLevel.get(level).size() - 1);
                groupsByLevel.get(level).remove(groupsByLevel.get(level).size() - 1);
                // First level with 2 winners must have the same size that level zero.
            } else if (numberOfWinners == 2 && groupsByLevel.get(level).size() > previousLevelSize) {
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
