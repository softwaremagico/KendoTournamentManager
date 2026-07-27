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
 * Test suite for {@link DoubleCryptoConverter}.
 * Tests JPA attribute converter for encrypted Double values.
 */
@Test(groups = "cryptoConverters")
public class DoubleCryptoConverterTest {

    @Mock
    private ICipherEngine cipherEngine;

    @InjectMocks
    private DoubleCryptoConverter converter;
    private static final String ENCRYPTION_KEY = "test-key";

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testConvertToDatabaseColumnWithoutEncryption() {

        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(null);

            String result = converter.convertToDatabaseColumn(42.5);

            assertEquals("42.5", result);
        }
    }

    @Test
    public void testConvertToEntityAttributeWithoutEncryption() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(null);

            Double result = converter.convertToEntityAttribute("42.5");

            assertEquals(42.5, result);
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

            Double result = converter.convertToEntityAttribute(null);

            assertNull(result);
        }
    }

    @Test
    public void testConvertZero() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(null);

            String dbColumn = converter.convertToDatabaseColumn(0.0);
            Double entity = converter.convertToEntityAttribute(dbColumn);

            assertEquals(0.0, entity);
        }
    }

    @Test
    public void testConvertNegativeNumber() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(null);

            String dbColumn = converter.convertToDatabaseColumn(-42.75);
            Double entity = converter.convertToEntityAttribute(dbColumn);

            assertEquals(-42.75, entity);
        }
    }

    @Test
    public void testConvertSmallDecimal() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(null);

            String dbColumn = converter.convertToDatabaseColumn(0.00001);
            Double entity = converter.convertToEntityAttribute(dbColumn);

            assertEquals(0.00001, entity, 0.000001);
        }
    }

    @Test
    public void testConvertLargeNumber() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(null);

            String dbColumn = converter.convertToDatabaseColumn(1234567890.123456);
            Double entity = converter.convertToEntityAttribute(dbColumn);

            assertEquals(1234567890.123456, entity, 0.000001);
        }
    }

    @Test
    public void testConvertMaxDouble() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(null);

            String dbColumn = converter.convertToDatabaseColumn(Double.MAX_VALUE);
            Double entity = converter.convertToEntityAttribute(dbColumn);

            assertEquals(Double.MAX_VALUE, entity);
        }
    }

    @Test
    public void testConvertMinDouble() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(null);

            String dbColumn = converter.convertToDatabaseColumn(Double.MIN_VALUE);
            Double entity = converter.convertToEntityAttribute(dbColumn);

            assertEquals(Double.MIN_VALUE, entity);
        }
    }

    @Test
    public void testConvertWithEncryption() throws InvalidEncryptionException {
        when(cipherEngine.encrypt("42.5")).thenReturn("encryptedValue");

        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(ENCRYPTION_KEY);

            String result = converter.convertToDatabaseColumn(42.5);

            assertEquals("encryptedValue", result);
        }
    }

    @Test
    public void testConvertFromEncryptedValue() throws InvalidEncryptionException {
        when(cipherEngine.decrypt("encryptedValue")).thenReturn("42.5");

        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(ENCRYPTION_KEY);

            Double result = converter.convertToEntityAttribute("encryptedValue");

            assertEquals(42.5, result);
        }
    }

    @Test
    public void testConstructorWithDefaultEngine() {
        DoubleCryptoConverter defaultConverter = new DoubleCryptoConverter();
        assertNotNull(defaultConverter);
    }

    @Test
    public void testIsNotNullOrEmptyMethod() {
        assertTrue(converter.isNotNullOrEmpty(1.0));
        assertTrue(converter.isNotNullOrEmpty(0.0));
        assertTrue(converter.isNotNullOrEmpty(-1.5));
        assertFalse(converter.isNotNullOrEmpty(null));
    }

    @Test
    public void testPrecisionPreservation() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(null);

            double originalValue = 3.141592653589793;
            String dbColumn = converter.convertToDatabaseColumn(originalValue);
            Double entity = converter.convertToEntityAttribute(dbColumn);

            assertEquals(originalValue, entity, 0.000000000000001);
        }
    }

    @Test
    public void testMultipleConversions() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(null);

            double[] testValues = {0.1, 0.5, 1.0, -1.5, 10.99, 100.001};
            for (double value : testValues) {
                String dbColumn = converter.convertToDatabaseColumn(value);
                Double entity = converter.convertToEntityAttribute(dbColumn);
                assertEquals(value, entity, 0.00001);
            }
        }
    }

    @Test
    public void testSpecialDoubleValues() {
        try (MockedStatic<KeyProperty> keyPropertyMocked = mockStatic(KeyProperty.class)) {
            keyPropertyMocked.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(null);

            // Test positive and negative infinity
            // Note: These might have special handling in the converter
            String posInfinity = converter.convertToDatabaseColumn(Double.POSITIVE_INFINITY);
            String negInfinity = converter.convertToDatabaseColumn(Double.NEGATIVE_INFINITY);

            assertNotNull(posInfinity);
            assertNotNull(negInfinity);
        }
    }
}

