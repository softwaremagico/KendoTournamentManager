package com.softwaremagico.kt.persistence.encryption;

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

import com.softwaremagico.kt.persistence.values.ScoreType;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = "cryptoConverters")
public class ScoreTypeCryptoConverterTest {

    @Test
    public void shouldConvertScoreTypeToDatabaseColumn() {
        final ScoreTypeCryptoConverter converter = new ScoreTypeCryptoConverter();
        Assert.assertEquals(converter.convertToDatabaseColumn(ScoreType.CLASSIC), "CLASSIC");
        Assert.assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    public void shouldConvertDatabaseColumnToScoreType() {
        final ScoreTypeCryptoConverter converter = new ScoreTypeCryptoConverter();
        Assert.assertEquals(converter.convertToEntityAttribute("CLASSIC"), ScoreType.CLASSIC);
        Assert.assertEquals(converter.convertToEntityAttribute("european"), ScoreType.EUROPEAN);
        Assert.assertNull(converter.convertToEntityAttribute(null));
        Assert.assertNull(converter.convertToEntityAttribute(""));
        Assert.assertNull(converter.convertToEntityAttribute("UNKNOWN"));
    }
}
