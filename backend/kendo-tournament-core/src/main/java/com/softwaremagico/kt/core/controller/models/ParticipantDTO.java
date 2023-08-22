package com.softwaremagico.kt.core.controller.models;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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

import com.softwaremagico.kt.utils.IParticipantName;
import com.softwaremagico.kt.utils.NameUtils;

import java.util.Objects;

public class ParticipantDTO extends ElementDTO implements IParticipantName {

    private String idCard;

    private String name;

    private String lastname;

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
        if (!(o instanceof ParticipantDTO that)) {
            return false;
        }
        return getIdCard().equals(that.getIdCard()) && getName().equals(that.getName()) && getLastname().equals(that.getLastname())
                && getClub().equals(that.getClub());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIdCard(), getName(), getLastname(), getClub());
    }
}
