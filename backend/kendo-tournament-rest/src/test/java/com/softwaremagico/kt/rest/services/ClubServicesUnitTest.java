package com.softwaremagico.kt.rest.services;

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

import com.softwaremagico.kt.core.controller.ClubController;
import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.rest.exceptions.BadRequestException;
import com.softwaremagico.kt.rest.security.KendoSecurityService;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.expectThrows;

@Test(groups = "restServicesUnit")
public class ClubServicesUnitTest {

    @Mock
    private ClubController clubController;

    @Mock
    private KendoSecurityService kendoSecurityService;

    @Mock
    private Authentication authentication;

    private ClubServices clubServices;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(authentication.getName()).thenReturn("tester");
        when(kendoSecurityService.getViewerPrivilege()).thenReturn("VIEWER");
        when(kendoSecurityService.getEditorPrivilege()).thenReturn("EDITOR");
        when(kendoSecurityService.getAdminPrivilege()).thenReturn("ADMIN");
        clubServices = new ClubServices(clubController, kendoSecurityService);
    }

    @Test
    public void shouldExposeRequiredRolesForEntityById() {
        final String[] roles = clubServices.requiredRoleForEntityById();
        assertEquals(roles.length, 3);
        assertEquals(roles[0], "VIEWER");
        assertEquals(roles[1], "EDITOR");
        assertEquals(roles[2], "ADMIN");
    }

    @Test
    public void shouldDelegateCrudOperationsToController() {
        final ClubDTO club = club("One");
        final ClubDTO club2 = club("Two");

        when(clubController.get()).thenReturn(List.of(club, club2));
        when(clubController.get(anyCollection())).thenReturn(List.of(club2));
        when(clubController.count()).thenReturn(2L);
        when(clubController.get(1)).thenReturn(club);
        when(clubController.create(any(ClubDTO.class), eq("tester"), eq("s1"))).thenReturn(club);
        when(clubController.create(any(Collection.class), eq("tester"), eq("s1"))).thenReturn(List.of(club, club2));
        when(clubController.update(any(ClubDTO.class), eq("tester"), eq("s1"))).thenReturn(club2);
        when(clubController.updateAll(any(List.class), eq("tester"), eq("s1"))).thenReturn(List.of(club2));
        doNothing().when(clubController).deleteById(anyInt(), eq("tester"), eq("s1"));
        doNothing().when(clubController).delete(any(ClubDTO.class), eq("tester"), eq("s1"));
        doNothing().when(clubController).delete(any(Collection.class), eq("tester"), eq("s1"));

        assertEquals(clubServices.getAll(null).size(), 2);
        assertEquals(clubServices.getAll(List.of(2), null).size(), 1);
        assertEquals(clubServices.count(null), 2L);
        assertSame(clubServices.get(1, null), club);
        assertSame(clubServices.add(club, authentication, "s1", null), club);
        assertEquals(clubServices.add(List.of(club, club2), authentication, "s1", null).size(), 2);
        assertSame(clubServices.update(club2, authentication, "s1", null), club2);
        assertEquals(clubServices.update(List.of(club2), authentication, "s1", null).size(), 1);

        clubServices.delete(1, authentication, "s1", null);
        clubServices.delete(club, "s1", authentication, null);
        clubServices.delete(List.of(club, club2), authentication, "s1", null);

        verify(clubController).deleteById(1, "tester", "s1");
        verify(clubController).delete(club, "tester", "s1");
    }

    @Test
    public void shouldRejectMissingCollectionsOnAddAndUpdate() {
        expectThrows(BadRequestException.class, () -> clubServices.add((Collection<ClubDTO>) null, authentication, "s", null));
        expectThrows(BadRequestException.class, () -> clubServices.add(List.of(), authentication, "s", null));
        expectThrows(BadRequestException.class, () -> clubServices.update((List<ClubDTO>) null, authentication, "s", null));
    }

    @Test
    public void shouldCreateClubFromBasicAndNewEndpoints() {
        final ClubDTO created = club("Created");
        when(clubController.create("Name", "ES", "Madrid", "tester", "s2")).thenReturn(created);
        when(clubController.create(any(ClubDTO.class), eq("tester"), eq("s2"))).thenReturn(created);

        assertSame(clubServices.add("Name", "ES", "Madrid", authentication, "s2", null), created);
        assertSame(clubServices.addNew(club("new"), authentication, "s2", null), created);
    }

    private ClubDTO club(String name) {
        final ClubDTO clubDTO = new ClubDTO();
        clubDTO.setName(name);
        clubDTO.setCountry("ES");
        clubDTO.setCity("Madrid");
        return clubDTO;
    }
}

