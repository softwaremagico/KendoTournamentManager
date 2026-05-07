package com.softwaremagico.kt.security;

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

import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = "availableRole")
public class AvailableRoleTests {

    @Test
    public void testGetWithValidRoleViewer() {
        final AvailableRole role = AvailableRole.get("viewer");
        Assert.assertNotNull(role);
        Assert.assertEquals(role, AvailableRole.VIEWER);
        Assert.assertNotEquals(role, AvailableRole.EDITOR);
    }

    @Test
    public void testGetWithValidRoleEditor() {
        final AvailableRole role = AvailableRole.get("editor");
        Assert.assertNotNull(role);
        Assert.assertEquals(role, AvailableRole.EDITOR);
        Assert.assertNotEquals(role, AvailableRole.VIEWER);
    }

    @Test
    public void testGetWithValidRoleAdmin() {
        final AvailableRole role = AvailableRole.get("admin");
        Assert.assertNotNull(role);
        Assert.assertEquals(role, AvailableRole.ADMIN);
        Assert.assertFalse(role.equals(AvailableRole.VIEWER));
    }

    @Test
    public void testGetWithValidRoleParticipant() {
        final AvailableRole role = AvailableRole.get("participant");
        Assert.assertNotNull(role);
        Assert.assertEquals(role, AvailableRole.PARTICIPANT);
    }

    @Test
    public void testGetWithValidRoleGuest() {
        final AvailableRole role = AvailableRole.get("guest");
        Assert.assertNotNull(role);
        Assert.assertEquals(role, AvailableRole.GUEST);
    }

    @Test
    public void testGetWithUppercaseRole() {
        final AvailableRole role = AvailableRole.get("VIEWER");
        Assert.assertNotNull(role);
        Assert.assertEquals(role, AvailableRole.VIEWER);
        Assert.assertTrue(role == AvailableRole.VIEWER);
    }

    @Test
    public void testGetWithMixedCaseRole() {
        final AvailableRole role = AvailableRole.get("EdItOr");
        Assert.assertNotNull(role);
        Assert.assertEquals(role, AvailableRole.EDITOR);
        Assert.assertFalse(role.equals(AvailableRole.VIEWER));
    }

    @Test
    public void testGetWithInvalidRole() {
        final AvailableRole role = AvailableRole.get("invalid");
        Assert.assertNull(role);
        Assert.assertNotEquals(role, AvailableRole.VIEWER);
    }

    @Test
    public void testGetWithNullRole() {
        final AvailableRole role = AvailableRole.get(null);
        Assert.assertNull(role);
    }

    @Test
    public void testGetWithEmptyRole() {
        final AvailableRole role = AvailableRole.get("");
        Assert.assertNull(role);
    }

    @Test
    public void testAllRolesCanBeRetrieved() {
        for (AvailableRole role : AvailableRole.values()) {
            final AvailableRole retrieved = AvailableRole.get(role.name());
            Assert.assertNotNull(retrieved);
            Assert.assertEquals(retrieved, role);
            Assert.assertTrue(retrieved == role);
        }
    }

    @Test
    public void testGetReturnsDifferentRoles() {
        final AvailableRole role1 = AvailableRole.get("viewer");
        final AvailableRole role2 = AvailableRole.get("admin");
        Assert.assertNotNull(role1);
        Assert.assertNotNull(role2);
        Assert.assertNotEquals(role1, role2);
        Assert.assertFalse(role1.equals(role2));
    }
}

