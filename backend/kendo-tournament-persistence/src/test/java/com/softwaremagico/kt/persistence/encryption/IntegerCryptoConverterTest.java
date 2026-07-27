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

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

/**
 * Test suite for {@link IntegerCryptoConverter}.
 * Tests JPA attribute converter for encrypted Integer values.
 */
@Test(groups = "cryptoConverters")
public class IntegerCryptoConverterTest {

    @Mock
    private ICipherEngine cipherEngine;

    @InjectMocks
    private IntegerCryptoConverter converter;
    private static final String ENCRYPTION_KEY = "test-key";

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testConvertToDatabaseColumnWithoutEncryption() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(null);

            String result = converter.convertToDatabaseColumn(42);

            assertEquals("42", result);
        }
    }

    @Test
    public void testConvertToEntityAttributeWithoutEncryption() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(null);

            Integer result = converter.convertToEntityAttribute("42");

            assertEquals(42, result);
        }
    }

    @Test
    public void testConvertToDatabaseColumnNull() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(ENCRYPTION_KEY);

            String result = converter.convertToDatabaseColumn(null);

            assertNull(result);
        }
    }

    @Test
    public void testConvertToEntityAttributeNull() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(ENCRYPTION_KEY);

            Integer result = converter.convertToEntityAttribute(null);

            assertNull(result);
        }
    }

    @Test
    public void testConvertToDatabaseColumnWithEncryption() throws InvalidEncryptionException {
        when(cipherEngine.encrypt("42")).thenReturn("encryptedValue");

        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(ENCRYPTION_KEY);

            String result = converter.convertToDatabaseColumn(42);

            assertEquals("encryptedValue", result);
        }
    }

    @Test
    public void testConvertToEntityAttributeWithEncryption() throws InvalidEncryptionException {
        when(cipherEngine.decrypt("encryptedValue")).thenReturn("42");

        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(ENCRYPTION_KEY);

            Integer result = converter.convertToEntityAttribute("encryptedValue");

            assertEquals(42, result);
        }
    }

    @Test
    public void testConvertZero() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(null);

            String dbColumn = converter.convertToDatabaseColumn(0);
            Integer entity = converter.convertToEntityAttribute(dbColumn);

            assertEquals(0, entity);
        }
    }

    @Test
    public void testConvertNegativeNumber() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(null);

            String dbColumn = converter.convertToDatabaseColumn(-42);
            Integer entity = converter.convertToEntityAttribute(dbColumn);

            assertEquals(-42, entity);
        }
    }

    @Test
    public void testConvertMaxInteger() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(null);

            String dbColumn = converter.convertToDatabaseColumn(Integer.MAX_VALUE);
            Integer entity = converter.convertToEntityAttribute(dbColumn);

            assertEquals(Integer.MAX_VALUE, entity);
        }
    }

    @Test
    public void testConvertMinInteger() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(null);

            String dbColumn = converter.convertToDatabaseColumn(Integer.MIN_VALUE);
            Integer entity = converter.convertToEntityAttribute(dbColumn);

            assertEquals(Integer.MIN_VALUE, entity);
        }
    }

    @Test
    public void testConstructorWithDefaultEngine() {
        IntegerCryptoConverter defaultConverter = new IntegerCryptoConverter();
        assertNotNull(defaultConverter);
    }

    @Test
    public void testIsNotNullOrEmptyMethod() {
        assertTrue(converter.isNotNullOrEmpty(1));
        assertTrue(converter.isNotNullOrEmpty(0));
        assertTrue(converter.isNotNullOrEmpty(-1));
        assertFalse(converter.isNotNullOrEmpty(null));
    }

    @Test
    public void testMultipleConversions() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(null);

            for (int i = -100; i < 100; i++) {
                String dbColumn = converter.convertToDatabaseColumn(i);
                Integer entity = converter.convertToEntityAttribute(dbColumn);
                assertEquals(i, entity);
            }
        }
    }
}

