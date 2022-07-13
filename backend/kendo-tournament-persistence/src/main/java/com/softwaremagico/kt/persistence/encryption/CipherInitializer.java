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
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

import static com.softwaremagico.kt.persistence.encryption.KeyProperty.databaseEncryptionKey;

public class CipherInitializer {

    private static final String CIPHER_INSTANCE_NAME = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY_ALGORITHM = "AES";
    private static final String SECRET_MESSAGE_DIGEST_ALGORITHM = "SHA-256";
    private static final SecureRandom random = new SecureRandom();
    private Cipher cipher;
    private IvParameterSpec ivSpec;
    private SecretKeySpec keySpec;

    public String encrypt(String input) throws InvalidAlgorithmParameterException, InvalidKeyException, NoSuchAlgorithmException,
            BadPaddingException, IllegalBlockSizeException {
        getCipher().init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        final byte[] encryptedBytes = getCipher().doFinal(input.getBytes(StandardCharsets.UTF_8));
        final String encodedValue = Base64.getEncoder().encodeToString(encryptedBytes);
        EncryptorLogger.debug(this.getClass().getName(), "Encrypted value for '{}' is '{}'.", input, encodedValue);
        return encodedValue;
    }

    public String decrypt(String encrypted) throws BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException,
            InvalidKeyException {
        getCipher().init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        final byte[] encryptedBytes = Base64.getDecoder().decode(encrypted.getBytes(StandardCharsets.UTF_8));
        final byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        final String decrypted = new String(decryptedBytes, StandardCharsets.UTF_8);
        EncryptorLogger.debug(this.getClass().getName(), "Decrypted value for '{}' is '{}'.", encrypted, decrypted);
        return decrypted;
    }

    private Cipher getCipher() {
        if (cipher == null) {
            try {
                cipher = Cipher.getInstance(CIPHER_INSTANCE_NAME);
                final byte[] iv = new byte[cipher.getBlockSize()];
                random.nextBytes(iv);
                ivSpec = new IvParameterSpec(iv);

                // hash keyString with SHA-256 and crop the output to 128-bit for key
                final MessageDigest digest = MessageDigest.getInstance(SECRET_MESSAGE_DIGEST_ALGORITHM);
                digest.update(databaseEncryptionKey.getBytes(StandardCharsets.UTF_8));
                final byte[] key = new byte[16];
                System.arraycopy(digest.digest(), 0, key, 0, key.length);
                keySpec = new SecretKeySpec(key, SECRET_KEY_ALGORITHM);
            } catch (Exception e) {
                EncryptorLogger.severe(this.getClass().getName(), "invalid cipher algorithm selected");
                EncryptorLogger.errorMessage(this.getClass().getName(), e);
                System.exit(0);
            }
        }
        return cipher;
    }
}
