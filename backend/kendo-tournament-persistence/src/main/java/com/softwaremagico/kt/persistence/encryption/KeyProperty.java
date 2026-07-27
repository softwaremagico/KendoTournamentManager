package com.softwaremagico.kt.persistence.encryption;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
 * %%
 * Copyright (C) 2021 - 2026 Softwaremagico
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

    private static KeyProperty instance;

    private final String databaseEncryptionKey;
    private final String databasePublicKey;
    private final String databasePrivateKey;

    protected KeyProperty(@Value("${database.encryption.key:#{null}}") String databaseEncryptionKey,
                       @Value("${database.public.key:#{null}}") String databasePublicKey,
                       @Value("${database.private.key:#{null}}") String databasePrivateKey) {
        this.databaseEncryptionKey = databaseEncryptionKey;
        this.databasePublicKey = databasePublicKey;
        this.databasePrivateKey = databasePrivateKey;
        instance = this;
    }

    /**
     * Factory method used to (re)configure the shared encryption keys, mainly from tests,
     * without exposing a public constructor on this class.
     */
    public static void configure(String databaseEncryptionKey, String databasePublicKey, String databasePrivateKey) {
        new KeyProperty(databaseEncryptionKey, databasePublicKey, databasePrivateKey);
    }

    public static String getDatabaseEncryptionKey() {
        return instance.databaseEncryptionKey;
    }

    public static String getDatabasePublicKey() {
        return instance.databasePublicKey;
    }

    public static String getDatabasePrivateKey() {
        return instance.databasePrivateKey;
    }
}
