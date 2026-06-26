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

import com.softwaremagico.kt.core.exceptions.CustomTournamentFightsException;
import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.RankingProvider;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.core.providers.TournamentExtraPropertyProvider;
import com.softwaremagico.kt.logger.KendoTournamentLogger;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SwissTournamentHandler extends LeagueHandler {

    public static final int DEFAULT_SWISS_MIN_ROUNDS = 1;
    public static final int SWISS_WIN_POINTS = 3;
    public static final int SWISS_DRAW_POINTS = 1;
    public static final boolean DEFAULT_AVOID_REPEATED_PAIRINGS = true;
    private final GroupProvider groupProvider;
    private final TournamentExtraPropertyProvider tournamentExtraPropertyProvider;

    public SwissTournamentHandler(GroupProvider groupProvider, TeamProvider teamProvider,
                                  RankingProvider rankingProvider, TournamentExtraPropertyProvider tournamentExtraPropertyProvider) {
        super(groupProvider, teamProvider, rankingProvider, tournamentExtraPropertyProvider);
        this.groupProvider = groupProvider;
        this.tournamentExtraPropertyProvider = tournamentExtraPropertyProvider;
    }

    @Override
    public List<Group> getGroups(Tournament tournament) {
        final List<Group> groups = this.groupProvider.getGroups(tournament);
        if (groups.isEmpty()) {
            return List.of(this.getFirstGroup(tournament));
        }
        return groups;
    }

    @Override
    public List<Group> getGroups(Tournament tournament, Integer level) {
        if (level == null) {
            return this.getGroups(tournament);
        }
        final List<Group> groups = this.groupProvider.getGroups(tournament, level);
        if (groups.isEmpty() && level == 0) {
            return List.of(this.getFirstGroup(tournament));
        }
        return groups;
    }

    @Override
    public Group addGroup(Tournament tournament, Group group) {
        return this.groupProvider.addGroup(tournament, group);
    }

    @Override
    public Group getGroup(Tournament tournament, Fight fight) {
        return this.groupProvider.getGroup(fight);
    }

    @Override
    public void removeGroup(Tournament tournament, Integer level, Integer groupIndex) {
        this.groupProvider.deleteGroupByLevelAndIndex(tournament, level, groupIndex);
    }

    public int getConfiguredRounds(Tournament tournament) {
        final int participantCount = this.getFirstGroup(tournament).getTeams().size();
        final int defaultRounds = this.getDefaultRounds(participantCount);
        final TournamentExtraProperty roundsProperty = this.tournamentExtraPropertyProvider.getByTournamentAndProperty(tournament,
                TournamentExtraPropertyKey.SWISS_ROUNDS, defaultRounds);

        try {
            final int configuredRounds = Integer.parseInt(roundsProperty.getPropertyValue());
            if (configuredRounds < DEFAULT_SWISS_MIN_ROUNDS) {
                return defaultRounds;
            }
            return configuredRounds;
        } catch (final Exception e) {
            KendoTournamentLogger.errorMessage(this.getClass(), e);
            return defaultRounds;
        }
    }

    public int getDefaultRounds(int participants) {
        final int checkedParticipants = Math.max(2, participants);
        int rounds = 0;
        int coveredParticipants = 1;
        while (coveredParticipants < checkedParticipants) {
            coveredParticipants *= 2;
            rounds++;
        }
        return Math.max(DEFAULT_SWISS_MIN_ROUNDS, rounds);
    }

    @Override
    public List<Fight> createFights(Tournament tournament, TeamsOrder teamsOrder, Integer level, String createdBy) {
        if (level == null || level < 0) {
            throw new CustomTournamentFightsException(this.getClass(), "Swiss round level must be greater than or equal to 0.");
        }

        final Group initialGroup = this.getFirstGroup(tournament);
        if (initialGroup.getTeams() == null || initialGroup.getTeams().size() < 2) {
            return new ArrayList<>();
        }

        if (level >= this.getConfiguredRounds(tournament)) {
            return new ArrayList<>();
        }

        final List<Group> tournamentGroups = this.getGroups(tournament);
        final List<Fight> previousFights = this.getAllFights(tournamentGroups);

        final boolean roundAlreadyGenerated = previousFights.stream().anyMatch(fight -> Objects.equals(fight.getLevel(), level));
        if (roundAlreadyGenerated) {
            return new ArrayList<>();
        }

        final Map<Team, Integer> pointsByTeam = this.getSwissPointsByTeam(initialGroup.getTeams(), previousFights);
        final List<Team> orderedTeams = this.getTeamsOrderedBySwissScore(initialGroup.getTeams(), pointsByTeam);

        Team byeTeam = null;
        if (orderedTeams.size() % 2 != 0) {
            // Assign bye to the lowest-ranked team that has not received a bye yet (if possible).
            final int byeIndex = this.selectByeTeamIndex(orderedTeams, initialGroup.getTeams(), previousFights);
            byeTeam = orderedTeams.remove(byeIndex);
        }

        final List<Fight> generatedFights = this.createSwissPairings(tournament, orderedTeams, level, createdBy, previousFights,
                this.avoidRepeatedPairings(tournament));

        final List<List<Team>> teamsByScoreGroup = this.groupTeamsByScore(initialGroup.getTeams(), pointsByTeam);
        this.persistRoundGroups(tournament, level, teamsByScoreGroup, generatedFights, pointsByTeam, byeTeam, createdBy);

        return generatedFights;
    }

    private List<Fight> getAllFights(List<Group> groups) {
        return groups.stream()
                .flatMap(group -> group.getFights() == null ? java.util.stream.Stream.<Fight>empty() : group.getFights().stream())
                .toList();
    }

    private void persistRoundGroups(Tournament tournament, Integer level, List<List<Team>> teamsByScoreGroup,
                                    List<Fight> generatedFights, Map<Team, Integer> pointsByTeam,
                                    Team byeTeam, String createdBy) {
        final Map<Integer, Integer> scoreToGroupIndex = new HashMap<>();
        final Map<Integer, Group> groupsByIndex = new HashMap<>();
        for (int groupIndex = 0; groupIndex < teamsByScoreGroup.size(); groupIndex++) {
            final List<Team> teams = teamsByScoreGroup.get(groupIndex);
            final Group roundGroup = this.getOrCreateRoundGroup(tournament, level, groupIndex, createdBy);
            roundGroup.setTeams(new ArrayList<>(teams));
            roundGroup.setFights(new ArrayList<>());
            groupsByIndex.put(groupIndex, roundGroup);
            if (!teams.isEmpty()) {
                scoreToGroupIndex.put(pointsByTeam.getOrDefault(teams.getFirst(), 0), groupIndex);
            }
        }

        for (final Fight fight : generatedFights) {
            final int fightScore = Math.max(pointsByTeam.getOrDefault(fight.getTeam1(), 0),
                    pointsByTeam.getOrDefault(fight.getTeam2(), 0));
            final int groupIndex = scoreToGroupIndex.getOrDefault(fightScore, 0);
            final Group roundGroup = groupsByIndex.get(groupIndex);
            roundGroup.getFights().add(fight);
        }

        if (byeTeam != null) {
            final int byeScore = pointsByTeam.getOrDefault(byeTeam, 0);
            final int groupIndex = scoreToGroupIndex.getOrDefault(byeScore, 0);
            final Group roundGroup = groupsByIndex.get(groupIndex);
            if (!roundGroup.getTeams().contains(byeTeam)) {
                roundGroup.getTeams().add(byeTeam);
            }
        }

        groupsByIndex.values().forEach(group -> this.groupProvider.addGroup(tournament, group));

        this.removeObsoleteGroupsInRound(tournament, level, teamsByScoreGroup.size());
    }

    private Group getOrCreateRoundGroup(Tournament tournament, Integer level, Integer index, String createdBy) {
        Group group = this.groupProvider.getGroupByLevelAndIndex(tournament, level, index);
        if (group == null) {
            group = new Group();
            group.setTournament(tournament);
            group.setLevel(level);
            group.setIndex(index);
            group.setTeams(new ArrayList<>());
            group.setFights(new ArrayList<>());
            group.setCreatedBy(createdBy);
        } else {
            if (group.getTeams() == null) {
                group.setTeams(new ArrayList<>());
            }
            if (group.getFights() == null) {
                group.setFights(new ArrayList<>());
            }
        }
        return group;
    }

    private void removeObsoleteGroupsInRound(Tournament tournament, Integer level, int expectedGroups) {
        final List<Group> roundGroups = this.groupProvider.getGroups(tournament, level);
        roundGroups.stream()
                .filter(group -> group.getIndex() >= expectedGroups)
                .forEach(group -> this.groupProvider.deleteGroupByLevelAndIndex(tournament, level, group.getIndex()));
    }

    private List<List<Team>> groupTeamsByScore(List<Team> teams, Map<Team, Integer> pointsByTeam) {
        final Map<Integer, List<Team>> teamsByScore = new LinkedHashMap<>();
        teams.stream()
                .sorted(Comparator.comparing((Team team) -> pointsByTeam.getOrDefault(team, 0)).reversed()
                        .thenComparing(Team::getName))
                .forEach(team -> teamsByScore.computeIfAbsent(pointsByTeam.getOrDefault(team, 0),
                        ignoredScore -> new ArrayList<>()).add(team));
        return new ArrayList<>(teamsByScore.values());
    }

    public boolean avoidRepeatedPairings(Tournament tournament) {
        final TournamentExtraProperty repeatedPairingsProperty = this.tournamentExtraPropertyProvider.getByTournamentAndProperty(tournament,
                TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS, DEFAULT_AVOID_REPEATED_PAIRINGS);
        try {
            return Boolean.parseBoolean(repeatedPairingsProperty.getPropertyValue());
        } catch (final Exception e) {
            KendoTournamentLogger.errorMessage(this.getClass(), e);
            return DEFAULT_AVOID_REPEATED_PAIRINGS;
        }
    }

    private List<Fight> createSwissPairings(Tournament tournament, List<Team> teams, Integer level, String createdBy,
                                            List<Fight> previousFights, boolean avoidRepeated) {
        final List<Fight> fights = this.tryCreateSwissPairings(tournament, teams, level, createdBy, previousFights, avoidRepeated);
        if (fights != null) {
            return fights;
        }
        if (!avoidRepeated) {
            return new ArrayList<>();
        }
        return this.tryCreateSwissPairings(tournament, teams, level, createdBy, previousFights, false);
    }

    private List<Fight> tryCreateSwissPairings(Tournament tournament, List<Team> teams, Integer level, String createdBy,
                                               List<Fight> previousFights, boolean avoidRepeated) {
        if (teams.isEmpty()) {
            return new ArrayList<>();
        }

        final Team firstTeam = teams.getFirst();
        for (int i = 1; i < teams.size(); i++) {
            final Team candidate = teams.get(i);
            if (avoidRepeated && this.havePlayed(firstTeam, candidate, previousFights)) {
                continue;
            }

            final List<Team> remainingTeams = new ArrayList<>(teams);
            remainingTeams.remove(i);
            remainingTeams.removeFirst();

            final List<Fight> remainingPairings = this.tryCreateSwissPairings(tournament, remainingTeams, level, createdBy, previousFights, avoidRepeated);
            if (remainingPairings != null) {
                final List<Fight> fights = new ArrayList<>();
                fights.add(new Fight(tournament, firstTeam, candidate, 0, level, createdBy));
                fights.addAll(remainingPairings);
                return fights;
            }
        }

        return null;
    }

    private boolean havePlayed(Team firstTeam, Team secondTeam, List<Fight> previousFights) {
        return previousFights.stream().anyMatch(fight ->
                (Objects.equals(fight.getTeam1(), firstTeam) && Objects.equals(fight.getTeam2(), secondTeam))
                        || (Objects.equals(fight.getTeam1(), secondTeam) && Objects.equals(fight.getTeam2(), firstTeam)));
    }

    private List<Team> getTeamsOrderedBySwissScore(List<Team> teams, Map<Team, Integer> pointsByTeam) {
        final List<Team> orderedTeams = new ArrayList<>(teams);
        orderedTeams.sort(Comparator
                .comparing((Team team) -> pointsByTeam.getOrDefault(team, 0)).reversed()
                .thenComparing(Team::getName));
        return orderedTeams;
    }

    private Map<Team, Integer> getSwissPointsByTeam(List<Team> teams, List<Fight> fights) {
        final Map<Team, Integer> pointsByTeam = new HashMap<>();
        teams.forEach(team -> pointsByTeam.put(team, 0));
        final Map<Team, Integer> byesByTeam = this.getByeCountByTeam(teams, fights);

        for (final Fight fight : fights) {
            final Team winner = fight.getWinner();
            if (winner != null) {
                pointsByTeam.computeIfPresent(winner, (ignoredTeam, points) -> points + SWISS_WIN_POINTS);
            } else if (fight.isOver() && fight.isDrawFight()) {
                pointsByTeam.computeIfPresent(fight.getTeam1(), (ignoredTeam, points) -> points + SWISS_DRAW_POINTS);
                pointsByTeam.computeIfPresent(fight.getTeam2(), (ignoredTeam, points) -> points + SWISS_DRAW_POINTS);
            }
        }

        byesByTeam.forEach((team, byeCount) -> pointsByTeam.computeIfPresent(team,
                (ignoredTeam, points) -> points + (byeCount * SWISS_WIN_POINTS)));
        return pointsByTeam;
    }

    private int selectByeTeamIndex(List<Team> orderedTeams, List<Team> teams, List<Fight> fights) {
        final Map<Team, Integer> byesByTeam = this.getByeCountByTeam(teams, fights);
        for (int i = orderedTeams.size() - 1; i >= 0; i--) {
            final Team candidate = orderedTeams.get(i);
            if (byesByTeam.getOrDefault(candidate, 0) == 0) {
                return i;
            }
        }
        return orderedTeams.size() - 1;
    }

    private Map<Team, Integer> getByeCountByTeam(List<Team> teams, List<Fight> fights) {
        final Map<Team, Integer> byesByTeam = new HashMap<>();
        teams.forEach(team -> byesByTeam.put(team, 0));

        final Map<Integer, Set<Team>> teamsByRound = fights.stream()
                .collect(Collectors.groupingBy(Fight::getLevel,
                        Collectors.flatMapping(fight -> java.util.stream.Stream.of(fight.getTeam1(), fight.getTeam2()),
                                Collectors.toSet())));

        for (final Set<Team> teamsInRound : teamsByRound.values()) {
            final long teamsPresent = teamsInRound.stream().filter(teams::contains).count();
            if (teamsPresent != teams.size() - 1L) {
                continue;
            }
            for (final Team team : teams) {
                if (!teamsInRound.contains(team)) {
                    byesByTeam.computeIfPresent(team, (ignoredTeam, value) -> value + 1);
                }
            }
        }
        return byesByTeam;
    }
}
