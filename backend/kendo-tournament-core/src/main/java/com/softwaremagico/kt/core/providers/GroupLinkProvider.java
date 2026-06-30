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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    // Pool mapping strategy: key = "sourceSize:numberOfWinners", value = pool selector function
    private static final Map<String, PoolSelector> POOL_STRATEGIES = initializePoolStrategies();

    @FunctionalInterface
    private interface PoolSelector {
        int getDestination(int sourceGroupLevelIndex, int winnerOrder);
    }

    private static Map<String, PoolSelector> initializePoolStrategies() {
        final Map<String, PoolSelector> strategies = new HashMap<>();
        strategies.put("13:2", Pool13To16winners2::getDestination);
        strategies.put("12:2", Pool12To16winners2::getDestination);
        strategies.put("11:1", Pool11To8winners1::getDestination);
        strategies.put("11:2", Pool11To16winners2::getDestination);
        strategies.put("9:1", Pool9to8winners1::getDestination);
        strategies.put("6:1", Pool6to4winners1::getDestination);
        strategies.put("6:2", Pool6to8winners2::getDestination);
        strategies.put("3:2", Pool3to4winners2::getDestination);
        return strategies;
    }

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
        } catch (Exception e) {
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
            final TournamentExtraProperty oddTeamsResolvedAsapProperty = tournamentExtraPropertyProvider
                    .getByTournamentAndProperty(sourceGroup.getTournament(),
                            TournamentExtraPropertyKey.ODD_FIGHTS_RESOLVED_ASAP, DEFAULT_ODD_TEAMS_RESOLUTION_ASAP);

            if (shouldUseFederationTemplates(oddTeamsResolvedAsapProperty, sourceGroup, currentLevelGroups, nextLevelGroups)) {
                return getDestinationUsingFederationTemplates(sourceGroup, numberOfWinners, winnerOrder, currentLevelGroups, nextLevelGroups);
            } else {
                final int position = obtainPositionOfWinnerAsBinaryTree(groups, sourceGroup.getIndex(),
                        currentLevelGroups.size(), numberOfWinners, winnerOrder, sourceGroup.getLevel());
                return nextLevelGroups.get(position);
            }
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private boolean shouldUseFederationTemplates(TournamentExtraProperty oddTeamsResolvedAsapProperty, Group sourceGroup,
                                                   List<Group> currentLevelGroups, List<Group> nextLevelGroups) {
        return Boolean.parseBoolean(oddTeamsResolvedAsapProperty.getPropertyValue())
               && sourceGroup.getLevel() == 0
               && currentLevelGroups.size() != nextLevelGroups.size()
               && !GroupUtils.isPowerOfTwo(currentLevelGroups.size());
    }

    private Group getDestinationUsingFederationTemplates(Group sourceGroup, int numberOfWinners, int winnerOrder,
                                                         List<Group> currentLevelGroups, List<Group> nextLevelGroups) {
        final int templateDestination = getWinnersByFederationTemplates(sourceGroup.getIndex(), currentLevelGroups.size(),
                numberOfWinners, winnerOrder);

        if (templateDestination >= 0) {
            return nextLevelGroups.get(templateDestination);
        }

        final int position = getFederationFallbackPosition(sourceGroup.getIndex(), currentLevelGroups.size(),
                nextLevelGroups.size(), numberOfWinners, winnerOrder);
        return nextLevelGroups.get(position);
    }

    private int getFederationFallbackPosition(int sourceGroupIndex, int currentLevelSize, int nextLevelSize,
                                              int numberOfWinners, int winnerOrder) {
        final boolean shouldSpreadByAsapRule = currentLevelSize < nextLevelSize
                && numberOfWinners > 1
                && currentLevelSize % 2 == 1;
        if (shouldSpreadByAsapRule || currentLevelSize % 2 == 0) {
            return spreadWinnersOnTreeAsMuchAsPossible(sourceGroupIndex, currentLevelSize, nextLevelSize, winnerOrder);
        }
        return obtainPositionOfWinnerNonBinaryTreeOddSize(sourceGroupIndex, currentLevelSize, nextLevelSize, winnerOrder);
    }


    private int obtainPositionOfWinnerAsBinaryTree(List<Group> groups, int sourceGroupLevelIndex, int sourceGroupLevelSize, int numberOfWinners,
                                                   int winnerOrder, int sourceLevel) {
        final int previousLevelSize = getPreviousLevelSize(groups, sourceLevel);

        if (isOddConsecutiveSingleWinnerCase(previousLevelSize, sourceGroupLevelSize, numberOfWinners)) {
            return (sourceGroupLevelIndex + 1) / 2;
        }

        final int oddTwoWinnersPosition = getOddTwoWinnersPosition(sourceGroupLevelIndex, sourceGroupLevelSize, numberOfWinners, winnerOrder);
        if (oddTwoWinnersPosition >= 0) {
            return oddTwoWinnersPosition;
        }

        return getStandardWinnerPosition(sourceGroupLevelIndex, sourceGroupLevelSize, numberOfWinners, winnerOrder, sourceLevel);
    }

    private int getPreviousLevelSize(List<Group> groups, int sourceLevel) {
        if (sourceLevel <= 0) {
            return 0;
        }
        return (int) groups.stream().filter(group -> Objects.equals(group.getLevel(), 0)).count();
    }

    private boolean isOddConsecutiveSingleWinnerCase(int previousLevelSize, int sourceGroupLevelSize, int numberOfWinners) {
        return numberOfWinners == 1
                && previousLevelSize % 2 == 1
                && sourceGroupLevelSize % 2 == 1
                && previousLevelSize != sourceGroupLevelSize;
    }

    private int getOddTwoWinnersPosition(int sourceGroupLevelIndex, int sourceGroupLevelSize, int numberOfWinners, int winnerOrder) {
        if (numberOfWinners != 2 || sourceGroupLevelSize % 2 != 1) {
            return -1;
        }
        return switch (winnerOrder) {
            case 0 -> sourceGroupLevelIndex;
            case 1 -> (sourceGroupLevelIndex + 1) % sourceGroupLevelSize;
            default -> -1;
        };
    }

    private int getStandardWinnerPosition(int sourceGroupLevelIndex, int sourceGroupLevelSize, int numberOfWinners,
                                          int winnerOrder, int sourceLevel) {
        return switch (winnerOrder) {
            case 0 -> sourceLevel > 0 || numberOfWinners == 1 ? sourceGroupLevelIndex / 2 : sourceGroupLevelIndex;
            case 1 -> sourceLevel > 0
                    ? (sourceGroupLevelSize - sourceGroupLevelIndex + 1) / 2 - 1
                    : (sourceGroupLevelSize - sourceGroupLevelIndex - 1);
            default -> -1;
        };
    }

    private int obtainPositionOfWinnerNonBinaryTreeOddSize(int sourceGroupLevelIndex, int sourceGroupLevelSize, int destinationGroupLevelSize,
                                                           int winnerOrder) {
        return switch (winnerOrder) {
            case 0 -> this.obtainFirstWinnerNonBinaryTreeOddSize(sourceGroupLevelIndex, sourceGroupLevelSize, destinationGroupLevelSize);
            case 1 -> this.obtainSecondWinnerNonBinaryTreeOddSize(sourceGroupLevelIndex, sourceGroupLevelSize, destinationGroupLevelSize);
            default -> -1;
        };
    }

    private int obtainFirstWinnerNonBinaryTreeOddSize(int sourceGroupLevelIndex, int sourceGroupLevelSize, int destinationGroupLevelSize) {
        if (sourceGroupLevelIndex <= (sourceGroupLevelSize - 1) / 2) {
            return sourceGroupLevelIndex / 2;
        } else {
            return destinationGroupLevelSize - (sourceGroupLevelSize - sourceGroupLevelIndex) / 2 - 1;
        }
    }

    private int obtainSecondWinnerNonBinaryTreeOddSize(int sourceGroupLevelIndex, int sourceGroupLevelSize, int destinationGroupLevelSize) {
        if (sourceGroupLevelIndex <= (sourceGroupLevelSize) / 2) {
            return (destinationGroupLevelSize / 2) + (sourceGroupLevelIndex / 2);
        } else {
            return (destinationGroupLevelSize / 2) - ((sourceGroupLevelSize - (sourceGroupLevelIndex + 1)) / 2) - 1;
        }
    }


    private int spreadWinnersOnTreeAsMuchAsPossible(int sourceGroupLevelIndex, int sourceGroupLevelSize, int destinationGroupLevelSize,
                                                    int winnerOrder) {
        return switch (winnerOrder) {
            case 0 -> this.getSpreadFirstWinnerPosition(sourceGroupLevelIndex, sourceGroupLevelSize, destinationGroupLevelSize);
            case 1 -> this.getSpreadSecondWinnerPosition(sourceGroupLevelIndex, sourceGroupLevelSize, destinationGroupLevelSize);
            default -> -1;
        };
    }

    private int getSpreadFirstWinnerPosition(int sourceGroupLevelIndex, int sourceGroupLevelSize, int destinationGroupLevelSize) {
        if (sourceGroupLevelIndex <= (sourceGroupLevelSize - 1) / 2) {
            return sourceGroupLevelIndex;
        }
        return destinationGroupLevelSize - (sourceGroupLevelSize - sourceGroupLevelIndex - 1) - 1;
    }

    private int getSpreadSecondWinnerPosition(int sourceGroupLevelIndex, int sourceGroupLevelSize, int destinationGroupLevelSize) {
        final int groupsDifferenceBetweenSecondAndFirstLevel = destinationGroupLevelSize - sourceGroupLevelSize;
        final int firstHalf = (sourceGroupLevelSize + 1) / 2;

        if (groupsDifferenceBetweenSecondAndFirstLevel > firstHalf) {
            if (sourceGroupLevelIndex <= (sourceGroupLevelSize - 1) / 2) {
                return destinationGroupLevelSize - sourceGroupLevelSize + sourceGroupLevelIndex;
            }
            return sourceGroupLevelIndex;
        }

        if (sourceGroupLevelIndex <= (sourceGroupLevelSize - 1) / 2) {
            return (destinationGroupLevelSize / 2) + sourceGroupLevelIndex - (destinationGroupLevelSize - sourceGroupLevelSize) / 2;
        }
        return (destinationGroupLevelSize / 2) - (sourceGroupLevelSize - sourceGroupLevelIndex - 1);
    }

    private int getWinnersByFederationTemplates(int sourceGroupLevelIndex, int sourceGroupLevelSize, int numberOfWinners, int winnerOrder) {
        final String key = sourceGroupLevelSize + ":" + numberOfWinners;
        final PoolSelector selector = POOL_STRATEGIES.get(key);
        if (selector != null) {
            return selector.getDestination(sourceGroupLevelIndex, winnerOrder);
        }
        return -1;
    }

    public void deleteByTournament(Tournament tournament) {
        getRepository().deleteByTournament(tournament);
    }
}
