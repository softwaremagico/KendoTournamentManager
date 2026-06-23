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

import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.AchievementRepository;
import com.softwaremagico.kt.persistence.repositories.DuelRepository;
import com.softwaremagico.kt.persistence.repositories.FightRepository;
import com.softwaremagico.kt.persistence.repositories.GroupRepository;
import com.softwaremagico.kt.persistence.repositories.RoleRepository;
import com.softwaremagico.kt.persistence.repositories.TeamRepository;
import com.softwaremagico.kt.persistence.repositories.TournamentExtraPropertyRepository;
import com.softwaremagico.kt.persistence.repositories.TournamentImageRepository;
import com.softwaremagico.kt.persistence.repositories.TournamentRepository;
import com.softwaremagico.kt.persistence.values.TournamentType;
import com.softwaremagico.kt.core.tournaments.TournamentHandlerSelector;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Test(groups = {"tournamentProviderTests"})
public class TournamentProviderTest {

    @Mock
    private TournamentRepository tournamentRepository;
    @Mock
    private TournamentExtraPropertyRepository tournamentExtraPropertyRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private FightRepository fightRepository;
    @Mock
    private DuelRepository duelRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private TournamentHandlerSelector tournamentHandlerSelector;
    @Mock
    private TournamentImageRepository tournamentImageRepository;
    @Mock
    private AchievementRepository achievementRepository;

