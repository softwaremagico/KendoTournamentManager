package com.softwaremagico.kt.persistence.encryption;

import com.softwaremagico.kt.logger.EncryptorLogger;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class FloatCryptoConverter extends AbstractCryptoConverter<Float> implements AttributeConverter<Float, String> {

    public FloatCryptoConverter() {
        this(new CipherInitializer());
    }

    public FloatCryptoConverter(CipherInitializer cipherInitializer) {
        super(cipherInitializer);
    }

    @Override
    protected boolean isNotNullOrEmpty(Float attribute) {
        return attribute != null;
    }

    @Override
    protected Float stringToEntityAttribute(String dbData) {
        try {
            return (dbData == null || dbData.isEmpty()) ? null : Float.parseFloat(dbData);
        } catch (NumberFormatException nfe) {
            EncryptorLogger.errorMessage(this.getClass().getName(), "Invalid long value in database.");
            return null;
        }
    }

    @Override
    protected String entityAttributeToString(Float attribute) {
        return attribute == null ? null : attribute.toString();
    }
}
