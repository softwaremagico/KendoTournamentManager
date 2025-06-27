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

import com.softwaremagico.kt.persistence.values.TournamentType;
import com.softwaremagico.kt.utils.IName;

import java.time.LocalDateTime;
import java.util.Objects;

public class TournamentDTO extends ElementDTO implements IName {

    private String name;

    private Integer shiaijos;

    private Integer teamSize;

    private TournamentType type;

    private TournamentScoreDTO tournamentScore;

    private Integer duelsDuration;

    private boolean locked;

    private LocalDateTime lockedAt;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

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

    public LocalDateTime getLockedAt() {
        return lockedAt;
    }

    public void setLockedAt(LocalDateTime lockedAt) {
        this.lockedAt = lockedAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
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
        if (!(o instanceof TournamentDTO that)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        return getName().equals(that.getName()) && getShiaijos().equals(that.getShiaijos()) && getTeamSize().equals(that.getTeamSize())
                && getType() == that.getType() && Objects.equals(getTournamentScore(), that.getTournamentScore());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getName(), getShiaijos(), getTeamSize(), getType(), getTournamentScore());
    }
}
