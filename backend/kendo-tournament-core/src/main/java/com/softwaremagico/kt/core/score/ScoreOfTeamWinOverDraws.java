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


import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Team;

import java.util.List;

public class ScoreOfTeamWinOverDraws extends ScoreOfTeam {

    public ScoreOfTeamWinOverDraws(Team team, List<Fight> fights, List<Duel> unties) {
        super(team, fights, unties);
    }

    @Override
    public int compareTo(ScoreOfTeam o) {
        if (getWonFights() > o.getWonFights()) {
            return -1;
        }

        if (getWonFights() < o.getWonFights()) {
            return 1;
        }

        if (getWonDuels() > o.getWonDuels()) {
            return -1;
        }

        if (getWonDuels() < o.getWonDuels()) {
            return 1;
        }

        if (getHits() > o.getHits()) {
            return -1;
        }

        if (getHits() < o.getHits()) {
            return 1;
        }

        if (getDrawFights() > o.getDrawFights()) {
            return -1;
        }

        if (getDrawFights() < o.getDrawFights()) {
            return 1;
        }

        if (getDrawDuels() > o.getDrawDuels()) {
            return -1;
        }

        if (getDrawDuels() < o.getDrawDuels()) {
            return 1;
        }

        return o.getGoldenPoints().compareTo(getGoldenPoints());

    }
}
