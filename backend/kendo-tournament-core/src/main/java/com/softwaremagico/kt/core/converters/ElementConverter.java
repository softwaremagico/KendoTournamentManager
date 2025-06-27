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

import com.softwaremagico.kt.core.controller.models.ElementDTO;
import com.softwaremagico.kt.core.converters.models.ConverterRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ElementConverter<ENTITY, DTO extends ElementDTO, REQUEST extends ConverterRequest<ENTITY>>
        extends SimpleConverter<ENTITY, DTO, REQUEST> implements IElementConverter<ENTITY, DTO, REQUEST> {

    protected abstract DTO convertElement(REQUEST from);

    @Override
    public List<DTO> convertAll(Collection<REQUEST> from) {
        if (from == null) {
            return new ArrayList<>();
        }
        //Returns the DTOs sorted by creation time by default
        return from.stream().map(this::convert).sorted(Comparator.comparing(ElementDTO::getCreatedAt,
                Comparator.nullsFirst(Comparator.naturalOrder()))).collect(Collectors.toList());
    }

    public List<DTO> convertAllNotSorted(Collection<REQUEST> from) {
        if (from == null) {
            return new ArrayList<>();
        }
        //Returns the DTOs sorted by creation time by default
        return from.stream().map(this::convert).collect(Collectors.toList());
    }
}
