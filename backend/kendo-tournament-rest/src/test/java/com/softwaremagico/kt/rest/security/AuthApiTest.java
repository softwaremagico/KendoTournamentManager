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

@Test(groups = {"authApiTests"})
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
		this.authApi = new AuthApi(this.authenticationManager, this.jwtTokenUtil, this.authenticatedUserController,
				this.bruteForceService, this.authenticatedUserProvider, this.participantController,
				this.tournamentProvider, "false");
	}

	// ========== Login Security Tests ==========

	@Test
	public void testLoginWithBlockedIp() {
		final AuthRequest authRequest = new AuthRequest();
		authRequest.setUsername("testuser");
		authRequest.setPassword("password");

		when(this.httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
		when(this.httpRequest.getRemoteAddr()).thenReturn("192.168.1.1");
		when(this.bruteForceService.isBlocked("192.168.1.1")).thenReturn(true);
		when(this.bruteForceService.getElementsTime("192.168.1.1")).thenReturn(10L);
		when(this.bruteForceService.getExpirationTime()).thenReturn(300L);

		final ResponseEntity<?> response = this.authApi.login(authRequest, this.httpRequest);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.LOCKED);
		assertThat(response.getHeaders().get(HttpHeaders.RETRY_AFTER)).isNotEmpty();
		verify(this.bruteForceService, times(1)).isBlocked("192.168.1.1");
	}

	@Test
	public void testLoginWithValidCredentials() {
		final AuthRequest authRequest = new AuthRequest();
		authRequest.setUsername("testuser");
		authRequest.setPassword("password123");

		final AuthenticatedUser user = new AuthenticatedUser();
		user.setUsername("testuser");

		when(this.httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
		when(this.httpRequest.getRemoteAddr()).thenReturn("192.168.1.1");
		when(this.bruteForceService.isBlocked("192.168.1.1")).thenReturn(false);
		when(this.authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(this.authentication);
		when(this.authentication.getName()).thenReturn("testuser");
		when(this.authenticatedUserProvider.findByUsername("testuser")).thenReturn(Optional.of(user));
		when(this.jwtTokenUtil.getJwtExpirationTime()).thenReturn(3600000L);
		when(this.jwtTokenUtil.generateAccessToken(user, "192.168.1.1")).thenReturn("jwt-token-value");
		when(this.jwtTokenUtil.getSession("jwt-token-value")).thenReturn("session-123");

		final ResponseEntity<?> response = this.authApi.login(authRequest, this.httpRequest);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getHeaders().get(HttpHeaders.AUTHORIZATION)).containsExactly("jwt-token-value");
		assertThat(response.getHeaders().get(AuthApi.SESSION_HEADER)).containsExactly("session-123");
		assertThat(response.getHeaders().get(HttpHeaders.EXPIRES)).containsExactly("3600000");
		verify(this.bruteForceService, times(1)).loginSucceeded("192.168.1.1");
	}

	@Test
	public void testLoginWithBadCredentials() {
		final AuthRequest authRequest = new AuthRequest();
		authRequest.setUsername("testuser");
		authRequest.setPassword("wrongpassword");

		when(this.httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
		when(this.httpRequest.getRemoteAddr()).thenReturn("192.168.1.1");
		when(this.bruteForceService.isBlocked("192.168.1.1")).thenReturn(false);
		when(this.authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenThrow(new BadCredentialsException("Bad credentials"));
		when(this.authenticatedUserController.countUsers()).thenReturn(1L);

		final ResponseEntity<?> response = this.authApi.login(authRequest, this.httpRequest);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		verify(this.bruteForceService, times(1)).loginFailed("192.168.1.1");
	}

	@Test
	public void testLoginCreateDefaultAdminWhenNoUsers() {
		final AuthRequest authRequest = new AuthRequest();
		authRequest.setUsername("admin");
		authRequest.setPassword("initialpassword");

		final AuthenticatedUser defaultAdmin = new AuthenticatedUser();
		defaultAdmin.setUsername("admin");

		when(this.httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
		when(this.httpRequest.getRemoteAddr()).thenReturn("192.168.1.1");
		when(this.bruteForceService.isBlocked("192.168.1.1")).thenReturn(false);
		when(this.authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenThrow(new BadCredentialsException("Bad credentials"));
		when(this.authenticatedUserController.countUsers()).thenReturn(0L);
		when(this.authenticatedUserController.createUser(null, "admin", "Default", "Admin", "initialpassword",
				AvailableRole.ADMIN)).thenReturn(defaultAdmin);
		when(this.jwtTokenUtil.getJwtExpirationTime()).thenReturn(3600000L);
		when(this.jwtTokenUtil.generateAccessToken(defaultAdmin, "192.168.1.1")).thenReturn("admin-jwt");

		final ResponseEntity<?> response = this.authApi.login(authRequest, this.httpRequest);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		verify(this.authenticatedUserController, times(1)).createUser(null, "admin", "Default", "Admin",
				"initialpassword", AvailableRole.ADMIN);
	}

	@Test
	public void testLoginWithXForwardedForHeader() {
		final AuthRequest authRequest = new AuthRequest();
		authRequest.setUsername("testuser");
		authRequest.setPassword("password");

		when(this.httpRequest.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1,10.0.0.2,10.0.0.3");
		when(this.bruteForceService.isBlocked("10.0.0.1")).thenReturn(true);
		when(this.bruteForceService.getElementsTime("10.0.0.1")).thenReturn(5L);
		when(this.bruteForceService.getExpirationTime()).thenReturn(100L);

		this.authApi.login(authRequest, this.httpRequest);

		verify(this.bruteForceService, times(1)).isBlocked("10.0.0.1");
	}

	@Test
	public void testLoginWithUserNotFoundAfterAuthentication() {
		final AuthRequest authRequest = new AuthRequest();
		authRequest.setUsername("testuser");
		authRequest.setPassword("password");

		when(this.httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
		when(this.httpRequest.getRemoteAddr()).thenReturn("192.168.1.1");
		when(this.bruteForceService.isBlocked("192.168.1.1")).thenReturn(false);
		when(this.authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(this.authentication);
		when(this.authentication.getName()).thenReturn("testuser");
		when(this.authenticatedUserProvider.findByUsername("testuser")).thenReturn(Optional.empty());

		final ResponseEntity<?> response = this.authApi.login(authRequest, this.httpRequest);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	// ========== Guest Login Tests ==========

	@Test
	public void testLoginAsGuestWhenDisabled() {
		final AuthGuestRequest guestRequest = new AuthGuestRequest();
		guestRequest.setTournamentId(1);

		assertThatThrownBy(() -> this.authApi.loginAsGuest(guestRequest, this.httpRequest))
				.isInstanceOf(GuestDisabledException.class).hasMessageContaining("disabled");
	}

	@Test
	public void testLoginAsGuestWithGuestUserNotFound() {
		final AuthApi guestEnabledApi = new AuthApi(this.authenticationManager, this.jwtTokenUtil,
				this.authenticatedUserController, this.bruteForceService, this.authenticatedUserProvider,
				this.participantController, this.tournamentProvider, "true");

		final AuthGuestRequest guestRequest = new AuthGuestRequest();
		guestRequest.setTournamentId(1);

		when(this.httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
		when(this.httpRequest.getRemoteAddr()).thenReturn("192.168.1.1");
		when(this.authenticatedUserProvider.findByUsername(AuthenticatedUserProvider.GUEST_USER))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> guestEnabledApi.loginAsGuest(guestRequest, this.httpRequest))
				.isInstanceOf(GuestDisabledException.class).hasMessageContaining("not found");
	}

	@Test
	public void testLoginAsGuestWithLockedTournament() {
		final AuthApi guestEnabledApi = new AuthApi(this.authenticationManager, this.jwtTokenUtil,
				this.authenticatedUserController, this.bruteForceService, this.authenticatedUserProvider,
				this.participantController, this.tournamentProvider, "true");

		final AuthenticatedUser guestUser = new AuthenticatedUser();
		guestUser.setUsername(AuthenticatedUserProvider.GUEST_USER);

		final Tournament lockedTournament = new Tournament();
		lockedTournament.setId(1);
		lockedTournament.setLocked(true);

		final AuthGuestRequest guestRequest = new AuthGuestRequest();
		guestRequest.setTournamentId(1);

		when(this.httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
		when(this.httpRequest.getRemoteAddr()).thenReturn("192.168.1.1");
		when(this.authenticatedUserProvider.findByUsername(AuthenticatedUserProvider.GUEST_USER))
				.thenReturn(Optional.of(guestUser));
		when(this.tournamentProvider.get(1)).thenReturn(Optional.of(lockedTournament));

		assertThatThrownBy(() -> guestEnabledApi.loginAsGuest(guestRequest, this.httpRequest))
				.isInstanceOf(GuestDisabledException.class).hasMessageContaining("finished");
	}

	@Test
	public void testLoginAsGuestWithUnlockedTournament() {
		final AuthApi guestEnabledApi = new AuthApi(this.authenticationManager, this.jwtTokenUtil,
				this.authenticatedUserController, this.bruteForceService, this.authenticatedUserProvider,
				this.participantController, this.tournamentProvider, "true");

		final AuthenticatedUser guestUser = new AuthenticatedUser();
		guestUser.setUsername(AuthenticatedUserProvider.GUEST_USER);

		final Tournament unlockedTournament = new Tournament();
		unlockedTournament.setId(1);
		unlockedTournament.setLocked(false);

		final AuthGuestRequest guestRequest = new AuthGuestRequest();
		guestRequest.setTournamentId(1);

		when(this.httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
		when(this.httpRequest.getRemoteAddr()).thenReturn("192.168.1.1");
		when(this.authenticatedUserProvider.findByUsername(AuthenticatedUserProvider.GUEST_USER))
				.thenReturn(Optional.of(guestUser));
		when(this.tournamentProvider.get(1)).thenReturn(Optional.of(unlockedTournament));
		when(this.jwtTokenUtil.getJwtGuestExpirationTime()).thenReturn(1800000L);
		when(this.jwtTokenUtil.generateAccessToken(guestUser, "192.168.1.1", 1800000L)).thenReturn("guest-jwt");
		when(this.jwtTokenUtil.getSession("guest-jwt")).thenReturn("guest-session");

		final ResponseEntity<?> response = guestEnabledApi.loginAsGuest(guestRequest, this.httpRequest);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getHeaders().get(HttpHeaders.AUTHORIZATION)).containsExactly("guest-jwt");
	}

	// ========== User Management Tests ==========

	@Test
	public void testGetAllUsers() {
		final AuthenticatedUser user1 = new AuthenticatedUser();
		user1.setUsername("user1");
		final AuthenticatedUser user2 = new AuthenticatedUser();
		user2.setUsername("user2");

		when(this.authenticatedUserController.findAll()).thenReturn(List.of(user1, user2));

		final Collection<AuthenticatedUser> users = this.authApi.getAll(this.httpRequest);

		assertThat(users).hasSize(2).containsExactlyInAnyOrder(user1, user2);
	}

	@Test
	public void testRegisterNewUser() {
		final CreateUserRequest createUserRequest = new CreateUserRequest();
		createUserRequest.setUsername("newuser");

		final AuthenticatedUser createdUser = new AuthenticatedUser();
		createdUser.setUsername("newuser");

		when(this.authentication.getName()).thenReturn("admin");
		when(this.authenticatedUserController.createUser("admin", createUserRequest)).thenReturn(createdUser);

		final AuthenticatedUser result = this.authApi.register(createUserRequest, this.authentication,
				this.httpRequest);

		assertThat(result).isNotNull().extracting(AuthenticatedUser::getUsername).isEqualTo("newuser");
		verify(this.authenticatedUserController, times(1)).createUser("admin", createUserRequest);
	}

	@Test
	public void testUpdateExistingUser() {
		final CreateUserRequest updateRequest = new CreateUserRequest();
		updateRequest.setUsername("existinguser");

		final AuthenticatedUser updatedUser = new AuthenticatedUser();
		updatedUser.setUsername("existinguser");

		when(this.authentication.getName()).thenReturn("admin");
		when(this.authenticatedUserController.updateUser("admin", updateRequest)).thenReturn(updatedUser);

		final AuthenticatedUser result = this.authApi.update(updateRequest, this.authentication, this.httpRequest);

		assertThat(result).isNotNull().extracting(AuthenticatedUser::getUsername).isEqualTo("existinguser");
		verify(this.authenticatedUserController, times(1)).updateUser("admin", updateRequest);
	}

	@Test
	public void testDeleteOwnAccountThrowsException() {
		when(this.authentication.getName()).thenReturn("admin");

		assertThatThrownBy(() -> this.authApi.delete("admin", this.authentication, this.httpRequest))
				.isInstanceOf(InvalidRequestException.class).hasMessageContaining("cannot delete the current user");
	}

	@Test
	public void testDeleteOtherUser() {
		when(this.authentication.getName()).thenReturn("admin");

		this.authApi.delete("otheruser", this.authentication, this.httpRequest);

		verify(this.authenticatedUserController, times(1)).deleteUser("admin", "otheruser");
	}

	// ========== Password Management Tests ==========

	@Test
	public void testUpdateOwnPassword() throws InterruptedException {
		final UpdatePasswordRequest passwordRequest = new UpdatePasswordRequest();
		passwordRequest.setOldPassword("oldpassword");
		passwordRequest.setNewPassword("newpassword");

		when(this.authentication.getName()).thenReturn("testuser");
		when(this.httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
		when(this.httpRequest.getRemoteAddr()).thenReturn("192.168.1.1");

		this.authApi.updatePassword(passwordRequest, this.authentication, this.httpRequest);

		verify(this.authenticatedUserController, times(1)).updatePassword("testuser", "oldpassword", "newpassword",
				"testuser");
	}

	@Test
	public void testUpdateUserPasswordByAdmin() throws InterruptedException {
		final UpdatePasswordRequest passwordRequest = new UpdatePasswordRequest();
		passwordRequest.setNewPassword("newpassword");

		when(this.authentication.getName()).thenReturn("admin");
		when(this.httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
		when(this.httpRequest.getRemoteAddr()).thenReturn("192.168.1.1");

		this.authApi.updateUserPassword("otheruser", passwordRequest, this.authentication, this.httpRequest);

		verify(this.authenticatedUserController, times(1)).updatePassword("admin", "otheruser", "newpassword",
				"newpassword");
	}

	// ========== JWT Token Tests ==========

	@Test
	public void testGetUserRoles() {
		final Set<String> roles = Set.of("ADMIN", "EDITOR");

		when(this.authentication.getName()).thenReturn("testuser");
		when(this.authenticatedUserController.getRoles("testuser")).thenReturn(roles);

		final Set<String> result = this.authApi.getRoles(this.authentication, this.httpRequest);

		assertThat(result).containsExactlyInAnyOrder("ADMIN", "EDITOR");
	}

	@Test
	public void testRenewJWTToken() {
		final AuthenticatedUser user = new AuthenticatedUser();
		user.setUsername("testuser");

		when(this.authentication.getName()).thenReturn("testuser");
		when(this.httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
		when(this.httpRequest.getRemoteAddr()).thenReturn("192.168.1.1");
		when(this.httpRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("old-jwt-token");
		when(this.authenticatedUserProvider.findByUsername("testuser")).thenReturn(Optional.of(user));
		when(this.jwtTokenUtil.getJwtExpirationTime()).thenReturn(3600000L);
		when(this.jwtTokenUtil.generateAccessToken(user, "192.168.1.1")).thenReturn("new-jwt-token");
		when(this.jwtTokenUtil.getSession("new-jwt-token")).thenReturn("new-session");

		final ResponseEntity<Void> response = this.authApi.getNewJWT(this.authentication, this.httpRequest,
				"old-jwt-token");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getHeaders().get(HttpHeaders.AUTHORIZATION)).containsExactly("new-jwt-token");
		assertThat(response.getHeaders().get(AuthApi.SESSION_HEADER)).containsExactly("new-session");
	}

	@Test
	public void testRenewJWTTokenWithMissingUser() {
		when(this.authentication.getName()).thenReturn("unknownuser");
		when(this.authenticatedUserProvider.findByUsername("unknownuser")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> this.authApi.getNewJWT(this.authentication, this.httpRequest, "jwt-token"))
				.isInstanceOf(UsernameNotFoundException.class);
	}

	// ========== Listener Tests ==========

	@Test
	public void testAddUserAdminGeneratedListener() {
		final AuthApi.UserAdminGeneratedListener listener = mock(AuthApi.UserAdminGeneratedListener.class);

		this.authApi.addUserAdminGeneratedListeners(listener);

		assertThat(this.authApi).isNotNull();
	}

	@Test
	public void testUserAdminGeneratedListenerInvoked() {
		final AuthRequest authRequest = new AuthRequest();
		authRequest.setUsername("admin");
		authRequest.setPassword("password");

		final AuthenticatedUser newAdmin = new AuthenticatedUser();
		newAdmin.setUsername("admin");

		final AuthApi.UserAdminGeneratedListener mockListener = mock(AuthApi.UserAdminGeneratedListener.class);
		this.authApi.addUserAdminGeneratedListeners(mockListener);

		when(this.httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
		when(this.httpRequest.getRemoteAddr()).thenReturn("192.168.1.1");
		when(this.bruteForceService.isBlocked("192.168.1.1")).thenReturn(false);
		when(this.authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenThrow(new BadCredentialsException("Bad credentials"));
		when(this.authenticatedUserController.countUsers()).thenReturn(0L);
		when(this.authenticatedUserController.createUser(null, "admin", "Default", "Admin", "password",
				AvailableRole.ADMIN)).thenReturn(newAdmin);
		when(this.jwtTokenUtil.getJwtExpirationTime()).thenReturn(3600000L);
		when(this.jwtTokenUtil.generateAccessToken(newAdmin, "192.168.1.1")).thenReturn("jwt-token");

		this.authApi.login(authRequest, this.httpRequest);

		verify(mockListener, times(1)).generated("admin");
	}

	// ========== Response Header Tests ==========

	@Test
	public void testLoginResponseIncludesSessionHeader() {
		final AuthRequest authRequest = new AuthRequest();
		authRequest.setUsername("testuser");
		authRequest.setPassword("password");

		final AuthenticatedUser user = new AuthenticatedUser();
		user.setUsername("testuser");

		when(this.httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
		when(this.httpRequest.getRemoteAddr()).thenReturn("192.168.1.1");
		when(this.bruteForceService.isBlocked("192.168.1.1")).thenReturn(false);
		when(this.authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(this.authentication);
		when(this.authentication.getName()).thenReturn("testuser");
		when(this.authenticatedUserProvider.findByUsername("testuser")).thenReturn(Optional.of(user));
		when(this.jwtTokenUtil.getJwtExpirationTime()).thenReturn(3600000L);
		when(this.jwtTokenUtil.generateAccessToken(user, "192.168.1.1")).thenReturn("jwt-token");
		when(this.jwtTokenUtil.getSession("jwt-token")).thenReturn("session-value");

		final ResponseEntity<?> response = this.authApi.login(authRequest, this.httpRequest);

		assertThat(response.getHeaders().get(AuthApi.SESSION_HEADER)).containsExactly("session-value");
		assertThat(response.getHeaders().get(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS))
				.containsExactly(AuthApi.SESSION_HEADER);
	}

	@Test
	public void testLoginResponseIncludesExpirationHeader() {
		final AuthRequest authRequest = new AuthRequest();
		authRequest.setUsername("testuser");
		authRequest.setPassword("password");

		final AuthenticatedUser user = new AuthenticatedUser();
		user.setUsername("testuser");

		when(this.httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
		when(this.httpRequest.getRemoteAddr()).thenReturn("192.168.1.1");
		when(this.bruteForceService.isBlocked("192.168.1.1")).thenReturn(false);
		when(this.authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(this.authentication);
		when(this.authentication.getName()).thenReturn("testuser");
		when(this.authenticatedUserProvider.findByUsername("testuser")).thenReturn(Optional.of(user));
		when(this.jwtTokenUtil.getJwtExpirationTime()).thenReturn(3600000L);
		when(this.jwtTokenUtil.generateAccessToken(user, "192.168.1.1")).thenReturn("jwt-token");
		when(this.jwtTokenUtil.getSession("jwt-token")).thenReturn("session-value");

		final ResponseEntity<?> response = this.authApi.login(authRequest, this.httpRequest);

		assertThat(response.getHeaders().get(HttpHeaders.EXPIRES)).containsExactly("3600000");
	}
}
