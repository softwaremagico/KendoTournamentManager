package com.softwaremagico.kt.core.score;


import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.utils.NameUtils;

import java.util.List;

public class ScoreOfCompetitorCustom extends ScoreOfCompetitor {

    public ScoreOfCompetitorCustom(Participant competitor, List<Fight> fights) {
        super(competitor, fights);
    }

    @Override
    public int compareTo(ScoreOfCompetitor scoreOfCompetitor) {
        if (fights.size() > 0) {
            if (getDuelsWon() * fights.get(0).getTournament().getTournamentScore().getPointsByVictory() + getDuelsDraw()
                    * fights.get(0).getTournament().getTournamentScore().getPointsByDraw() > scoreOfCompetitor.getDuelsWon()
                    * fights.get(0).getTournament().getTournamentScore().getPointsByVictory() + scoreOfCompetitor.getDuelsDraw()
                    * fights.get(0).getTournament().getTournamentScore().getPointsByDraw()) {
                return -1;
            }

            if (getDuelsWon() * fights.get(0).getTournament().getTournamentScore().getPointsByVictory() + getDuelsDraw()
                    * fights.get(0).getTournament().getTournamentScore().getPointsByDraw() < scoreOfCompetitor.getDuelsWon()
                    * fights.get(0).getTournament().getTournamentScore().getPointsByVictory() + scoreOfCompetitor.getDuelsDraw()
                    * fights.get(0).getTournament().getTournamentScore().getPointsByDraw()) {
                return 1;
            }

            if (getHits() > scoreOfCompetitor.getHits()) {
                return -1;
            }
            if (getHits() < scoreOfCompetitor.getHits()) {
                return 1;
            }

            // More duels done with same scoreOfCompetitor is negative.
            if (getDuelsDone() > scoreOfCompetitor.getDuelsDone()) {
                return 1;
            }

            if (getDuelsDone() < scoreOfCompetitor.getDuelsDone()) {
                return -1;
            }
        }

        // Draw scoreOfCompetitor, order by name;
        return NameUtils.getLastnameName(getCompetitor()).compareTo(NameUtils.getLastnameName(scoreOfCompetitor.getCompetitor()));
    }
}
