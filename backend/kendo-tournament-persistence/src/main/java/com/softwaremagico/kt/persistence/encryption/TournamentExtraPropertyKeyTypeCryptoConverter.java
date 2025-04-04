package com.softwaremagico.kt.persistence.encryption;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
 * %%
 * Copyright (C) 2021 - 2025 Softwaremagico
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
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TournamentExtraPropertyKeyTypeCryptoConverter extends AbstractCryptoConverter<TournamentExtraPropertyKey>
        implements AttributeConverter<TournamentExtraPropertyKey, String> {

    public TournamentExtraPropertyKeyTypeCryptoConverter() {
        this(AbstractCryptoConverter.generateEngine());
    }

    public TournamentExtraPropertyKeyTypeCryptoConverter(ICipherEngine cipherEngine) {
        super(cipherEngine);
    }

    @Override
    protected boolean isNotNullOrEmpty(TournamentExtraPropertyKey attribute) {
        return attribute != null;
    }

    @Override
    protected TournamentExtraPropertyKey stringToEntityAttribute(String dbData) {
        try {
            return (dbData == null || dbData.isEmpty()) ? null : TournamentExtraPropertyKey.getType(dbData);
        } catch (NumberFormatException nfe) {
            EncryptorLogger.errorMessage(this.getClass().getName(), "Invalid role value '{}' in database.", dbData);
            return null;
        }
    }

    @Override
    protected String entityAttributeToString(TournamentExtraPropertyKey attribute) {
        return attribute == null ? null : attribute.name();
    }
}
