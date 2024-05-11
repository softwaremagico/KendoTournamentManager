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

import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.core.controller.models.DuelDTO;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.ScoreOfCompetitorDTO;
import com.softwaremagico.kt.core.controller.models.ScoreOfTeamDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.ClubConverter;
import com.softwaremagico.kt.core.converters.DuelConverter;
import com.softwaremagico.kt.core.converters.FightConverter;
import com.softwaremagico.kt.core.converters.GroupConverter;
import com.softwaremagico.kt.core.converters.ParticipantConverter;
import com.softwaremagico.kt.core.converters.ParticipantReducedConverter;
import com.softwaremagico.kt.core.converters.ScoreOfCompetitorConverter;
import com.softwaremagico.kt.core.converters.ScoreOfTeamConverter;
import com.softwaremagico.kt.core.converters.TeamConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.models.ClubConverterRequest;
import com.softwaremagico.kt.core.converters.models.GroupConverterRequest;
import com.softwaremagico.kt.core.converters.models.ParticipantConverterRequest;
import com.softwaremagico.kt.core.converters.models.ScoreOfCompetitorConverterRequest;
import com.softwaremagico.kt.core.converters.models.ScoreOfTeamConverterRequest;
import com.softwaremagico.kt.core.converters.models.TeamConverterRequest;
import com.softwaremagico.kt.core.exceptions.ClubNotFoundException;
import com.softwaremagico.kt.core.exceptions.GroupNotFoundException;
import com.softwaremagico.kt.core.providers.ClubProvider;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.core.providers.RankingProvider;
import com.softwaremagico.kt.core.providers.RoleProvider;
import com.softwaremagico.kt.core.score.CompetitorRanking;
import com.softwaremagico.kt.core.score.ScoreOfCompetitor;
import com.softwaremagico.kt.persistence.entities.Club;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Role;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.ScoreType;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class RankingController {
    private static final int CACHE_EXPIRATION_TIME = 10 * 60 * 1000;

    private final GroupProvider groupProvider;

    private final GroupConverter groupConverter;

    private final TournamentConverter tournamentConverter;

    private final FightConverter fightConverter;

    private final TeamConverter teamConverter;

    private final DuelConverter duelConverter;

    private final ParticipantReducedConverter participantReducedConverter;

    private final ParticipantConverter participantConverter;

    private final RankingProvider rankingProvider;

    private final ScoreOfCompetitorConverter scoreOfCompetitorConverter;

    private final ScoreOfTeamConverter scoreOfTeamConverter;

    private final ClubProvider clubProvider;

    private final ParticipantProvider participantProvider;

    private final ClubConverter clubConverter;

    private final RoleProvider roleProvider;

    public RankingController(GroupProvider groupProvider, GroupConverter groupConverter,
                             TournamentConverter tournamentConverter, FightConverter fightConverter,
                             TeamConverter teamConverter, DuelConverter duelConverter,
                             ParticipantReducedConverter participantReducedConverter,
                             ParticipantConverter participantConverter, RankingProvider rankingProvider,
                             ScoreOfCompetitorConverter scoreOfCompetitorConverter,
                             ScoreOfTeamConverter scoreOfTeamConverter,
                             ClubProvider clubProvider, ParticipantProvider participantProvider, ClubConverter clubConverter,
                             RoleProvider roleProvider) {
        this.groupProvider = groupProvider;
        this.groupConverter = groupConverter;
        this.tournamentConverter = tournamentConverter;
        this.fightConverter = fightConverter;
        this.teamConverter = teamConverter;
        this.duelConverter = duelConverter;
        this.participantReducedConverter = participantReducedConverter;
        this.participantConverter = participantConverter;
        this.rankingProvider = rankingProvider;
        this.scoreOfCompetitorConverter = scoreOfCompetitorConverter;
        this.scoreOfTeamConverter = scoreOfTeamConverter;
        this.clubProvider = clubProvider;
        this.participantProvider = participantProvider;
        this.clubConverter = clubConverter;
        this.roleProvider = roleProvider;
    }

    private static Set<ParticipantDTO> getParticipants(List<TeamDTO> teams) {
        final Set<ParticipantDTO> allCompetitors = new HashSet<>();
        for (final TeamDTO team : teams) {
            allCompetitors.addAll(team.getMembers());
        }
        return allCompetitors;
    }

    private boolean checkLevel(TournamentDTO tournament) {
        return tournament == null || tournament.getType() != TournamentType.KING_OF_THE_MOUNTAIN;
    }

    public List<TeamDTO> getTeamsRanking(GroupDTO groupDTO) {
        return teamConverter.convertAll(rankingProvider.getTeamsRanking(groupConverter.reverse(groupDTO))
                .stream().map(TeamConverterRequest::new).toList());
    }

    public List<ScoreOfTeamDTO> getTeamsScoreRankingFromGroup(Integer groupId) {
        final Group group = groupProvider.getGroup(groupId);
        if (group == null) {
            throw new GroupNotFoundException(this.getClass(), "Group with id" + groupId + " not found!");
        }
        return getTeamsScoreRanking(groupConverter.convert(new GroupConverterRequest(group)));
    }

    public List<ScoreOfTeamDTO> getTeamsScoreRankingFromTournament(Integer tournamentId) {
        return scoreOfTeamConverter.convertAll(rankingProvider.getTeamsScoreRankingFromTournament(tournamentId)
                .stream().map(ScoreOfTeamConverterRequest::new).toList());
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
                //Checks ranking for same level or globally.
                checkLevel
        ).stream().map(ScoreOfTeamConverterRequest::new).toList());
    }

    public List<ScoreOfTeamDTO> getTeamsScoreRanking(TournamentDTO tournamentDTO) {
        return scoreOfTeamConverter.convertAll(rankingProvider.getTeamsScoreRanking(tournamentConverter.reverse(tournamentDTO))
                .stream().map(ScoreOfTeamConverterRequest::new).toList());
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
                .stream().map(TeamConverterRequest::new).toList())));
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
        return scoreOfCompetitorConverter.convertAll(rankingProvider.getCompetitorsScoreRankingFromTournament(tournamentId)
                .stream().map(ScoreOfCompetitorConverterRequest::new).toList());
    }

    public List<ScoreOfCompetitorDTO> getCompetitorsScoreRanking(TournamentDTO tournamentDTO) {
        return scoreOfCompetitorConverter.convertAll(rankingProvider.getCompetitorsScoreRanking(tournamentConverter.reverse(tournamentDTO))
                .stream().map(ScoreOfCompetitorConverterRequest::new).toList());
    }

    public List<ScoreOfCompetitorDTO> getCompetitorsScoreRanking(Set<ParticipantDTO> competitors, List<FightDTO> fights, List<DuelDTO> unties,
                                                                 TournamentDTO tournamentDTO) {
        return scoreOfCompetitorConverter.convertAll(rankingProvider.getCompetitorsScoreRanking(
                participantConverter.reverseAll(competitors),
                fightConverter.reverseAll(fights),
                duelConverter.reverseAll(unties),
                tournamentConverter.reverse(tournamentDTO)
        ).stream().map(ScoreOfCompetitorConverterRequest::new).toList());
    }

    public List<ScoreOfCompetitorDTO> getCompetitorsGlobalScoreRankingByClub(Integer clubId) {
        final Club club = clubProvider.get(clubId).orElseThrow(() ->
                new ClubNotFoundException(this.getClass(), "No club found with id '" + clubId + "'"));
        final List<Participant> participants = participantProvider.get(club);
        final List<Role> competitorRoles = roleProvider.get(participants, RoleType.COMPETITOR);
        participants.retainAll(competitorRoles.stream().map(Role::getParticipant).collect(Collectors.toSet()));
        return getCompetitorsGlobalScoreRanking(participantConverter.convertAll(participants.stream()
                .map(participant -> new ParticipantConverterRequest(participant, clubConverter.convert(new ClubConverterRequest(club)))).toList()), null);
    }

    public List<ScoreOfCompetitorDTO> getCompetitorsGlobalScoreRanking(Collection<ParticipantDTO> competitors, Integer fromNumberOfDays) {
        return getCompetitorsGlobalScoreRanking(competitors.stream().filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new)),
                ScoreType.DEFAULT, fromNumberOfDays);
    }


    public List<ScoreOfCompetitorDTO> getCompetitorsGlobalScoreRanking(Collection<ParticipantDTO> competitors, ScoreType scoreType, Integer fromNumberOfDays) {
        Map<Integer, ClubDTO> clubsById = null;
        try {
            clubsById = competitors.stream()
                    .map(ParticipantDTO::getClub).collect(Collectors.toMap(ClubDTO::getId, Function.identity(), (r1, r2) -> r1));
        } catch (NullPointerException ignore) {

        }

        final List<ScoreOfCompetitor> scoreOfCompetitors = rankingProvider.getCompetitorsGlobalScoreRanking(
                participantConverter.reverseAll(competitors), scoreType, fromNumberOfDays);

        if (clubsById != null) {
            final Map<Integer, ClubDTO> finalClubsById = clubsById;
            return scoreOfCompetitorConverter.convertAll(scoreOfCompetitors
                    .stream().map(scoreOfCompetitor -> new ScoreOfCompetitorConverterRequest(scoreOfCompetitor,
                            finalClubsById.get(scoreOfCompetitor.getCompetitor().getClub().getId()))).toList());
        }
        return scoreOfCompetitorConverter.convertAll(scoreOfCompetitors
                .stream().map(ScoreOfCompetitorConverterRequest::new).toList());
    }

    public List<ScoreOfCompetitorDTO> getCompetitorGlobalRanking(ScoreType scoreType) {
        return scoreOfCompetitorConverter.convertAll(rankingProvider.getCompetitorGlobalRanking(scoreType).stream()
                .map(ScoreOfCompetitorConverterRequest::new).collect(Collectors.toSet()));
    }

    public CompetitorRanking getCompetitorRanking(ParticipantDTO participantDTO) {
        if (participantDTO == null) {
            return null;
        }
        return rankingProvider.getCompetitorRanking(participantConverter.reverse(participantDTO));
    }

    public ScoreOfCompetitorDTO getScoreRanking(GroupDTO groupDTO, ParticipantDTO competitor) {
        return scoreOfCompetitorConverter.convert(new ScoreOfCompetitorConverterRequest(rankingProvider
                .getScoreRanking(groupConverter.reverse(groupDTO), participantConverter.reverse(competitor))));
    }

    public ParticipantDTO getCompetitor(GroupDTO groupDTO, Integer order) {
        return participantReducedConverter.convert(new ParticipantConverterRequest(
                rankingProvider.getCompetitor(groupConverter.reverse(groupDTO), order)));
    }

    public ScoreOfCompetitorDTO getScoreOfCompetitor(GroupDTO groupDTO, Integer order) {
        return scoreOfCompetitorConverter.convert(new ScoreOfCompetitorConverterRequest(rankingProvider
                .getScoreOfCompetitor(groupConverter.reverse(groupDTO), order)));
    }

    public Integer getOrder(GroupDTO groupDTO, TeamDTO teamDTO) {
        return rankingProvider.getOrder(groupConverter.reverse(groupDTO), teamConverter.reverse(teamDTO));
    }

    @CacheEvict(allEntries = true, value = {"ranking", "competitors-ranking"})
    @Scheduled(fixedDelay = CACHE_EXPIRATION_TIME)
    public void reportCacheEvict() {
        //Only for handling Spring cache.
    }
}
