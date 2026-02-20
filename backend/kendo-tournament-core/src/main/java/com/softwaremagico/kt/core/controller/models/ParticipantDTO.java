package com.softwaremagico.kt.core.controller.models;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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

import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.persistence.entities.IAuthenticatedUser;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.utils.IParticipantName;
import com.softwaremagico.kt.utils.NameUtils;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serial;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ParticipantDTO extends ElementDTO implements IParticipantName, IAuthenticatedUser {

    @Serial
    private static final long serialVersionUID = -646370421493264121L;

    private static final int HASH_VALUE = 31;

    private String idCard;

    @NotBlank
    private String name;

    @NotBlank
    private String lastname;

    @NotNull
    private ClubDTO club;

    private Boolean hasAvatar = false;

    public ParticipantDTO() {
    }

    public ParticipantDTO(String idCard, String name, String lastname, ClubDTO club) {
        setName(name);
        setLastname(lastname);
        setIdCard(idCard);
        setClub(club);
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getLastname() {
        return lastname;
    }

    @Override
    public Set<String> getRoles() {
        return new HashSet<>(List.of(Participant.PARTICIPANT_ROLE));
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public ClubDTO getClub() {
        return club;
    }

    public void setClub(ClubDTO club) {
        this.club = club;
    }

    public Boolean getHasAvatar() {
        return hasAvatar;
    }

    public void setHasAvatar(Boolean hasAvatar) {
        this.hasAvatar = hasAvatar;
    }

    @Override
    public String toString() {
        if (getName() != null) {
            return NameUtils.getLastnameName(getLastname(), getName());
        }
        return super.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final ParticipantDTO that = (ParticipantDTO) o;

        if (!name.equals(that.name)) {
            return false;
        }
        if (!lastname.equals(that.lastname)) {
            return false;
        }
        return Objects.equals(club, that.club);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = HASH_VALUE * result + name.hashCode();
        result = HASH_VALUE * result + lastname.hashCode();
        result = HASH_VALUE * result + (club != null ? club.hashCode() : 0);
        return result;
    }

    @Override
    public String getUsername() {
        return getId() + ParticipantProvider.TOKEN_NAME_SEPARATOR + name + ParticipantProvider.TOKEN_NAME_SEPARATOR + lastname;
    }
}
