package com.softwaremagico.kt.core.controller.models;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2026 Softwaremagico
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

import com.softwaremagico.kt.persistence.values.RoleType;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

public class ParticipantStatisticsDTO extends ElementDTO {

    @Serial
    private static final long serialVersionUID = -1980459158365146224L;

    private ParticipantFightStatisticsDTO participantFightStatistics;

    private Integer participantId;

    private String participantName;

    private LocalDateTime participantCreatedAt;

    private int tournaments;

    private long totalTournaments;

    private Map<RoleType, Long> rolesPerformed = new EnumMap<>(RoleType.class);

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

    public long getTotalTournaments() {
        return totalTournaments;
    }

    public void setTotalTournaments(long totalTournaments) {
        this.totalTournaments = totalTournaments;
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
