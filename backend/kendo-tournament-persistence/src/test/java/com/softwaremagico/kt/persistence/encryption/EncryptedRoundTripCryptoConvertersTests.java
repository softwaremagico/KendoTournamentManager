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

import com.softwaremagico.kt.persistence.values.AchievementGrade;
import com.softwaremagico.kt.persistence.values.AchievementType;
import com.softwaremagico.kt.persistence.values.ImageCompression;
import com.softwaremagico.kt.persistence.values.ImageFormat;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import com.softwaremagico.kt.persistence.values.TournamentImageType;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;

/**
 * Exercises the real encryption/decryption path (with a non-empty key configured)
 * for the enum, {@link LongCryptoConverter} and {@link ByteArrayCryptoConverter}
 * converters, which are otherwise only tested through the "plain" (no-key) path.
 */
@Test(groups = "cryptoConverters")
public class EncryptedRoundTripCryptoConvertersTests {

    private static final String ENCRYPTION_CODE = "encryptedRoundTripKey";

    @BeforeClass(alwaysRun = true)
    public void setup() {
        KeyProperty.configure(ENCRYPTION_CODE, null, null);
    }

    @Test
    public void shouldRoundTripTournamentTypeWithEncryption() {
        assertEncryptedRoundTrip(new TournamentTypeCryptoConverter(), TournamentType.LEAGUE);
    }

    @Test
    public void shouldRoundTripRoleTypeWithEncryption() {
        assertEncryptedRoundTrip(new RoleTypeCryptoConverter(), RoleType.COMPETITOR);
    }

    @Test
    public void shouldRoundTripAchievementTypeWithEncryption() {
        assertEncryptedRoundTrip(new AchievementTypeCryptoConverter(), AchievementType.THE_WINNER);
    }

    @Test
    public void shouldRoundTripAchievementGradeWithEncryption() {
        assertEncryptedRoundTrip(new AchievementGradeCryptoConverter(), AchievementGrade.SILVER);
    }

    @Test
    public void shouldRoundTripTournamentImageTypeWithEncryption() {
        assertEncryptedRoundTrip(new TournamentImageTypeCryptoConverter(), TournamentImageType.BANNER);
    }

    @Test
    public void shouldRoundTripImageCompressionWithEncryption() {
        assertEncryptedRoundTrip(new ImageCompressionCryptoConverter(), ImageCompression.JPG);
    }

    @Test
    public void shouldRoundTripImageFormatWithEncryption() {
        assertEncryptedRoundTrip(new ImageFormatCryptoConverter(), ImageFormat.SVG);
    }

    @Test
    public void shouldRoundTripTournamentExtraPropertyKeyWithEncryption() {
        assertEncryptedRoundTrip(new TournamentExtraPropertyKeyTypeCryptoConverter(),
                TournamentExtraPropertyKey.NUMBER_OF_WINNERS);
    }

    @Test
    public void shouldRoundTripLongValuesWithEncryption() {
        final LongCryptoConverter converter = new LongCryptoConverter();
        final Long value = 987654321L;

        final String encrypted = converter.convertToDatabaseColumn(value);
        final Long decrypted = converter.convertToEntityAttribute(encrypted);

        Assert.assertNotNull(encrypted);
        Assert.assertNotEquals(encrypted, value.toString());
        Assert.assertEquals(decrypted, value);
    }

    @Test
    public void shouldReturnNullForInvalidEncryptedLong() {
        final LongCryptoConverter converter = new LongCryptoConverter();

        Assert.assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    public void shouldReturnNullForNonNumericDecryptedLong() {
        final LongCryptoConverter converter = new LongCryptoConverter();
        // Encrypt a non-numeric string so that decryption succeeds but parsing fails.
        final String encryptedGarbage = new StringCryptoConverter().convertToDatabaseColumn("not-a-long");

        Assert.assertNull(converter.convertToEntityAttribute(encryptedGarbage));
    }

    @Test
    public void shouldRoundTripByteArrayValuesWithEncryption() {
        final ByteArrayCryptoConverter converter = new ByteArrayCryptoConverter();
        final byte[] value = "sensitive-bytes".getBytes(StandardCharsets.UTF_8);

        final String encrypted = converter.convertToDatabaseColumn(value);
        final byte[] decrypted = converter.convertToEntityAttribute(encrypted);

        Assert.assertNotNull(encrypted);
        Assert.assertEquals(decrypted, value);
    }

    @Test
    public void shouldNotEncryptEmptyOrNullByteArray() {
        final ByteArrayCryptoConverter converter = new ByteArrayCryptoConverter();

        // An empty array is not encrypted (isNotNullOrEmpty == false), only base64-encoded as-is.
        Assert.assertEquals(converter.convertToDatabaseColumn(new byte[0]), "");
        Assert.assertNull(converter.convertToDatabaseColumn(null));
    }

    private <E> void assertEncryptedRoundTrip(AbstractCryptoConverter<E> converter, E value) {
        final String encrypted = converter.convertToDatabaseColumn(value);
        final E decrypted = converter.convertToEntityAttribute(encrypted);

        Assert.assertNotNull(encrypted);
        Assert.assertNotEquals(encrypted, value.toString());
        Assert.assertEquals(decrypted, value);

        // Invalid/decrypted-garbage input must not blow up, only return null.
        final String garbage = new StringCryptoConverter().convertToDatabaseColumn("__not_a_valid_enum_value__");
        Assert.assertNull(converter.convertToEntityAttribute(garbage));
    }
}


