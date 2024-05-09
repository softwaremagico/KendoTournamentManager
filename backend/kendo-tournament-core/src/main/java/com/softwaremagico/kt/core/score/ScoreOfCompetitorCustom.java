package com.softwaremagico.kt.core.score;

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


import com.softwaremagico.kt.utils.NameUtils;

import java.util.Comparator;
import java.util.Objects;

public class ScoreOfCompetitorCustom implements Comparator<ScoreOfCompetitor> {

    @Override
    public int compare(ScoreOfCompetitor scoreOfCompetitor1, ScoreOfCompetitor scoreOfCompetitor2) {
        if (!scoreOfCompetitor1.getFights().isEmpty()) {
            if (scoreOfCompetitor1.getWonDuels() * scoreOfCompetitor1.getFights().get(0).getTournament().getTournamentScore().getPointsByVictory()
                    + scoreOfCompetitor1.getDrawDuels()
                    * scoreOfCompetitor1.getFights().get(0).getTournament().getTournamentScore().getPointsByDraw() > scoreOfCompetitor2.getWonDuels()
                    * scoreOfCompetitor1.getFights().get(0).getTournament().getTournamentScore().getPointsByVictory() + scoreOfCompetitor2.getDrawDuels()
                    * scoreOfCompetitor1.getFights().get(0).getTournament().getTournamentScore().getPointsByDraw()) {
                return -1;
            }

            if (scoreOfCompetitor1.getWonDuels() * scoreOfCompetitor1.getFights().get(0).getTournament().getTournamentScore().getPointsByVictory()
                    + scoreOfCompetitor1.getDrawDuels()
                    * scoreOfCompetitor1.getFights().get(0).getTournament().getTournamentScore().getPointsByDraw() < scoreOfCompetitor2.getWonDuels()
                    * scoreOfCompetitor1.getFights().get(0).getTournament().getTournamentScore().getPointsByVictory() + scoreOfCompetitor2.getDrawDuels()
                    * scoreOfCompetitor1.getFights().get(0).getTournament().getTournamentScore().getPointsByDraw()) {
                return 1;
            }

            if (!Objects.equals(scoreOfCompetitor1.getHits(), scoreOfCompetitor2.getHits())) {
                return scoreOfCompetitor2.getHits().compareTo(scoreOfCompetitor1.getHits());
            }

            // More duels done with same score is negative.
            if (!Objects.equals(scoreOfCompetitor1.getDuelsDone(), scoreOfCompetitor2.getDuelsDone())) {
                return scoreOfCompetitor2.getDuelsDone().compareTo(scoreOfCompetitor1.getDuelsDone());
            }
        }

        // Draw scoreOfCompetitor, order by name;
        return NameUtils.getLastnameName(scoreOfCompetitor1.getCompetitor()).compareTo(NameUtils.getLastnameName(scoreOfCompetitor2.getCompetitor()));
    }
}
