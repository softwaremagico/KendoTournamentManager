package com.softwaremagico.kt.rest.security.dto;

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

public class AuthParticipantRequestTest {

    @Test(groups = "createUserRequest")
    public void shouldKeepTokenValue() {
        final AuthParticipantRequest request = new AuthParticipantRequest();
        request.setToken(" participant-token ");

        assertEquals(request.getToken(), " participant-token ");
    }

    @Test(groups = "createUserRequest")
    public void shouldReturnNullTokenByDefault() {
        final AuthParticipantRequest request = new AuthParticipantRequest();

        assertNull(request.getToken());
    }
}

