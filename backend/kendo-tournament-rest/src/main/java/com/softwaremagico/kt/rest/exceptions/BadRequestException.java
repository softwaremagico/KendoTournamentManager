package com.softwaremagico.kt.rest.exceptions;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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

import com.softwaremagico.kt.logger.ExceptionType;
import com.softwaremagico.kt.logger.LoggedHttpException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BadRequestException extends LoggedHttpException {
    private static final long serialVersionUID = 7032994901678894370L;

    public BadRequestException(Class<?> clazz, String message, ExceptionType type) {
        super(clazz, message, type, HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(Class<?> clazz, String message) {
        super(clazz, message, ExceptionType.WARNING, HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(Class<?> clazz) {
        this(clazz, "Invalid parameters");
    }

    public BadRequestException(Class<?> clazz, Throwable e) {
        super(clazz, e);
    }
}
