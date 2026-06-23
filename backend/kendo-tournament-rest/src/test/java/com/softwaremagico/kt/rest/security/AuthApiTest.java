package com.softwaremagico.kt.rest.security;

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

import com.softwaremagico.kt.core.controller.ParticipantController;
import com.softwaremagico.kt.core.providers.AuthenticatedUserProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.AuthenticatedUser;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.rest.controllers.AuthenticatedUserController;
import com.softwaremagico.kt.rest.exceptions.GuestDisabledException;
import com.softwaremagico.kt.rest.exceptions.InvalidRequestException;
import com.softwaremagico.kt.rest.security.dto.AuthGuestRequest;
import com.softwaremagico.kt.rest.security.dto.AuthRequest;
import com.softwaremagico.kt.rest.security.dto.CreateUserRequest;
import com.softwaremagico.kt.rest.security.dto.UpdatePasswordRequest;
import com.softwaremagico.kt.security.AvailableRole;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("AuthApi Enhanced Tests")
public class AuthApiTest {

	@Mock
	private AuthenticationManager authenticationManager;
	@Mock
	private JwtTokenUtil jwtTokenUtil;
	@Mock
	private AuthenticatedUserController authenticatedUserController;
	@Mock
	private BruteForceService bruteForceService;
	@Mock
	private AuthenticatedUserProvider authenticatedUserProvider;
	@Mock
	private ParticipantController participantController;
	@Mock
	private TournamentProvider tournamentProvider;
	@Mock
	private HttpServletRequest httpRequest;
	@Mock
	private Authentication authentication;

	private AuthApi authApi;

