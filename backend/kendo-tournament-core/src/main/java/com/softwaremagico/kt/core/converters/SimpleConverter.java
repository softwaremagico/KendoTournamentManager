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

import com.softwaremagico.kt.core.converters.models.ConverterRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public abstract class SimpleConverter<ENTITY, DTO, REQUEST extends ConverterRequest<ENTITY>>
        implements IElementConverter<ENTITY, DTO, REQUEST> {

    protected abstract DTO convertElement(REQUEST from);

    public DTO convert(REQUEST from) {
        if (from == null || !from.hasEntity()) {
            return null;
        }
        return convertElement(from);
    }

    @Override
    public List<DTO> convertAll(Collection<REQUEST> from) {
        if (from == null) {
            return new ArrayList<>();
        }
        return from.stream().map(this::convert).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<ENTITY> reverseAll(Collection<DTO> to) {
        if (to == null) {
            return new ArrayList<>();
        }
        return to.stream().map(this::reverse).collect(Collectors.toCollection(ArrayList::new));
    }
}
