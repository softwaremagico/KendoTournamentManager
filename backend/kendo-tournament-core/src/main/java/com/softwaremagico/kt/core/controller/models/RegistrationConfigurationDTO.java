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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Public, minimal information needed to render a self-registration form. It
 * never exposes participants or their personal data.
 */
@SuppressWarnings("java:S2160")
public class RegistrationConfigurationDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -3120097763528096501L;

    private String tournamentName;

    private Integer teamSize;

    private boolean teamSelectionEnabled;

    private List<RegistrationClubDTO> clubs = new ArrayList<>();

    private List<RoleType> availableRoles = new ArrayList<>();

    public String getTournamentName() {
        return tournamentName;
    }

    public void setTournamentName(String tournamentName) {
        this.tournamentName = tournamentName;
    }

    public Integer getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(Integer teamSize) {
        this.teamSize = teamSize;
    }

    public boolean isTeamSelectionEnabled() {
        return teamSelectionEnabled;
    }

    public void setTeamSelectionEnabled(boolean teamSelectionEnabled) {
        this.teamSelectionEnabled = teamSelectionEnabled;
    }

    public List<RegistrationClubDTO> getClubs() {
        return clubs;
    }

    public void setClubs(List<RegistrationClubDTO> clubs) {
        this.clubs = clubs;
    }

    public List<RoleType> getAvailableRoles() {
        return availableRoles;
    }

    public void setAvailableRoles(List<RoleType> availableRoles) {
        this.availableRoles = availableRoles;
    }
}
