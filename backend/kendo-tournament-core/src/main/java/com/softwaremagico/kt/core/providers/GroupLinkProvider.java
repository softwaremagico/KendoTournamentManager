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

import com.softwaremagico.kt.core.providers.links.Pool11To16winners2;
import com.softwaremagico.kt.core.providers.links.Pool11To8winners1;
import com.softwaremagico.kt.core.providers.links.Pool12To16winners2;
import com.softwaremagico.kt.core.providers.links.Pool13To16winners2;
import com.softwaremagico.kt.core.providers.links.Pool3to4winners2;
import com.softwaremagico.kt.core.providers.links.Pool6to4winners1;
import com.softwaremagico.kt.core.providers.links.Pool6to8winners2;
import com.softwaremagico.kt.core.providers.links.Pool9to8winners1;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.GroupLink;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.repositories.GroupLinkRepository;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import com.softwaremagico.kt.utils.GroupUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static com.softwaremagico.kt.core.tournaments.TreeTournamentHandler.DEFAULT_ODD_TEAMS_RESOLUTION_ASAP;

/**
 * Requeriements for championships arrows calculations: *
 * -   One pool must always have one team.
 * -   two 1st winners cannot be assigned to the same pool.
 * -   When possible, 1st winners must not change shiaijo.
 * -   Only a 2nd winner can be on a bye, if all 1st winners are on a bye.
 * -   Two teams that have been faced in the first column must avoid to face again until the end of the tournament
 */
@Service
public class GroupLinkProvider extends CrudProvider<GroupLink, Integer, GroupLinkRepository> {

    private static final int SOURCE_13 = 13;
    private static final int SOURCE_12 = 12;
    private static final int SOURCE_11 = 11;
    private static final int SOURCE_9 = 9;
    private static final int SOURCE_6 = 6;
    private static final int SOURCE_3 = 3;

    private final TournamentExtraPropertyProvider tournamentExtraPropertyProvider;
    private final GroupProvider groupProvider;

    public GroupLinkProvider(GroupLinkRepository repository,
                             TournamentExtraPropertyProvider tournamentExtraPropertyProvider,
                             GroupProvider groupProvider) {
        super(repository);
        this.tournamentExtraPropertyProvider = tournamentExtraPropertyProvider;
        this.groupProvider = groupProvider;
    }

    public List<GroupLink> getGroupLinks(Tournament tournament) {
        final List<GroupLink> storedGroupLinks = getRepository().findByTournament(tournament);
        if (!storedGroupLinks.isEmpty()) {
            return storedGroupLinks;
        }
        return generateLinks(tournament);
    }

    public List<GroupLink> generateLinks(Tournament tournament) {
        final TournamentExtraProperty numberOfWinners = tournamentExtraPropertyProvider.getByTournamentAndProperty(
                tournament, TournamentExtraPropertyKey.NUMBER_OF_WINNERS);
        int tournamentWinners;
        try {
            tournamentWinners = Integer.parseInt(numberOfWinners.getPropertyValue());
        } catch (Exception ignored) {
            tournamentWinners = 1;
        }
        final List<Group> groups = groupProvider.getGroups(tournament).stream().sorted(Comparator.comparing(Group::getLevel)
                .thenComparing(Group::getIndex)).toList();
        return generateLinks(groups, tournamentWinners, groups.stream().max(Comparator.comparing(Group::getLevel)).orElse(new Group()).getLevel());
    }

    public List<GroupLink> generateLinks(List<Group> groups, int tournamentWinners, int tournamentLevels) {
        return generateLinks(groups, tournamentWinners, tournamentLevels, 0);
    }

