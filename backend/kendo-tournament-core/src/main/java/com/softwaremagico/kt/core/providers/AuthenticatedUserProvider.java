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
import com.softwaremagico.kt.persistence.entities.UserTenant;
import com.softwaremagico.kt.persistence.repositories.AuthenticatedUserRepository;
import com.softwaremagico.kt.persistence.repositories.UserTenantRepository;
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
    private final UserTenantRepository userTenantRepository;


    private final boolean guestEnabled;

    @Autowired
    public AuthenticatedUserProvider(AuthenticatedUserRepository authenticatedUserRepository, ParticipantProvider participantProvider,
                                     UserTenantRepository userTenantRepository,
                                     @Value("${enable.guest.user:false}") String guestUsersEnabled) {
        this.authenticatedUserRepository = authenticatedUserRepository;
        this.participantProvider = participantProvider;
        this.userTenantRepository = userTenantRepository;
        guestEnabled = Boolean.parseBoolean(guestUsersEnabled);
    }

    /** Kept for unit tests that do not exercise tenant membership. */
    public AuthenticatedUserProvider(AuthenticatedUserRepository authenticatedUserRepository, ParticipantProvider participantProvider,
                                     String guestUsersEnabled) {
        this(authenticatedUserRepository, participantProvider, null, guestUsersEnabled);
    }


    public Optional<IAuthenticatedUser> findByUsername(String username) {
        //Create guest user on the fly
        if (Objects.equals(username, GUEST_USER) && guestEnabled) {
            final AuthenticatedUser guest = new AuthenticatedUser(GUEST_USER);
            guest.setTenantId(TenantContext.getRequiredTenantId());
            guest.setRoles(Collections.singleton(GUEST_ROLE));
            return Optional.of(guest);
        }
        final Optional<AuthenticatedUser> authenticatedUser = findAuthenticatedUser(username);
        if (authenticatedUser.isPresent()) {
            return scopedUser(authenticatedUser);
        }
        final Optional<Participant> participant = participantProvider.findByTokenUsername(username);
        if (participant.isPresent()) {
            return Optional.of(participant.get());
        }
        return Optional.empty();
    }

    private Optional<AuthenticatedUser> findAuthenticatedUser(String username) {
        if (getDatabaseEncryptionKey() == null || getDatabaseEncryptionKey().isBlank()) {
            return authenticatedUserRepository.findByUsername(username);
        }
        final Optional<AuthenticatedUser> user = authenticatedUserRepository.findByUsernameHash(username);
        user.ifPresent(authenticatedUser -> authenticatedUser.setUsernameHash(authenticatedUser.getUsername()));
        return user;
    }

    private Optional<IAuthenticatedUser> scopedUser(Optional<AuthenticatedUser> user) {
        if (TenantContext.getTenantId() == null) {
            return user.map(IAuthenticatedUser.class::cast);
        }
        return user.filter(candidate -> userTenantRepository.existsByAuthenticatedUserIdAndTenantId(candidate.getId(),
                        TenantContext.getRequiredTenantId()))
                .map(candidate -> {
                    candidate.setTenantId(TenantContext.getRequiredTenantId());
                    return (IAuthenticatedUser) candidate;
                });
    }

    public List<AuthenticatedUser> findAllByUsername(String username) {
        if (getDatabaseEncryptionKey() != null && !getDatabaseEncryptionKey().isBlank()) {
            return authenticatedUserRepository.findAllByUsernameHash(username);
        }
        return authenticatedUserRepository.findAllByUsername(username);
    }

    public List<AuthenticatedUser> findAllUnscoped() {
        return authenticatedUserRepository.findAllUnscoped();
    }

    public boolean belongsToTenant(AuthenticatedUser user, Integer tenantId) {
        return userTenantRepository.existsByAuthenticatedUserIdAndTenantId(user.getId(), tenantId);
    }

    public List<Integer> getTenantIds(AuthenticatedUser user) {
        return userTenantRepository.findAllByAuthenticatedUserId(user.getId()).stream().map(UserTenant::getTenantId).toList();
    }

    public void updateLastTenant(String username, Integer tenantId) {
        authenticatedUserRepository.updateLastTenantIdByUsernameHash(username, tenantId);
    }

    public void removeFromTenant(String username, Integer tenantId) {
        findByUsername(username).ifPresent(user -> userTenantRepository.deleteByAuthenticatedUserIdAndTenantId(user.getId(), tenantId));
    }

    public long count() {
        return authenticatedUserRepository.count();
    }

    public Optional<IAuthenticatedUser> findByUniqueId(String uniqueId) {
        return findByUsername(uniqueId);
    }

    public AuthenticatedUser save(String creator, String username, String firstName, String lastName, String password, String... roles) {
        if (usernameExists(username)) {
            throw new DuplicatedUserException(this.getClass(), "Username exists!");
        }

        final AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setUsername(username);
        authenticatedUser.setName(firstName);
        authenticatedUser.setLastname(lastName);
        authenticatedUser.setPassword(password);
        authenticatedUser.setCreatedBy(creator);
        authenticatedUser.setLastTenantId(getEffectiveTenantId());
        if (roles != null) {
            authenticatedUser.setRoles(Stream.of(roles).collect(Collectors.toSet()));
        }

        return save(authenticatedUser);
    }

    private boolean usernameExists(String username) {
        if (getDatabaseEncryptionKey() != null && !getDatabaseEncryptionKey().isBlank()) {
            return authenticatedUserRepository.existsByUsernameHash(username);
        }
        return authenticatedUserRepository.findByUsername(username).isPresent();
    }

    public AuthenticatedUser save(AuthenticatedUser authenticatedUser) {
        final AuthenticatedUser saved = authenticatedUserRepository.save(authenticatedUser);
        final Integer tenantId = getEffectiveTenantId();
        if (userTenantRepository != null
                && !userTenantRepository.existsByAuthenticatedUserIdAndTenantId(saved.getId(), tenantId)) {
            userTenantRepository.save(new UserTenant(saved.getId(), tenantId));
        }
        saved.setTenantId(tenantId);
        return saved;
    }

    private Integer getEffectiveTenantId() {
        return TenantContext.getTenantId() != null ? TenantContext.getRequiredTenantId() : TenantContext.LEGACY_TENANT_ID;
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
                .filter(user -> userTenantRepository.existsByAuthenticatedUserIdAndTenantId(user.getId(), TenantContext.getRequiredTenantId()))
                .peek(user -> user.setTenantId(TenantContext.getRequiredTenantId())).toList();
    }

    public void delete(AuthenticatedUser authenticatedUser) {
        authenticatedUserRepository.delete(authenticatedUser);
    }

    public void deleteAll() {
        authenticatedUserRepository.deleteAll();
    }

}
