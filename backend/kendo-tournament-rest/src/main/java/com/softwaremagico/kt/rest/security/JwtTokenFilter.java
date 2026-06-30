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


import com.softwaremagico.kt.core.providers.AuthenticatedUserProvider;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.logger.JwtFilterLogger;
import com.softwaremagico.kt.persistence.entities.IAuthenticatedUser;
import com.softwaremagico.kt.rest.exceptions.InvalidIpException;
import com.softwaremagico.kt.rest.exceptions.InvalidJwtException;
import com.softwaremagico.kt.rest.exceptions.InvalidMacException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Servlet filter that validates the JWT Bearer token on every incoming HTTP request.
 * <p>
 * The filter runs once per request ({@link OncePerRequestFilter}). It:
 * </p>
 * <ol>
 *   <li>Extracts the {@code Authorization: Bearer &lt;token&gt;} header.</li>
 *   <li>Validates the token signature and expiration via {@link JwtTokenUtil}.</li>
 *   <li>Optionally verifies that the client IP stored in the token matches the
 *       request source IP (enabled via {@code jwt.ip.check=true}).</li>
 *   <li>Optionally verifies the MAC address for additional network-level binding
 *       when available.</li>
 *   <li>If all checks pass, sets a {@link UsernamePasswordAuthenticationToken} in
 *       the {@link SecurityContextHolder} to authenticate the request.</li>
 * </ol>
 * <p>
 * When {@code enable.participant.access=true}, participant accounts can also
 * obtain and use JWT tokens with reduced privileges (VIEWER role).
 * </p>
 * <p>
 * The filter inspects a list of proxy headers ({@link #HEADERS_TO_TRY}) to obtain
 * the real client IP when the server is behind a reverse proxy.
 * </p>
 */
@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private static final String JWT_OBTAINED_TEMPLATE = """
            JWT Obtained:
            Expiration date: '{}'
            User id: '{}'
            Username: '{}'
            Session: '{}'
            Ip: '{}'
            MAC: '{}'
            """;

    private record ResolvedUser(UserDetails details, boolean participantUser) {
    }

    private static final String[] HEADERS_TO_TRY = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"};

    private final boolean checkClientIp;
    private final boolean participantAccess;

    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    private final ParticipantProvider participantProvider;

    private final NetworkController networkController;

    @Autowired
    public JwtTokenFilter(@Value("${jwt.ip.check:false}") String ipCheck, @Value("${enable.participant.access:false}") String participantAccess,
                          JwtTokenUtil jwtTokenUtil, AuthenticatedUserProvider authenticatedUserProvider,
                          ParticipantProvider participantProvider,
                          NetworkController networkController) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.participantProvider = participantProvider;
        this.checkClientIp = Boolean.parseBoolean(ipCheck);
        this.participantAccess = Boolean.parseBoolean(participantAccess);
        this.networkController = networkController;
    }

    @Override
    public void doFilterInternal(@NonNull HttpServletRequest request,
                                 @NonNull HttpServletResponse response,
                                 @NonNull FilterChain chain)
            throws ServletException, IOException {
        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (this.isMissingBearerToken(header)) {
            chain.doFilter(request, response);
            this.logMissingBearerToken(request);
            return;
        }

        final String token = getTokenFromHeader(header);
        if (token.isEmpty() || !this.jwtTokenUtil.validate(token)) {
            this.handleInvalidToken(request, response, chain);
            return;
        }

        this.logTokenDetails(token);
        final ResolvedUser resolvedUser = this.resolveUser(token);
        final UsernamePasswordAuthenticationToken authentication = createAuthentication(resolvedUser.details());

        this.validateTokenNetworkBinding(request, token, resolvedUser.participantUser());

        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken createAuthentication(UserDetails userDetails) {
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails == null ? Collections.emptyList() : userDetails.getAuthorities()
        );
    }

    private boolean isMissingBearerToken(String header) {
        return ObjectUtils.isEmpty(header) || !header.startsWith(BEARER_PREFIX);
    }

    private String getTokenFromHeader(String header) {
        return header.substring(BEARER_PREFIX.length()).trim();
    }

    private void logMissingBearerToken(HttpServletRequest request) {
        if (request.getContextPath() != null && !request.getContextPath().contains("health-check")) {
            JwtFilterLogger.debug(this.getClass(), "No Bearer token found on headers");
        }
    }

    private void handleInvalidToken(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
        JwtFilterLogger.errorMessage(this.getClass().getName(), "JWT token invalid!");
        try {
            chain.doFilter(request, response);
        } catch (Exception ignored) {
            throw new InvalidJwtException(this.getClass(), "Invalid JWT token issued.");
        }
    }

    private void logTokenDetails(String token) {
        if (JwtFilterLogger.isDebugEnabled()) {
            JwtFilterLogger.debug(this.getClass().getName(), JWT_OBTAINED_TEMPLATE,
                    this.jwtTokenUtil.getExpirationDate(token), this.jwtTokenUtil.getUserId(token), this.jwtTokenUtil.getUsername(token),
                    this.jwtTokenUtil.getSession(token), this.jwtTokenUtil.getUserIp(token), this.jwtTokenUtil.getHostMac(token));
        }
    }

    private ResolvedUser resolveUser(String token) {
        final IAuthenticatedUser user = authenticatedUserProvider.findByUsername(jwtTokenUtil.getUsername(token)).orElse(null);
        if (user == null && participantAccess) {
            final UserDetails participant = participantProvider.findByTokenUsername(jwtTokenUtil.getUsername(token)).orElse(null);
            return new ResolvedUser(participant, true);
        }
        return new ResolvedUser((UserDetails) user, false);
    }

    private void validateTokenNetworkBinding(HttpServletRequest request, String token, boolean participantUser) {
        if (!checkClientIp || participantUser) {
            return;
        }

        final String userTokenIp = jwtTokenUtil.getUserIp(token);
        if (userTokenIp == null || userTokenIp.isEmpty() || !getClientIpAddress(request).contains(userTokenIp)) {
            throw new InvalidIpException(this.getClass(), "User token issued for ip '" + userTokenIp + "'.");
        }

        final String hostMac = networkController.getHostMac();
        if (hostMac != null && !hostMac.isEmpty() && !Objects.equals(jwtTokenUtil.getHostMac(token), hostMac)) {
            throw new InvalidMacException(this.getClass(), "User token issued for ip '" + userTokenIp + "'.");
        }
    }

    private List<String> getClientIpAddress(HttpServletRequest request) {
        for (final String header : HEADERS_TO_TRY) {
            final String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return parseHeaderIp(ip);
            }
        }

        return Collections.singletonList(request.getRemoteAddr());
    }

    private List<String> parseHeaderIp(String ip) {
        if (!ip.contains(",")) {
            return Collections.singletonList(ip.trim());
        }
        return Arrays.stream(ip.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }
}
