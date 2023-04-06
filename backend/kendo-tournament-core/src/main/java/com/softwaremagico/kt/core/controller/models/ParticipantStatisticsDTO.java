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

public class ParticipantStatisticsDTO extends ElementDTO {

    private ParticipantFightStatisticsDTO participantFightStatistics;

    private Integer participantId;

    private String participantName;

    private LocalDateTime participantCreatedAt;

    private int tournaments;

    private Map<RoleType, Long> rolesPerformed = new HashMap<>();

    public ParticipantFightStatisticsDTO getParticipantFightStatistics() {
        return participantFightStatistics;
    }

    public void setParticipantFightStatistics(ParticipantFightStatisticsDTO participantFightStatistics) {
        this.participantFightStatistics = participantFightStatistics;
    }

    public Integer getParticipantId() {
        return participantId;
    }

    public void setParticipantId(Integer participantId) {
        this.participantId = participantId;
    }

    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

    public int getTournaments() {
        return tournaments;
    }

    public void setTournaments(int tournaments) {
        this.tournaments = tournaments;
    }

    public Map<RoleType, Long> getRolesPerformed() {
        return rolesPerformed;
    }

    public void setRolesPerformed(Map<RoleType, Long> rolesPerformed) {
        this.rolesPerformed = rolesPerformed;
    }

    public LocalDateTime getParticipantCreatedAt() {
        return participantCreatedAt;
    }

    public void setParticipantCreatedAt(LocalDateTime participantCreatedAt) {
        this.participantCreatedAt = participantCreatedAt;
    }
}
