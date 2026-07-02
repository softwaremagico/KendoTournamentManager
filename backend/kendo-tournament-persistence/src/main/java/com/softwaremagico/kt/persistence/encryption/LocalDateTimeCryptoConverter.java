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
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

@Converter
@SuppressWarnings("java:S2143")
public class LocalDateTimeCryptoConverter extends AbstractCryptoConverter<LocalDateTime>
        implements
            AttributeConverter<LocalDateTime, String> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private final DateTimeFormatter formatterWithMilliseconds = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS", Locale.getDefault());
    private final DateTimeFormatter formatterOffset = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSx",
            Locale.getDefault());

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
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }

        LocalDateTime result = this.parseAsLongTimestamp(dbData);
        if (result != null) {
            return result;
        }

        result = this.parseAsLocalDateTime(dbData);
        if (result != null) {
            return result;
        }

        result = this.parseWithMilliseconds(dbData);
        if (result != null) {
            return result;
        }

        result = this.parseWithoutMilliseconds(dbData);
        if (result != null) {
            return result;
        }

        result = this.parseWithOffset(dbData);
        if (result != null) {
            return result;
        }

        EncryptorLogger.errorMessage(this.getClass(), "Invalid datetime value '{}' in database.", dbData);
        return null;
    }

    private LocalDateTime parseAsLongTimestamp(String dbData) {
        try {
            return new Timestamp(Long.parseLong(dbData)).toLocalDateTime();
        } catch (final NumberFormatException nfe) {
            return null;
        }
    }

    private LocalDateTime parseAsLocalDateTime(String dbData) {
        try {
            return LocalDateTime.parse(dbData);
        } catch (final DateTimeParseException dtpe) {
            return null;
        }
    }

    private LocalDateTime parseWithMilliseconds(String dbData) {
        try {
            return LocalDateTime.parse(dbData, this.formatterWithMilliseconds);
        } catch (final DateTimeParseException dte) {
            return null;
        }
    }

    private LocalDateTime parseWithoutMilliseconds(String dbData) {
        try {
            return LocalDateTime.parse(dbData, this.formatter);
        } catch (final DateTimeParseException dteo) {
            return null;
        }
    }

    private LocalDateTime parseWithOffset(String dbData) {
        try {
            return OffsetDateTime.parse(dbData, this.formatterOffset).toLocalDateTime();
        } catch (final DateTimeParseException e) {
            return null;
        }
    }

    @Override
    protected String entityAttributeToString(LocalDateTime attribute) {
        return attribute == null ? null : String.valueOf(Timestamp.valueOf(attribute).getTime());
    }
}
