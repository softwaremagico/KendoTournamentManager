package com.softwaremagico.kt.core.statistics;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2022 Softwaremagico
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

import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.RoleConverter;
import com.softwaremagico.kt.core.converters.TeamConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.models.RoleConverterRequest;
import com.softwaremagico.kt.core.converters.models.TeamConverterRequest;
import com.softwaremagico.kt.core.providers.DuelProvider;
import com.softwaremagico.kt.core.providers.RoleProvider;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.core.providers.TournamentExtraPropertyProvider;
import com.softwaremagico.kt.core.statistics.models.FightStatisticsDTO;
import com.softwaremagico.kt.persistence.entities.Role;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.entities.TournamentExtraPropertyKey;
import com.softwaremagico.kt.persistence.values.RoleType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class FightStatisticsProvider {
    private static final int TIME_BETWEEN_FIGHTS = 20;
    private static final int TIME_BETWEEN_DUELS = 10;

    private final DuelProvider duelProvider;
    private final TeamProvider teamProvider;
    private final TeamConverter teamConverter;
    private final TournamentConverter tournamentConverter;
    private final RoleProvider roleProvider;
    private final RoleConverter roleConverter;
    private final TournamentExtraPropertyProvider tournamentExtraPropertyProvider;

    public FightStatisticsProvider(DuelProvider duelProvider, TeamProvider teamProvider, TeamConverter teamConverter,
                                   TournamentConverter tournamentConverter, RoleProvider roleProvider, RoleConverter roleConverter,
                                   TournamentExtraPropertyProvider tournamentExtraPropertyProvider) {
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
     * @param tournamentDTO the tournament.
     * @return some estimations.
     */
    public FightStatisticsDTO calculate(TournamentDTO tournamentDTO) {
        final List<Team> teams = teamProvider.getAll(tournamentConverter.reverse(tournamentDTO));
        if (!teams.isEmpty()) {
            return calculate(tournamentDTO, teamConverter.convertAll(teams.stream()
                    .map(TeamConverterRequest::new).collect(Collectors.toList())));
        }
        final List<Role> roles = roleProvider.getAll(tournamentConverter.reverse(tournamentDTO));
        return calculateByRoles(tournamentDTO, roleConverter.convertAll(roles.stream().filter(role ->
                        Objects.equals(role.getRoleType(), RoleType.COMPETITOR)).collect(Collectors.toList()).stream()
                .map(RoleConverterRequest::new).collect(Collectors.toList())));
    }

    public FightStatisticsDTO calculateByTeams(TournamentDTO tournamentDTO) {
        final List<Team> teams = teamProvider.getAll(tournamentConverter.reverse(tournamentDTO));
        return calculate(tournamentDTO, teamConverter.convertAll(teams.stream()
                .map(TeamConverterRequest::new).collect(Collectors.toList())));
    }

    public FightStatisticsDTO calculateByMembers(TournamentDTO tournamentDTO) {
        final List<Role> roles = roleProvider.getAll(tournamentConverter.reverse(tournamentDTO));
        return calculateByRoles(tournamentDTO, roleConverter.convertAll(roles.stream().filter(role ->
                        Objects.equals(role.getRoleType(), RoleType.COMPETITOR)).collect(Collectors.toList()).stream()
                .map(RoleConverterRequest::new).collect(Collectors.toList())));
    }

    public FightStatisticsDTO calculate(TournamentDTO tournamentDTO, Collection<TeamDTO> teams) {
        return calculate(tournamentDTO, tournamentDTO.getTeamSize(), teams);
    }

    public FightStatisticsDTO calculateByRoles(TournamentDTO tournamentDTO, Collection<RoleDTO> roles) {
        return calculate(tournamentDTO, emulateTeams(tournamentDTO, roles.stream().map(RoleDTO::getParticipant).collect(Collectors.toList())));
    }

    public FightStatisticsDTO calculate(TournamentDTO tournamentDTO, int teamSize, Collection<TeamDTO> teams) {
        if (tournamentDTO == null || teams == null || teams.size() < 2) {
            return null;
        }
        switch (tournamentDTO.getType()) {
            case LEAGUE:
                return calculateLeagueStatistics(teamSize, teams);
            case LOOP:
                return calculateLoopStatistics(tournamentDTO, teamSize, teams);
            case CUSTOMIZED:
            case KING_OF_THE_MOUNTAIN:
            default:
                return null;
        }
    }

    private FightStatisticsDTO calculateLeagueStatistics(int teamSize, Collection<TeamDTO> teams) {
        final FightStatisticsDTO fightStatisticsDTO = new FightStatisticsDTO();
        fightStatisticsDTO.setFightsNumber((teams.size() * (teams.size() - 1)) / 2);
        fightStatisticsDTO.setFightsByTeam((teams.size() - 1));
        fightStatisticsDTO.setDuelsNumber(getDuels(fightStatisticsDTO.getFightsByTeam(), teamSize, teams));
        if (duelProvider.getDurationAverage() != null && fightStatisticsDTO.getDuelsNumber() != null) {
            fightStatisticsDTO.setTime(fightStatisticsDTO.getDuelsNumber() * (duelProvider.getDurationAverage() + TIME_BETWEEN_DUELS) +
                    (fightStatisticsDTO.getFightsNumber() != null ? (long) TIME_BETWEEN_FIGHTS * fightStatisticsDTO.getFightsNumber() : 0));
        }
        return fightStatisticsDTO;
    }

    private FightStatisticsDTO calculateLoopStatistics(TournamentDTO tournamentDTO, int teamSize, Collection<TeamDTO> teams) {
        final FightStatisticsDTO fightStatisticsDTO = new FightStatisticsDTO();
        final TournamentExtraProperty property = tournamentExtraPropertyProvider.getByTournamentAndProperty(tournamentConverter.reverse(tournamentDTO),
                TournamentExtraPropertyKey.MAXIMIZE_FIGHTS);
        final boolean maximizeFights = property != null && Boolean.parseBoolean(property.getValue());
        if (maximizeFights) {
            fightStatisticsDTO.setFightsNumber((teams.size() * (teams.size() - 1)));
            fightStatisticsDTO.setFightsByTeam((teams.size() - 1) * 2);
        } else {
            fightStatisticsDTO.setFightsNumber((teams.size() * teams.size() - 1) / 2);
            fightStatisticsDTO.setFightsByTeam((teams.size() - 1));
        }
        fightStatisticsDTO.setDuelsNumber(getDuels(fightStatisticsDTO.getFightsByTeam(), teamSize, teams));
        if (duelProvider.getDurationAverage() != null) {
            fightStatisticsDTO.setTime(fightStatisticsDTO.getDuelsNumber() * duelProvider.getDurationAverage());
        }
        return fightStatisticsDTO;
    }

    private int getDuels(int fightByTeam, int teamSize, Collection<TeamDTO> teams) {
        final AtomicInteger counter = new AtomicInteger();
        final AtomicInteger missingMembers = new AtomicInteger();

        teams.forEach(teamDTO -> {
            counter.addAndGet((teamDTO.getMembers().size() * fightByTeam) - missingMembers.get());
            missingMembers.addAndGet(teamSize - teamDTO.getMembers().size());
        });

        //Duels are counted twice, once for each team
        return counter.get() / 2;
    }

    /**
     * Emulate some teams if are not defined yet only to calculate the number of fights and duels.
     *
     * @param tournamentDTO the tournament where emulate.
     * @param participants  the list of members that will participate on the tournament.
     * @return a list of transient teams.
     */
    private List<TeamDTO> emulateTeams(TournamentDTO tournamentDTO, Collection<ParticipantDTO> participants) {
        int teamIndex = 0;
        final List<TeamDTO> teams = new ArrayList<>();
        TeamDTO team = null;
        int teamMember = 0;


        for (final ParticipantDTO competitor : participants) {
            // Create a new team.
            if (team == null) {
                teamIndex++;
                team = new TeamDTO("Team" + String.format("%02d", teamIndex), tournamentDTO);
                teamMember = 0;
            }

            // Add member.
            team.addMember(competitor);

            if (teamMember == 0) {
                teams.add(team);
            }

            teamMember++;

            // Team filled up, create a new team.
            if (teamMember >= tournamentDTO.getTeamSize()) {
                team = null;
            }
        }
        return teams;
    }
}