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

public class ScoreOfCompetitorEuropean implements Comparator<ScoreOfCompetitor> {

    @Override
    public int compare(ScoreOfCompetitor scoreOfCompetitor1, ScoreOfCompetitor scoreOfCompetitor2) {
        if (!Objects.equals(scoreOfCompetitor1.getWonDuels(), scoreOfCompetitor2.getWonDuels())) {
            return scoreOfCompetitor2.getWonDuels().compareTo(scoreOfCompetitor1.getWonDuels());
        }

        if (!Objects.equals(scoreOfCompetitor1.getDrawDuels(), scoreOfCompetitor2.getDrawDuels())) {
            return scoreOfCompetitor2.getDrawDuels().compareTo(scoreOfCompetitor1.getDrawDuels());
        }

        if (!Objects.equals(scoreOfCompetitor1.getHits(), scoreOfCompetitor2.getHits())) {
            return scoreOfCompetitor2.getHits().compareTo(scoreOfCompetitor1.getHits());
        }

        // More duels done with same score is negative.
        if (!Objects.equals(scoreOfCompetitor1.getDuelsDone(), scoreOfCompetitor2.getDuelsDone())) {
            return scoreOfCompetitor1.getDuelsDone().compareTo(scoreOfCompetitor2.getDuelsDone());
        }

        // Draw score, order by name;
        return NameUtils.getLastnameName(scoreOfCompetitor1.getCompetitor()).compareTo(NameUtils.getLastnameName(scoreOfCompetitor2.getCompetitor()));
    }

}
