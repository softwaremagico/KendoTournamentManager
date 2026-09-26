package com.softwaremagico.kt.rest.controllers;

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

import com.softwaremagico.kt.core.exceptions.DuplicatedUserException;
import com.softwaremagico.kt.core.providers.AuthenticatedUserProvider;
import com.softwaremagico.kt.persistence.entities.AuthenticatedUser;
import com.softwaremagico.kt.persistence.entities.IAuthenticatedUser;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.rest.exceptions.BadRequestException;
import com.softwaremagico.kt.rest.exceptions.InvalidPasswordException;
import com.softwaremagico.kt.rest.exceptions.UserNotFoundException;
import com.softwaremagico.kt.rest.security.dto.CreateUserRequest;
import com.softwaremagico.kt.security.AvailableRole;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

@Test(groups = "authenticatedUserController")
public class AuthenticatedUserControllerTest {

	@Mock
	private AuthenticatedUserProvider provider;

	private AuthenticatedUserController controller;

	@BeforeMethod(alwaysRun = true)
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		controller = new AuthenticatedUserController(provider);
	}

	@Test
	public void shouldCreateUserFromRequest() {
		final CreateUserRequest request = new CreateUserRequest();
		request.setUsername("user1");
		request.setName("Name");
		request.setLastname("Last");
		request.setPassword("pass");
		request.setRoles(Set.of("admin"));
		final AuthenticatedUser saved = mock(AuthenticatedUser.class);

		when(provider.save(any(), any(), any(), any(), any(), any(String[].class))).thenReturn(saved);

		assertSame(controller.createUser("creator", request), saved);
	}

	@Test
	public void shouldCreateUserFromRequestWithoutRoles() {
		final CreateUserRequest request = new CreateUserRequest();
		request.setUsername("user1");
		request.setName("Name");
		request.setLastname("Last");
		request.setPassword("pass");
		final AuthenticatedUser saved = mock(AuthenticatedUser.class);

		when(provider.save(any(), any(), any(), any(), any(), (String[]) any())).thenReturn(saved);

		assertSame(controller.createUser("creator", request), saved);
	}

	@Test
	public void shouldCreateUserWithAvailableRoles() {
		final AuthenticatedUser saved = mock(AuthenticatedUser.class);
		when(provider.save(any(), any(), any(), any(), any(), any(String[].class))).thenReturn(saved);

		assertSame(controller.createUser("creator", "user1", "Name", "Last", "pass", AvailableRole.ADMIN), saved);
	}

	@Test(expectedExceptions = BadRequestException.class)
	public void shouldThrowBadRequestWhenUserDuplicated() {
		when(provider.save(any(), any(), any(), any(), any(), any(String[].class)))
				.thenThrow(new DuplicatedUserException(this.getClass(), "duplicated"));

		controller.createUser("creator", "user1", "Name", "Last", "pass", "admin");
	}

	@Test(expectedExceptions = UserNotFoundException.class)
	public void shouldThrowWhenUpdatingPasswordForMissingUser() {
		when(provider.findByUsername("missing")).thenReturn(Optional.empty());
		controller.updatePassword("missing", "old", "new", "actioner");
	}

	@Test(expectedExceptions = UserNotFoundException.class)
	public void shouldThrowWhenUpdatingPasswordForParticipant() {
		final Participant participant = new Participant();
		when(provider.findByUsername("part")).thenReturn(Optional.of(participant));
		controller.updatePassword("part", "old", "new", "actioner");
	}

	@Test(expectedExceptions = InvalidPasswordException.class)
	public void shouldThrowWhenOldPasswordIsIncorrect() {
		final AuthenticatedUser user = new AuthenticatedUser();
		user.setPassword(BCrypt.hashpw("correct", BCrypt.gensalt()));
		when(provider.findByUsername("user1")).thenReturn(Optional.of(user));

		controller.updatePassword("user1", "wrong", "new", "actioner");
	}

	@Test
	public void shouldUpdatePasswordSuccessfully() {
		final AuthenticatedUser user = new AuthenticatedUser();
		user.setUsername("user1");
		user.setPassword(BCrypt.hashpw("old", BCrypt.gensalt()));
		when(provider.findByUsername("user1")).thenReturn(Optional.of(user));

		controller.updatePassword("user1", "old", "newPass", "actioner");

		assertEquals(user.getPassword(), "newPass");
		assertEquals(user.getUpdatedBy(), "actioner");
		verify(provider, times(1)).save(user);
	}

	@Test(expectedExceptions = UserNotFoundException.class)
	public void shouldThrowWhenUpdatingUserNotFound() {
		final CreateUserRequest request = new CreateUserRequest();
		request.setUsername("missing");
		when(provider.findByUsername("missing")).thenReturn(Optional.empty());

		controller.updateUser("actioner", request);
	}

	@Test
	public void shouldUpdateUserKeepingOwnRolesWhenSelfUpdate() {
		final AuthenticatedUser user = new AuthenticatedUser();
		user.setUsername("user1");
		user.setRoles(Set.of("admin"));
		final CreateUserRequest request = new CreateUserRequest();
		request.setUsername("user1");
		request.setName("New\nName");
		request.setLastname(null);
		request.setRoles(Set.of("viewer"));

		when(provider.findByUsername("user1")).thenReturn(Optional.of((IAuthenticatedUser) user));
		when(provider.save(user)).thenReturn(user);

		final AuthenticatedUser result = controller.updateUser("user1", request);

		assertEquals(result.getName(), "New_Name");
		assertEquals(result.getLastname(), "");
		assertEquals(result.getRoles(), Set.of("admin"));
	}

	@Test
	public void shouldUpdateUserRolesWhenUpdatedByAnotherUser() {
		final AuthenticatedUser user = new AuthenticatedUser();
		user.setUsername("user1");
		user.setRoles(Set.of("admin"));
		final CreateUserRequest request = new CreateUserRequest();
		request.setUsername("user1");
		request.setName("Name");
		request.setLastname("Last");
		request.setRoles(Set.of("viewer"));

		when(provider.findByUsername("user1")).thenReturn(Optional.of((IAuthenticatedUser) user));
		when(provider.save(user)).thenReturn(user);

		final AuthenticatedUser result = controller.updateUser("admin2", request);

		assertEquals(result.getRoles(), Set.of("viewer"));
	}

	@Test(expectedExceptions = UserNotFoundException.class)
	public void shouldThrowWhenDeletingMissingUser() {
		when(provider.findByUsername("missing")).thenReturn(Optional.empty());
		controller.deleteUser("actioner", "missing");
	}

	@Test(expectedExceptions = UserNotFoundException.class)
	public void shouldThrowWhenDeletingParticipant() {
		when(provider.findByUsername("part")).thenReturn(Optional.of(new Participant()));
		controller.deleteUser("actioner", "part");
	}

	@Test
	public void shouldDeleteUserWhenMoreThanOneRemains() {
		final AuthenticatedUser user = new AuthenticatedUser();
		user.setUsername("user1");
		when(provider.findByUsername("user1")).thenReturn(Optional.of((IAuthenticatedUser) user));
		when(provider.count()).thenReturn(2L);

		controller.deleteUser("actioner", "user1");

		verify(provider, times(1)).delete(user);
	}

	@Test
	public void shouldNotDeleteLastRemainingUser() {
		final AuthenticatedUser user = new AuthenticatedUser();
		user.setUsername("user1");
		when(provider.findByUsername("user1")).thenReturn(Optional.of((IAuthenticatedUser) user));
		when(provider.count()).thenReturn(1L);

		controller.deleteUser("actioner", "user1");

		verify(provider, never()).delete(any());
	}

	@Test(expectedExceptions = UserNotFoundException.class)
	public void shouldThrowWhenGettingRolesForMissingUser() {
		when(provider.findByUsername("missing")).thenReturn(Optional.empty());
		controller.getRoles("missing");
	}

	@Test
	public void shouldReturnRolesForUser() {
		final AuthenticatedUser user = new AuthenticatedUser();
		user.setRoles(Set.of("admin", "viewer"));
		when(provider.findByUsername("user1")).thenReturn(Optional.of((IAuthenticatedUser) user));

		assertEquals(controller.getRoles("user1"), Set.of("admin", "viewer"));
	}

	@Test
	public void shouldReturnAllUsers() {
		final AuthenticatedUser user = new AuthenticatedUser();
		when(provider.findAll()).thenReturn(List.of(user));

		assertEquals(controller.findAll(), List.of(user));
	}

	@Test
	public void shouldDeleteGivenUser() {
		final AuthenticatedUser user = new AuthenticatedUser();
		controller.delete(user);
		verify(provider, times(1)).delete(user);
	}

	@Test
	public void shouldDeleteAllUsers() {
		controller.deleteAll();
		verify(provider, times(1)).deleteAll();
	}

	@Test
	public void shouldCountUsers() {
		when(provider.count()).thenReturn(4L);
		assertEquals(controller.countUsers(), 4L);
	}
}

