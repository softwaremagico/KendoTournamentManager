package com.softwaremagico.kt.persistence.encryption;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
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

	void callCipherInit(Cipher cipher, int encryptionMode, Key secretKey, AlgorithmParameterSpec algorithmParameters) throws InvalidKeyException,
			InvalidAlgorithmParameterException {
		cipher.init(encryptionMode, secretKey, algorithmParameters);
	}

	int getCipherBlockSize(Cipher cipher) {
		return cipher.getBlockSize();
	}

	private AlgorithmParameterSpec getAlgorithmParameterSpec(Cipher cipher) {
		final byte[] iv = new byte[getCipherBlockSize(cipher)];
		return new IvParameterSpec(iv);
	}
}
