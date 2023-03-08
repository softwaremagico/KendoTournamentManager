package com.softwaremagico.kt.core.controller.models;

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

public class TournamentStatisticsDTO extends ElementDTO {

    private FightStatisticsDTO fightStatisticsDTO;

    private long menNumber;

    private long koteNumber;

    private long doNumber;

    private long tsukiNumber;

    private long hansokuNumber;

    public FightStatisticsDTO getFightStatisticsDTO() {
        return fightStatisticsDTO;
    }

    public void setFightStatisticsDTO(FightStatisticsDTO fightStatisticsDTO) {
        this.fightStatisticsDTO = fightStatisticsDTO;
    }

    public long getMenNumber() {
        return menNumber;
    }

    public void setMenNumber(long menNumber) {
        this.menNumber = menNumber;
    }

    public long getKoteNumber() {
        return koteNumber;
    }

    public void setKoteNumber(long koteNumber) {
        this.koteNumber = koteNumber;
    }

    public long getDoNumber() {
        return doNumber;
    }

    public void setDoNumber(long doNumber) {
        this.doNumber = doNumber;
    }

    public long getTsukiNumber() {
        return tsukiNumber;
    }

    public void setTsukiNumber(long tsukiNumber) {
        this.tsukiNumber = tsukiNumber;
    }

    public long getHansokuNumber() {
        return hansokuNumber;
    }

    public void setHansokuNumber(long hansokuNumber) {
        this.hansokuNumber = hansokuNumber;
    }
}
