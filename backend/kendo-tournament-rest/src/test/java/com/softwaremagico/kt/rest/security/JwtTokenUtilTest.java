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

import com.softwaremagico.kt.persistence.entities.IAuthenticatedUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import static org.testng.Assert.*;

public class JwtTokenUtilTest {

    private static final String SECRET = "test-secret";

    private NetworkController networkController;
    private IAuthenticatedUser user;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        this.networkController = new TestNetworkController();
        this.user = new TestUser();
    }

    private SecretKey signingKey(String secret) {
        try {
            return Keys.hmacShaKeyFor(MessageDigest.getInstance("SHA-512").digest(secret.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test(groups = "jwtTokenUtil")
    @SuppressWarnings("java:S1313")
    public void shouldGenerateAndParseTokenWithAllFields() {
        final JwtTokenUtil util = new JwtTokenUtil(SECRET, "1200000", "3000", "4000", this.networkController);

        final String token = util.generateAccessToken(this.user, "10.0.0.1", 5000L, "session-1");

        assertTrue(util.validate(token));
        assertEquals(util.getUserId(token), "7");
        assertEquals(util.getUsername(token), "user7");
        assertEquals(util.getSession(token), "session-1");
        assertEquals(util.getUserIp(token), "10.0.0.1");
        assertEquals(util.getHostMac(token), "AA-BB-CC-DD");
        assertNotNull(util.getExpirationDate(token));
    }

    @Test(groups = "jwtTokenUtil")
    public void shouldUseFallbackExpirationWhenInvalid() {
        final JwtTokenUtil util = new JwtTokenUtil(SECRET, "invalid", null, null, this.networkController);

        final long now = System.currentTimeMillis();
        final long standard = util.getJwtExpirationTime() - now;
        final long guest = util.getJwtGuestExpirationTime() - now;
        final long participant = util.getJwtParticipantExpirationTime() - now;

        assertTrue(standard > 1_199_000 && standard < 1_201_000);
        assertTrue(guest > 1_199_000 && guest < 1_201_000);
        assertTrue(participant > 1_199_000 && participant < 1_201_000);
    }

    @Test(groups = "jwtTokenUtil")
    public void shouldReturnFalseForInvalidToken() {
        final JwtTokenUtil util = new JwtTokenUtil(SECRET, "1200000", "1200000", "1200000", this.networkController);

        assertFalse(util.validate("not-a-jwt"));
    }

    @Test(groups = "jwtTokenUtil")
    public void shouldReturnNullForMissingSubjectParts() {
        final JwtTokenUtil util = new JwtTokenUtil(SECRET, "1200000", "1200000", "1200000", this.networkController);
        final String malformedSubjectToken = Jwts.builder().subject("only-id")
                .signWith(signingKey(SECRET), Jwts.SIG.HS512).compact();

        assertEquals(util.getUserId(malformedSubjectToken), "only-id");
        assertNull(util.getUsername(malformedSubjectToken));
        assertNull(util.getSession(malformedSubjectToken));
        assertNull(util.getUserIp(malformedSubjectToken));
        assertNull(util.getHostMac(malformedSubjectToken));
    }

    @Test(groups = "jwtTokenUtil")
    @SuppressWarnings("java:S1313")
    public void shouldGenerateSessionWhenNullSessionIsProvided() {
        final JwtTokenUtil util = new JwtTokenUtil(SECRET, "1200000", "1200000", "1200000", this.networkController);

        final String token = util.generateAccessToken(this.user, "10.0.0.1", 5000L, null);

        assertNotNull(util.getSession(token));
        assertFalse(util.getSession(token).isBlank());
    }

    @Test(groups = "jwtTokenUtil")
    public void shouldReturnFalseForExpiredToken() {
        final JwtTokenUtil util = new JwtTokenUtil(SECRET, "1200000", "1200000", "1200000", this.networkController);
        final String expiredToken = Jwts.builder().subject("7,user7,s,10.0.0.1,AA-BB")
                .issuedAt(new Date(System.currentTimeMillis() - 10_000L))
                .expiration(new Date(System.currentTimeMillis() - 1_000L))
                .signWith(signingKey(SECRET), Jwts.SIG.HS512)
                .compact();

        assertFalse(util.validate(expiredToken));
    }

    @Test(groups = "jwtTokenUtil")
    public void shouldReturnFalseForTokenWithWrongSignature() {
        final JwtTokenUtil util = new JwtTokenUtil(SECRET, "1200000", "1200000", "1200000", this.networkController);
        final String invalidSignatureToken = Jwts.builder().subject("7,user7,s,10.0.0.1,AA-BB")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 20_000L))
                .signWith(signingKey("different-secret"), Jwts.SIG.HS512)
                .compact();

        assertFalse(util.validate(invalidSignatureToken));
    }

    @Test(groups = "jwtTokenUtil")
    public void shouldFallbackGuestAndParticipantExpirationWhenInvalid() {
        final JwtTokenUtil util = new JwtTokenUtil(SECRET, "1200000", "invalid", "invalid", this.networkController);

        final long now = System.currentTimeMillis();
        final long guest = util.getJwtGuestExpirationTime() - now;
        final long participant = util.getJwtParticipantExpirationTime() - now;

        assertTrue(guest > 1_198_500 && guest < 1_201_500);
        assertTrue(participant > 1_198_500 && participant < 1_201_500);
    }

    @Test(groups = "jwtTokenUtil")
    public void shouldFallbackGuestAndParticipantExpirationWhenLiteralNullString() {
        final JwtTokenUtil util = new JwtTokenUtil(SECRET, "1200000", "null", "null", this.networkController);

        final long now = System.currentTimeMillis();
        final long guest = util.getJwtGuestExpirationTime() - now;
        final long participant = util.getJwtParticipantExpirationTime() - now;

        assertTrue(guest > 1_198_500 && guest < 1_201_500);
        assertTrue(participant > 1_198_500 && participant < 1_201_500);
    }

    @Test(groups = "jwtTokenUtil")
    @SuppressWarnings("java:S1313")
    public void shouldUseRandomSecretWhenConfiguredSecretIsBlank() {
        final JwtTokenUtil utilA = new JwtTokenUtil("   ", "1200000", "1200000", "1200000", this.networkController);
        final JwtTokenUtil utilB = new JwtTokenUtil("   ", "1200000", "1200000", "1200000", this.networkController);

        final String token = utilA.generateAccessToken(this.user, "10.0.0.1", 5000L, "session-1");

        assertTrue(utilA.validate(token));
        assertFalse(utilB.validate(token));
    }

    @Test(groups = "jwtTokenUtil")
    public void shouldParsePartialSubjectWithThreeFields() {
        final JwtTokenUtil util = new JwtTokenUtil(SECRET, "1200000", "1200000", "1200000", this.networkController);
        final String partialSubjectToken = Jwts.builder().subject("7,user7,session-x")
                .signWith(signingKey(SECRET), Jwts.SIG.HS512)
                .compact();

        assertEquals(util.getUserId(partialSubjectToken), "7");
        assertEquals(util.getUsername(partialSubjectToken), "user7");
        assertEquals(util.getSession(partialSubjectToken), "session-x");
        assertNull(util.getUserIp(partialSubjectToken));
        assertNull(util.getHostMac(partialSubjectToken));
    }
}

class TestNetworkController extends NetworkController {
    @Override
    public String getHostMac() {
        return "AA-BB-CC-DD";
    }
}

class TestUser implements IAuthenticatedUser {
    @Override
    public Integer getId() {
        return 7;
    }

    @Override
    public String getUsername() {
        return "user7";
    }

    @Override
    public String getName() {
        return "name";
    }

    @Override
    public String getLastname() {
        return "lastname";
    }

    @Override
    public java.util.Set<String> getRoles() {
        return java.util.Set.of("ROLE_USER");
    }

    @Override
    public String getCreatedBy() {
        return "system";
    }

    @Override
    public java.time.LocalDateTime getCreatedAt() {
        return java.time.LocalDateTime.now();
    }

    @Override
    public String getUpdatedBy() {
        return "system";
    }

    @Override
    public java.time.LocalDateTime getUpdatedAt() {
        return java.time.LocalDateTime.now();
    }
}
