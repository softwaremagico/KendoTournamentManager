package com.softwaremagico.kt.core.controller.models;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
 * %%
 * Copyright (C) 2021 - 2024 Softwaremagico
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

public class ParticipantReducedDTO extends ParticipantDTO implements IParticipantName {

    public ParticipantReducedDTO() {
    }

    public ParticipantReducedDTO(String idCard, String name, String lastname, ClubDTO club) {
        setName(name);
        setLastname(lastname);
        setIdCard(idCard);
        setClub(club);
    }

    public ParticipantReducedDTO(ParticipantDTO participantDTO) {
        setName(participantDTO.getName());
        setLastname(participantDTO.getLastname());

        setId(participantDTO.getId());
        setCreatedAt(participantDTO.getCreatedAt());
        setCreatedBy(participantDTO.getCreatedBy());
        setUpdatedAt(participantDTO.getUpdatedAt());
        setUpdatedBy(participantDTO.getUpdatedBy());
    }

    @Override
    public String getIdCard() {
        return null;
    }

    @Override
    public void setIdCard(String idCard) {
        //Ignore it.
    }

    @Override
    public ClubDTO getClub() {
        return null;
    }

    @Override
    public void setClub(ClubDTO club) {
        //Ignore it.
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
        if (!(o instanceof ParticipantReducedDTO that)) {
            return false;
        }
        return getId().equals(that.getId()) && getName().equals(that.getName()) && getLastname().equals(that.getLastname());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getLastname(), getClub());
    }
}
