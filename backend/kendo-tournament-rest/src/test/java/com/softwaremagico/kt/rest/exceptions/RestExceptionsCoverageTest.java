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

import com.softwaremagico.kt.logger.ExceptionType;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

@Test(groups = "restErrorResponse")
public class RestExceptionsCoverageTest {

    @Test
    public void shouldCoverBadRequestExceptionConstructors() {
        final BadRequestException defaultException = new BadRequestException(getClass());
        final BadRequestException warningException = new BadRequestException(getClass(), "bad request");
        final BadRequestException typedException = new BadRequestException(getClass(), "typed", ExceptionType.INFO);
        final IllegalArgumentException cause = new IllegalArgumentException("boom");
        final BadRequestException causeException = new BadRequestException(getClass(), cause);

        assertEquals(defaultException.getMessage(), "Invalid parameters");
        assertEquals(defaultException.getStatus(), HttpStatus.BAD_REQUEST);
        assertEquals(warningException.getMessage(), "bad request");
        assertEquals(typedException.getMessage(), "typed");
        assertSame(causeException.getCause(), cause);
        assertEquals(causeException.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void shouldCoverInvalidPasswordExceptionConstructors() {
        final InvalidPasswordException defaultException = new InvalidPasswordException(getClass());
        final InvalidPasswordException warningException = new InvalidPasswordException(getClass(), "invalid password");
        final InvalidPasswordException typedException = new InvalidPasswordException(getClass(), "typed", ExceptionType.SEVERE);
        final IllegalStateException cause = new IllegalStateException("cause");
        final InvalidPasswordException causeException = new InvalidPasswordException(getClass(), cause);

        assertEquals(defaultException.getMessage(), "Invalid parameters");
        assertEquals(defaultException.getStatus(), HttpStatus.BAD_REQUEST);
        assertEquals(warningException.getMessage(), "invalid password");
        assertEquals(typedException.getMessage(), "typed");
        assertSame(causeException.getCause(), cause);
        assertEquals(causeException.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void shouldCoverInvalidRequestExceptionConstructors() {
        final InvalidRequestException defaultException = new InvalidRequestException(getClass());
        final InvalidRequestException warningException = new InvalidRequestException(getClass(), "invalid request");
        final InvalidRequestException typedException = new InvalidRequestException(getClass(), "typed", ExceptionType.WARNING);
        final RuntimeException cause = new RuntimeException("cause");
        final InvalidRequestException causeException = new InvalidRequestException(getClass(), cause);

        assertEquals(defaultException.getMessage(), "Status not found");
        assertEquals(defaultException.getStatus(), HttpStatus.NOT_FOUND);
        assertEquals(warningException.getMessage(), "invalid request");
        assertEquals(warningException.getStatus(), HttpStatus.NOT_FOUND);
        assertEquals(typedException.getMessage(), "typed");
        assertEquals(typedException.getStatus(), HttpStatus.NOT_FOUND);
        assertSame(causeException.getCause(), cause);
        assertEquals(causeException.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void shouldCoverInvalidMacExceptionConstructor() {
        final InvalidMacException exception = new InvalidMacException(getClass(), "invalid mac");

        assertEquals(exception.getMessage(), "invalid mac");
        assertEquals(exception.getStatus(), HttpStatus.CONFLICT);
    }

    @Test
    public void shouldCoverInvalidIpExceptionConstructor() {
        final InvalidIpException exception = new InvalidIpException(getClass(), "invalid ip");

        assertEquals(exception.getMessage(), "invalid ip");
        assertEquals(exception.getStatus(), HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldCoverGuestDisabledExceptionConstructor() {
        final GuestDisabledException exception = new GuestDisabledException(getClass(), "guest disabled");

        assertEquals(exception.getMessage(), "guest disabled");
        assertEquals(exception.getStatus(), HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldCoverUserBlockedExceptionConstructor() {
        final UserBlockedException exception = new UserBlockedException(getClass(), "user blocked");

        assertEquals(exception.getMessage(), "user blocked");
        assertEquals(exception.getStatus(), HttpStatus.LOCKED);
    }

    @Test
    public void shouldCoverInvalidJwtExceptionConstructor() {
        final InvalidJwtException exception = new InvalidJwtException(getClass(), "invalid jwt");

        assertEquals(exception.getMessage(), "invalid jwt");
        assertEquals(exception.getStatus(), HttpStatus.UNAUTHORIZED);
    }
}

