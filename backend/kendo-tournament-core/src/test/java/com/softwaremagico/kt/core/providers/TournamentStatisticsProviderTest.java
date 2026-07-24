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

import com.softwaremagico.kt.core.statistics.TournamentFightStatistics;
import com.softwaremagico.kt.core.statistics.TournamentStatistics;
import com.softwaremagico.kt.core.statistics.TournamentStatisticsRepository;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.RoleType;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

@Test(groups = "tournamentStatisticsProviderTests")
public class TournamentStatisticsProviderTest {

    @Mock
    private TournamentStatisticsRepository repository;

    @Mock
    private TournamentFightStatisticsProvider fightStatisticsProvider;

    @Mock
    private TeamProvider teamProvider;

    @Mock
    private RoleProvider roleProvider;

    private TournamentStatisticsProvider provider;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        provider = new TournamentStatisticsProvider(repository, fightStatisticsProvider, teamProvider, roleProvider);
    }

    private Tournament createTournament() {
        final Tournament tournament = new Tournament();
        tournament.setId(42);
        tournament.setName("World Kendo Championship");
        tournament.setTeamSize(3);
        tournament.setFightSize(3);
        tournament.setCreatedAt(LocalDateTime.of(2026, 1, 1, 9, 0));
        tournament.setLockedAt(LocalDateTime.of(2026, 1, 10, 12, 0));
        tournament.setFinishedAt(LocalDateTime.of(2026, 1, 11, 18, 0));
        return tournament;
    }

    @Test
    public void shouldPopulateTournamentMetadataFromEntity() {
        final Tournament tournament = createTournament();
        final TournamentFightStatistics fightStats = new TournamentFightStatistics();

        when(fightStatisticsProvider.get(tournament)).thenReturn(fightStats);
        when(teamProvider.count(tournament)).thenReturn(8L);
        for (final RoleType roleType : RoleType.values()) {
            when(roleProvider.count(tournament, roleType)).thenReturn(0L);
        }

        final TournamentStatistics result = provider.get(tournament);

        assertNotNull(result);
        assertEquals(result.getTournamentId(), Integer.valueOf(42));
        assertEquals(result.getTournamentName(), "World Kendo Championship");
        assertEquals(result.getTeamSize(), Integer.valueOf(3));
        assertEquals(result.getFightSize(), Integer.valueOf(3));
    }

    @Test
    public void shouldSetFightStatisticsFromFightStatisticsProvider() {
        final Tournament tournament = createTournament();
        final TournamentFightStatistics fightStats = new TournamentFightStatistics();
        fightStats.setFightsNumber(10L);

        when(fightStatisticsProvider.get(tournament)).thenReturn(fightStats);
        when(teamProvider.count(tournament)).thenReturn(4L);
        for (final RoleType roleType : RoleType.values()) {
            when(roleProvider.count(tournament, roleType)).thenReturn(0L);
        }

        final TournamentStatistics result = provider.get(tournament);

        assertNotNull(result.getFightStatistics());
        assertEquals(result.getFightStatistics().getFightsNumber(), Long.valueOf(10L));
        verify(fightStatisticsProvider).get(tournament);
    }

    @Test
    public void shouldSetNumberOfTeamsFromTeamProvider() {
        final Tournament tournament = createTournament();

        when(fightStatisticsProvider.get(tournament)).thenReturn(new TournamentFightStatistics());
        when(teamProvider.count(tournament)).thenReturn(6L);
        for (final RoleType roleType : RoleType.values()) {
            when(roleProvider.count(tournament, roleType)).thenReturn(0L);
        }

        final TournamentStatistics result = provider.get(tournament);

        assertEquals(result.getNumberOfTeams(), Long.valueOf(6L));
        verify(teamProvider).count(tournament);
    }

    @Test
    public void shouldSetTournamentDatesFromEntity() {
        final Tournament tournament = createTournament();

        when(fightStatisticsProvider.get(tournament)).thenReturn(new TournamentFightStatistics());
        when(teamProvider.count(tournament)).thenReturn(0L);
        for (final RoleType roleType : RoleType.values()) {
            when(roleProvider.count(tournament, roleType)).thenReturn(0L);
        }

        final TournamentStatistics result = provider.get(tournament);

        assertEquals(result.getTournamentCreatedAt(), LocalDateTime.of(2026, 1, 1, 9, 0));
        assertEquals(result.getTournamentLockedAt(), LocalDateTime.of(2026, 1, 10, 12, 0));
        assertEquals(result.getTournamentFinishedAt(), LocalDateTime.of(2026, 1, 11, 18, 0));
    }

    @Test
    public void shouldCountParticipantsForEachRoleType() {
        final Tournament tournament = createTournament();

        when(fightStatisticsProvider.get(tournament)).thenReturn(new TournamentFightStatistics());
        when(teamProvider.count(tournament)).thenReturn(0L);
        when(roleProvider.count(tournament, RoleType.COMPETITOR)).thenReturn(20L);
        when(roleProvider.count(tournament, RoleType.REFEREE)).thenReturn(3L);
        when(roleProvider.count(tournament, RoleType.ORGANIZER)).thenReturn(5L);
        when(roleProvider.count(tournament, RoleType.VOLUNTEER)).thenReturn(2L);
        when(roleProvider.count(tournament, RoleType.PRESS)).thenReturn(1L);

        final TournamentStatistics result = provider.get(tournament);

        assertNotNull(result.getNumberOfParticipants());
        assertEquals(result.getNumberOfParticipants().size(), RoleType.values().length);
        assertEquals(result.getNumberOfParticipants().get(RoleType.COMPETITOR), Long.valueOf(20L));
        assertEquals(result.getNumberOfParticipants().get(RoleType.REFEREE), Long.valueOf(3L));
        assertEquals(result.getNumberOfParticipants().get(RoleType.ORGANIZER), Long.valueOf(5L));
        assertEquals(result.getNumberOfParticipants().get(RoleType.VOLUNTEER), Long.valueOf(2L));
        assertEquals(result.getNumberOfParticipants().get(RoleType.PRESS), Long.valueOf(1L));

        for (final RoleType roleType : RoleType.values()) {
            verify(roleProvider).count(tournament, roleType);
        }
    }

    @Test
    public void shouldHandleTournamentWithNullDates() {
        final Tournament tournament = new Tournament();
        tournament.setId(1);
        tournament.setName("Minimal Tournament");
        tournament.setTeamSize(3);
        tournament.setFightSize(3);

        when(fightStatisticsProvider.get(tournament)).thenReturn(new TournamentFightStatistics());
        when(teamProvider.count(tournament)).thenReturn(0L);
        for (final RoleType roleType : RoleType.values()) {
            when(roleProvider.count(tournament, roleType)).thenReturn(0L);
        }

        final TournamentStatistics result = provider.get(tournament);

        assertNotNull(result);
        assertNull(result.getTournamentCreatedAt());
        assertNull(result.getTournamentLockedAt());
        assertNull(result.getTournamentFinishedAt());
    }

    @Test
    public void shouldReturnRepositoryInstance() {
        assertNotNull(provider.getRepository());
        assertEquals(provider.getRepository(), repository);
    }
}

