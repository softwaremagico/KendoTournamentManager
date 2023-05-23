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

import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;
import org.testng.annotations.Test;

@SpringBootTest
@Test(groups = "encryptedData")
public class CheckEncryptionTest {
    private static final String PASSWORD = "myEncryptionCode";

    private CipherInitializer cipherInitializer = new CipherInitializer();
    private final KeyProperty keyProperty = new KeyProperty(PASSWORD);

    @Test
    public void checkPassword(){
        Assert.assertEquals(KeyProperty.databaseEncryptionKey, PASSWORD);
    }

    @Test
    public void encryptAndDecrypt() throws Exception {
        final String testText = "The text I want to encrypt";
        final String encryptedText = cipherInitializer.encrypt(testText, PASSWORD);
        final String decryptedText = cipherInitializer.decrypt(encryptedText, PASSWORD);
        Assert.assertEquals(testText, decryptedText);
    }

    @Test
    public void encryptAndDecrypt2() throws Exception {
        final String testText = "The text I want to encrypt";
        final String encryptedText = cipherInitializer.encrypt(testText);
        final String decryptedText = cipherInitializer.decrypt(encryptedText);
        Assert.assertEquals(testText, decryptedText);
    }
}
