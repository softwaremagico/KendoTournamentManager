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

public class TournamentStatisticsDTO extends ElementDTO {

    @Serial
    private static final long serialVersionUID = 3369949989764424142L;

    private TournamentFightStatisticsDTO tournamentFightStatistics;

    private Integer tournamentId;

    private String tournamentName;

    private LocalDateTime tournamentCreatedAt;

    private LocalDateTime tournamentFinishedAt;

    private LocalDateTime tournamentLockedAt;

    private Long numberOfTeams;

    private Integer teamSize;

    private Map<RoleType, Long> numberOfParticipants = new EnumMap<>(RoleType.class);

    public TournamentFightStatisticsDTO getTournamentFightStatistics() {
        return tournamentFightStatistics;
    }

    public void setTournamentFightStatistics(TournamentFightStatisticsDTO tournamentFightStatistics) {
        this.tournamentFightStatistics = tournamentFightStatistics;
    }

    public Long getNumberOfTeams() {
        return numberOfTeams;
    }

    public void setNumberOfTeams(Long numberOfTeams) {
        this.numberOfTeams = numberOfTeams;
    }

    public Map<RoleType, Long> getNumberOfParticipants() {
        return numberOfParticipants;
    }

    public void setNumberOfParticipants(Map<RoleType, Long> numberOfParticipants) {
        this.numberOfParticipants = numberOfParticipants;
    }

    public Integer getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Integer tournamentId) {
        this.tournamentId = tournamentId;
    }

    public String getTournamentName() {
        return tournamentName;
    }

    public void setTournamentName(String tournamentName) {
        this.tournamentName = tournamentName;
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return tournamentCreatedAt;
    }

    @Override
    public void setCreatedAt(LocalDateTime tournamentCreatedAt) {
        this.tournamentCreatedAt = tournamentCreatedAt;
    }

    public LocalDateTime getTournamentLockedAt() {
        return tournamentLockedAt;
    }

    public void setTournamentLockedAt(LocalDateTime tournamentLockedAt) {
        this.tournamentLockedAt = tournamentLockedAt;
    }

    public LocalDateTime getTournamentFinishedAt() {
        return tournamentFinishedAt;
    }

    public void setTournamentFinishedAt(LocalDateTime tournamentFinishedAt) {
        this.tournamentFinishedAt = tournamentFinishedAt;
    }

    public Integer getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(Integer teamSize) {
        this.teamSize = teamSize;
    }
}
