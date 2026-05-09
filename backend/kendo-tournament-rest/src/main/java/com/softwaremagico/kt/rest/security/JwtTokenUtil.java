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
import com.softwaremagico.kt.logger.RestServerLogger;
import com.softwaremagico.kt.persistence.entities.IAuthenticatedUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

/**
 * Utility component for creating, parsing, and validating JSON Web Tokens (JWT).
 * <p>
 * Tokens are signed with HMAC-SHA512. The JWT subject is a pipe-delimited string with
 * five indexed fields: {@code id|username|session|ip|mac}.
 * </p>
 * <p>
 * The signing secret is read from the {@code jwt.secret} property. If the property is
 * absent or blank, a cryptographically secure random 32-character secret is generated
 * at startup — this means tokens will be invalidated on server restart.
 * </p>
 * <p>
 * Three separate expiration periods are supported (all in milliseconds):
 * </p>
 * <ul>
 *   <li>{@code jwt.expiration} — standard authenticated user tokens (default: 1 200 000 ms / 20 min)</li>
 *   <li>{@code jwt.guest.expiration} — guest (QR-code) access tokens</li>
 *   <li>{@code jwt.participant.expiration} — participant self-service tokens</li>
 * </ul>
 * <p>
 * If a per-role expiration is not configured it falls back to the standard value.
 * </p>
 */
@Component
public class JwtTokenUtil {
    private static final String JWT_ISSUER = "com.softwaremagico";
    /** Default token validity period in milliseconds (20 minutes). */
    private static final long JWT_EXPIRATION = 1200000;
    /** Index of the entity ID field within the pipe-separated token subject. */
    private static final int ID_INDEX = 0;
    /** Index of the username field within the pipe-separated token subject. */
    private static final int USERNAME_INDEX = 1;
    /** Index of the session identifier field within the pipe-separated token subject. */
    private static final int SESSION_INDEX = 2;
    /** Index of the client IP address field within the pipe-separated token subject. */
    private static final int IP_INDEX = 3;
    /** Index of the network MAC address field within the pipe-separated token subject. */
    private static final int MAC_INDEX = 4;

    //JWT Secret key
    private static final int RANDOM_LEFT_LIMIT = 48; // numeral '0'
    private static final int RANDOM_RIGHT_LIMIT = 122; // letter 'z'
    private static final int RANDOM_LENGTH = 32; // 32 characters by key
    private static final Random RANDOM = new SecureRandom();

    private final NetworkController networkController;

    private final String jwtSecret;
    private final long jwtExpiration;
    private final long jwtGuestExpiration;
    private final long jwtParticipantExpiration;

