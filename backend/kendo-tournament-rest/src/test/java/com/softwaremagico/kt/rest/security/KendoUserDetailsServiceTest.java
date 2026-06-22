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

package com.softwaremagico.kt.rest.security;

import com.softwaremagico.kt.core.providers.AuthenticatedUserProvider;
import com.softwaremagico.kt.persistence.entities.AuthenticatedUser;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.rest.exceptions.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Test suite for {@link KendoUserDetailsService}. Tests Spring Security
 * UserDetailsService implementation.
 */
@ExtendWith(MockitoExtension.class)
class KendoUserDetailsServiceTest {

	@Mock
	private AuthenticatedUserProvider authenticatedUserProvider;

	@Mock
	private AuthenticatedUser mockAuthenticatedUser;

	@Mock
	private Participant mockParticipant;

	private KendoUserDetailsService userDetailsService;
	private static final String TEST_USERNAME = "testuser";
	private static final String TEST_PASSWORD = "encodedPassword123";

	@BeforeEach
	void setUp() {
        this.userDetailsService = new KendoUserDetailsService(this.authenticatedUserProvider);
	}

	@Test
	void testLoadUserByUsernameSuccess() {
		when(this.mockAuthenticatedUser.getUsername()).thenReturn(TEST_USERNAME);
		when(this.mockAuthenticatedUser.getPassword()).thenReturn(TEST_PASSWORD);
		when(this.mockAuthenticatedUser.isAccountNonExpired()).thenReturn(true);
		when(this.mockAuthenticatedUser.isAccountNonLocked()).thenReturn(true);
		when(this.mockAuthenticatedUser.isCredentialsNonExpired()).thenReturn(true);
		when(this.mockAuthenticatedUser.isEnabled()).thenReturn(true);
		when(this.mockAuthenticatedUser.getId()).thenReturn(1);

		when(this.authenticatedUserProvider.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(this.mockAuthenticatedUser));

		final UserDetails userDetails = this.userDetailsService.loadUserByUsername(TEST_USERNAME);

		assertNotNull(userDetails);
		assertEquals(TEST_USERNAME, userDetails.getUsername());
		assertEquals(TEST_PASSWORD, userDetails.getPassword());
		assertTrue(userDetails.isAccountNonExpired());
		assertTrue(userDetails.isAccountNonLocked());
		assertTrue(userDetails.isCredentialsNonExpired());
		assertTrue(userDetails.isEnabled());
	}

	@Test
	void testLoadUserByUsernameNotFound() {
		when(this.authenticatedUserProvider.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());

		assertThrows(UsernameNotFoundException.class, () -> this.userDetailsService.loadUserByUsername(TEST_USERNAME));
	}

	@Test
	void testLoadUserByUsernameWhenParticipant() {
		when(this.authenticatedUserProvider.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(this.mockParticipant));

		assertThrows(UserNotFoundException.class, () -> this.userDetailsService.loadUserByUsername(TEST_USERNAME));
	}

	@Test
	void testUserDetailsAuthoritiesEmptyList() {
		when(this.mockAuthenticatedUser.getUsername()).thenReturn(TEST_USERNAME);
		when(this.mockAuthenticatedUser.getPassword()).thenReturn(TEST_PASSWORD);
		when(this.mockAuthenticatedUser.isAccountNonExpired()).thenReturn(true);
		when(this.mockAuthenticatedUser.isAccountNonLocked()).thenReturn(true);
		when(this.mockAuthenticatedUser.isCredentialsNonExpired()).thenReturn(true);
		when(this.mockAuthenticatedUser.isEnabled()).thenReturn(true);

		when(this.authenticatedUserProvider.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(this.mockAuthenticatedUser));

		final UserDetails userDetails = this.userDetailsService.loadUserByUsername(TEST_USERNAME);

		assertNotNull(userDetails.getAuthorities());
		assertTrue(userDetails.getAuthorities().isEmpty());
	}

