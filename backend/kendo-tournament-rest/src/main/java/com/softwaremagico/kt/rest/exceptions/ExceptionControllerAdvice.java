package com.softwaremagico.kt.rest.exceptions;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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

import com.softwaremagico.kt.core.exceptions.InvalidChallengeDistanceException;
import com.softwaremagico.kt.core.exceptions.InvalidCsvFieldException;
import com.softwaremagico.kt.core.exceptions.InvalidCsvRowException;
import com.softwaremagico.kt.core.exceptions.InvalidFightException;
import com.softwaremagico.kt.core.exceptions.InvalidGroupException;
import com.softwaremagico.kt.core.exceptions.LevelNotFinishedException;
import com.softwaremagico.kt.core.exceptions.NoContentException;
import com.softwaremagico.kt.core.exceptions.NotFoundException;
import com.softwaremagico.kt.core.exceptions.TokenExpiredException;
import com.softwaremagico.kt.logger.RestServerExceptionLogger;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ExceptionControllerAdvice extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        RestServerExceptionLogger.errorMessage(this.getClass(), ex);
        return new ResponseEntity<>(new ErrorResponse("MESSAGE_NOT_READABLE", ex), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Object> handleNullPointer(NullPointerException ex) {
        RestServerExceptionLogger.errorMessage(this.getClass(), ex);
        return new ResponseEntity<>(new ErrorResponse("INTERNAL_SERVER_ERROR", ex), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        RestServerExceptionLogger.errorMessage(this.getClass(), ex);
        return new ResponseEntity<>(new ErrorResponse("BAD_REQUEST", ex), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> unknownException(Exception ex) {
        RestServerExceptionLogger.errorMessage(this.getClass().getName(), ex);
        return new ResponseEntity<>(new ErrorResponse("INTERNAL_SERVER_ERROR", ex), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> badRequestException(Exception ex) {
        RestServerExceptionLogger.errorMessage(this.getClass().getName(), ex);
        return new ResponseEntity<>(new ErrorResponse("BAD_REQUEST", ex), HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatusCode status,
            WebRequest request) {
        RestServerExceptionLogger.errorMessage(this.getClass(), ex);
        return new ResponseEntity<>(new ErrorResponse("METHOD_NOT_ALLOWED", ex), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatusCode status,
            WebRequest request) {
        RestServerExceptionLogger.errorMessage(this.getClass(), ex);
        return new ResponseEntity<>(new ErrorResponse("UNSUPPORTED_MEDIA_TYPE", ex), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> accessDeniedException(Exception ex) {
        RestServerExceptionLogger.errorMessage(this.getClass().getName(), ex);
        return new ResponseEntity<>(new ErrorResponse("INVALID CREDENTIALS", ex), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> notFoundException(Exception ex) {
        RestServerExceptionLogger.errorMessage(this.getClass().getName(), ex);
        //Generated on this way, as endpoints that produces multiples formats (i.e. produces = {MediaType.APPLICATION_PDF_VALUE,
        // MediaType.APPLICATION_JSON_VALUE}) must specify that the exception is JSON.
        return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse(ex.getMessage(), "not_found"));
    }

    @ExceptionHandler(NoContentException.class)
    public ResponseEntity<Object> noContent(Exception ex) {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @ExceptionHandler(UserBlockedException.class)
    public ResponseEntity<Object> userBlockedException(Exception ex) {
        RestServerExceptionLogger.errorMessage(this.getClass().getName(), ex);
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), "user_blocked", ex), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(LevelNotFinishedException.class)
    public ResponseEntity<Object> levelNotFinishedException(Exception ex) {
        RestServerExceptionLogger.errorMessage(this.getClass().getName(), ex);
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), "level_not_finished", ex), HttpStatus.NO_CONTENT);
    }

    @ExceptionHandler(InvalidMacException.class)
    public ResponseEntity<Object> invalidMacException(Exception ex) {
        RestServerExceptionLogger.errorMessage(this.getClass().getName(), ex);
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), "invalid_mac", ex), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidIpException.class)
    public ResponseEntity<Object> invalidIpException(Exception ex) {
        RestServerExceptionLogger.errorMessage(this.getClass().getName(), ex);
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), "invalid_ip", ex), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidJwtException.class)
    public ResponseEntity<Object> invalidJwtException(Exception ex) {
        RestServerExceptionLogger.errorMessage(this.getClass().getName(), ex);
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), "invalid_jwt", ex), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(GuestDisabledException.class)
    public ResponseEntity<Object> guestDisabledException(Exception ex) {
        RestServerExceptionLogger.errorMessage(this.getClass().getName(), ex);
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), "guest_disabled", ex), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<Object> tokenExpiredException(Exception ex) {
        RestServerExceptionLogger.errorMessage(this.getClass().getName(), ex);
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), "token_expired", ex), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<Object> invalidRequestException(Exception ex) {
        RestServerExceptionLogger.errorMessage(this.getClass().getName(), ex);
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), "invalid_request", ex), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidGroupException.class)
    public ResponseEntity<Object> invalidGroupException(Exception ex) {
        RestServerExceptionLogger.errorMessage(this.getClass().getName(), ex);
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), "invalid_group", ex), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidChallengeDistanceException.class)
    public ResponseEntity<Object> invalidChallengeDistanceException(Exception ex) {
        RestServerExceptionLogger.errorMessage(this.getClass().getName(), ex);
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), "invalid_challenge_distance", ex), HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(InvalidFightException.class)
    public ResponseEntity<Object> invalidFightException(Exception ex) {
        RestServerExceptionLogger.errorMessage(this.getClass().getName(), ex);
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), "invalid_fight", ex), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidCsvFieldException.class)
    public ResponseEntity<Object> invalidCsvFieldException(Exception ex) {
        RestServerExceptionLogger.errorMessage(this.getClass().getName(), ex);
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), "Invalid field: " + ((InvalidCsvFieldException) ex).getHeader(), ex),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidCsvRowException.class)
    public ResponseEntity<Object> invalidCsvRowException(Exception ex) {
        RestServerExceptionLogger.errorMessage(this.getClass().getName(), ex);
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), "Elements failed: " + ((InvalidCsvRowException) ex).getNumberOfFailedRows(), ex),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Object> invalidPasswordException(Exception ex) {
        RestServerExceptionLogger.errorMessage(this.getClass().getName(), ex);
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), "invalid_password", ex), HttpStatus.BAD_REQUEST);
    }


    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            final MethodArgumentNotValidException ex, @NotNull final HttpHeaders headers, @NotNull final HttpStatusCode status,
            @NotNull final WebRequest request) {
        final Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            final String fieldName = error.getField();
            final String errorMessage = error.getDefaultMessage();
            if (errorMessage != null) {
                errors.put(fieldName, errorMessage);
            }
        });

        return new ResponseEntity<>(new ErrorResponse(errors.toString(), "input_data_is_invalid"), HttpStatus.BAD_REQUEST);
    }
}
