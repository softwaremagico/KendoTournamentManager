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

/**
 * Aggregated scoring summary for a single competitor in a tournament or group.
 * <p>
 * An instance is computed from all {@link Fight}s that involve the competitor's
 * {@link com.softwaremagico.kt.persistence.entities.Team} and from any optional untie {@link Duel}s. Statistics are lazily
 * calculated the first time each getter is invoked and cached until {@link #update()}
 * is called.
 * </p>
 * <p>
 * Fields annotated with {@link JsonIgnore} are omitted from REST responses (e.g. the
 * raw fight list and untie duels) to keep the payload small. The computed numeric
 * values (won/draw/total fights and duels, hits, etc.) are serialised and returned
 * to clients.
 * </p>
 * <p>
 * Subclasses implement scoring-rule variants:
 * </p>
 * <ul>
 *   <li>{@link ScoreOfCompetitorClassic} — traditional Spanish kendo scoring</li>
 *   <li>{@link ScoreOfCompetitorEuropean} — EKF scoring rules</li>
 *   <li>{@link ScoreOfCompetitorInternational} — IKF scoring rules</li>
 *   <li>{@link ScoreOfCompetitorWinOverDraws} — wins weighted over draws</li>
 *   <li>{@link ScoreOfCompetitorCustom} — fully configurable rule set</li>
 * </ul>
 */
public class ScoreOfCompetitor {

    /**
     * All fights that the competitor's team participated in. Not serialised into JSON.
     */
    @JsonIgnore
    private List<Fight> fights;
    /**
     * The competitor whose score this object represents.
     */
    private Participant competitor;
    /**
     * Extra untie duels used to break ties after regular fights. Not serialised into JSON.
     */
    @JsonIgnore
    private List<Duel> unties;
    /**
     * Number of duels the competitor won outright (2 ippon).
     */
    private Integer wonDuels = null;
    /**
     * Number of duels that ended in a draw (equal ippon).
     */
    private Integer drawDuels = null;
    /**
     * Number of untie duels won by the competitor.
     */
    private Integer untieDuels = null;
    /**
     * Total ippon-equivalent points scored by the competitor across all duels.
     */
    private Integer hits = null;
    /**
     * Total points conceded by the competitor across all duels.
     */
    private Integer hitsLost = null;
    /**
     * Ippon-equivalent points scored in untie duels.
     */
    private Integer untieHits = null;
    /**
     * Total number of duels the competitor participated in.
     */
    private Integer duelsDone = null;
    /**
     * Number of fights in which the competitor's team won.
     */
    private Integer wonFights = null;
    /**
     * Number of fights that ended in a tie for the competitor's team.
     */
    private Integer drawFights = null;
    /**
     * Total number of fights the competitor's team participated in.
     */
    private Integer totalFights = null;
    /**
     * When {@code true}, duels that were not finished (i.e. timed out) are still
     * counted in the statistics. Defaults to {@code false}.
     */
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

    /**
     * Resets all cached computed statistics and recalculates them from the current
     * values of {@link #fights}, {@link #unties} and {@link #competitor}.
     * <p>
     * Must be called after modifying the underlying fight or duel data so that the
     * computed fields stay consistent.
     * </p>
     */
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

    /**
     * Computes and stores the total number of duels participated in by the competitor,
     * counting only duels from finished fights (or all fights if {@link #countNotOver} is {@code true}).
     */
    public void setDuelsDone() {
        duelsDone = 0;
        fights.forEach(fight -> {
            if ((fight != null && fight.isOver()) || (fight != null && countNotOver)) {
                duelsDone += fight.getDuels(competitor).size();
            }
        });
    }

    /**
     * Computes and stores the number of duels won by the competitor across all relevant fights.
     */
    public void setDuelsWon() {
        wonDuels = 0;
        fights.forEach(fight -> {
            if ((fight != null && fight.isOver()) || (fight != null && countNotOver)) {
                wonDuels += fight.getDuelsWon(competitor);
            }
        });
    }

    /**
     * Computes and stores the number of fights whose result was a win for the competitor's team.
     */
    public void setFightsWon() {
        wonFights = 0;
        for (final Fight fight : fights) {
            if (((fight != null && fight.isOver()) || (fight != null && countNotOver)) && fight.isWon(competitor)) {
                wonFights++;
            }
        }
    }

    /**
     * Computes and stores the number of fights that ended in a draw and in which
     * the competitor's team participated.
     */
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

    /**
     * Computes and stores the total number of fights in which the competitor's team participated.
     */
    public void setTotalFights() {
        totalFights = 0;
        for (final Fight fight : fights) {
            if ((fight != null && fight.isOver() && fight.getTeam1().isMember(competitor))
                    || (fight != null && fight.getTeam2().isMember(competitor))) {
                totalFights++;
            }
        }
    }

    /**
     * Computes and stores the number of duels that ended in a draw and in which
     * the competitor participated.
     */
    public void setDuelsDraw() {
        drawDuels = 0;
        for (final Fight fight : fights) {
            if ((fight != null && fight.isOver()) || (fight != null && countNotOver)) {
                drawDuels += fight.getDrawDuels(competitor);
            }
        }
    }

    /**
     * Computes and stores the total ippon points scored by the competitor across all fights.
     */
    public void setHits() {
        hits = 0;
        for (final Fight fight : fights) {
            if (fight != null) {
                hits += fight.getScore(competitor);
            }
        }
    }

    /**
     * Computes and stores the total ippon points conceded by the competitor across all fights.
     */
    public void setHitsLost() {
        hitsLost = 0;
        for (final Fight fight : fights) {
            if (fight != null) {
                hitsLost += fight.getScoreAgainst(competitor);
            }
        }
    }

    /**
     * Computes and stores the number of untie duels won by the competitor.
     */
    public void setUntieDuels() {
        untieDuels = 0;
        unties.forEach(duel -> {
            if (Objects.equals(duel.getCompetitor1(), competitor) && duel.getWinner() == -1
                    || Objects.equals(duel.getCompetitor2(), competitor) && duel.getWinner() == 1) {
                untieDuels++;
            }
        });
    }

    /**
     * Computes and stores the total ippon points scored by the competitor in untie duels.
     */
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
