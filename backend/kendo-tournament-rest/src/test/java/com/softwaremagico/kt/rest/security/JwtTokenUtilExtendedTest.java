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

package com.softwaremagico.kt.rest.security;

import com.softwaremagico.kt.persistence.entities.IAuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Extended test suite for {@link JwtTokenUtil}.
 * Tests JWT token generation, parsing, validation and claim extraction.
 */
@ExtendWith(MockitoExtension.class)
class JwtTokenUtilExtendedTest {

    @Mock
    private IAuthenticatedUser authenticatedUser;

    @Mock
    private NetworkController networkController;

    private JwtTokenUtil jwtTokenUtil;
    private static final String TEST_USER_ID = "12345";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_IP = "192.168.1.1";
    private static final String TEST_MAC = "00:11:22:33:44:55";

    @BeforeEach
    void setUp() {
        when(authenticatedUser.getId()).thenReturn(Integer.parseInt(TEST_USER_ID));
        when(authenticatedUser.getUsername()).thenReturn(TEST_USERNAME);
        when(networkController.getHostMac()).thenReturn(TEST_MAC);

        jwtTokenUtil = new JwtTokenUtil(
                "test-secret-key-for-testing-purposes-1234567890",
                "120000",
                null,
                null,
                networkController
        );
    }

    @Test
    void testGenerateAccessTokenWithoutSession() {
        String token = jwtTokenUtil.generateAccessToken(authenticatedUser, TEST_IP);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts separated by dots
    }

    @Test
    void testGenerateAccessTokenWithSession() {
        String session = "test-session-123";
        String token = jwtTokenUtil.generateAccessToken(authenticatedUser, TEST_IP, session);

        assertNotNull(token);
        assertEquals(session, jwtTokenUtil.getSession(token));
    }

    @Test
    void testGenerateAccessTokenWithCustomExpiration() {
        long expirationTime = 3600000L; // 1 hour
        String token = jwtTokenUtil.generateAccessToken(authenticatedUser, TEST_IP, expirationTime);

        assertNotNull(token);
        Date expirationDate = jwtTokenUtil.getExpirationDate(token);
        assertNotNull(expirationDate);
        assertTrue(expirationDate.getTime() > System.currentTimeMillis());
    }

    @Test
    void testGenerateAccessTokenWithCustomExpirationAndSession() {
        long expirationTime = 3600000L;
        String session = "custom-session-456";
        String token = jwtTokenUtil.generateAccessToken(authenticatedUser, TEST_IP, expirationTime, session);

        assertNotNull(token);
        assertEquals(session, jwtTokenUtil.getSession(token));
    }

    @Test
    void testValidateToken() {
        String token = jwtTokenUtil.generateAccessToken(authenticatedUser, TEST_IP);

        assertTrue(jwtTokenUtil.validate(token));
    }

    @Test
    void testValidateInvalidToken() {
        String invalidToken = "invalid.token.string";

        assertFalse(jwtTokenUtil.validate(invalidToken));
    }

    @Test
    void testValidateEmptyToken() {
        assertFalse(jwtTokenUtil.validate(""));
    }

    @Test
    void testValidateNullToken() {
        assertFalse(jwtTokenUtil.validate(null));
    }

    @Test
    void testGetUserIdFromToken() {
        String token = jwtTokenUtil.generateAccessToken(authenticatedUser, TEST_IP);
        String userId = jwtTokenUtil.getUserId(token);

        assertEquals(TEST_USER_ID, userId);
    }

    @Test
    void testGetUsernameFromToken() {
        String token = jwtTokenUtil.generateAccessToken(authenticatedUser, TEST_IP);
        String username = jwtTokenUtil.getUsername(token);

        assertEquals(TEST_USERNAME, username);
    }

    @Test
    void testGetSessionFromToken() {
        String session = "test-session-789";
        String token = jwtTokenUtil.generateAccessToken(authenticatedUser, TEST_IP, session);
        String retrievedSession = jwtTokenUtil.getSession(token);

        assertEquals(session, retrievedSession);
    }

