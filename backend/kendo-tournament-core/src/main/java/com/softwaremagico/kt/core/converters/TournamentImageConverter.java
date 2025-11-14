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

import com.softwaremagico.kt.core.controller.models.TournamentImageDTO;
import com.softwaremagico.kt.core.converters.models.TournamentConverterRequest;
import com.softwaremagico.kt.core.converters.models.TournamentImageConverterRequest;
import com.softwaremagico.kt.persistence.entities.TournamentImage;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TournamentImageConverter extends ElementConverter<TournamentImage, TournamentImageDTO, TournamentImageConverterRequest> {
    private final TournamentConverter tournamentConverter;

    @Autowired
    public TournamentImageConverter(TournamentConverter tournamentConverter) {
        this.tournamentConverter = tournamentConverter;
    }


    @Override
    protected TournamentImageDTO convertElement(TournamentImageConverterRequest from) {
        final TournamentImageDTO tournamentImageDTO = new TournamentImageDTO();
        BeanUtils.copyProperties(from.getEntity(), tournamentImageDTO, ConverterUtils.getNullPropertyNames(from.getEntity()));
        tournamentImageDTO.setImageType(from.getEntity().getImageType());
        tournamentImageDTO.setTournament(tournamentConverter.convertElement(new TournamentConverterRequest(from.getEntity().getTournament())));
        return tournamentImageDTO;
    }

    @Override
    public TournamentImage reverse(TournamentImageDTO to) {
        if (to == null) {
            return null;
        }
        final TournamentImage tournamentImage = new TournamentImage();
        BeanUtils.copyProperties(to, tournamentImage, ConverterUtils.getNullPropertyNames(to));
        tournamentImage.setImageType(to.getImageType());
        tournamentImage.setTournament(tournamentConverter.reverse(to.getTournament()));
        return tournamentImage;
    }
}
