package com.softwaremagico.kt.utils;

import com.softwaremagico.kt.persistence.entities.User;
import org.springframework.stereotype.Service;
import org.testng.Assert;

import java.time.LocalDate;

@Service
public class UserFactory {
    public static final String DEFAULT_USER_FIRSTNAME = "Clarke";
    public static final String DEFAULT_USER_LASTNAME = "Griffin";
    public static final String DEFAULT_USER_ID_CARD = "11111111A";
    public static final String DEFAULT_USER_EMAIL = "clarke@ark.com";
    public static final String DEFAULT_USER_BIRTHDAY = "2070-11-30";
    public static final String DEFAULT_USER_PHONE_NUMBER = "555-123456";
    public static final String DEFAULT_USER_ADDRESS = "Area G, Room 67";
    public static final String DEFAULT_USER_POSTAL_CODE = "99999";
    public static final String DEFAULT_USER_CITY = "The Ark";

    public User createDefaultUser() {
        final User user = new User();
        user.setFirstname(DEFAULT_USER_FIRSTNAME);
        user.setLastname(DEFAULT_USER_LASTNAME);
        user.setIdCardNumber(DEFAULT_USER_ID_CARD);
        user.setEmail(DEFAULT_USER_EMAIL);
        user.setBirthdate(LocalDate.parse(DEFAULT_USER_BIRTHDAY));
        user.setPhoneNumber(DEFAULT_USER_PHONE_NUMBER);
        user.setAddress(DEFAULT_USER_ADDRESS);
        user.setPostalCode(DEFAULT_USER_POSTAL_CODE);
        user.setCity(DEFAULT_USER_CITY);
        return user;
    }

    public void checkDefaultUser(User user) {
        Assert.assertEquals(user.getFirstname(), DEFAULT_USER_FIRSTNAME);
        Assert.assertEquals(user.getLastname(), DEFAULT_USER_LASTNAME);
        Assert.assertEquals(user.getIdCardNumber(), DEFAULT_USER_ID_CARD);
        Assert.assertEquals(user.getEmail(), DEFAULT_USER_EMAIL);
        Assert.assertEquals(user.getBirthdate().toString(), DEFAULT_USER_BIRTHDAY);
        Assert.assertEquals(user.getPhoneNumber(), DEFAULT_USER_PHONE_NUMBER);
        Assert.assertEquals(user.getAddress(), DEFAULT_USER_ADDRESS);
        Assert.assertEquals(user.getPostalCode(), DEFAULT_USER_POSTAL_CODE);
        Assert.assertEquals(user.getCity(), DEFAULT_USER_CITY);

    }
}
