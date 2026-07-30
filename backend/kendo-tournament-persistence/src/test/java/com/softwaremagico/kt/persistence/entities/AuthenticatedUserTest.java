package com.softwaremagico.kt.persistence.entities;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
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

import org.springframework.security.core.GrantedAuthority;
import org.testng.annotations.Test;

import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

@Test(groups = "authenticatedUserEntity")
public class AuthenticatedUserTest {

    @Test
    public void constructor_withUsername_expectUsernameAndHashSet() {
        final AuthenticatedUser user = new AuthenticatedUser("john");
        assertEquals(user.getUsername(), "john");
        assertEquals(user.getUsernameHash(), "john");
    }

    @Test
    public void accountFlags_expectAllTrue() {
        final AuthenticatedUser user = new AuthenticatedUser();
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());
    }

    @Test
    public void getMobilePhone_expectAlwaysNull() {
        final AuthenticatedUser user = new AuthenticatedUser();
        assertNull(user.getMobilePhone());
    }

    @Test
    public void getAuthorities_withValidRoles_expectMappedAuthorities() {
        final AuthenticatedUser user = new AuthenticatedUser("john");
        user.setRoles(Set.of("ADMIN"));

        final Set<? extends GrantedAuthority> authorities = (Set<? extends GrantedAuthority>) user.getAuthorities();

        assertEquals(authorities.size(), 1);
        assertEquals(authorities.iterator().next().getAuthority(), "ADMIN");
    }

    @Test
    public void getAuthorities_withUnknownRole_expectEmptyAuthorities() {
        final AuthenticatedUser user = new AuthenticatedUser("john");
        user.setRoles(Set.of("NOT_A_ROLE"));

        assertTrue(user.getAuthorities().isEmpty());
    }

    @Test
    public void getAuthorities_withNullRoles_expectEmptyAuthorities() {
        final AuthenticatedUser user = new AuthenticatedUser("john");
        assertTrue(user.getAuthorities().isEmpty());
    }

    @Test
    public void getAuthorities_calledTwice_expectCachedResult() {
        final AuthenticatedUser user = new AuthenticatedUser("john");
        user.setRoles(Set.of("VIEWER"));

        final Object first = user.getAuthorities();
        final Object second = user.getAuthorities();

        assertEquals(first, second);
    }

    @Test
    public void toString_expectContainsUsernameAndRoles() {
        final AuthenticatedUser user = new AuthenticatedUser("john");
        user.setName("John");
        user.setLastname("Doe");
        user.setRoles(Set.of("ADMIN"));

        final String text = user.toString();

        assertTrue(text.contains("john"));
        assertTrue(text.contains("John"));
        assertTrue(text.contains("Doe"));
    }

    @Test
    public void equals_withSameIdAndSameFields_expectTrue() {
        final AuthenticatedUser user1 = new AuthenticatedUser("john");
        user1.setId(1);
        final AuthenticatedUser user2 = new AuthenticatedUser("john");
        user2.setId(1);

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    public void equals_withSameIdButDifferentUsername_expectFalse() {
        final AuthenticatedUser user1 = new AuthenticatedUser("john");
        user1.setId(1);
        final AuthenticatedUser user2 = new AuthenticatedUser("jane");
        user2.setId(1);

        // Element#equals() only compares the primary key, but AuthenticatedUser overrides equals()
        // to also require the mutable fields to match, since two persisted rows sharing an id would
        // be a data corruption bug that should never be masked by equals().
        assertNotEquals(user1, user2);
    }

    @Test
    public void equals_withDifferentTransientFields_expectFalse() {
        final AuthenticatedUser user1 = new AuthenticatedUser("john");
        final AuthenticatedUser user2 = new AuthenticatedUser("jane");

        assertNotEquals(user1, user2);
    }

    @Test
    public void equals_withSameFieldsButNoId_expectFalseSinceBothTransient() {
        final AuthenticatedUser user1 = new AuthenticatedUser("john");
        final AuthenticatedUser user2 = new AuthenticatedUser("john");

        // Two distinct transient entities are never equal, even with identical field values,
        // because Element.equals requires a non-null shared id.
        assertNotEquals(user1, user2);
    }

    @Test
    public void equals_withNull_expectFalse() {
        final AuthenticatedUser user = new AuthenticatedUser("john");
        assertFalse(user.equals(null));
    }

    @Test
    public void equals_withDifferentClass_expectFalse() {
        final AuthenticatedUser user = new AuthenticatedUser("john");
        assertFalse(user.equals("not-a-user"));
    }

    @Test
    public void equals_withItself_expectTrue() {
        final AuthenticatedUser user = new AuthenticatedUser("john");
        assertEquals(user, user);
    }
}


