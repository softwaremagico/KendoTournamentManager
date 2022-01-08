package com.softwaremagico.kt.rest.controllers;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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

import com.softwaremagico.kt.core.exceptions.DuplicatedUserException;
import com.softwaremagico.kt.core.providers.AuthenticatedUserProvider;
import com.softwaremagico.kt.persistence.entities.AuthenticatedUser;
import com.softwaremagico.kt.rest.exceptions.BadRequestException;
import com.softwaremagico.kt.rest.security.dto.CreateUserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class AuthenticatedUserController {

    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Autowired
    public AuthenticatedUserController(AuthenticatedUserProvider authenticatedUserProvider) {
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    public AuthenticatedUser createUser(CreateUserRequest createUserRequest) {
        return createUser(createUserRequest.getUsername(), createUserRequest.getFullName(), createUserRequest.getPassword());
    }

    public AuthenticatedUser createUser(String username, String fullName, String password) {
        try {
            return authenticatedUserProvider.createUser(username, fullName, password);
        } catch (DuplicatedUserException e) {
            throw new BadRequestException(this.getClass(), "Username exists!");
        }
    }

    public List<AuthenticatedUser> findAll() {
        return authenticatedUserProvider.findAll();
    }

    public void delete(AuthenticatedUser authenticatedUser) {
        authenticatedUserProvider.delete(authenticatedUser);
    }
}
