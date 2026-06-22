package com.softwaremagico.kt.security;

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

import com.softwaremagico.kt.core.controller.ParticipantController;
import com.softwaremagico.kt.core.providers.AuthenticatedUserProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.IAuthenticatedUser;
import com.softwaremagico.kt.rest.controllers.AuthenticatedUserController;
import com.softwaremagico.kt.rest.exceptions.GuestDisabledException;
import com.softwaremagico.kt.rest.exceptions.InvalidRequestException;
import com.softwaremagico.kt.rest.security.AuthApi;
import com.softwaremagico.kt.rest.security.BruteForceService;
import com.softwaremagico.kt.rest.security.JwtTokenUtil;
import com.softwaremagico.kt.rest.security.dto.AuthGuestRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Test(groups = "authApi")
public class AuthApiUnitTests {

    private AuthenticationManager authenticationManager;
    private JwtTokenUtil jwtTokenUtil;
    private AuthenticatedUserController authenticatedUserController;
    private BruteForceService bruteForceService;
    private AuthenticatedUserProvider authenticatedUserProvider;
    private ParticipantController participantController;
    private TournamentProvider tournamentProvider;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        jwtTokenUtil = mock(JwtTokenUtil.class);
        authenticatedUserController = mock(AuthenticatedUserController.class);
        bruteForceService = mock(BruteForceService.class);
        authenticatedUserProvider = mock(AuthenticatedUserProvider.class);
        participantController = mock(ParticipantController.class);
        tournamentProvider = mock(TournamentProvider.class);
    }

    @Test(expectedExceptions = GuestDisabledException.class)
    public void shouldRejectGuestLoginWhenGuestUsersDisabled() {
        final AuthApi authApi = new AuthApi(authenticationManager, jwtTokenUtil, authenticatedUserController,
                bruteForceService, authenticatedUserProvider, participantController, tournamentProvider, "false");

        final AuthGuestRequest request = new AuthGuestRequest();
        request.setTournamentId(1);

        authApi.loginAsGuest(request, mock(HttpServletRequest.class));
    }

    @Test
    public void shouldGetRolesFromAuthenticatedUserController() {
        final AuthApi authApi = new AuthApi(authenticationManager, jwtTokenUtil, authenticatedUserController,
                bruteForceService, authenticatedUserProvider, participantController, tournamentProvider, "true");

        final Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("admin");
        when(authenticatedUserController.getRoles("admin")).thenReturn(Set.of("admin", "editor"));

        final Set<String> roles = authApi.getRoles(authentication, mock(HttpServletRequest.class));

        assertEquals(roles.size(), 2);
        assertTrue(roles.contains("admin"));
    }

    @Test(expectedExceptions = InvalidRequestException.class)
    public void shouldRejectDeleteOfCurrentUser() {
        final AuthApi authApi = new AuthApi(authenticationManager, jwtTokenUtil, authenticatedUserController,
                bruteForceService, authenticatedUserProvider, participantController, tournamentProvider, "true");

        final Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("same-user");

        authApi.delete("same-user", authentication, mock(HttpServletRequest.class));
    }

    @Test
    public void shouldGenerateRenewedJwtHeaders() {
        final AuthApi authApi = new AuthApi(authenticationManager, jwtTokenUtil, authenticatedUserController,
                bruteForceService, authenticatedUserProvider, participantController, tournamentProvider, "true");

        final Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("john");

        final IAuthenticatedUser authenticatedUser = mock(IAuthenticatedUser.class);
        when(authenticatedUserProvider.findByUsername("john")).thenReturn(Optional.of(authenticatedUser));

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("1.2.3.4, 10.0.0.1");

        when(jwtTokenUtil.getJwtExpirationTime()).thenReturn(99999L);
        when(jwtTokenUtil.generateAccessToken(eq(authenticatedUser), eq("1.2.3.4"))).thenReturn("jwt-token");
        when(jwtTokenUtil.getSession("jwt-token")).thenReturn("session-id");

        final ResponseEntity<Void> response = authApi.getNewJWT(authentication, request, "old-token");

        assertEquals(response.getStatusCode().value(), 200);
        assertNotNull(response.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        assertEquals(response.getHeaders().getFirst(HttpHeaders.AUTHORIZATION), "jwt-token");
        assertEquals(response.getHeaders().getFirst(AuthApi.SESSION_HEADER), "session-id");
    }

    @Test(expectedExceptions = UsernameNotFoundException.class)
    public void shouldFailRenewJwtWhenAuthenticatedUserNotFound() {
        final AuthApi authApi = new AuthApi(authenticationManager, jwtTokenUtil, authenticatedUserController,
                bruteForceService, authenticatedUserProvider, participantController, tournamentProvider, "true");

        final Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("missing");
        when(authenticatedUserProvider.findByUsername("missing")).thenReturn(Optional.empty());

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        authApi.getNewJWT(authentication, request, "old-token");
    }
}

