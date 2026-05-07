package com.softwaremagico.kt.logger.tests;

/*-
 * #%L
 * Kendo Tournament Manager (Logger)
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

import com.softwaremagico.kt.logger.ExceptionType;
import com.softwaremagico.kt.logger.LoggedException;
import org.springframework.http.HttpStatus;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class LoggedExceptionTest {

    private static final class TestLoggedException extends LoggedException {
        private TestLoggedException(Class<?> clazz, String message, ExceptionType type, HttpStatus status) {
            super(clazz, message, type, status);
        }

        private TestLoggedException(Class<?> clazz, Throwable e, HttpStatus status) {
            super(clazz, e, status);
        }

        private TestLoggedException(Class<?> clazz, Throwable e) {
            super(clazz, e);
        }
    }

    @DataProvider
    public Object[][] types() {
        return new Object[][]{
                {ExceptionType.DEBUG},
                {ExceptionType.INFO},
                {ExceptionType.WARNING},
                {ExceptionType.SEVERE}
        };
    }

    @Test(groups = "loggedExceptionTests", dataProvider = "types")
    public void shouldKeepStatusForEachExceptionType(ExceptionType type) {
        TestLoggedException exception = new TestLoggedException(getClass(), "msg", type, HttpStatus.BAD_REQUEST);

        assertEquals(exception.getStatus(), HttpStatus.BAD_REQUEST);
        assertEquals(exception.getMessage(), "msg");
        assertTrue(exception instanceof RuntimeException);
    }

    @Test(groups = "loggedExceptionTests")
    public void shouldKeepCauseAndExplicitStatus() {
        Throwable cause = new IllegalStateException("cause");

        TestLoggedException exception = new TestLoggedException(getClass(), cause, HttpStatus.CONFLICT);

        assertEquals(exception.getCause(), cause);
        assertEquals(exception.getStatus(), HttpStatus.CONFLICT);
    }

    @Test(groups = "loggedExceptionTests")
    public void shouldUseInternalServerErrorWhenStatusNotProvided() {
        Throwable cause = new IllegalArgumentException("cause");

        TestLoggedException exception = new TestLoggedException(getClass(), cause);

        assertEquals(exception.getCause(), cause);
        assertEquals(exception.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

