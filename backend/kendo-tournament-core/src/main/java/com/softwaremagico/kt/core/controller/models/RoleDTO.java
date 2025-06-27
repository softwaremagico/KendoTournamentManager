package com.softwaremagico.kt.core.controller.models;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

public class RoleDTO extends ElementDTO {

    @NotNull
    private TournamentDTO tournament;

    @NotNull
    private ParticipantDTO participant;

    @NotNull
    private RoleType roleType;

    private boolean diplomaPrinted = false;

    private boolean accreditationPrinted = false;


    public RoleDTO() {
        super();
    }

    public RoleDTO(TournamentDTO tournament, ParticipantDTO participant, RoleType roleType) {
        this();
        setTournament(tournament);
        setParticipant(participant);
        setRoleType(roleType);
    }

    public TournamentDTO getTournament() {
        return tournament;
    }

    public void setTournament(TournamentDTO tournament) {
        this.tournament = tournament;
    }

    public ParticipantDTO getParticipant() {
        return participant;
    }

    public void setParticipant(ParticipantDTO participant) {
        this.participant = participant;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }

    public boolean isDiplomaPrinted() {
        return diplomaPrinted;
    }

    public void setDiplomaPrinted(boolean diplomaPrinted) {
        this.diplomaPrinted = diplomaPrinted;
    }

    public boolean isAccreditationPrinted() {
        return accreditationPrinted;
    }

    public void setAccreditationPrinted(boolean accreditationPrinted) {
        this.accreditationPrinted = accreditationPrinted;
    }

    @Override
    public String toString() {
        if (getTournament() != null) {
            return String.format("ROLE{%s %s %s}", getTournament().getName(), getParticipant().getName(), getRoleType());
        }
        return super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RoleDTO roleDTO)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        return getTournament().equals(roleDTO.getTournament()) && getParticipant().equals(roleDTO.getParticipant())
                && getRoleType() == roleDTO.getRoleType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getTournament(), getParticipant(), getRoleType());
    }
}
