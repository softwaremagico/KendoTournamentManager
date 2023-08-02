package com.softwaremagico.kt.core.statistics;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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

import java.time.LocalDateTime;

public class TournamentFightStatistics {

    private Long menNumber;

    private Long koteNumber;

    private Long doNumber;

    private Long tsukiNumber;

    private Long hansokuNumber;

    private Long ipponNumber;

    private Long fightsNumber;
    private Long fightsByTeam;
    private Long duelsNumber;
    //In seconds.
    private Long averageTime;
    private Long estimatedTime;

    private Long fightsFinished;

    private LocalDateTime fightsStartedAt;

    private LocalDateTime fightsFinishedAt;

    private long faults;

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

    public LocalDateTime getFightsStartedAt() {
        return fightsStartedAt;
    }

    public void setFightsStartedAt(LocalDateTime fightsStartedAt) {
        this.fightsStartedAt = fightsStartedAt;
    }

    public LocalDateTime getFightsFinishedAt() {
        return fightsFinishedAt;
    }

    public void setFightsFinishedAt(LocalDateTime fightsFinishedAt) {
        this.fightsFinishedAt = fightsFinishedAt;
    }

    public Long getMenNumber() {
        return menNumber;
    }

    public void setMenNumber(Long menNumber) {
        this.menNumber = menNumber;
    }

    public Long getKoteNumber() {
        return koteNumber;
    }

    public void setKoteNumber(Long koteNumber) {
        this.koteNumber = koteNumber;
    }

    public Long getDoNumber() {
        return doNumber;
    }

    public void setDoNumber(Long doNumber) {
        this.doNumber = doNumber;
    }

    public Long getTsukiNumber() {
        return tsukiNumber;
    }

    public void setTsukiNumber(Long tsukiNumber) {
        this.tsukiNumber = tsukiNumber;
    }

    public Long getHansokuNumber() {
        return hansokuNumber;
    }

    public void setHansokuNumber(Long hansokuNumber) {
        this.hansokuNumber = hansokuNumber;
    }

    public Long getIpponNumber() {
        return ipponNumber;
    }

    public void setIpponNumber(Long ipponNumber) {
        this.ipponNumber = ipponNumber;
    }

    /**
     * Count fights that has at least one duel finished.
     */
    public Long getFightsFinished() {
        return fightsFinished;
    }

    public void setFightsFinished(Long fightsFinished) {
        this.fightsFinished = fightsFinished;
    }

    public long getFaults() {
        return faults;
    }

    public void setFaults(long faults) {
        this.faults = faults;
    }
}
