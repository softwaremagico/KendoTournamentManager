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

import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.GroupLink;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.repositories.GroupLinkRepository;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class GroupLinkProvider extends CrudProvider<GroupLink, Integer, GroupLinkRepository> {

    private final TournamentExtraPropertyProvider tournamentExtraPropertyProvider;
    private final GroupProvider groupProvider;

    public GroupLinkProvider(GroupLinkRepository groupLinkRepository, TournamentExtraPropertyProvider tournamentExtraPropertyProvider,
                             GroupProvider groupProvider) {
        super(groupLinkRepository);
        this.tournamentExtraPropertyProvider = tournamentExtraPropertyProvider;
        this.groupProvider = groupProvider;
    }

    public List<GroupLink> generateLinks(Tournament tournament) {
        final TournamentExtraProperty numberOfWinners = tournamentExtraPropertyProvider.getByTournamentAndProperty(
                tournament, TournamentExtraPropertyKey.NUMBER_OF_WINNERS);
        int tournamentWinners;
        try {
            tournamentWinners = Integer.parseInt(numberOfWinners.getPropertyValue());
        } catch (Exception e) {
            tournamentWinners = 1;
        }
        final List<Group> groups = groupProvider.getGroups(tournament);
        return generateLinks(groups, tournamentWinners, groups.stream().max(Comparator.comparing(Group::getLevel)).orElse(new Group()).getLevel());
    }

    public List<GroupLink> generateLinks(List<Group> groups, int tournamentWinners, int tournamentLevels) {
        final List<GroupLink> groupLinks = new ArrayList<>();
        groups.forEach(group -> {
            if (group.getLevel() < tournamentLevels) {
                for (int i = 0; i < getNumberOfTotalTeamsPassNextRound(group, tournamentWinners); i++) {
                    final GroupLink groupLink = new GroupLink();
                    groupLink.setSource(group);
                    final Group destination = getDestination(group, i, groups);
                    if (destination != null) {
                        groupLink.setDestination(destination);
                        groupLinks.add(groupLink);
                    }
                }
            }
        });
        return groupLinks;
    }

    private int getNumberOfTotalTeamsPassNextRound(Group group, int tournamentWinners) {
        if (group.getLevel() == 0) {
            return tournamentWinners;
        }
        return 1;
    }

    public Group getDestination(Group sourceGroup, int winnerOrder, List<Group> groups) {
        final List<Group> currentLevelGroups = groups.stream().filter(group -> Objects.equals(group.getLevel(), sourceGroup.getLevel())).toList();
        final List<Group> nextLevelGroups = groups.stream().filter(group -> Objects.equals(group.getLevel(), sourceGroup.getLevel() + 1)).toList();
        try {
            return nextLevelGroups.get(obtainPositionOfWinner(sourceGroup.getIndex(), currentLevelGroups.size(), winnerOrder));
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private int obtainPositionOfWinner(Integer sourceGroupLevelIndex, Integer sourceGroupLevelSize, int winnerOrder) {
        if (winnerOrder == 0) {
            if (sourceGroupLevelIndex % 2 == 0) {
                return sourceGroupLevelIndex / 2;
            } else {
                return (sourceGroupLevelSize - sourceGroupLevelIndex) / 2;
            }
        } else if (winnerOrder == 1) {
            if (sourceGroupLevelIndex % 2 == 0) {
                return (sourceGroupLevelSize - sourceGroupLevelIndex) / 2;
            } else {
                return sourceGroupLevelIndex / 2;
            }
        } else {
            return -1;
        }
    }
}
