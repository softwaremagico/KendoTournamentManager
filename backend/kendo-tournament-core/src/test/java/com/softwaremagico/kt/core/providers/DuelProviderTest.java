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


