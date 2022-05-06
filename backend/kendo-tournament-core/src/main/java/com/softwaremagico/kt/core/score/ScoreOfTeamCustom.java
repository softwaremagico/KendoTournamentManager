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

import com.softwaremagico.kt.core.controller.models.DuelDTO;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;

import java.util.List;

public class ScoreOfTeamCustom extends ScoreOfTeam {

    public ScoreOfTeamCustom(TeamDTO team, List<FightDTO> fights, List<DuelDTO> unties) {
        super(team, fights, unties);
    }

    @Override
    public int compareTo(ScoreOfTeam o) {
        if (getWonFights() * getTeam().getTournament().getTournamentScore().getPointsByVictory() + getDrawFights() *
                getTeam().getTournament().getTournamentScore().getPointsByDraw()
                > o.getWonFights() * getTeam().getTournament().getTournamentScore().getPointsByVictory() + o.getDrawFights() *
                getTeam().getTournament().getTournamentScore().getPointsByDraw()) {
            return -1;
        }

        if (getWonFights() * getTeam().getTournament().getTournamentScore().getPointsByVictory() + getDrawFights() *
                getTeam().getTournament().getTournamentScore().getPointsByDraw()
                < o.getWonFights() * getTeam().getTournament().getTournamentScore().getPointsByVictory() + o.getDrawFights() *
                getTeam().getTournament().getTournamentScore().getPointsByDraw()) {
            return 1;
        }

        if (getWonDuels() * getTeam().getTournament().getTournamentScore().getPointsByVictory() + getDrawDuels() *
                getTeam().getTournament().getTournamentScore().getPointsByDraw()
                > o.getWonDuels() * getTeam().getTournament().getTournamentScore().getPointsByVictory() + o.getDrawDuels() *
                getTeam().getTournament().getTournamentScore().getPointsByDraw()) {
            return -1;
        }

        if (getWonDuels() * getTeam().getTournament().getTournamentScore().getPointsByVictory() + getDrawDuels() *
                getTeam().getTournament().getTournamentScore().getPointsByDraw()
                < o.getWonDuels() * getTeam().getTournament().getTournamentScore().getPointsByVictory() + o.getDrawDuels() *
                getTeam().getTournament().getTournamentScore().getPointsByDraw()) {
            return 1;
        }

        if (getHits() > o.getHits()) {
            return -1;
        }

        if (getHits() < o.getHits()) {
            return 1;
        }

        return o.getGoldenPoints().compareTo(getGoldenPoints());

    }
}
