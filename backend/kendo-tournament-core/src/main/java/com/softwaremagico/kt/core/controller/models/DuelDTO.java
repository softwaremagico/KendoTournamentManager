package com.softwaremagico.kt.core.controller.models;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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

import com.softwaremagico.kt.persistence.entities.DuelType;
import com.softwaremagico.kt.persistence.values.Score;

import java.util.ArrayList;
import java.util.List;

public class DuelDTO extends ElementDTO {
    private ParticipantDTO competitor1;
    private ParticipantDTO competitor2;
    private List<Score> competitor1Score = new ArrayList<>(); // M, K, T, D, H, I
    private List<Score> competitor2Score = new ArrayList<>(); // M, K, T, D, H, I
    private Boolean competitor1Fault = false;
    private Boolean competitor2Fault = false;
    private DuelType type;

    public ParticipantDTO getCompetitor1() {
        return competitor1;
    }

    public void setCompetitor1(ParticipantDTO competitor1) {
        this.competitor1 = competitor1;
    }

    public ParticipantDTO getCompetitor2() {
        return competitor2;
    }

    public void setCompetitor2(ParticipantDTO competitor2) {
        this.competitor2 = competitor2;
    }

    public List<Score> getCompetitor1Score() {
        return competitor1Score;
    }

    public void setCompetitor1Score(List<Score> competitor1Score) {
        this.competitor1Score = competitor1Score;
    }

    public List<Score> getCompetitor2Score() {
        return competitor2Score;
    }

    public void setCompetitor2Score(List<Score> competitor2Score) {
        this.competitor2Score = competitor2Score;
    }

    public Boolean getCompetitor1Fault() {
        return competitor1Fault;
    }

    public void setCompetitor1Fault(Boolean competitor1Fault) {
        this.competitor1Fault = competitor1Fault;
    }

    public Boolean getCompetitor2Fault() {
        return competitor2Fault;
    }

    public void setCompetitor2Fault(Boolean competitor2Fault) {
        this.competitor2Fault = competitor2Fault;
    }

    public DuelType getType() {
        return type;
    }

    public void setType(DuelType type) {
        this.type = type;
    }
}