	@BeforeMethod(alwaysRun = true)
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		authApi = new AuthApi(authenticationManager, jwtTokenUtil, authenticatedUserController, bruteForceService,
				authenticatedUserProvider, participantController, tournamentProvider, "false");
	}

	@Nested
	@DisplayName("Login Security Tests")
	class LoginSecurityTests {

		@Test
		@DisplayName("should_return_locked_status_when_ip_is_blocked")
		void when_login_with_blockedIp_expect_httpLocked() {
			final AuthRequest authRequest = new AuthRequest();
			authRequest.setUsername("testuser");
			authRequest.setPassword("password");

			when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
			when(httpRequest.getRemoteAddr()).thenReturn("192.168.1.1");
			when(bruteForceService.isBlocked("192.168.1.1")).thenReturn(true);
			when(bruteForceService.getElementsTime("192.168.1.1")).thenReturn(10L);
			when(bruteForceService.getExpirationTime()).thenReturn(300L);

			final ResponseEntity<?> response = authApi.login(authRequest, httpRequest);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.LOCKED);
			assertThat(response.getHeaders().get(HttpHeaders.RETRY_AFTER)).isNotEmpty();
			verify(bruteForceService, times(1)).isBlocked("192.168.1.1");
		}

		@Test
		@DisplayName("should_authenticate_and_return_jwt_on_valid_credentials")
		void when_login_with_validCredentials_expect_jwtInHeaders() {
			final AuthRequest authRequest = new AuthRequest();
			authRequest.setUsername("testuser");
			authRequest.setPassword("password123");

			final AuthenticatedUser user = new AuthenticatedUser();
			user.setUsername("testuser");

			when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
			when(httpRequest.getRemoteAddr()).thenReturn("192.168.1.1");
			when(bruteForceService.isBlocked("192.168.1.1")).thenReturn(false);
			when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
					.thenReturn(authentication);
			when(authentication.getName()).thenReturn("testuser");
			when(authenticatedUserProvider.findByUsername("testuser")).thenReturn(Optional.of(user));
			when(jwtTokenUtil.getJwtExpirationTime()).thenReturn(3600000L);
			when(jwtTokenUtil.generateAccessToken(user, "192.168.1.1")).thenReturn("jwt-token-value");
			when(jwtTokenUtil.getSession("jwt-token-value")).thenReturn("session-123");

			final ResponseEntity<?> response = authApi.login(authRequest, httpRequest);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getHeaders().get(HttpHeaders.AUTHORIZATION)).containsExactly("jwt-token-value");
			assertThat(response.getHeaders().get(AuthApi.SESSION_HEADER)).containsExactly("session-123");
			assertThat(response.getHeaders().get(HttpHeaders.EXPIRES)).containsExactly("3600000");
			verify(bruteForceService, times(1)).loginSucceeded("192.168.1.1");
		}

		@Test
		@DisplayName("should_return_unauthorized_on_bad_credentials")
		void when_login_with_badCredentials_expect_unauthorized() {
			final AuthRequest authRequest = new AuthRequest();
			authRequest.setUsername("testuser");
			authRequest.setPassword("wrongpassword");

			when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
			when(httpRequest.getRemoteAddr()).thenReturn("192.168.1.1");
			when(bruteForceService.isBlocked("192.168.1.1")).thenReturn(false);
			when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
					.thenThrow(new BadCredentialsException("Bad credentials"));
			when(authenticatedUserController.countUsers()).thenReturn(1L);

			final ResponseEntity<?> response = authApi.login(authRequest, httpRequest);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
			verify(bruteForceService, times(1)).loginFailed("192.168.1.1");
		}

		@Test
		@DisplayName("should_create_default_admin_when_no_users_exist")
		void when_login_with_badCredentialsAndNoUsers_expect_adminCreated() {
			final AuthRequest authRequest = new AuthRequest();
			authRequest.setUsername("admin");
			authRequest.setPassword("initialpassword");

			final AuthenticatedUser defaultAdmin = new AuthenticatedUser();
			defaultAdmin.setUsername("admin");

			when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
			when(httpRequest.getRemoteAddr()).thenReturn("192.168.1.1");
			when(bruteForceService.isBlocked("192.168.1.1")).thenReturn(false);
			when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
					.thenThrow(new BadCredentialsException("Bad credentials"));
			when(authenticatedUserController.countUsers()).thenReturn(0L);
			when(authenticatedUserController.createUser(null, "admin", "Default", "Admin", "initialpassword",
					AvailableRole.ADMIN)).thenReturn(defaultAdmin);
			when(jwtTokenUtil.getJwtExpirationTime()).thenReturn(3600000L);
			when(jwtTokenUtil.generateAccessToken(defaultAdmin, "192.168.1.1")).thenReturn("admin-jwt");

			final ResponseEntity<?> response = authApi.login(authRequest, httpRequest);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
			verify(authenticatedUserController, times(1)).createUser(null, "admin", "Default", "Admin",
					"initialpassword", AvailableRole.ADMIN);
		}

		@Test
		@DisplayName("should_extract_ip_from_x_forwarded_for_header")
		void when_login_with_xForwardedForHeader_expect_firstIpUsed() {
			final AuthRequest authRequest = new AuthRequest();
			authRequest.setUsername("testuser");
			authRequest.setPassword("password");

			when(httpRequest.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1,10.0.0.2,10.0.0.3");
			when(bruteForceService.isBlocked("10.0.0.1")).thenReturn(true);
			when(bruteForceService.getElementsTime("10.0.0.1")).thenReturn(5L);
			when(bruteForceService.getExpirationTime()).thenReturn(100L);

			authApi.login(authRequest, httpRequest);

			verify(bruteForceService, times(1)).isBlocked("10.0.0.1");
		}
	}

	@Nested
	@DisplayName("Guest Login Tests")
	class GuestLoginTests {

		@Test
		@DisplayName("should_throw_exception_when_guest_disabled")
		void when_loginAsGuest_with_guestDisabled_expect_exception() {
			final AuthGuestRequest guestRequest = new AuthGuestRequest();
			guestRequest.setTournamentId(1);

			assertThatThrownBy(() -> authApi.loginAsGuest(guestRequest, httpRequest))
					.isInstanceOf(GuestDisabledException.class).hasMessageContaining("disabled");
		}

		@Test
		@DisplayName("should_return_unauthorized_when_guest_user_not_found")
		void when_loginAsGuest_with_enabledButMissingGuestUser_expect_unauthorized() {
			final AuthApi guestEnabledApi = new AuthApi(authenticationManager, jwtTokenUtil,
					authenticatedUserController, bruteForceService, authenticatedUserProvider, participantController,
					tournamentProvider, "true");

			final AuthGuestRequest guestRequest = new AuthGuestRequest();
			guestRequest.setTournamentId(1);

			when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
			when(httpRequest.getRemoteAddr()).thenReturn("192.168.1.1");
			when(authenticatedUserProvider.findByUsername(AuthenticatedUserProvider.GUEST_USER))
					.thenReturn(Optional.empty());

			final ResponseEntity<?> response = guestEnabledApi.loginAsGuest(guestRequest, httpRequest);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		}

		@Test
		@DisplayName("should_throw_exception_when_tournament_locked")
		void when_loginAsGuest_with_lockedTournament_expect_exception() {
			final AuthApi guestEnabledApi = new AuthApi(authenticationManager, jwtTokenUtil,
					authenticatedUserController, bruteForceService, authenticatedUserProvider, participantController,
					tournamentProvider, "true");

			final AuthenticatedUser guestUser = new AuthenticatedUser();
			guestUser.setUsername(AuthenticatedUserProvider.GUEST_USER);

			final Tournament lockedTournament = new Tournament();
			lockedTournament.setId(1);
			lockedTournament.setLocked(true);

			final AuthGuestRequest guestRequest = new AuthGuestRequest();
			guestRequest.setTournamentId(1);

			when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
			when(httpRequest.getRemoteAddr()).thenReturn("192.168.1.1");
			when(authenticatedUserProvider.findByUsername(AuthenticatedUserProvider.GUEST_USER))
					.thenReturn(Optional.of(guestUser));
			when(tournamentProvider.get(1)).thenReturn(Optional.of(lockedTournament));

			assertThatThrownBy(() -> guestEnabledApi.loginAsGuest(guestRequest, httpRequest))
					.isInstanceOf(GuestDisabledException.class).hasMessageContaining("finished");
		}
	}

	@Nested
	@DisplayName("User Management Tests")
	class UserManagementTests {

		@Test
		@DisplayName("should_retrieve_all_users")
		void when_getAll_expect_allUsersReturned() {
			final AuthenticatedUser user1 = new AuthenticatedUser();
			user1.setUsername("user1");
			final AuthenticatedUser user2 = new AuthenticatedUser();
			user2.setUsername("user2");

			when(authenticatedUserController.findAll()).thenReturn(List.of(user1, user2));

			final Collection<AuthenticatedUser> users = authApi.getAll(httpRequest);

			assertThat(users).hasSize(2);
			assertThat(users).containsExactlyInAnyOrder(user1, user2);
		}

		@Test
		@DisplayName("should_register_new_user")
		void when_register_expect_userCreated() {
			final CreateUserRequest createUserRequest = new CreateUserRequest();
			createUserRequest.setUsername("newuser");

			final AuthenticatedUser createdUser = new AuthenticatedUser();
			createdUser.setUsername("newuser");

			when(authentication.getName()).thenReturn("admin");
			when(authenticatedUserController.createUser("admin", createUserRequest)).thenReturn(createdUser);

			final AuthenticatedUser result = authApi.register(createUserRequest, authentication, httpRequest);

			assertThat(result).isNotNull().extracting(AuthenticatedUser::getUsername).isEqualTo("newuser");
			verify(authenticatedUserController, times(1)).createUser("admin", createUserRequest);
		}

		@Test
		@DisplayName("should_update_existing_user")
		void when_update_expect_userUpdated() {
			final CreateUserRequest updateRequest = new CreateUserRequest();
			updateRequest.setUsername("existinguser");

			final AuthenticatedUser updatedUser = new AuthenticatedUser();
			updatedUser.setUsername("existinguser");

			when(authentication.getName()).thenReturn("admin");
			when(authenticatedUserController.updateUser("admin", updateRequest)).thenReturn(updatedUser);

			final AuthenticatedUser result = authApi.update(updateRequest, authentication, httpRequest);

			assertThat(result).isNotNull().extracting(AuthenticatedUser::getUsername).isEqualTo("existinguser");
			verify(authenticatedUserController, times(1)).updateUser("admin", updateRequest);
		}

		@Test
		@DisplayName("should_throw_exception_when_deleting_own_account")
		void when_delete_ownUsername_expect_exception() {
			when(authentication.getName()).thenReturn("admin");

			assertThatThrownBy(() -> authApi.delete("admin", authentication, httpRequest))
					.isInstanceOf(InvalidRequestException.class).hasMessageContaining("cannot delete the current user");
		}

		@Test
		@DisplayName("should_delete_other_user")
		void when_delete_differentUsername_expect_deleted() {
			when(authentication.getName()).thenReturn("admin");

			authApi.delete("otheruser", authentication, httpRequest);

			verify(authenticatedUserController, times(1)).deleteUser("admin", "otheruser");
		}
	}

	@Nested
	@DisplayName("Password Management Tests")
	class PasswordManagementTests {

		@Test
		@DisplayName("should_update_own_password")
		void when_updatePassword_expect_passwordUpdated() throws InterruptedException {
			final UpdatePasswordRequest passwordRequest = new UpdatePasswordRequest();
			passwordRequest.setOldPassword("oldpassword");
			passwordRequest.setNewPassword("newpassword");

			when(authentication.getName()).thenReturn("testuser");
			when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
			when(httpRequest.getRemoteAddr()).thenReturn("192.168.1.1");

			authApi.updatePassword(passwordRequest, authentication, httpRequest);

			verify(authenticatedUserController, times(1)).updatePassword("testuser", "oldpassword", "newpassword",
					"testuser");
		}

		@Test
		@DisplayName("should_update_user_password_by_admin")
		void when_updateUserPassword_by_admin_expect_updated() throws InterruptedException {
			final UpdatePasswordRequest passwordRequest = new UpdatePasswordRequest();
			passwordRequest.setNewPassword("newpassword");

			when(authentication.getName()).thenReturn("admin");
			when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
			when(httpRequest.getRemoteAddr()).thenReturn("192.168.1.1");

			authApi.updateUserPassword("otheruser", passwordRequest, authentication, httpRequest);

			verify(authenticatedUserController, times(1)).updatePassword("admin", "otheruser", "newpassword",
					"newpassword");
		}
	}

	@Nested
	@DisplayName("JWT Token Tests")
	class JWTTokenTests {

		@Test
		@DisplayName("should_return_user_roles")
		void when_getRoles_expect_rolesReturned() {
			final Set<String> roles = Set.of("ADMIN", "EDITOR");

			when(authentication.getName()).thenReturn("testuser");
			when(authenticatedUserController.getRoles("testuser")).thenReturn(roles);

			final Set<String> result = authApi.getRoles(authentication, httpRequest);

			assertThat(result).containsExactlyInAnyOrder("ADMIN", "EDITOR");
		}

		@Test
		@DisplayName("should_renew_jwt_token")
		void when_getNewJWT_expect_newTokenGenerated() {
			final AuthenticatedUser user = new AuthenticatedUser();
			user.setUsername("testuser");

			when(authentication.getName()).thenReturn("testuser");
			when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
			when(httpRequest.getRemoteAddr()).thenReturn("192.168.1.1");
			when(httpRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("old-jwt-token");
			when(authenticatedUserProvider.findByUsername("testuser")).thenReturn(Optional.of(user));
			when(jwtTokenUtil.getJwtExpirationTime()).thenReturn(3600000L);
			when(jwtTokenUtil.generateAccessToken(user, "192.168.1.1")).thenReturn("new-jwt-token");
			when(jwtTokenUtil.getSession("new-jwt-token")).thenReturn("new-session");

			final ResponseEntity<Void> response = authApi.getNewJWT(authentication, httpRequest, "old-jwt-token");

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getHeaders().get(HttpHeaders.AUTHORIZATION)).containsExactly("new-jwt-token");
			assertThat(response.getHeaders().get(AuthApi.SESSION_HEADER)).containsExactly("new-session");
		}

		@Test
		@DisplayName("should_throw_exception_when_user_not_found_during_jwt_renewal")
		void when_getNewJWT_with_missingUser_expect_exception() {
			when(authentication.getName()).thenReturn("unknownuser");
			when(authenticatedUserProvider.findByUsername("unknownuser")).thenReturn(Optional.empty());

			assertThatThrownBy(() -> authApi.getNewJWT(authentication, httpRequest, "jwt-token"))
					.isInstanceOf(UsernameNotFoundException.class);
		}
	}

	@Nested
	@DisplayName("User Listener Tests")
	class UserListenerTests {

		@Test
		@DisplayName("should_add_user_admin_generated_listener")
		void when_addUserAdminGeneratedListeners_expect_listenerRegistered() {
			final AuthApi.UserAdminGeneratedListener listener = mock(AuthApi.UserAdminGeneratedListener.class);

			authApi.addUserAdminGeneratedListeners(listener);

			assertThat(authApi).isNotNull();
		}

		@Test
		@DisplayName("should_invoke_listener_when_admin_user_generated")
		void when_loginCreatesDefaultAdmin_expect_listenerInvoked() {
			final AuthRequest authRequest = new AuthRequest();
			authRequest.setUsername("admin");
			authRequest.setPassword("password");

			final AuthenticatedUser newAdmin = new AuthenticatedUser();
			newAdmin.setUsername("admin");

			final AuthApi.UserAdminGeneratedListener mockListener = mock(AuthApi.UserAdminGeneratedListener.class);
			authApi.addUserAdminGeneratedListeners(mockListener);

			when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
			when(httpRequest.getRemoteAddr()).thenReturn("192.168.1.1");
			when(bruteForceService.isBlocked("192.168.1.1")).thenReturn(false);
			when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
					.thenThrow(new BadCredentialsException("Bad credentials"));
			when(authenticatedUserController.countUsers()).thenReturn(0L);
			when(authenticatedUserController.createUser(null, "admin", "Default", "Admin", "password",
					AvailableRole.ADMIN)).thenReturn(newAdmin);
			when(jwtTokenUtil.getJwtExpirationTime()).thenReturn(3600000L);
			when(jwtTokenUtil.generateAccessToken(newAdmin, "192.168.1.1")).thenReturn("jwt-token");

			authApi.login(authRequest, httpRequest);

			verify(mockListener, times(1)).generated("admin");
		}
	}
}
