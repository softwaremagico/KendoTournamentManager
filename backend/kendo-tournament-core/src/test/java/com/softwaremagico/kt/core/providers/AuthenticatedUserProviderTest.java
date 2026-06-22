package com.softwaremagico.kt.core.providers;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2026 SoftwareMagico
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
import com.softwaremagico.kt.persistence.encryption.KeyProperty;
import com.softwaremagico.kt.persistence.entities.AuthenticatedUser;
import com.softwaremagico.kt.persistence.entities.IAuthenticatedUser;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.repositories.AuthenticatedUserRepository;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Test(groups = "converterTests")
public class AuthenticatedUserProviderTest {

    private AuthenticatedUserRepository authenticatedUserRepository;
    private ParticipantProvider participantProvider;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        authenticatedUserRepository = mock(AuthenticatedUserRepository.class);
        participantProvider = mock(ParticipantProvider.class);
        new KeyProperty(null, null, null);
    }

    @Test
    public void shouldReturnGuestUserWhenEnabled() {
        final AuthenticatedUserProvider provider = new AuthenticatedUserProvider(authenticatedUserRepository, participantProvider, "true");

        final Optional<IAuthenticatedUser> user = provider.findByUsername(AuthenticatedUserProvider.GUEST_USER);

        assertTrue(user.isPresent());
        assertEquals(user.get().getUsername(), AuthenticatedUserProvider.GUEST_USER);
        assertTrue(user.get().getRoles().contains(AuthenticatedUserProvider.GUEST_ROLE));
    }

    @Test
    public void shouldSearchByUsernameHashWhenEncryptionEnabled() {
        new KeyProperty("enc-key", null, null);
        final AuthenticatedUser auth = new AuthenticatedUser("john");
        auth.setUsernameHash("legacy");

        when(authenticatedUserRepository.findByUsernameHash("john")).thenReturn(Optional.of(auth));

        final AuthenticatedUserProvider provider = new AuthenticatedUserProvider(authenticatedUserRepository, participantProvider, "false");
        final Optional<IAuthenticatedUser> found = provider.findByUsername("john");

        assertTrue(found.isPresent());
        assertEquals(found.get().getUsername(), "john");
        assertEquals(((AuthenticatedUser) found.get()).getUsernameHash(), "john");
    }

    @Test
    public void shouldSearchByPlainUsernameWhenEncryptionDisabled() {
        final AuthenticatedUser auth = new AuthenticatedUser("john");
        when(authenticatedUserRepository.findByUsername("john")).thenReturn(Optional.of(auth));

        final AuthenticatedUserProvider provider = new AuthenticatedUserProvider(authenticatedUserRepository, participantProvider, "false");
        final Optional<IAuthenticatedUser> found = provider.findByUsername("john");

        assertTrue(found.isPresent());
        assertEquals(found.get().getUsername(), "john");
    }

    @Test
    public void shouldFallbackToParticipantWhenNoAuthenticatedUserFound() {
        final Participant participant = new Participant();
        participant.setName("P");
        participant.setLastname("L");

        when(authenticatedUserRepository.findByUsername("token-user")).thenReturn(Optional.empty());
        when(participantProvider.findByTokenUsername("token-user")).thenReturn(Optional.of(participant));

        final AuthenticatedUserProvider provider = new AuthenticatedUserProvider(authenticatedUserRepository, participantProvider, "false");
        final Optional<IAuthenticatedUser> found = provider.findByUsername("token-user");

        assertTrue(found.isPresent());
        assertTrue(found.get() instanceof Participant);
    }

    @Test(expectedExceptions = DuplicatedUserException.class)
    public void shouldRejectDuplicateUserOnSave() {
        final AuthenticatedUser existing = new AuthenticatedUser("john");
        when(authenticatedUserRepository.findByUsername("john")).thenReturn(Optional.of(existing));

        final AuthenticatedUserProvider provider = new AuthenticatedUserProvider(authenticatedUserRepository, participantProvider, "false");
        provider.save("creator", "john", "John", "Doe", "pwd", "admin");
    }

    @Test
    public void shouldCreateAndSaveUserWhenUnique() {
        when(authenticatedUserRepository.findByUsername("john")).thenReturn(Optional.empty());
        when(participantProvider.findByTokenUsername("john")).thenReturn(Optional.empty());
        when(authenticatedUserRepository.save(any(AuthenticatedUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final AuthenticatedUserProvider provider = new AuthenticatedUserProvider(authenticatedUserRepository, participantProvider, "false");
        final AuthenticatedUser saved = provider.save("creator", "john", "John", "Doe", "pwd", "admin", "editor");

        assertEquals(saved.getUsername(), "john");
        assertEquals(saved.getName(), "John");
        assertEquals(saved.getLastname(), "Doe");
        assertEquals(saved.getPassword(), "pwd");
        assertNotNull(saved.getRoles());
        assertTrue(saved.getRoles().contains("admin"));
        assertTrue(saved.getRoles().contains("editor"));
    }

    @Test
    public void shouldExposeRepositoryDelegatedMethods() {
        when(authenticatedUserRepository.count()).thenReturn(3L);
        when(authenticatedUserRepository.findAll()).thenReturn(List.of(new AuthenticatedUser("a")));

        final AuthenticatedUserProvider provider = new AuthenticatedUserProvider(authenticatedUserRepository, participantProvider, "false");

        assertEquals(provider.count(), 3L);
        assertEquals(provider.findAll().size(), 1);

        final AuthenticatedUser user = new AuthenticatedUser("john");
        provider.updateRoles(user, Set.of("admin"));
        provider.delete(user);
        provider.deleteAll();

        verify(authenticatedUserRepository).save(user);
        verify(authenticatedUserRepository).delete(user);
        verify(authenticatedUserRepository).deleteAll();
    }

    @Test
    public void shouldResolveUniqueIdAsUsername() {
        final AuthenticatedUser auth = new AuthenticatedUser("uid");
        when(authenticatedUserRepository.findByUsername("uid")).thenReturn(Optional.of(auth));

        final AuthenticatedUserProvider provider = new AuthenticatedUserProvider(authenticatedUserRepository, participantProvider, "false");
        final Optional<IAuthenticatedUser> found = provider.findByUniqueId("uid");

        assertTrue(found.isPresent());
        assertFalse(found.get().getUsername().isBlank());
    }
}

