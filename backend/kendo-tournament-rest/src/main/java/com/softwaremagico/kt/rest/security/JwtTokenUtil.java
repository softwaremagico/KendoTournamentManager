package com.softwaremagico.kt.rest.security;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
 * %%
 * Copyright (C) 2021 - 2022 Softwaremagico
 * %%
 * This software is designed by Jorge Hortelano Otero. Jorge Hortelano Otero
 * <softwaremagico@gmail.com> Valencia (Spain).
 *  
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *  
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.softwaremagico.kt.logger.RestServerLogger;
import com.softwaremagico.kt.persistence.entities.AuthenticatedUser;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenUtil {
    private static final String JWT_ISSUER = "com.softwaremagico";
    private static final long JWT_EXPIRATION = 604800000;

    private final String jwtSecret;
    private final long jwtExpiration;

    @Autowired
    public JwtTokenUtil(@Value("${jwt.secret}") String jwtSecret, @Value("${jwt.expiration}") String jwtExpiration) {
        this.jwtSecret = jwtSecret;

        long calculatedJwtExpiration;
        if (jwtExpiration == null) {
            calculatedJwtExpiration = JWT_EXPIRATION;
        } else {
            try {
                calculatedJwtExpiration = Long.parseLong(jwtExpiration);
            } catch (NumberFormatException e) {
                RestServerLogger.warning(this.getClass().getName(), "jwt.expiration value '{}' is invalid", jwtExpiration);
                calculatedJwtExpiration = JWT_EXPIRATION;
            }
        }
        this.jwtExpiration = calculatedJwtExpiration;
    }

    public String generateAccessToken(AuthenticatedUser user) {
        return Jwts.builder()
                .setSubject(String.format("%s,%s", user.getId(), user.getUsername()))
                .setIssuer(JWT_ISSUER)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)) // 1 week
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String getUserId(String token) {
        final Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject().split(",")[0];
    }

    public String getUsername(String token) {
        final Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject().split(",")[1];
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
            RestServerLogger.errorMessage(this.getClass().getName(), "Invalid JWT signature '{}'", ex.getMessage());
        } catch (MalformedJwtException ex) {
            RestServerLogger.errorMessage(this.getClass().getName(), "Invalid JWT token '{}'", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            RestServerLogger.errorMessage(this.getClass().getName(), "Expired JWT token '{}'", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            RestServerLogger.errorMessage(this.getClass().getName(), "Unsupported JWT token '{}'", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            RestServerLogger.errorMessage(this.getClass().getName(), "JWT claims string is empty '{}'", ex.getMessage());
        }
        return false;
    }

}
