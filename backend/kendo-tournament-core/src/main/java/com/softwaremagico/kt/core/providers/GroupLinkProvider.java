package com.softwaremagico.kt.core.providers;

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

import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.GroupLink;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static com.softwaremagico.kt.core.tournaments.TreeTournamentHandler.DEFAULT_ODD_TEAMS_RESOLUTION_ASAP;

@Service
public class GroupLinkProvider {

    private final TournamentExtraPropertyProvider tournamentExtraPropertyProvider;
    private final GroupProvider groupProvider;

    public GroupLinkProvider(TournamentExtraPropertyProvider tournamentExtraPropertyProvider,
                             GroupProvider groupProvider) {
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
        final List<Group> groups = groupProvider.getGroups(tournament).stream().sorted(Comparator.comparing(Group::getLevel)
                .thenComparing(Group::getIndex)).toList();
        return generateLinks(groups, tournamentWinners, groups.stream().max(Comparator.comparing(Group::getLevel)).orElse(new Group()).getLevel());
    }

    public List<GroupLink> generateLinks(List<Group> groups, int tournamentWinners, int tournamentLevels) {
        final List<GroupLink> groupLinks = new ArrayList<>();
        groups.forEach(group -> {
            if (group.getLevel() < tournamentLevels) {
                final int numberOfWinners = getNumberOfTotalTeamsPassNextRound(group, tournamentWinners);
                for (int winner = 0; winner < numberOfWinners; winner++) {
                    final GroupLink groupLink = new GroupLink();
                    groupLink.setSource(group);
                    final Group destination = getDestination(group, numberOfWinners, winner, groups);
                    if (destination != null) {
                        groupLink.setDestination(destination);
                        groupLinks.add(groupLink);
                    }
                    groupLink.setWinner(winner);
                }
            }
        });
        return groupLinks.stream().sorted(Comparator.<GroupLink, Integer>comparing(o -> o.getSource().getLevel())
                .thenComparing(GroupLink::getWinner).thenComparing(o -> o.getSource().getIndex())).toList();
    }

    private int getNumberOfTotalTeamsPassNextRound(Group group, int tournamentWinners) {
        if (group.getLevel() == 0) {
            return tournamentWinners;
        }
        return 1;
    }

    public Group getDestination(Group sourceGroup, int numberOfWinners, int winnerOrder, List<Group> groups) {
        final List<Group> currentLevelGroups = groups.stream().filter(group -> Objects.equals(group.getLevel(), sourceGroup.getLevel())).toList();
        final List<Group> nextLevelGroups = groups.stream().filter(group -> Objects.equals(group.getLevel(), sourceGroup.getLevel() + 1)).toList();
        try {
            final TournamentExtraProperty oddTeamsResolvedAsapProperty = tournamentExtraPropertyProvider
                    .getByTournamentAndProperty(sourceGroup.getTournament(),
                            TournamentExtraPropertyKey.ODD_TEAMS_RESOLVED_ASAP, DEFAULT_ODD_TEAMS_RESOLUTION_ASAP);
            if (Boolean.parseBoolean(oddTeamsResolvedAsapProperty.getPropertyValue()) && sourceGroup.getLevel() == 0
                    //If has same number of groups, can be use the standard way.
                    && currentLevelGroups.size() != nextLevelGroups.size()) {
                return nextLevelGroups.get(obtainPositionOfWinnerRemovingOddNumbers(sourceGroup.getIndex(),
                        currentLevelGroups.size(), nextLevelGroups.size(), winnerOrder));
            } else {
                return nextLevelGroups.get(obtainPositionOfWinnerAsBinaryTree(groups, sourceGroup.getIndex(),
                        currentLevelGroups.size(), numberOfWinners, winnerOrder, sourceGroup.getLevel()));
            }
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private int obtainPositionOfWinnerAsBinaryTree(List<Group> groups, int sourceGroupLevelIndex, int sourceGroupLevelSize, int numberOfWinners,
                                                   int winnerOrder, int sourceLevel) {
        final List<Group> previousLevelGroups;
        if (sourceLevel > 0) {
            previousLevelGroups = groups.stream().filter(group -> Objects.equals(group.getLevel(), 0)).toList();
        } else {
            previousLevelGroups = new ArrayList<>();
        }

        //Special case: two odd number of groups in two consecutive levels. Only one winner.
        //Ensure no team passes two levels without fighting.
        if (numberOfWinners == 1 && previousLevelGroups.size() % 2 == 1 && sourceGroupLevelSize % 2 == 1
                && previousLevelGroups.size() != sourceGroupLevelSize) {
            return (sourceGroupLevelIndex + 1) / 2;
        }

        //Odd groups number but two winners on each group:
        if (numberOfWinners == 2 && sourceGroupLevelSize % 2 == 1) {
            //First on same group.
            if (winnerOrder == 0) {
                return sourceGroupLevelIndex;
            } else if (winnerOrder == 1) {
                //Second winner to next group. The Last one goes to the first group.
                return (sourceGroupLevelIndex + 1) % sourceGroupLevelSize;
            }
        }

        //Standard case.
        if (winnerOrder == 0) {
            //Half-groups number on next level.
            if (sourceLevel > 0 || numberOfWinners == 1) {
                return sourceGroupLevelIndex / 2;
            } else {
                //Same number of groups on the next level (needed for two winners).
                return sourceGroupLevelIndex;
            }
        } else if (winnerOrder == 1) {
            //Second winner in standard case, goes to the opposite group.
            if (sourceLevel > 0) {
                //+1 for rounding, -1 as a list starts in 0.
                return (sourceGroupLevelSize - sourceGroupLevelIndex + 1) / 2 - 1;
            } else {
                return (sourceGroupLevelSize - sourceGroupLevelIndex - 1);
            }
        } else {
            return -1;
        }
    }

    private int obtainPositionOfWinnerRemovingOddNumbers(int sourceGroupLevelIndex, int sourceGroupLevelSize, int destinationGroupLevelSize,
                                                         int winnerOrder) {
        //Standard case.
        if (winnerOrder == 0) {
            if (sourceGroupLevelIndex <= sourceGroupLevelSize / 2) {
                return sourceGroupLevelIndex;
            } else {
                return destinationGroupLevelSize - (sourceGroupLevelSize - sourceGroupLevelIndex - 1) - 1;
            }
        } else if (winnerOrder == 1) {
            if (sourceGroupLevelIndex <= sourceGroupLevelSize / 2) {
                //Last -1 is for list starts at 0.
                return (destinationGroupLevelSize / 2) + (sourceGroupLevelIndex / 2);
            } else {
                return (destinationGroupLevelSize / 2) - ((sourceGroupLevelSize - (sourceGroupLevelIndex + 1)) / 2) - 1;
            }
        } else {
            return -1;
        }
    }
}
