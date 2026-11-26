package com.softwaremagico.kt.rest.services;

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

import com.softwaremagico.kt.core.controller.DuelController;
import com.softwaremagico.kt.core.controller.models.DuelDTO;
import com.softwaremagico.kt.rest.security.KendoSecurityService;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

public class DuelServicesTest {

    @Mock
    private DuelController mockDuelController;

    @Mock
    private KendoSecurityService mockSecurityService;

    private DuelServices service;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new DuelServices(mockDuelController, mockSecurityService);
    }

    @Test(groups = "duelServices")
    public void getUntiesFromGroup_shouldReturnUntiedDuelsForGroup() {
        Integer groupId = 1;
        List<DuelDTO> expectedDuels = createDuelDTOs(2);

        when(mockDuelController.getUntiesFromGroup(groupId)).thenReturn(expectedDuels);

        List<DuelDTO> result = service.getUntiesFromGroup(groupId, null);

        assertNotNull(result);
        assertEquals(result.size(), 2);
        verify(mockDuelController).getUntiesFromGroup(groupId);
    }

    @Test(groups = "duelServices")
    public void getUntiesFromGroup_shouldReturnEmptyListWhenNoUnties() {
        Integer groupId = 1;

        when(mockDuelController.getUntiesFromGroup(groupId)).thenReturn(Collections.emptyList());

        List<DuelDTO> result = service.getUntiesFromGroup(groupId, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test(groups = "duelServices")
    public void getByCompetitor_shouldReturnAllDuelsForCompetitor() {
        Integer competitorId = 5;
        List<DuelDTO> expectedDuels = createDuelDTOs(3);

        when(mockDuelController.getBy(competitorId)).thenReturn(expectedDuels);

        List<DuelDTO> result = service.getByCompetitor(competitorId, null);

        assertNotNull(result);
        assertEquals(result.size(), 3);
        verify(mockDuelController).getBy(competitorId);
    }

    @Test(groups = "duelServices")
    public void getByCompetitor_shouldReturnEmptyListWhenNoCompetitorDuels() {
        Integer competitorId = 999;

        when(mockDuelController.getBy(competitorId)).thenReturn(Collections.emptyList());

        List<DuelDTO> result = service.getByCompetitor(competitorId, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test(groups = "duelServices")
    public void getUntiesFromTournament_shouldReturnUntiedDuelsForTournament() {
        Integer tournamentId = 2;
        List<DuelDTO> expectedDuels = createDuelDTOs(5);

        when(mockDuelController.getUntiesFromTournament(tournamentId)).thenReturn(expectedDuels);

        List<DuelDTO> result = service.getUntiesFromTournament(tournamentId, null);

        assertNotNull(result);
        assertEquals(result.size(), 5);
        verify(mockDuelController).getUntiesFromTournament(tournamentId);
    }

    @Test(groups = "duelServices")
    public void getUntiesFromTournament_shouldReturnEmptyListWhenNoUnties() {
        Integer tournamentId = 2;

        when(mockDuelController.getUntiesFromTournament(tournamentId)).thenReturn(Collections.emptyList());

        List<DuelDTO> result = service.getUntiesFromTournament(tournamentId, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test(groups = "duelServices")
    public void getUntiesFromParticipant_shouldReturnUntiedDuelsForParticipant() {
        Integer participantId = 10;
        List<DuelDTO> expectedDuels = createDuelDTOs(4);

        when(mockDuelController.getUntiesFromParticipant(participantId)).thenReturn(expectedDuels);

        List<DuelDTO> result = service.getUntiesFromParticipant(participantId, null);

        assertNotNull(result);
        assertEquals(result.size(), 4);
        verify(mockDuelController).getUntiesFromParticipant(participantId);
    }

    @Test(groups = "duelServices")
    public void getUntiesFromParticipant_shouldReturnEmptyListWhenNoUnties() {
        Integer participantId = 10;

        when(mockDuelController.getUntiesFromParticipant(participantId)).thenReturn(Collections.emptyList());

        List<DuelDTO> result = service.getUntiesFromParticipant(participantId, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test(groups = "duelServices")
    public void getUntiesFromGroup_shouldCallControllerWithCorrectParameter() {
        Integer groupId = 15;

        when(mockDuelController.getUntiesFromGroup(groupId)).thenReturn(Collections.emptyList());

        service.getUntiesFromGroup(groupId, null);

        verify(mockDuelController).getUntiesFromGroup(groupId);
    }

    @Test(groups = "duelServices")
    public void getByCompetitor_shouldCallControllerWithCorrectParameter() {
        Integer competitorId = 25;

        when(mockDuelController.getBy(competitorId)).thenReturn(Collections.emptyList());

        service.getByCompetitor(competitorId, null);

        verify(mockDuelController).getBy(competitorId);
    }

    @Test(groups = "duelServices")
    public void getUntiesFromTournament_shouldCallControllerWithCorrectParameter() {
        Integer tournamentId = 35;

        when(mockDuelController.getUntiesFromTournament(tournamentId)).thenReturn(Collections.emptyList());

        service.getUntiesFromTournament(tournamentId, null);

        verify(mockDuelController).getUntiesFromTournament(tournamentId);
    }

    @Test(groups = "duelServices")
    public void getUntiesFromParticipant_shouldCallControllerWithCorrectParameter() {
        Integer participantId = 45;

        when(mockDuelController.getUntiesFromParticipant(participantId)).thenReturn(Collections.emptyList());

        service.getUntiesFromParticipant(participantId, null);

        verify(mockDuelController).getUntiesFromParticipant(participantId);
    }

    // Helper methods

    private List<DuelDTO> createDuelDTOs(int count) {
        List<DuelDTO> duels = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            DuelDTO duel = new DuelDTO();
            duel.setId(i);
            duels.add(duel);
        }
        return duels;
    }
}

