package com.softwaremagico.kt.persistence.entities;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
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

import com.softwaremagico.kt.persistence.encryption.TournamentTypeCryptoConverter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "tournament_scores")
public class TournamentScore extends Element {

    @Column(name = "tournament_type")
    @Enumerated(EnumType.STRING)
    @Convert(converter = TournamentTypeCryptoConverter.class)
    private ScoreType scoreType;

    @Column(name = "points_by_victory")
    private int pointsByVictory = 1;

    @Column(name = "points_by_draw")
    private int pointsByDraw = 0;

    public TournamentScore() {
        super();
        scoreType = ScoreType.CLASSIC;
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
}
