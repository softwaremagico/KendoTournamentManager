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

import com.softwaremagico.kt.core.exceptions.GroupNotFoundException;
import com.softwaremagico.kt.core.exceptions.TournamentNotFoundException;
import com.softwaremagico.kt.core.score.CompetitorRanking;
import com.softwaremagico.kt.core.score.ScoreOfCompetitor;
import com.softwaremagico.kt.core.score.ScoreOfCompetitorClassic;
import com.softwaremagico.kt.core.score.ScoreOfCompetitorCustom;
import com.softwaremagico.kt.core.score.ScoreOfCompetitorEuropean;
import com.softwaremagico.kt.core.score.ScoreOfCompetitorInternational;
import com.softwaremagico.kt.core.score.ScoreOfCompetitorWinOverDraws;
import com.softwaremagico.kt.core.score.ScoreOfTeam;
import com.softwaremagico.kt.core.score.ScoreOfTeamClassic;
import com.softwaremagico.kt.core.score.ScoreOfTeamCustom;
import com.softwaremagico.kt.core.score.ScoreOfTeamEuropean;
import com.softwaremagico.kt.core.score.ScoreOfTeamInternational;
import com.softwaremagico.kt.core.score.ScoreOfTeamWinOverDraws;
import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Role;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.ScoreType;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

@Service
public class RankingProvider {

    private final FightProvider fightProvider;
    private final DuelProvider duelProvider;

    private final ParticipantProvider participantProvider;

    private final TournamentProvider tournamentProvider;

    private final GroupProvider groupProvider;

    private final RoleProvider roleProvider;

    private final TeamProvider teamProvider;

    public RankingProvider(FightProvider fightProvider, DuelProvider duelProvider, ParticipantProvider participantProvider,
                           TournamentProvider tournamentProvider, GroupProvider groupProvider, RoleProvider roleProvider, TeamProvider teamProvider) {
        this.fightProvider = fightProvider;
        this.duelProvider = duelProvider;
        this.participantProvider = participantProvider;
        this.tournamentProvider = tournamentProvider;
        this.groupProvider = groupProvider;
        this.roleProvider = roleProvider;
        this.teamProvider = teamProvider;
    }

