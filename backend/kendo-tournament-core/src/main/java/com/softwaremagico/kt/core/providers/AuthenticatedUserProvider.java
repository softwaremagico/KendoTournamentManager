package com.softwaremagico.kt.core.providers;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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

import com.softwaremagico.kt.core.exceptions.DuplicatedUserException;
import com.softwaremagico.kt.persistence.entities.AuthenticatedUser;
import com.softwaremagico.kt.persistence.repositories.AuthenticatedUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public long count() {
        return authenticatedUserRepository.count();
    }

    public Optional<AuthenticatedUser> findByUniqueId(String uniqueId) {
        return findByUsername(uniqueId);
    }

    public AuthenticatedUser save(String username, String firstName, String lastName, String password, String... roles) {
        if (findByUsername(username).isPresent()) {
            throw new DuplicatedUserException(this.getClass(), "Username exists!");
        }

        final AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setUsername(username);
        authenticatedUser.setName(firstName);
        authenticatedUser.setLastname(lastName);
        authenticatedUser.setPassword(password);
        if (roles != null) {
            authenticatedUser.setRoles(Stream.of(roles).collect(Collectors.toSet()));
        }

        return save(authenticatedUser);
    }

    public AuthenticatedUser save(AuthenticatedUser authenticatedUser) {
        return authenticatedUserRepository.save(authenticatedUser);
    }

    public AuthenticatedUser updateRoles(AuthenticatedUser authenticatedUser, Set<String> roles) {
        authenticatedUser.setRoles(roles);
        return authenticatedUserRepository.save(authenticatedUser);
    }

    public List<AuthenticatedUser> findAll() {
        return authenticatedUserRepository.findAll();
    }

    public void delete(AuthenticatedUser authenticatedUser) {
        authenticatedUserRepository.delete(authenticatedUser);
    }

}
