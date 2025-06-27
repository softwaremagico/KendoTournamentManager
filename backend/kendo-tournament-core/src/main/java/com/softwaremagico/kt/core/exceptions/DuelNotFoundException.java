package com.softwaremagico.kt.core.exceptions;

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

import com.softwaremagico.kt.logger.ExceptionType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class DuelNotFoundException extends NotFoundException {
    private static final long serialVersionUID = 3392553855955576861L;

    public DuelNotFoundException(Class<?> clazz, String message, ExceptionType type) {
        super(clazz, message, type);
    }

    public DuelNotFoundException(Class<?> clazz, String message) {
        super(clazz, message, ExceptionType.WARNING);
    }

    public DuelNotFoundException(Class<?> clazz) {
        this(clazz, "Duel not found");
    }

    public DuelNotFoundException(Class<?> clazz, Throwable e) {
        super(clazz, e);
    }
}
