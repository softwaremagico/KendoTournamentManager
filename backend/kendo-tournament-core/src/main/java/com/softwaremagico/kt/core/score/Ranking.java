package com.softwaremagico.kt.core.score;

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


import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.ScoreType;
import com.softwaremagico.kt.persistence.entities.Team;

import java.util.*;

public class Ranking {

    private final List<Fight> fights;
    private List<Team> teamRanking = null;
    private List<Participant> participants = null;
    private List<ScoreOfTeam> teamScoreRanking = null;
    private List<ScoreOfCompetitor> competitorsScoreRanking = null;

    public Ranking(List<Fight> fights) {
        this.fights = fights;
    }

    public List<Team> getTeamsRanking() {
        if (teamRanking == null) {
            teamRanking = getTeamsRanking(fights);
        }
        return teamRanking;
    }

    public List<ScoreOfTeam> getTeamsScoreRanking() {
        if (teamScoreRanking == null) {
            teamScoreRanking = getTeamsScoreRanking(fights);
        }
        return teamScoreRanking;
    }

    /**
     * Return a Hashmap that classify the teams by position (1st, 2nd, 3rd,...)
     *
     * @return classification of the teams
     */
    private HashMap<Integer, List<Team>> getTeamsByPosition() {
        final HashMap<Integer, List<Team>> teamsByPosition = new HashMap<>();
        final List<ScoreOfTeam> scores = getTeamsScoreRanking();

        Integer position = 0;
        for (int i = 0; i < scores.size(); i++) {
            teamsByPosition.computeIfAbsent(position, k -> new ArrayList<>());
            // Put team in position.
            teamsByPosition.get(position).add(scores.get(i).getTeam());
            // Different score with next team.
            if ((i < scores.size() - 1) && scores.get(i).compareTo(scores.get(i + 1)) != 0) {
                position++;
            }
        }

        return teamsByPosition;
    }

    public List<Team> getFirstTeamsWithDrawScore(Integer maxWinners) {
        final HashMap<Integer, List<Team>> teamsByPosition = getTeamsByPosition();
        for (int i = 0; i < maxWinners; i++) {
            final List<Team> teamsInDraw = teamsByPosition.get(i);
            if (teamsInDraw.size() > 1) {
                return teamsInDraw;
            }
        }
        return null;
    }

    public Team getTeam(Integer order) {
        final List<Team> teamsOrder = getTeamsRanking();
        if (order >= 0 && order < teamsOrder.size()) {
            return teamsOrder.get(order);
        }
        return null;
    }

    public ScoreOfTeam getScoreOfTeam(Integer order) {
        final List<ScoreOfTeam> teamsOrder = getTeamsScoreRanking();
        if (order >= 0 && order < teamsOrder.size()) {
            return teamsOrder.get(order);
        }
        return null;
    }

    public List<Participant> getParticipants() {
        if (participants == null) {
            participants = getCompetitorsRanking(fights);
        }
        return participants;
    }

    public List<ScoreOfCompetitor> getCompetitorsScoreRanking() {
        if (competitorsScoreRanking == null) {
            competitorsScoreRanking = getCompetitorsScoreRanking(fights);
        }
        return competitorsScoreRanking;
    }

    public ScoreOfCompetitor getScoreRanking(Participant competitor) {
        final List<ScoreOfCompetitor> scoreRanking = getCompetitorsScoreRanking();
        for (final ScoreOfCompetitor score : scoreRanking) {
            if (score.getCompetitor().equals(competitor)) {
                return score;
            }
        }
        return null;
    }

    public Participant getCompetitor(Integer order) {
        final List<Participant> competitorOrder = getParticipants();
        if (order >= 0 && order < competitorOrder.size()) {
            return competitorOrder.get(order);
        }
        return null;
    }

    public ScoreOfCompetitor getScoreOfCompetitor(Integer order) {
        final List<ScoreOfCompetitor> teamsOrder = getCompetitorsScoreRanking();
        if (order >= 0 && order < teamsOrder.size()) {
            return teamsOrder.get(order);
        }
        return null;
    }

    private static List<Team> getTeams(List<Fight> fights) {
        final List<Team> teamsOfFights = new ArrayList<>();
        for (final Fight fight : fights) {
            if (!teamsOfFights.contains(fight.getTeam1())) {
                teamsOfFights.add(fight.getTeam1());
            }
            if (!teamsOfFights.contains(fight.getTeam2())) {
                teamsOfFights.add(fight.getTeam2());
            }
        }
        return teamsOfFights;
    }