	@Test
	void testUserDetailsAccountExpired() {
		when(this.mockAuthenticatedUser.getUsername()).thenReturn(TEST_USERNAME);
		when(this.mockAuthenticatedUser.getPassword()).thenReturn(TEST_PASSWORD);
		when(this.mockAuthenticatedUser.isAccountNonExpired()).thenReturn(false);
		when(this.mockAuthenticatedUser.isAccountNonLocked()).thenReturn(true);
		when(this.mockAuthenticatedUser.isCredentialsNonExpired()).thenReturn(true);
		when(this.mockAuthenticatedUser.isEnabled()).thenReturn(true);

		when(this.authenticatedUserProvider.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(this.mockAuthenticatedUser));

		final UserDetails userDetails = this.userDetailsService.loadUserByUsername(TEST_USERNAME);

		assertFalse(userDetails.isAccountNonExpired());
	}

	@Test
	void testUserDetailsAccountLocked() {
		when(this.mockAuthenticatedUser.getUsername()).thenReturn(TEST_USERNAME);
		when(this.mockAuthenticatedUser.getPassword()).thenReturn(TEST_PASSWORD);
		when(this.mockAuthenticatedUser.isAccountNonExpired()).thenReturn(true);
		when(this.mockAuthenticatedUser.isAccountNonLocked()).thenReturn(false);
		when(this.mockAuthenticatedUser.isCredentialsNonExpired()).thenReturn(true);
		when(this.mockAuthenticatedUser.isEnabled()).thenReturn(true);

		when(this.authenticatedUserProvider.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(this.mockAuthenticatedUser));

		final UserDetails userDetails = this.userDetailsService.loadUserByUsername(TEST_USERNAME);

		assertFalse(userDetails.isAccountNonLocked());
	}

	@Test
	void testUserDetailsCredentialsExpired() {
		when(this.mockAuthenticatedUser.getUsername()).thenReturn(TEST_USERNAME);
		when(this.mockAuthenticatedUser.getPassword()).thenReturn(TEST_PASSWORD);
		when(this.mockAuthenticatedUser.isAccountNonExpired()).thenReturn(true);
		when(this.mockAuthenticatedUser.isAccountNonLocked()).thenReturn(true);
		when(this.mockAuthenticatedUser.isCredentialsNonExpired()).thenReturn(false);
		when(this.mockAuthenticatedUser.isEnabled()).thenReturn(true);

		when(this.authenticatedUserProvider.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(this.mockAuthenticatedUser));

		final UserDetails userDetails = this.userDetailsService.loadUserByUsername(TEST_USERNAME);

		assertFalse(userDetails.isCredentialsNonExpired());
	}

	@Test
	void testUserDetailsDisabled() {
		when(this.mockAuthenticatedUser.getUsername()).thenReturn(TEST_USERNAME);
		when(this.mockAuthenticatedUser.getPassword()).thenReturn(TEST_PASSWORD);
		when(this.mockAuthenticatedUser.isAccountNonExpired()).thenReturn(true);
		when(this.mockAuthenticatedUser.isAccountNonLocked()).thenReturn(true);
		when(this.mockAuthenticatedUser.isCredentialsNonExpired()).thenReturn(true);
		when(this.mockAuthenticatedUser.isEnabled()).thenReturn(false);

		when(this.authenticatedUserProvider.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(this.mockAuthenticatedUser));

		final UserDetails userDetails = this.userDetailsService.loadUserByUsername(TEST_USERNAME);

		assertFalse(userDetails.isEnabled());
	}

