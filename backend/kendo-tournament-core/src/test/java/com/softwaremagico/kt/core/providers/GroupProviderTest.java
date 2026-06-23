package com.softwaremagico.kt.core.providers;

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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

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

    private Tournament tournament() {
        final Tournament tournament = new Tournament("Tournament", 1, 3, TournamentType.LEAGUE, "tester");
        tournament.setId(99);
        return tournament;
    }
}

