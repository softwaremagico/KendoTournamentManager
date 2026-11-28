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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.softwaremagico.kt.persistence.values.RoleType;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Public acknowledgement of a registration. It reuses
 * {@link ParticipantReducedDTO} so the id card is never serialized.
 */
@SuppressWarnings("java:S2160")
public class RegistrationResponseDTO extends ParticipantReducedDTO {

    @Serial
    private static final long serialVersionUID = 6013646756426298869L;

    private List<RoleType> roles = new ArrayList<>();

    private String teamName;

    private boolean existingParticipant;

    private boolean pendingReview;

    /** Authentication roles inherited from {@code IAuthenticatedUser}. */
    @Override
    @JsonIgnore
    public Set<String> getRoles() {
        return super.getRoles();
    }

    /** Tournament roles granted by this registration. */
    @JsonProperty("roles")
    public List<RoleType> getRegistrationRoles() {
        return roles;
    }

    public void setRoles(List<RoleType> roles) {
        this.roles = roles;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public boolean isExistingParticipant() {
        return existingParticipant;
    }

    public void setExistingParticipant(boolean existingParticipant) {
        this.existingParticipant = existingParticipant;
    }

    public boolean isPendingReview() {
        return pendingReview;
    }

    public void setPendingReview(boolean pendingReview) {
        this.pendingReview = pendingReview;
    }
}
