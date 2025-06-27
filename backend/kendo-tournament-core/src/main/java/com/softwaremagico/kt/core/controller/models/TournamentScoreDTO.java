package com.softwaremagico.kt.core.controller.models;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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

import com.softwaremagico.kt.persistence.values.ScoreType;

import java.util.Objects;

public class TournamentScoreDTO extends ElementDTO {

    private ScoreType scoreType;

    private int pointsByVictory = 1;

    private int pointsByDraw = 0;

    public TournamentScoreDTO() {
        setScoreType(ScoreType.CLASSIC);
    }

    public ScoreType getScoreType() {
        return scoreType;
    }

    public void setScoreType(ScoreType scoreType) {
        this.scoreType = scoreType;
    }

    public int getPointsByVictory() {
        return pointsByVictory;
    }

    public void setPointsByVictory(int pointsByVictory) {
        this.pointsByVictory = pointsByVictory;
    }

    public int getPointsByDraw() {
        return pointsByDraw;
    }

    public void setPointsByDraw(int pointsByDraw) {
        this.pointsByDraw = pointsByDraw;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TournamentScoreDTO that)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        return getPointsByVictory() == that.getPointsByVictory() && getPointsByDraw() == that.getPointsByDraw() && getScoreType() == that.getScoreType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getScoreType(), getPointsByVictory(), getPointsByDraw());
    }
}
