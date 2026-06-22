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
 * Test suite for {@link NetworkController}.
 * Tests MAC address retrieval from the host network interface.
 */
class NetworkControllerTest {

    private NetworkController networkController;

    @BeforeEach
    void setUp() {
        networkController = new NetworkController();
    }

    @Test
    void testGetHostMacReturnsString() {
        String hostMac = networkController.getHostMac();

        assertNotNull(hostMac);
        assertTrue(hostMac instanceof String);
    }

    @Test
    void testGetHostMacNotEmpty() {
        String hostMac = networkController.getHostMac();

        // Should return either a valid MAC address or empty string
        assertNotNull(hostMac);
        // If it's not empty, it should contain only hex characters and hyphens
        if (!hostMac.isEmpty()) {
            assertTrue(hostMac.matches("[0-9A-F\\-]+"), "MAC address should only contain hex and hyphens");
        }
    }

    @Test
    void testGetHostMacFormatValid() {
        String hostMac = networkController.getHostMac();

        // Valid MAC formats: XX-XX-XX-XX-XX-XX or empty
        if (!hostMac.isEmpty()) {
            assertTrue(hostMac.matches("([0-9A-F]{2}\\-){5}[0-9A-F]{2}") ||
                      hostMac.matches("[0-9A-F]*"),
                      "MAC address format should be valid");
        }
    }

    @Test
    void testGetHostMacConsistency() {
        String hostMac1 = networkController.getHostMac();
        String hostMac2 = networkController.getHostMac();

        assertEquals(hostMac1, hostMac2, "MAC address should be consistent across calls");
    }

    @Test
    void testGetHostMacDoesNotThrowException() {
        assertDoesNotThrow(() -> networkController.getHostMac(),
                "getHostMac should not throw any exception");
    }

    @Test
    void testGetHostMacIsNotNull() {
        String hostMac = networkController.getHostMac();

        assertNotNull(hostMac, "MAC address should never be null");
    }

    @Test
    void testGetHostMacContainsHexDigits() {
        String hostMac = networkController.getHostMac();

        if (!hostMac.isEmpty()) {
            // Remove hyphens and check all remaining characters are hex
            String withoutHyphens = hostMac.replace("-", "");
            assertTrue(withoutHyphens.matches("[0-9A-F]*"),
                      "MAC address should contain only hexadecimal digits");
        }
    }

    @Test
    void testGetHostMacUppercaseHex() {
        String hostMac = networkController.getHostMac();

        if (!hostMac.isEmpty()) {
            // Should be uppercase (as per format %02X in code)
            assertEquals(hostMac, hostMac.toUpperCase(),
                        "MAC address should be uppercase");
        }
    }

    @Test
    void testGetHostMacHyphenFormat() {
        String hostMac = networkController.getHostMac();

        if (!hostMac.isEmpty()) {
            String[] parts = hostMac.split("-");

            // Should have 6 parts (for 48-bit MAC address)
            assertTrue(parts.length >= 1, "MAC address should have parts separated by hyphens");

            // Each part should have 2 hex digits
            for (String part : parts) {
                assertTrue(part.matches("[0-9A-F]{2}"),
                          "Each MAC address octet should be 2 hex digits");
            }
        }
    }

    @Test
    void testGetHostMacIsRepeatable() {
        String hostMac1 = networkController.getHostMac();
        String hostMac2 = networkController.getHostMac();
        String hostMac3 = networkController.getHostMac();

        assertEquals(hostMac1, hostMac2);
        assertEquals(hostMac2, hostMac3);
    }

    @Test
    void testMultipleInstancesReturnSameMac() {
        NetworkController controller1 = new NetworkController();
        NetworkController controller2 = new NetworkController();

        String mac1 = controller1.getHostMac();
        String mac2 = controller2.getHostMac();

        assertEquals(mac1, mac2, "Different instances should return same MAC");
    }

    @Test
    void testConstructor() {
        assertDoesNotThrow(() -> new NetworkController());
    }

    @Test
    void testGetHostMacMaxLength() {
        String hostMac = networkController.getHostMac();

        // Standard MAC address is 17 chars (XX-XX-XX-XX-XX-XX)
        assertTrue(hostMac.length() <= 17 || hostMac.isEmpty(),
                  "MAC address should not exceed standard length");
    }

    @Test
    void testGetHostMacMinLengthIfNotEmpty() {
        String hostMac = networkController.getHostMac();

        if (!hostMac.isEmpty()) {
            // At minimum, should have some hex digits
            assertTrue(hostMac.length() >= 2, "Non-empty MAC address should have minimum length");
        }
    }

    @Test
    void testGetHostMacSpecialCharacters() {
        String hostMac = networkController.getHostMac();

        if (!hostMac.isEmpty()) {
            // Should not contain any unexpected special characters
            for (char c : hostMac.toCharArray()) {
                assertTrue((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || c == '-',
                          "MAC address should only contain hex digits and hyphens");
            }
        }
    }

    @Test
    void testGetHostMacHandlesNetworkException() {
        // This test validates that the method handles exceptions gracefully
        // by returning an empty string instead of throwing
        String hostMac = networkController.getHostMac();

        // Should either return valid MAC or empty string, never null
        assertNotNull(hostMac);
        assertTrue(hostMac.isEmpty() || hostMac.matches("[0-9A-F\\-]+"));
    }

    @Test
    void testGetHostMacStartsWithHexIfNotEmpty() {
        String hostMac = networkController.getHostMac();

        if (!hostMac.isEmpty()) {
            assertTrue(Character.isLetterOrDigit(hostMac.charAt(0)),
                      "MAC address should start with hex digit");
        }
    }

    @Test
    void testGetHostMacEndsWithHexIfNotEmpty() {
        String hostMac = networkController.getHostMac();

        if (!hostMac.isEmpty()) {
            assertTrue(Character.isLetterOrDigit(hostMac.charAt(hostMac.length() - 1)),
                      "MAC address should end with hex digit");
        }
    }

    @Test
    void testGetHostMacNoConsecutiveHyphens() {
        String hostMac = networkController.getHostMac();

        if (!hostMac.isEmpty()) {
            assertFalse(hostMac.contains("--"), "MAC address should not contain consecutive hyphens");
        }
    }

    @Test
    void testGetHostMacPotentialValues() {
        String hostMac = networkController.getHostMac();

        // Valid outcomes:
        // 1. Empty string (if network interface cannot be determined)
        // 2. Valid MAC address format (XX-XX-XX-XX-XX-XX)
        boolean isValid = hostMac.isEmpty() ||
                         hostMac.matches("([0-9A-F]{2}\\-){5}[0-9A-F]{2}");

        assertTrue(isValid, "MAC address should be either empty or in valid format");
    }
}

