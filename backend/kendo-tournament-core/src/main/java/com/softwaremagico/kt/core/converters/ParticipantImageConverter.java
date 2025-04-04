package com.softwaremagico.kt.core.converters;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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

import com.softwaremagico.kt.core.controller.models.ParticipantImageDTO;
import com.softwaremagico.kt.core.converters.models.ParticipantConverterRequest;
import com.softwaremagico.kt.core.converters.models.ParticipantImageConverterRequest;
import com.softwaremagico.kt.persistence.entities.ParticipantImage;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ParticipantImageConverter extends ElementConverter<ParticipantImage, ParticipantImageDTO, ParticipantImageConverterRequest> {
    private final ParticipantConverter participantConverter;
    private final ParticipantReducedConverter participantReducedConverter;

    @Autowired
    public ParticipantImageConverter(ParticipantConverter participantConverter, ParticipantReducedConverter participantReducedConverter) {
        this.participantConverter = participantConverter;
        this.participantReducedConverter = participantReducedConverter;
    }


    @Override
    protected ParticipantImageDTO convertElement(ParticipantImageConverterRequest from) {
        final ParticipantImageDTO participantImageDTO = new ParticipantImageDTO();
        BeanUtils.copyProperties(from.getEntity(), participantImageDTO, ConverterUtils.getNullPropertyNames(from.getEntity()));
        participantImageDTO.setImageFormat(from.getEntity().getImageFormat());
        participantImageDTO.setParticipant(participantReducedConverter.convertElement(new ParticipantConverterRequest(from.getEntity().getParticipant())));
        return participantImageDTO;
    }

    @Override
    public ParticipantImage reverse(ParticipantImageDTO to) {
        if (to == null) {
            return null;
        }
        final ParticipantImage participantImage = new ParticipantImage();
        BeanUtils.copyProperties(to, participantImage, ConverterUtils.getNullPropertyNames(to));
        participantImage.setImageFormat(to.getImageFormat());
        participantImage.setParticipant(participantConverter.reverse(to.getParticipant()));
        return participantImage;
    }
}
