package com.softwaremagico.kt.core.statistics;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2025 Softwaremagico
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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ParticipantStatistics {

    private ParticipantFightStatistics participantFightStatistics;

    private Integer participantId;

    private String participantName;

    private LocalDateTime participantCreatedAt;

    private int tournaments;

    private long totalTournaments;

    private Map<RoleType, Long> rolesPerformed = new HashMap<>();

    public ParticipantFightStatistics getFightStatistics() {
        return participantFightStatistics;
    }

    public void setFightStatistics(ParticipantFightStatistics participantFightStatistics) {
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

    public void addRolePerformed(RoleType roleType, Long number) {
        if (this.rolesPerformed == null) {
            this.rolesPerformed = new HashMap<>();
        }
        this.rolesPerformed.put(roleType, number);
    }

    public ParticipantFightStatistics getParticipantFightStatistics() {
        return participantFightStatistics;
    }

    public void setParticipantFightStatistics(ParticipantFightStatistics participantFightStatistics) {
        this.participantFightStatistics = participantFightStatistics;
    }

    public LocalDateTime getParticipantCreatedAt() {
        return participantCreatedAt;
    }

    public void setParticipantCreatedAt(LocalDateTime participantCreatedAt) {
        this.participantCreatedAt = participantCreatedAt;
    }
}
