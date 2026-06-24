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

import com.softwaremagico.kt.persistence.encryption.KeyProperty;
import com.softwaremagico.kt.persistence.entities.Club;
import com.softwaremagico.kt.persistence.repositories.ClubRepository;
import com.softwaremagico.kt.persistence.repositories.ParticipantRepository;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Test(groups = "clubProviderTests")
public class ClubProviderTest {

    private ClubRepository clubRepository;
    private ClubProvider clubProvider;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        clubRepository = mock(ClubRepository.class);
        final ParticipantRepository participantRepository = mock(ParticipantRepository.class);
        clubProvider = new ClubProvider(clubRepository, participantRepository);
        new KeyProperty(null, null, null);
    }

    @Test
    public void shouldUseRepositoryQueryWhenEncryptionDisabled() {
        final Club club = new Club("Dojo", "Spain", "Madrid");
        when(clubRepository.findByNameIgnoreCaseAndCityIgnoreCase("Dojo", "Madrid")).thenReturn(Optional.of(club));

        final Optional<Club> found = clubProvider.findBy("Dojo", "Madrid");

        assertTrue(found.isPresent());
        assertEquals(found.get(), club);
        verify(clubRepository).findByNameIgnoreCaseAndCityIgnoreCase("Dojo", "Madrid");
        verify(clubRepository, never()).findAll();
    }

    @Test
    public void shouldUseRepositoryQueryWhenEncryptionKeyIsBlank() {
        new KeyProperty("   ", null, null);
        when(clubRepository.findByNameIgnoreCaseAndCityIgnoreCase("Dojo", "Madrid")).thenReturn(Optional.empty());

        final Optional<Club> found = clubProvider.findBy("Dojo", "Madrid");

        assertFalse(found.isPresent());
        verify(clubRepository).findByNameIgnoreCaseAndCityIgnoreCase("Dojo", "Madrid");
        verify(clubRepository, never()).findAll();
    }

    @Test
    public void shouldFindClubFromInMemoryLoopWhenEncryptionEnabled() {
        new KeyProperty("enc-key", null, null);
        final Club first = new Club("Other", "Spain", "Bilbao");
        final Club matching = new Club("Dojo", "Spain", "Madrid");
        when(clubRepository.findAll()).thenReturn(List.of(first, matching));

        final Optional<Club> found = clubProvider.findBy("dojo", "madrid");

        assertTrue(found.isPresent());
        assertEquals(found.get(), matching);
        verify(clubRepository).findAll();
        verify(clubRepository, never()).findByNameIgnoreCaseAndCityIgnoreCase("dojo", "madrid");
    }

    @Test
    public void shouldReturnEmptyWhenEncryptionEnabledAndNoClubMatches() {
        new KeyProperty("enc-key", null, null);
        when(clubRepository.findAll()).thenReturn(List.of(new Club("Dojo", "Spain", "Sevilla")));

        final Optional<Club> found = clubProvider.findBy("Dojo", "Madrid");

        assertFalse(found.isPresent());
        verify(clubRepository).findAll();
    }
}

