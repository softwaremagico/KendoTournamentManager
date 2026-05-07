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
        Assert.assertEquals(AvailableRole.get("viewer"), AvailableRole.VIEWER);
    }

    @Test
    public void testGetWithValidRoleEditor() {
        Assert.assertEquals(AvailableRole.get("editor"), AvailableRole.EDITOR);
    }

    @Test
    public void testGetWithValidRoleAdmin() {
        Assert.assertEquals(AvailableRole.get("admin"), AvailableRole.ADMIN);
    }

    @Test
    public void testGetWithValidRoleParticipant() {
        Assert.assertEquals(AvailableRole.get("participant"), AvailableRole.PARTICIPANT);
    }

    @Test
    public void testGetWithValidRoleGuest() {
        Assert.assertEquals(AvailableRole.get("guest"), AvailableRole.GUEST);
    }

    @Test
    public void testGetWithUppercaseRole() {
        Assert.assertEquals(AvailableRole.get("VIEWER"), AvailableRole.VIEWER);
    }

    @Test
    public void testGetWithMixedCaseRole() {
        Assert.assertEquals(AvailableRole.get("EdItOr"), AvailableRole.EDITOR);
    }

    @Test
    public void testGetWithInvalidRole() {
        Assert.assertNull(AvailableRole.get("invalid"));
    }

    @Test
    public void testGetWithNullRole() {
        Assert.assertNull(AvailableRole.get(null));
    }

    @Test
    public void testGetWithEmptyRole() {
        Assert.assertNull(AvailableRole.get(""));
    }

    @Test
    public void testAllRolesCanBeRetrieved() {
        for (AvailableRole role : AvailableRole.values()) {
            Assert.assertEquals(AvailableRole.get(role.name()), role);
        }
    }

    @Test
    public void testGetReturnsDifferentRoles() {
        Assert.assertNotEquals(AvailableRole.get("viewer"), AvailableRole.get("admin"));
        Assert.assertNotEquals(AvailableRole.get("editor"), AvailableRole.get("participant"));
    }
}

