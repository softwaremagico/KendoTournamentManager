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

import com.softwaremagico.kt.core.security.GuestTicketProvider;
import com.softwaremagico.kt.core.security.RegistrationInvitation;
import com.softwaremagico.kt.core.security.RegistrationTicketProvider;
import com.softwaremagico.kt.logger.JwtFilterLogger;
import com.softwaremagico.kt.logger.RestServerLogger;
import com.softwaremagico.kt.persistence.entities.AuthenticatedUser;
import com.softwaremagico.kt.persistence.entities.IAuthenticatedUser;
import com.softwaremagico.kt.persistence.entities.TenantContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Utility component for creating, parsing, and validating JSON Web Tokens
 * (JWT).
 * <p>
 * Tokens are signed with HMAC-SHA512. The JWT subject is a comma-delimited
 * string with five indexed fields: {@code id|username|session|ip|mac}.
 * </p>
 * <p>
 * The signing secret is read from the {@code jwt.secret} property. If the
 * property is absent or blank, a cryptographically secure random 32-character
 * secret is generated at startup — this means tokens will be invalidated on
 * server restart.
 * </p>
 * <p>
 * Three separate expiration periods are supported (all in milliseconds):
 * </p>
 * <ul>
 * <li>{@code jwt.expiration} — standard authenticated user tokens (default: 1
 * 200 000 ms / 20 min)</li>
 * <li>{@code jwt.guest.expiration} — guest (QR-code) access tokens</li>
 * <li>{@code jwt.participant.expiration} — participant self-service tokens</li>
 * </ul>
 * <p>
 * If a per-role expiration is not configured it falls back to the standard
 * value.
 * </p>
 */
@Component
public class JwtTokenUtil implements GuestTicketProvider, RegistrationTicketProvider {
    private static final String JWT_ISSUER = "com.softwaremagico";
    private static final String GUEST_TICKET_PREFIX = "guest:";
    private static final String REGISTRATION_TICKET_PREFIX = "registration:";
    private static final int REGISTRATION_FIELDS = 3;
    private static final int REGISTRATION_VERSION_INDEX = 2;
    /** Registration links live long enough to be printed on a tournament poster. */
    private static final long REGISTRATION_EXPIRATION = 30L * 24 * 60 * 60 * 1000;
    private static final String TENANT_ID_CLAIM = "tenantId";
    private static final String ROLES_CLAIM = "roles";
    /** Default token validity period in milliseconds (20 minutes). */
    private static final long JWT_EXPIRATION = 1200000;
    /** Index of the entity ID field within the pipe-separated token subject. */
    private static final int ID_INDEX = 0;
    /** Index of the username field within the pipe-separated token subject. */
    private static final int USERNAME_INDEX = 1;
    /**
     * Index of the session identifier field within the pipe-separated token
     * subject.
     */
    private static final int SESSION_INDEX = 2;
    /**
     * Index of the client IP address field within the pipe-separated token subject.
     */
    private static final int IP_INDEX = 3;
    /**
     * Index of the network MAC address field within the pipe-separated token
     * subject.
     */
    private static final int MAC_INDEX = 4;

    // JWT Secret key
    private static final int RANDOM_LEFT_LIMIT = 48; // numeral '0'
    private static final int RANDOM_RIGHT_LIMIT = 122; // letter 'z'
    private static final int RANDOM_LENGTH = 32; // 32 characters by key
    private static final SecureRandom RANDOM = new SecureRandom();

    private final NetworkController networkController;

    private final SecretKey signingKey;
    private final long jwtExpiration;
    private final long jwtGuestExpiration;
    private final long jwtParticipantExpiration;
    private final long jwtRegistrationExpiration;

    public JwtTokenUtil(String jwtSecret, String jwtExpiration, String jwtGuestExpiration,
            String jwtParticipantExpiration, NetworkController networkController) {
        this(jwtSecret, jwtExpiration, jwtGuestExpiration, jwtParticipantExpiration, null, networkController);
    }

