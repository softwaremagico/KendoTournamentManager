package com.softwaremagico.kt.persistence.encryption;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
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

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Timestamp;
import java.time.LocalDate;

@Test(groups = "cryptoConverters")
public class CryptoConvertersAdditionalTests {

    private static final String ENCRYPTION_CODE = "myEncryptionCode";

    private IntegerCryptoConverter integerConverter;
    private DoubleCryptoConverter doubleConverter;
    private LocalDateCryptoConverter localDateConverter;
    private TimestampCryptoConverter timestampConverter;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        new KeyProperty(ENCRYPTION_CODE, null, null);
        integerConverter = new IntegerCryptoConverter();
        doubleConverter = new DoubleCryptoConverter();
        localDateConverter = new LocalDateCryptoConverter();
        timestampConverter = new TimestampCryptoConverter();
    }

    @Test
    public void shouldRoundTripIntegerValues() {
        final Integer value = -321;
        final String encrypted = integerConverter.convertToDatabaseColumn(value);
        final Integer decrypted = integerConverter.convertToEntityAttribute(encrypted);

        Assert.assertNotNull(encrypted);
        Assert.assertEquals(decrypted, value);
    }

    @Test
    public void shouldReturnNullForInvalidInteger() {
        final Integer value = integerConverter.convertToEntityAttribute("not-an-int");

        Assert.assertNull(value);
    }

    @Test
    public void shouldRoundTripDoubleValues() {
        final Double value = 12345.6789;
        final String encrypted = doubleConverter.convertToDatabaseColumn(value);
        final Double decrypted = doubleConverter.convertToEntityAttribute(encrypted);

        Assert.assertNotNull(encrypted);
        Assert.assertEquals(decrypted, value, 0.0000001);
    }

    @Test
    public void shouldReturnNullForInvalidDouble() {
        final Double value = doubleConverter.convertToEntityAttribute("not-a-double");

        Assert.assertNull(value);
    }

    @Test
    public void shouldRoundTripLocalDateValues() {
        final LocalDate value = LocalDate.of(2026, 6, 22);
        final String encrypted = localDateConverter.convertToDatabaseColumn(value);
        final LocalDate decrypted = localDateConverter.convertToEntityAttribute(encrypted);

        Assert.assertNotNull(encrypted);
        Assert.assertEquals(decrypted, value);
    }

    @Test
    public void shouldReturnNullForInvalidLocalDate() {
        final LocalDate value = localDateConverter.convertToEntityAttribute("2026/06/22");

        Assert.assertNull(value);
    }

    @Test
    public void shouldRoundTripTimestampValues() {
        final Timestamp value = new Timestamp(1_719_052_800_000L);
        final String encrypted = timestampConverter.convertToDatabaseColumn(value);
        final Timestamp decrypted = timestampConverter.convertToEntityAttribute(encrypted);

        Assert.assertNotNull(encrypted);
        Assert.assertEquals(decrypted, value);
    }

    @Test
    public void shouldReturnNullForInvalidTimestamp() {
        final Timestamp value = timestampConverter.convertToEntityAttribute("not-a-timestamp");

        Assert.assertNull(value);
    }

    @Test
    public void shouldKeepNullValuesAsNull() {
        Assert.assertNull(integerConverter.convertToDatabaseColumn(null));
        Assert.assertNull(doubleConverter.convertToDatabaseColumn(null));
        Assert.assertNull(localDateConverter.convertToDatabaseColumn(null));
        Assert.assertNull(timestampConverter.convertToDatabaseColumn(null));
    }
}

