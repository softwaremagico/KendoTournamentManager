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
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Test suite for {@link StringCryptoConverter}. Tests JPA attribute converter
 * for encrypted String values.
 */
@ExtendWith(MockitoExtension.class)
class StringCryptoConverterTest {

	@Mock
	private ICipherEngine cipherEngine;

	private StringCryptoConverter converter;
	private static final String TEST_VALUE = "sensitiveString";
	private static final String ENCRYPTED_VALUE = "encryptedValue123456";
	private static final String ENCRYPTION_KEY = "test-key";

	@BeforeEach
	void setUp() {
        this.converter = new StringCryptoConverter(this.cipherEngine);
	}

	@Test
	void testConvertToDatabaseColumnWithEncryption() throws InvalidEncryptionException {
		when(this.cipherEngine.encrypt(TEST_VALUE)).thenReturn(ENCRYPTED_VALUE);

		try (final MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
			keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(ENCRYPTION_KEY);

			final String result = this.converter.convertToDatabaseColumn(TEST_VALUE);

			assertEquals(ENCRYPTED_VALUE, result);
		}
	}

	@Test
	void testConvertToDatabaseColumnWithoutEncryptionKey() {
		try (final MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
			keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(null);

			final String result = this.converter.convertToDatabaseColumn(TEST_VALUE);

			assertEquals(TEST_VALUE, result);
		}
	}

	@Test
	void testConvertToDatabaseColumnWithEmptyEncryptionKey() {
		try (final MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
			keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn("");

			final String result = this.converter.convertToDatabaseColumn(TEST_VALUE);

			assertEquals(TEST_VALUE, result);
		}
	}

	@Test
	void testConvertToDatabaseColumnNull() {
		try (final MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
			keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(ENCRYPTION_KEY);

			final String result = this.converter.convertToDatabaseColumn(null);

			assertNull(result);
		}
	}

	@Test
	void testConvertToDatabaseColumnEmpty() {
		try (final MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
			keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(ENCRYPTION_KEY);

			final String result = this.converter.convertToDatabaseColumn("");

			assertEquals("", result);
		}
	}

	@Test
	void testConvertToEntityAttributeWithDecryption() throws InvalidEncryptionException {
		when(this.cipherEngine.decrypt(ENCRYPTED_VALUE)).thenReturn(TEST_VALUE);

		try (final MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
			keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(ENCRYPTION_KEY);

			final String result = this.converter.convertToEntityAttribute(ENCRYPTED_VALUE);

			assertEquals(TEST_VALUE, result);
		}
	}

	@Test
	void testConvertToEntityAttributeWithoutEncryptionKey() {
		try (final MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
			keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(null);

			final String result = this.converter.convertToEntityAttribute(ENCRYPTED_VALUE);

			assertEquals(ENCRYPTED_VALUE, result);
		}
	}

	@Test
	void testConvertToEntityAttributeWithEmptyEncryptionKey() {
		try (final MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
			keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn("");

			final String result = this.converter.convertToEntityAttribute(ENCRYPTED_VALUE);

			assertEquals(ENCRYPTED_VALUE, result);
		}
	}

	@Test
	void testConvertToEntityAttributeNull() {
		try (final MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
			keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(ENCRYPTION_KEY);

			final String result = this.converter.convertToEntityAttribute(null);

			assertNull(result);
		}
	}

	@Test
	void testConvertToEntityAttributeEmpty() {
		try (final MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
			keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(ENCRYPTION_KEY);

			final String result = this.converter.convertToEntityAttribute("");

			assertEquals("", result);
		}
	}

	@Test
	void testRoundTripEncryptionDecryption() throws InvalidEncryptionException {
		// Create a converter with default cipher engine for round-trip test
		final StringCryptoConverter converter2 = new StringCryptoConverter();

		try (final MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
			keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn("roundtrip-test-key");

			final String original = "This is a test string";
			final String encrypted = converter2.convertToDatabaseColumn(original);
			final String decrypted = converter2.convertToEntityAttribute(encrypted);

			assertEquals(original, decrypted);
			assertNotEquals(original, encrypted);
		}
	}

