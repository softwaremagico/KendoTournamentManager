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
import com.softwaremagico.kt.logger.LoggedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NotValidInputException extends LoggedException {
    private static final long serialVersionUID = -5399325226777756514L;

    public NotValidInputException(Class<?> clazz, String message) {

        super(clazz, message, ExceptionType.INFO, HttpStatus.BAD_REQUEST);
    }

    public NotValidInputException(Class<?> clazz, String message, ExceptionType exceptionType) {

        super(clazz, message, exceptionType, HttpStatus.BAD_REQUEST);
    }
}
