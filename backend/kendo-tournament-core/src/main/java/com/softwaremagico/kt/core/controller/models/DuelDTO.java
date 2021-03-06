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
import com.softwaremagico.kt.utils.NameUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DuelDTO extends ElementDTO {
    private ParticipantDTO competitor1;
    private ParticipantDTO competitor2;
    private List<Score> competitor1Score = new ArrayList<>(); // M, K, T, D, H, I
    private List<Score> competitor2Score = new ArrayList<>(); // M, K, T, D, H, I
    private Boolean competitor1Fault = false;
    private Boolean competitor2Fault = false;
    private DuelType type;

    public DuelDTO() {
        super();
        setType(DuelType.STANDARD);
    }

    public DuelDTO(ParticipantDTO competitor1, ParticipantDTO competitor2) {
        this();
        setCompetitor1(competitor1);
        setCompetitor2(competitor2);
    }

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

    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder();
        if (competitor1 != null) {
            text.append(NameUtils.getShortLastnameName(competitor1, 10)).append(" ");
            if (competitor1Fault != null && competitor1Fault) {
                text.append("^");
            }
            text.append("[");
            for (final Score hitsFromCompetitorA1 : competitor1Score) {
                if (hitsFromCompetitorA1 != null) {
                    text.append(hitsFromCompetitorA1.getAbbreviation());
                }
            }
            text.append("] ");
        } else {
            text.append("  <<Empty>>  []  ");
        }
        if (competitor2 != null) {
            text.append("[");
            for (final Score hitsFromCompetitorB1 : competitor2Score) {
                if (hitsFromCompetitorB1 != null) {
                    text.append(hitsFromCompetitorB1.getAbbreviation());
                }
            }
            text.append("]");
            if (competitor2Fault != null && competitor2Fault) {
                text.append("^");
            }
            text.append(" ");
            text.append(NameUtils.getShortLastnameName(competitor2, 10));
        } else {
            text.append("[]  <<Empty>>  ");
        }

        return text.toString();
    }

    /**
     * Gets the winner of the duel.
     *
     * @return -1 if player of first team, 0 if draw, 1 if player of second
     * tiem.
     */
    public int getWinner() {
        return Integer.compare(getCompetitor2ScoreValue(), getCompetitor1ScoreValue());
    }

    public Integer getCompetitor1ScoreValue() {
        return (int) competitor1Score.stream().filter(Score::isValidPoint).count();
    }

    public Integer getCompetitor2ScoreValue() {
        return (int) competitor2Score.stream().filter(Score::isValidPoint).count();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DuelDTO)) {
            return false;
        }
        final DuelDTO duelDTO = (DuelDTO) o;
        return Objects.equals(getCompetitor1(), duelDTO.getCompetitor1()) && Objects.equals(getCompetitor2(), duelDTO.getCompetitor2())
                && Objects.equals(getCompetitor1Score(), duelDTO.getCompetitor1Score()) && Objects.equals(getCompetitor2Score(),
                duelDTO.getCompetitor2Score()) && Objects.equals(getCompetitor1Fault(), duelDTO.getCompetitor1Fault()) &&
                Objects.equals(getCompetitor2Fault(), duelDTO.getCompetitor2Fault()) && getType() == duelDTO.getType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCompetitor1(), getCompetitor2(), getCompetitor1Score(), getCompetitor2Score(), getCompetitor1Fault(),
                getCompetitor2Fault(), getType());
    }
}
