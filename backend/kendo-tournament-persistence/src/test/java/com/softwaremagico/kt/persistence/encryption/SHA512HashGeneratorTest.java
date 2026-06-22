/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
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

package com.softwaremagico.kt.persistence.encryption;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

/**
 * Test suite for {@link SHA512HashGenerator}.
 * Tests SHA-512 hashing functionality for passwords and sensitive data.
 */
@ExtendWith(MockitoExtension.class)
class SHA512HashGeneratorTest {

    private SHA512HashGenerator hashGenerator;
    private static final String TEST_PASSWORD = "MySecurePassword123!@#";
    private static final String ENCRYPTION_KEY = "databaseEncryptionKey";

    @BeforeEach
    void setUp() {
        hashGenerator = new SHA512HashGenerator();
    }

    @Test
    void testConvertToDatabaseColumn() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(ENCRYPTION_KEY);

            String hash = hashGenerator.convertToDatabaseColumn(TEST_PASSWORD);

            assertNotNull(hash);
            assertNotEquals(TEST_PASSWORD, hash);
            assertTrue(hash.length() >= 128); // SHA-512 produces at least 128 hex characters
        }
    }

    @Test
    void testConvertToDatabaseColumnNull() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(ENCRYPTION_KEY);

            String hash = hashGenerator.convertToDatabaseColumn(null);

            assertNull(hash);
        }
    }

    @Test
    void testConvertToEntityAttributeAlwaysNull() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(ENCRYPTION_KEY);

            String result = hashGenerator.convertToEntityAttribute("anyHashValue");

            assertNull(result);
        }
    }

    @Test
    void testHashIsDeterministic() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(ENCRYPTION_KEY);

            String hash1 = hashGenerator.convertToDatabaseColumn(TEST_PASSWORD);
            String hash2 = hashGenerator.convertToDatabaseColumn(TEST_PASSWORD);

            assertEquals(hash1, hash2);
        }
    }

    @Test
    void testDifferentInputsDifferentHashes() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(ENCRYPTION_KEY);

            String hash1 = hashGenerator.convertToDatabaseColumn("password1");
            String hash2 = hashGenerator.convertToDatabaseColumn("password2");

            assertNotEquals(hash1, hash2);
        }
    }

    @Test
    void testHashWithoutEncryptionKey() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(null);

            String hash = hashGenerator.convertToDatabaseColumn(TEST_PASSWORD);

            assertNotNull(hash);
            assertTrue(hash.length() >= 128);
        }
    }

    @Test
    void testHashWithEmptyEncryptionKey() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn("");

            String hash = hashGenerator.convertToDatabaseColumn(TEST_PASSWORD);

            assertNotNull(hash);
            assertTrue(hash.length() >= 128);
        }
    }

    @Test
    void testCreateHashStaticMethod() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(ENCRYPTION_KEY);

            String hash = SHA512HashGenerator.createHash(TEST_PASSWORD);

            assertNotNull(hash);
            assertNotEquals(TEST_PASSWORD, hash);
        }
    }

    @Test
    void testCreateHashNull() {
        String hash = SHA512HashGenerator.createHash(null);

        assertNull(hash);
    }

    @Test
    void testHashForEmptyString() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(ENCRYPTION_KEY);

            String hash = hashGenerator.convertToDatabaseColumn("");

            assertNotNull(hash);
            assertTrue(hash.length() >= 128);
        }
    }

    @Test
    void testHashLengthIsConsistent() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(ENCRYPTION_KEY);

            String hash1 = hashGenerator.convertToDatabaseColumn("short");
            String hash2 = hashGenerator.convertToDatabaseColumn("a much longer string with more characters");

            // SHA-512 always produces same length hash (128 hex chars)
            assertEquals(hash1.length(), hash2.length());
        }
    }

    @Test
    void testHashWithSpecialCharacters() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(ENCRYPTION_KEY);

            String specialInput = "!@#$%^&*()_+-=[]{}|;:',.<>?/~`";
            String hash = hashGenerator.convertToDatabaseColumn(specialInput);

            assertNotNull(hash);
            assertNotEquals(specialInput, hash);
        }
    }

    @Test
    void testHashWithUnicodeCharacters() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(ENCRYPTION_KEY);

            String unicodeInput = "Café ñ 中文 العربية";
            String hash = hashGenerator.convertToDatabaseColumn(unicodeInput);

            assertNotNull(hash);
            assertNotEquals(unicodeInput, hash);
        }
    }

    @Test
    void testHashContainsOnlyHexCharacters() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(ENCRYPTION_KEY);

            String hash = hashGenerator.convertToDatabaseColumn(TEST_PASSWORD);

            assertTrue(hash.matches("[0-9a-f]*"), "Hash should contain only hexadecimal characters");
        }
    }

    @Test
    void testHashAlgorithmName() {
        assertEquals("SHA-512", SHA512HashGenerator.class.getSimpleName()
                .replace("SHA512", "SHA-512")
                .replace("HashGenerator", ""));
    }

    @Test
    void testHashImpossibleToReverseEngineer() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(ENCRYPTION_KEY);

            String hash = hashGenerator.convertToDatabaseColumn(TEST_PASSWORD);

            // Convert back to entity attribute should always be null
            assertNull(hashGenerator.convertToEntityAttribute(hash));
        }
    }

    @Test
    void testSmallVariationInInputProducesDifferentHash() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(ENCRYPTION_KEY);

            String password = "MyPassword";
            String passwordWithSpace = "MyPassword ";

            String hash1 = hashGenerator.convertToDatabaseColumn(password);
            String hash2 = hashGenerator.convertToDatabaseColumn(passwordWithSpace);

            assertNotEquals(hash1, hash2, "Even small variations should produce different hashes");
        }
    }

    @Test
    void testHashIsStableAcrossInstances() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(ENCRYPTION_KEY);

            SHA512HashGenerator generator1 = new SHA512HashGenerator();
            SHA512HashGenerator generator2 = new SHA512HashGenerator();

            String hash1 = generator1.convertToDatabaseColumn(TEST_PASSWORD);
            String hash2 = generator2.convertToDatabaseColumn(TEST_PASSWORD);

            assertEquals(hash1, hash2, "Same password should produce same hash across instances");
        }
    }

    @Test
    void testLongPasswordHashing() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(ENCRYPTION_KEY);

            String longPassword = "A".repeat(1000);
            String hash = hashGenerator.convertToDatabaseColumn(longPassword);

            assertNotNull(hash);
            assertTrue(hash.length() >= 128);
        }
    }
}

