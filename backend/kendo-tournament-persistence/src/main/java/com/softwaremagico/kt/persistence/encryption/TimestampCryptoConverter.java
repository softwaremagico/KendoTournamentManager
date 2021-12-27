package com.softwaremagico.kt.persistence.encryption;


import com.softwaremagico.kt.logger.EncryptorLogger;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Timestamp;

@Converter
public class TimestampCryptoConverter extends AbstractCryptoConverter<Timestamp> implements AttributeConverter<Timestamp, String> {

    public TimestampCryptoConverter() {
        this(new CipherInitializer());
    }

    public TimestampCryptoConverter(CipherInitializer cipherInitializer) {
        super(cipherInitializer);
    }

    @Override
    protected boolean isNotNullOrEmpty(Timestamp attribute) {
        return attribute != null;
    }

    @Override
    protected Timestamp stringToEntityAttribute(String dbData) {
        try {
            return (dbData == null || dbData.isEmpty()) ? null : new Timestamp(Long.parseLong(dbData));
        } catch (NumberFormatException nfe) {
            EncryptorLogger.errorMessage(this.getClass().getName(), "Invalid long value in database.");
            return null;
        }
    }

    @Override
    protected String entityAttributeToString(Timestamp attribute) {
        return attribute == null ? null : attribute.getTime() + "";
    }
}
