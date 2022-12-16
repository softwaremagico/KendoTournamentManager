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

import com.softwaremagico.kt.persistence.values.TournamentType;

import java.util.Objects;

public class TournamentDTO extends ElementDTO {

    private String name;

    private Integer shiaijos;

    private Integer teamSize;

    private TournamentType type;

    private TournamentScoreDTO tournamentScore;

    private Integer duelsDuration;

    private boolean locked;

    public TournamentDTO() {
        super();
    }

    public TournamentDTO(String name, int shiaijos, int teamSize, TournamentType type) {
        this();
        setName(name);
        setShiaijos(shiaijos);
        setTeamSize(teamSize);
        setType(type);
        setTournamentScore(new TournamentScoreDTO());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getShiaijos() {
        return shiaijos;
    }

    public void setShiaijos(Integer shiaijos) {
        this.shiaijos = shiaijos;
    }

    public Integer getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(Integer teamSize) {
        this.teamSize = teamSize;
    }

    public TournamentType getType() {
        return type;
    }

    public void setType(TournamentType type) {
        this.type = type;
    }

    public TournamentScoreDTO getTournamentScore() {
        return tournamentScore;
    }

    public void setTournamentScore(TournamentScoreDTO tournamentScore) {
        this.tournamentScore = tournamentScore;
    }

    public Integer getDuelsDuration() {
        return duelsDuration;
    }

    public void setDuelsDuration(Integer duelsDuration) {
        this.duelsDuration = duelsDuration;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Override
    public String toString() {
        if (getName() != null) {
            return getName();
        }
        return super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TournamentDTO)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final TournamentDTO that = (TournamentDTO) o;
        return getName().equals(that.getName()) && getShiaijos().equals(that.getShiaijos()) && getTeamSize().equals(that.getTeamSize())
                && getType() == that.getType() && Objects.equals(getTournamentScore(), that.getTournamentScore());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getName(), getShiaijos(), getTeamSize(), getType(), getTournamentScore());
    }
}
