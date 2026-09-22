package com.softwaremagico.kt.core.providers;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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

import com.softwaremagico.kt.core.exceptions.DuplicatedUserException;
import com.softwaremagico.kt.persistence.entities.AuthenticatedUser;
import com.softwaremagico.kt.persistence.entities.IAuthenticatedUser;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.TenantContext;
import com.softwaremagico.kt.persistence.repositories.AuthenticatedUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.softwaremagico.kt.persistence.encryption.KeyProperty.getDatabaseEncryptionKey;

@Repository
public class AuthenticatedUserProvider {

    public static final String GUEST_USER = "guest";
    public static final String GUEST_ROLE = "guest";

    private final AuthenticatedUserRepository authenticatedUserRepository;

    private final ParticipantProvider participantProvider;


    private final boolean guestEnabled;

    @Autowired
    public AuthenticatedUserProvider(AuthenticatedUserRepository authenticatedUserRepository, ParticipantProvider participantProvider,
                                     @Value("${enable.guest.user:false}") String guestUsersEnabled) {
        this.authenticatedUserRepository = authenticatedUserRepository;
        this.participantProvider = participantProvider;
        guestEnabled = Boolean.parseBoolean(guestUsersEnabled);
    }


    public Optional<IAuthenticatedUser> findByUsername(String username) {
        //Create guest user on the fly
        if (Objects.equals(username, GUEST_USER) && guestEnabled) {
            final AuthenticatedUser guest = new AuthenticatedUser(GUEST_USER, TenantContext.getRequiredTenantId());
            guest.setRoles(Collections.singleton(GUEST_ROLE));
            return Optional.of(guest);
        }
        if (getDatabaseEncryptionKey() != null && !getDatabaseEncryptionKey().isBlank()) {
            //Username is encrypted, use hash
            final Optional<AuthenticatedUser> authenticatedUser = TenantContext.getTenantId() == null
                    ? authenticatedUserRepository.findByUsernameHash(username)
                    : authenticatedUserRepository.findByUsernameHashAndTenantId(username, TenantContext.getRequiredTenantId());
            if (authenticatedUser.isPresent()) {
                authenticatedUser.get().setUsernameHash(authenticatedUser.get().getUsername());
                return TenantContext.getTenantId() == null ? authenticatedUser.map(IAuthenticatedUser.class::cast)
                        : authenticatedUser.filter(user -> Objects.equals(user.getTenantId(), TenantContext.getRequiredTenantId()))
                        .map(IAuthenticatedUser.class::cast);
            }
        } else {
            //Username is not encrypted, use username for compatibility with old databases.
            final Optional<AuthenticatedUser> authenticatedUser = TenantContext.getTenantId() == null
                    ? authenticatedUserRepository.findByUsername(username)
                    : authenticatedUserRepository.findByUsernameAndTenantId(username, TenantContext.getRequiredTenantId());
            if (authenticatedUser.isPresent()) {
                return TenantContext.getTenantId() == null ? authenticatedUser.map(IAuthenticatedUser.class::cast)
                        : authenticatedUser.filter(user -> Objects.equals(user.getTenantId(), TenantContext.getRequiredTenantId()))
                        .map(IAuthenticatedUser.class::cast);
            }
        }
        final Optional<Participant> participant = participantProvider.findByTokenUsername(username);
        if (participant.isPresent()) {
            return Optional.of(participant.get());
        }
        return Optional.empty();
    }

    public long count() {
        return authenticatedUserRepository.count();
    }

    public Optional<IAuthenticatedUser> findByUniqueId(String uniqueId) {
        return findByUsername(uniqueId);
    }

    public AuthenticatedUser save(String creator, String username, String firstName, String lastName, String password, String... roles) {
        if (findByUsername(username).isPresent()) {
            throw new DuplicatedUserException(this.getClass(), "Username exists!");
        }

        final AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setUsername(username);
        authenticatedUser.setName(firstName);
        authenticatedUser.setLastname(lastName);
        authenticatedUser.setPassword(password);
        authenticatedUser.setCreatedBy(creator);
        if (roles != null) {
            authenticatedUser.setRoles(Stream.of(roles).collect(Collectors.toSet()));
        }

        return save(authenticatedUser);
    }

    public AuthenticatedUser save(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser.getTenantId() == null) {
            authenticatedUser.setTenantId(TenantContext.getRequiredTenantId());
        }
        if (!Objects.equals(authenticatedUser.getTenantId(), TenantContext.getRequiredTenantId())) {
            throw new SecurityException("A user cannot be saved in another tenant.");
        }
        return authenticatedUserRepository.save(authenticatedUser);
    }

    public AuthenticatedUser updateRoles(AuthenticatedUser authenticatedUser, Set<String> roles) {
        authenticatedUser.setRoles(roles);
        return authenticatedUserRepository.save(authenticatedUser);
    }

    public List<AuthenticatedUser> findAll() {
        if (TenantContext.getTenantId() == null) {
            return authenticatedUserRepository.findAll();
        }
        return authenticatedUserRepository.findAll().stream()
                .filter(user -> Objects.equals(user.getTenantId(), TenantContext.getRequiredTenantId())).toList();
    }

    public void delete(AuthenticatedUser authenticatedUser) {
        authenticatedUserRepository.delete(authenticatedUser);
    }

    public void deleteAll() {
        authenticatedUserRepository.deleteAll();
    }

}
