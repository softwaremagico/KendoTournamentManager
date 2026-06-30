package com.softwaremagico.kt.rest.security;

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

import com.softwaremagico.kt.core.providers.AuthenticatedUserProvider;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.persistence.entities.AuthenticatedUser;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.rest.exceptions.InvalidIpException;
import com.softwaremagico.kt.rest.exceptions.InvalidMacException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertThrows;

public class JwtTokenFilterTest {

    private JwtTokenUtil jwtTokenUtil;
    private AuthenticatedUserProvider authenticatedUserProvider;
    private ParticipantProvider participantProvider;
    private NetworkController networkController;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        jwtTokenUtil = mock(JwtTokenUtil.class);
        authenticatedUserProvider = mock(AuthenticatedUserProvider.class);
        participantProvider = mock(ParticipantProvider.class);
        networkController = mock(NetworkController.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    @AfterMethod(alwaysRun = true)
    public void cleanContext() {
        SecurityContextHolder.clearContext();
    }

    @Test(groups = {"jwtTokenUtil"})
    public void shouldContinueChainWhenAuthorizationHeaderIsMissing() throws Exception {
        final JwtTokenFilter filter = new JwtTokenFilter("false", "false", jwtTokenUtil, authenticatedUserProvider,
                participantProvider, networkController);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        when(request.getContextPath()).thenReturn("/api");

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(jwtTokenUtil, never()).validate(any());
    }

    @Test(groups = {"jwtTokenUtil"})
    public void shouldContinueChainWhenTokenIsInvalid() throws Exception {
        final JwtTokenFilter filter = new JwtTokenFilter("false", "false", jwtTokenUtil, authenticatedUserProvider,
                participantProvider, networkController);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer invalid-token");
        when(jwtTokenUtil.validate("invalid-token")).thenReturn(false);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(authenticatedUserProvider, never()).findByUsername(any());
    }

    @Test(groups = {"jwtTokenUtil"})
    public void shouldContinueChainWhenBearerTokenIsBlank() throws Exception {
        final JwtTokenFilter filter = new JwtTokenFilter("false", "false", jwtTokenUtil, authenticatedUserProvider,
                participantProvider, networkController);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer   ");

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(jwtTokenUtil, never()).validate(any());
        verify(authenticatedUserProvider, never()).findByUsername(any());
    }

    @Test(groups = {"jwtTokenUtil"})
    public void shouldAuthenticateStandardUserWhenTokenIsValid() throws Exception {
        final JwtTokenFilter filter = new JwtTokenFilter("false", "false", jwtTokenUtil, authenticatedUserProvider,
                participantProvider, networkController);
        final AuthenticatedUser authenticatedUser = new AuthenticatedUser("admin");
        authenticatedUser.setRoles(Set.of("ROLE_ADMIN"));

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer valid-token");
        when(jwtTokenUtil.validate("valid-token")).thenReturn(true);
        when(jwtTokenUtil.getUsername("valid-token")).thenReturn("admin");
        when(authenticatedUserProvider.findByUsername("admin")).thenReturn(Optional.of(authenticatedUser));

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertSame(SecurityContextHolder.getContext().getAuthentication().getPrincipal(), authenticatedUser);
    }

    @Test(groups = {"jwtTokenUtil"})
    public void shouldThrowInvalidIpWhenIpDoesNotMatchToken() {
        final JwtTokenFilter filter = new JwtTokenFilter("true", "false", jwtTokenUtil, authenticatedUserProvider,
                participantProvider, networkController);
        final AuthenticatedUser authenticatedUser = new AuthenticatedUser("admin");

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer token-ip");
        when(jwtTokenUtil.validate("token-ip")).thenReturn(true);
        when(jwtTokenUtil.getUsername("token-ip")).thenReturn("admin");
        when(jwtTokenUtil.getUserIp("token-ip")).thenReturn("10.10.10.10");
        when(authenticatedUserProvider.findByUsername("admin")).thenReturn(Optional.of(authenticatedUser));
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn(null);
        when(request.getHeader("HTTP_X_FORWARDED")).thenReturn(null);
        when(request.getHeader("HTTP_X_CLUSTER_CLIENT_IP")).thenReturn(null);
        when(request.getHeader("HTTP_CLIENT_IP")).thenReturn(null);
        when(request.getHeader("HTTP_FORWARDED_FOR")).thenReturn(null);
        when(request.getHeader("HTTP_FORWARDED")).thenReturn(null);
        when(request.getHeader("HTTP_VIA")).thenReturn(null);
        when(request.getHeader("REMOTE_ADDR")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.99");

        assertThrows(InvalidIpException.class, () -> filter.doFilterInternal(request, response, chain));
    }

    @Test(groups = {"jwtTokenUtil"})
    public void shouldThrowInvalidMacWhenMacDoesNotMatchToken() {
        final JwtTokenFilter filter = new JwtTokenFilter("true", "false", jwtTokenUtil, authenticatedUserProvider,
                participantProvider, networkController);
        final AuthenticatedUser authenticatedUser = new AuthenticatedUser("admin");

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer token-mac");
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.10.10.10,10.10.10.11");
        when(jwtTokenUtil.validate("token-mac")).thenReturn(true);
        when(jwtTokenUtil.getUsername("token-mac")).thenReturn("admin");
        when(jwtTokenUtil.getUserIp("token-mac")).thenReturn("10.10.10.10");
        when(jwtTokenUtil.getHostMac("token-mac")).thenReturn("AA-BB");
        when(networkController.getHostMac()).thenReturn("CC-DD");
        when(authenticatedUserProvider.findByUsername("admin")).thenReturn(Optional.of(authenticatedUser));

        assertThrows(InvalidMacException.class, () -> filter.doFilterInternal(request, response, chain));
    }

    @Test(groups = {"jwtTokenUtil"})
    public void shouldAllowParticipantUserWhenParticipantAccessIsEnabled() throws Exception {
        final JwtTokenFilter filter = new JwtTokenFilter("true", "true", jwtTokenUtil, authenticatedUserProvider,
                participantProvider, networkController);
        final Participant participant = Mockito.mock(Participant.class);

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer participant-token");
        when(jwtTokenUtil.validate("participant-token")).thenReturn(true);
        when(jwtTokenUtil.getUsername("participant-token")).thenReturn("participant-user");
        when(jwtTokenUtil.getUserIp("participant-token")).thenReturn("11.11.11.11");
        when(authenticatedUserProvider.findByUsername("participant-user")).thenReturn(Optional.empty());
        when(participantProvider.findByTokenUsername("participant-user")).thenReturn(Optional.of(participant));

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertSame(SecurityContextHolder.getContext().getAuthentication().getPrincipal(), participant);
    }
}

