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

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CustomTournamentFightsException extends LoggedException {
    private static final long serialVersionUID = -1399325226733756592L;

    public CustomTournamentFightsException(Class<?> clazz, String message) {
        super(clazz, message, ExceptionType.SEVERE, HttpStatus.BAD_REQUEST);
    }

    public CustomTournamentFightsException(Class<?> clazz, String message, ExceptionType exceptionType) {
        super(clazz, message, exceptionType, HttpStatus.BAD_REQUEST);
    }
}
