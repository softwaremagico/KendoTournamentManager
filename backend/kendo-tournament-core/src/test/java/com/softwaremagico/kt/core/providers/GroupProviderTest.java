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

import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.DuelRepository;
import com.softwaremagico.kt.persistence.repositories.FightRepository;
import com.softwaremagico.kt.persistence.repositories.GroupLinkRepository;
import com.softwaremagico.kt.persistence.repositories.GroupRepository;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

@Test(groups = {"groupsTest"})
public class GroupProviderTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private FightRepository fightRepository;

    @Mock
    private DuelRepository duelRepository;

    @Mock
    private GroupLinkRepository groupLinkRepository;

    private GroupProvider provider;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        provider = new GroupProvider(groupRepository, fightRepository, duelRepository, groupLinkRepository);
    }

    @Test
    public void shouldUseDefaultLevelAndIndexWhenFindingGroupByLevelAndIndex() {
        final Tournament tournament = tournament();
        final Group group = new Group(tournament, 0, 0);

        when(groupRepository.findByTournamentAndLevelAndIndex(tournament, 0, 0)).thenReturn(group);

        final Group result = provider.getGroupByLevelAndIndex(tournament, null, null);

        assertSame(result, group);
        verify(groupRepository).findByTournamentAndLevelAndIndex(tournament, 0, 0);
    }

    @Test
    public void shouldDeleteGroupByLevelAndIndexUsingDefaultValues() {
        final Tournament tournament = tournament();
        when(groupRepository.deleteByTournamentAndLevelAndIndex(tournament, 0, 0)).thenReturn(1);

        final boolean deleted = provider.deleteGroupByLevelAndIndex(tournament, null, null);

        assertTrue(deleted);
        verify(groupLinkRepository).deleteByTournament(tournament);
        verify(groupRepository).deleteByTournamentAndLevelAndIndex(tournament, 0, 0);
    }

    @Test
    public void shouldUseProvidedLevelAndDefaultIndexWhenFindingGroupByLevelAndIndex() {
        final Tournament tournament = tournament();
        final Group group = new Group(tournament, 2, 0);

        when(groupRepository.findByTournamentAndLevelAndIndex(tournament, 2, 0)).thenReturn(group);

        final Group result = provider.getGroupByLevelAndIndex(tournament, 2, null);

        assertSame(result, group);
        verify(groupRepository).findByTournamentAndLevelAndIndex(tournament, 2, 0);
    }

    @Test
    public void shouldUseDefaultLevelAndProvidedIndexWhenFindingGroupByLevelAndIndex() {
        final Tournament tournament = tournament();
        final Group group = new Group(tournament, 0, 4);

        when(groupRepository.findByTournamentAndLevelAndIndex(tournament, 0, 4)).thenReturn(group);

        final Group result = provider.getGroupByLevelAndIndex(tournament, null, 4);

        assertSame(result, group);
        verify(groupRepository).findByTournamentAndLevelAndIndex(tournament, 0, 4);
    }

    @Test
    public void shouldReturnFalseWhenDeleteGroupByLevelAndIndexDeletesNothing() {
        final Tournament tournament = tournament();
        when(groupRepository.deleteByTournamentAndLevelAndIndex(tournament, 0, 0)).thenReturn(0);

        final boolean deleted = provider.deleteGroupByLevelAndIndex(tournament, null, null);

        assertTrue(!deleted);
    }

    @Test
    public void shouldUpdateExistingGroupWhenAddingGroup() {
        final Tournament tournament = tournament();
        final Group existing = new Group(tournament, 0, 0);
        existing.setId(10);
        existing.setUpdatedBy("old-user");
        existing.setTeams(new ArrayList<>(List.of(new Team("old-team", tournament))));
        existing.setFights(new ArrayList<>(List.of(new Fight())));
        existing.setUnties(new ArrayList<>(List.of(new Duel())));

        final Group incoming = new Group(tournament, 2, 3);
        incoming.setId(10);
        incoming.setUpdatedBy("new-user");
        incoming.setTeams(new ArrayList<>(List.of(new Team("new-team", tournament))));
        incoming.setFights(new ArrayList<>(List.of(new Fight())));
        incoming.setUnties(new ArrayList<>(List.of(new Duel())));
        incoming.setShiaijo(4);
        incoming.setNumberOfWinners(2);

        when(groupRepository.findById(10)).thenReturn(Optional.of(existing));
        when(groupRepository.save(existing)).thenAnswer(invocation -> invocation.getArgument(0));

        final Group result = provider.addGroup(tournament, incoming);

        assertSame(result, existing);
        assertEquals(existing.getTournament(), tournament);
        assertEquals(existing.getLevel(), 2);
        assertEquals(existing.getIndex(), 3);
        assertEquals(existing.getShiaijo(), 4);
        assertEquals(existing.getNumberOfWinners(), 2);
        assertEquals(existing.getTeams().get(0).getName(), "new-team");
        assertEquals(existing.getUpdatedBy(), "new-user");
        verify(groupLinkRepository).deleteByTournament(tournament);
        verify(groupRepository).save(existing);
    }

    @Test
    public void shouldKeepPreviousUpdatedByWhenIncomingUpdatedByIsNull() {
        final Tournament tournament = tournament();
        final Group existing = new Group(tournament, 0, 0);
        existing.setId(11);
        existing.setUpdatedBy("old-user");

        final Group incoming = new Group(tournament, 1, 1);
        incoming.setId(11);
        incoming.setUpdatedBy(null);
        incoming.setTeams(new ArrayList<>());
        incoming.setFights(new ArrayList<>());
        incoming.setUnties(new ArrayList<>());

        when(groupRepository.findById(11)).thenReturn(Optional.of(existing));
        when(groupRepository.save(existing)).thenAnswer(invocation -> invocation.getArgument(0));

        final Group result = provider.addGroup(tournament, incoming);

        assertSame(result, existing);
        assertEquals(existing.getUpdatedBy(), "old-user");
    }

    @Test
    public void shouldSetTeamsAndRebuildRelatedCollections() {
        final Tournament tournament = tournament();
        final Group group = new Group(tournament, 0, 0);
        group.setId(1);
        group.setTeams(new ArrayList<>(List.of(new Team("old-team", tournament))));
        group.setFights(new ArrayList<>(List.of(new Fight())));
        group.setUnties(new ArrayList<>(List.of(new Duel())));

        final Team team1 = new Team("team-1", tournament);
        final Team team2 = new Team("team-2", tournament);

        when(groupRepository.findById(1)).thenReturn(Optional.of(group));
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(fightRepository).deleteAll(any());
        doNothing().when(duelRepository).deleteAll(any());

        final Group result = provider.setTeams(1, List.of(team1, team2), "alice");

        assertSame(result, group);
        assertEquals(result.getTeams().size(), 2);
        assertEquals(result.getTeams().get(0).getName(), "team-1");
        assertEquals(result.getTeams().get(1).getName(), "team-2");
        assertEquals(result.getUpdatedBy(), "alice");
        verify(groupRepository, times(2)).save(group);
        verify(fightRepository).deleteAll(any());
        verify(duelRepository).deleteAll(any());
        verify(groupRepository).findById(1);
    }

    @Test
    public void shouldDeleteLevelZeroWhenThereAreNoGroupsAtLevelZero() {
        final Tournament tournament = tournament();
        final Group levelOne = new Group(tournament, 1, 0);

        when(groupRepository.findByTournamentOrderByLevelAscIndexAsc(tournament)).thenReturn(new ArrayList<>(List.of(levelOne)));
        when(groupRepository.deleteByTournamentAndLevel(tournament, 0)).thenReturn(2L);

        final long deleted = provider.delete(tournament, 1);

        assertEquals(deleted, 2L);
        verify(groupLinkRepository).deleteByTournament(tournament);
        verify(groupRepository).deleteByTournamentAndLevel(tournament, 0);
    }

    @Test
    public void shouldDeleteUnbalancedGroupsAtHigherLevelsWhenOneWinnerPerGroup() {
        final Tournament tournament = tournament();
        final Group level0 = new Group(tournament, 0, 0);
        level0.setNumberOfWinners(1);

        final Group level1a = new Group(tournament, 1, 0);
        final Group level1b = new Group(tournament, 1, 1);
        final Group level1c = new Group(tournament, 1, 2);

        when(groupRepository.findByTournamentOrderByLevelAscIndexAsc(tournament))
                .thenReturn(new ArrayList<>(List.of(level0, level1a, level1b, level1c)));
        doNothing().when(groupRepository).delete(any(Group.class));

        final long deleted = provider.delete(tournament, 1);

        // For previous level size=1, level 1 is reduced to zero in this scenario.
        assertEquals(deleted, 3L);
        verify(groupRepository, times(3)).delete(any(Group.class));
    }

    @Test
    public void shouldDecreaseOnlyLevelOneWhenTwoWinnersConfigured() {
        final Tournament tournament = tournament();
        final Group level0a = new Group(tournament, 0, 0);
        final Group level0b = new Group(tournament, 0, 1);
        level0a.setNumberOfWinners(2);
        level0b.setNumberOfWinners(2);

        final Group level1a = new Group(tournament, 1, 0);
        final Group level1b = new Group(tournament, 1, 1);
        final Group level1c = new Group(tournament, 1, 2);
        final Group level1d = new Group(tournament, 1, 3);

        when(groupRepository.findByTournamentOrderByLevelAscIndexAsc(tournament))
                .thenReturn(new ArrayList<>(List.of(level0a, level0b, level1a, level1b, level1c, level1d)));
        doNothing().when(groupRepository).delete(any(Group.class));

        final long deleted = provider.delete(tournament, 1);

        // With 2 winners, only the specific level-1 reduction branch applies.
        assertEquals(deleted, 1L);
        verify(groupRepository, times(1)).delete(any(Group.class));
    }

    @Test
    public void shouldPersistAsNewWhenGroupIdExistsButRowWasDeleted() {
        final Tournament tournament = tournament();
        final Group incoming = new Group(tournament, 2, 2);
        incoming.setId(33);
        incoming.setVersion(99);

        when(groupRepository.findById(33)).thenReturn(Optional.empty());
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final Group saved = provider.addGroup(tournament, incoming);

        assertNull(saved.getId());
        assertNull(saved.getVersion());
        assertEquals(saved.getTournament(), tournament);
        verify(groupRepository).save(incoming);
    }

    @Test
    public void shouldDeleteGroupWhenTournamentMatches() {
        final Tournament tournament = tournament();
        final Group group = new Group(tournament, 2, 3);

        when(groupRepository.deleteByTournamentAndLevelAndIndex(tournament, 2, 3)).thenReturn(1);

        provider.delete(tournament, group);

        verify(groupLinkRepository, times(2)).deleteByTournament(tournament);
        verify(groupRepository).deleteByTournamentAndLevelAndIndex(tournament, 2, 3);
    }

    @Test
    public void shouldNotDeleteGroupWhenTournamentDoesNotMatch() {
        final Tournament tournament = tournament();
        final Tournament otherTournament = new Tournament("Other", 1, 3, TournamentType.LEAGUE, "tester");
        final Group group = new Group(otherTournament, 2, 3);

        provider.delete(tournament, group);

        verify(groupLinkRepository).deleteByTournament(tournament);
        verify(groupRepository, never()).deleteByTournamentAndLevelAndIndex(any(), anyInt(), anyInt());
    }

    @Test
    public void shouldAddOnlyMissingTeamsToGroup() {
        final Tournament tournament = tournament();
        final Group group = new Group(tournament, 0, 0);
        group.setId(1);
        final Team existing = new Team("existing", tournament);
        existing.setId(1);
        final Team newTeam = new Team("new", tournament);
        newTeam.setId(2);
        group.setTeams(new ArrayList<>(List.of(existing)));

        when(groupRepository.findById(1)).thenReturn(Optional.of(group));
        when(groupRepository.save(group)).thenReturn(group);

        final Group updated = provider.addTeams(1, List.of(existing, newTeam), "user");

        assertEquals(updated.getTeams().size(), 2);
        assertTrue(updated.getTeams().contains(existing));
        assertTrue(updated.getTeams().contains(newTeam));
        assertEquals(updated.getUpdatedBy(), "user");
    }

    @Test
    public void shouldDeleteTeamsFromTournamentOnlyWhenPresent() {
        final Tournament tournament = tournament();
        final Team sharedTeam = new Team("shared", tournament);
        sharedTeam.setId(100);
        final Group g1 = new Group(tournament, 0, 0);
        g1.setTeams(new ArrayList<>(List.of(sharedTeam)));
        final Group g2 = new Group(tournament, 0, 1);
        final Team otherTeam = new Team("other", tournament);
        otherTeam.setId(101);
        g2.setTeams(new ArrayList<>(List.of(otherTeam)));

        when(groupRepository.findByTournamentOrderByLevelAscIndexAsc(tournament)).thenReturn(new ArrayList<>(List.of(g1, g2)));
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final List<Group> updated = provider.deleteTeams(tournament, List.of(sharedTeam), "tester");

        assertEquals(updated.size(), 1);
        assertSame(updated.get(0), g1);
        verify(groupRepository, times(1)).save(any(Group.class));
    }

    @Test
    public void shouldDeleteAllTeamsOnlyFromNonEmptyGroups() {
        final Tournament tournament = tournament();
        final Group nonEmpty = new Group(tournament, 0, 0);
        nonEmpty.setTeams(new ArrayList<>(List.of(new Team("t1", tournament))));
        final Group empty = new Group(tournament, 0, 1);
        empty.setTeams(new ArrayList<>());

        when(groupRepository.findByTournamentOrderByLevelAscIndexAsc(tournament)).thenReturn(new ArrayList<>(List.of(nonEmpty, empty)));
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final List<Group> updated = provider.deleteTeams(tournament, "tester");

        assertEquals(updated.size(), 1);
        assertSame(updated.get(0), nonEmpty);
        assertTrue(nonEmpty.getTeams().isEmpty());
        verify(groupRepository, times(1)).save(any(Group.class));
    }

    @Test
    public void shouldThrowWhenSettingEmptyTeams() {
        Assert.assertThrows(RuntimeException.class, () -> provider.setTeams(1, List.of(), "tester"));
    }

    private Tournament tournament() {
        final Tournament tournament = new Tournament("Tournament", 1, 3, TournamentType.LEAGUE, "tester");
        tournament.setId(99);
        return tournament;
    }
}

