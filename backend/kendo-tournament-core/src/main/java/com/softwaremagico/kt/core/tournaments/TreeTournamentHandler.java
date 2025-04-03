package com.softwaremagico.kt.core.tournaments;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2025 Softwaremagico
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

import com.softwaremagico.kt.core.exceptions.InvalidGroupException;
import com.softwaremagico.kt.core.exceptions.LevelNotFinishedException;
import com.softwaremagico.kt.core.managers.CompleteGroupFightManager;
import com.softwaremagico.kt.core.managers.MinimumGroupFightManager;
import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.core.providers.FightProvider;
import com.softwaremagico.kt.core.providers.GroupLinkProvider;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.RankingProvider;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.core.providers.TournamentExtraPropertyProvider;
import com.softwaremagico.kt.core.score.ScoreOfTeam;
import com.softwaremagico.kt.logger.KendoTournamentLogger;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.GroupLink;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.values.LeagueFightsOrder;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import com.softwaremagico.kt.utils.GroupUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class TreeTournamentHandler extends LeagueHandler {
    public static final boolean DEFAULT_ODD_TEAMS_RESOLUTION_ASAP = true;
    private final GroupProvider groupProvider;
    private final TournamentExtraPropertyProvider tournamentExtraPropertyProvider;
    private final CompleteGroupFightManager completeGroupFightManager;
    private final MinimumGroupFightManager minimumGroupFightManager;
    private final FightProvider fightProvider;
    private final GroupLinkProvider groupLinkProvider;
    private final RankingProvider rankingProvider;


    public TreeTournamentHandler(GroupProvider groupProvider, TeamProvider teamProvider, RankingProvider rankingProvider,
                                 TournamentExtraPropertyProvider tournamentExtraPropertyProvider, CompleteGroupFightManager completeGroupFightManager,
                                 MinimumGroupFightManager minimumGroupFightManager, FightProvider fightProvider, GroupLinkProvider groupLinkProvider) {
        super(groupProvider, teamProvider, rankingProvider, tournamentExtraPropertyProvider);
        this.rankingProvider = rankingProvider;
        this.groupProvider = groupProvider;
        this.tournamentExtraPropertyProvider = tournamentExtraPropertyProvider;
        this.completeGroupFightManager = completeGroupFightManager;
        this.minimumGroupFightManager = minimumGroupFightManager;
        this.fightProvider = fightProvider;
        this.groupLinkProvider = groupLinkProvider;
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
                //Ignored.
            }
        }
        return 1;
    }


    private boolean getMaxGroupFights(Tournament tournament) {
        final TournamentExtraProperty maximizeFightsProperty = tournamentExtraPropertyProvider.getByTournamentAndProperty(tournament,
                TournamentExtraPropertyKey.MAXIMIZE_FIGHTS);

        if (maximizeFightsProperty != null) {
            try {
                return Boolean.parseBoolean(maximizeFightsProperty.getPropertyValue());
            } catch (Exception ignore) {
                //Ignored.
            }
        }
        return true;
    }


    @Override
    public Group addGroup(Tournament tournament, Group group) {
        if (group.getLevel() > 0) {
            throw new InvalidGroupException(this.getClass(), "Groups can only be added at level 0.");
        }
        final Group savedGroup = groupProvider.addGroup(tournament, group);
        adjustGroupSize(tournament, getNumberOfWinners(tournament));
        adjustGroupsShiaijos(tournament);
        return savedGroup;
    }


    /**
     * Clean up all inner levels and recalculate them.
     *
     * @param tournament      The tournament to be updated.
     * @param numberOfWinners Number of winners that pass from level one to level two.
     */
    public void recreateGroupSize(Tournament tournament, int numberOfWinners) {
        groupProvider.delete(tournament, 1);
        adjustGroupSize(tournament, numberOfWinners);
        adjustGroupsShiaijos(tournament);
    }


    public void adjustGroupSize(Tournament tournament, int numberOfWinners) {
        final TournamentExtraProperty oddTeamsResolvedAsapProperty = tournamentExtraPropertyProvider
                .getByTournamentAndProperty(tournament,
                        TournamentExtraPropertyKey.ODD_FIGHTS_RESOLVED_ASAP, DEFAULT_ODD_TEAMS_RESOLUTION_ASAP);

        //Update the shiaijo numbers.
        if (Boolean.parseBoolean(oddTeamsResolvedAsapProperty.getPropertyValue())) {
            adjustGroupsSizeRemovingOddNumbers(tournament, numberOfWinners);
        } else {
            adjustGroupsSizeAsBinaryTree(tournament, numberOfWinners);
        }
    }


    private void adjustGroupsSizeAsBinaryTree(Tournament tournament, int numberOfWinners) {
        //Check if inner levels must be increased on size.
        final List<Group> tournamentGroups = groupProvider.getGroups(tournament);
        final Map<Integer, List<Group>> groupsByLevel = GroupUtils.orderByLevel(tournamentGroups);
        int previousLevelSize = 0;
        for (final Integer level : new HashSet<>(groupsByLevel.keySet())) {
            while (groupsByLevel.get(level).size()
                    < (((previousLevelSize
                    // Add +1 unless the number of winners 2.
                    // This +1 will be rounded later but is needed if even teams pass from the previous level.
                    + (level == 1 && numberOfWinners == 2 ? 0 : 1))
                    //Check on level 1 the number of winners.
                    * (level == 1 ? numberOfWinners : 1)) / 2)) {
                final Group levelGroup = new Group(tournament, level, groupsByLevel.get(level).size());
                groupProvider.addGroup(tournament, levelGroup);
                groupsByLevel.get(level).add(levelGroup);
            }
            previousLevelSize = groupsByLevel.get(level).size();
        }

        //Add extra level if needed.
        addExtraLevelIfNeeded(tournament, groupsByLevel, numberOfWinners);
    }


    public void adjustGroupsSizeRemovingOddNumbers(Tournament tournament, int numberOfWinners) {
        //Check if inner levels must be increased on size.
        final List<Group> tournamentGroups = groupProvider.getGroups(tournament);
        final Map<Integer, List<Group>> groupsByLevel = GroupUtils.orderByLevel(tournamentGroups);
        int previousLevelSize = 0;
        for (final Integer level : new HashSet<>(groupsByLevel.keySet())) {
            while (level > 0
                    //It is not a power of two.
                    && (groupsByLevel.get(level).size()
                    < GroupUtils.getNextPowerOfTwo(((groupsByLevel.get(level - 1).size() * (level == 1 ? numberOfWinners : 1)) + 1) / 2))
                    //Except the Last level, that has only one group. Skip this if the previous level has more than one winner.
                    && !(groupsByLevel.get(level).size() == 1 && previousLevelSize == 2 && groupsByLevel.get(level - 1).get(0).getNumberOfWinners() > 1)) {
                final Group levelGroup = new Group(tournament, level, groupsByLevel.get(level).size());
                groupProvider.addGroup(tournament, levelGroup);
                groupsByLevel.get(level).add(levelGroup);
            }
            previousLevelSize = groupsByLevel.get(level).size();
        }

        if (addExtraLevelIfNeeded(tournament, groupsByLevel, numberOfWinners)) {
            adjustGroupsSizeRemovingOddNumbers(tournament, numberOfWinners);
        }
    }


    private boolean addExtraLevelIfNeeded(Tournament tournament, Map<Integer, List<Group>> groupsByLevel, int numberOfWinners) {
        //Add extra level if needed.
        if (groupsByLevel.get(groupsByLevel.size() - 1).size() > 1 || (groupsByLevel.size() == 1 && numberOfWinners > 1)) {
            final Integer newLevel = groupsByLevel.size();
            final Group levelGroup = new Group(tournament, newLevel, 0);
            groupsByLevel.put(newLevel, new ArrayList<>());
            groupsByLevel.get(newLevel).add(levelGroup);
            groupProvider.addGroup(tournament, levelGroup);
            return true;
        }
        return false;
    }


    private void adjustGroupsShiaijos(Tournament tournament) {
        if (tournament.getShiaijos() > 1) {
            final List<Group> tournamentGroups = groupProvider.getGroups(tournament);
            final Map<Integer, List<Group>> groupsByLevel = GroupUtils.orderByLevel(tournamentGroups);
            for (final Integer level : new HashSet<>(groupsByLevel.keySet())) {
                final int groupsByShiaijo = groupsByLevel.get(level).size() / tournament.getShiaijos();
                int currentShiaijo = 0;
                int groupsInCurrentShiaijo = 0;
                for (Group group : groupsByLevel.get(level)) {
                    if (groupsInCurrentShiaijo >= (currentShiaijo < groupsByLevel.get(level).size() % tournament.getShiaijos()
                            ? groupsByShiaijo + 1 : groupsByShiaijo)) {
                        currentShiaijo++;
                        groupsInCurrentShiaijo = 0;
                    }
                    //Correct shiaijo if needed.
                    if (group.getShiaijo() != currentShiaijo) {
                        KendoTournamentLogger.info(this.getClass(), "Adjusting shiaijo for group '{}' to '{}'", group, currentShiaijo);
                        group.setShiaijo(currentShiaijo);
                        groupProvider.save(group);
                    }
                    groupsInCurrentShiaijo++;
                }
            }
        }
    }


    @Override
    public void removeGroup(Tournament tournament, Integer groupLevel, Integer groupIndex) {
        if (groupLevel > 0) {
            throw new InvalidGroupException(this.getClass(), "Groups can only be deleted at level 0.");
        }

        groupProvider.deleteGroupByLevelAndIndex(tournament, groupLevel, groupIndex);
        final int numberOfWinners = getNumberOfWinners(tournament);

        final TournamentExtraProperty oddTeamsResolvedAsapProperty = tournamentExtraPropertyProvider
                .getByTournamentAndProperty(tournament,
                        TournamentExtraPropertyKey.ODD_FIGHTS_RESOLVED_ASAP, DEFAULT_ODD_TEAMS_RESOLUTION_ASAP);


        //Check if inner levels must be decreased on size.
        final List<Group> tournamentGroups = groupProvider.getGroups(tournament);
        final Map<Integer, List<Group>> groupsByLevel = GroupUtils.orderByLevel(tournamentGroups);
        int previousLevelSize = Integer.MAX_VALUE - 1;
        for (final Integer level : new HashSet<>(groupsByLevel.keySet())) {
            //If previous level has no groups, remove all.
            if (level > 0 && (!groupsByLevel.containsKey(level - 1) || groupsByLevel.get(level - 1).isEmpty())) {
                while (!groupsByLevel.get(level).isEmpty()) {
                    groupProvider.deleteGroupByLevelAndIndex(tournament, level, groupsByLevel.get(level).size() - 1);
                    groupsByLevel.get(level).remove(groupsByLevel.get(level).size() - 1);
                }
            }
            if (Boolean.parseBoolean(oddTeamsResolvedAsapProperty.getPropertyValue())) {
                // Normal levels, the number of groups must be the half rounded up that the previous one.
                if ((level > 1)) {
                    while ((previousLevelSize == 1 && !groupsByLevel.get(level).isEmpty())
                            || groupsByLevel.get(level).size() > ((previousLevelSize + 1) / 2)) {
                        groupProvider.deleteGroupByLevelAndIndex(tournament, level, groupsByLevel.get(level).size() - 1);
                        groupsByLevel.get(level).remove(groupsByLevel.get(level).size() - 1);
                    }
                } else if (level == 1) {
                    while (GroupUtils.getNextPowerOfTwo((groupsByLevel.get(0).size() * numberOfWinners) / 2) < groupsByLevel.get(level).size()) {
                        groupProvider.deleteGroupByLevelAndIndex(tournament, level, groupsByLevel.get(level).size() - 1);
                        groupsByLevel.get(level).remove(groupsByLevel.get(level).size() - 1);
                    }
                }
            } else {
                // Normal levels, the number of groups must be the half rounded up that the previous one.
                if (((numberOfWinners == 1 || level > 1)
                        && (previousLevelSize == 1 || groupsByLevel.get(level).size() > ((previousLevelSize + 1) / 2)))
                        // The First level with 2 winners must have the same size that level zero.
                        || (numberOfWinners == 2 && groupsByLevel.get(level).size() > previousLevelSize)) {
                    groupProvider.deleteGroupByLevelAndIndex(tournament, level, groupsByLevel.get(level).size() - 1);
                    groupsByLevel.get(level).remove(groupsByLevel.get(level).size() - 1);
                }
            }
            previousLevelSize = groupsByLevel.get(level).size();
        }
        adjustGroupsShiaijos(tournament);
    }


    @Override
    public List<Fight> createFights(Tournament tournament, TeamsOrder teamsOrder, Integer level, String createdBy) {
        final List<Group> tournamentGroups = groupProvider.getGroups(tournament);
        final List<Fight> createdFights = new ArrayList<>();
        tournamentGroups.forEach(group -> {
            if (Objects.equals(group.getLevel(), level)) {
                final List<Fight> fights;
                if (getMaxGroupFights(tournament)) {
                    final TournamentExtraProperty extraProperty = getLeagueFightsOrder(tournament);
                    fights = fightProvider.saveAll(completeGroupFightManager.createFights(tournament, group.getTeams(), TeamsOrder.NONE,
                            level, group.getShiaijo(), LeagueFightsOrder.get(extraProperty.getPropertyValue()) == LeagueFightsOrder.FIFO, createdBy));
                } else {
                    fights = fightProvider.saveAll(minimumGroupFightManager.createFights(tournament, group.getTeams(),
                            TeamsOrder.NONE, level, group.getShiaijo(), createdBy));
                }
                group.setFights(fights);
                groupProvider.save(group);
                createdFights.addAll(fights);
            }
        });
        return createdFights;
    }


    private Integer getNextEmptyLevel(List<Group> tournamentGroups) {
        if (tournamentGroups == null) {
            return null;
        }
        for (Group group : tournamentGroups) {
            if (group.getTeams().isEmpty()) {
                return group.getLevel();
            }
        }
        return null;
    }


    private void populateLevel(Tournament tournament, int level) throws LevelNotFinishedException {
        final List<GroupLink> links = groupLinkProvider.generateLinks(tournament);
        final List<GroupLink> levelLinks = links.stream().filter(link -> link.getDestination().getLevel() == level).toList();
        final Set<Group> groupsOfLevel = new HashSet<>();
        for (GroupLink link : levelLinks) {
            final List<ScoreOfTeam> teamsRanking = rankingProvider.getTeamsScoreRanking(link.getSource());
            checkDrawScore(link.getSource(), teamsRanking, link.getWinner());
            if (link.getWinner() != null && teamsRanking.get(link.getWinner()) != null && teamsRanking.get(link.getWinner()).getTeam() != null) {
                link.getDestination().getTeams().add(teamsRanking.get(link.getWinner()).getTeam());
            } else {
                KendoTournamentLogger.warning(this.getClass(), "Missing data for level '{}' population with winner '{}' using ranking:\n\t{}",
                        level, link.getWinner(), link.getWinner() != null ? teamsRanking.get(link.getWinner()) : null);
            }
            groupsOfLevel.add(link.getDestination());
        }
        groupProvider.saveAll(groupsOfLevel);
    }


    private void checkDrawScore(Group group, List<ScoreOfTeam> scoresOfTeamsDTO, int numberOfWinners) {
        for (int i = 0; i <= numberOfWinners; i++) {
            final int winner = i;
            final List<ScoreOfTeam> sameLevelScore = scoresOfTeamsDTO.stream().filter(scoreOfTeamDTO -> scoreOfTeamDTO.getSortingIndex() == winner).toList();
            if (sameLevelScore.size() > 1) {
                KendoTournamentLogger.debug(this.getClass(), "Teams with same score are '{}'.", sameLevelScore.stream().map(ScoreOfTeam::getTeam).toList());
                throw new LevelNotFinishedException(this.getClass(), "There is a draw value on winner '" + winner + "' on group '" + group + "'");
            }
        }
    }


    @Override
    public List<Fight> generateNextFights(Tournament tournament, String createdBy) {
        //Get the next level to continue if exists.
        final List<Group> tournamentGroups = groupProvider.getGroups(tournament);
        if (tournamentGroups == null) {
            return new ArrayList<>();
        }

        final Integer nextLevel = getNextEmptyLevel(tournamentGroups);
        if (nextLevel == null) {
            KendoTournamentLogger.debug(this.getClass(), "No next level to populate!");
            return new ArrayList<>();
        }

        //Populate the next level with winners.
        populateLevel(tournament, nextLevel);


        //Generate next Level fights.
        return createFights(tournament, TeamsOrder.NONE, nextLevel, createdBy);
    }
}
