package com.softwaremagico.kt.logger;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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

import org.springframework.http.HttpStatus;

public abstract class LoggedHttpException extends RuntimeException {
    private static final long serialVersionUID = -2118048384077287599L;
    private HttpStatus status;

    protected LoggedHttpException(Class<?> clazz, String message, ExceptionType type, HttpStatus status) {
        super(message);
        this.status = status;
        final String className = clazz.getName();
        switch (type) {
            case INFO:
                RestServerLogger.info(className, message);
                break;
            case WARNING:
                RestServerLogger.warning(className, message);
                break;
            case SEVERE:
                RestServerLogger.severe(className, message);
                break;
            default:
                RestServerLogger.debug(className, message);
                break;
        }
    }

    protected LoggedHttpException(Class<?> clazz, Throwable e, HttpStatus status) {
        this(clazz, e);
        this.status = status;
    }

    protected LoggedHttpException(Class<?> clazz, String message, Throwable e, HttpStatus status) {
        this(clazz, message, e);
        this.status = status;
    }

    public LoggedHttpException(Class<?> clazz, Throwable e) {
        super(e);
        RestServerLogger.errorMessage(clazz, e);
    }

    protected LoggedHttpException(Class<?> clazz, String message, Throwable e) {
        super(message, e);
        RestServerLogger.errorMessage(clazz, e);
    }

    public HttpStatus getStatus() {
        return status;
    }
}
