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
import java.util.EnumMap;
import java.util.Map;

public class TournamentStatistics {

    private TournamentFightStatistics tournamentFightStatistics;

    private Integer tournamentId;

    private String tournamentName;

    private LocalDateTime tournamentCreatedAt;

    private LocalDateTime tournamentFinishedAt;

    private LocalDateTime tournamentLockedAt;

    private Long numberOfTeams;

    private Integer teamSize;

    private Integer fightSize;

    private Map<RoleType, Long> numberOfParticipants = new EnumMap<>(RoleType.class);

    public TournamentFightStatistics getFightStatistics() {
        return tournamentFightStatistics;
    }

    public void setFightStatistics(TournamentFightStatistics tournamentFightStatistics) {
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

    public void addNumberOfParticipants(RoleType roleType, Long number) {
        if (this.numberOfParticipants == null) {
            this.numberOfParticipants = new EnumMap<>(RoleType.class);
        }
        this.numberOfParticipants.put(roleType, number);
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

    public LocalDateTime getTournamentCreatedAt() {
        return tournamentCreatedAt;
    }

    public void setTournamentCreatedAt(LocalDateTime startedAt) {
        this.tournamentCreatedAt = startedAt;
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

    public void setTeamSize(Integer teamsMember) {
        this.teamSize = teamsMember;
    }

    public Integer getFightSize() {
        return fightSize;
    }

    public void setFightSize(Integer fightSize) {
        this.fightSize = fightSize;
    }
}
