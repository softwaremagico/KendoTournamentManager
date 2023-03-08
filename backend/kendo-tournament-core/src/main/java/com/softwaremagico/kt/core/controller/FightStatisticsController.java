package com.softwaremagico.kt.core.controller;

import com.softwaremagico.kt.core.controller.models.*;
import com.softwaremagico.kt.core.converters.FightStatisticsConverter;
import com.softwaremagico.kt.core.converters.TeamConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.models.FightStatisticsConverterRequest;
import com.softwaremagico.kt.core.providers.FightStatisticsProvider;
import com.softwaremagico.kt.core.statistics.FightStatistics;
import com.softwaremagico.kt.core.statistics.FightStatisticsRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class FightStatisticsController extends BasicInsertableController<FightStatistics, FightStatisticsDTO, FightStatisticsRepository,
        FightStatisticsProvider, FightStatisticsConverterRequest, FightStatisticsConverter> {

    private final TournamentConverter tournamentConverter;

    private final TeamConverter teamConverter;

    public FightStatisticsController(FightStatisticsProvider provider, FightStatisticsConverter converter, TournamentConverter tournamentConverter,
                                     TeamConverter teamConverter) {
        super(provider, converter);
        this.tournamentConverter = tournamentConverter;
        this.teamConverter = teamConverter;
    }

    @Override
    protected FightStatisticsConverterRequest createConverterRequest(FightStatistics fightStatistics) {
        return new FightStatisticsConverterRequest(fightStatistics);
    }

    /**
     * Calculate the statistics by teams if they are already defined, or using the members if not.
     *
     * @param tournamentDTO the tournament.
     * @return some estimations.
     */
    public FightStatisticsDTO estimate(TournamentDTO tournamentDTO) {
        return converter.convert(new FightStatisticsConverterRequest(provider.estimate(tournamentConverter.reverse(tournamentDTO))));
    }

    public FightStatisticsDTO estimateByTeams(TournamentDTO tournamentDTO) {
        return converter.convert(new FightStatisticsConverterRequest(provider.estimateByTeams(tournamentConverter.reverse(tournamentDTO))));
    }

    public FightStatisticsDTO estimateByMembers(TournamentDTO tournamentDTO) {
        return converter.convert(new FightStatisticsConverterRequest(provider.estimateByMembers(tournamentConverter.reverse(tournamentDTO))));
    }

    public FightStatisticsDTO estimate(TournamentDTO tournamentDTO, Collection<TeamDTO> teams) {
        return estimate(tournamentDTO, tournamentDTO.getTeamSize(), teams);
    }

    public FightStatisticsDTO estimateByRoles(TournamentDTO tournamentDTO, Collection<RoleDTO> roles) {
        return estimate(tournamentDTO, emulateTeams(tournamentDTO, roles.stream().map(RoleDTO::getParticipant).collect(Collectors.toList())));
    }

    public FightStatisticsDTO estimate(TournamentDTO tournamentDTO, int teamSize, Collection<TeamDTO> teams) {
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

    private FightStatisticsDTO estimateLeagueStatistics(int teamSize, Collection<TeamDTO> teams) {
        return converter.convert(new FightStatisticsConverterRequest(provider.estimateLeagueStatistics(teamSize,
                teamConverter.reverseAll(teams))));
    }

    private FightStatisticsDTO estimateLoopStatistics(TournamentDTO tournamentDTO, int teamSize, Collection<TeamDTO> teams) {
        return converter.convert(new FightStatisticsConverterRequest(provider.estimateLoopStatistics(tournamentConverter.reverse(tournamentDTO),
                teamSize, teamConverter.reverseAll(teams))));
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
