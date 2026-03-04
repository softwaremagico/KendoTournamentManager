package com.softwaremagico.kt.core.score;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2026 Softwaremagico
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
import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.utils.NameUtils;

import java.util.List;
import java.util.Objects;

public class ScoreOfCompetitor {

    @JsonIgnore
    private List<Fight> fights;
    private Participant competitor;
    @JsonIgnore
    private List<Duel> unties;
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

    public ScoreOfCompetitor() {

    }

    public ScoreOfCompetitor(Participant competitor, List<Fight> fights, List<Duel> unties, boolean countNotOver) {
        this.competitor = competitor;
        this.fights = fights;
        this.unties = unties;
        this.countNotOver = countNotOver;
        update();
    }

    public List<Fight> getFights() {
        return fights;
    }

    public void setFights(List<Fight> fights) {
        this.fights = fights;
    }

    public List<Duel> getUnties() {
        return unties;
    }

    public void setUnties(List<Duel> unties) {
        this.unties = unties;
    }

    public void update() {
        wonFights = null;
        drawFights = null;
        wonDuels = null;
        drawDuels = null;
        hits = null;
        hitsLost = null;
        totalFights = null;
        setDuelsWon();
        setDuelsDraw();
        setDuelsDone();
        setFightsWon();
        setFightsDraw();
        setUntieDuels();
        setUntieHits();
        setHits();
        setHitsLost();
        setTotalFights();
    }

    public Participant getCompetitor() {
        return competitor;
    }

    public void setCompetitor(Participant competitor) {
        this.competitor = competitor;
    }

    public void setDuelsDone() {
        duelsDone = 0;
        fights.forEach(fight -> {
            if ((fight != null && fight.isOver()) || (fight != null && countNotOver)) {
                duelsDone += fight.getDuels(competitor).size();
            }
        });
    }

    public void setDuelsWon() {
        wonDuels = 0;
        fights.forEach(fight -> {
            if ((fight != null && fight.isOver()) || (fight != null && countNotOver)) {
                wonDuels += fight.getDuelsWon(competitor);
            }
        });
    }

    public void setFightsWon() {
        wonFights = 0;
        for (final Fight fight : fights) {
            if (((fight != null && fight.isOver()) || (fight != null && countNotOver)) && fight.isWon(competitor)) {
                wonFights++;
            }
        }
    }

    public void setFightsDraw() {
        drawFights = 0;
        for (final Fight fight : fights) {
            if (((fight != null && fight.isOver()) || (fight != null && countNotOver))
                    && (fight.getWinner() == null && (fight.getTeam1().isMember(competitor)
                    || fight.getTeam2().isMember(competitor)))) {
                drawFights++;
            }
        }
    }

    public void setTotalFights() {
        totalFights = 0;
        for (final Fight fight : fights) {
            if ((fight != null && fight.isOver() && fight.getTeam1().isMember(competitor))
                    || (fight != null && fight.getTeam2().isMember(competitor))) {
                totalFights++;
            }
        }
    }

    public void setDuelsDraw() {
        drawDuels = 0;
        for (final Fight fight : fights) {
            if ((fight != null && fight.isOver()) || (fight != null && countNotOver)) {
                drawDuels += fight.getDrawDuels(competitor);
            }
        }
    }

    public void setHits() {
        hits = 0;
        for (final Fight fight : fights) {
            if (fight != null) {
                hits += fight.getScore(competitor);
            }
        }
    }

    public void setHitsLost() {
        hitsLost = 0;
        for (final Fight fight : fights) {
            if (fight != null) {
                hitsLost += fight.getScoreAgainst(competitor);
            }
        }
    }

    public void setUntieDuels() {
        untieDuels = 0;
        unties.forEach(duel -> {
            if (Objects.equals(duel.getCompetitor1(), competitor) && duel.getWinner() == -1
                    || Objects.equals(duel.getCompetitor2(), competitor) && duel.getWinner() == 1) {
                untieDuels++;
            }
        });
    }

    public void setUntieHits() {
        untieHits = 0;
        unties.forEach(duel -> {
            if (Objects.equals(duel.getCompetitor1(), competitor)) {
                untieHits += duel.getCompetitor1ScoreValue();
            } else if (Objects.equals(duel.getCompetitor2(), competitor)) {
                untieHits += duel.getCompetitor2ScoreValue();
            }
        });
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

    public Integer getUntieDuels() {
        return untieDuels;
    }

    public void setUntieDuels(Integer untieDuels) {
        this.untieDuels = untieDuels;
    }

    public Integer getUntieHits() {
        return untieHits;
    }

    public void setUntieHits(Integer untieHits) {
        this.untieHits = untieHits;
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
        return "{" + NameUtils.getLastnameName(competitor) + " D:" + getWonDuels() + "/"
                + getDrawDuels() + ", H:" + getHits() + ", HL:" + getHitsLost() + "}";
    }

}
