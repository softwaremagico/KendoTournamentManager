package com.softwaremagico.kt.persistence.encryption;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
 * %%
 * This software is designed by Jorge Hortelano Otero. Jorge Hortelano Otero
 * <softwaremagico@gmail.com> Valencia (Spain).
 *  
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *  
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.softwaremagico.kt.logger.EncryptorLogger;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class LongCryptoConverter extends AbstractCryptoConverter<Long> implements AttributeConverter<Long, String> {

	public LongCryptoConverter() {
		this(new CBCCipherEngine());
	}

	public LongCryptoConverter(ICipherEngine CBCCipherEngine) {
		super(CBCCipherEngine);
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
			EncryptorLogger.errorMessage(this.getClass().getName(), "Invalid long value '{}' in database.", dbData);
			return null;
		}
	}

	@Override
	protected String entityAttributeToString(Long attribute) {
		return attribute == null ? null : attribute.toString();
	}
}
