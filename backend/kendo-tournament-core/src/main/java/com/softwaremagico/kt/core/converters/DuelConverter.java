package com.softwaremagico.kt.core.converters;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2022 Softwaremagico
 * %%
 * This software is designed by Jorge Hortelano Otero. Jorge Hortelano Otero
 * <softwaremagico@gmail.com> Valencia (Spain).
 *  
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *  
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.softwaremagico.kt.core.controller.models.DuelDTO;
import com.softwaremagico.kt.core.converters.models.DuelConverterRequest;
import com.softwaremagico.kt.core.converters.models.ParticipantConverterRequest;
import com.softwaremagico.kt.persistence.entities.Duel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DuelConverter extends ElementConverter<Duel, DuelDTO, DuelConverterRequest> {
    private final ParticipantConverter participantConverter;

    @Autowired
    public DuelConverter(ParticipantConverter participantConverter) {
        this.participantConverter = participantConverter;
    }


    @Override
    public DuelDTO convert(DuelConverterRequest from) {
        final DuelDTO duelDTO = new DuelDTO();
        BeanUtils.copyProperties(from.getEntity(), duelDTO);
        duelDTO.setCompetitor1(participantConverter.convert(
                new ParticipantConverterRequest(from.getEntity().getCompetitor1())));
        duelDTO.setCompetitor2(participantConverter.convert(
                new ParticipantConverterRequest(from.getEntity().getCompetitor2())));
        return duelDTO;
    }

    @Override
    public Duel reverse(DuelDTO to) {
        if (to == null) {
            return null;
        }
        final Duel duel = new Duel();
        BeanUtils.copyProperties(to, duel);
        duel.setCompetitor1(participantConverter.reverse(to.getCompetitor1()));
        duel.setCompetitor2(participantConverter.reverse(to.getCompetitor2()));
        return duel;
    }
}
