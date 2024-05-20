package com.softwaremagico.kt.persistence.encryption;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
 * %%
 * Copyright (C) 2021 - 2024 Softwaremagico
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

import com.softwaremagico.kt.logger.EncryptorLogger;
import jakarta.persistence.AttributeConverter;

import static com.softwaremagico.kt.persistence.encryption.KeyProperty.getDatabaseEncryptionKey;


public abstract class AbstractCryptoConverter<T> implements AttributeConverter<T, String> {

    private final ICipherEngine cipherEngine;

    public AbstractCryptoConverter() {
        this(AbstractCryptoConverter.generateEngine());
    }

    public AbstractCryptoConverter(ICipherEngine cipherEngine) {
        this.cipherEngine = cipherEngine;
    }

    public static ICipherEngine generateEngine() {
        return new CBCCipherEngine();
    }

    @Override
    public String convertToDatabaseColumn(T attribute) {
        if (getDatabaseEncryptionKey() != null && !getDatabaseEncryptionKey().isEmpty() && isNotNullOrEmpty(attribute)) {
            try {
                return encrypt(attribute);
            } catch (InvalidEncryptionException e) {
                throw new RuntimeException(e);
            }
        }
        return entityAttributeToString(attribute);
    }

    @Override
    public T convertToEntityAttribute(String dbData) {
        if (getDatabaseEncryptionKey() != null && !getDatabaseEncryptionKey().isEmpty() && dbData != null && !dbData.isEmpty()) {
            try {
                return decrypt(dbData);
            } catch (InvalidEncryptionException e) {
                throw new RuntimeException(e);
            }
        }
        return stringToEntityAttribute(dbData);
    }

    protected abstract boolean isNotNullOrEmpty(T attribute);

    protected abstract T stringToEntityAttribute(String dbData);

    protected abstract String entityAttributeToString(T attribute);

    private String encrypt(T attribute) throws InvalidEncryptionException {
        return cipherEngine.encrypt(entityAttributeToString(attribute));
    }

    private T decrypt(String dbData) throws InvalidEncryptionException {
        final T entity = stringToEntityAttribute(cipherEngine.decrypt(dbData));
        EncryptorLogger.debug(this.getClass().getName(), "Decrypted value for '{}' is '{}'.", dbData, entity);
        return entity;
    }

}
