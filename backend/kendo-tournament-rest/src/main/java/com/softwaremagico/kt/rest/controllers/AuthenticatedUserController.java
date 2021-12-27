package com.softwaremagico.kt.rest.controllers;

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
