package com.softwaremagico.kt.rest.security.dto;

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

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@Test(groups = "createUserRequest")
public class AuthRequestsCoverageTest {

    @Test
    public void shouldKeepAuthRequestValues() {
        final AuthRequest request = new AuthRequest();
        request.setUsername("  user  ");
        request.setPassword("  pass  ");

        assertEquals(request.getUsername(), "  user  ");
        assertEquals(request.getPassword(), "  pass  ");
    }

    @Test
    public void shouldKeepAuthGuestRequestTournamentId() {
        final AuthGuestRequest request = new AuthGuestRequest();

        assertNull(request.getTournamentId());

        request.setTournamentId(42);

        assertEquals(request.getTournamentId(), Integer.valueOf(42));
    }
}