    @Autowired
    public JwtTokenUtil(@Value("${jwt.secret:#{null}}") String jwtSecret,
            @Value("${jwt.expiration}") String jwtExpiration,
            @Value("${jwt.guest.expiration:null}") String jwtGuestExpiration,
            @Value("${jwt.participant.expiration:null}") String jwtParticipantExpiration,
            @Value("${jwt.registration.expiration:null}") String jwtRegistrationExpiration,
            NetworkController networkController) {
        this.networkController = networkController;

        long calculatedJwtExpiration;
        if (jwtExpiration == null) {
            calculatedJwtExpiration = JWT_EXPIRATION;
        } else {
            try {
                calculatedJwtExpiration = Long.parseLong(jwtExpiration);
            } catch (NumberFormatException _) {
                RestServerLogger.warning(this.getClass(),
                        "jwt.expiration value '{}' is invalid. Setting default to '{}'.", jwtExpiration,
                        JWT_EXPIRATION);
                calculatedJwtExpiration = JWT_EXPIRATION;
            }
        }
        final String signingSecret = jwtSecret != null && !jwtSecret.isBlank()
                ? jwtSecret
                : this.generateRandomSecret();
        this.signingKey = this.createSigningKey(signingSecret);
        this.jwtExpiration = calculatedJwtExpiration;

        // If not set, guest expiration is the same that the standard one.
        long calculatedGuestJwtExpiration;
        if (jwtGuestExpiration == null) {
            calculatedGuestJwtExpiration = this.jwtExpiration;
        } else {
            try {
                calculatedGuestJwtExpiration = Long.parseLong(jwtGuestExpiration);
            } catch (NumberFormatException _) {
                RestServerLogger.debug(this.getClass(),
                        "jwt.guest.expiration value '{}' is invalid ({}). Using default.", jwtGuestExpiration,
                        "invalid");
                calculatedGuestJwtExpiration = this.jwtExpiration;
            }
        }
        this.jwtGuestExpiration = calculatedGuestJwtExpiration;

        // If not set, participant expiration is the same that the standard one.
        long calculatedParticipantJwtExpiration;
        if (jwtParticipantExpiration == null) {
            calculatedParticipantJwtExpiration = this.jwtExpiration;
        } else {
            try {
                calculatedParticipantJwtExpiration = Long.parseLong(jwtParticipantExpiration);
            } catch (NumberFormatException _) {
                RestServerLogger.debug(this.getClass(),
                        "jwt.participant.expiration value '{}' is invalid ({}). Using default.",
                        jwtParticipantExpiration, "invalid");
                calculatedParticipantJwtExpiration = this.jwtExpiration;
            }
        }
        this.jwtParticipantExpiration = calculatedParticipantJwtExpiration;

        // Registration invitations are valid for 30 days by default so a printed
        // poster keeps working during the weeks before the tournament.
        long calculatedRegistrationJwtExpiration = REGISTRATION_EXPIRATION;
        if (jwtRegistrationExpiration != null && !jwtRegistrationExpiration.isBlank()) {
            try {
                calculatedRegistrationJwtExpiration = Long.parseLong(jwtRegistrationExpiration);
            } catch (NumberFormatException _) {
                RestServerLogger.warning(this.getClass(),
                        "jwt.registration.expiration value '{}' is invalid. Setting default to '{}'.",
                        jwtRegistrationExpiration, REGISTRATION_EXPIRATION);
            }
        }
        this.jwtRegistrationExpiration = calculatedRegistrationJwtExpiration;
    }

    /**
     * Generates a cryptographically secure random alphanumeric string of length
     * {@link #RANDOM_LENGTH} to be used as a JWT signing secret when none is
     * configured.
     *
     * @return a random secret string
     */
    private String generateRandomSecret() {
        return RANDOM.ints(RANDOM_LEFT_LIMIT, RANDOM_RIGHT_LIMIT + 1).limit(RANDOM_LENGTH)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    }

