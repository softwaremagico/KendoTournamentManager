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

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

import static com.softwaremagico.kt.persistence.encryption.KeyProperty.databasePrivateKey;
import static com.softwaremagico.kt.persistence.encryption.KeyProperty.databasePublicKey;

/**
 * RSA/ECB/OAEPWithSHA-1AndMGF1Padding implementation for encrypt and decrypt.
 * Better on performance that GCM, but still too slow.
 */
public class ECBCipherEngine implements ICipherEngine {

    private static final String CIPHER_INSTANCE_NAME = "RSA/ECB/OAEPWithSHA-1AndMGF1Padding";
    private static final String SECRET_KEY_ALGORITHM = "RSA";
    public PrivateKey privateKey;
    public PublicKey publicKey;

    @Override
    public String encrypt(String input) throws InvalidEncryptionException {
        return encrypt(input, databasePublicKey);
    }

    @Override
    public String encrypt(String input, String publickey) throws InvalidEncryptionException {
        try {
            final Cipher cipher = Cipher.getInstance(CIPHER_INSTANCE_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, loadPublicKey(publickey));
            return Base64.getEncoder().encodeToString(cipher.doFinal(input.getBytes(StandardCharsets.UTF_8)));
        } catch (InvalidEncryptionException | GeneralSecurityException e) {
            throw new InvalidEncryptionException(e);
        }
    }

    @Override
    public String decrypt(String encrypted) throws InvalidEncryptionException {
        return decrypt(encrypted, databasePrivateKey);
    }

    @Override
    public String decrypt(String encrypted, String privatekey) throws InvalidEncryptionException {
        try {
            final Cipher cipher = Cipher.getInstance(CIPHER_INSTANCE_NAME);
            cipher.init(Cipher.DECRYPT_MODE, loadPrivateKey(privatekey));
            return new String(cipher.doFinal(Base64.getDecoder().decode(encrypted)), StandardCharsets.UTF_8);
        } catch (InvalidEncryptionException | GeneralSecurityException e) {
            throw new InvalidEncryptionException(e);
        }
    }

    // convert String publickey to Key object
    public Key loadPublicKey(String stored)
            throws GeneralSecurityException {
        final byte[] data = Base64.getDecoder().decode(stored);
        final X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
        final KeyFactory fact = KeyFactory.getInstance(SECRET_KEY_ALGORITHM);
        return fact.generatePublic(spec);
    }

    // Convert String private key to privateKey object
    public PrivateKey loadPrivateKey(String stored)
            throws GeneralSecurityException {
        final byte[] clear = Base64.getDecoder().decode(stored);
        final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(clear);
        final KeyFactory fact = KeyFactory.getInstance(SECRET_KEY_ALGORITHM);
        final PrivateKey priv = fact.generatePrivate(keySpec);
        Arrays.fill(clear, (byte) 0);
        return priv;
    }

    public void generateKeys() throws NoSuchAlgorithmException {
        final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(SECRET_KEY_ALGORITHM);
        keyGen.initialize(4096);
        final KeyPair pair = keyGen.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
    }

    public String getPrivateKey() {
        if (privateKey == null) {
            throw new InvalidEncryptionException("No private key loaded!");
        }
        final String encodedPrivateKey = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        EncryptorLogger.debug(this.getClass().getName(), "Private key: " + encodedPrivateKey);
        return encodedPrivateKey;
    }

    public String getPublicKey() {
        if (publicKey == null) {
            throw new InvalidEncryptionException("No public key loaded!");
        }

        final String encodedPublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        EncryptorLogger.debug(this.getClass().getName(), "Public key: " + encodedPublicKey);
        return encodedPublicKey;
    }
}
