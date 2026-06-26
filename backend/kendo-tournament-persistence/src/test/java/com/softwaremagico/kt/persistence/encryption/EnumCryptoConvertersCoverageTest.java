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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = "cryptoConverters")
public class EnumCryptoConvertersCoverageTest {

    @BeforeMethod(alwaysRun = true)
    public void resetEncryptionKey() {
        // Plain conversion path is enough to execute enum converter branches.
        new KeyProperty(null, null, null);
    }

    @Test
    public void shouldCoverTournamentTypeConverter() {
        final TournamentTypeCryptoConverter converter = new TournamentTypeCryptoConverter();
        assertRoundTrip(converter, TournamentType.LEAGUE);
    }

    @Test
    public void shouldCoverRoleTypeConverter() {
        final RoleTypeCryptoConverter converter = new RoleTypeCryptoConverter();
        assertRoundTrip(converter, RoleType.COMPETITOR);
    }

    @Test
    public void shouldCoverAchievementTypeConverter() {
        final AchievementTypeCryptoConverter converter = new AchievementTypeCryptoConverter();
        assertRoundTrip(converter, AchievementType.THE_WINNER);
    }

    @Test
    public void shouldCoverAchievementGradeConverter() {
        final AchievementGradeCryptoConverter converter = new AchievementGradeCryptoConverter();
        assertRoundTrip(converter, AchievementGrade.SILVER);
    }

    @Test
    public void shouldCoverTournamentImageTypeConverter() {
        final TournamentImageTypeCryptoConverter converter = new TournamentImageTypeCryptoConverter();
        assertRoundTrip(converter, TournamentImageType.BANNER);
    }

    @Test
    public void shouldCoverImageCompressionConverter() {
        final ImageCompressionCryptoConverter converter = new ImageCompressionCryptoConverter();
        assertRoundTrip(converter, ImageCompression.JPG);
    }

    @Test
    public void shouldCoverImageFormatConverter() {
        final ImageFormatCryptoConverter converter = new ImageFormatCryptoConverter();
        assertRoundTrip(converter, ImageFormat.SVG);
    }

    @Test
    public void shouldCoverTournamentExtraPropertyKeyConverter() {
        final TournamentExtraPropertyKeyTypeCryptoConverter converter = new TournamentExtraPropertyKeyTypeCryptoConverter();
        assertRoundTrip(converter, TournamentExtraPropertyKey.NUMBER_OF_WINNERS);
    }

    private <E> void assertRoundTrip(AbstractCryptoConverter<E> converter, E value) {
        final String rawValue = converter.convertToDatabaseColumn(value);

        Assert.assertEquals(rawValue, value.toString());
        Assert.assertEquals(converter.convertToEntityAttribute(value.toString().toLowerCase()), value);

        Assert.assertNull(converter.convertToEntityAttribute("__invalid__"));
        Assert.assertNull(converter.convertToEntityAttribute(""));
        Assert.assertNull(converter.convertToEntityAttribute(null));
        Assert.assertNull(converter.convertToDatabaseColumn(null));
    }
}


