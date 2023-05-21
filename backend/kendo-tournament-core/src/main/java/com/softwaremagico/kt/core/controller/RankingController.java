package com.softwaremagico.kt.core.controller;

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

import com.softwaremagico.kt.core.controller.models.*;
import com.softwaremagico.kt.core.converters.*;
import com.softwaremagico.kt.core.converters.models.*;
import com.softwaremagico.kt.core.exceptions.GroupNotFoundException;
import com.softwaremagico.kt.core.exceptions.TournamentNotFoundException;
import com.softwaremagico.kt.core.providers.*;
import com.softwaremagico.kt.core.score.CompetitorRanking;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.ScoreType;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class RankingController {

    private final GroupProvider groupProvider;

    private final GroupConverter groupConverter;

    private final FightProvider fightProvider;

    private final TournamentConverter tournamentConverter;

    private final FightConverter fightConverter;

    private final TeamProvider teamProvider;

    private final TeamConverter teamConverter;

    private final TournamentProvider tournamentProvider;

    private final DuelConverter duelConverter;

    private final DuelProvider duelProvider;

    private final ParticipantConverter participantConverter;

    private final ParticipantProvider participantProvider;

    private final RoleProvider roleProvider;

    private final RankingProvider rankingProvider;

    private final ScoreOfCompetitorConverter scoreOfCompetitorConverter;

    private final ScoreOfTeamConverter scoreOfTeamConverter;

    public RankingController(GroupProvider groupProvider, GroupConverter groupConverter, FightProvider fightProvider,
                             TournamentConverter tournamentConverter, FightConverter fightConverter,
                             TeamProvider teamProvider, TeamConverter teamConverter, TournamentProvider tournamentProvider,
                             DuelConverter duelConverter, DuelProvider duelProvider, ParticipantConverter participantConverter,
                             ParticipantProvider participantProvider, RoleProvider roleProvider, RankingProvider rankingProvider,
                             ScoreOfCompetitorConverter scoreOfCompetitorConverter, ScoreOfTeamConverter scoreOfTeamConverter) {
        this.groupProvider = groupProvider;
        this.groupConverter = groupConverter;
        this.fightProvider = fightProvider;
        this.tournamentConverter = tournamentConverter;
        this.fightConverter = fightConverter;
        this.teamProvider = teamProvider;
        this.teamConverter = teamConverter;
        this.tournamentProvider = tournamentProvider;
        this.duelConverter = duelConverter;
        this.duelProvider = duelProvider;
        this.participantConverter = participantConverter;
        this.participantProvider = participantProvider;
        this.roleProvider = roleProvider;
        this.rankingProvider = rankingProvider;
        this.scoreOfCompetitorConverter = scoreOfCompetitorConverter;
        this.scoreOfTeamConverter = scoreOfTeamConverter;
    }

    private boolean checkLevel(TournamentDTO tournament) {
        return tournament == null || tournament.getType() != TournamentType.KING_OF_THE_MOUNTAIN;
    }

    public List<TeamDTO> getTeamsRanking(GroupDTO groupDTO) {
        return teamConverter.convertAll(rankingProvider.getTeamsRanking(groupConverter.reverse(groupDTO))
                .stream().map(TeamConverterRequest::new).collect(Collectors.toList()));
    }

    public List<ScoreOfTeamDTO> getTeamsScoreRankingFromGroup(Integer groupId) {
        final Group group = groupProvider.getGroup(groupId);
        if (group == null) {
            throw new GroupNotFoundException(this.getClass(), "Group with id" + groupId + " not found!");
        }
        return getTeamsScoreRanking(groupConverter.convert(new GroupConverterRequest(group)));
    }

    public List<ScoreOfTeamDTO> getTeamsScoreRankingFromTournament(Integer tournamentId) {
        final Tournament tournament = tournamentProvider.get(tournamentId).orElseThrow(() ->
                new TournamentNotFoundException(this.getClass(), "Tournament with id" + tournamentId + " not found!"));
        return getTeamsScoreRanking(tournamentConverter.convert(new TournamentConverterRequest(tournament)));
    }

    public List<ScoreOfTeamDTO> getTeamsScoreRanking(GroupDTO groupDTO) {
        if (groupDTO == null) {
            return new ArrayList<>();
        }
        return getTeamsScoreRanking(groupDTO.getTournament().getTournamentScore().getScoreType(),
                groupDTO.getTeams(), groupDTO.getFights(), groupDTO.getUnties(), checkLevel(groupDTO.getTournament()));
    }

    public List<ScoreOfTeamDTO> getTeamsScoreRanking(ScoreType type, List<TeamDTO> teams, List<FightDTO> fights, List<DuelDTO> unties,
                                                     boolean checkLevel) {
        return scoreOfTeamConverter.convertAll(rankingProvider.getTeamsScoreRanking(
                type,
                teamConverter.reverseAll(teams),
                fightConverter.reverseAll(fights),
                duelConverter.reverseAll(unties),
                checkLevel
        ).stream().map(ScoreOfTeamConverterRequest::new).collect(Collectors.toList()));
    }

    public List<ScoreOfTeamDTO> getTeamsScoreRanking(TournamentDTO tournamentDTO) {
        return scoreOfTeamConverter.convertAll(rankingProvider.getTeamsScoreRanking(tournamentConverter.reverse(tournamentDTO))
                .stream().map(ScoreOfTeamConverterRequest::new).collect(Collectors.toList()));
    }

    /**
     * Return a Hashmap that classify the teams by position (1st, 2nd, 3rd,...)
     *
     * @return classification of the teams
     */
    public Map<Integer, List<TeamDTO>> getTeamsByPosition(GroupDTO groupDTO) {
        final Map<Integer, List<Team>> teamsByPosition = rankingProvider.getTeamsByPosition(groupConverter.reverse(groupDTO));
        final Map<Integer, List<TeamDTO>> teamsByPositionDTO = new HashMap<>();
        teamsByPosition.keySet().forEach(key -> teamsByPositionDTO.put(key, teamConverter.convertAll(teamsByPosition.get(key)
                .stream().map(TeamConverterRequest::new).collect(Collectors.toList()))));
        return teamsByPositionDTO;
    }

    public List<TeamDTO> getFirstTeamsWithDrawScore(GroupDTO groupDTO, Integer maxWinners) {
        final Map<Integer, List<TeamDTO>> teamsByPosition = getTeamsByPosition(groupDTO);
        for (int i = 0; i < maxWinners; i++) {
            final List<TeamDTO> teamsInDraw = teamsByPosition.get(i);
            if (teamsInDraw.size() > 1) {
                return teamsInDraw;
            }
        }
        return new ArrayList<>();
    }

    public TeamDTO getTeam(GroupDTO groupDTO, Integer order) {
        final List<TeamDTO> teamsOrder = getTeamsRanking(groupDTO);
        if (order >= 0 && order < teamsOrder.size()) {
            return teamsOrder.get(order);
        }
        return null;
    }

    public List<ScoreOfCompetitorDTO> getCompetitorsScoreRankingFromGroup(Integer groupId) {
        final Group group = groupProvider.getGroup(groupId);
        if (group == null) {
            throw new GroupNotFoundException(this.getClass(), "Group with id" + groupId + " not found!");
        }
        return getCompetitorsScoreRanking(groupConverter.convert(new GroupConverterRequest(group)));
    }

    public List<ScoreOfCompetitorDTO> getCompetitorsScoreRanking(GroupDTO groupDTO) {
        return getCompetitorsScoreRanking(getParticipants(groupDTO.getTeams()), groupDTO.getFights(), groupDTO.getUnties(), groupDTO.getTournament());
    }

    public List<ScoreOfCompetitorDTO> getCompetitorsScoreRankingFromTournament(Integer tournamentId) {
        final Tournament tournament = tournamentProvider.get(tournamentId).orElseThrow(() ->
                new TournamentNotFoundException(this.getClass(), "Tournament with id" + tournamentId + " not found!"));
        return getCompetitorsScoreRanking(tournamentConverter.convert(new TournamentConverterRequest(tournament)));
    }

    public List<ScoreOfCompetitorDTO> getCompetitorsScoreRanking(TournamentDTO tournamentDTO) {
        return scoreOfCompetitorConverter.convertAll(rankingProvider.getCompetitorsScoreRanking(tournamentConverter.reverse(tournamentDTO))
                .stream().map(ScoreOfCompetitorConverterRequest::new).collect(Collectors.toList()));
    }

    public List<ScoreOfCompetitorDTO> getCompetitorsScoreRanking(Set<ParticipantDTO> competitors, List<FightDTO> fights, List<DuelDTO> unties,
                                                                 TournamentDTO tournamentDTO) {
        return scoreOfCompetitorConverter.convertAll(rankingProvider.getCompetitorsScoreRanking(
                participantConverter.reverseAll(competitors),
                fightConverter.reverseAll(fights),
                duelConverter.reverseAll(unties),
                tournamentConverter.reverse(tournamentDTO)
        ).stream().map(ScoreOfCompetitorConverterRequest::new).collect(Collectors.toList()));
    }

    public List<ScoreOfCompetitorDTO> getCompetitorsGlobalScoreRanking(Collection<ParticipantDTO> competitors) {
        return getCompetitorsGlobalScoreRanking(competitors, ScoreType.DEFAULT);
    }

    @Cacheable("competitors-ranking")
    public List<ScoreOfCompetitorDTO> getCompetitorsGlobalScoreRanking(Collection<ParticipantDTO> competitors, ScoreType scoreType) {
        return scoreOfCompetitorConverter.convertAll(rankingProvider.getCompetitorsGlobalScoreRanking(
                        participantConverter.reverseAll(competitors),
                        scoreType
                )
                .stream().map(ScoreOfCompetitorConverterRequest::new).collect(Collectors.toList()));
    }

    @Cacheable("competitors-ranking")
    public List<ScoreOfCompetitorDTO> getCompetitorGlobalRanking(ScoreType scoreType) {
        return scoreOfCompetitorConverter.convertAll(rankingProvider.getCompetitorGlobalRanking(scoreType).stream()
                .map(ScoreOfCompetitorConverterRequest::new).collect(Collectors.toSet()));
    }

    public CompetitorRanking getCompetitorRanking(ParticipantDTO participantDTO) {
        return rankingProvider.getCompetitorRanking(participantConverter.reverse(participantDTO));
    }

    public ScoreOfCompetitorDTO getScoreRanking(GroupDTO groupDTO, ParticipantDTO competitor) {
        return scoreOfCompetitorConverter.convert(new ScoreOfCompetitorConverterRequest(rankingProvider
                .getScoreRanking(groupConverter.reverse(groupDTO), participantConverter.reverse(competitor))));
    }

    public ParticipantDTO getCompetitor(GroupDTO groupDTO, Integer order) {
        return participantConverter.convert(new ParticipantConverterRequest(
                rankingProvider.getCompetitor(groupConverter.reverse(groupDTO), order)));
    }

    public ScoreOfCompetitorDTO getScoreOfCompetitor(GroupDTO groupDTO, Integer order) {
        return scoreOfCompetitorConverter.convert(new ScoreOfCompetitorConverterRequest(rankingProvider
                .getScoreOfCompetitor(groupConverter.reverse(groupDTO), order)));
    }

    private static Set<ParticipantDTO> getParticipants(List<TeamDTO> teams) {
        final Set<ParticipantDTO> allCompetitors = new HashSet<>();
        for (final TeamDTO team : teams) {
            allCompetitors.addAll(team.getMembers());
        }
        return allCompetitors;
    }

    public Integer getOrder(GroupDTO group, TeamDTO team) {
        final List<TeamDTO> ranking = getTeamsRanking(group);

        for (int i = 0; i < ranking.size(); i++) {
            if (ranking.get(i).equals(team)) {
                return i;
            }
        }
        return null;
    }

    @CacheEvict(allEntries = true, value = {"ranking", "competitors-ranking"})
    @Scheduled(fixedDelay = 60 * 10 * 1000)
    public void reportCacheEvict() {
        //Only for handling Spring cache.
    }
}
