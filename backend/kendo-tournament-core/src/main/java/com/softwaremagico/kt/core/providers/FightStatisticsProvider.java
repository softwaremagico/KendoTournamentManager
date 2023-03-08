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

import com.softwaremagico.kt.core.converters.RoleConverter;
import com.softwaremagico.kt.core.converters.TeamConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.statistics.FightStatistics;
import com.softwaremagico.kt.core.statistics.FightStatisticsRepository;
import com.softwaremagico.kt.persistence.entities.*;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class FightStatisticsProvider extends CrudProvider<FightStatistics, Integer, FightStatisticsRepository> {

    private static final int TIME_BETWEEN_FIGHTS = 20;
    private static final int TIME_BETWEEN_DUELS = 10;

    private final DuelProvider duelProvider;
    private final TeamProvider teamProvider;
    private final TeamConverter teamConverter;
    private final TournamentConverter tournamentConverter;
    private final RoleProvider roleProvider;
    private final RoleConverter roleConverter;
    private final TournamentExtraPropertyProvider tournamentExtraPropertyProvider;

    public FightStatisticsProvider(FightStatisticsRepository fightStatisticsRepository, DuelProvider duelProvider, TeamProvider teamProvider,
                                   TeamConverter teamConverter, TournamentConverter tournamentConverter, RoleProvider roleProvider,
                                   RoleConverter roleConverter, TournamentExtraPropertyProvider tournamentExtraPropertyProvider) {
        super(fightStatisticsRepository);
        this.duelProvider = duelProvider;
        this.teamProvider = teamProvider;
        this.teamConverter = teamConverter;
        this.tournamentConverter = tournamentConverter;
        this.roleProvider = roleProvider;
        this.roleConverter = roleConverter;
        this.tournamentExtraPropertyProvider = tournamentExtraPropertyProvider;
    }

    /**
     * Calculate the statistics by teams if they are already defined, or using the members if not.
     *
     * @param tournament the tournament.
     * @return some estimations.
     */
    public FightStatistics estimate(Tournament tournament) {
        final List<Team> teams = teamProvider.getAll(tournament);
        if (!teams.isEmpty()) {
            return estimate(tournament, teams);
        }
        final List<Role> roles = roleProvider.getAll(tournament);
        return estimateByRoles(tournament, roles);
    }

    public FightStatistics estimateByTeams(Tournament tournament) {
        final List<Team> teams = teamProvider.getAll(tournament);
        return estimate(tournament, teams);
    }

    public FightStatistics estimateByMembers(Tournament tournament) {
        final List<Role> roles = roleProvider.getAll(tournament);
        return estimateByRoles(tournament, roles.stream().filter(role ->
                Objects.equals(role.getRoleType(), RoleType.COMPETITOR)).collect(Collectors.toList()));
    }

    public FightStatistics estimate(Tournament tournament, Collection<Team> teams) {
        return estimate(tournament, tournament.getTeamSize(), teams);
    }

    public FightStatistics estimateByRoles(Tournament tournament, Collection<Role> roles) {
        return estimate(tournament, emulateTeams(tournament, roles.stream().map(Role::getParticipant).collect(Collectors.toList())));
    }

    public FightStatistics estimate(Tournament tournament, int teamSize, Collection<Team> teams) {
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

    public FightStatistics estimateLeagueStatistics(int teamSize, Collection<Team> teams) {
        final FightStatistics fightStatistics = new FightStatistics();
        fightStatistics.setFightsNumber((teams.size() * (teams.size() - 1)) / 2);
        fightStatistics.setFightsByTeam((teams.size() - 1));
        fightStatistics.setDuelsNumber(getDuels(fightStatistics.getFightsByTeam(), teamSize, teams));
        if (duelProvider.getDurationAverage() != null && fightStatistics.getDuelsNumber() != null) {
            fightStatistics.setTime(fightStatistics.getDuelsNumber() * (duelProvider.getDurationAverage() + TIME_BETWEEN_DUELS) +
                    (fightStatistics.getFightsNumber() != null ? (long) TIME_BETWEEN_FIGHTS * fightStatistics.getFightsNumber() : 0));
        }
        return fightStatistics;
    }

    public FightStatistics estimateLoopStatistics(Tournament tournament, int teamSize, Collection<Team> teams) {
        final FightStatistics fightStatistics = new FightStatistics();
        final TournamentExtraProperty property = tournamentExtraPropertyProvider.getByTournamentAndProperty(tournament,
                TournamentExtraPropertyKey.MAXIMIZE_FIGHTS);
        final boolean maximizeFights = property != null && Boolean.parseBoolean(property.getValue());
        if (maximizeFights) {
            fightStatistics.setFightsNumber((teams.size() * (teams.size() - 1)));
            fightStatistics.setFightsByTeam((teams.size() - 1) * 2);
        } else {
            fightStatistics.setFightsNumber((teams.size() * teams.size() - 1) / 2);
            fightStatistics.setFightsByTeam((teams.size() - 1));
        }
        fightStatistics.setDuelsNumber(getDuels(fightStatistics.getFightsByTeam(), teamSize, teams));
        if (duelProvider.getDurationAverage() != null) {
            fightStatistics.setTime(fightStatistics.getDuelsNumber() * duelProvider.getDurationAverage());
        }
        return fightStatistics;
    }

    private int getDuels(int fightByTeam, int teamSize, Collection<Team> teams) {
        final AtomicInteger counter = new AtomicInteger();
        final AtomicInteger missingMembers = new AtomicInteger();

        teams.forEach(team -> {
            counter.addAndGet((team.getMembers().size() * fightByTeam) - missingMembers.get());
            missingMembers.addAndGet(teamSize - team.getMembers().size());
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
}
