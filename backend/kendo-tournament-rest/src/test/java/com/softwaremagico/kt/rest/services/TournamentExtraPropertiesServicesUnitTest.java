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

import com.softwaremagico.kt.core.controller.TournamentExtraPropertyController;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.controller.models.TournamentExtraPropertyDTO;
import com.softwaremagico.kt.persistence.values.SwissTieBreakRule;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@Test(groups = "restServicesUnit")
public class TournamentExtraPropertiesServicesUnitTest {

    @Mock
    private TournamentExtraPropertyController tournamentExtraPropertyController;

    @Mock
    private Authentication authentication;

    private TournamentExtraPropertiesServices services;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(authentication.getName()).thenReturn("editor");
        services = new TournamentExtraPropertiesServices(tournamentExtraPropertyController);
    }

    @Test
    public void shouldGetPropertiesByTournament() {
        when(tournamentExtraPropertyController.getByTournamentId(10)).thenReturn(List.of(new TournamentExtraPropertyDTO()));

        final List<TournamentExtraPropertyDTO> result = services.get(10, authentication, null);

        assertEquals(result.size(), 1);
        verify(tournamentExtraPropertyController).getByTournamentId(10);
    }

    @Test
    public void shouldGetPropertyBySwissKey() {
        final TournamentExtraPropertyDTO propertyDTO = new TournamentExtraPropertyDTO();
        propertyDTO.setPropertyValue("4");
        when(tournamentExtraPropertyController.getByTournamentAndProperty(7, TournamentExtraPropertyKey.SWISS_ROUNDS)).thenReturn(propertyDTO);

        final TournamentExtraPropertyDTO result = services.getByKey(7, TournamentExtraPropertyKey.SWISS_ROUNDS, authentication, null);

        assertNotNull(result);
        assertEquals(result.getPropertyValue(), "4");
        verify(tournamentExtraPropertyController).getByTournamentAndProperty(7, TournamentExtraPropertyKey.SWISS_ROUNDS);
    }

    @Test
    public void shouldCreateSwissProperty() {
        final TournamentDTO tournamentDTO = new TournamentDTO("Swiss", 1, 3, TournamentType.SWISS, null);
        final TournamentExtraPropertyDTO propertyDTO = new TournamentExtraPropertyDTO(tournamentDTO,
                TournamentExtraPropertyKey.SWISS_TIE_BREAK_RULE, SwissTieBreakRule.BUCHHOLZ.name());
        when(tournamentExtraPropertyController.create(propertyDTO, "editor", "session-1")).thenReturn(propertyDTO);

        final TournamentExtraPropertyDTO result = services.add(propertyDTO, authentication, "session-1", null);

        assertNotNull(result);
        assertEquals(result.getPropertyValue(), SwissTieBreakRule.BUCHHOLZ.name());
        verify(tournamentExtraPropertyController).create(propertyDTO, "editor", "session-1");
    }

    @Test
    public void shouldUpdateSwissProperty() {
        final TournamentDTO tournamentDTO = new TournamentDTO("Swiss", 1, 3, TournamentType.SWISS, null);
        final TournamentExtraPropertyDTO propertyDTO = new TournamentExtraPropertyDTO(tournamentDTO,
                TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS, "false");
        when(tournamentExtraPropertyController.update(propertyDTO, "editor", "session-2")).thenReturn(propertyDTO);

        final TournamentExtraPropertyDTO result = services.update(propertyDTO, "session-2", authentication, null);

        assertNotNull(result);
        assertEquals(result.getPropertyValue(), "false");
        verify(tournamentExtraPropertyController).update(propertyDTO, "editor", "session-2");
    }

    @Test
    public void shouldGetLatestProperties() {
        when(tournamentExtraPropertyController.getLatest("editor")).thenReturn(List.of(new TournamentExtraPropertyDTO()));

        final List<TournamentExtraPropertyDTO> result = services.getLatest(authentication, null);

        assertEquals(result.size(), 1);
        verify(tournamentExtraPropertyController).getLatest("editor");
    }
}

