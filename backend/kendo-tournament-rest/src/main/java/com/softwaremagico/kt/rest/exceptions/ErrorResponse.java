package com.softwaremagico.kt.rest.exceptions;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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

import java.io.Serial;
import java.io.Serializable;

public class ErrorResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = -3296687318782961235L;
    private final Throwable cause;
    private final String message;
    private final String code;

    public ErrorResponse(String message) {
        this(message, null, null);
    }

    public ErrorResponse(String message, String code) {
        this(message, code, null);
    }

    public ErrorResponse(String message, Throwable cause) {
        this(message, null, cause);
    }

    public ErrorResponse(String message, String code, Throwable cause) {
        this.message = message;
        this.cause = cause;
        this.code = code;
    }

    public Throwable getCause() {
        return cause;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        if (code != null) {
            return code;
        }
        if (message != null) {
            return message.replaceAll(" ", "_").toLowerCase();
        }
        return null;
    }

    @Override
    public String toString() {
        return this.message;
    }
}
