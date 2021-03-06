package com.softwaremagico.kt.core.controller.models;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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

import java.util.List;
import java.util.Objects;

public class GroupDTO extends ElementDTO {

    private TournamentDTO tournament;

    private List<TeamDTO> teams;

    private Integer shiaijo;

    private Integer level;

    private List<FightDTO> fights;

    private Integer numberOfWinners;

    private List<DuelDTO> unties;

    public TournamentDTO getTournament() {
        return tournament;
    }

    public void setTournament(TournamentDTO tournamentDTO) {
        this.tournament = tournamentDTO;
    }

    public List<TeamDTO> getTeams() {
        return teams;
    }

    public void setTeams(List<TeamDTO> teams) {
        this.teams = teams;
    }

    public Integer getShiaijo() {
        return shiaijo;
    }

    public void setShiaijo(Integer shiaijo) {
        this.shiaijo = shiaijo;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public List<FightDTO> getFights() {
        return fights;
    }

    public void setFights(List<FightDTO> fights) {
        this.fights = fights;
    }

    public Integer getNumberOfWinners() {
        return numberOfWinners;
    }

    public void setNumberOfWinners(Integer numberOfWinners) {
        this.numberOfWinners = numberOfWinners;
    }

    public List<DuelDTO> getUnties() {
        return unties;
    }

    public void setUnties(List<DuelDTO> unties) {
        this.unties = unties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GroupDTO)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final GroupDTO groupDTO = (GroupDTO) o;
        return getTournament().equals(groupDTO.getTournament()) && Objects.equals(getTeams(), groupDTO.getTeams()) &&
                getShiaijo().equals(groupDTO.getShiaijo()) && getLevel().equals(groupDTO.getLevel()) && Objects.equals(getFights(),
                groupDTO.getFights()) && getNumberOfWinners().equals(groupDTO.getNumberOfWinners()) && Objects.equals(getUnties(), groupDTO.getUnties());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getTournament(), getTeams(), getShiaijo(), getLevel(), getFights(), getNumberOfWinners(), getUnties());
    }
}
