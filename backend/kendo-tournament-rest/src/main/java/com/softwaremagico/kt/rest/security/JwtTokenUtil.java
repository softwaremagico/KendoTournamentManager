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

import com.softwaremagico.kt.logger.JwtFilterLogger;
import com.softwaremagico.kt.persistence.entities.IAuthenticatedUser;
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
@SuppressWarnings("java:S2143")
public class JwtTokenUtil {
    private static final String JWT_ISSUER = "com.softwaremagico";
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

    private final String jwtSecret;
    private final SecretKey signingKey;
    private final long jwtExpiration;
    private final long jwtGuestExpiration;
    private final long jwtParticipantExpiration;

    @Autowired
    public JwtTokenUtil(@Value("${jwt.secret:#{null}}") String jwtSecret,
            @Value("${jwt.expiration}") String jwtExpiration,
            @Value("${jwt.guest.expiration:null}") String jwtGuestExpiration,
            @Value("${jwt.participant.expiration:null}") String jwtParticipantExpiration,
            NetworkController networkController) {
        this.networkController = networkController;

        long calculatedJwtExpiration;
        if (jwtExpiration == null) {
            calculatedJwtExpiration = JWT_EXPIRATION;
        } else {
            try {
                calculatedJwtExpiration = Long.parseLong(jwtExpiration);
            } catch (final NumberFormatException e) {
                JwtFilterLogger.warning(this.getClass().getName(),
                        "jwt.expiration value '{}' is invalid. Setting default to '{}'.", jwtExpiration,
                        JWT_EXPIRATION);
                calculatedJwtExpiration = JWT_EXPIRATION;
            }
        }
        if (jwtSecret != null && !jwtSecret.isBlank()) {
            this.jwtSecret = jwtSecret;
        } else {
            this.jwtSecret = this.generateRandomSecret();
        }
        this.signingKey = this.createSigningKey(this.jwtSecret);
        this.jwtExpiration = calculatedJwtExpiration;

        // If not set, guest expiration is the same that the standard one.
        long calculatedGuestJwtExpiration;
        if (jwtGuestExpiration == null) {
            calculatedGuestJwtExpiration = this.jwtExpiration;
        } else {
            try {
                calculatedGuestJwtExpiration = Long.parseLong(jwtGuestExpiration);
            } catch (final NumberFormatException e) {
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
            } catch (final NumberFormatException e) {
                calculatedParticipantJwtExpiration = this.jwtExpiration;
            }
        }
        this.jwtParticipantExpiration = calculatedParticipantJwtExpiration;
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
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-512 algorithm is not available.", e);
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser().verifyWith(this.signingKey).build().parseSignedClaims(token).getPayload();
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
        final Instant issuedAt = Instant.now();
        return Jwts.builder()
                .subject(new TokenSubject(String.valueOf(user.getId()), user.getUsername(),
                        session != null ? session : UUID.randomUUID().toString(), userIp,
                        this.networkController.getHostMac()).value())
                .issuer(JWT_ISSUER).issuedAt(Date.from(issuedAt))
                .expiration(Date.from(issuedAt.plusMillis(expirationTime))).signWith(this.signingKey, Jwts.SIG.HS512)
                .compact();
    }

    /**
     * Returns the absolute expiration timestamp for a standard user token.
     *
     * @return current time plus the configured standard expiration, in Unix epoch
     *         milliseconds
     */
    public long getJwtExpirationTime() {
        return (System.currentTimeMillis() + this.jwtExpiration);
    }

    /**
     * Returns the absolute expiration timestamp for a guest token.
     *
     * @return current time plus the configured guest expiration, in Unix epoch
     *         milliseconds
     */
    public long getJwtGuestExpirationTime() {
        return (System.currentTimeMillis() + this.jwtGuestExpiration);
    }

    /**
     * Returns the absolute expiration timestamp for a participant token.
     *
     * @return current time plus the configured participant expiration, in Unix
     *         epoch milliseconds
     */
    public long getJwtParticipantExpirationTime() {
        return (System.currentTimeMillis() + this.jwtParticipantExpiration);
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
            JwtFilterLogger.warning(this.getClass().getName(), "No filed 'user id' on JWT token!");
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
            JwtFilterLogger.warning(this.getClass().getName(), "No filed 'user name' on JWT token!");
        }
        return username;
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
            JwtFilterLogger.debug(this.getClass().getName(), "No session information on JWT token!");
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
            JwtFilterLogger.debug(this.getClass().getName(), "No filed 'user IP' on JWT token!");
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
            JwtFilterLogger.debug(this.getClass().getName(), "No filed 'host MAC' on JWT token!");
        }
        return hostMac;
    }

    /**
     * Returns the expiration date embedded in the given JWT token.
     *
     * @param token
     *            the JWT string to parse
     * @return the expiration {@link Date}
     */
    public Date getExpirationDate(String token) {
        return this.getClaims(token).getExpiration();
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
            this.getClaims(token);
            return true;
        } catch (final ExpiredJwtException ex) {
            JwtFilterLogger.errorMessage(this.getClass().getName(), "Expired JWT token '{}'", ex.getMessage());
        } catch (final JwtException ex) {
            JwtFilterLogger.errorMessage(this.getClass().getName(), "Invalid JWT token '{}'", ex.getMessage());
        } catch (final IllegalArgumentException ex) {
            JwtFilterLogger.errorMessage(this.getClass().getName(), "JWT claims string is empty '{}'", ex.getMessage());
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
