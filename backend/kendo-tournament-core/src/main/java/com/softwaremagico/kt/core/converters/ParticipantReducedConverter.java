package com.softwaremagico.kt.core.converters;

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

import com.softwaremagico.kt.core.controller.models.ParticipantReducedDTO;
import com.softwaremagico.kt.core.converters.models.ClubConverterRequest;
import com.softwaremagico.kt.core.converters.models.ParticipantConverterRequest;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.repositories.ClubRepository;
import org.hibernate.LazyInitializationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ParticipantReducedConverter extends ElementConverter<Participant, ParticipantReducedDTO, ParticipantConverterRequest> {
    private final ClubConverter clubConverter;
    private final ClubRepository clubRepository;

    @Autowired
    public ParticipantReducedConverter(ClubConverter clubConverter, ClubRepository clubRepository) {
        this.clubConverter = clubConverter;
        this.clubRepository = clubRepository;
    }


    @Override
    protected ParticipantReducedDTO convertElement(ParticipantConverterRequest from) {
        final ParticipantReducedDTO participantDTO = new ParticipantReducedDTO();
        BeanUtils.copyProperties(from.getEntity(), participantDTO, ConverterUtils.getNullPropertyNames(from.getEntity()));

        try {
            if (from.getClub() != null) {
                participantDTO.setClub(from.getClubDTO());
            } else {
                participantDTO.setClub(clubConverter.convert(new ClubConverterRequest(from.getEntity().getClub())));
            }
        } catch (LazyInitializationException | FatalBeanException e) {
            participantDTO.setClub(clubConverter.convert(
                    new ClubConverterRequest(clubRepository.findById(from.getEntity().getClub().getId()).orElse(null))));
        }
        return participantDTO;
    }

    @Override
    public Participant reverse(ParticipantReducedDTO to) {
        if (to == null) {
            return null;
        }
        final Participant participant = new Participant();
        BeanUtils.copyProperties(to, participant, ConverterUtils.getNullPropertyNames(to));
        participant.setClub(clubConverter.reverse(to.getClub()));
        return participant;
    }
}
