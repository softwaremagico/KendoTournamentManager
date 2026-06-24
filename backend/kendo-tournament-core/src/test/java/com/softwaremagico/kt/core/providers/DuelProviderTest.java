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

import com.softwaremagico.kt.core.exceptions.TournamentNotFoundException;
import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.DuelRepository;
import com.softwaremagico.kt.persistence.repositories.GroupRepository;
import com.softwaremagico.kt.persistence.repositories.TournamentRepository;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Test(groups = {"duelProviderTests"})
public class DuelProviderTest {

    @Mock
    private DuelRepository duelRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private TournamentRepository tournamentRepository;

    private DuelProvider provider;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        provider = new DuelProvider(duelRepository, groupRepository, tournamentRepository);
    }

    @Test
    public void testGetDurationAverageReturnsFallbackWhenNull() {
        when(duelRepository.getDurationAverage()).thenReturn(null);

        final Long duration = provider.getDurationAverage();

        assertThat(duration).isEqualTo(-1L);
    }

    @Test
    public void testGetDurationAverageReturnsRepositoryValueWhenPresent() {
        when(duelRepository.getDurationAverage()).thenReturn(42L);

        final Long duration = provider.getDurationAverage();

        assertThat(duration).isEqualTo(42L);
    }

    @Test
    public void testGetDurationAverageByParticipantReturnsFallbackWhenNull() {
        final Participant participant = participant("P1");
        when(duelRepository.getDurationAverage(participant)).thenReturn(null);

        final Long duration = provider.getDurationAverage(participant);

        assertThat(duration).isEqualTo(-1L);
    }

    @Test
    public void testCountFaultsHandlesNullValues() {
        final Tournament tournament = tournament("Autumn Cup");
        when(duelRepository.countFaultsByTournament(tournament, true)).thenReturn(null);
        when(duelRepository.countScore(tournament, List.of(Score.HANSOKU))).thenReturn(null);

        final long faults = provider.countFaults(tournament);

        assertThat(faults).isEqualTo(0L);
    }

    @Test
    public void testCountFaultsAddsHansokusWithMultiplier() {
        final Tournament tournament = tournament("Autumn Cup");
        when(duelRepository.countFaultsByTournament(tournament, true)).thenReturn(3L);
        when(duelRepository.countScore(tournament, List.of(Score.HANSOKU))).thenReturn(2L);

        final long faults = provider.countFaults(tournament);

        assertThat(faults).isEqualTo(7L);
    }

    @Test
    public void testGetUntiesFromGroupReturnsEmptyWhenGroupNotFound() {
        when(groupRepository.findById(10)).thenReturn(Optional.empty());

        final List<Duel> unties = provider.getUntiesFromGroup(10);

        assertThat(unties).isEmpty();
    }

    @Test
    public void testGetUntiesFromGroupReturnsGroupUntiesWhenGroupExists() {
        final Group group = new Group(tournament("Autumn Cup"), 0, 0);
        final Duel duel = new Duel();
        group.setUnties(new ArrayList<>(List.of(duel)));
        when(groupRepository.findById(10)).thenReturn(Optional.of(group));

        final List<Duel> unties = provider.getUntiesFromGroup(10);

        assertThat(unties).containsExactly(duel);
    }

    @Test
    public void testGetUntiesFromTournamentThrowsWhenTournamentNotFound() {
        when(tournamentRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> provider.getUntiesFromTournament(99))
                .isInstanceOf(TournamentNotFoundException.class)
                .hasMessageContaining("No tournament found with id '99'");
    }

    @Test
    public void testGetUntiesFromTournamentFlattensUntiesFromAllGroups() {
        final Tournament tournament = tournament("Autumn Cup");
        final Group group1 = new Group(tournament, 0, 0);
        final Group group2 = new Group(tournament, 0, 1);
        final Duel duel1 = new Duel();
        final Duel duel2 = new Duel();
        group1.setUnties(new ArrayList<>(List.of(duel1)));
        group2.setUnties(new ArrayList<>(List.of(duel2)));

        when(tournamentRepository.findById(tournament.getId())).thenReturn(Optional.of(tournament));
        when(groupRepository.findByTournamentOrderByLevelAscIndexAsc(tournament)).thenReturn(List.of(group1, group2));

        final List<Duel> unties = provider.getUntiesFromTournament(tournament.getId());

        assertThat(unties).containsExactly(duel1, duel2);
    }

    @Test
    public void testCountScoreFromCompetitorReturnsZeroWhenRepositoryThrowsNullPointer() {
        final Participant participant = participant("P1");
        final Collection<Tournament> tournaments = List.of(tournament("Autumn Cup"));
        when(duelRepository.countLeftScoreFromCompetitor(participant, tournaments)).thenThrow(new NullPointerException());

        final long count = provider.countScoreFromCompetitor(participant, tournaments);

        assertThat(count).isEqualTo(0L);
    }

    @Test
    public void testCountScoreAgainstCompetitorReturnsZeroWhenRepositoryThrowsNullPointer() {
        final Participant participant = participant("P1");
        final Collection<Tournament> tournaments = List.of(tournament("Autumn Cup"));
        when(duelRepository.countLeftScoreAgainstCompetitor(participant, tournaments)).thenThrow(new NullPointerException());

        final long count = provider.countScoreAgainstCompetitor(participant, tournaments);

        assertThat(count).isEqualTo(0L);
    }

     @Test
     public void testReportCacheEvictIsCallable() {
         provider.reportCacheEvict();

         verify(duelRepository, org.mockito.Mockito.never()).countByTournament(any(Tournament.class));
     }

     // ============= Additional comprehensive tests for 90%+ coverage =============

     @Test
     public void testDeleteByTournamentDelegatesToRepository() {
         final Tournament tournament = tournament("Spring Cup");
         when(duelRepository.deleteByTournament(tournament)).thenReturn(5L);

         final long deleted = provider.delete(tournament);

         assertThat(deleted).isEqualTo(5L);
         verify(duelRepository).deleteByTournament(tournament);
     }

     @Test
     public void testCountByTournamentDelegatesToRepository() {
         final Tournament tournament = tournament("Summer Cup");
         when(duelRepository.countByTournament(tournament)).thenReturn(10L);

         final long count = provider.count(tournament);

         assertThat(count).isEqualTo(10L);
         verify(duelRepository).countByTournament(tournament);
     }

     @Test
     public void testGetByParticipantDelegatesToRepository() {
         final Participant participant = participant("P1");
         final List<Duel> duels = List.of(new Duel(), new Duel());
         when(duelRepository.findByParticipant(participant)).thenReturn(duels);

         final List<Duel> result = provider.get(participant);

         assertThat(result).isEqualTo(duels);
         verify(duelRepository).findByParticipant(participant);
     }

     @Test
     public void testGetWhenBothAreInvolvedDelegatesToRepository() {
         final Participant p1 = participant("P1");
         final Participant p2 = participant("P2");
         final List<Duel> duels = List.of(new Duel());
         when(duelRepository.findByParticipants(p1, p2)).thenReturn(duels);

         final List<Duel> result = provider.getWhenBothAreInvolved(p1, p2);

         assertThat(result).isEqualTo(duels);
         verify(duelRepository).findByParticipants(p1, p2);
     }

     @Test
     public void testGetByTournamentDelegatesToRepository() {
         final Tournament tournament = tournament("Fall Cup");
         final List<Duel> duels = List.of(new Duel(), new Duel(), new Duel());
         when(duelRepository.findByTournament(tournament)).thenReturn(duels);

         final List<Duel> result = provider.get(tournament);

         assertThat(result).isEqualTo(duels);
         verify(duelRepository).findByTournament(tournament);
     }

     @Test
     public void testGetUntiesByParticipantsDelegatesToRepository() {
         final Collection<Participant> participants = List.of(participant("P1"), participant("P2"));
         final List<Duel> duels = List.of(new Duel());
         when(duelRepository.findUntiesByParticipantIn(participants)).thenReturn(duels);

         final List<Duel> result = provider.getUnties(participants);

         assertThat(result).isEqualTo(duels);
         verify(duelRepository).findUntiesByParticipantIn(participants);
     }

     @Test
     public void testGetAllUntiesDelegatesToRepository() {
         final List<Duel> duels = List.of(new Duel(), new Duel());
         when(duelRepository.findAllUnties()).thenReturn(duels);

         final List<Duel> result = provider.getUnties();

         assertThat(result).isEqualTo(duels);
         verify(duelRepository).findAllUnties();
     }

     @Test
     public void testGetDurationAverageByParticipantReturnsValueWhenPresent() {
         final Participant participant = participant("P1");
         when(duelRepository.getDurationAverage(participant)).thenReturn(100L);

         final Long duration = provider.getDurationAverage(participant);

         assertThat(duration).isEqualTo(100L);
         verify(duelRepository).getDurationAverage(participant);
     }

     @Test
     public void testGetDurationAverageByTournamentDelegatesToRepository() {
         final Tournament tournament = tournament("Winter Cup");
         when(duelRepository.getDurationAverage(tournament)).thenReturn(50L);

         final Long duration = provider.getDurationAverage(tournament);

         assertThat(duration).isEqualTo(50L);
         verify(duelRepository).getDurationAverage(tournament);
     }

     @Test
     public void testGetFirstDuelDelegatesToRepository() {
         final Tournament tournament = tournament("Spring Cup");
         final Duel duel = new Duel();
         when(duelRepository.findFirstByTournamentOrderByStartedAtAsc(tournament)).thenReturn(duel);

         final Duel result = provider.getFirstDuel(tournament);

         assertThat(result).isEqualTo(duel);
         verify(duelRepository).findFirstByTournamentOrderByStartedAtAsc(tournament);
     }

     @Test
     public void testGetLastDuelDelegatesToRepository() {
         final Tournament tournament = tournament("Summer Cup");
         final Duel duel = new Duel();
         when(duelRepository.findFirstByTournamentOrderByFinishedAtDesc(tournament)).thenReturn(duel);

         final Duel result = provider.getLastDuel(tournament);

         assertThat(result).isEqualTo(duel);
         verify(duelRepository).findFirstByTournamentOrderByFinishedAtDesc(tournament);
     }

     @Test
     public void testCountScoreDelegatesToRepository() {
         final Tournament tournament = tournament("Fall Cup");
         when(duelRepository.countScore(tournament, List.of(Score.MEN))).thenReturn(7L);

         final Long count = provider.countScore(tournament, Score.MEN);

         assertThat(count).isEqualTo(7L);
         verify(duelRepository).countScore(tournament, List.of(Score.MEN));
     }

     @Test
     public void testFindByOnlyScoreRemovesScoreAndEmpty() {
         final Tournament tournament = tournament("Winter Cup");
         final java.util.Set<Duel> duels = java.util.Set.of(new Duel());
         when(duelRepository.findByOnlyScore(any(), any())).thenReturn(duels);

         final java.util.Set<Duel> result = provider.findByOnlyScore(tournament, Score.MEN);

         assertThat(result).isEqualTo(duels);
     }

     @Test
     public void testFindByScorePerformedInLessThanDelegatesToRepository() {
         final Tournament tournament = tournament("Spring Cup");
         final java.util.Set<Duel> duels = java.util.Set.of(new Duel());
         when(duelRepository.findByScoreOnTimeLess(tournament, 30)).thenReturn(duels);

         final java.util.Set<Duel> result = provider.findByScorePerformedInLessThan(tournament, 30);

         assertThat(result).isEqualTo(duels);
         verify(duelRepository).findByScoreOnTimeLess(tournament, 30);
     }

     @Test
     public void testFindByScoreDurationDelegatesToRepository() {
         final Tournament tournament = tournament("Summer Cup");
         final List<Duel> duels = List.of(new Duel());
         when(duelRepository.findByTournamentAndCompetitor1ScoreTimeLessThanEqualOrCompetitor2ScoreTimeLessThanEqual(
                 tournament, 60, 60)).thenReturn(duels);

         final List<Duel> result = provider.findByScoreDuration(tournament, 60);

         assertThat(result).isEqualTo(duels);
     }

     @Test
     public void testCountFaultsWithBothValuesPresent() {
         final Tournament tournament = tournament("Fall Cup");
         when(duelRepository.countFaultsByTournament(tournament, true)).thenReturn(5L);
         when(duelRepository.countScore(tournament, List.of(Score.HANSOKU))).thenReturn(3L);

         final long faults = provider.countFaults(tournament);

         assertThat(faults).isEqualTo(11L);
     }

     @Test
     public void testCountFaultsWithOnlyFaults() {
         final Tournament tournament = tournament("Winter Cup");
         when(duelRepository.countFaultsByTournament(tournament, true)).thenReturn(8L);
         when(duelRepository.countScore(tournament, List.of(Score.HANSOKU))).thenReturn(null);

         final long faults = provider.countFaults(tournament);

         assertThat(faults).isEqualTo(8L);
     }

     @Test
     public void testCountFaultsWithOnlyHansokus() {
         final Tournament tournament = tournament("Spring Cup");
         when(duelRepository.countFaultsByTournament(tournament, true)).thenReturn(null);
         when(duelRepository.countScore(tournament, List.of(Score.HANSOKU))).thenReturn(4L);

         final long faults = provider.countFaults(tournament);

         assertThat(faults).isEqualTo(8L);
     }

     @Test
     public void testCountScoreFromCompetitorAddsLeftAndRight() {
         final Participant participant = participant("P1");
         final Collection<Tournament> tournaments = List.of(tournament("Autumn Cup"));
         when(duelRepository.countLeftScoreFromCompetitor(participant, tournaments)).thenReturn(5L);
         when(duelRepository.countRightScoreFromCompetitor(participant, tournaments)).thenReturn(3L);

         final long count = provider.countScoreFromCompetitor(participant, tournaments);

         assertThat(count).isEqualTo(8L);
     }

     @Test
     public void testCountScoreFromCompetitorRightThrowsNullPointer() {
         final Participant participant = participant("P1");
         final Collection<Tournament> tournaments = List.of(tournament("Autumn Cup"));
         when(duelRepository.countLeftScoreFromCompetitor(participant, tournaments)).thenReturn(5L);
         when(duelRepository.countRightScoreFromCompetitor(participant, tournaments))
                 .thenThrow(new NullPointerException());

         final long count = provider.countScoreFromCompetitor(participant, tournaments);

         assertThat(count).isEqualTo(0L);
     }

     @Test
     public void testCountScoreAgainstCompetitorAddsLeftAndRight() {
         final Participant participant = participant("P1");
         final Collection<Tournament> tournaments = List.of(tournament("Autumn Cup"));
         when(duelRepository.countLeftScoreAgainstCompetitor(participant, tournaments)).thenReturn(2L);
         when(duelRepository.countRightScoreAgainstCompetitor(participant, tournaments)).thenReturn(4L);

         final long count = provider.countScoreAgainstCompetitor(participant, tournaments);

         assertThat(count).isEqualTo(6L);
     }

     @Test
     public void testCountScoreAgainstCompetitorRightThrowsNullPointer() {
         final Participant participant = participant("P1");
         final Collection<Tournament> tournaments = List.of(tournament("Autumn Cup"));
         when(duelRepository.countLeftScoreAgainstCompetitor(participant, tournaments)).thenReturn(2L);
         when(duelRepository.countRightScoreAgainstCompetitor(participant, tournaments))
                 .thenThrow(new NullPointerException());

         final long count = provider.countScoreAgainstCompetitor(participant, tournaments);

         assertThat(count).isEqualTo(0L);
     }

     @Test
     public void testGetUntiesFromParticipantDelegatesToRepository() {
         final Participant participant = participant("P1");
         final List<Duel> duels = List.of(new Duel());
         when(duelRepository.findUntiesByParticipantIn(List.of(participant))).thenReturn(duels);

         final List<Duel> result = provider.getUntiesFromParticipant(participant);

         assertThat(result).isEqualTo(duels);
         verify(duelRepository).findUntiesByParticipantIn(List.of(participant));
     }

     // ============= ParticipantFightStatisticsProvider Tests =============

     private Tournament tournament(String name) {
         final Tournament tournament = new Tournament(name, 2, 3, TournamentType.LEAGUE, "user");
         tournament.setId(Math.abs(name.hashCode()));
         return tournament;
     }

     private Participant participant(String name) {
         final Participant participant = new Participant();
         participant.setId(Math.abs(name.hashCode()));
         participant.setName(name);
         participant.setLastname("Lastname");
         participant.setCreatedBy("user");
         return participant;
     }
}


