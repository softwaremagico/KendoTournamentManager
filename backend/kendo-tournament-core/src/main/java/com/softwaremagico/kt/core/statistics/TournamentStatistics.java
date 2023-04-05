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

import com.softwaremagico.kt.persistence.values.RoleType;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class TournamentStatistics {

    private FightStatistics fightStatistics;

    private Integer tournamentId;

    private String tournamentName;

    private LocalDateTime tournamentCreatedAt;

    private LocalDateTime tournamentLockedAt;

    private Long numberOfTeams;

    private Integer teamSize;

    private Map<RoleType, Long> numberOfParticipants = new HashMap<>();

    public FightStatistics getFightStatistics() {
        return fightStatistics;
    }

    public void setFightStatistics(FightStatistics fightStatistics) {
        this.fightStatistics = fightStatistics;
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
            this.numberOfParticipants = new HashMap<>();
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

    public void setTournamentLockedAt(LocalDateTime finishedAt) {
        this.tournamentLockedAt = finishedAt;
    }

    public Integer getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(Integer teamsMember) {
        this.teamSize = teamsMember;
    }
}
