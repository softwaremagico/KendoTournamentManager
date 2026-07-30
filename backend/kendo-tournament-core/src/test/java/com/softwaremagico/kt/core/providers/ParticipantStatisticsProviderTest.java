package com.softwaremagico.kt.core.providers;

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

import com.softwaremagico.kt.core.statistics.ParticipantStatistics;
import com.softwaremagico.kt.core.statistics.ParticipantStatisticsRepository;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Test(groups = "participantStatisticsProviderTests")
public class ParticipantStatisticsProviderTest {

    @Mock
    private ParticipantStatisticsRepository repository;

    private ParticipantStatisticsProvider provider;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        provider = new ParticipantStatisticsProvider(repository);
    }

    @Test
    public void shouldSaveEntityViaRepository() {
        final ParticipantStatistics stats = new ParticipantStatistics();
        when(repository.save(stats)).thenReturn(stats);

        final ParticipantStatistics saved = provider.save(stats);

        assertNotNull(saved);
        assertEquals(saved, stats);
        verify(repository).save(stats);
    }

    @Test
    public void shouldReturnEntityByIdWhenPresent() {
        final ParticipantStatistics stats = new ParticipantStatistics();
        when(repository.findById(1)).thenReturn(Optional.of(stats));

        final Optional<ParticipantStatistics> result = provider.get(1);

        assertTrue(result.isPresent());
        assertEquals(result.get(), stats);
        verify(repository).findById(1);
    }

    @Test
    public void shouldReturnEmptyOptionalWhenEntityNotFound() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        final Optional<ParticipantStatistics> result = provider.get(99);

        assertFalse(result.isPresent());
        verify(repository).findById(99);
    }

    @Test
    public void shouldReturnAllEntitiesFromRepository() {
        final ParticipantStatistics s1 = new ParticipantStatistics();
        final ParticipantStatistics s2 = new ParticipantStatistics();
        when(repository.findAll()).thenReturn(List.of(s1, s2));

        final List<ParticipantStatistics> all = provider.getAll();

        assertNotNull(all);
        assertEquals(all.size(), 2);
        verify(repository).findAll();
    }

    @Test
    public void shouldDeleteByIdViaRepository() {
        provider.deleteById(5);

        verify(repository).deleteById(5);
    }

    @Test
    public void shouldDeleteAllViaRepository() {
        provider.deleteAll();

        verify(repository).deleteAll();
    }

    @Test
    public void shouldCountEntitiesViaRepository() {
        when(repository.count()).thenReturn(7L);

        final long count = provider.count();

        assertEquals(count, 7L);
        verify(repository).count();
    }

    @Test
    public void shouldReturnRepositoryInstance() {
        assertNotNull(provider.getRepository());
        assertEquals(provider.getRepository(), repository);
    }
}