    public List<GroupLink> generateLinks(List<Group> groups, int tournamentWinners, int tournamentLevels, int fromLevel) {
        final List<GroupLink> groupLinks = new ArrayList<>();
        groups.forEach(group -> {
            if (group.getLevel() < tournamentLevels && group.getLevel() >= fromLevel) {
                final int numberOfWinners = getNumberOfTotalTeamsPassNextRound(group, tournamentWinners);
                for (int winner = 0; winner < numberOfWinners; winner++) {
                    final GroupLink groupLink = new GroupLink();
                    groupLink.setSource(group);
                    groupLink.setTournament(group.getTournament());
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
            return resolveDestination(sourceGroup, numberOfWinners, winnerOrder, groups, currentLevelGroups, nextLevelGroups);
        } catch (IndexOutOfBoundsException ignored) {
            return null;
        }
    }

    private Group resolveDestination(Group sourceGroup, int numberOfWinners, int winnerOrder, List<Group> groups,
                                     List<Group> currentLevelGroups, List<Group> nextLevelGroups) {
        if (useOddTreeResolution(sourceGroup, currentLevelGroups, nextLevelGroups)) {
            return resolveOddTreeDestination(sourceGroup, numberOfWinners, winnerOrder, currentLevelGroups, nextLevelGroups);
        }
        return nextLevelGroups.get(obtainPositionOfWinnerAsBinaryTree(groups, sourceGroup.getIndex(), currentLevelGroups.size(),
                numberOfWinners, winnerOrder, sourceGroup.getLevel()));
    }

    private boolean useOddTreeResolution(Group sourceGroup, List<Group> currentLevelGroups, List<Group> nextLevelGroups) {
        final TournamentExtraProperty oddTeamsResolvedAsapProperty = tournamentExtraPropertyProvider
                .getByTournamentAndProperty(sourceGroup.getTournament(), TournamentExtraPropertyKey.ODD_FIGHTS_RESOLVED_ASAP,
                        DEFAULT_ODD_TEAMS_RESOLUTION_ASAP);
        return Boolean.parseBoolean(oddTeamsResolvedAsapProperty.getPropertyValue()) && sourceGroup.getLevel() == 0
                //If it has the same number of groups, can be use the standard way.
                && currentLevelGroups.size() != nextLevelGroups.size() && !GroupUtils.isPowerOfTwo(currentLevelGroups.size());
    }

    private Group resolveOddTreeDestination(Group sourceGroup, int numberOfWinners, int winnerOrder,
                                            List<Group> currentLevelGroups, List<Group> nextLevelGroups) {
        final int templateDestination = getWinnersByFederationTemplates(sourceGroup.getIndex(), currentLevelGroups.size(), numberOfWinners,
                winnerOrder);
        if (templateDestination >= 0) {
            return nextLevelGroups.get(templateDestination);
        }
        final int destinationIndex = currentLevelGroups.size() < nextLevelGroups.size() && numberOfWinners > 1
                && currentLevelGroups.size() % 2 == 1
                ? spreadWinnersOnTreeAsMuchAsPossible(sourceGroup.getIndex(), currentLevelGroups.size(), nextLevelGroups.size(), winnerOrder)
                : currentLevelGroups.size() % 2 == 0
                        ? spreadWinnersOnTreeAsMuchAsPossible(sourceGroup.getIndex(), currentLevelGroups.size(), nextLevelGroups.size(), winnerOrder)
                        : obtainPositionOfWinnerNonBinaryTreeOddSize(sourceGroup.getIndex(), currentLevelGroups.size(), nextLevelGroups.size(),
                                winnerOrder);
        return nextLevelGroups.get(destinationIndex);
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

        return switch (winnerOrder) {
            case 0 -> getFirstWinnerBinaryTreePosition(sourceGroupLevelIndex, sourceGroupLevelSize, numberOfWinners, sourceLevel);
            case 1 -> getSecondWinnerBinaryTreePosition(sourceGroupLevelIndex, sourceGroupLevelSize, sourceLevel);
            default -> -1;
        };
    }

    private int getFirstWinnerBinaryTreePosition(int sourceGroupLevelIndex, int sourceGroupLevelSize, int numberOfWinners, int sourceLevel) {
        //Half-groups number on next level.
        if (sourceLevel > 0 || numberOfWinners == 1) {
            return sourceGroupLevelIndex / 2;
        }
        //Same number of groups on the next level (needed for two winners).
        return sourceGroupLevelIndex;
    }

    private int getSecondWinnerBinaryTreePosition(int sourceGroupLevelIndex, int sourceGroupLevelSize, int sourceLevel) {
        //Second winner in standard case, goes to the opposite group.
        if (sourceLevel > 0) {
            //+1 for rounding, -1 as a list starts in 0.
            return (sourceGroupLevelSize - sourceGroupLevelIndex + 1) / 2 - 1;
        }
        return sourceGroupLevelSize - sourceGroupLevelIndex - 1;
    }

    private int obtainPositionOfWinnerNonBinaryTreeOddSize(int sourceGroupLevelIndex, int sourceGroupLevelSize, int destinationGroupLevelSize,
                                                           int winnerOrder) {
        return switch (winnerOrder) {
            case 0 -> getFirstWinnerOddTreePosition(sourceGroupLevelIndex, sourceGroupLevelSize, destinationGroupLevelSize);
            case 1 -> getSecondWinnerOddTreePosition(sourceGroupLevelIndex, sourceGroupLevelSize, destinationGroupLevelSize);
            default -> -1;
        };
    }

    private int getFirstWinnerOddTreePosition(int sourceGroupLevelIndex, int sourceGroupLevelSize, int destinationGroupLevelSize) {
        if (sourceGroupLevelIndex <= (sourceGroupLevelSize - 1) / 2) {
            return sourceGroupLevelIndex / 2;
        }
        return destinationGroupLevelSize - (sourceGroupLevelSize - sourceGroupLevelIndex) / 2 - 1;
    }

    private int getSecondWinnerOddTreePosition(int sourceGroupLevelIndex, int sourceGroupLevelSize, int destinationGroupLevelSize) {
        if (sourceGroupLevelIndex <= sourceGroupLevelSize / 2) {
            //Last -1 is for list starts at 0.
            return (destinationGroupLevelSize / 2) + (sourceGroupLevelIndex / 2);
        }
        return (destinationGroupLevelSize / 2) - ((sourceGroupLevelSize - (sourceGroupLevelIndex + 1)) / 2) - 1;
    }


    private int spreadWinnersOnTreeAsMuchAsPossible(int sourceGroupLevelIndex, int sourceGroupLevelSize, int destinationGroupLevelSize,
                                                    int winnerOrder) {
        return switch (winnerOrder) {
            case 0 -> spreadFirstWinner(sourceGroupLevelIndex, sourceGroupLevelSize, destinationGroupLevelSize);
            case 1 -> spreadSecondWinner(sourceGroupLevelIndex, sourceGroupLevelSize, destinationGroupLevelSize);
            default -> -1;
        };
    }

    private int spreadFirstWinner(int sourceGroupLevelIndex, int sourceGroupLevelSize, int destinationGroupLevelSize) {
        if (sourceGroupLevelIndex <= (sourceGroupLevelSize - 1) / 2) {
            return sourceGroupLevelIndex;
        }
        return destinationGroupLevelSize - (sourceGroupLevelSize - sourceGroupLevelIndex - 1) - 1;
    }

    private int spreadSecondWinner(int sourceGroupLevelIndex, int sourceGroupLevelSize, int destinationGroupLevelSize) {
        final int groupsDifferenceBetweenSecondAndFirstLevel = destinationGroupLevelSize - sourceGroupLevelSize;
        final int firstHalf = (sourceGroupLevelSize + 1) / 2;
        final boolean firstHalfIndex = sourceGroupLevelIndex <= (sourceGroupLevelSize - 1) / 2;
        if (groupsDifferenceBetweenSecondAndFirstLevel > firstHalf) {
            return firstHalfIndex
                    ? destinationGroupLevelSize - sourceGroupLevelSize + sourceGroupLevelIndex
                    : sourceGroupLevelIndex;
        }
        return firstHalfIndex
                ? (destinationGroupLevelSize / 2) + sourceGroupLevelIndex - (destinationGroupLevelSize - sourceGroupLevelSize) / 2
                : (destinationGroupLevelSize / 2) - (sourceGroupLevelSize - sourceGroupLevelIndex - 1);
    }

    private int getWinnersByFederationTemplates(int sourceGroupLevelIndex, int sourceGroupLevelSize, int numberOfWinners, int winnerOrder) {
        return switch (sourceGroupLevelSize) {
            case SOURCE_13 -> numberOfWinners == 2
                    ? Pool13To16winners2.getDestination(sourceGroupLevelIndex, winnerOrder)
                    : -1;
            case SOURCE_12 -> numberOfWinners == 2
                    ? Pool12To16winners2.getDestination(sourceGroupLevelIndex, winnerOrder)
                    : -1;
            case SOURCE_11 -> switch (numberOfWinners) {
                case 1 -> Pool11To8winners1.getDestination(sourceGroupLevelIndex, winnerOrder);
                case 2 -> Pool11To16winners2.getDestination(sourceGroupLevelIndex, winnerOrder);
                default -> -1;
            };
            case SOURCE_9 -> numberOfWinners == 1
                    ? Pool9to8winners1.getDestination(sourceGroupLevelIndex, winnerOrder)
                    : -1;
            case SOURCE_6 -> numberOfWinners == 1
                    ? Pool6to4winners1.getDestination(sourceGroupLevelIndex, winnerOrder)
                    : Pool6to8winners2.getDestination(sourceGroupLevelIndex, winnerOrder);
            case SOURCE_3 -> numberOfWinners == 2
                    ? Pool3to4winners2.getDestination(sourceGroupLevelIndex, winnerOrder)
                    : -1;
            default -> -1;
        };
    }

    public void deleteByTournament(Tournament tournament) {
        getRepository().deleteByTournament(tournament);
    }
}
