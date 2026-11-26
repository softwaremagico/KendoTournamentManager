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

import com.softwaremagico.kt.logger.ExceptionType;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Exercises {@link com.softwaremagico.kt.logger.LoggedHttpException} branches (message logging
 * per {@link ExceptionType}, and the throwable-based constructor) through its concrete subclass
 * {@link BadRequestException}, since the abstract base class cannot be instantiated directly.
 */
@Test(groups = "loggedHttpException")
public class LoggedHttpExceptionTest {

    @Test
    public void constructor_withDebugType_expectStatusAndMessageSet() {
        final BadRequestException exception = new BadRequestException(this.getClass(), "debug message", ExceptionType.DEBUG);
        assertEquals(exception.getStatus(), HttpStatus.BAD_REQUEST);
        assertEquals(exception.getMessage(), "debug message");
    }

    @Test
    public void constructor_withInfoType_expectStatusAndMessageSet() {
        final BadRequestException exception = new BadRequestException(this.getClass(), "info message", ExceptionType.INFO);
        assertEquals(exception.getStatus(), HttpStatus.BAD_REQUEST);
        assertEquals(exception.getMessage(), "info message");
    }

    @Test
    public void constructor_withWarningType_expectStatusAndMessageSet() {
        final BadRequestException exception = new BadRequestException(this.getClass(), "warning message", ExceptionType.WARNING);
        assertEquals(exception.getStatus(), HttpStatus.BAD_REQUEST);
        assertEquals(exception.getMessage(), "warning message");
    }

    @Test
    public void constructor_withSevereType_expectStatusAndMessageSet() {
        final BadRequestException exception = new BadRequestException(this.getClass(), "severe message", ExceptionType.SEVERE);
        assertEquals(exception.getStatus(), HttpStatus.BAD_REQUEST);
        assertEquals(exception.getMessage(), "severe message");
    }

    @Test
    public void constructor_withMessageOnly_expectDefaultWarningTypeAndStatus() {
        final BadRequestException exception = new BadRequestException(this.getClass(), "default message");
        assertEquals(exception.getStatus(), HttpStatus.BAD_REQUEST);
        assertEquals(exception.getMessage(), "default message");
    }

    @Test
    public void constructor_withClassOnly_expectInvalidParametersMessage() {
        final BadRequestException exception = new BadRequestException(this.getClass());
        assertEquals(exception.getStatus(), HttpStatus.BAD_REQUEST);
        assertEquals(exception.getMessage(), "Invalid parameters");
    }

    @Test
    public void constructor_withThrowable_expectCauseWrappedAndInternalServerErrorStatus() {
        final RuntimeException cause = new RuntimeException("root cause");
        final BadRequestException exception = new BadRequestException(this.getClass(), cause);
        // This constructor variant always forces INTERNAL_SERVER_ERROR regardless of the subclass's
        // own @ResponseStatus, since LoggedHttpException(Class, Throwable) hardcodes that status.
        assertEquals(exception.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR);
        assertNotNull(exception.getCause());
        assertEquals(exception.getCause(), cause);
    }
}


