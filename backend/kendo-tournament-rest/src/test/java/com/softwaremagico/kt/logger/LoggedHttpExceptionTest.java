package com.softwaremagico.kt.logger;

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

import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

@Test(groups = "loggedHttpException")
public class LoggedHttpExceptionTest {

    private static final class TestLoggedHttpException extends LoggedHttpException {
        TestLoggedHttpException(Class<?> clazz, String message, ExceptionType type, HttpStatus status) {
            super(clazz, message, type, status);
        }

        TestLoggedHttpException(Class<?> clazz, Throwable e, HttpStatus status) {
            super(clazz, e, status);
        }

        TestLoggedHttpException(Class<?> clazz, String message, Throwable e, HttpStatus status) {
            super(clazz, message, e, status);
        }

        TestLoggedHttpException(Class<?> clazz, Throwable e) {
            super(clazz, e);
        }

        TestLoggedHttpException(Class<?> clazz, String message, Throwable e) {
            super(clazz, message, e);
        }
    }

    @Test
    public void constructor_withMessageAndInfoType_expectStatusSet() {
        final TestLoggedHttpException exception =
                new TestLoggedHttpException(getClass(), "info message", ExceptionType.INFO, HttpStatus.OK);
        assertEquals(exception.getStatus(), HttpStatus.OK);
        assertEquals(exception.getMessage(), "info message");
    }

    @Test
    public void constructor_withMessageAndWarningType_expectStatusSet() {
        final TestLoggedHttpException exception =
                new TestLoggedHttpException(getClass(), "warning message", ExceptionType.WARNING, HttpStatus.BAD_REQUEST);
        assertEquals(exception.getStatus(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void constructor_withMessageAndSevereType_expectStatusSet() {
        final TestLoggedHttpException exception =
                new TestLoggedHttpException(getClass(), "severe message", ExceptionType.SEVERE, HttpStatus.INTERNAL_SERVER_ERROR);
        assertEquals(exception.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void constructor_withMessageAndDebugType_expectStatusSet() {
        final TestLoggedHttpException exception =
                new TestLoggedHttpException(getClass(), "debug message", ExceptionType.DEBUG, HttpStatus.NOT_FOUND);
        assertEquals(exception.getStatus(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void constructor_withThrowableAndStatus_expectStatusSetAndCauseWrapped() {
        final RuntimeException cause = new RuntimeException("cause");
        final TestLoggedHttpException exception = new TestLoggedHttpException(getClass(), cause, HttpStatus.CONFLICT);
        assertEquals(exception.getStatus(), HttpStatus.CONFLICT);
        assertSame(exception.getCause(), cause);
    }

    @Test
    public void constructor_withMessageThrowableAndStatus_expectStatusSetAndCauseWrapped() {
        final RuntimeException cause = new RuntimeException("cause");
        final TestLoggedHttpException exception =
                new TestLoggedHttpException(getClass(), "wrapping message", cause, HttpStatus.FORBIDDEN);
        assertEquals(exception.getStatus(), HttpStatus.FORBIDDEN);
        assertEquals(exception.getMessage(), "wrapping message");
        assertSame(exception.getCause(), cause);
    }

    @Test
    public void constructor_withThrowableOnly_expectInternalServerErrorStatus() {
        final RuntimeException cause = new RuntimeException("cause");
        final TestLoggedHttpException exception = new TestLoggedHttpException(getClass(), cause);
        assertEquals(exception.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR);
        assertSame(exception.getCause(), cause);
    }

    @Test
    public void constructor_withMessageAndThrowableOnly_expectInternalServerErrorStatus() {
        final RuntimeException cause = new RuntimeException("cause");
        final TestLoggedHttpException exception = new TestLoggedHttpException(getClass(), "message", cause);
        assertEquals(exception.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR);
        assertEquals(exception.getMessage(), "message");
        assertSame(exception.getCause(), cause);
    }
}

