package com.softwaremagico.kt.core.controller.models;

/*-
 * #%L
 * Kendo TournamentDTO Manager (Core)
 * %%
 * Copyright (C) 2021 - 2025 Softwaremagico
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


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Objects;

public class ScoreOfTeamDTO extends ElementDTO {

    @NotNull
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
    private Integer untieDuels = null;
    private Integer hits = null;
    private Integer level = null;
    private Integer sortingIndex = null;

    public ScoreOfTeamDTO() {

    }

    public ScoreOfTeamDTO(TeamDTO team, List<FightDTO> fights, List<DuelDTO> unties) {
        this.team = team;
        this.fights = fights;
        this.unties = unties;
        update();
    }

    public TeamDTO getTeam() {
        return team;
    }

    public void setTeam(TeamDTO team) {
        this.team = team;
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
        untieDuels = null;
        hits = null;
        level = null;
        fightsDone = null;
        setLevel();
        setWonDuels();
        setDrawDuels();
        setWonFights();
        setDrawFights();
        setFightsDone();
        setUntieDuels();
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
        drawFights = (int) fights.stream().filter(fight -> (Objects.equals(fight.getTeam1(), team) || Objects.equals(fight.getTeam2(), team))
                && (fight.isOver() && fight.isDrawFight())).count();
    }

    public void setFightsDone() {
        fightsDone = (int) fights.stream().filter(fight -> (Objects.equals(fight.getTeam1(), team) || Objects.equals(fight.getTeam2(), team))).count();
    }

    public void setWonDuels() {
        wonDuels = fights.stream().mapToInt(fight -> fight.getWonDuels(team)).sum();
    }

    public void setDrawDuels() {
        drawDuels = fights.stream().mapToInt(fight -> fight.getDrawDuels(team)).sum();
    }

    public void setHits() {
        hits = fights.stream().mapToInt(fight -> fight.getScore(team)).sum();
    }

    public void setUntieDuels() {
        untieDuels = 0;
        unties.forEach(duel -> {
            if (((team.getMembers().contains(duel.getCompetitor1())) && duel.getWinner() == -1)
                    || ((team.getMembers().contains(duel.getCompetitor2())) && duel.getWinner() == 1)) {
                untieDuels++;
            }
        });
    }

    public List<FightDTO> getFights() {
        return fights;
    }

    public void setFights(List<FightDTO> fights) {
        this.fights = fights;
    }

    public List<DuelDTO> getUnties() {
        return unties;
    }

    public void setUnties(List<DuelDTO> unties) {
        this.unties = unties;
    }

    public Integer getWonFights() {
        return wonFights;
    }

    public void setWonFights(Integer wonFights) {
        this.wonFights = wonFights;
    }

    public Integer getDrawFights() {
        return drawFights;
    }

    public void setDrawFights(Integer drawFights) {
        this.drawFights = drawFights;
    }

    public Integer getFightsDone() {
        return fightsDone;
    }

    public void setFightsDone(Integer fightsDone) {
        this.fightsDone = fightsDone;
    }

    public Integer getWonDuels() {
        return wonDuels;
    }

    public void setWonDuels(Integer wonDuels) {
        this.wonDuels = wonDuels;
    }

    public Integer getDrawDuels() {
        return drawDuels;
    }

    public void setDrawDuels(Integer drawDuels) {
        this.drawDuels = drawDuels;
    }

    public Integer getUntieDuels() {
        return untieDuels;
    }

    public void setUntieDuels(Integer untieDuels) {
        this.untieDuels = untieDuels;
    }

    public Integer getHits() {
        return hits;
    }

    public void setHits(Integer hits) {
        this.hits = hits;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getSortingIndex() {
        return sortingIndex;
    }

    public void setSortingIndex(Integer sortingIndex) {
        this.sortingIndex = sortingIndex;
    }

    @Override
    public String toString() {
        return "{" + team.getName() + ": Fights:" + getWonFights() + "/" + getDrawFights() + ", Duels: "
                + getWonDuels() + "/" + getDrawDuels() + ", hits:" + getHits() + "*".repeat(Math.max(0, getUntieDuels())) + "}";
    }
}