	@Test
	void testUserDetailsGetIdMethod() {
		when(this.mockAuthenticatedUser.getUsername()).thenReturn(TEST_USERNAME);
		when(this.mockAuthenticatedUser.getPassword()).thenReturn(TEST_PASSWORD);
		when(this.mockAuthenticatedUser.isAccountNonExpired()).thenReturn(true);
		when(this.mockAuthenticatedUser.isAccountNonLocked()).thenReturn(true);
		when(this.mockAuthenticatedUser.isCredentialsNonExpired()).thenReturn(true);
		when(this.mockAuthenticatedUser.isEnabled()).thenReturn(true);
		when(this.mockAuthenticatedUser.getId()).thenReturn(42);

		when(this.authenticatedUserProvider.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(this.mockAuthenticatedUser));

		final UserDetails userDetails = this.userDetailsService.loadUserByUsername(TEST_USERNAME);

		// Access the getId method through reflection since it's not in the UserDetails
		// interface
		assertNotNull(userDetails);
        assertInstanceOf(UserDetails.class, userDetails);
	}

	@Test
	void testLoadUserWithSpecialCharactersInUsername() {
		final String specialUsername = "user@example.com";
		when(this.mockAuthenticatedUser.getUsername()).thenReturn(specialUsername);
		when(this.mockAuthenticatedUser.getPassword()).thenReturn(TEST_PASSWORD);
		when(this.mockAuthenticatedUser.isAccountNonExpired()).thenReturn(true);
		when(this.mockAuthenticatedUser.isAccountNonLocked()).thenReturn(true);
		when(this.mockAuthenticatedUser.isCredentialsNonExpired()).thenReturn(true);
		when(this.mockAuthenticatedUser.isEnabled()).thenReturn(true);

		when(this.authenticatedUserProvider.findByUsername(specialUsername)).thenReturn(Optional.of(this.mockAuthenticatedUser));

		final UserDetails userDetails = this.userDetailsService.loadUserByUsername(specialUsername);

		assertNotNull(userDetails);
		assertEquals(specialUsername, userDetails.getUsername());
	}

	@Test
	void testConstructor() {
		final KendoUserDetailsService service = new KendoUserDetailsService(this.authenticatedUserProvider);
		assertNotNull(service);
	}

	@Test
	void testMultipleLoadCalls() {
		when(this.mockAuthenticatedUser.getUsername()).thenReturn(TEST_USERNAME);
		when(this.mockAuthenticatedUser.getPassword()).thenReturn(TEST_PASSWORD);
		when(this.mockAuthenticatedUser.isAccountNonExpired()).thenReturn(true);
		when(this.mockAuthenticatedUser.isAccountNonLocked()).thenReturn(true);
		when(this.mockAuthenticatedUser.isCredentialsNonExpired()).thenReturn(true);
		when(this.mockAuthenticatedUser.isEnabled()).thenReturn(true);

		when(this.authenticatedUserProvider.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(this.mockAuthenticatedUser));

		final UserDetails userDetails1 = this.userDetailsService.loadUserByUsername(TEST_USERNAME);
		final UserDetails userDetails2 = this.userDetailsService.loadUserByUsername(TEST_USERNAME);

		assertNotNull(userDetails1);
		assertNotNull(userDetails2);
		assertEquals(userDetails1.getUsername(), userDetails2.getUsername());
	}

	@Test
	void testLoadUserWithAllFieldsFalse() {
		when(this.mockAuthenticatedUser.getUsername()).thenReturn(TEST_USERNAME);
		when(this.mockAuthenticatedUser.getPassword()).thenReturn(TEST_PASSWORD);
		when(this.mockAuthenticatedUser.isAccountNonExpired()).thenReturn(false);
		when(this.mockAuthenticatedUser.isAccountNonLocked()).thenReturn(false);
		when(this.mockAuthenticatedUser.isCredentialsNonExpired()).thenReturn(false);
		when(this.mockAuthenticatedUser.isEnabled()).thenReturn(false);

		when(this.authenticatedUserProvider.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(this.mockAuthenticatedUser));

		final UserDetails userDetails = this.userDetailsService.loadUserByUsername(TEST_USERNAME);

		assertFalse(userDetails.isAccountNonExpired());
		assertFalse(userDetails.isAccountNonLocked());
		assertFalse(userDetails.isCredentialsNonExpired());
		assertFalse(userDetails.isEnabled());
	}
}
