package com.softwaremagico.kt.persistence.encryption;

import com.softwaremagico.kt.logger.EncryptorLogger;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Converter
public class ByteArrayCryptoConverter extends AbstractCryptoConverter<byte[]>
        implements AttributeConverter<byte[], String> {

    public ByteArrayCryptoConverter() {
        this(new CipherInitializer());
    }

    public ByteArrayCryptoConverter(CipherInitializer cipherInitializer) {
        super(cipherInitializer);
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
