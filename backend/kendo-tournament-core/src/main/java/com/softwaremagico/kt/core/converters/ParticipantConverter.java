package com.softwaremagico.kt.core.converters;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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

import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.converters.models.ClubConverterRequest;
import com.softwaremagico.kt.core.converters.models.ParticipantConverterRequest;
import com.softwaremagico.kt.persistence.entities.Participant;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ParticipantConverter extends ElementConverter<Participant, ParticipantDTO, ParticipantConverterRequest> {
    private final ClubConverter clubConverter;

    @Autowired
    public ParticipantConverter(ClubConverter clubConverter) {
        this.clubConverter = clubConverter;
    }


    @Override
    protected ParticipantDTO convertElement(ParticipantConverterRequest from) {
        final ParticipantDTO participantDTO = new ParticipantDTO();
        BeanUtils.copyProperties(from.getEntity(), participantDTO, ConverterUtils.getNullPropertyNames(from.getEntity()));
        participantDTO.setClub(clubConverter.convert(new ClubConverterRequest(from.getEntity().getClub())));
        return participantDTO;
    }

    @Override
    public Participant reverse(ParticipantDTO to) {
        if (to == null) {
            return null;
        }
        final Participant participant = new Participant();
        BeanUtils.copyProperties(to, participant, ConverterUtils.getNullPropertyNames(to));
        participant.setClub(clubConverter.reverse(to.getClub()));
        return participant;
    }
}
