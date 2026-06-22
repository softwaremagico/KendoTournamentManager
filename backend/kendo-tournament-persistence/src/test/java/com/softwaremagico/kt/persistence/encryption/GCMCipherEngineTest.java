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
 * Test suite for {@link GCMCipherEngine}.
 * Tests AES/GCM encryption and decryption with high security requirements.
 */
@ExtendWith(MockitoExtension.class)
class GCMCipherEngineTest {

    private GCMCipherEngine cipherEngine;
    private static final String TEST_PASSWORD = "testPassword123!@#";
    private static final String TEST_DATA = "This is sensitive data";
    private static final String EMPTY_STRING = "";

    @BeforeEach
    void setUp() {
        cipherEngine = new GCMCipherEngine();
    }

    @Test
    void testEncryptAndDecrypt() throws InvalidEncryptionException {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(TEST_PASSWORD);

            String encrypted = cipherEngine.encrypt(TEST_DATA);
            assertNotNull(encrypted);
            assertNotEquals(TEST_DATA, encrypted);

            String decrypted = cipherEngine.decrypt(encrypted);
            assertEquals(TEST_DATA, decrypted);
        }
    }

    @Test
    void testEncryptWithCustomPassword() throws InvalidEncryptionException {
        String encrypted = cipherEngine.encrypt(TEST_DATA, TEST_PASSWORD);
        assertNotNull(encrypted);
        assertNotEquals(TEST_DATA, encrypted);

        String decrypted = cipherEngine.decrypt(encrypted, TEST_PASSWORD);
        assertEquals(TEST_DATA, decrypted);
    }

    @Test
    void testEncryptNullValue() throws InvalidEncryptionException {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(TEST_PASSWORD);

            String encrypted = cipherEngine.encrypt(null);
            assertNull(encrypted);
        }
    }

    @Test
    void testDecryptNullValue() throws InvalidEncryptionException {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(TEST_PASSWORD);

            String decrypted = cipherEngine.decrypt(null);
            assertNull(decrypted);
        }
    }

    @Test
    void testEncryptEmptyString() throws InvalidEncryptionException {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(TEST_PASSWORD);

            String encrypted = cipherEngine.encrypt(EMPTY_STRING);
            assertNotNull(encrypted);

            String decrypted = cipherEngine.decrypt(encrypted);
            assertEquals(EMPTY_STRING, decrypted);
        }
    }

    @Test
    void testMultipleEncryptionsProduceDifferentCiphertexts() throws InvalidEncryptionException {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(TEST_PASSWORD);

            String encrypted1 = cipherEngine.encrypt(TEST_DATA);
            String encrypted2 = cipherEngine.encrypt(TEST_DATA);

            // Different due to random IV and salt
            assertNotEquals(encrypted1, encrypted2);

            // Both decrypt to same value
            assertEquals(TEST_DATA, cipherEngine.decrypt(encrypted1));
            assertEquals(TEST_DATA, cipherEngine.decrypt(encrypted2));
        }
    }

    @Test
    void testWrongPasswordDecryptionFails() throws InvalidEncryptionException {
        String encrypted = cipherEngine.encrypt(TEST_DATA, "correctPassword123");

        assertThrows(InvalidEncryptionException.class, () ->
            cipherEngine.decrypt(encrypted, "wrongPassword123"));
    }

    @Test
    void testTamperedCiphertextFailsDecryption() throws InvalidEncryptionException {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(TEST_PASSWORD);

            String encrypted = cipherEngine.encrypt(TEST_DATA);

            // Tamper with the encrypted data
            String tamperedEncrypted = encrypted.substring(0, encrypted.length() - 2) + "XX";

            assertThrows(InvalidEncryptionException.class, () ->
                cipherEngine.decrypt(tamperedEncrypted));
        }
    }

    @Test
    void testEncryptLongText() throws InvalidEncryptionException {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(TEST_PASSWORD);

            String longText = "A".repeat(10000);
            String encrypted = cipherEngine.encrypt(longText);
            String decrypted = cipherEngine.decrypt(encrypted);

            assertEquals(longText, decrypted);
        }
    }

    @Test
    void testEncryptSpecialCharacters() throws InvalidEncryptionException {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(TEST_PASSWORD);

            String specialText = "!@#$%^&*()_+-=[]{}|;:',.<>?/~`";
            String encrypted = cipherEngine.encrypt(specialText);
            String decrypted = cipherEngine.decrypt(encrypted);

            assertEquals(specialText, decrypted);
        }
    }

    @Test
    void testEncryptUnicodeCharacters() throws InvalidEncryptionException {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(TEST_PASSWORD);

            String unicodeText = "こんにちは世界 مرحبا بالعالم 你好世界";
            String encrypted = cipherEngine.encrypt(unicodeText);
            String decrypted = cipherEngine.decrypt(encrypted);

            assertEquals(unicodeText, decrypted);
        }
    }

    @Test
    void testRandomNonceGeneration() {
        byte[] nonce1 = GCMCipherEngine.getRandomNonce(12);
        byte[] nonce2 = GCMCipherEngine.getRandomNonce(12);

        assertNotNull(nonce1);
        assertNotNull(nonce2);
        assertEquals(12, nonce1.length);
        assertEquals(12, nonce2.length);
        assertNotEquals(nonce1, nonce2);
    }

    @Test
    void testAESKeyGeneration() throws Exception {
        byte[] salt = GCMCipherEngine.getRandomNonce(16);
        var key = GCMCipherEngine.getAESKey(TEST_PASSWORD, salt);

        assertNotNull(key);
        assertEquals("AES", key.getAlgorithm());
    }

    @Test
    void testConsistentKeyDerivation() throws Exception {
        byte[] salt = GCMCipherEngine.getRandomNonce(16);
        var key1 = GCMCipherEngine.getAESKey(TEST_PASSWORD, salt);
        var key2 = GCMCipherEngine.getAESKey(TEST_PASSWORD, salt);

        // Same salt and password should produce same key
        assertEquals(key1.getAlgorithm(), key2.getAlgorithm());
    }

    @Test
    void testInvalidBase64Decryption() {
        assertThrows(InvalidEncryptionException.class, () ->
            cipherEngine.decrypt("not@valid@base64!!!"));
    }

    @Test
    void testCiphertextIsBase64Encoded() throws InvalidEncryptionException {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(TEST_PASSWORD);

            String encrypted = cipherEngine.encrypt(TEST_DATA);

            // Should contain only valid Base64 characters
            assertTrue(encrypted.matches("[A-Za-z0-9+/]*={0,2}"));
        }
    }
}