    @Autowired
    public JwtTokenUtil(@Value("${jwt.secret:#{null}}") String jwtSecret, @Value("${jwt.expiration}") String jwtExpiration,
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
            } catch (NumberFormatException e) {
                RestServerLogger.warning(this.getClass().getName(), "jwt.expiration value '{}' is invalid. Setting default to '{}'.",
                        jwtExpiration, JWT_EXPIRATION);
                calculatedJwtExpiration = JWT_EXPIRATION;
            }
        }
        if (jwtSecret != null && !jwtSecret.isBlank()) {
            this.jwtSecret = jwtSecret;
        } else {
            this.jwtSecret = generateRandomSecret();
        }
        this.jwtExpiration = calculatedJwtExpiration;

        //If not set, guest expiration is the same that the standard one.
        long calculatedGuestJwtExpiration;
        if (jwtGuestExpiration == null) {
            calculatedGuestJwtExpiration = this.jwtExpiration;
        } else {
            try {
                calculatedGuestJwtExpiration = Long.parseLong(jwtGuestExpiration);
            } catch (NumberFormatException e) {
                calculatedGuestJwtExpiration = this.jwtExpiration;
            }
        }
        this.jwtGuestExpiration = calculatedGuestJwtExpiration;

        //If not set, participant expiration is the same that the standard one.
        long calculatedParticipantJwtExpiration;
        if (jwtParticipantExpiration == null) {
            calculatedParticipantJwtExpiration = this.jwtExpiration;
        } else {
            try {
                calculatedParticipantJwtExpiration = Long.parseLong(jwtParticipantExpiration);
            } catch (NumberFormatException e) {
                calculatedParticipantJwtExpiration = this.jwtExpiration;
            }
        }
        this.jwtParticipantExpiration = calculatedParticipantJwtExpiration;
    }

    /**
     * Generates a cryptographically secure random alphanumeric string of length
     * {@link #RANDOM_LENGTH} to be used as a JWT signing secret when none is configured.
     *
     * @return a random secret string
     */
    private String generateRandomSecret() {
        return RANDOM.ints(RANDOM_LEFT_LIMIT, RANDOM_RIGHT_LIMIT + 1)
                .limit(RANDOM_LENGTH)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }


    /**
     * Generates a standard JWT access token for the given user with a new random session ID.
     *
     * @param user   the authenticated user for whom the token is generated
     * @param userIp the IP address of the client making the request
     * @return a signed JWT string
     */
    public String generateAccessToken(IAuthenticatedUser user, String userIp) {
        return generateAccessToken(user, userIp, jwtExpiration, UUID.randomUUID().toString());
    }

    /**
     * Generates a standard JWT access token for the given user with an explicit session ID.
     *
     * @param user    the authenticated user for whom the token is generated
     * @param userIp  the IP address of the client making the request
     * @param session the session identifier to embed in the token
     * @return a signed JWT string
     */
    public String generateAccessToken(IAuthenticatedUser user, String userIp, String session) {
        return generateAccessToken(user, userIp, jwtExpiration, session);
    }

    /**
     * Generates a JWT access token with a custom expiration time and a new random session ID.
     *
     * @param user           the authenticated user for whom the token is generated
     * @param userIp         the IP address of the client making the request
     * @param expirationTime token validity period in milliseconds from now
     * @return a signed JWT string
     */
    public String generateAccessToken(IAuthenticatedUser user, String userIp, Long expirationTime) {
        return generateAccessToken(user, userIp, expirationTime, UUID.randomUUID().toString());
    }

    /**
     * Generates a signed JWT access token embedding the user identity, session, client IP and
     * server MAC address in the token subject as a comma-separated string.
     * <p>
     * Subject format: {@code id,username,session,ip,mac}
     * </p>
     *
     * @param user           the authenticated user for whom the token is generated
     * @param userIp         the IP address of the client making the request
     * @param expirationTime token validity period in milliseconds from now
     * @param session        the session identifier to embed in the token
     * @return a signed JWT string
     */
    public String generateAccessToken(IAuthenticatedUser user, String userIp, Long expirationTime, String session) {
        return Jwts.builder()
                .setSubject(String.format("%s,%s,%s,%s,%s", user.getId(), user.getUsername(), session != null ? session : UUID.randomUUID(),
                        userIp, networkController.getHostMac()))
                .setIssuer(JWT_ISSUER)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // 1 week
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    /**
     * Returns the absolute expiration timestamp for a standard user token.
     *
     * @return current time plus the configured standard expiration, in Unix epoch milliseconds
     */
    public long getJwtExpirationTime() {
        return (System.currentTimeMillis() + jwtExpiration);
    }

    /**
     * Returns the absolute expiration timestamp for a guest token.
     *
     * @return current time plus the configured guest expiration, in Unix epoch milliseconds
     */
    public long getJwtGuestExpirationTime() {
        return (System.currentTimeMillis() + jwtGuestExpiration);
    }

    /**
     * Returns the absolute expiration timestamp for a participant token.
     *
     * @return current time plus the configured participant expiration, in Unix epoch milliseconds
     */
    public long getJwtParticipantExpirationTime() {
        return (System.currentTimeMillis() + jwtParticipantExpiration);
    }

    /**
     * Extracts the entity ID from the JWT token subject.
     *
     * @param token the JWT string to parse
     * @return the user entity ID, or {@code null} if the claim is absent or the token is malformed
     */
    public String getUserId(String token) {
        final Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        try {
            return claims.getSubject().split(",")[ID_INDEX];
        } catch (Exception e) {
            JwtFilterLogger.warning(this.getClass().getName(), "No filed 'user id' on JWT token!");
            return null;
        }
    }

    /**
     * Extracts the username from the JWT token subject.
     *
     * @param token the JWT string to parse
     * @return the username, or {@code null} if the claim is absent or the token is malformed
     */
    public String getUsername(String token) {
        final Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        try {
            return claims.getSubject().split(",")[USERNAME_INDEX];
        } catch (Exception e) {
            JwtFilterLogger.warning(this.getClass().getName(), "No filed 'user name' on JWT token!");
            return null;
        }
    }

    /**
     * Extracts the session identifier from the JWT token subject.
     *
     * @param token the JWT string to parse
     * @return the session ID, or {@code null} if the claim is absent or the token is malformed
     */
    public String getSession(String token) {
        final Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
        try {
            return claims.getSubject().split(",")[SESSION_INDEX];
        } catch (Exception e) {
            JwtFilterLogger.debug(this.getClass().getName(), "No session information on JWT token!");
            return null;
        }
    }

    /**
     * Extracts the client IP address from the JWT token subject.
     *
     * @param token the JWT string to parse
     * @return the IP address string, or {@code null} if the claim is absent or the token is malformed
     */
    public String getUserIp(String token) {
        final Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
        try {
            return claims.getSubject().split(",")[IP_INDEX];
        } catch (Exception e) {
            JwtFilterLogger.debug(this.getClass().getName(), "No filed 'user IP' on JWT token!");
            return null;
        }
    }

    /**
     * Extracts the server MAC address from the JWT token subject.
     *
     * @param token the JWT string to parse
     * @return the MAC address string, or {@code null} if the claim is absent or the token is malformed
     */
    public String getHostMac(String token) {
        final Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
        try {
            return claims.getSubject().split(",")[MAC_INDEX];
        } catch (Exception e) {
            JwtFilterLogger.debug(this.getClass().getName(), "No filed 'host MAC' on JWT token!");
            return null;
        }
    }

    /**
     * Returns the expiration date embedded in the given JWT token.
     *
     * @param token the JWT string to parse
     * @return the expiration {@link Date}
     */
    public Date getExpirationDate(String token) {
        final Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration();
    }

    /**
     * Validates the given JWT token by verifying its signature, structure and expiration.
     *
     * @param token the JWT string to validate
     * @return {@code true} if the token is valid; {@code false} if it is expired, malformed,
     *         has an invalid signature, or is otherwise unacceptable
     */
    public boolean validate(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (SignatureException ex) {
            JwtFilterLogger.errorMessage(this.getClass().getName(), "Invalid JWT signature '{}'", ex.getMessage());
        } catch (MalformedJwtException ex) {
            JwtFilterLogger.errorMessage(this.getClass().getName(), "Invalid JWT token '{}'", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            JwtFilterLogger.errorMessage(this.getClass().getName(), "Expired JWT token '{}'", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            JwtFilterLogger.errorMessage(this.getClass().getName(), "Unsupported JWT token '{}'", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            JwtFilterLogger.errorMessage(this.getClass().getName(), "JWT claims string is empty '{}'", ex.getMessage());
        }
        return false;
    }

}
