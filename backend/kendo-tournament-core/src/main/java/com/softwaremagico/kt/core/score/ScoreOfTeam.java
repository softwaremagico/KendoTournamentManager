package com.softwaremagico.kt.core.score;

/*-
 * #%L
 * Kendo TournamentDTO Manager (Core)
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


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.softwaremagico.kt.core.controller.models.DuelDTO;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;

import java.util.List;
import java.util.Objects;

public class ScoreOfTeam {

    private TeamDTO team;
    @JsonIgnore
    private List<FightDTO> fights;
    @JsonIgnore
    private List<DuelDTO> unties;
    private Integer wonFights = null;
    private Integer drawFights = null;

    private Integer fightsDone = null;
    private Integer wonDuels = null;
    private Integer drawDuels = null;
    private Integer goldenPoints = null;
    private Integer hits = null;
    private Integer level = null;

    public ScoreOfTeam() {

    }

    public ScoreOfTeam(TeamDTO team, List<FightDTO> fights, List<DuelDTO> unties) {
        this.team = team;
        this.fights = fights;
        this.unties = unties;
        update();
    }

    public TeamDTO getTeam() {
        return team;
    }

    public TournamentDTO getTournament() {
        if (team != null) {
            return team.getTournament();
        }
        return null;
    }

    public void update() {
        wonFights = null;
        drawFights = null;
        wonDuels = null;
        drawDuels = null;
        goldenPoints = null;
        hits = null;
        level = null;
        fightsDone = null;
        setLevel();
        setWonDuels();
        setDrawDuels();
        setWonFights();
        setDrawFights();
        setFightsDone();
        setGoldenPoints();
        setHits();
    }

    public void setLevel() {
        level = fights.stream().filter(fightDTO -> fightDTO.getTeam1().equals(team) || fightDTO.getTeam2().equals(team))
                .map(FightDTO::getLevel).max(Integer::compareTo).orElse(0);
    }

    public void setWonFights() {
        wonFights = 0;
        for (final FightDTO fight : fights) {
            final TeamDTO winner = fight.getWinner();
            if (winner != null && winner.equals(team)) {
                wonFights++;
            }
        }
    }

    public void setDrawFights() {
        drawFights = 0;
        fights.forEach(fight -> {
            if ((Objects.equals(fight.getTeam1(), team) || Objects.equals(fight.getTeam2(), team))) {
                if (fight.isOver() && fight.isDrawFight()) {
                    drawFights++;
                }
            }
        });
    }

    public void setFightsDone() {
        fightsDone = 0;
        fights.forEach(fight -> {
            if ((Objects.equals(fight.getTeam1(), team) || Objects.equals(fight.getTeam2(), team))) {
                drawFights++;
            }
        });
    }

    public void setWonDuels() {
        wonDuels = 0;
        fights.forEach(fight -> wonDuels += fight.getWonDuels(team));
    }

    public void setDrawDuels() {
        drawDuels = 0;
        fights.forEach(fight -> drawDuels += fight.getDrawDuels(team));
    }

    public void setHits() {
        hits = 0;
        fights.forEach(fight -> hits += fight.getScore(team));
    }

    public void setGoldenPoints() {
        goldenPoints = 0;
        unties.forEach(duel -> {
            if ((team.getMembers().contains(duel.getCompetitor1())) && duel.getWinner() == -1) {
                goldenPoints++;
            } else if ((team.getMembers().contains(duel.getCompetitor2())) && duel.getWinner() == 1) {
                goldenPoints++;
            }
        });
    }

    public List<FightDTO> getFights() {
        return fights;
    }

    public List<DuelDTO> getUnties() {
        return unties;
    }

    public Integer getWonFights() {
        return wonFights;
    }

    public Integer getDrawFights() {
        return drawFights;
    }

    public Integer getFightsDone() {
        return fightsDone;
    }

    public Integer getWonDuels() {
        return wonDuels;
    }

    public Integer getDrawDuels() {
        return drawDuels;
    }

    public Integer getGoldenPoints() {
        return goldenPoints;
    }

    public Integer getHits() {
        return hits;
    }

    public Integer getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return team.getName() + ": Fights:" + getWonFights() + "/" + getDrawFights() + ", Duels: "
                + getWonDuels() + "/" + getDrawDuels() + ", hits:" + getHits() + "*".repeat(Math.max(0, getGoldenPoints())) + "\n";
    }
}
