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

import com.softwaremagico.kt.core.exceptions.NotFoundException;
import com.softwaremagico.kt.logger.RestServerExceptionLogger;
import org.modelmapper.spi.ErrorMessage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.PrintWriter;
import java.io.StringWriter;

@ControllerAdvice
public class ExceptionControllerAdvice extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status,
            WebRequest request) {
        RestServerExceptionLogger.errorMessage(this.getClass(), ex);
        return new ResponseEntity<>(new ErrorMessage("MESSAGE_NOT_READABLE", ex), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Object> handleNullPointer(NullPointerException ex) {
        RestServerExceptionLogger.errorMessage(this.getClass(), ex);
        return new ResponseEntity<>(new ErrorMessage("INTERNAL_SERVER_ERROR", ex), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        RestServerExceptionLogger.errorMessage(this.getClass(), ex);
        return new ResponseEntity<>(new ErrorMessage("BAD_REQUEST", ex), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> unknownException(Exception ex) {
        RestServerExceptionLogger.errorMessage(this.getClass().getName(), ex);
        return new ResponseEntity<>(new ErrorMessage("INTERNAL_SERVER_ERROR", ex), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> badRequestException(Exception ex) {
        RestServerExceptionLogger.errorMessage(this.getClass().getName(), ex);
        return new ResponseEntity<>(new ErrorMessage("BAD_REQUEST", ex), HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatus status,
            WebRequest request) {
        RestServerExceptionLogger.errorMessage(this.getClass(), ex);
        return new ResponseEntity<>(new ErrorMessage("METHOD_NOT_ALLOWED", ex), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatus status,
            WebRequest request) {
        RestServerExceptionLogger.errorMessage(this.getClass(), ex);
        return new ResponseEntity<>(new ErrorMessage("UNSUPPORTED_MEDIA_TYPE", ex), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> accessDeniedException(Exception ex) {
        RestServerExceptionLogger.errorMessage(this.getClass().getName(), ex);
        return new ResponseEntity<>(new ErrorMessage("INVALID CREDENTIALS", ex), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> notFoundException(Exception ex) {
        RestServerExceptionLogger.errorMessage(this.getClass().getName(), ex);
        return new ResponseEntity<>(new ErrorMessage("NOT_FOUND", ex), HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(UserBlockedException.class)
    public ResponseEntity<Object> userBlockedException(Exception ex) {
        RestServerExceptionLogger.errorMessage(this.getClass().getName(), ex);
        return new ResponseEntity<>(new ErrorMessage("USER BLOCKED", ex), HttpStatus.UNAUTHORIZED);
    }

    private String getStacktrace(Throwable e) {
        try {
            final StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            return sw.toString();
        } catch (Exception ex) {
            return "";
        }
    }
}
