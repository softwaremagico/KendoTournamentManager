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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link BruteForceService}.
 * Tests failed login attempt tracking and blocking mechanism.
 */
class BruteForceServiceTest {

    private BruteForceService bruteForceService;
    private static final String TEST_IP = "192.168.1.100";
    private static final String TEST_USERNAME = "testuser";
    private static final String ANOTHER_IP = "192.168.1.101";

    @BeforeEach
    void setUp() {
        bruteForceService = new BruteForceService();
    }

    @Test
    void testLoginSucceededRemovesEntry() {
        bruteForceService.loginFailed(TEST_IP);
        bruteForceService.loginFailed(TEST_IP);

        assertFalse(bruteForceService.isBlocked(TEST_IP));

        bruteForceService.loginSucceeded(TEST_IP);

        assertFalse(bruteForceService.isBlocked(TEST_IP));
    }

    @Test
    void testLoginFailedIncrementsCounter() {
        assertFalse(bruteForceService.isBlocked(TEST_IP));

        bruteForceService.loginFailed(TEST_IP);
        assertFalse(bruteForceService.isBlocked(TEST_IP));
    }

    @Test
    void testBlockAfterMaxAttempts() {
        for (int i = 0; i < BruteForceService.MAX_ATTEMPTS - 1; i++) {
            bruteForceService.loginFailed(TEST_IP);
            assertFalse(bruteForceService.isBlocked(TEST_IP),
                    "Should not be blocked after " + (i + 1) + " attempts");
        }

        bruteForceService.loginFailed(TEST_IP);
        assertTrue(bruteForceService.isBlocked(TEST_IP),
                "Should be blocked after " + BruteForceService.MAX_ATTEMPTS + " attempts");
    }

    @Test
    void testMultipleIPsIndependent() {
        bruteForceService.loginFailed(TEST_IP);
        bruteForceService.loginFailed(TEST_IP);

        assertFalse(bruteForceService.isBlocked(ANOTHER_IP));
        assertFalse(bruteForceService.isBlocked(TEST_IP));
    }

    @Test
    void testBlockedIPNotResetByOtherIP() {
        for (int i = 0; i < BruteForceService.MAX_ATTEMPTS; i++) {
            bruteForceService.loginFailed(TEST_IP);
        }

        assertTrue(bruteForceService.isBlocked(TEST_IP));

        bruteForceService.loginSucceeded(ANOTHER_IP);

        assertTrue(bruteForceService.isBlocked(TEST_IP));
    }

    @Test
    void testResetBlockedIP() {
        for (int i = 0; i < BruteForceService.MAX_ATTEMPTS; i++) {
            bruteForceService.loginFailed(TEST_IP);
        }

        assertTrue(bruteForceService.isBlocked(TEST_IP));

        bruteForceService.loginSucceeded(TEST_IP);

        assertFalse(bruteForceService.isBlocked(TEST_IP));
    }

    @Test
    void testSingleFailureNotBlocking() {
        bruteForceService.loginFailed(TEST_IP);

        assertFalse(bruteForceService.isBlocked(TEST_IP));
    }

    @Test
    void testHalfwayToBlockNotBlocking() {
        int halfAttempts = BruteForceService.MAX_ATTEMPTS / 2;
        for (int i = 0; i < halfAttempts; i++) {
            bruteForceService.loginFailed(TEST_IP);
        }

        assertFalse(bruteForceService.isBlocked(TEST_IP));
    }

    @Test
    void testExactlyMaxAttemptsBlocks() {
        for (int i = 0; i < BruteForceService.MAX_ATTEMPTS; i++) {
            bruteForceService.loginFailed(TEST_IP);
        }

        assertTrue(bruteForceService.isBlocked(TEST_IP));
    }

    @Test
    void testMoreThanMaxAttemptsStaysBlocked() {
        for (int i = 0; i < BruteForceService.MAX_ATTEMPTS + 5; i++) {
            bruteForceService.loginFailed(TEST_IP);
        }

        assertTrue(bruteForceService.isBlocked(TEST_IP));
    }

