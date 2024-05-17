package com.softwaremagico.kt.persistence.encryption;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
 * %%
 * Copyright (C) 2021 - 2024 Softwaremagico
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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KeyProperty {

    private static String databaseEncryptionKey;
    private static String databasePublicKey;
    private static String databasePrivateKey;

    public KeyProperty(@Value("${database.encryption.key:#{null}}") String databaseEncryptionKey,
                       @Value("${database.public.key:#{null}}") String databasePublicKey,
                       @Value("${database.private.key:#{null}}") String databasePrivateKey) {
        setDatabaseEncryptionKey(databaseEncryptionKey);
        setDatabasePublicKey(databasePublicKey);
        setDatabasePrivateKey(databasePrivateKey);
    }

    public static synchronized String getDatabaseEncryptionKey() {
        return databaseEncryptionKey;
    }

    private static synchronized void setDatabaseEncryptionKey(String databaseEncryptionKey) {
        KeyProperty.databaseEncryptionKey = databaseEncryptionKey;
    }

    public static synchronized String getDatabasePublicKey() {
        return databasePublicKey;
    }

    private static synchronized void setDatabasePublicKey(String databasePublicKey) {
        KeyProperty.databasePublicKey = databasePublicKey;
    }

    public static synchronized String getDatabasePrivateKey() {
        return databasePrivateKey;
    }

    private static synchronized void setDatabasePrivateKey(String databasePrivateKey) {
        KeyProperty.databasePrivateKey = databasePrivateKey;
    }
}
