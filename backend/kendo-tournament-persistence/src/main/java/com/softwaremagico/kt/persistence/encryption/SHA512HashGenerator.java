package com.softwaremagico.kt.persistence.encryption;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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

import jakarta.persistence.AttributeConverter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.softwaremagico.kt.persistence.encryption.KeyProperty.getDatabaseEncryptionKey;

/**
 * SHA-512 implementation for encrypting and decrypt.
 */
public class SHA512HashGenerator implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "SHA-512";
    public static final int ALGORITHM_LENGTH = 150;
    private static final int HEX_VALUE = 16;
    private static final int TO_32_BITS = 0xff;
    private static final int NINTH_BIT_TO_1 = 0x100;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return createHash(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        //It is a hash that cannot be reverted.
        return null;
    }

    public static String createHash(String attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance(ALGORITHM);
            if (getDatabaseEncryptionKey() != null) {
                messageDigest.update(getDatabaseEncryptionKey().getBytes(StandardCharsets.UTF_8));
            }
            final byte[] bytes = messageDigest.digest(attribute.getBytes(StandardCharsets.UTF_8));
            final StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & TO_32_BITS) + NINTH_BIT_TO_1, HEX_VALUE).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new InvalidEncryptionException(e);
        }
    }
}
