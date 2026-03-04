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

@Component
public class JwtTokenUtil {
    private static final String JWT_ISSUER = "com.softwaremagico";
    private static final long JWT_EXPIRATION = 1200000;
    private static final int ID_INDEX = 0;
    private static final int USERNAME_INDEX = 1;
    private static final int SESSION_INDEX = 2;
    private static final int IP_INDEX = 3;
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

    private String generateRandomSecret() {
        return RANDOM.ints(RANDOM_LEFT_LIMIT, RANDOM_RIGHT_LIMIT + 1)
                .limit(RANDOM_LENGTH)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }


    public String generateAccessToken(IAuthenticatedUser user, String userIp) {
        return generateAccessToken(user, userIp, jwtExpiration, UUID.randomUUID().toString());
    }

    public String generateAccessToken(IAuthenticatedUser user, String userIp, String session) {
        return generateAccessToken(user, userIp, jwtExpiration, session);
    }

    public String generateAccessToken(IAuthenticatedUser user, String userIp, Long expirationTime) {
        return generateAccessToken(user, userIp, expirationTime, UUID.randomUUID().toString());
    }

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
     * Gets current expiration time in milliseconds.
     *
     * @return unix epoch time in milliseconds.
     */
    public long getJwtExpirationTime() {
        return (System.currentTimeMillis() + jwtExpiration);
    }

    public long getJwtGuestExpirationTime() {
        return (System.currentTimeMillis() + jwtGuestExpiration);
    }

    public long getJwtParticipantExpirationTime() {
        return (System.currentTimeMillis() + jwtParticipantExpiration);
    }

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

    public Date getExpirationDate(String token) {
        final Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration();
    }

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
