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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Test suite for {@link BruteForceService}.
 * Tests failed login attempt tracking and blocking mechanism.
 */
@Test(groups = "bruteForceService")
public class BruteForceServiceTests {

    private static final String TEST_IP = "192.168.1.100";
    private static final String TEST_USERNAME = "testuser";
    private static final String ANOTHER_IP = "192.168.1.101";

    private BruteForceService bruteForceService;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        this.bruteForceService = new BruteForceService();
    }

    @Test
    public void testLoginSucceededRemovesEntry() {
        this.bruteForceService.loginFailed(TEST_IP);
        this.bruteForceService.loginFailed(TEST_IP);

        assertFalse(this.bruteForceService.isBlocked(TEST_IP));

        this.bruteForceService.loginSucceeded(TEST_IP);

        assertFalse(this.bruteForceService.isBlocked(TEST_IP));
    }

    @Test
    public void testLoginFailedIncrementsCounter() {
        assertFalse(this.bruteForceService.isBlocked(TEST_IP));

        this.bruteForceService.loginFailed(TEST_IP);
        assertFalse(this.bruteForceService.isBlocked(TEST_IP));
    }

    @Test
    public void testBlockAfterMaxAttempts() {
        for (int i = 0; i < BruteForceService.MAX_ATTEMPTS - 1; i++) {
            this.bruteForceService.loginFailed(TEST_IP);
            assertFalse(this.bruteForceService.isBlocked(TEST_IP),
                    "Should not be blocked after " + (i + 1) + " attempts");
        }

        this.bruteForceService.loginFailed(TEST_IP);
        assertTrue(this.bruteForceService.isBlocked(TEST_IP),
                "Should be blocked after " + BruteForceService.MAX_ATTEMPTS + " attempts");
    }

    @Test
    public void testMultipleIPsIndependent() {
        this.bruteForceService.loginFailed(TEST_IP);
        this.bruteForceService.loginFailed(TEST_IP);

        assertFalse(this.bruteForceService.isBlocked(ANOTHER_IP));
        assertFalse(this.bruteForceService.isBlocked(TEST_IP));
    }

    @Test
    public void testBlockedIPNotResetByOtherIP() {
        for (int i = 0; i < BruteForceService.MAX_ATTEMPTS; i++) {
            this.bruteForceService.loginFailed(TEST_IP);
        }

        assertTrue(this.bruteForceService.isBlocked(TEST_IP));

        this.bruteForceService.loginSucceeded(ANOTHER_IP);

        assertTrue(this.bruteForceService.isBlocked(TEST_IP));
    }

    @Test
    public void testResetBlockedIP() {
        for (int i = 0; i < BruteForceService.MAX_ATTEMPTS; i++) {
            this.bruteForceService.loginFailed(TEST_IP);
        }

        assertTrue(this.bruteForceService.isBlocked(TEST_IP));

        this.bruteForceService.loginSucceeded(TEST_IP);

        assertFalse(this.bruteForceService.isBlocked(TEST_IP));
    }

    @Test
    public void testSingleFailureNotBlocking() {
        this.bruteForceService.loginFailed(TEST_IP);

        assertFalse(this.bruteForceService.isBlocked(TEST_IP));
    }

    @Test
    public void testHalfwayToBlockNotBlocking() {
        final int halfAttempts = BruteForceService.MAX_ATTEMPTS / 2;
        for (int i = 0; i < halfAttempts; i++) {
            this.bruteForceService.loginFailed(TEST_IP);
        }

        assertFalse(this.bruteForceService.isBlocked(TEST_IP));
    }

    @Test
    public void testExactlyMaxAttemptsBlocks() {
        for (int i = 0; i < BruteForceService.MAX_ATTEMPTS; i++) {
            this.bruteForceService.loginFailed(TEST_IP);
        }

        assertTrue(this.bruteForceService.isBlocked(TEST_IP));
    }

    @Test
    public void testMoreThanMaxAttemptsStaysBlocked() {
        for (int i = 0; i < BruteForceService.MAX_ATTEMPTS + 5; i++) {
            this.bruteForceService.loginFailed(TEST_IP);
        }

        assertTrue(this.bruteForceService.isBlocked(TEST_IP));
    }

    @Test
    public void testUsernameBlocking() {
        for (int i = 0; i < BruteForceService.MAX_ATTEMPTS; i++) {
            this.bruteForceService.loginFailed(TEST_USERNAME);
        }

        assertTrue(this.bruteForceService.isBlocked(TEST_USERNAME));
    }

    @Test
    public void testUsernameAndIPIndependent() {
        for (int i = 0; i < BruteForceService.MAX_ATTEMPTS; i++) {
            this.bruteForceService.loginFailed(TEST_IP);
        }

        assertTrue(this.bruteForceService.isBlocked(TEST_IP));
        assertFalse(this.bruteForceService.isBlocked(TEST_USERNAME));
    }

    @Test
    public void testMultipleUsersOnSameIP() {
        this.bruteForceService.loginFailed(TEST_IP);
        this.bruteForceService.loginFailed(ANOTHER_IP);

        assertFalse(this.bruteForceService.isBlocked(TEST_IP));
        assertFalse(this.bruteForceService.isBlocked(ANOTHER_IP));
    }

    @Test
    public void testBlockMultipleIPsSimultaneously() {
        for (int i = 0; i < BruteForceService.MAX_ATTEMPTS; i++) {
            this.bruteForceService.loginFailed(TEST_IP);
            this.bruteForceService.loginFailed(ANOTHER_IP);
        }

        assertTrue(this.bruteForceService.isBlocked(TEST_IP));
        assertTrue(this.bruteForceService.isBlocked(ANOTHER_IP));
    }

    @Test
    public void testExpirationTimeConstant() {
        final long expirationTime = this.bruteForceService.getExpirationTime();

        assertEquals(expirationTime, 10 * 60 * 1000L);
    }

    @Test
    public void testIsDirtyAlwaysFalse() {
        assertFalse(this.bruteForceService.isDirty(1));
        assertFalse(this.bruteForceService.isDirty(BruteForceService.MAX_ATTEMPTS));
        assertFalse(this.bruteForceService.isDirty(0));
    }

    @Test
    public void testRepeatedSuccessAfterBlock() {
        for (int i = 0; i < BruteForceService.MAX_ATTEMPTS; i++) {
            this.bruteForceService.loginFailed(TEST_IP);
        }

        assertTrue(this.bruteForceService.isBlocked(TEST_IP));

        this.bruteForceService.loginSucceeded(TEST_IP);
        assertFalse(this.bruteForceService.isBlocked(TEST_IP));

        this.bruteForceService.loginFailed(TEST_IP);
        assertFalse(this.bruteForceService.isBlocked(TEST_IP));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testNullKeyHandling() {
        // The underlying pool is backed by a ConcurrentHashMap, which does not accept null keys.
        this.bruteForceService.loginFailed(null);
    }

    @Test
    public void testEmptyStringKey() {
        this.bruteForceService.loginFailed("");

        assertFalse(this.bruteForceService.isBlocked(""));
    }

    @Test
    public void testWhitespaceKey() {
        final String whitespaceKey = "   ";
        this.bruteForceService.loginFailed(whitespaceKey);

        assertFalse(this.bruteForceService.isBlocked(whitespaceKey));
    }

    @Test
    public void testCaseSensitiveKeys() {
        final String lowercase = "testip";
        final String uppercase = "TESTIP";

        this.bruteForceService.loginFailed(lowercase);

        assertFalse(this.bruteForceService.isBlocked(uppercase));
    }

    @Test
    public void testMaxAttemptsConstant() {
        assertEquals(BruteForceService.MAX_ATTEMPTS, 10);
    }

    @Test
    public void testConsecutiveFailuresOnDifferentKeys() {
        for (int i = 0; i < 5; i++) {
            this.bruteForceService.loginFailed(TEST_IP);
            this.bruteForceService.loginFailed(ANOTHER_IP);
            this.bruteForceService.loginFailed(TEST_USERNAME);
        }

        assertFalse(this.bruteForceService.isBlocked(TEST_IP));
        assertFalse(this.bruteForceService.isBlocked(ANOTHER_IP));
        assertFalse(this.bruteForceService.isBlocked(TEST_USERNAME));
    }
}

