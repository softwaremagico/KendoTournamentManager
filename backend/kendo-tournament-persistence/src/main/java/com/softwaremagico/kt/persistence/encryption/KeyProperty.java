package com.softwaremagico.kt.persistence.encryption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KeyProperty {

    public static String databaseEncryptionKey;

    public KeyProperty(@Value("${database.encryption.key}") String databaseEncryptionKey) {
        setDatabaseEncryptionKey(databaseEncryptionKey);
    }

    private static synchronized  void setDatabaseEncryptionKey(String databaseEncryptionKey) {
        KeyProperty.databaseEncryptionKey = databaseEncryptionKey;
    }
}
