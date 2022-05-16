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


import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.utils.NameUtils;

import java.util.List;

public class ScoreOfCompetitorEuropean extends ScoreOfCompetitor {

    public ScoreOfCompetitorEuropean(ParticipantDTO competitor, List<FightDTO> fights) {
        super(competitor, fights);
    }

    @Override
    public int compareTo(ScoreOfCompetitor o) {
        if (getDuelsWon() > o.getDuelsWon()) {
            return -1;
        }
        if (getDuelsWon() < o.getDuelsWon()) {
            return 1;
        }

        if (getDuelsDraw() > o.getDuelsDraw()) {
            return -1;
        }
        if (getDuelsDraw() < o.getDuelsDraw()) {
            return 1;
        }

        if (getHits() > o.getHits()) {
            return -1;
        }
        if (getHits() < o.getHits()) {
            return 1;
        }

        // More duels done with same score is negative.
        if (getDuelsDone() > o.getDuelsDone()) {
            return 1;
        }

        if (getDuelsDone() < o.getDuelsDone()) {
            return -1;
        }

        // Draw score, order by name;
        return NameUtils.getLastnameName(getCompetitor()).compareTo(NameUtils.getLastnameName(o.getCompetitor()));
    }

}
