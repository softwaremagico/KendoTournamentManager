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

public class TournamentStatistics {

    private FightStatistics fightStatistics;

    private int menNumber;

    private int koteNumber;

    private int doNumber;

    private int tsukiNumber;

    private int hansokuNumber;

    public FightStatistics getFightStatistics() {
        return fightStatistics;
    }

    public void setFightStatistics(FightStatistics fightStatistics) {
        this.fightStatistics = fightStatistics;
    }

    public int getMenNumber() {
        return menNumber;
    }

    public void setMenNumber(int menNumber) {
        this.menNumber = menNumber;
    }

    public int getKoteNumber() {
        return koteNumber;
    }

    public void setKoteNumber(int koteNumber) {
        this.koteNumber = koteNumber;
    }

    public int getDoNumber() {
        return doNumber;
    }

    public void setDoNumber(int doNumber) {
        this.doNumber = doNumber;
    }

    public int getTsukiNumber() {
        return tsukiNumber;
    }

    public void setTsukiNumber(int tsukiNumber) {
        this.tsukiNumber = tsukiNumber;
    }

    public int getHansokuNumber() {
        return hansokuNumber;
    }

    public void setHansokuNumber(int hansokuNumber) {
        this.hansokuNumber = hansokuNumber;
    }
}
