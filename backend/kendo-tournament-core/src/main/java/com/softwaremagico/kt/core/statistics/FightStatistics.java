package com.softwaremagico.kt.core.statistics;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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

public class FightStatistics {

    private Long fightsNumber;
    private Long fightsByTeam;
    private Long duelsNumber;
    //In seconds.
    private Long averageTime;
    private Long estimatedTime;

    public Long getFightsNumber() {
        return fightsNumber;
    }

    public void setFightsNumber(Long fightsNumber) {
        if (fightsNumber != null && fightsNumber >= 0) {
            this.fightsNumber = fightsNumber;
        } else {
            this.fightsNumber = null;
        }
    }

    public Long getFightsByTeam() {
        return fightsByTeam;
    }

    public void setFightsByTeam(Long fightsByTeam) {
        this.fightsByTeam = fightsByTeam;
    }

    public Long getDuelsNumber() {
        return duelsNumber;
    }

    public void setDuelsNumber(Long duelsNumber) {
        if (duelsNumber != null && duelsNumber >= 0) {
            this.duelsNumber = duelsNumber;
        } else {
            this.duelsNumber = null;
        }
    }

    public Long getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(Long estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public Long getAverageTime() {
        return averageTime;
    }

    public void setAverageTime(Long averageTime) {
        this.averageTime = averageTime;
    }
}