    private TournamentProvider provider;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        provider = new TournamentProvider(tournamentRepository, tournamentExtraPropertyRepository,
                groupRepository, fightRepository, duelRepository, teamRepository, roleRepository,
                tournamentHandlerSelector, tournamentImageRepository, achievementRepository);
    }

    // ========== getPreviousTo Tests ==========

    @Test
    public void testGetPreviousToWithNullTournament() {
        final List<Tournament> result = provider.getPreviousTo(null);
        assertThat(result).isEmpty();
    }

    @Test
    public void testGetPreviousToWithNullCreatedAt() {
        final Tournament tournament = new Tournament("T", 1, 3, TournamentType.LEAGUE, "user");
        // createdAt is null by default
        final List<Tournament> result = provider.getPreviousTo(tournament);
        assertThat(result).isEmpty();
    }

    @Test
    public void testGetPreviousToWithNullTournamentAndLimit() {
        final List<Tournament> result = provider.getPreviousTo(null, 5);
        assertThat(result).isEmpty();
    }

    @Test
    public void testGetPreviousToWithNullCreatedAtAndLimit() {
        final Tournament tournament = new Tournament("T", 1, 3, TournamentType.LEAGUE, "user");
        final List<Tournament> result = provider.getPreviousTo(tournament, 5);
        assertThat(result).isEmpty();
    }

    @Test
    public void testGetPreviousToReturnsOlderTournaments() {
        final Tournament t1 = tournamentWithDate("T1", LocalDateTime.now().minusDays(1));
        final Tournament t2 = tournamentWithDate("T2", LocalDateTime.now().minusDays(2));
        final Tournament t3 = tournamentWithDate("T3", LocalDateTime.now().minusDays(3));

        when(tournamentRepository.findAll()).thenReturn(new ArrayList<>(List.of(t1, t2, t3)));

        final List<Tournament> previous = provider.getPreviousTo(t1);

        assertThat(previous).containsExactly(t2, t3);
    }

    @Test
    public void testGetPreviousToWithLimitReturnsLimitedResults() {
        final Tournament t1 = tournamentWithDate("T1", LocalDateTime.now().minusDays(1));
        final Tournament t2 = tournamentWithDate("T2", LocalDateTime.now().minusDays(2));
        final Tournament t3 = tournamentWithDate("T3", LocalDateTime.now().minusDays(3));
        final Tournament t4 = tournamentWithDate("T4", LocalDateTime.now().minusDays(4));

        when(tournamentRepository.findAll()).thenReturn(new ArrayList<>(List.of(t1, t2, t3, t4)));

        final List<Tournament> previous = provider.getPreviousTo(t1, 2);

        assertThat(previous).hasSize(2).containsExactly(t2, t3);
    }

    // ========== markAsFinished Tests ==========

    @Test
    public void testMarkAsFinishedSetsFinishedAt() {
        final Tournament tournament = new Tournament("T", 1, 3, TournamentType.LEAGUE, "user");
        tournament.setId(1);

        provider.markAsFinished(tournament, true);

        assertThat(tournament.getFinishedAt()).isNotNull();
        verify(tournamentRepository).save(tournament);
    }

    @Test
    public void testMarkAsFinishedDoesNotSetIfAlreadyFinished() {
        final Tournament tournament = new Tournament("T", 1, 3, TournamentType.LEAGUE, "user");
        tournament.setId(1);
        final LocalDateTime alreadySet = LocalDateTime.now().minusHours(1);
        tournament.updateFinishedAt(alreadySet);

        provider.markAsFinished(tournament, true);

        assertThat(tournament.getFinishedAt()).isEqualTo(alreadySet);
        verify(tournamentRepository, never()).save(tournament);
    }

    @Test
    public void testMarkAsUnfinishedClearsFinishedAt() {
        final Tournament tournament = new Tournament("T", 1, 3, TournamentType.LEAGUE, "user");
        tournament.setId(1);
        tournament.updateFinishedAt(LocalDateTime.now());

        provider.markAsFinished(tournament, false);

        assertThat(tournament.getFinishedAt()).isNull();
        verify(tournamentRepository).save(tournament);
    }

    @Test
    public void testMarkAsUnfinishedNoSaveWhenAlreadyNotFinished() {
        final Tournament tournament = new Tournament("T", 1, 3, TournamentType.LEAGUE, "user");
        tournament.setId(1);
        // finishedAt already null

        provider.markAsFinished(tournament, false);

        verify(tournamentRepository, never()).save(tournament);
    }

    // ========== countTournamentsAfter Tests ==========

    @Test
    public void testCountTournamentsAfterWithNullDateUsesDefault() {
        final Tournament t1 = tournamentWithDate("T1", LocalDateTime.now().minusMonths(6));
        final Tournament t2 = tournamentWithDate("T2", LocalDateTime.now().minusYears(2));

        when(tournamentRepository.findAll()).thenReturn(List.of(t1, t2));

        final long count = provider.countTournamentsAfter(null);

        assertThat(count).isEqualTo(1L); // Only t1 is within 1 year
    }

    @Test
    public void testCountTournamentsAfterWithSpecificDate() {
        final LocalDateTime cutoff = LocalDateTime.now().minusDays(10);
        final Tournament t1 = tournamentWithDate("T1", LocalDateTime.now().minusDays(5));
        final Tournament t2 = tournamentWithDate("T2", LocalDateTime.now().minusDays(20));

        when(tournamentRepository.findAll()).thenReturn(List.of(t1, t2));

        final long count = provider.countTournamentsAfter(cutoff);

        assertThat(count).isEqualTo(1L);
    }

    @Test
    public void testCountTournamentsAfterExcludesNullCreatedAt() {
        final Tournament t1 = new Tournament("T1", 1, 3, TournamentType.LEAGUE, "user");
        // t1.createdAt is null

        when(tournamentRepository.findAll()).thenReturn(List.of(t1));

        final long count = provider.countTournamentsAfter(LocalDateTime.now().minusDays(30));

        assertThat(count).isZero();
    }

    // ========== findLastByUnlocked Tests ==========

    @Test
    public void testFindLastByUnlockedReturnsLatestUnlocked() {
        final Tournament t1 = tournamentWithDate("T1", LocalDateTime.now().minusDays(1));
        final Tournament t2 = tournamentWithDate("T2", LocalDateTime.now().minusDays(2));

        when(tournamentRepository.findByLocked(false)).thenReturn(new ArrayList<>(List.of(t1, t2)));

        final Tournament result = provider.findLastByUnlocked();

        assertThat(result).isEqualTo(t1);
    }

    @Test
    public void testFindLastByUnlockedReturnsNullWhenEmpty() {
        when(tournamentRepository.findByLocked(false)).thenReturn(List.of());

        final Tournament result = provider.findLastByUnlocked();

        assertThat(result).isNull();
    }

    // ========== update Tests ==========

    @Test
    public void testUpdateSetsLockedAtWhenLocking() {
        final Tournament tournament = new Tournament("T", 1, 3, TournamentType.LEAGUE, "user");
        tournament.setId(1);
        tournament.setLocked(true);
        // lockedAt is null

        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        provider.update(tournament);

        assertThat(tournament.getLockedAt()).isNotNull();
    }

    @Test
    public void testUpdateClearsLockedAtWhenUnlocking() {
        final Tournament tournament = new Tournament("T", 1, 3, TournamentType.LEAGUE, "user");
        tournament.setId(1);
        tournament.setLocked(false);
        tournament.setLockedAt(LocalDateTime.now());

        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        provider.update(tournament);

        assertThat(tournament.getLockedAt()).isNull();
    }

    @Test
    public void testUpdateDoesNotOverwriteExistingLockedAt() {
        final Tournament tournament = new Tournament("T", 1, 3, TournamentType.LEAGUE, "user");
        tournament.setId(1);
        tournament.setLocked(true);
        final LocalDateTime existingLockedAt = LocalDateTime.now().minusHours(2);
        tournament.setLockedAt(existingLockedAt);

        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        provider.update(tournament);

        assertThat(tournament.getLockedAt()).isEqualTo(existingLockedAt);
    }

    // ========== delete Tests ==========

    @Test
    public void testDeleteNullDoesNothing() {
        provider.delete((Tournament) null);

        verify(tournamentRepository, never()).delete(any(Tournament.class));
    }

    @Test
    public void testDeleteTournamentCleansRelatedEntities() {
        final Tournament tournament = new Tournament("T", 1, 3, TournamentType.LEAGUE, "user");
        tournament.setId(1);

        provider.delete(tournament);

        verify(tournamentExtraPropertyRepository).deleteByTournament(tournament);
        verify(groupRepository).deleteByTournament(tournament);
        verify(fightRepository).deleteByTournament(tournament);
        verify(duelRepository).deleteByTournament(tournament);
        verify(teamRepository).deleteByTournament(tournament);
        verify(roleRepository).deleteByTournament(tournament);
        verify(achievementRepository).deleteByTournament(tournament);
        verify(tournamentRepository).delete(tournament);
    }

    // ========== findByName Tests ==========

    @Test
    public void testFindByNameDelegatesToRepository() {
        final Tournament tournament = new Tournament("MyTournament", 1, 3, TournamentType.LEAGUE, "user");
        when(tournamentRepository.findByName("MyTournament")).thenReturn(Optional.of(tournament));

        final Optional<Tournament> result = provider.findByName("MyTournament");

        assertThat(result).isPresent().contains(tournament);
    }

    @Test
    public void testFindByNameReturnsEmptyWhenNotFound() {
        when(tournamentRepository.findByName("Unknown")).thenReturn(Optional.empty());

        final Optional<Tournament> result = provider.findByName("Unknown");

        assertThat(result).isEmpty();
    }

    // ========== Helper Methods ==========

    private Tournament tournamentWithDate(String name, LocalDateTime createdAt) {
        final Tournament tournament = new Tournament(name, 1, 3, TournamentType.LEAGUE, "user");
        tournament.setCreatedAt(createdAt);
        return tournament;
    }
}

