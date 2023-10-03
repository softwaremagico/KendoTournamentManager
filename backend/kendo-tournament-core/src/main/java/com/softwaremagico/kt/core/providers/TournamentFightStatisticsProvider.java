package com.softwaremagico.kt.core.providers;

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

import com.softwaremagico.kt.core.statistics.TournamentFightStatistics;
import com.softwaremagico.kt.core.statistics.TournamentFightStatisticsRepository;
import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Role;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TournamentFightStatisticsProvider extends CrudProvider<TournamentFightStatistics, Integer, TournamentFightStatisticsRepository> {

    private static final int TIME_BETWEEN_FIGHTS = 20;
    private static final int TIME_BETWEEN_DUELS = 10;

    private final DuelProvider duelProvider;

    private final FightProvider fightProvider;
    private final TeamProvider teamProvider;
    private final RoleProvider roleProvider;
    private final TournamentExtraPropertyProvider tournamentExtraPropertyProvider;

    public TournamentFightStatisticsProvider(TournamentFightStatisticsRepository tournamentFightStatisticsRepository, DuelProvider duelProvider,
                                             FightProvider fightProvider, TeamProvider teamProvider, RoleProvider roleProvider,
                                             TournamentExtraPropertyProvider tournamentExtraPropertyProvider) {
        super(tournamentFightStatisticsRepository);
        this.duelProvider = duelProvider;
        this.fightProvider = fightProvider;
        this.teamProvider = teamProvider;
        this.roleProvider = roleProvider;
        this.tournamentExtraPropertyProvider = tournamentExtraPropertyProvider;
    }

    /**
     * Calculate the statistics by teams if they are already defined, or using the members if not.
     *
     * @param tournament the tournament.
     * @return some estimations.
     */
    public TournamentFightStatistics estimate(Tournament tournament) {
        final List<Team> teams = teamProvider.getAll(tournament);
        if (!teams.isEmpty()) {
            return estimate(tournament, teams);
        }
        final List<Role> roles = roleProvider.getAll(tournament);
        return estimateByRoles(tournament, roles);
    }

    public TournamentFightStatistics estimateByTeams(Tournament tournament) {
        final List<Team> teams = teamProvider.getAll(tournament);
        return estimate(tournament, teams);
    }

    public TournamentFightStatistics estimateByMembers(Tournament tournament) {
        final List<Role> roles = roleProvider.getAll(tournament);
        return estimateByRoles(tournament, roles.stream().filter(role ->
                Objects.equals(role.getRoleType(), RoleType.COMPETITOR)).toList());
    }

    public TournamentFightStatistics estimate(Tournament tournament, Collection<Team> teams) {
        return estimate(tournament, tournament.getTeamSize(), teams);
    }

    public TournamentFightStatistics estimateByRoles(Tournament tournament, Collection<Role> roles) {
        return estimate(tournament, emulateTeams(tournament, roles.stream().map(Role::getParticipant).toList()));
    }

    public TournamentFightStatistics estimate(Tournament tournament, int teamSize, Collection<Team> teams) {
        if (tournament == null || teams == null || teams.size() < 2) {
            return null;
        }
        switch (tournament.getType()) {
            case LEAGUE:
                return estimateLeagueStatistics(teamSize, teams);
            case LOOP:
                return estimateLoopStatistics(tournament, teamSize, teams);
            case CUSTOMIZED:
            case KING_OF_THE_MOUNTAIN:
            default:
                return null;
        }
    }

    public TournamentFightStatistics estimateLeagueStatistics(int teamSize, Collection<Team> teams) {
        final TournamentFightStatistics tournamentFightStatistics = new TournamentFightStatistics();
        tournamentFightStatistics.setFightsNumber((((long) teams.size() * (teams.size() - 1)) / 2));
        tournamentFightStatistics.setFightsByTeam(((long) teams.size() - 1));
        tournamentFightStatistics.setDuelsNumber(getDuels(tournamentFightStatistics.getFightsByTeam(), teamSize, teams));
        final Long durationAverage = duelProvider.getDurationAverage();
        if (durationAverage != null && durationAverage > 0 && tournamentFightStatistics.getDuelsNumber() != null) {
            tournamentFightStatistics.setAverageTime(durationAverage);
            tournamentFightStatistics.setEstimatedTime(tournamentFightStatistics.getDuelsNumber() * (durationAverage + TIME_BETWEEN_DUELS)
                    + (tournamentFightStatistics.getFightsNumber() != null ? (long) TIME_BETWEEN_FIGHTS * tournamentFightStatistics.getFightsNumber() : 0));
        }
        return tournamentFightStatistics;
    }

    public TournamentFightStatistics estimateLoopStatistics(Tournament tournament, int teamSize, Collection<Team> teams) {
        final TournamentFightStatistics tournamentFightStatistics = new TournamentFightStatistics();
        final TournamentExtraProperty property = tournamentExtraPropertyProvider.getByTournamentAndProperty(tournament,
                TournamentExtraPropertyKey.MAXIMIZE_FIGHTS);
        final boolean maximizeFights = property != null && Boolean.parseBoolean(property.getPropertyValue());
        if (maximizeFights) {
            tournamentFightStatistics.setFightsNumber(((long) teams.size() * (teams.size() - 1)));
            tournamentFightStatistics.setFightsByTeam(((long) teams.size() - 1) * 2);
        } else {
            tournamentFightStatistics.setFightsNumber(((long) teams.size() * teams.size() - 1) / 2);
            tournamentFightStatistics.setFightsByTeam(((long) teams.size() - 1));
        }
        tournamentFightStatistics.setDuelsNumber(getDuels(tournamentFightStatistics.getFightsByTeam(), teamSize, teams));
        if (duelProvider.getDurationAverage() != null) {
            final Long average = duelProvider.getDurationAverage();
            if (tournamentFightStatistics.getDuelsNumber() != null) {
                tournamentFightStatistics.setEstimatedTime(tournamentFightStatistics.getDuelsNumber() * (average > 0 ? average : 0));
            } else {
                tournamentFightStatistics.setEstimatedTime(0L);
            }
        }
        return tournamentFightStatistics;
    }

    private long getDuels(long fightByTeam, int teamSize, Collection<Team> teams) {
        final AtomicLong counter = new AtomicLong();
        final AtomicLong missingMembers = new AtomicLong();

        teams.forEach(team -> {
            counter.addAndGet((team.getMembers().size() * fightByTeam) - missingMembers.get());
            missingMembers.addAndGet((long) teamSize - team.getMembers().size());
        });

        //Duels are counted twice, once for each team
        return counter.get() / 2;
    }

    /**
     * Emulate some teams if are not defined yet only to calculate the number of fights and duels.
     *
     * @param tournament   the tournament where emulate.
     * @param participants the list of members that will participate on the tournament.
     * @return a list of transient teams.
     */
    private List<Team> emulateTeams(Tournament tournament, Collection<Participant> participants) {
        int teamIndex = 0;
        final List<Team> teams = new ArrayList<>();
        Team team = null;
        int teamMember = 0;


        for (final Participant competitor : participants) {
            // Create a new team.
            if (team == null) {
                teamIndex++;
                team = new Team("Team" + String.format("%02d", teamIndex), tournament);
                teamMember = 0;
            }

            // Add member.
            team.addMember(competitor);

            if (teamMember == 0) {
                teams.add(team);
            }

            teamMember++;

            // Team filled up, create a new team.
            if (teamMember >= tournament.getTeamSize()) {
                team = null;
            }
        }
        return teams;
    }

    public TournamentFightStatistics get(Tournament tournament) {
        final TournamentFightStatistics tournamentFightStatistics = new TournamentFightStatistics();
        tournamentFightStatistics.setFightsNumber(fightProvider.count(tournament));
        final long teams = teamProvider.count(tournament);
        if (teams > 0) {
            tournamentFightStatistics.setFightsByTeam(fightProvider.count(tournament) / teams);
        } else {
            tournamentFightStatistics.setFightsByTeam(0L);
        }
        tournamentFightStatistics.setDuelsNumber(duelProvider.count(tournament));
        tournamentFightStatistics.setAverageTime(duelProvider.getDurationAverage(tournament));
        tournamentFightStatistics.setMenNumber(duelProvider.countScore(tournament, Score.MEN));
        tournamentFightStatistics.setDoNumber(duelProvider.countScore(tournament, Score.DO));
        tournamentFightStatistics.setKoteNumber(duelProvider.countScore(tournament, Score.KOTE));
        tournamentFightStatistics.setHansokuNumber(duelProvider.countScore(tournament, Score.HANSOKU));
        tournamentFightStatistics.setTsukiNumber(duelProvider.countScore(tournament, Score.TSUKI));
        tournamentFightStatistics.setIpponNumber(duelProvider.countScore(tournament, Score.IPPON));
        final Duel firstDuel = duelProvider.getFirstDuel(tournament);
        final Duel lastDuel = duelProvider.getLastDuel(tournament);
        if (firstDuel != null) {
            if (firstDuel.getStartedAt() != null) {
                tournamentFightStatistics.setFightsStartedAt(firstDuel.getStartedAt());
            } else {
                if (firstDuel.getFinishedAt() != null) {
                    tournamentFightStatistics.setFightsStartedAt(firstDuel.getFinishedAt().minusMinutes(2));
                }
            }
        }
        if (lastDuel != null) {
            tournamentFightStatistics.setFightsFinishedAt(lastDuel.getFinishedAt());
        }
        tournamentFightStatistics.setFightsFinished(fightProvider.countByTournamentAndFinished(tournament));
        tournamentFightStatistics.setFaults(duelProvider.countFaults(tournament));
        return tournamentFightStatistics;
    }

}
