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

import com.softwaremagico.kt.logger.EncryptorLogger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.persistence.AttributeConverter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static com.softwaremagico.kt.persistence.encryption.KeyProperty.databaseEncryptionKey;


public abstract class AbstractCryptoConverter<T> implements AttributeConverter<T, String> {

    private CipherInitializer cipherInitializer;

    private static Cipher cipherEncryptor;
    private static Cipher cipherDecryptor;

    public AbstractCryptoConverter() {
        this(new CipherInitializer());
    }

    public AbstractCryptoConverter(CipherInitializer cipherInitializer) {
        this.cipherInitializer = cipherInitializer;
    }

    @Override
    public String convertToDatabaseColumn(T attribute) {
        if (databaseEncryptionKey != null && !databaseEncryptionKey.isEmpty() && isNotNullOrEmpty(attribute)) {
            try {
                return encrypt(getCipherEncryptor(), attribute);
            } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | BadPaddingException | NoSuchPaddingException
                    | IllegalBlockSizeException e) {
                throw new RuntimeException(e);
            }
        }
        return entityAttributeToString(attribute);
    }

    @Override
    public T convertToEntityAttribute(String dbData) {
        if (databaseEncryptionKey != null && !databaseEncryptionKey.isEmpty() && dbData != null && !dbData.isEmpty()) {
            try {
                return decrypt(getCipherDecryptor(), dbData);
            } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | BadPaddingException | NoSuchPaddingException
                    | IllegalBlockSizeException e) {
                throw new RuntimeException(e);
            }
        }
        return stringToEntityAttribute(dbData);
    }

    protected abstract boolean isNotNullOrEmpty(T attribute);

    protected abstract T stringToEntityAttribute(String dbData);

    protected abstract String entityAttributeToString(T attribute);

    private byte[] callCipherDoFinal(Cipher cipher, byte[] bytes) throws IllegalBlockSizeException, BadPaddingException {
        return cipher.doFinal(bytes);
    }

    private static Cipher getCipherEncryptor() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            NoSuchPaddingException, InvalidKeyException {
        if (cipherEncryptor == null) {
            final CipherInitializer cipherInitializer = new CipherInitializer();
            cipherEncryptor = cipherInitializer.prepareAndInitCipher(Cipher.ENCRYPT_MODE, databaseEncryptionKey);
        }
        return cipherEncryptor;
    }

    private static Cipher getCipherDecryptor() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            NoSuchPaddingException, InvalidKeyException {
        if (cipherDecryptor == null) {
            final CipherInitializer cipherInitializer = new CipherInitializer();
            cipherDecryptor = cipherInitializer.prepareAndInitCipher(Cipher.DECRYPT_MODE, databaseEncryptionKey);
        }
        return cipherDecryptor;
    }

    private String encrypt(Cipher cipher, T attribute) throws IllegalBlockSizeException, BadPaddingException {
        final byte[] bytesToEncrypt = entityAttributeToString(attribute).getBytes(StandardCharsets.UTF_8);
        final byte[] encryptedBytes = callCipherDoFinal(cipher, bytesToEncrypt);
        final String encodedValue = Base64.getEncoder().encodeToString(encryptedBytes);
        EncryptorLogger.debug(this.getClass().getName(), "Encrypted value for '{}' is '{}'.", attribute, encodedValue);
        return encodedValue;
    }

    private T decrypt(Cipher cipher, String dbData) throws IllegalBlockSizeException, BadPaddingException {
        final byte[] encryptedBytes = Base64.getDecoder().decode(dbData.getBytes(StandardCharsets.UTF_8));
        final byte[] decryptedBytes = callCipherDoFinal(cipher, encryptedBytes);
        final T entity = stringToEntityAttribute(new String(decryptedBytes, Charset.defaultCharset()));
        EncryptorLogger.debug(this.getClass().getName(), "Decrypted value for '{}' is '{}'.", dbData, entity);
        return entity;
    }
}
