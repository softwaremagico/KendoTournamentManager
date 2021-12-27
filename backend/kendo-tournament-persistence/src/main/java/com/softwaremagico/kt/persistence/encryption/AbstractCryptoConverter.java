package com.softwaremagico.kt.persistence.encryption;

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
                final Cipher cipher = cipherInitializer.prepareAndInitCipher(Cipher.ENCRYPT_MODE, databaseEncryptionKey);
                return encrypt(cipher, attribute);
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
                final Cipher cipher = cipherInitializer.prepareAndInitCipher(Cipher.DECRYPT_MODE, databaseEncryptionKey);
                return decrypt(cipher, dbData);
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
