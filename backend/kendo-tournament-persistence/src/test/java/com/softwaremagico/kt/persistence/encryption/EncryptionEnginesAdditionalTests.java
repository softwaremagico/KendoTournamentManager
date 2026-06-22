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

@Test(groups = "encryptionEngines")
public class EncryptionEnginesAdditionalTests {

    private static final String ENCRYPTION_CODE = "myEncryptionCode";

    private GCMCipherEngine gcmCipherEngine;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        new KeyProperty(ENCRYPTION_CODE, null, null);
        gcmCipherEngine = new GCMCipherEngine();
    }

    @Test
    public void shouldEncryptAndDecryptWithGcmUsingDefaultKey() {
        final String rawText = "sensitive-value-123";

        final String encrypted = gcmCipherEngine.encrypt(rawText);
        final String decrypted = gcmCipherEngine.decrypt(encrypted);

        Assert.assertNotNull(encrypted);
        Assert.assertNotEquals(encrypted, rawText);
        Assert.assertEquals(decrypted, rawText);
    }

    @Test
    public void shouldEncryptAndDecryptWithGcmUsingCustomPassword() {
        final String rawText = "custom-password-data";
        final String password = "P@ssw0rd";

        final String encrypted = gcmCipherEngine.encrypt(rawText, password);
        final String decrypted = gcmCipherEngine.decrypt(encrypted, password);

        Assert.assertNotNull(encrypted);
        Assert.assertEquals(decrypted, rawText);
    }

    @Test(expectedExceptions = InvalidEncryptionException.class)
    public void shouldFailDecryptWithWrongPassword() {
        final String encrypted = gcmCipherEngine.encrypt("x", "right-password");

        gcmCipherEngine.decrypt(encrypted, "wrong-password");
    }

    @Test
    public void shouldHashWithSha512AndReturnDeterministicResult() {
        final String first = SHA512HashGenerator.createHash("my-username");
        final String second = SHA512HashGenerator.createHash("my-username");

        Assert.assertNotNull(first);
        Assert.assertEquals(first, second);
        Assert.assertNotEquals(first, SHA512HashGenerator.createHash("other-username"));
    }

    @Test
    public void shouldReturnNullHashForNullInput() {
        Assert.assertNull(SHA512HashGenerator.createHash(null));
    }

    @Test
    public void shouldConvertToDatabaseColumnWithSha512Converter() {
        final SHA512HashGenerator generator = new SHA512HashGenerator();
        final String hashed = generator.convertToDatabaseColumn("value");

        Assert.assertNotNull(hashed);
        Assert.assertTrue(hashed.length() > 100);
        Assert.assertNull(generator.convertToEntityAttribute(hashed));
    }
}

