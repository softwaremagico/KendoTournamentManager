package com.softwaremagico.kt.persistence.encryption;

import com.softwaremagico.kt.logger.EncryptorLogger;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Converter
public class LocalDateCryptoConverter extends AbstractCryptoConverter<LocalDate> implements AttributeConverter<LocalDate, String> {

    public LocalDateCryptoConverter() {
        this(new CipherInitializer());
    }

    public LocalDateCryptoConverter(CipherInitializer cipherInitializer) {
        super(cipherInitializer);
    }

    @Override
    protected boolean isNotNullOrEmpty(LocalDate attribute) {
        return attribute != null;
    }

    @Override
    protected LocalDate stringToEntityAttribute(String dbData) {
        try {
            return (dbData == null || dbData.isEmpty()) ? null : LocalDate.parse(dbData);
        } catch (DateTimeParseException nfe) {
            EncryptorLogger.errorMessage(this.getClass().getName(), "Invalid long value in database.");
            return null;
        }
    }

    @Override
    protected String entityAttributeToString(LocalDate attribute) {
        return attribute == null ? null : attribute.toString() + "";
    }
}
