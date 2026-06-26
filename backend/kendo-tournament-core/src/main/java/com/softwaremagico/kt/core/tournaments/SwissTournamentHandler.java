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
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class SwissTournamentHandler extends LeagueHandler {

    public static final int DEFAULT_SWISS_MIN_ROUNDS = 1;
    public static final int SWISS_WIN_POINTS = 3;
    public static final int SWISS_DRAW_POINTS = 1;
    public static final boolean DEFAULT_AVOID_REPEATED_PAIRINGS = true;
    private final TournamentExtraPropertyProvider tournamentExtraPropertyProvider;

    public SwissTournamentHandler(GroupProvider groupProvider, TeamProvider teamProvider,
                                  RankingProvider rankingProvider, TournamentExtraPropertyProvider tournamentExtraPropertyProvider) {
        super(groupProvider, teamProvider, rankingProvider, tournamentExtraPropertyProvider);
        this.tournamentExtraPropertyProvider = tournamentExtraPropertyProvider;
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

        final Group group = this.getFirstGroup(tournament);
        if (group.getTeams() == null || group.getTeams().size() < 2) {
            return new ArrayList<>();
        }
        if (group.getFights() == null) {
            group.setFights(new ArrayList<>());
        }

        if (level >= this.getConfiguredRounds(tournament)) {
            return new ArrayList<>();
        }

        final boolean roundAlreadyGenerated = group.getFights().stream().anyMatch(fight -> Objects.equals(fight.getLevel(), level));
        if (roundAlreadyGenerated) {
            return new ArrayList<>();
        }

        final List<Team> orderedTeams = this.getTeamsOrderedBySwissScore(group);
        if (orderedTeams.size() % 2 != 0) {
            // Lowest-ranked team gets a bye in this simple initial implementation.
            orderedTeams.removeLast();
        }

        final List<Fight> generatedFights = this.createSwissPairings(tournament, orderedTeams, level, createdBy, group.getFights(),
                this.avoidRepeatedPairings(tournament));

        group.getFights().addAll(generatedFights);
        this.addGroup(tournament, group);
        return generatedFights;
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
                (this.sameTeam(fight.getTeam1(), firstTeam) && this.sameTeam(fight.getTeam2(), secondTeam))
                        || (this.sameTeam(fight.getTeam1(), secondTeam) && this.sameTeam(fight.getTeam2(), firstTeam)));
    }

    private boolean sameTeam(Team firstTeam, Team secondTeam) {
        if (firstTeam == secondTeam) {
            return true;
        }
        if (firstTeam == null || secondTeam == null) {
            return false;
        }
        if (firstTeam.getId() != null && secondTeam.getId() != null) {
            return Objects.equals(firstTeam.getId(), secondTeam.getId());
        }
        return Objects.equals(firstTeam.getName(), secondTeam.getName())
                && (Objects.equals(firstTeam.getTournament(), secondTeam.getTournament())
                || firstTeam.getTournament() == secondTeam.getTournament());
    }

    private List<Team> getTeamsOrderedBySwissScore(Group group) {
        final List<Team> orderedTeams = new ArrayList<>(group.getTeams());
        // Team.equals/hashCode are id-based; transient teams share null ids and collide in HashMap.
        final Map<Team, Integer> pointsByTeam = new IdentityHashMap<>();
        orderedTeams.forEach(team -> pointsByTeam.put(team, 0));

        for (final Fight fight : group.getFights()) {
            final Team winner = fight.getWinner();
            if (winner != null) {
                pointsByTeam.computeIfPresent(winner, (ignoredTeam, points) -> points + SWISS_WIN_POINTS);
            } else if (fight.isOver() && fight.isDrawFight()) {
                pointsByTeam.computeIfPresent(fight.getTeam1(), (ignoredTeam, points) -> points + SWISS_DRAW_POINTS);
                pointsByTeam.computeIfPresent(fight.getTeam2(), (ignoredTeam, points) -> points + SWISS_DRAW_POINTS);
            }
        }

        orderedTeams.sort(Comparator
                .comparing((Team team) -> pointsByTeam.getOrDefault(team, 0)).reversed()
                .thenComparing(Team::getName));
        return orderedTeams;
    }
}

