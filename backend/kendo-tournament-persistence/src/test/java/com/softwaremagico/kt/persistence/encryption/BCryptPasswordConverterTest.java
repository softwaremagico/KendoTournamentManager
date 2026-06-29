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

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.testng.annotations.Test;

import java.lang.reflect.Field;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

@Test(groups = "cryptoConverters")
class BCryptPasswordConverterTest {

    @Test
    void shouldKeepNullAndEmptyValuesUntouched() {
        final BCryptPasswordConverter converter = new BCryptPasswordConverter();

        assertNull(converter.convertToDatabaseColumn(null));
        assertEquals("", converter.convertToDatabaseColumn(""));
    }

    @Test
    void shouldNotReEncodeExistingBcryptHashes() {
        final BCryptPasswordConverter converter = new BCryptPasswordConverter();
        final String bcryptHash = new BCryptPasswordEncoder().encode("secret-password");

        assertEquals(bcryptHash, converter.convertToDatabaseColumn(bcryptHash));
        assertEquals(bcryptHash, converter.convertToEntityAttribute(bcryptHash));
    }

    @Test
    void shouldEncodePlainTextPasswordEvenWhenEncoderIsReset() throws Exception {
        final BCryptPasswordConverter converter = new BCryptPasswordConverter();
        final Field encoderField = BCryptPasswordConverter.class.getDeclaredField("encoder");
        encoderField.setAccessible(true);
        encoderField.set(converter, null);

        final String encoded = converter.convertToDatabaseColumn("secret-password");

        assertNotEquals("secret-password", encoded);
        assertTrue(new BCryptPasswordEncoder().matches("secret-password", encoded));
    }
}


