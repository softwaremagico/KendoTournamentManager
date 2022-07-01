package com.softwaremagico.kt.persistence.encryption;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
 * %%
 * Copyright (C) 2021 - 2022 Softwaremagico
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

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;

public class CipherInitializer {

	private static final String CIPHER_INSTANCE_NAME = "AES/CBC/PKCS5Padding";
	private static final String SECRET_KEY_ALGORITHM = "AES";

	public Cipher prepareAndInitCipher(int encryptionMode, String key) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException,
			InvalidAlgorithmParameterException {
		final Cipher cipher = Cipher.getInstance(CIPHER_INSTANCE_NAME);
		final Key secretKey = new SecretKeySpec(key.getBytes(Charset.defaultCharset()), SECRET_KEY_ALGORITHM);
		final AlgorithmParameterSpec algorithmParameters = getAlgorithmParameterSpec(cipher);

		callCipherInit(cipher, encryptionMode, secretKey, algorithmParameters);
		return cipher;
	}

	private void callCipherInit(Cipher cipher, int encryptionMode, Key secretKey, AlgorithmParameterSpec algorithmParameters) throws InvalidKeyException,
			InvalidAlgorithmParameterException {
		cipher.init(encryptionMode, secretKey, algorithmParameters);
	}

	private int getCipherBlockSize(Cipher cipher) {
		return cipher.getBlockSize();
	}

	private AlgorithmParameterSpec getAlgorithmParameterSpec(Cipher cipher) {
		final byte[] iv = new byte[getCipherBlockSize(cipher)];
		return new IvParameterSpec(iv);
	}
}
