package com.softwaremagico.kt.core.controller;

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

import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.controller.models.TournamentExtraPropertyDTO;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.TournamentExtraPropertyConverter;
import com.softwaremagico.kt.core.exceptions.TournamentNotFoundException;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.TournamentExtraPropertyProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.core.tournaments.SenbatsuTournamentHandler;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class TournamentExtraPropertyControllerTest {

    @Mock
    private TournamentExtraPropertyProvider provider;

    @Mock
    private TournamentExtraPropertyConverter converter;

    @Mock
    private TournamentProvider tournamentProvider;

    @Mock
    private TournamentConverter tournamentConverter;

    @Mock
    private GroupProvider groupProvider;

    private TournamentExtraPropertyController controller;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new TournamentExtraPropertyController(provider, converter, tournamentProvider, tournamentConverter, groupProvider);
    }

    @Test(groups = "tournamentExtraProperties")
    public void update_shouldReuseExistingPropertyIdAndDeleteGroupsForOddFightsFlag() {
        Tournament tournament = new Tournament();
        TournamentDTO tournamentDTO = new TournamentDTO();
        tournamentDTO.setName("Tournament");

        TournamentExtraPropertyDTO dto = new TournamentExtraPropertyDTO();
        dto.setTournament(tournamentDTO);
        dto.setPropertyKey(TournamentExtraPropertyKey.ODD_FIGHTS_RESOLVED_ASAP);
        dto.setPropertyValue("true");

        TournamentExtraProperty existing = new TournamentExtraProperty();
        existing.setId(77);

        TournamentExtraProperty mapped = new TournamentExtraProperty();
        TournamentExtraProperty saved = new TournamentExtraProperty();
        TournamentExtraPropertyDTO savedDTO = new TournamentExtraPropertyDTO();

        when(tournamentConverter.reverse(tournamentDTO)).thenReturn(tournament);
        when(provider.getByTournamentAndProperty(tournament, TournamentExtraPropertyKey.ODD_FIGHTS_RESOLVED_ASAP)).thenReturn(existing);
        when(converter.reverse(dto)).thenReturn(mapped);
        when(provider.save(mapped)).thenReturn(saved);
        when(converter.convert(any())).thenReturn(savedDTO);

        TournamentExtraPropertyDTO result = controller.update(dto, "tester", "session");

        assertEquals(dto.getId(), Integer.valueOf(77));
        assertEquals(result, savedDTO);
        verify(groupProvider).delete(tournament);
    }

    @Test(groups = "tournamentExtraProperties")
    public void update_shouldNotDeleteGroupsWhenPropertyIsNotOddFightsFlag() {
        Tournament tournament = new Tournament();
        TournamentDTO tournamentDTO = new TournamentDTO();
        tournamentDTO.setName("Tournament");

        TournamentExtraPropertyDTO dto = new TournamentExtraPropertyDTO();
        dto.setTournament(tournamentDTO);
        dto.setPropertyKey(TournamentExtraPropertyKey.AVOID_DUPLICATES);
        dto.setPropertyValue("false");

        TournamentExtraProperty mapped = new TournamentExtraProperty();
        TournamentExtraProperty saved = new TournamentExtraProperty();

        when(tournamentConverter.reverse(tournamentDTO)).thenReturn(tournament);
        when(provider.getByTournamentAndProperty(tournament, TournamentExtraPropertyKey.AVOID_DUPLICATES)).thenReturn(null);
        when(converter.reverse(dto)).thenReturn(mapped);
        when(provider.save(mapped)).thenReturn(saved);
        when(converter.convert(any())).thenReturn(new TournamentExtraPropertyDTO());

        controller.update(dto, "tester", "session");

        assertNull(dto.getId());
        verify(groupProvider, never()).delete(tournament);
    }

    @Test(groups = "tournamentExtraProperties")
    public void getByTournamentAndProperty_shouldUseDefaultDistanceForSenbatsuChallengeDistance() {
        Tournament tournament = new Tournament();
        TournamentExtraProperty property = new TournamentExtraProperty();
        TournamentExtraPropertyDTO expected = new TournamentExtraPropertyDTO();

        when(tournamentProvider.get(15)).thenReturn(Optional.of(tournament));
        when(provider.getByTournamentAndProperty(tournament, TournamentExtraPropertyKey.SENBATSU_CHALLENGE_DISTANCE,
                SenbatsuTournamentHandler.DEFAULT_CHALLENGE_DISTANCE)).thenReturn(property);
        when(converter.convert(any())).thenReturn(expected);

        TournamentExtraPropertyDTO result = controller.getByTournamentAndProperty(15, TournamentExtraPropertyKey.SENBATSU_CHALLENGE_DISTANCE);

        assertEquals(result, expected);
    }

    @Test(groups = "tournamentExtraProperties", expectedExceptions = TournamentNotFoundException.class)
    public void getByTournamentId_shouldThrowWhenTournamentDoesNotExist() {
        when(tournamentProvider.get(999)).thenReturn(Optional.empty());

        controller.getByTournamentId(999);
    }
}