    private static Set<Participant> getParticipants(List<Team> teams) {
        final Set<Participant> allCompetitors = new HashSet<>();
        for (final Team team : teams) {
            allCompetitors.addAll(team.getMembers());
        }
        return allCompetitors;
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

    public List<ScoreOfCompetitor> getCompetitorsScoreRankingFromTournament(Integer tournamentId) {
        final Tournament tournament = tournamentProvider.get(tournamentId).orElseThrow(() ->
                new TournamentNotFoundException(this.getClass(), "Tournament with id" + tournamentId + " not found!"));
        return getCompetitorsScoreRanking(tournament);
    }

    public List<ScoreOfCompetitor> getCompetitorsScoreRanking(Tournament tournament) {
        final List<Group> groups = groupProvider.getGroups(tournament);

        return getCompetitorsScoreRanking(getParticipants(groups.stream()
                        .flatMap(group -> group.getTeams().stream())
                        .toList()),
                groups.stream()
                        .flatMap(group -> group.getFights().stream())
                        .toList(),
                groups.stream()
                        .flatMap(group -> group.getUnties().stream())
                        .toList(),
                tournament);
    }

    public List<ScoreOfCompetitor> getCompetitorsScoreRanking(Group group) {
        return getCompetitorsScoreRanking(getParticipants(group.getTeams()), group.getFights(), group.getUnties(), group.getTournament());
    }

    public List<ScoreOfCompetitor> getCompetitorsScoreRanking(Collection<Participant> competitors, List<Fight> fights, List<Duel> unties,
                                                              Tournament tournamentDTO) {
        final List<ScoreOfCompetitor> scores = new ArrayList<>();
        for (final Participant competitor : competitors) {
            scores.add(new ScoreOfCompetitor(competitor, fights, unties, countNotOver(tournamentDTO)));
        }
        sortCompetitorsScores(tournamentDTO.getTournamentScore().getScoreType(), scores);
        return scores;
    }

    public List<ScoreOfTeam> getTeamsScoreRankingFromTournament(Integer tournamentId) {
        final Tournament tournament = tournamentProvider.get(tournamentId).orElseThrow(() ->
                new TournamentNotFoundException(this.getClass(), "Tournament with id" + tournamentId + " not found!"));
        return getTeamsScoreRanking(tournament);
    }

    /**
     * On some leagues, we need to count the fights not finished for the score.
     *
     * @param tournament
     * @return if it must be counted.
     */
    private boolean countNotOver(Tournament tournament) {
        return tournament.getType() == TournamentType.KING_OF_THE_MOUNTAIN;
    }

    public List<ScoreOfCompetitor> getCompetitorGlobalRanking(ScoreType scoreType) {
        final List<ScoreOfCompetitor> scores = new ArrayList<>();
        final List<Fight> fights = fightProvider.getAll();
        final List<Duel> unties = duelProvider.getUnties();
        final List<Participant> competitors = roleProvider.getAll().stream()
                .filter(role -> role.getRoleType() == RoleType.COMPETITOR).map(Role::getParticipant).toList();
        for (final Participant competitor : competitors) {
            scores.add(new ScoreOfCompetitor(competitor, fights, unties, false));
        }
        sortCompetitorsScores(scoreType, scores);
        return scores;
    }

    public List<ScoreOfCompetitor> getCompetitorsGlobalScoreRanking(Collection<Participant> competitors, ScoreType scoreType) {
        if (competitors == null || competitors.isEmpty()) {
            competitors = participantProvider.getAll();
        }
        final List<ScoreOfCompetitor> scores = new ArrayList<>();
        final List<Fight> fights = fightProvider.get(competitors);
        final List<Duel> unties = duelProvider.getUnties(competitors);
        for (final Participant competitor : competitors) {
            scores.add(new ScoreOfCompetitor(competitor, fights, unties, false));
        }
        sortCompetitorsScores(scoreType, scores);
        return scores;
    }

    public CompetitorRanking getCompetitorRanking(Participant participantDTO) {
        final List<ScoreOfCompetitor> ranking = getCompetitorGlobalRanking(ScoreType.DEFAULT);
        return new CompetitorRanking(IntStream.range(0, ranking.size())
                .filter(i -> Objects.equals(participantDTO, ranking.get(i).getCompetitor()))
                .findFirst().orElse(ranking.size() - 1), ranking.size());
    }

    public ScoreOfCompetitor getScoreRanking(Group group, Participant competitor) {
        final List<ScoreOfCompetitor> scoreRanking = getCompetitorsScoreRanking(group);
        for (final ScoreOfCompetitor score : scoreRanking) {
            if (score.getCompetitor().equals(competitor)) {
                return score;
            }
        }
        return null;
    }

    public Participant getCompetitor(Group group, Integer order) {
        final List<Participant> competitorOrder = getParticipants(group);
        if (order >= 0 && order < competitorOrder.size()) {
            return competitorOrder.get(order);
        }
        return null;
    }

    public List<Participant> getParticipants(Group group) {
        final Set<Participant> competitors = getParticipants(group.getTeams());
        final List<ScoreOfCompetitor> scores = new ArrayList<>();
        for (final Participant competitor : competitors) {
            scores.add(new ScoreOfCompetitor(competitor, group.getFights(), group.getUnties(),
                    countNotOver(group.getTournament())));
        }
        sortCompetitorsScores(group.getTournament().getTournamentScore().getScoreType(), scores);
        final List<Participant> competitorsRanking = new ArrayList<>();
        for (final ScoreOfCompetitor score : scores) {
            competitorsRanking.add(score.getCompetitor());
        }
        return competitorsRanking;
    }

    public ScoreOfCompetitor getScoreOfCompetitor(Group group, Integer order) {
        final List<ScoreOfCompetitor> teamsOrder = getCompetitorsScoreRanking(group);
        if (order >= 0 && order < teamsOrder.size()) {
            return teamsOrder.get(order);
        }
        return null;
    }

    public Integer getOrder(Group group, Team team) {
        final List<Team> ranking = getTeamsRanking(group);

        for (int i = 0; i < ranking.size(); i++) {
            if (ranking.get(i).equals(team)) {
                return i;
            }
        }
        return null;
    }

    public Integer getOrderFromRanking(List<ScoreOfTeam> ranking, Team team) {
        for (int i = 0; i < ranking.size(); i++) {
            if (ranking.get(i).getTeam().equals(team)) {
                return i;
            }
        }
        return null;
    }

    public List<Team> getTeamsRanking(Integer groupId) {
        final Group group = groupProvider.getGroup(groupId);
        if (group == null) {
            throw new GroupNotFoundException(this.getClass(), "Group with id" + groupId + " not found!");
        }
        return getTeamsRanking(group);
    }

    public List<Team> getTeamsRanking(Group group) {
        final List<ScoreOfTeam> scores = getTeamsScoreRanking(group);
        final List<Team> teamRanking = new ArrayList<>();
        for (final ScoreOfTeam score : scores) {
            teamRanking.add(score.getTeam());
        }
        return teamRanking;
    }

    public List<ScoreOfTeam> getTeamsScoreRanking(Group group) {
        if (group == null) {
            return new ArrayList<>();
        }
        return getTeamsScoreRanking(group.getTournament().getTournamentScore().getScoreType(),
                group.getTeams(), group.getFights(), group.getUnties(), checkLevel(group.getTournament()));
    }

    public List<ScoreOfTeam> getTeamsScoreRanking(ScoreType type, List<Team> teams, List<Fight> fights, List<Duel> unties,
                                                  boolean checkLevel) {
        final List<ScoreOfTeam> scores = new ArrayList<>();
        for (final Team team : teams) {
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

    private boolean checkLevel(Tournament tournament) {
        return tournament == null || tournament.getType() != TournamentType.KING_OF_THE_MOUNTAIN;
    }

    /**
     * Return a Hashmap that classify the teams by position (1st, 2nd, 3rd,...)
     *
     * @return classification of the teams
     */
    public Map<Integer, List<Team>> getTeamsByPosition(Group group) {
        final HashMap<Integer, List<Team>> teamsByPosition = new HashMap<>();
        final List<ScoreOfTeam> scores = getTeamsScoreRanking(group);

        Integer position = 0;
        for (int i = 0; i < scores.size(); i++) {
            teamsByPosition.computeIfAbsent(position, k -> new ArrayList<>());
            // Put team in position.
            teamsByPosition.get(position).add(scores.get(i).getTeam());
            // Different score with next team.
            if ((i < scores.size() - 1) && getTeamsSorter(group.getTournament().getTournamentScore().getScoreType(),
                    checkLevel(group.getTournament()))
                    .compare(scores.get(i), scores.get(i + 1)) != 0) {
                position++;
            }
        }

        return teamsByPosition;
    }

    public List<ScoreOfTeam> getTeamsScoreRanking(Tournament tournament) {
        return getTeamsScoreRanking(tournament.getTournamentScore().getScoreType(),
                teamProvider.getAll(tournament),
                fightProvider.getFights(tournament),
                groupProvider.getGroups(tournament).stream()
                        .flatMap(group -> group.getUnties().stream())
                        .toList(),
                checkLevel(tournament));
    }

    public List<Team> getFirstTeamsWithDrawScore(Group group, Integer maxWinners) {
        final Map<Integer, List<Team>> teamsByPosition = getTeamsByPosition(group);
        for (int i = 0; i < maxWinners; i++) {
            final List<Team> teamsInDraw = teamsByPosition.get(i);
            if (teamsInDraw.size() > 1) {
                return teamsInDraw;
            }
        }
        return new ArrayList<>();
    }

}
