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

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class ErrorResponseTest {

    @Test(groups = "restErrorResponse")
    public void shouldKeepExplicitCode() {
        ErrorResponse response = new ErrorResponse("Message", "EXPLICIT_CODE");

        assertEquals(response.getCode(), "EXPLICIT_CODE");
        assertEquals(response.getMessage(), "Message");
    }

    @Test(groups = "restErrorResponse")
    public void shouldGenerateCodeFromMessageWhenCodeIsNull() {
        ErrorResponse response = new ErrorResponse("Invalid Credentials");

        assertEquals(response.getCode(), "invalid_credentials");
    }

    @Test(groups = "restErrorResponse")
    public void shouldReturnNullCodeWhenMessageAndCodeAreNull() {
        ErrorResponse response = new ErrorResponse(null, null, null);

        assertNull(response.getCode());
    }

    @Test(groups = "restErrorResponse")
    public void shouldExposeCauseAndToString() {
        RuntimeException cause = new RuntimeException("boom");
        ErrorResponse response = new ErrorResponse("Bad request", cause);

        assertEquals(response.getCause(), cause);
        assertEquals(response.toString(), "Bad request");
    }
}

