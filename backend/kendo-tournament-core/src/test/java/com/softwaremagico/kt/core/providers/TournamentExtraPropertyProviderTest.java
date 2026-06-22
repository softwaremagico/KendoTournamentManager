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

import com.softwaremagico.kt.core.exceptions.InvalidExtraPropertyException;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.repositories.GroupRepository;
import com.softwaremagico.kt.persistence.repositories.TournamentExtraPropertyRepository;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

@Test(groups = {"tournamentExtraProperties"})
public class TournamentExtraPropertyProviderTest {

    private static final int WAIT_ATTEMPTS = 20;
    private static final long PAUSE_MILLIS = 50L;

    @Mock
    private TournamentExtraPropertyRepository repository;

    @Mock
    private GroupRepository groupRepository;

    private TournamentExtraPropertyProvider provider;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        provider = new TournamentExtraPropertyProvider(repository, groupRepository);
    }

    @Test
    public void shouldCreateDefaultPropertyWhenMissing() {
        final Tournament tournament = tournament();
        when(repository.findByTournamentAndPropertyKey(tournament, TournamentExtraPropertyKey.MAXIMIZE_FIGHTS)).thenReturn(null);
        when(repository.save(any(TournamentExtraProperty.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final TournamentExtraProperty result = provider.getByTournamentAndProperty(tournament,
                TournamentExtraPropertyKey.MAXIMIZE_FIGHTS, true);

        assertEquals(result.getPropertyKey(), TournamentExtraPropertyKey.MAXIMIZE_FIGHTS);
        assertEquals(result.getPropertyValue(), "true");
        verify(repository).save(any(TournamentExtraProperty.class));
    }

    @Test
    public void shouldUpdateExistingPropertyInPlace() {
        final Tournament tournament = tournament();
        final TournamentExtraProperty existing = new TournamentExtraProperty(tournament,
                TournamentExtraPropertyKey.MAXIMIZE_FIGHTS, "false");
        existing.setCreatedBy(null);

        final TournamentExtraProperty input = new TournamentExtraProperty(tournament,
                TournamentExtraPropertyKey.MAXIMIZE_FIGHTS, "true");
        input.setCreatedBy("creator");
        input.setUpdatedBy("updater");

        when(repository.findByTournamentAndPropertyKey(tournament, TournamentExtraPropertyKey.MAXIMIZE_FIGHTS)).thenReturn(existing);
        when(repository.save(existing)).thenAnswer(invocation -> invocation.getArgument(0));

        final TournamentExtraProperty result = provider.save(input);

        assertSame(result, existing);
        assertEquals(result.getPropertyValue(), "true");
        assertEquals(result.getCreatedBy(), "creator");
        assertEquals(result.getUpdatedBy(), "updater");
        verify(repository).save(existing);
    }

    @Test
    public void shouldThrowWhenPropertyIsNotAllowedForTournament() {
        final Tournament tournament = tournament();
        final TournamentExtraProperty property = new TournamentExtraProperty(tournament,
                TournamentExtraPropertyKey.AVOID_DUPLICATES, "true");

        try {
            provider.save(property);
            fail("Expected InvalidExtraPropertyException");
        } catch (InvalidExtraPropertyException expected) {
            assertTrue(expected.getMessage().contains("cannot have property"));
        }

        verify(repository, never()).save(any(TournamentExtraProperty.class));
    }

    @Test
    public void shouldUpdateNumberOfWinnersInGroupsInBackground() {
        final Tournament tournament = tournament();
        final TournamentExtraProperty property = new TournamentExtraProperty(tournament,
                TournamentExtraPropertyKey.NUMBER_OF_WINNERS, "2");

        final Group firstLevelGroup = new Group(tournament, 0, 0);
        firstLevelGroup.setId(1);
        firstLevelGroup.setNumberOfWinners(1);
        final Group secondLevelGroup = new Group(tournament, 1, 0);
        secondLevelGroup.setId(2);
        secondLevelGroup.setNumberOfWinners(1);

        when(repository.findByTournamentAndPropertyKey(tournament, TournamentExtraPropertyKey.NUMBER_OF_WINNERS)).thenReturn(null);
        when(repository.save(any(TournamentExtraProperty.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(groupRepository.findByTournamentOrderByLevelAscIndexAsc(tournament)).thenReturn(List.of(firstLevelGroup, secondLevelGroup));
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));

        provider.save(property);

        waitUntil(() -> firstLevelGroup.getNumberOfWinners() == 2, WAIT_ATTEMPTS, PAUSE_MILLIS);

        assertEquals(firstLevelGroup.getNumberOfWinners(), 2);
        assertEquals(secondLevelGroup.getNumberOfWinners(), 1);
        verify(groupRepository).save(firstLevelGroup);
        verify(groupRepository, never()).save(secondLevelGroup);
    }

    private void waitUntil(BooleanSupplier condition, int attempts, long pauseMillis) {
        for (int i = 0; i < attempts; i++) {
            if (condition.getAsBoolean()) {
                return;
            }
            LockSupport.parkNanos(Duration.ofMillis(pauseMillis).toNanos());
        }
        assertTrue(condition.getAsBoolean());
    }

    private Tournament tournament() {
        final Tournament tournament = new Tournament("Tournament", 1, 3, TournamentType.LEAGUE, "tester");
        tournament.setId(101);
        return tournament;
    }
}


