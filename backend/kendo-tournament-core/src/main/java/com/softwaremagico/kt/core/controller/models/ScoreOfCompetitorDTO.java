package com.softwaremagico.kt.core.controller.models;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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
import com.softwaremagico.kt.utils.NameUtils;
import jakarta.validation.constraints.NotNull;

public class ScoreOfCompetitorDTO {

    @NotNull
    private ParticipantDTO competitor;
    private Integer wonDuels = null;
    private Integer drawDuels = null;
    private Integer untieDuels = null;
    private Integer hits = null;
    private Integer hitsLost = null;
    private Integer untieHits = null;
    private Integer duelsDone = null;
    private Integer wonFights = null;
    private Integer drawFights = null;
    private Integer totalFights = null;
    @JsonIgnore
    private boolean countNotOver = false;

    public ScoreOfCompetitorDTO() {

    }

    public ScoreOfCompetitorDTO(ParticipantDTO competitor, boolean countNotOver) {
        this.competitor = competitor;
        this.countNotOver = countNotOver;
    }


    public ParticipantDTO getCompetitor() {
        return competitor;
    }

    public void setCompetitor(ParticipantDTO competitor) {
        this.competitor = competitor;
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

    public Integer getHitsLost() {
        return hitsLost;
    }

    public void setHitsLost(Integer hitsLost) {
        this.hitsLost = hitsLost;
    }

    public Integer getUntieHits() {
        return untieHits;
    }

    public void setUntieHits(Integer untieHits) {
        this.untieHits = untieHits;
    }

    public Integer getDuelsDone() {
        return duelsDone;
    }

    public void setDuelsDone(Integer duelsDone) {
        this.duelsDone = duelsDone;
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

    public boolean isCountNotOver() {
        return countNotOver;
    }

    public void setCountNotOver(boolean countNotOver) {
        this.countNotOver = countNotOver;
    }

    public Integer getTotalFights() {
        return totalFights;
    }

    public void setTotalFights(Integer totalFights) {
        this.totalFights = totalFights;
    }

    @Override
    public String toString() {
        return "{" + NameUtils.getLastnameName(competitor) + " D:" + getWonDuels() + "/" + getDrawDuels() + ", H:" + getHits() + "}";
    }

}
