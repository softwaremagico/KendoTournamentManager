package com.softwaremagico.kt.utils;

import com.softwaremagico.kt.persistence.entities.User;
import org.springframework.stereotype.Service;
import org.testng.Assert;

@Service
public class UserFactory {
    public static final String DEFAULT_USER_FIRSTNAME = "Clarke";
    public static final String DEFAULT_USER_LASTNAME = "Griffin";
    public static final String DEFAULT_USER_ID_CARD = "11111111A";

    public User createDefaultUser() {
        final User user = new User(DEFAULT_USER_ID_CARD, DEFAULT_USER_FIRSTNAME, DEFAULT_USER_LASTNAME);
        return user;
    }

    public void checkDefaultUser(User user) {
        Assert.assertEquals(user.getIdCard(), DEFAULT_USER_ID_CARD);
        Assert.assertEquals(user.getLastname(), DEFAULT_USER_LASTNAME);
        Assert.assertEquals(user.getName(), DEFAULT_USER_FIRSTNAME);

    }
}
