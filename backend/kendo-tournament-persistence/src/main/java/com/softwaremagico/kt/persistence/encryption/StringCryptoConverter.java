package com.softwaremagico.kt.persistence.encryption;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class StringCryptoConverter extends AbstractCryptoConverter<String> implements AttributeConverter<String, String> {

	public StringCryptoConverter() {
		this(new CipherInitializer());
	}

	public StringCryptoConverter(CipherInitializer cipherInitializer) {
		super(cipherInitializer);
	}

	@Override
	protected boolean isNotNullOrEmpty(String attribute) {
		return attribute != null && !attribute.isEmpty();
	}

	@Override
	protected String stringToEntityAttribute(String dbData) {
		return dbData;
	}

	@Override
	protected String entityAttributeToString(String attribute) {
		return attribute;
	}
}
