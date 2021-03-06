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

import com.softwaremagico.kt.core.converters.models.ConverterRequest;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ElementConverter<F, T, R extends ConverterRequest<F>> implements IElementConverter<F, T, R> {

    @Override
    public List<T> convertAll(Collection<R> from) {
        return from.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public List<F> reverseAll(Collection<T> to) {
        return to.stream().map(this::reverse).collect(Collectors.toList());
    }
}