    private SecretKey createSigningKey(String secret) {
        try {
            final byte[] keyBytes = MessageDigest.getInstance("SHA-512")
                    .digest(secret.getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (NoSuchAlgorithmException _) {
            throw new IllegalStateException("SHA-512 algorithm is not available.");
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser().verifyWith(this.signingKey).build().parseSignedClaims(token).getPayload();
    }

    @Override
    public String createGuestTicket(Integer tournamentId, Integer tenantId) {
        if (tournamentId == null || tenantId == null) {
            throw new IllegalArgumentException("A guest ticket requires a tournament and a tenant.");
        }
        return Jwts.builder()
                .subject(GUEST_TICKET_PREFIX + tournamentId + ":" + tenantId)
                .issuer(JWT_ISSUER).issuedAt(toLegacyDate(Instant.now()))
                .signWith(this.signingKey, Jwts.SIG.HS512)
                .compact();
    }

    @Override
    public Integer validateGuestTicket(String ticket, Integer tournamentId) {
        if (ticket == null || ticket.isBlank() || tournamentId == null) {
            return null;
        }
        try {
            final String subject = this.getClaims(ticket).getSubject();
            if (subject == null || !subject.startsWith(GUEST_TICKET_PREFIX)) {
                return null;
            }
            final String[] fields = subject.substring(GUEST_TICKET_PREFIX.length()).split(":");
            if (fields.length != 2 || !tournamentId.toString().equals(fields[0])) {
                return null;
            }
            return Integer.valueOf(fields[1]);
        } catch (JwtException | IllegalArgumentException _) {
            return null;
        }
    }

    @Override
    public String createRegistrationTicket(Integer tournamentId, Integer tenantId, int version) {
        if (tournamentId == null || tenantId == null || version < 1) {
            throw new IllegalArgumentException("A registration ticket requires a tournament, a tenant and a valid version.");
        }
        final Instant issuedAt = Instant.now();
        return Jwts.builder()
                .subject(REGISTRATION_TICKET_PREFIX + tournamentId + ":" + tenantId + ":" + version)
                .issuer(JWT_ISSUER).issuedAt(toLegacyDate(issuedAt))
                .expiration(toLegacyDate(issuedAt.plusMillis(this.jwtRegistrationExpiration)))
                .signWith(this.signingKey, Jwts.SIG.HS512)
                .compact();
    }

    @Override
    public RegistrationInvitation validateRegistrationTicket(String ticket) {
        if (ticket == null || ticket.isBlank()) {
            return null;
        }
        try {
            final String subject = this.getClaims(ticket).getSubject();
            if (subject == null || !subject.startsWith(REGISTRATION_TICKET_PREFIX)) {
                return null;
            }
            final String[] fields = subject.substring(REGISTRATION_TICKET_PREFIX.length()).split(":");
            if (fields.length != REGISTRATION_FIELDS) {
                return null;
            }
            final int version = Integer.parseInt(fields[REGISTRATION_VERSION_INDEX]);
            if (version < 1) {
                return null;
            }
            return new RegistrationInvitation(Integer.valueOf(fields[0]), Integer.valueOf(fields[1]), version);
        } catch (JwtException | IllegalArgumentException _) {
            return null;
        }
    }

    private TokenSubject getTokenSubject(String token) {
        return TokenSubject.from(this.getClaims(token).getSubject());
    }

    /**
     * Generates a standard JWT access token for the given user with a new random
     * session ID.
     *
     * @param user
     *            the authenticated user for whom the token is generated
     * @param userIp
     *            the IP address of the client making the request
     * @return a signed JWT string
     */
    public String generateAccessToken(IAuthenticatedUser user, String userIp) {
        return this.generateAccessToken(user, userIp, this.jwtExpiration, UUID.randomUUID().toString());
    }

    /**
     * Generates a standard JWT access token for the given user with an explicit
     * session ID.
     *
     * @param user
     *            the authenticated user for whom the token is generated
     * @param userIp
     *            the IP address of the client making the request
     * @param session
     *            the session identifier to embed in the token
     * @return a signed JWT string
     */
    public String generateAccessToken(IAuthenticatedUser user, String userIp, String session) {
        return this.generateAccessToken(user, userIp, this.jwtExpiration, session);
    }

    /**
     * Generates a JWT access token with a custom expiration time and a new random
     * session ID.
     *
     * @param user
     *            the authenticated user for whom the token is generated
     * @param userIp
     *            the IP address of the client making the request
     * @param expirationTime
     *            token validity period in milliseconds from now
     * @return a signed JWT string
     */
    public String generateAccessToken(IAuthenticatedUser user, String userIp, Long expirationTime) {
        return this.generateAccessToken(user, userIp, expirationTime, UUID.randomUUID().toString());
    }

    /**
     * Generates a signed JWT access token embedding the user identity, session,
     * client IP and server MAC address in the token subject as a comma-separated
     * string.
     * <p>
     * Subject format: {@code id,username,session,ip,mac}
     * </p>
     *
     * @param user
     *            the authenticated user for whom the token is generated
     * @param userIp
     *            the IP address of the client making the request
     * @param expirationTime
     *            token validity period in milliseconds from now
     * @param session
     *            the session identifier to embed in the token
     * @return a signed JWT string
     */
    public String generateAccessToken(IAuthenticatedUser user, String userIp, Long expirationTime, String session) {
        final Integer tenantId = getTenantId(user);
        final Instant issuedAt = Instant.now();
        return Jwts.builder()
                .subject(new TokenSubject(String.valueOf(user.getId()), user.getUsername(),
                        session != null ? session : UUID.randomUUID().toString(), userIp,
                        this.networkController.getHostMac()).value())
                .claim(TENANT_ID_CLAIM, tenantId)
                .claim(ROLES_CLAIM, user.getRoles() == null ? "" : String.join(",", user.getRoles()))
                .issuer(JWT_ISSUER).issuedAt(toLegacyDate(issuedAt))
                .expiration(toLegacyDate(issuedAt.plusMillis(expirationTime))).signWith(this.signingKey, Jwts.SIG.HS512)
                .compact();
    }

    private Integer getTenantId(IAuthenticatedUser user) {
        if (user instanceof AuthenticatedUser authenticatedUser && authenticatedUser.getTenantId() != null) {
            return authenticatedUser.getTenantId();
        }
        if (user instanceof com.softwaremagico.kt.persistence.entities.Element tenantScopedUser
                && tenantScopedUser.getTenantId() != null) {
            return tenantScopedUser.getTenantId();
        }
        return TenantContext.LEGACY_TENANT_ID;
    }

    /**
     * Converts a {@link java.time.Instant} to a legacy {@link Date}.
     * <p>
     * The JJWT builder API ({@link Jwts.Builder#issuedAt(Date)} and
     * {@link Jwts.Builder#expiration(Date)}) only accepts {@link Date}, so this
     * conversion at the library boundary is unavoidable. Isolating it here keeps
     * the rest of the class working exclusively with the {@code java.time} API.
     * </p>
     *
     * @param instant the instant to convert
     * @return the equivalent {@link Date}
     */
    @SuppressWarnings("java:S2143") // NOSONAR - JJWT exposes only java.util.Date at this API boundary.
    private static Date toLegacyDate(Instant instant) { //NOSONAR - JJWT API requires java.util.Date, conversion isolated here.
        return Date.from(instant); //NOSONAR - JJWT API requires java.util.Date, conversion isolated here.
    }

    /**
     * Returns the absolute expiration timestamp for a standard user token.
     *
     * @return current time plus the configured standard expiration, in Unix epoch
     *         milliseconds
     */
    public long getJwtExpirationTime() {
        return Instant.now().plusMillis(this.jwtExpiration).toEpochMilli();
    }

    /**
     * Returns the absolute expiration timestamp for a guest token.
     *
     * @return current time plus the configured guest expiration, in Unix epoch
     *         milliseconds
     */
    public long getJwtGuestExpirationTime() {
        return Instant.now().plusMillis(this.jwtGuestExpiration).toEpochMilli();
    }

    /**
     * Returns the absolute expiration timestamp for a participant token.
     *
     * @return current time plus the configured participant expiration, in Unix
     *         epoch milliseconds
     */
    public long getJwtParticipantExpirationTime() {
        return Instant.now().plusMillis(this.jwtParticipantExpiration).toEpochMilli();
    }

    /**
     * Extracts the entity ID from the JWT token subject.
     *
     * @param token
     *            the JWT string to parse
     * @return the user entity ID, or {@code null} if the claim is absent or the
     *         token is malformed
     */
    public String getUserId(String token) {
        final String userId = this.getTokenSubject(token).userId();

        if (userId == null) {
            JwtFilterLogger.warning(this.getClass(), "No filed 'user id' on JWT token!");
        }
        return userId;
    }

    /**
     * Extracts the username from the JWT token subject.
     *
     * @param token
     *            the JWT string to parse
     * @return the username, or {@code null} if the claim is absent or the token is
     *         malformed
     */
    public String getUsername(String token) {
        final String username = this.getTokenSubject(token).username();

        if (username == null) {
            JwtFilterLogger.warning(this.getClass(), "No filed 'user name' on JWT token!");
        }
        return username;
    }

    /** Returns the signed tenant boundary assigned to this token. */
    public Integer getTenantId(String token) {
        final Object tenantId = this.getClaims(token).get(TENANT_ID_CLAIM);
        if (tenantId instanceof Number number) {
            return number.intValue();
        }
        return null;
    }

    /** Returns the roles granted to this token, or an empty set for older tokens. */
    public java.util.Set<String> getRoles(String token) {
        final Object roles = this.getClaims(token).get(ROLES_CLAIM);
        if (roles instanceof String value && !value.isBlank()) {
            return java.util.Arrays.stream(value.split(","))
                    .map(String::trim)
                    .filter(role -> !role.isEmpty())
                    .collect(java.util.stream.Collectors.toSet());
        }
        return java.util.Collections.emptySet();
    }

    /**
     * Extracts the session identifier from the JWT token subject.
     *
     * @param token
     *            the JWT string to parse
     * @return the session ID, or {@code null} if the claim is absent or the token
     *         is malformed
     */
    public String getSession(String token) {
        final String session = this.getTokenSubject(token).session();
        if (session == null) {
            JwtFilterLogger.debug(this.getClass(), "No session information on JWT token!");
        }
        return session;
    }

    /**
     * Extracts the client IP address from the JWT token subject.
     *
     * @param token
     *            the JWT string to parse
     * @return the IP address string, or {@code null} if the claim is absent or the
     *         token is malformed
     */
    public String getUserIp(String token) {
        final String userIp = this.getTokenSubject(token).userIp();
        if (userIp == null) {
            JwtFilterLogger.debug(this.getClass(), "No filed 'user IP' on JWT token!");
        }
        return userIp;
    }

    /**
     * Extracts the server MAC address from the JWT token subject.
     *
     * @param token
     *            the JWT string to parse
     * @return the MAC address string, or {@code null} if the claim is absent or the
     *         token is malformed
     */
    public String getHostMac(String token) {
        final String hostMac = this.getTokenSubject(token).hostMac();
        if (hostMac == null) {
            JwtFilterLogger.debug(this.getClass(), "No filed 'host MAC' on JWT token!");
        }
        return hostMac;
    }

    /** Returns the configured validity window for standard tokens, in milliseconds. */
    public long getJwtExpirationDuration() {
        return this.jwtExpiration;
    }

    /** Returns the configured validity window for guest tokens, in milliseconds. */
    public long getJwtGuestExpirationDuration() {
        return this.jwtGuestExpiration;
    }

    /** Returns the configured validity window for participant tokens, in milliseconds. */
    public long getJwtParticipantExpirationDuration() {
        return this.jwtParticipantExpiration;
    }

    /**
     * Returns the expiration date embedded in the given JWT token.
     *
     * @param token
     *            the JWT string to parse
     * @return the expiration {@link Instant}
     */
    public Instant getExpirationDate(String token) {
        return this.getClaims(token).getExpiration().toInstant();
    }

    /**
     * Validates the given JWT token by verifying its signature, structure and
     * expiration.
     *
     * @param token
     *            the JWT string to validate
     * @return {@code true} if the token is valid; {@code false} if it is expired,
     *         malformed, has an invalid signature, or is otherwise unacceptable
     */
    public boolean validate(String token) {
        try {
            final Claims claims = this.getClaims(token);
            return claims.get(TENANT_ID_CLAIM) instanceof Number;
        } catch (ExpiredJwtException _) {
            JwtFilterLogger.errorMessage(this.getClass(), "Expired JWT token");
        } catch (JwtException _) {
            JwtFilterLogger.errorMessage(this.getClass(), "Invalid JWT token");
        } catch (IllegalArgumentException _) {
            JwtFilterLogger.errorMessage(this.getClass(), "JWT claims string is empty");
        }
        return false;
    }

    private record TokenSubject(String userId, String username, String session, String userIp, String hostMac) {
        private static TokenSubject from(String subject) {
            final String[] parts = subject == null ? new String[0] : subject.split(",", -1);
            return new TokenSubject(getPart(parts, ID_INDEX), getPart(parts, USERNAME_INDEX),
                    getPart(parts, SESSION_INDEX), getPart(parts, IP_INDEX), getPart(parts, MAC_INDEX));
        }

        private static String getPart(String[] parts, int index) {
            return index < parts.length && !parts[index].isBlank() ? parts[index] : null;
        }

        private String value() {
            return String.join(",", this.userId, this.username, this.session, this.userIp, this.hostMac);
        }
    }

}
