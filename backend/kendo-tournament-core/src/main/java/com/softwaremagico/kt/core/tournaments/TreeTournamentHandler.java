package com.softwaremagico.kt.core.tournaments;

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
import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.DuelType;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.GroupLink;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Team;
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

    public TreeTournamentHandler(GroupProvider groupProvider, TeamProvider teamProvider,
            RankingProvider rankingProvider, TournamentExtraPropertyProvider tournamentExtraPropertyProvider,
            CompleteGroupFightManager completeGroupFightManager, MinimumGroupFightManager minimumGroupFightManager,
            FightProvider fightProvider, GroupLinkProvider groupLinkProvider) {
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
        return this.groupProvider.getGroups(tournament, level);
    }

    private int getNumberOfWinners(Tournament tournament) {
        final TournamentExtraProperty numberOfWinnersProperty = this.tournamentExtraPropertyProvider
                .getByTournamentAndProperty(tournament, TournamentExtraPropertyKey.NUMBER_OF_WINNERS);

        if (numberOfWinnersProperty != null) {
            try {
                return Integer.parseInt(numberOfWinnersProperty.getPropertyValue());
            } catch (Exception ex) {
                KendoTournamentLogger.debug(this.getClass(), "Invalid NUMBER_OF_WINNERS '{}': {}", numberOfWinnersProperty.getPropertyValue(),
                        ex.getMessage());
            }
        }
        return 1;
    }

    private boolean getMaxGroupFights(Tournament tournament) {
        final TournamentExtraProperty maximizeFightsProperty = this.tournamentExtraPropertyProvider
                .getByTournamentAndProperty(tournament, TournamentExtraPropertyKey.MAXIMIZE_FIGHTS);

        if (maximizeFightsProperty != null) {
            try {
                return Boolean.parseBoolean(maximizeFightsProperty.getPropertyValue());
            } catch (Exception ex) {
                KendoTournamentLogger.debug(this.getClass(), "Invalid MAXIMIZE_FIGHTS '{}': {}", maximizeFightsProperty.getPropertyValue(),
                        ex.getMessage());
            }
        }
        return true;
    }

    @Override
    public Group addGroup(Tournament tournament, Group group) {
        if (group.getLevel() > 0) {
            throw new InvalidGroupException(this.getClass(), "Groups can only be added at level 0.");
        }
      this.correctGroupWinners(tournament, group);
        final Group savedGroup = this.groupProvider.addGroup(tournament, group);
      this.adjustGroupSize(tournament, this.getNumberOfWinners(tournament));
      this.adjustGroupsShiaijos(tournament);
        return savedGroup;
    }

    private void correctGroupWinners(Tournament tournament, Group group) {
        final TournamentExtraProperty numberOfWinners = this.tournamentExtraPropertyProvider
                .getByTournamentAndProperty(tournament, TournamentExtraPropertyKey.NUMBER_OF_WINNERS);
        if (numberOfWinners != null) {
            try {
                final int winners = Integer.parseInt(numberOfWinners.getPropertyValue());
                if (group.getLevel() == 0 && winners != group.getNumberOfWinners()) {
                    group.setNumberOfWinners(winners);
                }
            } catch (final Exception e) {
                KendoTournamentLogger.errorMessage(this.getClass(), e);
            }
        }
    }

    /**
     * Clean up all inner levels and recalculate them.
     *
     * @param tournament
     *            The tournament to be updated.
     * @param numberOfWinners
     *            Number of winners that pass from level one to level two.
     */
    public void recreateGroupSize(Tournament tournament, int numberOfWinners) {
      this.groupProvider.delete(tournament, 1);
      this.adjustGroupSize(tournament, numberOfWinners);
      this.adjustGroupsShiaijos(tournament);
    }

    public void adjustGroupSize(Tournament tournament, int numberOfWinners) {
        final TournamentExtraProperty oddTeamsResolvedAsapProperty = this.tournamentExtraPropertyProvider
                .getByTournamentAndProperty(tournament, TournamentExtraPropertyKey.ODD_FIGHTS_RESOLVED_ASAP,
                        DEFAULT_ODD_TEAMS_RESOLUTION_ASAP);

        // Update the group size.
        if (Boolean.parseBoolean(oddTeamsResolvedAsapProperty.getPropertyValue())) {
          this.adjustGroupsSizeRemovingOddNumbers(tournament, numberOfWinners);
        } else {
          this.adjustGroupsSizeAsBinaryTree(tournament, numberOfWinners);
        }
    }

    private void adjustGroupsSizeAsBinaryTree(Tournament tournament, int numberOfWinners) {
        // Check if inner levels must be increased on size.
        final List<Group> tournamentGroups = this.groupProvider.getGroups(tournament);
        final Map<Integer, List<Group>> groupsByLevel = GroupUtils.orderByLevel(tournamentGroups);
        int previousLevelSize = 0;
        for (final Integer level : new HashSet<>(groupsByLevel.keySet())) {
            while (groupsByLevel.get(level).size() < (((previousLevelSize
                    // Add +1 unless the number of winners 2.
                    // This +1 will be rounded later but is needed if even teams pass from the
                    // previous level.
                    + (level == 1 && numberOfWinners == 2 ? 0 : 1))
                    // Check on level 1 the number of winners.
                    * (level == 1 ? numberOfWinners : 1)) / 2)) {
                final Group levelGroup = new Group(tournament, level, groupsByLevel.get(level).size());
              this.groupProvider.addGroup(tournament, levelGroup);
                groupsByLevel.get(level).add(levelGroup);
            }
            previousLevelSize = groupsByLevel.get(level).size();
        }

        // Add extra level if needed.
      this.addExtraLevelIfNeeded(tournament, groupsByLevel, numberOfWinners);
    }

    public void adjustGroupsSizeRemovingOddNumbers(Tournament tournament, int numberOfWinners) {
        // Check if inner levels must be increased on size.
        final List<Group> tournamentGroups = this.groupProvider.getGroups(tournament);
        final Map<Integer, List<Group>> groupsByLevel = GroupUtils.orderByLevel(tournamentGroups);
        int previousLevelSize = 0;
        for (final Integer level : new HashSet<>(groupsByLevel.keySet())) {
            while (this.shouldAddGroupRemovingOddNumbers(groupsByLevel, level, previousLevelSize,
                    numberOfWinners)) {
                this.addGroupToLevel(tournament, groupsByLevel, level);
            }
            previousLevelSize = groupsByLevel.get(level).size();
        }

        if (this.addExtraLevelIfNeeded(tournament, groupsByLevel, numberOfWinners)) {
            this.adjustGroupsSizeRemovingOddNumbers(tournament, numberOfWinners);
        }
    }

    private boolean shouldAddGroupRemovingOddNumbers(Map<Integer, List<Group>> groupsByLevel, Integer level,
            int previousLevelSize, int numberOfWinners) {
        return level > 0 && this.isLevelBelowExpectedOddResolvedSize(groupsByLevel, level, numberOfWinners)
                && !this.isSingleLastLevelException(groupsByLevel, level, previousLevelSize);
    }

    private boolean isLevelBelowExpectedOddResolvedSize(Map<Integer, List<Group>> groupsByLevel, Integer level,
            int numberOfWinners) {
        final int previousLevelWinners = level == 1 ? numberOfWinners : 1;
        final int expectedLevelSize = GroupUtils.getNextPowerOfTwo(
                ((groupsByLevel.get(level - 1).size() * previousLevelWinners) + 1) / 2);
        return groupsByLevel.get(level).size() < expectedLevelSize;
    }

    private boolean isSingleLastLevelException(Map<Integer, List<Group>> groupsByLevel, Integer level,
            int previousLevelSize) {
        return groupsByLevel.get(level).size() == 1 && previousLevelSize == 2
                && groupsByLevel.get(level - 1).getFirst().getNumberOfWinners() == 1;
    }

    private void addGroupToLevel(Tournament tournament, Map<Integer, List<Group>> groupsByLevel, Integer level) {
        final Group levelGroup = new Group(tournament, level, groupsByLevel.get(level).size());
        this.groupProvider.addGroup(tournament, levelGroup);
        groupsByLevel.get(level).add(levelGroup);
    }

    private boolean addExtraLevelIfNeeded(Tournament tournament, Map<Integer, List<Group>> groupsByLevel,
            int numberOfWinners) {
        // Add extra level if needed.
        if (groupsByLevel.get(groupsByLevel.size() - 1).size() > 1
                || (groupsByLevel.size() == 1 && numberOfWinners > 1)) {
            final Integer newLevel = groupsByLevel.size();
            final Group levelGroup = new Group(tournament, newLevel, 0);
            groupsByLevel.put(newLevel, new ArrayList<>());
            groupsByLevel.get(newLevel).add(levelGroup);
          this.groupProvider.addGroup(tournament, levelGroup);
            return true;
        }
        return false;
    }

    private void adjustGroupsShiaijos(Tournament tournament) {
        if (tournament.getShiaijos() <= 1) {
            return;
        }

        final List<Group> tournamentGroups = this.groupProvider.getGroups(tournament);
        final Map<Integer, List<Group>> groupsByLevel = GroupUtils.orderByLevel(tournamentGroups);
        for (final Integer level : new HashSet<>(groupsByLevel.keySet())) {
            this.adjustLevelShiaijos(groupsByLevel.get(level), tournament.getShiaijos());
        }
    }

    private void adjustLevelShiaijos(List<Group> groups, int shiaijos) {
        final int groupsByShiaijo = groups.size() / shiaijos;
        int currentShiaijo = 0;
        int groupsInCurrentShiaijo = 0;
        for (final Group group : groups) {
            if (groupsInCurrentShiaijo >= this.getGroupsLimitForShiaijo(groups, shiaijos, groupsByShiaijo, currentShiaijo)) {
                currentShiaijo++;
                groupsInCurrentShiaijo = 0;
            }
            this.updateGroupShiaijoIfNeeded(group, currentShiaijo);
            groupsInCurrentShiaijo++;
        }
    }

    private int getGroupsLimitForShiaijo(List<Group> groups, int shiaijos, int groupsByShiaijo, int currentShiaijo) {
        if (currentShiaijo < groups.size() % shiaijos) {
            return groupsByShiaijo + 1;
        }
        return groupsByShiaijo;
    }

    private void updateGroupShiaijoIfNeeded(Group group, int shiaijo) {
        if (group.getShiaijo() == shiaijo) {
            return;
        }
        KendoTournamentLogger.info(this.getClass(), "Adjusting shiaijo for group '{}' to '{}'", group, shiaijo);
        group.setShiaijo(shiaijo);
        this.groupProvider.save(group);
    }

    @Override
    public void removeGroup(Tournament tournament, Integer groupLevel, Integer groupIndex) {
        if (groupLevel > 0) {
            throw new InvalidGroupException(this.getClass(), "Groups can only be deleted at level 0.");
        }

        this.removeLevelZeroGroupAndAdjustTree(tournament, groupLevel, groupIndex);
    }

    private void removeLevelZeroGroupAndAdjustTree(Tournament tournament, Integer groupLevel, Integer groupIndex) {
        this.groupProvider.deleteGroupByLevelAndIndex(tournament, groupLevel, groupIndex);
        final int numberOfWinners = this.getNumberOfWinners(tournament);
        final boolean oddTeamsResolvedAsap = Boolean.parseBoolean(this.tournamentExtraPropertyProvider
                .getByTournamentAndProperty(tournament, TournamentExtraPropertyKey.ODD_FIGHTS_RESOLVED_ASAP,
                        DEFAULT_ODD_TEAMS_RESOLUTION_ASAP)
                .getPropertyValue());

        // Check if inner levels must be decreased on size.
        final List<Group> tournamentGroups = this.groupProvider.getGroups(tournament);
        final Map<Integer, List<Group>> groupsByLevel = GroupUtils.orderByLevel(tournamentGroups);
        int previousLevelSize = Integer.MAX_VALUE - 1;
        for (final Integer level : new HashSet<>(groupsByLevel.keySet())) {
            previousLevelSize = this.adjustLevelAfterRemovingGroup(tournament, groupsByLevel, level,
                    previousLevelSize, numberOfWinners, oddTeamsResolvedAsap);
        }
        this.adjustGroupsShiaijos(tournament);
    }

    private int adjustLevelAfterRemovingGroup(Tournament tournament, Map<Integer, List<Group>> groupsByLevel,
            Integer level, int previousLevelSize, int numberOfWinners, boolean oddTeamsResolvedAsap) {
        this.removeLevelIfPreviousIsEmpty(tournament, groupsByLevel, level);
        if (oddTeamsResolvedAsap) {
            this.adjustOddResolvedLevelAfterGroupRemoval(tournament, groupsByLevel, level, previousLevelSize,
                    numberOfWinners);
        } else {
            this.adjustStandardLevelAfterGroupRemoval(tournament, groupsByLevel, level, previousLevelSize,
                    numberOfWinners);
        }
        return groupsByLevel.get(level).size();
    }

    private void removeLevelIfPreviousIsEmpty(Tournament tournament, Map<Integer, List<Group>> groupsByLevel,
            Integer level) {
        if (level > 0 && (!groupsByLevel.containsKey(level - 1) || groupsByLevel.get(level - 1).isEmpty())) {
            this.removeAllGroupsFromLevel(tournament, groupsByLevel, level);
        }
    }

    private void adjustOddResolvedLevelAfterGroupRemoval(Tournament tournament, Map<Integer, List<Group>> groupsByLevel,
            Integer level, int previousLevelSize, int numberOfWinners) {
        if (level > 1) {
            while (this.shouldShrinkOddResolvedLevel(groupsByLevel.get(level), previousLevelSize)) {
                this.removeLastGroupFromLevel(tournament, groupsByLevel, level);
            }
            return;
        }

        if (level == 1) {
            while (this.shouldShrinkFirstOddResolvedLevel(groupsByLevel, level, numberOfWinners)) {
                this.removeLastGroupFromLevel(tournament, groupsByLevel, level);
            }
            if (numberOfWinners == 1 && groupsByLevel.get(0).size() == 1) {
                this.removeAllGroupsFromLevel(tournament, groupsByLevel, 1);
            }
        }
    }

    private void adjustStandardLevelAfterGroupRemoval(Tournament tournament, Map<Integer, List<Group>> groupsByLevel,
            Integer level, int previousLevelSize, int numberOfWinners) {
        if (this.shouldShrinkStandardLevel(groupsByLevel.get(level).size(), level, previousLevelSize,
                numberOfWinners)) {
            this.removeLastGroupFromLevel(tournament, groupsByLevel, level);
        }
    }

    private boolean shouldShrinkOddResolvedLevel(List<Group> groups, int previousLevelSize) {
        return (previousLevelSize == 1 && !groups.isEmpty()) || groups.size() > ((previousLevelSize + 1) / 2);
    }

    private boolean shouldShrinkFirstOddResolvedLevel(Map<Integer, List<Group>> groupsByLevel, Integer level,
            int numberOfWinners) {
        return GroupUtils.getNextPowerOfTwo(((groupsByLevel.get(0).size() * numberOfWinners) + 1) / 2)
                < groupsByLevel.get(level).size();
    }

    private boolean shouldShrinkStandardLevel(int levelSize, Integer level, int previousLevelSize,
            int numberOfWinners) {
        return ((numberOfWinners == 1 || level > 1) && (previousLevelSize == 1
                || levelSize > ((previousLevelSize + 1) / 2)))
                || (numberOfWinners == 2 && levelSize > previousLevelSize);
    }

    private void removeAllGroupsFromLevel(Tournament tournament, Map<Integer, List<Group>> groupsByLevel, Integer level) {
        while (!groupsByLevel.get(level).isEmpty()) {
            this.removeLastGroupFromLevel(tournament, groupsByLevel, level);
        }
    }

    private void removeLastGroupFromLevel(Tournament tournament, Map<Integer, List<Group>> groupsByLevel, Integer level) {
        final List<Group> groups = groupsByLevel.get(level);
        this.groupProvider.deleteGroupByLevelAndIndex(tournament, level, groups.size() - 1);
        groups.removeLast();
    }

    @Override
    public List<Fight> createFights(Tournament tournament, TeamsOrder teamsOrder, Integer level, String createdBy) {
        final List<Group> tournamentGroups = this.groupProvider.getGroups(tournament);
        final List<Fight> createdFights = new ArrayList<>();
        tournamentGroups.forEach(group -> {
            if (Objects.equals(group.getLevel(), level)) {
                final List<Fight> fights;
                if (this.getMaxGroupFights(tournament)) {
                    final TournamentExtraProperty extraProperty = this.getLeagueFightsOrder(tournament);
                    fights = this.fightProvider.saveAll(this.completeGroupFightManager.createFights(tournament, group.getTeams(),
                            TeamsOrder.NONE, level, group.getShiaijo(),
                            LeagueFightsOrder.get(extraProperty.getPropertyValue()) == LeagueFightsOrder.FIFO,
                            createdBy));
                } else {
                    fights = this.fightProvider.saveAll(this.minimumGroupFightManager.createFights(tournament, group.getTeams(),
                            TeamsOrder.NONE, level, group.getShiaijo(), createdBy));
                }
                group.setFights(fights);
              this.groupProvider.save(group);
                createdFights.addAll(fights);
            }
        });
        return createdFights;
    }

    private Integer getNextEmptyLevel(List<Group> tournamentGroups) {
        if (tournamentGroups == null) {
            return null;
        }
        for (final Group group : tournamentGroups) {
            if (group.getTeams().isEmpty()) {
                return group.getLevel();
            }
        }
        return null;
    }

    private void populateLevel(Tournament tournament, int level) throws LevelNotFinishedException {
        final List<GroupLink> links = this.groupLinkProvider.getGroupLinks(tournament);
        final List<GroupLink> levelLinks = links.stream().filter(link -> link.getDestination().getLevel() == level)
                .toList();
        final Set<Group> groupsOfLevel = new HashSet<>();
        for (final GroupLink link : levelLinks) {
            final List<ScoreOfTeam> teamsRanking = this.rankingProvider.getTeamsScoreRanking(link.getSource());
          this.checkDrawScore(link.getSource(), teamsRanking, link.getWinner());
            if (link.getWinner() != null && teamsRanking.get(link.getWinner()) != null
                    && teamsRanking.get(link.getWinner()).getTeam() != null
                    && !link.getDestination().getTeams().contains(teamsRanking.get(link.getWinner()).getTeam())) {
                link.getDestination().getTeams().add(teamsRanking.get(link.getWinner()).getTeam());
            } else {
                KendoTournamentLogger.warning(this.getClass(),
                        "Missing data for level '{}' population with winner '{}' using ranking:\n\t{}", level,
                        link.getWinner(), link.getWinner() != null ? teamsRanking.get(link.getWinner()) : null);
            }
            groupsOfLevel.add(link.getDestination());
        }
      this.groupProvider.saveAll(groupsOfLevel);
    }

    private void checkDrawScore(Group group, List<ScoreOfTeam> scoresOfTeamsDTO, int numberOfWinners) {
        for (int i = 0; i <= numberOfWinners; i++) {
            final int winner = i;
            final List<ScoreOfTeam> sameLevelScore = scoresOfTeamsDTO.stream()
                    .filter(scoreOfTeamDTO -> scoreOfTeamDTO.getSortingIndex() == winner).toList();
            if (sameLevelScore.size() > 1) {
              this.createCriticalUntieIfRequired(group, sameLevelScore);
                KendoTournamentLogger.debug(this.getClass(), "Teams with same score are '{}'.",
                        sameLevelScore.stream().map(ScoreOfTeam::getTeam).toList());
                throw new LevelNotFinishedException(this.getClass(),
                        "There is a draw value on winner '" + winner + "' on group '" + group + "'");
            }
        }
    }

    private void createCriticalUntieIfRequired(Group group, List<ScoreOfTeam> tiedScores) {
        if (tiedScores.size() != 2) {
            return;
        }

        final Team firstTeam = tiedScores.getFirst().getTeam();
        final Team secondTeam = tiedScores.get(1).getTeam();
        if (firstTeam == null || secondTeam == null) {
            return;
        }

        final Participant firstCompetitor = this.getRepresentativeCompetitor(firstTeam);
        final Participant secondCompetitor = this.getRepresentativeCompetitor(secondTeam);
        if (firstCompetitor == null || secondCompetitor == null) {
            return;
        }

        // Reload the group from DB to get a clean managed entity. Using the detached
        // entity from the GroupLink cache can cause the cascade-persist to be silently
        // dropped when the PersistentBag is merged. The fresh load also ensures that
        // hasPendingCriticalUntie reflects any untie already created on a prior
        // attempt.
        final Group freshGroup = this.groupProvider.getGroup(group.getId());
        if (freshGroup == null) {
            return;
        }

        if (this.hasPendingCriticalUntie(freshGroup, firstTeam, secondTeam)) {
            return;
        }

        final Duel untie = new Duel(firstCompetitor, secondCompetitor, freshGroup.getTournament(), "system");
        untie.setType(DuelType.UNDRAW);
        freshGroup.getUnties().add(untie);
      this.groupProvider.save(freshGroup);
    }

    private boolean hasPendingCriticalUntie(Group group, Team firstTeam, Team secondTeam) {
        return group.getUnties().stream().filter(duel -> duel.getType() == DuelType.UNDRAW)
                .anyMatch(duel -> !duel.isFinished() && this.isDuelBetweenTeams(duel, firstTeam, secondTeam));
    }

    private boolean isDuelBetweenTeams(Duel duel, Team firstTeam, Team secondTeam) {
        return this.isCompetitorFromTeam(duel.getCompetitor1(), firstTeam)
                && this.isCompetitorFromTeam(duel.getCompetitor2(), secondTeam)
                || this.isCompetitorFromTeam(duel.getCompetitor1(), secondTeam)
                        && this.isCompetitorFromTeam(duel.getCompetitor2(), firstTeam);
    }

    private boolean isCompetitorFromTeam(Participant competitor, Team team) {
        return competitor != null && team.getMembers() != null && team.getMembers().contains(competitor);
    }

    private Participant getRepresentativeCompetitor(Team team) {
        if (team.getMembers() == null || team.getMembers().isEmpty()) {
            return null;
        }
        return team.getMembers().getFirst();
    }

    @Override
    public List<Fight> generateNextFights(Tournament tournament, String createdBy) {
        // Get the next level to continue if exists.
        final List<Group> tournamentGroups = this.groupProvider.getGroups(tournament);
        if (tournamentGroups == null) {
            return new ArrayList<>();
        }

        final Integer nextLevel = this.getNextEmptyLevel(tournamentGroups);
        if (nextLevel == null) {
            KendoTournamentLogger.debug(this.getClass(), "No next level to populate!");
            return new ArrayList<>();
        }

        // Populate the next level with winners.
      this.populateLevel(tournament, nextLevel);

        // Generate next Level fights.
        return this.createFights(tournament, TeamsOrder.NONE, nextLevel, createdBy);
    }
}
