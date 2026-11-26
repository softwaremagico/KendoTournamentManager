package com.softwaremagico.kt.rest.security;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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

import com.softwaremagico.kt.core.providers.AuthenticatedUserProvider;
import com.softwaremagico.kt.persistence.entities.AuthenticatedUser;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.rest.exceptions.UserNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Test(groups = "kendoUserDetailsServiceTests")
public class KendoUserDetailsServiceTests {

    private AuthenticatedUserProvider authenticatedUserProvider;
    private KendoUserDetailsService kendoUserDetailsService;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        this.authenticatedUserProvider = mock(AuthenticatedUserProvider.class);
        this.kendoUserDetailsService = new KendoUserDetailsService(this.authenticatedUserProvider);
    }

    @Test
    public void shouldBuildUserDetailsForAuthenticatedUser() {
        final AuthenticatedUser authenticatedUser = new AuthenticatedUser("admin");
        authenticatedUser.setPassword("encoded-password");

        when(this.authenticatedUserProvider.findByUsername("admin")).thenReturn(Optional.of(authenticatedUser));

        final UserDetails details = this.kendoUserDetailsService.loadUserByUsername("admin");

        assertEquals(details.getUsername(), "admin");
        assertEquals(details.getPassword(), "encoded-password");
        assertTrue(details.isAccountNonExpired());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.isCredentialsNonExpired());
        assertTrue(details.isEnabled());
        assertTrue(details.getAuthorities().isEmpty());
    }

    @Test(expectedExceptions = UsernameNotFoundException.class)
    public void shouldThrowWhenUserDoesNotExist() {
        when(this.authenticatedUserProvider.findByUsername("missing")).thenReturn(Optional.empty());
        this.kendoUserDetailsService.loadUserByUsername("missing");
    }

    @Test(expectedExceptions = UserNotFoundException.class)
    public void shouldThrowWhenFoundUserIsParticipant() {
        final Participant participant = new Participant();
        participant.setName("p-name");
        participant.setLastname("p-lastname");

        when(this.authenticatedUserProvider.findByUsername("participant")).thenReturn(Optional.of(participant));
        this.kendoUserDetailsService.loadUserByUsername("participant");
    }

    @Test
    public void shouldProvideIndependentUserDetailsInstances() {
        final AuthenticatedUser authenticatedUser = new AuthenticatedUser("admin");
        authenticatedUser.setPassword("encoded-password");

        when(this.authenticatedUserProvider.findByUsername("admin")).thenReturn(Optional.of(authenticatedUser));

        final UserDetails first = this.kendoUserDetailsService.loadUserByUsername("admin");
        final UserDetails second = this.kendoUserDetailsService.loadUserByUsername("admin");

        org.testng.Assert.assertNotSame(first, second);
        assertEquals(first.getUsername(), second.getUsername());
        assertEquals(first.getPassword(), second.getPassword());
    }

    @Test
    public void shouldReturnEmptyAuthoritiesList() {
        final AuthenticatedUser authenticatedUser = new AuthenticatedUser("admin");
        authenticatedUser.setPassword("encoded-password");

        when(this.authenticatedUserProvider.findByUsername("admin")).thenReturn(Optional.of(authenticatedUser));

        final UserDetails details = this.kendoUserDetailsService.loadUserByUsername("admin");

        assertNotNull(details.getAuthorities());
        assertTrue(details.getAuthorities().isEmpty());
    }

    @Test
    public void shouldReflectAccountExpiredFlag() {
        final AuthenticatedUser mockAuthenticatedUser = mock(AuthenticatedUser.class);
        when(mockAuthenticatedUser.getUsername()).thenReturn("admin");
        when(mockAuthenticatedUser.getPassword()).thenReturn("encoded-password");
        when(mockAuthenticatedUser.isAccountNonExpired()).thenReturn(false);
        when(mockAuthenticatedUser.isAccountNonLocked()).thenReturn(true);
        when(mockAuthenticatedUser.isCredentialsNonExpired()).thenReturn(true);
        when(mockAuthenticatedUser.isEnabled()).thenReturn(true);

        when(this.authenticatedUserProvider.findByUsername("admin")).thenReturn(Optional.of(mockAuthenticatedUser));

        final UserDetails details = this.kendoUserDetailsService.loadUserByUsername("admin");

        assertFalse(details.isAccountNonExpired());
    }

    @Test
    public void shouldReflectAccountLockedFlag() {
        final AuthenticatedUser mockAuthenticatedUser = mock(AuthenticatedUser.class);
        when(mockAuthenticatedUser.getUsername()).thenReturn("admin");
        when(mockAuthenticatedUser.getPassword()).thenReturn("encoded-password");
        when(mockAuthenticatedUser.isAccountNonExpired()).thenReturn(true);
        when(mockAuthenticatedUser.isAccountNonLocked()).thenReturn(false);
        when(mockAuthenticatedUser.isCredentialsNonExpired()).thenReturn(true);
        when(mockAuthenticatedUser.isEnabled()).thenReturn(true);

        when(this.authenticatedUserProvider.findByUsername("admin")).thenReturn(Optional.of(mockAuthenticatedUser));

        final UserDetails details = this.kendoUserDetailsService.loadUserByUsername("admin");

        assertFalse(details.isAccountNonLocked());
    }

    @Test
    public void shouldReflectCredentialsExpiredFlag() {
        final AuthenticatedUser mockAuthenticatedUser = mock(AuthenticatedUser.class);
        when(mockAuthenticatedUser.getUsername()).thenReturn("admin");
        when(mockAuthenticatedUser.getPassword()).thenReturn("encoded-password");
        when(mockAuthenticatedUser.isAccountNonExpired()).thenReturn(true);
        when(mockAuthenticatedUser.isAccountNonLocked()).thenReturn(true);
        when(mockAuthenticatedUser.isCredentialsNonExpired()).thenReturn(false);
        when(mockAuthenticatedUser.isEnabled()).thenReturn(true);

        when(this.authenticatedUserProvider.findByUsername("admin")).thenReturn(Optional.of(mockAuthenticatedUser));

        final UserDetails details = this.kendoUserDetailsService.loadUserByUsername("admin");

        assertFalse(details.isCredentialsNonExpired());
    }

    @Test
    public void shouldReflectDisabledFlag() {
        final AuthenticatedUser mockAuthenticatedUser = mock(AuthenticatedUser.class);
        when(mockAuthenticatedUser.getUsername()).thenReturn("admin");
        when(mockAuthenticatedUser.getPassword()).thenReturn("encoded-password");
        when(mockAuthenticatedUser.isAccountNonExpired()).thenReturn(true);
        when(mockAuthenticatedUser.isAccountNonLocked()).thenReturn(true);
        when(mockAuthenticatedUser.isCredentialsNonExpired()).thenReturn(true);
        when(mockAuthenticatedUser.isEnabled()).thenReturn(false);

        when(this.authenticatedUserProvider.findByUsername("admin")).thenReturn(Optional.of(mockAuthenticatedUser));

        final UserDetails details = this.kendoUserDetailsService.loadUserByUsername("admin");

        assertFalse(details.isEnabled());
    }

    @Test
    public void shouldLoadUserWithSpecialCharactersInUsername() {
        final String specialUsername = "user@example.com";
        final AuthenticatedUser authenticatedUser = new AuthenticatedUser(specialUsername);
        authenticatedUser.setPassword("encoded-password");

        when(this.authenticatedUserProvider.findByUsername(specialUsername)).thenReturn(Optional.of(authenticatedUser));

        final UserDetails details = this.kendoUserDetailsService.loadUserByUsername(specialUsername);

        assertNotNull(details);
        assertEquals(details.getUsername(), specialUsername);
    }

    @Test
    public void shouldBuildServiceThroughConstructor() {
        final KendoUserDetailsService service = new KendoUserDetailsService(this.authenticatedUserProvider);
        assertNotNull(service);
    }
}


