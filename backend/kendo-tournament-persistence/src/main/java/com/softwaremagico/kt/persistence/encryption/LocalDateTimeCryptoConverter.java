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

import com.softwaremagico.kt.logger.EncryptorLogger;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

@Converter
public class LocalDateTimeCryptoConverter extends AbstractCryptoConverter<LocalDateTime> implements AttributeConverter<LocalDateTime, String> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private final DateTimeFormatter formatterOffset = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSx", Locale.getDefault());

    public LocalDateTimeCryptoConverter() {
        this(AbstractCryptoConverter.generateEngine());
    }

    public LocalDateTimeCryptoConverter(ICipherEngine cipherEngine) {
        super(cipherEngine);
    }

    @Override
    protected boolean isNotNullOrEmpty(LocalDateTime attribute) {
        return attribute != null;
    }

    @Override
    protected LocalDateTime stringToEntityAttribute(String dbData) {
        try {
            return (dbData == null || dbData.isEmpty()) ? null : new Timestamp(Long.parseLong(dbData)).toLocalDateTime();
        } catch (NumberFormatException nfe) {
            try {
                //Old versions store it as LocalDateTime
                return LocalDateTime.parse(dbData);
            } catch (DateTimeParseException dtpe) {
                try {
                    // From SQL Script.
                    return LocalDateTime.parse(dbData, formatter);
                } catch (DateTimeParseException dte) {
                    try {
                        //Try with offset.
                        return OffsetDateTime.parse(dbData, formatterOffset).toLocalDateTime();
                    } catch (DateTimeParseException e) {
                        EncryptorLogger.errorMessage(this.getClass().getName(), "Invalid datetime value '{}' in database.", dbData);
                        return null;
                    }
                }
            }
        }
    }

    @Override
    protected String entityAttributeToString(LocalDateTime attribute) {
        return attribute == null ? null : String.valueOf(Timestamp.valueOf(attribute).getTime());
    }
}
