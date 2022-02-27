package com.softwaremagico.kt.rest.model;

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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FightDto extends ElementDto {
    private TeamDto team1;
    private TeamDto team2;
    private TournamentDto tournament;
    private Integer shiaijo;
    private List<DuelDto> duels = new ArrayList<>();
    private LocalDateTime finishedAt;
    private Integer level;

    public TeamDto getTeam1() {
        return team1;
    }

    public void setTeam1(TeamDto team1) {
        this.team1 = team1;
    }

    public TeamDto getTeam2() {
        return team2;
    }

    public void setTeam2(TeamDto team2) {
        this.team2 = team2;
    }

    public TournamentDto getTournament() {
        return tournament;
    }

    public void setTournament(TournamentDto tournament) {
        this.tournament = tournament;
    }

    public Integer getShiaijo() {
        return shiaijo;
    }

    public void setShiaijo(Integer shiaijo) {
        this.shiaijo = shiaijo;
    }

    public List<DuelDto> getDuels() {
        return duels;
    }

    public void setDuels(List<DuelDto> duels) {
        this.duels = duels;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }
}
