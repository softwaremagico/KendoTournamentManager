package com.softwaremagico.kt.persistence.encryption;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
 * %%
 * This software is designed by Jorge Hortelano Otero. Jorge Hortelano Otero
 * <softwaremagico@gmail.com> Valencia (Spain).
 *  
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *  
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.softwaremagico.kt.logger.EncryptorLogger;

import javax.crypto.NoSuchPaddingException;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Converter(autoApply = true)
public class ByteArrayCryptoConverter extends AbstractCryptoConverter<byte[]>
        implements AttributeConverter<byte[], String> {

    public ByteArrayCryptoConverter() throws NoSuchAlgorithmException, NoSuchPaddingException {
        this(new CBCCipherEngine());
    }

    public ByteArrayCryptoConverter(ICipherEngine CBCCipherEngine) {
        super(CBCCipherEngine);
    }

    @Override
    protected boolean isNotNullOrEmpty(byte[] attribute) {
        return attribute != null && attribute.length != 0;
    }

    @Override
    protected byte[] stringToEntityAttribute(String dbData) {
        try {
            return (dbData == null || dbData.isEmpty()) ? null : Base64.getDecoder().decode(dbData.getBytes(StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            EncryptorLogger.errorMessage(this.getClass().getName(), e);
            return null;
        }
    }

    @Override
    protected String entityAttributeToString(byte[] attribute) {
        if (attribute != null) {
            return Base64.getEncoder().encodeToString(attribute);
        }
        return null;
    }
}
