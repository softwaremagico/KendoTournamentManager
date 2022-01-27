package com.softwaremagico.kt.core.score;


import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.utils.NameUtils;

import java.util.List;

public abstract class ScoreOfCompetitor implements Comparable<ScoreOfCompetitor> {

    private final Participant competitor;
    protected List<Fight> fights;
    private Integer wonDuels = null;
    private Integer drawDuels = null;
    private Integer hits = null;
    private Integer duelsDone = null;
    private Integer fightsWon = null;
    private Integer fightsDraw = null;

    public ScoreOfCompetitor(Participant competitor, List<Fight> fights) {
        this.competitor = competitor;
        this.fights = fights;
    }

    public Participant getCompetitor() {
        return competitor;
    }

    public Integer getDuelsDone() {
        if (duelsDone == null) {
            duelsDone = 0;
            for (Fight fight : fights) {
                duelsDone += fight.getDuels(competitor).size();
            }
        }
        return duelsDone;
    }

    public Integer getDuelsWon() {
        if (wonDuels == null) {
            wonDuels = 0;
            fights.forEach(fight -> wonDuels += fight.getScore(competitor));
        }
        return wonDuels;
    }

    public Integer getFightsWon() {
        if (fightsWon == null) {
            fightsWon = 0;
            for (final Fight fight : fights) {
                if (fight.isWon(competitor)) {
                    fightsWon++;
                }
            }
        }
        return fightsWon;
    }

    public Integer getFightsDraw() {
        if (fightsDraw == null) {
            fightsDraw = 0;
            for (final Fight fight : fights) {
                if (fight.isOver()) {
                    if (fight.getWinner() == null && (fight.getTeam1().isMember(competitor)
                            || fight.getTeam2().isMember(competitor))) {
                        fightsDraw++;
                    }
                }
            }
        }
        return fightsDraw;

    }

    public Integer getDuelsDraw() {
        if (drawDuels == null) {
            drawDuels = 0;
            for (final Fight fight : fights) {
                if (fight.isOver()) {
                    drawDuels += fight.getDrawDuels(competitor);
                }
            }
        }
        return drawDuels;
    }

    public Integer getHits() {
        if (hits == null) {
            hits = 0;
            for (final Fight fight : fights) {
                hits += fight.getScore(competitor);
            }
        }
        return hits;
    }

    @Override
    public String toString() {
        return NameUtils.getLastnameName(competitor) + " D:" + getDuelsWon() + "/" + getDuelsDraw() + ", H:" + getHits();
    }

}
