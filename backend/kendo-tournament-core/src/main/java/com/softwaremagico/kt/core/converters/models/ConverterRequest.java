package com.softwaremagico.kt.core.converters.models;

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

import com.softwaremagico.kt.core.exceptions.UnexpectedValueException;
import com.softwaremagico.kt.logger.ExceptionType;

import java.util.Optional;

public class ConverterRequest<T> {
    private T entity;

    public ConverterRequest(T entity) {
        this.entity = entity;
    }

    public ConverterRequest(Optional<T> entity) {
        entity.ifPresent(ent -> this.entity = ent);
    }

    public boolean hasEntity() {
        return entity != null;
    }

    public T getEntity() {
        if (entity == null) {
            throw new UnexpectedValueException(this.getClass(), "Entity could not be converted into a proper object.", ExceptionType.WARNING);
        }
        return entity;
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }

    public T getEntityWithoutChecks() {
        return entity;
    }
}
