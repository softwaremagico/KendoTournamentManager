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

import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.providers.DuelProvider;
import com.softwaremagico.kt.core.statistics.models.FightStatisticsDTO;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class FightStatisticsProvider {
    private static final int TIME_BETWEEN_FIGHTS = 20;
    private static final int TIME_BETWEEN_DUELS = 10;

    private final DuelProvider duelProvider;

    public FightStatisticsProvider(DuelProvider duelProvider) {
        this.duelProvider = duelProvider;
    }

    public FightStatisticsDTO calculate(TournamentDTO tournamentDTO, Collection<TeamDTO> teams) {
        return calculate(tournamentDTO.getType(), tournamentDTO.isMaximizeFights(), tournamentDTO.getTeamSize(), teams);
    }


    public FightStatisticsDTO calculate(TournamentType tournamentType, boolean maximizeFights, int teamSize, Collection<TeamDTO> teams) {
        if (tournamentType == null || teams == null || teams.size() < 2) {
            return null;
        }
        switch (tournamentType) {
            case LEAGUE:
                return calculateLeagueStatistics(teamSize, teams);
            case LOOP:
                return calculateLoopStatistics(maximizeFights, teamSize, teams);
            case CUSTOMIZED:
            case KING_OF_THE_MOUNTAIN:
                return null;
        }
        return null;
    }

    private FightStatisticsDTO calculateLeagueStatistics(int teamSize, Collection<TeamDTO> teams) {
        final FightStatisticsDTO fightStatisticsDTO = new FightStatisticsDTO();
        fightStatisticsDTO.setFightNumber((teams.size() * (teams.size() - 1)) / 2);
        fightStatisticsDTO.setFightsByTeam((teams.size() - 1));
        fightStatisticsDTO.setDuelNumber(getDuels(fightStatisticsDTO.getFightsByTeam(), teamSize, teams));
        if (duelProvider.getDurationAverage() != null) {
            fightStatisticsDTO.setTime(fightStatisticsDTO.getDuelNumber() * (duelProvider.getDurationAverage() + TIME_BETWEEN_DUELS) +
                    (long) TIME_BETWEEN_FIGHTS * fightStatisticsDTO.getFightNumber());
        }
        return fightStatisticsDTO;
    }

    private FightStatisticsDTO calculateLoopStatistics(boolean maximizeFights, int teamSize, Collection<TeamDTO> teams) {
        final FightStatisticsDTO fightStatisticsDTO = new FightStatisticsDTO();
        if (maximizeFights) {
            fightStatisticsDTO.setFightNumber((teams.size() * (teams.size() - 1)));
            fightStatisticsDTO.setFightsByTeam((teams.size() - 1) * 2);
        } else {
            fightStatisticsDTO.setFightNumber((teams.size() * teams.size() - 1) / 2);
            fightStatisticsDTO.setFightsByTeam((teams.size() - 1));
        }
        fightStatisticsDTO.setDuelNumber(getDuels(fightStatisticsDTO.getFightsByTeam(), teamSize, teams));
        if (duelProvider.getDurationAverage() != null) {
            fightStatisticsDTO.setTime(fightStatisticsDTO.getDuelNumber() * duelProvider.getDurationAverage());
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
}
