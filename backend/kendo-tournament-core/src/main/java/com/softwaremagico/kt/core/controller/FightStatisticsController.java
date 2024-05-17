package com.softwaremagico.kt.core.controller;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2024 Softwaremagico
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

import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.controller.models.TournamentFightStatisticsDTO;
import com.softwaremagico.kt.core.converters.TeamConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.TournamentFightStatisticsConverter;
import com.softwaremagico.kt.core.converters.models.TournamentFightStatisticsConverterRequest;
import com.softwaremagico.kt.core.providers.TournamentFightStatisticsProvider;
import com.softwaremagico.kt.core.statistics.TournamentFightStatistics;
import com.softwaremagico.kt.core.statistics.TournamentFightStatisticsRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class FightStatisticsController extends BasicInsertableController<TournamentFightStatistics, TournamentFightStatisticsDTO,
        TournamentFightStatisticsRepository, TournamentFightStatisticsProvider, TournamentFightStatisticsConverterRequest, TournamentFightStatisticsConverter> {

    private final TournamentConverter tournamentConverter;

    private final TeamConverter teamConverter;

    public FightStatisticsController(TournamentFightStatisticsProvider provider, TournamentFightStatisticsConverter converter,
                                     TournamentConverter tournamentConverter, TeamConverter teamConverter) {
        super(provider, converter);
        this.tournamentConverter = tournamentConverter;
        this.teamConverter = teamConverter;
    }

    @Override
    protected TournamentFightStatisticsConverterRequest createConverterRequest(TournamentFightStatistics tournamentFightStatistics) {
        return new TournamentFightStatisticsConverterRequest(tournamentFightStatistics);
    }

    /**
     * Calculate the statistics by teams if they are already defined, or using the members if not.
     *
     * @param tournamentDTO the tournament.
     * @return some estimations.
     */
    public TournamentFightStatisticsDTO estimate(TournamentDTO tournamentDTO) {
        return convert(getProvider().estimate(tournamentConverter.reverse(tournamentDTO)));
    }

    public TournamentFightStatisticsDTO estimateByTeams(TournamentDTO tournamentDTO) {
        return convert(getProvider().estimateByTeams(tournamentConverter.reverse(tournamentDTO)));
    }

    public TournamentFightStatisticsDTO estimateByMembers(TournamentDTO tournamentDTO) {
        return convert(getProvider().estimateByMembers(tournamentConverter.reverse(tournamentDTO)));
    }

    public TournamentFightStatisticsDTO estimate(TournamentDTO tournamentDTO, Collection<TeamDTO> teams) {
        return estimate(tournamentDTO, tournamentDTO.getTeamSize(), teams);
    }

    public TournamentFightStatisticsDTO estimateByRoles(TournamentDTO tournamentDTO, Collection<RoleDTO> roles) {
        return estimate(tournamentDTO, emulateTeams(tournamentDTO, roles.stream().map(RoleDTO::getParticipant).toList()));
    }

    public TournamentFightStatisticsDTO estimate(TournamentDTO tournamentDTO, int teamSize, Collection<TeamDTO> teams) {
        if (tournamentDTO == null || teams == null || teams.size() < 2) {
            return null;
        }
        switch (tournamentDTO.getType()) {
            case LEAGUE:
                return estimateLeagueStatistics(teamSize, teams);
            case LOOP:
                return estimateLoopStatistics(tournamentDTO, teamSize, teams);
            case CUSTOMIZED:
            case KING_OF_THE_MOUNTAIN:
            default:
                return null;
        }
    }

    private TournamentFightStatisticsDTO estimateLeagueStatistics(int teamSize, Collection<TeamDTO> teams) {
        return convert(getProvider().estimateLeagueStatistics(teamSize, teamConverter.reverseAll(teams)));
    }

    private TournamentFightStatisticsDTO estimateLoopStatistics(TournamentDTO tournamentDTO, int teamSize, Collection<TeamDTO> teams) {
        return convert(getProvider().estimateLoopStatistics(tournamentConverter.reverse(tournamentDTO), teamSize, teamConverter.reverseAll(teams)));
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