    @Test
    void testUsernameBlocking() {
        for (int i = 0; i < BruteForceService.MAX_ATTEMPTS; i++) {
            bruteForceService.loginFailed(TEST_USERNAME);
        }

        assertTrue(bruteForceService.isBlocked(TEST_USERNAME));
    }

    @Test
    void testUsernameAndIPIndependent() {
        for (int i = 0; i < BruteForceService.MAX_ATTEMPTS; i++) {
            bruteForceService.loginFailed(TEST_IP);
        }

        assertTrue(bruteForceService.isBlocked(TEST_IP));
        assertFalse(bruteForceService.isBlocked(TEST_USERNAME));
    }

    @Test
    void testMultipleUsersOnSameIP() {
        bruteForceService.loginFailed(TEST_IP);
        bruteForceService.loginFailed(ANOTHER_IP);

        assertFalse(bruteForceService.isBlocked(TEST_IP));
        assertFalse(bruteForceService.isBlocked(ANOTHER_IP));
    }

    @Test
    void testBlockMultipleIPsSimultaneously() {
        for (int i = 0; i < BruteForceService.MAX_ATTEMPTS; i++) {
            bruteForceService.loginFailed(TEST_IP);
            bruteForceService.loginFailed(ANOTHER_IP);
        }

        assertTrue(bruteForceService.isBlocked(TEST_IP));
        assertTrue(bruteForceService.isBlocked(ANOTHER_IP));
    }

    @Test
    void testExpirationTimeConstant() {
        long expirationTime = bruteForceService.getExpirationTime();

        assertEquals(10 * 60 * 1000L, expirationTime);
    }

    @Test
    void testIsDirtyAlwaysFalse() {
        assertFalse(bruteForceService.isDirty(1));
        assertFalse(bruteForceService.isDirty(BruteForceService.MAX_ATTEMPTS));
        assertFalse(bruteForceService.isDirty(0));
    }

    @Test
    void testRepeatedSuccessAfterBlock() {
        for (int i = 0; i < BruteForceService.MAX_ATTEMPTS; i++) {
            bruteForceService.loginFailed(TEST_IP);
        }

        assertTrue(bruteForceService.isBlocked(TEST_IP));

        bruteForceService.loginSucceeded(TEST_IP);
        assertFalse(bruteForceService.isBlocked(TEST_IP));

        bruteForceService.loginFailed(TEST_IP);
        assertFalse(bruteForceService.isBlocked(TEST_IP));
    }

    @Test
    void testNullKeyHandling() {
        // Behavior with null key - should not throw exception
        assertDoesNotThrow(() -> bruteForceService.loginFailed(null));
        assertFalse(bruteForceService.isBlocked(null));
    }

    @Test
    void testEmptyStringKey() {
        bruteForceService.loginFailed("");

        assertFalse(bruteForceService.isBlocked(""));
    }

    @Test
    void testWhitespaceKey() {
        String whitespaceKey = "   ";
        bruteForceService.loginFailed(whitespaceKey);

        assertFalse(bruteForceService.isBlocked(whitespaceKey));
    }

    @Test
    void testCaseSensitiveKeys() {
        String lowercase = "testip";
        String uppercase = "TESTIP";

        bruteForceService.loginFailed(lowercase);

        assertFalse(bruteForceService.isBlocked(uppercase));
    }

    @Test
    void testMaxAttemptsConstant() {
        assertEquals(10, BruteForceService.MAX_ATTEMPTS);
    }

    @Test
    void testConsecutiveFailuresOnDifferentKeys() {
        for (int i = 0; i < 5; i++) {
            bruteForceService.loginFailed(TEST_IP);
            bruteForceService.loginFailed(ANOTHER_IP);
            bruteForceService.loginFailed(TEST_USERNAME);
        }

        assertFalse(bruteForceService.isBlocked(TEST_IP));
        assertFalse(bruteForceService.isBlocked(ANOTHER_IP));
        assertFalse(bruteForceService.isBlocked(TEST_USERNAME));
    }
}

