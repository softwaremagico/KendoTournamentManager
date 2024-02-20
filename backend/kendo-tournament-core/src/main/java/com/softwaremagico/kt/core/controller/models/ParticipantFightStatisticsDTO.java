package com.softwaremagico.kt.core.controller.models;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2022 Softwaremagico
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

import java.util.Objects;

public class ParticipantFightStatisticsDTO extends ElementDTO {

    private Long menNumber;

    private Long koteNumber;

    private Long doNumber;

    private Long tsukiNumber;

    private Long hansokuNumber;

    private Long ipponNumber;

    private Long receivedMenNumber;

    private Long receivedKoteNumber;

    private Long receivedDoNumber;

    private Long receivedTsukiNumber;

    private Long receivedHansokuNumber;

    private Long receivedIpponNumber;

    private Long duelsNumber;
    //In seconds.
    private Long averageTime;

    private Long totalDuelsTime;

    private Long faults;

    private Long receivedFaults;

    private Long quickestHit;

    private Long quickestReceivedHit;

    private Long wonDuels;

    private Long lostDuels;

    private Long drawDuels;

    public Long getDuelsNumber() {
        return Objects.requireNonNullElse(duelsNumber, 0L);
    }

    public void setDuelsNumber(Long duelsNumber) {
        if (duelsNumber != null && duelsNumber >= 0) {
            this.duelsNumber = duelsNumber;
        } else {
            this.duelsNumber = null;
        }
    }


    public Long getAverageTime() {
        return averageTime;
    }

    public void setAverageTime(Long averageTime) {
        this.averageTime = averageTime;
    }

    public Long getMenNumber() {
        return Objects.requireNonNullElse(menNumber, 0L);
    }

    public void setMenNumber(Long menNumber) {
        this.menNumber = menNumber;
    }

    public Long getKoteNumber() {
        return Objects.requireNonNullElse(koteNumber, 0L);
    }

    public void setKoteNumber(Long koteNumber) {
        this.koteNumber = koteNumber;
    }

    public Long getDoNumber() {
        return Objects.requireNonNullElse(doNumber, 0L);
    }

    public void setDoNumber(Long doNumber) {
        this.doNumber = doNumber;
    }

    public Long getTsukiNumber() {
        return Objects.requireNonNullElse(tsukiNumber, 0L);
    }

    public void setTsukiNumber(Long tsukiNumber) {
        this.tsukiNumber = tsukiNumber;
    }

    public Long getHansokuNumber() {
        return Objects.requireNonNullElse(hansokuNumber, 0L);
    }

    public void setHansokuNumber(Long hansokuNumber) {
        this.hansokuNumber = hansokuNumber;
    }

    public Long getIpponNumber() {
        return Objects.requireNonNullElse(ipponNumber, 0L);
    }

    public void setIpponNumber(Long ipponNumber) {
        this.ipponNumber = ipponNumber;
    }

    public Long getFaults() {
        return Objects.requireNonNullElse(faults, 0L);
    }

    public void setFaults(Long faults) {
        this.faults = faults;
    }

    public Long getReceivedMenNumber() {
        return Objects.requireNonNullElse(receivedMenNumber, 0L);
    }

    public void setReceivedMenNumber(Long receivedMenNumber) {
        this.receivedMenNumber = receivedMenNumber;
    }

    public Long getReceivedKoteNumber() {
        return Objects.requireNonNullElse(receivedKoteNumber, 0L);
    }

    public void setReceivedKoteNumber(Long receivedKoteNumber) {
        this.receivedKoteNumber = receivedKoteNumber;
    }

    public Long getReceivedDoNumber() {
        return Objects.requireNonNullElse(receivedDoNumber, 0L);
    }

    public void setReceivedDoNumber(Long receivedDoNumber) {
        this.receivedDoNumber = receivedDoNumber;
    }

    public Long getReceivedTsukiNumber() {
        return Objects.requireNonNullElse(receivedTsukiNumber, 0L);
    }

    public void setReceivedTsukiNumber(Long receivedTsukiNumber) {
        this.receivedTsukiNumber = receivedTsukiNumber;
    }

    public Long getReceivedHansokuNumber() {
        return Objects.requireNonNullElse(receivedHansokuNumber, 0L);
    }

    public void setReceivedHansokuNumber(Long receivedHansokuNumber) {
        this.receivedHansokuNumber = receivedHansokuNumber;
    }

    public Long getReceivedIpponNumber() {
        return Objects.requireNonNullElse(receivedIpponNumber, 0L);
    }

    public void setReceivedIpponNumber(Long receivedIpponNumber) {
        this.receivedIpponNumber = receivedIpponNumber;
    }

    public Long getReceivedFaults() {
        return receivedFaults;
    }

    public void setReceivedFaults(Long receivedFaults) {
        this.receivedFaults = receivedFaults;
    }

    public Long getTotalDuelsTime() {
        return totalDuelsTime;
    }

    public void setTotalDuelsTime(Long totalDuelsTime) {
        this.totalDuelsTime = totalDuelsTime;
    }

    public Long getQuickestHit() {
        return quickestHit;
    }

    public void setQuickestHit(Long quickestHit) {
        this.quickestHit = quickestHit;
    }

    public Long getQuickestReceivedHit() {
        return quickestReceivedHit;
    }

    public void setQuickestReceivedHit(Long quickestReceivedHit) {
        this.quickestReceivedHit = quickestReceivedHit;
    }

    public Long getWonDuels() {
        return wonDuels;
    }

    public void setWonDuels(Long wonDuels) {
        this.wonDuels = wonDuels;
    }

    public Long getLostDuels() {
        return lostDuels;
    }

    public void setLostDuels(Long lostDuels) {
        this.lostDuels = lostDuels;
    }

    public Long getDrawDuels() {
        return drawDuels;
    }

    public void setDrawDuels(Long drawDuels) {
        this.drawDuels = drawDuels;
    }
}
