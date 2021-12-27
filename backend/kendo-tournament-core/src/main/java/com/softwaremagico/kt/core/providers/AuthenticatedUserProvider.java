package com.softwaremagico.kt.core.providers;

import com.softwaremagico.kt.core.exceptions.DuplicatedUserException;
import com.softwaremagico.kt.persistence.entities.AuthenticatedUser;
import com.softwaremagico.kt.persistence.repositories.AuthenticatedUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AuthenticatedUserProvider {
    private final AuthenticatedUserRepository authenticatedUserRepository;

    @Autowired
    public AuthenticatedUserProvider(AuthenticatedUserRepository authenticatedUserRepository) {
        this.authenticatedUserRepository = authenticatedUserRepository;
    }


    public Optional<AuthenticatedUser> findByUsername(String username) {
        return authenticatedUserRepository.findByUsername(username);
    }

    public Optional<AuthenticatedUser> findByUniqueId(String uniqueId) {
        return findByUsername(uniqueId);
    }

    public AuthenticatedUser createUser(String username, String fullName, String password) {
        if (findByUsername(username).isPresent()) {
            throw new DuplicatedUserException(this.getClass(), "Username exists!");
        }

        final AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setUsername(username);
        authenticatedUser.setFullName(fullName);
        authenticatedUser.setPassword(password);

        return createUser(authenticatedUser);
    }

    public AuthenticatedUser createUser(AuthenticatedUser authenticatedUser) {
        return authenticatedUserRepository.save(authenticatedUser);
    }

    public List<AuthenticatedUser> findAll() {
        return authenticatedUserRepository.findAll();
    }

    public void delete(AuthenticatedUser authenticatedUser) {
        authenticatedUserRepository.delete(authenticatedUser);
    }

}