	@Test
	void testIsNotNullOrEmptyMethod() {
		assertTrue(this.converter.isNotNullOrEmpty("value"));
		assertFalse(this.converter.isNotNullOrEmpty(""));
		assertFalse(this.converter.isNotNullOrEmpty(null));
	}

	@Test
	void testStringToEntityAttributeMethod() {
		final String input = "testString";
		final String result = this.converter.stringToEntityAttribute(input);

		assertEquals(input, result);
	}

	@Test
	void testStringToEntityAttributeNull() {
		final String result = this.converter.stringToEntityAttribute(null);

		assertNull(result);
	}

	@Test
	void testEntityAttributeToStringMethod() {
		final String input = "testString";
		final String result = this.converter.entityAttributeToString(input);

		assertEquals(input, result);
	}

	@Test
	void testEntityAttributeToStringNull() {
		final String result = this.converter.entityAttributeToString(null);

		assertNull(result);
	}

	@Test
	void testConverterWithSpecialCharacters() throws InvalidEncryptionException {
		final String specialValue = "!@#$%^&*()_+-=[]{}|;:',.<>?/~`";
		when(this.cipherEngine.encrypt(specialValue)).thenReturn(ENCRYPTED_VALUE);

		try (final MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
			keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(ENCRYPTION_KEY);

			final String result = this.converter.convertToDatabaseColumn(specialValue);

			assertEquals(ENCRYPTED_VALUE, result);
		}
	}

	@Test
	void testConverterWithUnicodeCharacters() throws InvalidEncryptionException {
		final String unicodeValue = "你好世界 مرحبا بالعالم";
		when(this.cipherEngine.encrypt(unicodeValue)).thenReturn(ENCRYPTED_VALUE);

		try (final MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
			keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(ENCRYPTION_KEY);

			final String result = this.converter.convertToDatabaseColumn(unicodeValue);

			assertEquals(ENCRYPTED_VALUE, result);
		}
	}

	@Test
	void testConverterWithLongString() throws InvalidEncryptionException {
		final String longValue = "A".repeat(10000);
		when(this.cipherEngine.encrypt(longValue)).thenReturn(ENCRYPTED_VALUE);

		try (final MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
			keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(ENCRYPTION_KEY);

			final String result = this.converter.convertToDatabaseColumn(longValue);

			assertEquals(ENCRYPTED_VALUE, result);
		}
	}

	@Test
	void testConstructorWithDefaultEngine() {
		final StringCryptoConverter defaultConverter = new StringCryptoConverter();
		assertNotNull(defaultConverter);
	}

	@Test
	void testConverterEncryptionException() throws InvalidEncryptionException {
		when(this.cipherEngine.encrypt(TEST_VALUE)).thenThrow(new InvalidEncryptionException("Encryption failed"));

		try (final MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
			keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(ENCRYPTION_KEY);

			assertThrows(InvalidEncryptionException.class, () -> this.converter.convertToDatabaseColumn(TEST_VALUE));
		}
	}

	@Test
	void testConverterDecryptionException() throws InvalidEncryptionException {
		when(this.cipherEngine.decrypt(ENCRYPTED_VALUE)).thenThrow(new InvalidEncryptionException("Decryption failed"));

		try (final MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
			keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(ENCRYPTION_KEY);

			assertThrows(InvalidEncryptionException.class, () -> this.converter.convertToEntityAttribute(ENCRYPTED_VALUE));
		}
	}

	@Test
	void testMultipleConversionsWithoutEncryption() {
		try (final MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
			keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(null);

			final String value1 = "value1";
			final String value2 = "value2";
			final String value3 = "value3";

			assertEquals(value1, this.converter.convertToDatabaseColumn(value1));
			assertEquals(value2, this.converter.convertToDatabaseColumn(value2));
			assertEquals(value3, this.converter.convertToDatabaseColumn(value3));
		}
	}
}
