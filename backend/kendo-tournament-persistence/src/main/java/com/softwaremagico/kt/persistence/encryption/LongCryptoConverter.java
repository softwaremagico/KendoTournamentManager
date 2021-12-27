package com.softwaremagico.kt.persistence.encryption;

import com.softwaremagico.kt.logger.EncryptorLogger;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class LongCryptoConverter extends AbstractCryptoConverter<Long> implements AttributeConverter<Long, String> {

	public LongCryptoConverter() {
		this(new CipherInitializer());
	}

	public LongCryptoConverter(CipherInitializer cipherInitializer) {
		super(cipherInitializer);
	}

	@Override
	protected boolean isNotNullOrEmpty(Long attribute) {
		return attribute != null;
	}

	@Override
	protected Long stringToEntityAttribute(String dbData) {
		try {
			return (dbData == null || dbData.isEmpty()) ? null : Long.parseLong(dbData);
		} catch (NumberFormatException nfe) {
			EncryptorLogger.errorMessage(this.getClass().getName(), "Invalid long value in database.");
			return null;
		}
	}

	@Override
	protected String entityAttributeToString(Long attribute) {
		return attribute == null ? null : attribute.toString();
	}
}
