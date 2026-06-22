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
import com.softwaremagico.kt.core.exceptions.NotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static org.testng.Assert.assertEquals;
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
    public void shouldHandleMethodArgumentNotValidAndSkipNullMessages() throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new DummyDto(), "dto");
        bindingResult.addError(new FieldError("dto", "username", "must not be blank"));
        bindingResult.addError(new FieldError("dto", "email", null));
        Method method = DummyController.class.getDeclaredMethod("setDto", DummyDto.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException validationException = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<Object> response = advice.callHandleMethodArgumentNotValid(validationException, HttpHeaders.EMPTY, HttpStatus.BAD_REQUEST, null);

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
        ErrorResponse body = (ErrorResponse) response.getBody();
        assertEquals(body.getCode(), "input_data_is_invalid");
        assertTrue(body.getMessage().contains("username=must not be blank"));
        assertTrue(!body.getMessage().contains("email="));
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


