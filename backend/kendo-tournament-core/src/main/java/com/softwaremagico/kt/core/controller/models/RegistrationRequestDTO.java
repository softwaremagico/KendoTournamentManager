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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Payload accepted by the public self-registration endpoint. */
@SuppressWarnings("java:S2160")
public class RegistrationRequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -3054415405925517371L;

    /** Id of an existing club, or {@code null} to create a new one. */
    private Integer clubId;

    private String newClubName;

    private String newClubCity;

    private String newClubCountry;

    @NotBlank
    private String idCard;

    @NotBlank
    private String name;

    @NotBlank
    private String lastname;

    /** Only COMPETITOR and REFEREE are accepted. */
    @NotEmpty
    private List<RoleType> roles = new ArrayList<>();

    /** Optional team name; ignored when the tournament has {@code teamSize <= 1}. */
    private String teamPreference;

    public Integer getClubId() {
        return clubId;
    }

    public void setClubId(Integer clubId) {
        this.clubId = clubId;
    }

    public String getNewClubName() {
        return newClubName;
    }

    public void setNewClubName(String newClubName) {
        this.newClubName = newClubName;
    }

    public String getNewClubCity() {
        return newClubCity;
    }

    public void setNewClubCity(String newClubCity) {
        this.newClubCity = newClubCity;
    }

    public String getNewClubCountry() {
        return newClubCountry;
    }

    public void setNewClubCountry(String newClubCountry) {
        this.newClubCountry = newClubCountry;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public List<RoleType> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleType> roles) {
        this.roles = roles;
    }

    public String getTeamPreference() {
        return teamPreference;
    }

    public void setTeamPreference(String teamPreference) {
        this.teamPreference = teamPreference;
    }
}
