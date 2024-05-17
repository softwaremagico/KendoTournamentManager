package com.softwaremagico.kt.core.converters;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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

import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.core.converters.models.ClubConverterRequest;
import com.softwaremagico.kt.persistence.entities.Club;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class ClubConverter extends ElementConverter<Club, ClubDTO, ClubConverterRequest> {

    @Override
    protected ClubDTO convertElement(ClubConverterRequest from) {
        final ClubDTO clubDTO = new ClubDTO();
        BeanUtils.copyProperties(from.getEntity(), clubDTO);
        return clubDTO;
    }

    @Override
    public Club reverse(ClubDTO to) {
        if (to == null) {
            return null;
        }
        final Club club = new Club();
        BeanUtils.copyProperties(to, club);
        return club;
    }
}
