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
import com.softwaremagico.kt.core.score.*;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Role;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.ScoreType;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    public RankingController(GroupProvider groupProvider, GroupConverter groupConverter, FightProvider fightProvider,
                             TournamentConverter tournamentConverter, FightConverter fightConverter,
                             TeamProvider teamProvider, TeamConverter teamConverter, TournamentProvider tournamentProvider,
                             DuelConverter duelConverter, DuelProvider duelProvider, ParticipantConverter participantConverter,
                             ParticipantProvider participantProvider, RoleProvider roleProvider) {
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
    }

    private boolean checkLevel(TournamentDTO tournament) {
        return tournament == null || tournament.getType() != TournamentType.KING_OF_THE_MOUNTAIN;
    }

    public List<TeamDTO> getTeamsRanking(Integer groupId) {
        final Group group = groupProvider.getGroup(groupId);
        if (group == null) {
            throw new GroupNotFoundException(this.getClass(), "Group with id" + groupId + " not found!");
        }
        return getTeamsRanking(groupConverter.convert(new GroupConverterRequest(group)));
    }

    public List<TeamDTO> getTeamsRanking(GroupDTO groupDTO) {
        final List<ScoreOfTeam> scores = getTeamsScoreRanking(groupDTO);
        final List<TeamDTO> teamRanking = new ArrayList<>();
        for (final ScoreOfTeam score : scores) {
            teamRanking.add(score.getTeam());
        }
        return teamRanking;
    }

    public List<ScoreOfTeam> getTeamsScoreRankingFromGroup(Integer groupId) {
        final Group group = groupProvider.getGroup(groupId);
        if (group == null) {
            throw new GroupNotFoundException(this.getClass(), "Group with id" + groupId + " not found!");
        }
        return getTeamsScoreRanking(groupConverter.convert(new GroupConverterRequest(group)));
    }

    public List<ScoreOfTeam> getTeamsScoreRankingFromTournament(Integer tournamentId) {
        final Tournament tournament = tournamentProvider.get(tournamentId).orElseThrow(() ->
                new TournamentNotFoundException(this.getClass(), "Tournament with id" + tournamentId + " not found!"));
        return getTeamsScoreRanking(tournamentConverter.convert(new TournamentConverterRequest(tournament)));
    }

    public List<ScoreOfTeam> getTeamsScoreRanking(GroupDTO groupDTO) {
        if (groupDTO == null) {
            return new ArrayList<>();
        }
        return getTeamsScoreRanking(groupDTO.getTournament().getTournamentScore().getScoreType(),
                groupDTO.getTeams(), groupDTO.getFights(), groupDTO.getUnties(), checkLevel(groupDTO.getTournament()));
    }

    public List<ScoreOfTeam> getTeamsScoreRanking(ScoreType type, List<TeamDTO> teams, List<FightDTO> fights, List<DuelDTO> unties,
                                                  boolean checkLevel) {
        final List<ScoreOfTeam> scores = new ArrayList<>();
        for (final TeamDTO team : teams) {
            scores.add(new ScoreOfTeam(team, fights, unties));
        }
        sortTeamsScores(type, scores, checkLevel);
        if (scores.isEmpty()) {
            return scores;
        }
        //check draw values.
        int sortingIndex = 0;
        scores.get(0).setSortingIndex(sortingIndex);
        for (int i = 1; i < scores.size(); i++) {
            if (getTeamsSorter(type, checkLevel).compare(scores.get(i - 1), scores.get(i)) != 0) {
                sortingIndex++;
            }
            scores.get(i).setSortingIndex(sortingIndex);
        }
        return scores;
    }

    public List<ScoreOfTeam> getTeamsScoreRanking(TournamentDTO tournamentDTO) {
        final Tournament tournament = tournamentConverter.reverse(tournamentDTO);
        return getTeamsScoreRanking(tournamentDTO.getTournamentScore().getScoreType(),
                teamConverter.convertAll(teamProvider.getAll(tournament).stream()
                        .map(TeamConverterRequest::new).collect(Collectors.toList())),
                fightConverter.convertAll(fightProvider.getFights(tournament).stream()
                        .map(FightConverterRequest::new).collect(Collectors.toList())),
                duelConverter.convertAll(groupProvider.getGroups(tournament).stream()
                        .flatMap(group -> group.getUnties().stream())
                        .collect(Collectors.toList()).stream().map(DuelConverterRequest::new).collect(Collectors.toList())),
                checkLevel(tournamentDTO));
    }

    /**
     * Return a Hashmap that classify the teams by position (1st, 2nd, 3rd,...)
     *
     * @return classification of the teams
     */
    public Map<Integer, List<TeamDTO>> getTeamsByPosition(GroupDTO groupDTO) {
        final HashMap<Integer, List<TeamDTO>> teamsByPosition = new HashMap<>();
        final List<ScoreOfTeam> scores = getTeamsScoreRanking(groupDTO);

        Integer position = 0;
        for (int i = 0; i < scores.size(); i++) {
            teamsByPosition.computeIfAbsent(position, k -> new ArrayList<>());
            // Put team in position.
            teamsByPosition.get(position).add(scores.get(i).getTeam());
            // Different score with next team.
            if ((i < scores.size() - 1) && getTeamsSorter(groupDTO.getTournament().getTournamentScore().getScoreType(),
                    checkLevel(groupDTO.getTournament()))
                    .compare(scores.get(i), scores.get(i + 1)) != 0) {
                position++;
            }
        }

        return teamsByPosition;
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

    public ScoreOfTeam getScoreOfTeam(GroupDTO groupDTO, Integer order) {
        final List<ScoreOfTeam> teamsOrder = getTeamsScoreRanking(groupDTO);
        if (order >= 0 && order < teamsOrder.size()) {
            return teamsOrder.get(order);
        }
        return null;
    }

    public List<ParticipantDTO> getParticipants(GroupDTO groupDTO) {
        final Set<ParticipantDTO> competitors = getParticipants(groupDTO.getTeams());
        final List<ScoreOfCompetitor> scores = new ArrayList<>();
        for (final ParticipantDTO competitor : competitors) {
            scores.add(new ScoreOfCompetitor(competitor, groupDTO.getFights(), groupDTO.getUnties(),
                    countNotOver(groupDTO.getTournament())));
        }
        sortCompetitorsScores(groupDTO.getTournament().getTournamentScore().getScoreType(), scores);
        final List<ParticipantDTO> competitorsRanking = new ArrayList<>();
        for (final ScoreOfCompetitor score : scores) {
            competitorsRanking.add(score.getCompetitor());
        }
        return competitorsRanking;
    }

    public List<ScoreOfCompetitor> getCompetitorsScoreRankingFromGroup(Integer groupId) {
        final Group group = groupProvider.getGroup(groupId);
        if (group == null) {
            throw new GroupNotFoundException(this.getClass(), "Group with id" + groupId + " not found!");
        }
        return getCompetitorsScoreRanking(groupConverter.convert(new GroupConverterRequest(group)));
    }

    public List<ScoreOfCompetitor> getCompetitorsScoreRanking(GroupDTO groupDTO) {
        return getCompetitorsScoreRanking(getParticipants(groupDTO.getTeams()), groupDTO.getFights(), groupDTO.getUnties(), groupDTO.getTournament());
    }

    public List<ScoreOfCompetitor> getCompetitorsScoreRankingFromTournament(Integer tournamentId) {
        final Tournament tournament = tournamentProvider.get(tournamentId).orElseThrow(() ->
                new TournamentNotFoundException(this.getClass(), "Tournament with id" + tournamentId + " not found!"));
        return getCompetitorsScoreRanking(tournamentConverter.convert(new TournamentConverterRequest(tournament)));
    }

    public List<ScoreOfCompetitor> getCompetitorsScoreRanking(TournamentDTO tournamentDTO) {
        final List<GroupDTO> groups = groupConverter.convertAll(groupProvider.
                getGroups(tournamentConverter.reverse(tournamentDTO)).stream().map(GroupConverterRequest::new).collect(Collectors.toList()));

        return getCompetitorsScoreRanking(getParticipants(groups.stream()
                        .flatMap(group -> group.getTeams().stream())
                        .collect(Collectors.toList())),
                groups.stream()
                        .flatMap(group -> group.getFights().stream())
                        .collect(Collectors.toList()),
                groups.stream()
                        .flatMap(group -> group.getUnties().stream())
                        .collect(Collectors.toList()),
                tournamentDTO);
    }

    public List<ScoreOfCompetitor> getCompetitorsScoreRanking(Set<ParticipantDTO> competitors, List<FightDTO> fights, List<DuelDTO> unties,
                                                              TournamentDTO tournamentDTO) {
        final List<ScoreOfCompetitor> scores = new ArrayList<>();
        for (final ParticipantDTO competitor : competitors) {
            scores.add(new ScoreOfCompetitor(competitor, fights, unties, countNotOver(tournamentDTO)));
        }
        sortCompetitorsScores(tournamentDTO.getTournamentScore().getScoreType(), scores);
        return scores;
    }

    public List<ScoreOfCompetitor> getCompetitorsGlobalScoreRanking(Collection<ParticipantDTO> competitors) {
        return getCompetitorsGlobalScoreRanking(competitors, ScoreType.DEFAULT);
    }

    public List<ScoreOfCompetitor> getCompetitorsGlobalScoreRanking(Collection<ParticipantDTO> competitors, ScoreType scoreType) {
        if (competitors == null || competitors.isEmpty()) {
            competitors = participantConverter.convertAll(participantProvider.getAll().stream()
                    .map(ParticipantConverterRequest::new).collect(Collectors.toList()));
        }
        final List<ScoreOfCompetitor> scores = new ArrayList<>();
        final List<FightDTO> fights = fightConverter.convertAll(fightProvider.get(participantConverter.reverseAll(competitors)).stream()
                .map(FightConverterRequest::new).collect(Collectors.toSet()));
        final List<DuelDTO> unties = duelConverter.convertAll(duelProvider.getUnties(participantConverter.reverseAll(competitors)).stream()
                .map(DuelConverterRequest::new).collect(Collectors.toSet()));
        for (final ParticipantDTO competitor : competitors) {
            scores.add(new ScoreOfCompetitor(competitor, fights, unties, false));
        }
        sortCompetitorsScores(scoreType, scores);
        return scores;
    }

    public List<ScoreOfCompetitor> getCompetitorGlobalRanking(ScoreType scoreType) {
        final List<ScoreOfCompetitor> scores = new ArrayList<>();
        final List<FightDTO> fights = fightConverter.convertAll(fightProvider.getAll().stream()
                .map(FightConverterRequest::new).collect(Collectors.toSet()));
        final List<DuelDTO> unties = duelConverter.convertAll(duelProvider.getUnties().stream()
                .map(DuelConverterRequest::new).collect(Collectors.toSet()));
        final List<ParticipantDTO> competitors = participantConverter.convertAll(roleProvider.getAll().stream()
                .filter(role -> role.getRoleType() == RoleType.COMPETITOR).map(Role::getParticipant)
                .map(ParticipantConverterRequest::new).collect(Collectors.toSet()));
        for (final ParticipantDTO competitor : competitors) {
            scores.add(new ScoreOfCompetitor(competitor, fights, unties, false));
        }
        sortCompetitorsScores(scoreType, scores);
        return scores;
    }

    public CompetitorRanking getCompetitorRanking(ParticipantDTO participantDTO) {
        final List<ScoreOfCompetitor> ranking = getCompetitorGlobalRanking(ScoreType.DEFAULT);
        return new CompetitorRanking(IntStream.range(0, ranking.size())
                .filter(i -> Objects.equals(participantDTO, ranking.get(i).getCompetitor()))
                .findFirst().orElse(ranking.size() - 1), ranking.size());
    }

    public ScoreOfCompetitor getScoreRanking(GroupDTO groupDTO, ParticipantDTO competitor) {
        final List<ScoreOfCompetitor> scoreRanking = getCompetitorsScoreRanking(groupDTO);
        for (final ScoreOfCompetitor score : scoreRanking) {
            if (score.getCompetitor().equals(competitor)) {
                return score;
            }
        }
        return null;
    }

    public ParticipantDTO getCompetitor(GroupDTO groupDTO, Integer order) {
        final List<ParticipantDTO> competitorOrder = getParticipants(groupDTO);
        if (order >= 0 && order < competitorOrder.size()) {
            return competitorOrder.get(order);
        }
        return null;
    }

    public ScoreOfCompetitor getScoreOfCompetitor(GroupDTO groupDTO, Integer order) {
        final List<ScoreOfCompetitor> teamsOrder = getCompetitorsScoreRanking(groupDTO);
        if (order >= 0 && order < teamsOrder.size()) {
            return teamsOrder.get(order);
        }
        return null;
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

    public Integer getOrderFromRanking(List<ScoreOfTeam> ranking, TeamDTO team) {
        for (int i = 0; i < ranking.size(); i++) {
            if (ranking.get(i).getTeam().equals(team)) {
                return i;
            }
        }
        return null;
    }

    private static void sortTeamsScores(ScoreType type, List<ScoreOfTeam> scores, boolean checkLevel) {
        if (scores == null) {
            return;
        }
        scores.sort(getTeamsSorter(type, checkLevel));
    }

    private static Comparator<ScoreOfTeam> getTeamsSorter(ScoreType type, boolean checkLevel) {
        switch (type) {
            case CUSTOM:
                return new ScoreOfTeamCustom(checkLevel);
            case EUROPEAN:
                return new ScoreOfTeamEuropean(checkLevel);
            case INTERNATIONAL:
                return new ScoreOfTeamInternational(checkLevel);
            case WIN_OVER_DRAWS:
                return new ScoreOfTeamWinOverDraws(checkLevel);
            case CLASSIC:
            default:
                return new ScoreOfTeamClassic(checkLevel);
        }
    }

    private static void sortCompetitorsScores(ScoreType type, List<ScoreOfCompetitor> scores) {
        if (scores == null) {
            return;
        }
        scores.sort(getCompetitorsSorter(type));
    }

    private static Comparator<ScoreOfCompetitor> getCompetitorsSorter(ScoreType type) {
        switch (type) {
            case CUSTOM:
                return new ScoreOfCompetitorCustom();
            case EUROPEAN:
                return new ScoreOfCompetitorEuropean();
            case INTERNATIONAL:
                return new ScoreOfCompetitorInternational();
            case WIN_OVER_DRAWS:
                return new ScoreOfCompetitorWinOverDraws();
            case CLASSIC:
            default:
                return new ScoreOfCompetitorClassic();
        }
    }

    /**
     * On some leagues, we need to count the fights not finished for the score.
     *
     * @param tournamentDTO
     * @return if it must be counted.
     */
    private boolean countNotOver(TournamentDTO tournamentDTO) {
        return tournamentDTO.getType() == TournamentType.KING_OF_THE_MOUNTAIN;
    }

    @CacheEvict(allEntries = true, value = {"ranking"})
    @Scheduled(fixedDelay = 60 * 10 * 1000)
    public void reportCacheEvict() {
        //Only for handling Spring cache.
    }
}
