package com.softwaremagico.kt.persistence.encryption;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.softwaremagico.kt.logger.EncryptorLogger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import static com.softwaremagico.kt.persistence.encryption.KeyProperty.getDatabaseEncryptionKey;

/**
 * AES/GCM/NoPadding implementation for encrypt and decrypt.
 * Must be slow to avoid attacks. Not useful for database encryption.
 */
public class GCMCipherEngine implements ICipherEngine {

    public static final int GCM_IV_LENGTH = 12;
    private static final String CIPHER_INSTANCE_NAME = "AES/GCM/NoPadding";
    private static final String SECRET_KEY_ALGORITHM = "AES";
    private static final String SECRET_KEY_FACTORY_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int SALT_LENGTH_BYTE = 16;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private Cipher cipher;

    public static byte[] getRandomNonce(int numBytes) {
        final byte[] nonce = new byte[numBytes];
        SECURE_RANDOM.nextBytes(nonce);
        return nonce;
    }

    // AES secret key
    public static SecretKeySpec getAESKey(byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return getAESKey(getDatabaseEncryptionKey(), salt);
    }

    public static SecretKeySpec getAESKey(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        final SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_KEY_FACTORY_ALGORITHM);
        final KeySpec spec = new PBEKeySpec(password != null ? password.toCharArray() : null, salt,
                512, 256);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), SECRET_KEY_ALGORITHM);
    }

    // AES-GCM needs GCMParameterSpec
    @Override
    public String encrypt(String input) throws InvalidEncryptionException {
        return encrypt(input, getDatabaseEncryptionKey());
    }

    @Override
    public String encrypt(String input, String password) throws InvalidEncryptionException {
        try {
            if (input == null) {
                return null;
            }
            final byte[] salt = getRandomNonce(SALT_LENGTH_BYTE);
            final byte[] iv = getRandomNonce(GCM_IV_LENGTH);
            getCipher().init(Cipher.ENCRYPT_MODE, getAESKey(password, salt), new GCMParameterSpec(TAG_LENGTH_BIT, iv));
            final byte[] encryptedText = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));
            final byte[] encryptedBytes = ByteBuffer.allocate(iv.length + salt.length + encryptedText.length)
                    .put(iv)
                    .put(salt)
                    .put(encryptedText)
                    .array();
            final String encodedValue = Base64.getEncoder().encodeToString(encryptedBytes);
            EncryptorLogger.debug(this.getClass().getName(), "Encrypted value for '{}' is '{}'.", input, encodedValue);
            return encodedValue;
        } catch (BadPaddingException
                 | IllegalBlockSizeException
                 | InvalidAlgorithmParameterException
                 | InvalidKeyException
                 | NoSuchPaddingException
                 | NoSuchAlgorithmException
                 | InvalidKeySpecException e) {
            throw new InvalidEncryptionException(e);
        }
    }

    @Override
    public String decrypt(String encrypted) throws InvalidEncryptionException {
        return decrypt(encrypted, getDatabaseEncryptionKey());
    }

    @Override
    public String decrypt(String encrypted, String password) throws InvalidEncryptionException {
        try {
            if (encrypted == null) {
                return null;
            }
            final byte[] encryptedBytes = Base64.getDecoder().decode(encrypted.getBytes(StandardCharsets.UTF_8));
            final ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedBytes);

            final byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            final byte[] salt = new byte[SALT_LENGTH_BYTE];
            byteBuffer.get(salt);
            final byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            getCipher().init(Cipher.DECRYPT_MODE, getAESKey(password, salt), new GCMParameterSpec(TAG_LENGTH_BIT, iv));
            final byte[] decryptedBytes = cipher.doFinal(cipherText);
            final String decrypted = new String(decryptedBytes, StandardCharsets.UTF_8);
            EncryptorLogger.debug(this.getClass().getName(), "Decrypted value for '{}' is '{}'.", encrypted, decrypted);
            return decrypted;
        } catch (BadPaddingException
                 | IllegalBlockSizeException
                 | InvalidAlgorithmParameterException
                 | InvalidKeyException
                 | NoSuchPaddingException
                 | NoSuchAlgorithmException
                 | InvalidKeySpecException e) {
            throw new InvalidEncryptionException(e);
        }
    }

    private Cipher getCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
        if (cipher == null) {
            cipher = Cipher.getInstance(CIPHER_INSTANCE_NAME);
        }
        return cipher;
    }
}
