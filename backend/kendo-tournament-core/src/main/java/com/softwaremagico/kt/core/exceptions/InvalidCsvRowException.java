package com.softwaremagico.kt.core.exceptions;

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

import com.softwaremagico.kt.logger.ExceptionType;
import com.softwaremagico.kt.logger.LoggedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidCsvRowException extends LoggedException {

    private final int numberOfFailedRows;

    @Serial
    private static final long serialVersionUID = -980206083113568196L;

    public InvalidCsvRowException(Class<?> clazz, String message, ExceptionType type, int numberOfFailedRows) {
        super(clazz, message, type, HttpStatus.BAD_REQUEST);
        this.numberOfFailedRows = numberOfFailedRows;
    }

    public InvalidCsvRowException(Class<?> clazz, String message, int numberOfFailedRows) {
        super(clazz, message, ExceptionType.WARNING, HttpStatus.BAD_REQUEST);
        this.numberOfFailedRows = numberOfFailedRows;
    }

    public int getNumberOfFailedRows() {
        return numberOfFailedRows;
    }
}
