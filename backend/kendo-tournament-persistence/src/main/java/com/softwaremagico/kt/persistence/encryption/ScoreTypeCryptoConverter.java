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

import com.softwaremagico.kt.logger.EncryptorLogger;
import com.softwaremagico.kt.persistence.values.ScoreType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Encrypts the {@link ScoreType} of a tournament score.
 * <p>
 * Hibernate 7 rejects attributes that combine {@code @Enumerated} with an
 * {@code @Convert} converter, so the enum to string conversion is performed
 * by this converter instead of by the persistence provider. The stored
 * representation is unchanged: the encrypted enum name.
 * </p>
 */
@Converter
public class ScoreTypeCryptoConverter extends AbstractCryptoConverter<ScoreType> implements AttributeConverter<ScoreType, String> {

    public ScoreTypeCryptoConverter() {
        this(AbstractCryptoConverter.generateEngine());
    }

    public ScoreTypeCryptoConverter(ICipherEngine cipherEngine) {
        super(cipherEngine);
    }

    @Override
    protected boolean isNotNullOrEmpty(ScoreType attribute) {
        return attribute != null;
    }

    @Override
    protected ScoreType stringToEntityAttribute(String dbData) {
        try {
            return (dbData == null || dbData.isEmpty()) ? null : ScoreType.getType(dbData);
        } catch (NumberFormatException ex) {
            EncryptorLogger.errorMessage(this.getClass(), "Invalid score type value '{}' in database. Cause: {}", dbData,
                    ex.getMessage());
            return null;
        }
    }

    @Override
    protected String entityAttributeToString(ScoreType attribute) {
        return attribute == null ? null : attribute.name();
    }
}
