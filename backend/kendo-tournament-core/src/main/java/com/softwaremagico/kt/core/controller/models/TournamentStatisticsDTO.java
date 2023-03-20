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

import com.softwaremagico.kt.persistence.values.RoleType;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class TournamentStatisticsDTO extends ElementDTO {

    private FightStatisticsDTO fightStatisticsDTO;

    private Long menNumber;

    private Long koteNumber;

    private Long doNumber;

    private Long tsukiNumber;

    private Long hansokuNumber;

    private Long ipponNumber;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private Integer numberOfTeams;

    private Map<RoleType, Long> numberOfParticipants = new HashMap<>();

    public FightStatisticsDTO getFightStatisticsDTO() {
        return fightStatisticsDTO;
    }

    public void setFightStatisticsDTO(FightStatisticsDTO fightStatisticsDTO) {
        this.fightStatisticsDTO = fightStatisticsDTO;
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

    public Integer getNumberOfTeams() {
        return numberOfTeams;
    }

    public void setNumberOfTeams(Integer numberOfTeams) {
        this.numberOfTeams = numberOfTeams;
    }

    public Map<RoleType, Long> getNumberOfParticipants() {
        return numberOfParticipants;
    }

    public void setNumberOfParticipants(Map<RoleType, Long> numberOfParticipants) {
        this.numberOfParticipants = numberOfParticipants;
    }
}
