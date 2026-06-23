package com.softwaremagico.kt.rest.exceptions;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
 * %%
 * Copyright (C) 2021 - 2026 SoftwareMagico
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

import com.softwaremagico.kt.core.exceptions.InvalidCsvFieldException;
import com.softwaremagico.kt.core.exceptions.InvalidCsvRowException;
import com.softwaremagico.kt.core.exceptions.InvalidFightException;
import com.softwaremagico.kt.core.exceptions.InvalidGroupException;
import com.softwaremagico.kt.core.exceptions.LevelNotFinishedException;
import com.softwaremagico.kt.core.exceptions.NotFoundException;
import com.softwaremagico.kt.core.exceptions.TokenExpiredException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class ExceptionControllerAdviceTest {

    private ExposedExceptionControllerAdvice advice;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        advice = new ExposedExceptionControllerAdvice();
    }

    @Test(groups = "exceptionAdvice")
    public void shouldHandleNullPointerException() {
        ResponseEntity<Object> response = advice.handleNullPointer(new NullPointerException("npe"));

        assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
        ErrorResponse body = (ErrorResponse) response.getBody();
        assertEquals(body.getCode(), "internal_server_error");
    }

    @Test(groups = "exceptionAdvice")
    public void shouldHandleIllegalArgumentException() {
        ResponseEntity<Object> response = advice.handleIllegalArgumentException(new IllegalArgumentException("bad"));

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
        ErrorResponse body = (ErrorResponse) response.getBody();
        assertEquals(body.getCode(), "bad_request");
    }

    @Test(groups = "exceptionAdvice")
    public void shouldHandleNotFoundExceptionWithJsonContentType() {
        ResponseEntity<Object> response = advice.notFoundException(new NotFoundException(getClass(), "missing"));

        assertEquals(response.getStatusCode(), HttpStatus.NOT_FOUND);
        assertEquals(response.getHeaders().getContentType(), org.springframework.http.MediaType.APPLICATION_JSON);
        ErrorResponse body = (ErrorResponse) response.getBody();
        assertEquals(body.getCode(), "not_found");
    }

    @Test(groups = "exceptionAdvice")
    public void shouldHandleNoContentException() {
        ResponseEntity<Object> response = advice.noContent(new RuntimeException("ignored"));

        assertEquals(response.getStatusCode(), HttpStatus.NO_CONTENT);
        assertNull(response.getBody());
    }

    @Test(groups = "exceptionAdvice")
    public void shouldHandleInvalidCsvFieldException() {
        InvalidCsvFieldException exception = new InvalidCsvFieldException(getClass(), "invalid", "name");

        ResponseEntity<Object> response = advice.invalidCsvFieldException(exception);

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
        ErrorResponse body = (ErrorResponse) response.getBody();
        assertEquals(body.getCode(), "Invalid field: name");
    }

    @Test(groups = "exceptionAdvice")
    public void shouldHandleInvalidCsvRowException() {
        InvalidCsvRowException exception = new InvalidCsvRowException(getClass(), "invalid", 3);

        ResponseEntity<Object> response = advice.invalidCsvRowException(exception);

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
        ErrorResponse body = (ErrorResponse) response.getBody();
        assertEquals(body.getCode(), "Elements failed: 3");
    }

    @Test(groups = "exceptionAdvice")
    public void shouldHandleHttpMessageNotReadable() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("malformed");

        ResponseEntity<Object> response = advice.callHandleHttpMessageNotReadable(exception, HttpHeaders.EMPTY, HttpStatus.BAD_REQUEST, null);

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
        ErrorResponse body = (ErrorResponse) response.getBody();
        assertEquals(body.getCode(), "message_not_readable");
    }

    @Test(groups = "exceptionAdvice")
    public void shouldHandleMethodArgumentNotValidAndSkipNullMessages() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new DummyDto(), "dto");
        bindingResult.addError(new FieldError("dto", "username", "must not be blank"));
        bindingResult.addError(new FieldError("dto", "email", null));
        Method method = DummyController.class.getDeclaredMethods()[0];
        MethodParameter methodParameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException validationException = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<Object> response = advice.callHandleMethodArgumentNotValid(validationException, HttpHeaders.EMPTY, HttpStatus.BAD_REQUEST, null);

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
        ErrorResponse body = (ErrorResponse) response.getBody();
        assertEquals(body.getCode(), "input_data_is_invalid");
        assertTrue(body.getMessage().contains("username=must not be blank"));
        assertFalse(body.getMessage().contains("email="));
    }

    @Test(groups = "exceptionAdvice")
    public void shouldHandleUnknownException() {
        ResponseEntity<Object> response = advice.unknownException(new RuntimeException("boom"));

        assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
        ErrorResponse body = (ErrorResponse) response.getBody();
        assertEquals(body.getCode(), "internal_server_error");
    }

    @Test(groups = "exceptionAdvice")
    public void shouldHandleBadRequestException() {
        ResponseEntity<Object> response = advice.badRequestException(new BadRequestException(getClass(), "bad request"));

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
        ErrorResponse body = (ErrorResponse) response.getBody();
        assertEquals(body.getCode(), "bad_request");
    }

    @Test(groups = "exceptionAdvice")
    public void shouldHandleAccessDeniedException() {
        ResponseEntity<Object> response = advice.accessDeniedException(new AccessDeniedException("denied"));

        assertEquals(response.getStatusCode(), HttpStatus.UNAUTHORIZED);
        ErrorResponse body = (ErrorResponse) response.getBody();
        assertEquals(body.getCode(), "invalid_credentials");
    }

    @Test(groups = "exceptionAdvice")
    public void shouldHandleUserBlockedException() {
        ResponseEntity<Object> response = advice.userBlockedException(new UserBlockedException(getClass(), "blocked"));

        assertEquals(response.getStatusCode(), HttpStatus.UNAUTHORIZED);
        ErrorResponse body = (ErrorResponse) response.getBody();
        assertEquals(body.getCode(), "user_blocked");
    }

    @Test(groups = "exceptionAdvice")
    public void shouldHandleLevelNotFinishedException() {
        ResponseEntity<Object> response = advice.levelNotFinishedException(new LevelNotFinishedException(getClass(), "level not finished"));

        assertEquals(response.getStatusCode(), HttpStatus.NO_CONTENT);
        ErrorResponse body = (ErrorResponse) response.getBody();
        assertEquals(body.getCode(), "level_not_finished");
    }

    @Test(groups = "exceptionAdvice")
    public void shouldHandleInvalidMacException() {
        ResponseEntity<Object> response = advice.invalidMacException(new InvalidMacException(getClass(), "invalid mac"));

        assertEquals(response.getStatusCode(), HttpStatus.CONFLICT);
        ErrorResponse body = (ErrorResponse) response.getBody();
        assertEquals(body.getCode(), "invalid_mac");
    }

    @Test(groups = "exceptionAdvice")
    public void shouldHandleInvalidIpException() {
        ResponseEntity<Object> response = advice.invalidIpException(new InvalidIpException(getClass(), "invalid ip"));

        assertEquals(response.getStatusCode(), HttpStatus.UNAUTHORIZED);
        ErrorResponse body = (ErrorResponse) response.getBody();
        assertEquals(body.getCode(), "invalid_ip");
    }

    @Test(groups = "exceptionAdvice")
    public void shouldHandleInvalidJwtException() {
        ResponseEntity<Object> response = advice.invalidJwtException(new InvalidJwtException(getClass(), "invalid jwt"));

        assertEquals(response.getStatusCode(), HttpStatus.UNAUTHORIZED);
        ErrorResponse body = (ErrorResponse) response.getBody();
        assertEquals(body.getCode(), "invalid_jwt");
    }

    @Test(groups = "exceptionAdvice")
    public void shouldHandleGuestDisabledException() {
        ResponseEntity<Object> response = advice.guestDisabledException(new GuestDisabledException(getClass(), "guest disabled"));

        assertEquals(response.getStatusCode(), HttpStatus.UNAUTHORIZED);
        ErrorResponse body = (ErrorResponse) response.getBody();
        assertEquals(body.getCode(), "guest_disabled");
    }

    @Test(groups = "exceptionAdvice")
    public void shouldHandleTokenExpiredException() {
        ResponseEntity<Object> response = advice.tokenExpiredException(new TokenExpiredException(getClass(), "expired"));

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
        ErrorResponse body = (ErrorResponse) response.getBody();
        assertEquals(body.getCode(), "token_expired");
    }

    @Test(groups = "exceptionAdvice")
    public void shouldHandleInvalidRequestException() {
        ResponseEntity<Object> response = advice.invalidRequestException(new InvalidRequestException(getClass(), "invalid request"));

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
        ErrorResponse body = (ErrorResponse) response.getBody();
        assertEquals(body.getCode(), "invalid_request");
    }

    @Test(groups = "exceptionAdvice")
    public void shouldHandleInvalidGroupException() {
        ResponseEntity<Object> response = advice.invalidGroupException(new InvalidGroupException(getClass(), "invalid group"));

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
        ErrorResponse body = (ErrorResponse) response.getBody();
        assertEquals(body.getCode(), "invalid_group");
    }

    @Test(groups = "exceptionAdvice")
    public void shouldHandleInvalidFightException() {
        ResponseEntity<Object> response = advice.invalidFightException(new InvalidFightException(getClass(), "invalid fight"));

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
        ErrorResponse body = (ErrorResponse) response.getBody();
        assertEquals(body.getCode(), "invalid_fight");
    }

    @Test(groups = "exceptionAdvice")
    public void shouldHandleInvalidPasswordException() {
        ResponseEntity<Object> response = advice.invalidPasswordException(new InvalidPasswordException(getClass(), "invalid password"));

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
        ErrorResponse body = (ErrorResponse) response.getBody();
        assertEquals(body.getCode(), "invalid_password");
    }

    private static class DummyDto {
    }

    private static class DummyController {
        public void setDto(DummyDto dto) {
            // Helper method to build MethodParameter for MethodArgumentNotValidException.
        }
    }

    private static class ExposedExceptionControllerAdvice extends ExceptionControllerAdvice {
        public ResponseEntity<Object> callHandleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                        HttpHeaders headers,
                                                                        HttpStatus status,
                                                                        org.springframework.web.context.request.WebRequest request) {
            return super.handleHttpMessageNotReadable(ex, headers, status, request);
        }

        public ResponseEntity<Object> callHandleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                        HttpHeaders headers,
                                                                        HttpStatus status,
                                                                        org.springframework.web.context.request.WebRequest request) {
            return super.handleMethodArgumentNotValid(ex, headers, status, request);
        }
    }
}