    @Test
    void testGetUserIpFromToken() {
        String token = jwtTokenUtil.generateAccessToken(authenticatedUser, TEST_IP);
        String userIp = jwtTokenUtil.getUserIp(token);

        assertEquals(TEST_IP, userIp);
    }

    @Test
    void testGetHostMacFromToken() {
        String token = jwtTokenUtil.generateAccessToken(authenticatedUser, TEST_IP);
        String hostMac = jwtTokenUtil.getHostMac(token);

        assertEquals(TEST_MAC, hostMac);
    }

    @Test
    void testGetExpirationDateFromToken() {
        String token = jwtTokenUtil.generateAccessToken(authenticatedUser, TEST_IP);
        Date expirationDate = jwtTokenUtil.getExpirationDate(token);

        assertNotNull(expirationDate);
        assertTrue(expirationDate.getTime() > System.currentTimeMillis());
    }

    @Test
    void testGetJwtExpirationTime() {
        long expirationTime = jwtTokenUtil.getJwtExpirationTime();

        assertTrue(expirationTime > System.currentTimeMillis());
    }

    @Test
    void testGetJwtGuestExpirationTime() {
        long expirationTime = jwtTokenUtil.getJwtGuestExpirationTime();

        assertTrue(expirationTime > System.currentTimeMillis());
    }

    @Test
    void testGetJwtParticipantExpirationTime() {
        long expirationTime = jwtTokenUtil.getJwtParticipantExpirationTime();

        assertTrue(expirationTime > System.currentTimeMillis());
    }

    @Test
    void testGenerateAccessTokenMultipleTimes() {
        String token1 = jwtTokenUtil.generateAccessToken(authenticatedUser, TEST_IP);
        String token2 = jwtTokenUtil.generateAccessToken(authenticatedUser, TEST_IP);

        // Different sessions should be generated
        assertNotEquals(jwtTokenUtil.getSession(token1), jwtTokenUtil.getSession(token2));
    }

    @Test
    void testTokenSignatureValidation() {
        String token = jwtTokenUtil.generateAccessToken(authenticatedUser, TEST_IP);

        // Tamper with the signature
        String[] parts = token.split("\\.");
        String tamperedToken = parts[0] + "." + parts[1] + ".invalidsignature";

        assertFalse(jwtTokenUtil.validate(tamperedToken));
    }

    @Test
    void testConstructorWithNullSecret() {
        JwtTokenUtil util = new JwtTokenUtil(
                null,
                "120000",
                null,
                null,
                networkController
        );

        assertNotNull(util);
        String token = util.generateAccessToken(authenticatedUser, TEST_IP);
        assertNotNull(token);
    }

    @Test
    void testConstructorWithBlankSecret() {
        JwtTokenUtil util = new JwtTokenUtil(
                "   ",
                "120000",
                null,
                null,
                networkController
        );

        assertNotNull(util);
        String token = util.generateAccessToken(authenticatedUser, TEST_IP);
        assertNotNull(token);
    }

    @Test
    void testConstructorWithInvalidExpirationFormat() {
        JwtTokenUtil util = new JwtTokenUtil(
                "test-secret",
                "not-a-number",
                null,
                null,
                networkController
        );

        assertNotNull(util);
        // Should use default expiration
        assertTrue(util.getJwtExpirationTime() > System.currentTimeMillis());
    }

    @Test
    void testGenerateAccessTokenWithNullSession() {
        String token = jwtTokenUtil.generateAccessToken(authenticatedUser, TEST_IP, (String) null);

        assertNotNull(token);
        assertNotNull(jwtTokenUtil.getSession(token)); // Should have a generated session
    }

    @Test
    void testExpirationTimeConsistency() {
        long before = System.currentTimeMillis();
        long expirationTime = jwtTokenUtil.getJwtExpirationTime();
        long after = System.currentTimeMillis();

        assertTrue(expirationTime >= before + 120000);
        assertTrue(expirationTime <= after + 120000 + 1000); // Allow 1 second tolerance
    }
}

