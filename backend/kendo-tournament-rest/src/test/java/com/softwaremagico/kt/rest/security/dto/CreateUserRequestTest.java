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

import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class CreateUserRequestTest {

	@Test(groups = "createUserRequest")
	public void shouldTrimAllTextFields() {
		final CreateUserRequest request = new CreateUserRequest();
		request.setUsername("  user  ");
		request.setName("  name  ");
		request.setLastname("  lastname  ");
		request.setPassword("  pass  ");

		assertEquals(request.getUsername(), "user");
		assertEquals(request.getName(), "name");
		assertEquals(request.getLastname(), "lastname");
		assertEquals(request.getPassword(), "pass");
	}

	@Test(groups = "createUserRequest")
	public void shouldReturnNullForNullTextFields() {
		final CreateUserRequest request = new CreateUserRequest();

		assertNull(request.getUsername());
		assertNull(request.getName());
		assertNull(request.getLastname());
		assertNull(request.getPassword());
	}

	@Test(groups = "createUserRequest")
	public void shouldKeepRoles() {
		final CreateUserRequest request = new CreateUserRequest();
		final Set<String> roles = Set.of("ROLE_ADMIN", "ROLE_USER");

		request.setRoles(roles);

		assertEquals(request.getRoles(), roles);
	}
}
