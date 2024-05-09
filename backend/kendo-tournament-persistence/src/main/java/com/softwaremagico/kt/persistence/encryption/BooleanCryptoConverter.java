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

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class BooleanCryptoConverter extends AbstractCryptoConverter<Boolean> implements AttributeConverter<Boolean, String> {

    @Override
    protected boolean isNotNullOrEmpty(Boolean attribute) {
        return attribute != null;
    }

    @Override
    protected Boolean stringToEntityAttribute(String dbData) {
        return (dbData == null || dbData.isEmpty()) ? null : Boolean.parseBoolean(dbData);
    }

    @Override
    protected String entityAttributeToString(Boolean attribute) {
        return attribute == null ? null : attribute.toString();
    }
}
