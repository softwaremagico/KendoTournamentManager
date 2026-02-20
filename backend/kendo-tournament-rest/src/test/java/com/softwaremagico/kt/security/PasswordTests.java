package com.softwaremagico.kt.security;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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

import com.softwaremagico.kt.rest.controllers.AuthenticatedUserController;
import com.softwaremagico.kt.rest.exceptions.InvalidPasswordException;
import com.softwaremagico.kt.rest.exceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = "passwordTests")
@SpringBootTest
public class PasswordTests extends AbstractTestNGSpringContextTests {
    private static final String USER_NAME = "user";
    private static final String USER_FIRST_NAME = "Test";
    private static final String USER_LAST_NAME = "User";
    private static final String USER_PASSWORD = "password";

    private static final String USER_NEW_PASSWORD = "password2";

    private static final String[] USER_ROLES = new String[] {"admin", "viewer"};

    @Autowired
    private AuthenticatedUserController authenticatedUserController;

    @BeforeClass
    public void setUp() {
        authenticatedUserController.createUser(null, USER_NAME, USER_FIRST_NAME, USER_LAST_NAME, USER_PASSWORD, USER_ROLES);
    }

    @Test(expectedExceptions = UserNotFoundException.class)
    public void updatePasswordInvalidUser() {
        authenticatedUserController.updatePassword(USER_NAME + "_error", USER_PASSWORD, USER_NEW_PASSWORD, null);
    }

    @Test(expectedExceptions = InvalidPasswordException.class)
    public void updatePasswordInvalidOld() {
        authenticatedUserController.updatePassword(USER_NAME, USER_PASSWORD + "_error", USER_NEW_PASSWORD, null);
    }

    @Test
    public void updatePasswordCorrectOld() {
        authenticatedUserController.updatePassword(USER_NAME, USER_PASSWORD, USER_NEW_PASSWORD, null);
        authenticatedUserController.updatePassword(USER_NAME, USER_NEW_PASSWORD, USER_PASSWORD, null);
    }
}
