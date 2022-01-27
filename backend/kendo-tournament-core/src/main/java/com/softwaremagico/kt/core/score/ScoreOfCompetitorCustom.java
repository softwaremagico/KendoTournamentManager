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