    private static Set<Participant> getParticipants(List<Fight> fights) {
        final Set<Participant> allCompetitors = new HashSet<>();
        for (final Fight fight : fights) {
            allCompetitors.addAll(fight.getTeam1().getMembers());
            allCompetitors.addAll(fight.getTeam2().getMembers());
        }
        return allCompetitors;
    }

    public static List<Team> getTeamsRanking(List<Fight> fights) {
        final List<ScoreOfTeam> scores = getTeamsScoreRanking(fights);
        final List<Team> teamRanking = new ArrayList<>();
        for (final ScoreOfTeam score : scores) {
            teamRanking.add(score.getTeam());
        }
        return teamRanking;
    }

    public static List<ScoreOfTeam> getTeamsScoreRanking(List<Fight> fights) {
        final List<Team> teamsOfFights = getTeams(fights);
        final List<ScoreOfTeam> scores = new ArrayList<>();
        for (final Team team : teamsOfFights) {
            scores.add(ScoreOfTeam.getScoreOfTeam(team, fights));
        }
        Collections.sort(scores);

        return scores;
    }

    /**
     * Gets the more restrictive score for obtaining the ranking.
     *
     * @param competitor
     * @param fights
     * @return
     */
    private static ScoreOfCompetitor getScoreOfCompetitor(Participant competitor, List<Fight> fights) {
        // If one fight is classic, use classic score.
        for (final Fight fight : fights) {
            if (fight.getTournament().getTournamentScore().getScoreType().equals(ScoreType.CLASSIC)) {
                return new ScoreOfCompetitorClassic(competitor, fights);
            }
        }

        // If one fight is european, use european score
        for (final Fight fight : fights) {
            if (fight.getTournament().getTournamentScore().getScoreType().equals(ScoreType.EUROPEAN)) {
                return new ScoreOfCompetitorEuropean(competitor, fights);
            }
        }

        // If one fight is european, use european score
        for (final Fight fight : fights) {
            if (fight.getTournament().getTournamentScore().getScoreType().equals(ScoreType.INTERNATIONAL)) {
                return new ScoreOfCompetitorInternational(competitor, fights);
            }
        }

        // If one fight is winOverDraw, use winOverDraw score
        for (final Fight fight : fights) {
            if (fight.getTournament().getTournamentScore().getScoreType().equals(ScoreType.WIN_OVER_DRAWS)) {
                return new ScoreOfCompetitorWinOverDraws(competitor, fights);
            }
        }

        return new ScoreOfCompetitorCustom(competitor, fights);
    }

    public static ScoreOfCompetitor getScoreRanking(Participant competitor, List<Fight> fights) {
        return getScoreOfCompetitor(competitor, fights);
    }

    public static List<ScoreOfCompetitor> getCompetitorsScoreRanking(List<Fight> fights) {
        final Set<Participant> competitors = getParticipants(fights);
        final List<ScoreOfCompetitor> scores = new ArrayList<>();
        for (final Participant competitor : competitors) {
            scores.add(getScoreOfCompetitor(competitor, fights));
        }
        Collections.sort(scores);
        return scores;
    }

    public static Integer getOrder(List<Fight> fights, Team team) {
        final List<Team> ranking = getTeamsRanking(fights);

        for (int i = 0; i < ranking.size(); i++) {
            if (ranking.get(i).equals(team)) {
                return i;
            }
        }
        return null;
    }

    public static Integer getOrderFromRanking(List<ScoreOfTeam> ranking, Team team) {
        for (int i = 0; i < ranking.size(); i++) {
            if (ranking.get(i).getTeam().equals(team)) {
                return i;
            }
        }
        return null;
    }

    public static Team getTeam(List<Fight> fights, Integer order) {
        final List<Team> ranking = getTeamsRanking(fights);
        if (order < ranking.size() && order >= 0) {
            return ranking.get(order);
        }
        return null;
    }

    public static List<Participant> getCompetitorsRanking(List<Fight> fights) {
        final Set<Participant> competitors = getParticipants(fights);
        final List<ScoreOfCompetitor> scores = new ArrayList<>();
        for (final Participant competitor : competitors) {
            scores.add(getScoreOfCompetitor(competitor, fights));
        }
        Collections.sort(scores);
        final List<Participant> competitorsRanking = new ArrayList<>();
        for (final ScoreOfCompetitor score : scores) {
            competitorsRanking.add(score.getCompetitor());
        }
        return competitorsRanking;
    }

    @Override
    public String toString() {
        return getTeamsRanking().toString();
    }
}
