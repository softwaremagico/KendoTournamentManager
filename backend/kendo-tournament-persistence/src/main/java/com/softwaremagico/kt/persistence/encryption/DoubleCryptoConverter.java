package com.softwaremagico.kt.persistence.encryption;

import com.softwaremagico.kt.logger.EncryptorLogger;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class DoubleCryptoConverter extends AbstractCryptoConverter<Double> implements AttributeConverter<Double, String> {

	public DoubleCryptoConverter() {
		this(new CipherInitializer());
	}

	public DoubleCryptoConverter(CipherInitializer cipherInitializer) {
		super(cipherInitializer);
	}

	@Override
	protected boolean isNotNullOrEmpty(Double attribute) {
		return attribute != null;
	}

	@Override
	protected Double stringToEntityAttribute(String dbData) {
		try {
			return (dbData == null || dbData.isEmpty()) ? null : Double.parseDouble(dbData);
		} catch (NumberFormatException nfe) {
			EncryptorLogger.errorMessage(this.getClass().getName(), "Invalid long value in database.");
			return null;
		}
	}

	@Override
	protected String entityAttributeToString(Double attribute) {
		return attribute == null ? null : attribute.toString();
	}
}
