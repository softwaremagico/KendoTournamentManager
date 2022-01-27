package com.softwaremagico.kt.core.score;

import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Team;

import java.util.List;

public class ScoreOfTeamCustom extends ScoreOfTeam {

    public ScoreOfTeamCustom(Team team, List<Fight> fights) {
        super(team, fights);
    }

    @Override
    public int compareTo(ScoreOfTeam o) {
        if (getWonFights() * getTeam().getTournament().getTournamentScore().getPointsByVictory() + getDrawFights() * getTeam().getTournament().getTournamentScore().getPointsByDraw()
                > o.getWonFights() * getTeam().getTournament().getTournamentScore().getPointsByVictory() + o.getDrawFights() * getTeam().getTournament().getTournamentScore().getPointsByDraw()) {
            return -1;
        }

        if (getWonFights() * getTeam().getTournament().getTournamentScore().getPointsByVictory() + getDrawFights() * getTeam().getTournament().getTournamentScore().getPointsByDraw()
                < o.getWonFights() * getTeam().getTournament().getTournamentScore().getPointsByVictory() + o.getDrawFights() * getTeam().getTournament().getTournamentScore().getPointsByDraw()) {
            return 1;
        }

        if (getWonDuels() * getTeam().getTournament().getTournamentScore().getPointsByVictory() + getDrawDuels() * getTeam().getTournament().getTournamentScore().getPointsByDraw()
                > o.getWonDuels() * getTeam().getTournament().getTournamentScore().getPointsByVictory() + o.getDrawDuels() * getTeam().getTournament().getTournamentScore().getPointsByDraw()) {
            return -1;
        }

        if (getWonDuels() * getTeam().getTournament().getTournamentScore().getPointsByVictory() + getDrawDuels() * getTeam().getTournament().getTournamentScore().getPointsByDraw()
                < o.getWonDuels() * getTeam().getTournament().getTournamentScore().getPointsByVictory() + o.getDrawDuels() * getTeam().getTournament().getTournamentScore().getPointsByDraw()) {
            return 1;
        }

        if (getHits() > o.getHits()) {
            return -1;
        }

        if (getHits() < o.getHits()) {
            return 1;
        }

        if (getGoldenPoints() > o.getGoldenPoints()) {
            return -1;
        }

        if (getGoldenPoints() < o.getGoldenPoints()) {
            return 1;
        }

        return 0;
    }
}
