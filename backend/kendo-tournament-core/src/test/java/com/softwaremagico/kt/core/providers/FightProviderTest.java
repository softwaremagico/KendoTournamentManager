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
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.FightRepository;
import com.softwaremagico.kt.persistence.repositories.GroupRepository;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

@Test(groups = {"fightProviderTests"})
public class FightProviderTest {

    @Mock
    private FightRepository fightRepository;

    @Mock
    private GroupRepository groupRepository;

    private FightProvider provider;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        provider = new FightProvider(fightRepository, groupRepository);
    }

    @Test
    public void shouldSetTournamentOnFightsWhenGettingByLevel() {
        final Tournament tournament = tournament();
        final Fight fight = Mockito.mock(Fight.class);
        when(fightRepository.findByTournamentAndLevel(tournament, 2)).thenReturn(List.of(fight));

        final List<Fight> result = provider.getFights(tournament, 2);

        assertEquals(result.size(), 1);
        assertSame(result.get(0), fight);
        verify(fight).setTournament(tournament);
    }

    @Test
    public void shouldSetTournamentOnFightAndTeamsWhenGettingByTournament() {
        final Tournament tournament = tournament();
        final Fight fight = Mockito.mock(Fight.class);
        final Team team1 = Mockito.mock(Team.class);
        final Team team2 = Mockito.mock(Team.class);

        when(fightRepository.findByTournament(tournament)).thenReturn(List.of(fight));
        when(fight.getTeam1()).thenReturn(team1);
        when(fight.getTeam2()).thenReturn(team2);

        final List<Fight> result = provider.getFights(tournament);

        assertEquals(result.size(), 1);
        verify(fight).setTournament(tournament);
        verify(team1).setTournament(tournament);
        verify(team2).setTournament(tournament);
    }

    @Test
    public void shouldReturnFirstFightWithAnyDuelNotOver() {
        final Tournament tournament = tournament();
        final Fight fightOver = Mockito.mock(Fight.class);
        final Fight fightNotOver = Mockito.mock(Fight.class);
        final Duel overDuel = Mockito.mock(Duel.class);
        final Duel notOverDuel = Mockito.mock(Duel.class);

        when(fightRepository.findByTournament(tournament)).thenReturn(List.of(fightOver, fightNotOver));
        when(fightOver.getTeam1()).thenReturn(Mockito.mock(Team.class));
        when(fightOver.getTeam2()).thenReturn(Mockito.mock(Team.class));
        when(fightNotOver.getTeam1()).thenReturn(Mockito.mock(Team.class));
        when(fightNotOver.getTeam2()).thenReturn(Mockito.mock(Team.class));
        when(fightOver.getDuels()).thenReturn(List.of(overDuel));
        when(fightNotOver.getDuels()).thenReturn(List.of(notOverDuel));
        when(overDuel.isOver()).thenReturn(true);
        when(notOverDuel.isOver()).thenReturn(false);

        final Fight result = provider.getFirstNotOver(tournament);

        assertSame(result, fightNotOver);
    }

    @Test
    public void shouldReturnNullWhenAllDuelsAreOver() {
        final Tournament tournament = tournament();
        final Fight fight = Mockito.mock(Fight.class);
        final Duel duel = Mockito.mock(Duel.class);

        when(fightRepository.findByTournament(tournament)).thenReturn(List.of(fight));
        when(fight.getTeam1()).thenReturn(Mockito.mock(Team.class));
        when(fight.getTeam2()).thenReturn(Mockito.mock(Team.class));
        when(fight.getDuels()).thenReturn(List.of(duel));
        when(duel.isOver()).thenReturn(true);

        final Fight result = provider.getFirstNotOver(tournament);

        assertNull(result);
        assertTrue(provider.areOver(tournament));
    }

    @Test
    public void shouldReturnCurrentFightAndSetTournamentOnFightAndTeams() {
        final Tournament tournament = tournament();
        final Fight fight = Mockito.mock(Fight.class);
        final Duel duel = Mockito.mock(Duel.class);
        final Team team1 = Mockito.mock(Team.class);
        final Team team2 = Mockito.mock(Team.class);

        when(fightRepository.findByTournament(tournament)).thenReturn(List.of(fight));
        when(fight.getDuels()).thenReturn(List.of(duel));
        when(duel.isOver()).thenReturn(false);
        when(fight.getTeam1()).thenReturn(team1);
        when(fight.getTeam2()).thenReturn(team2);

        final Fight current = provider.getCurrent(tournament);

        assertSame(current, fight);
        verify(fight, times(2)).setTournament(tournament);
        verify(team1, times(2)).setTournament(tournament);
        verify(team2, times(2)).setTournament(tournament);
    }

    @Test
    public void shouldDelegateGetByParticipants() {
        final Participant participant = Mockito.mock(Participant.class);
        final Fight fight = Mockito.mock(Fight.class);
        final Collection<Participant> participants = List.of(participant);
        when(fightRepository.findByParticipantIn(participants)).thenReturn(List.of(fight));

        final List<Fight> result = provider.getBy(participants);

        assertEquals(result.size(), 1);
        assertSame(result.get(0), fight);
    }

    @Test
    public void shouldClearGroupFightsAndDeleteTournamentFights() {
        final Tournament tournament = tournament();
        final Group group1 = Mockito.mock(Group.class);
        final Group group2 = Mockito.mock(Group.class);

        when(groupRepository.findByTournamentOrderByLevelAscIndexAsc(tournament)).thenReturn(List.of(group1, group2));

        provider.delete(tournament);

        verify(group1).setFights(any(ArrayList.class));
        verify(group2).setFights(any(ArrayList.class));
        verify(groupRepository).save(group1);
        verify(groupRepository).save(group2);
        verify(fightRepository).deleteByTournament(tournament);
    }

    @Test
    public void shouldClearGroupFightsFromLevelAndDeleteTournamentFightsFromLevel() {
        final Tournament tournament = tournament();
        final Group group = Mockito.mock(Group.class);

        when(groupRepository.findByTournamentAndLevelIsGreaterThanEqual(tournament, 3)).thenReturn(List.of(group));

        provider.delete(tournament, 3);

        verify(group).setFights(any(ArrayList.class));
        verify(groupRepository).save(group);
        verify(fightRepository).deleteByTournamentAndLevelGreaterThanEqual(tournament, 3);
    }

    @Test
    public void shouldDelegateCountMethods() {
        final Tournament tournament = tournament();
        when(fightRepository.countByTournament(tournament)).thenReturn(9L);
        when(fightRepository.countByTournamentAndFinishedNot(tournament, false)).thenReturn(6L);

        assertEquals(provider.count(tournament), 9L);
        assertEquals(provider.countByTournamentAndFinished(tournament), 6L);
    }

    @Test
    public void shouldReturnCurrentLevelWhenFightExists() {
        final Tournament tournament = tournament();
        final Fight fight = Mockito.mock(Fight.class);
        when(fightRepository.findFirstByTournamentOrderByLevelDesc(tournament)).thenReturn(Optional.of(fight));
        when(fight.getLevel()).thenReturn(4);

        final Integer result = provider.getCurrentLevel(tournament);

        assertEquals(result.intValue(), 4);
    }

    @Test
    public void shouldReturnZeroCurrentLevelWhenNoFightExists() {
        final Tournament tournament = tournament();
        when(fightRepository.findFirstByTournamentOrderByLevelDesc(tournament)).thenReturn(Optional.empty());

        final Integer result = provider.getCurrentLevel(tournament);

        assertEquals(result.intValue(), 0);
    }

    @Test
    public void shouldDeleteFightAndRemoveItFromGroupWhenGroupExists() {
        final Fight fight = Mockito.mock(Fight.class);
        final Group group = Mockito.mock(Group.class);
        final List<Fight> groupFights = new ArrayList<>();
        groupFights.add(fight);

        when(fight.getId()).thenReturn(12);
        when(groupRepository.findByFightsId(12)).thenReturn(Optional.of(group));
        when(group.getFights()).thenReturn(groupFights);

        provider.delete(fight);

        assertTrue(groupFights.isEmpty());
        verify(groupRepository).save(group);
        verify(fightRepository).delete(fight);
    }

    @Test
    public void shouldDeleteFightWithoutGroupWhenNoGroupFound() {
        final Fight fight = Mockito.mock(Fight.class);
        when(fight.getId()).thenReturn(15);
        when(groupRepository.findByFightsId(15)).thenReturn(Optional.empty());

        provider.delete(fight);

        verify(groupRepository, never()).save(any(Group.class));
        verify(fightRepository).delete(fight);
    }

    @Test
    public void shouldNotDeleteWhenFightIsNull() {
        provider.delete((Fight) null);

        verify(fightRepository, never()).delete(any(Fight.class));
    }

    @Test
    public void shouldDeleteFightCollectionAndRemoveFromGroups() {
        final Fight fight1 = Mockito.mock(Fight.class);
        final Fight fight2 = Mockito.mock(Fight.class);
        when(fight1.getId()).thenReturn(1);
        when(fight2.getId()).thenReturn(2);

        final Group group = Mockito.mock(Group.class);
        final List<Fight> groupFights = new ArrayList<>(List.of(fight1, fight2, Mockito.mock(Fight.class)));
        when(group.getFights()).thenReturn(groupFights);
        when(groupRepository.findDistinctByFightsIdIn(Set.of(1, 2))).thenReturn(List.of(group));

        final List<Fight> fightsToDelete = List.of(fight1, fight2);
        provider.delete(fightsToDelete);

        assertEquals(groupFights.size(), 1);
        verify(groupRepository).saveAll(List.of(group));
        verify(fightRepository).deleteAll(fightsToDelete);
    }

    @Test
    public void shouldNotDeleteFightCollectionWhenNull() {
        provider.delete((Collection<Fight>) null);

        verify(groupRepository, never()).saveAll(any());
        verify(fightRepository, never()).deleteAll(any(Collection.class));
    }

    @Test
    public void shouldDelegateFindByDuelsAndByShiaijo() {
        final Duel duel = Mockito.mock(Duel.class);
        final Fight fight = Mockito.mock(Fight.class);
        final Tournament tournament = tournament();

        when(fightRepository.findByDuels(duel)).thenReturn(Optional.of(fight));
        when(fightRepository.findByTournamentAndShiaijo(tournament, 2)).thenReturn(List.of(fight));

        final Optional<Fight> byDuel = provider.findByDuels(duel);
        final List<Fight> byShiaijo = provider.findByTournamentAndShiaijo(tournament, 2);

        assertTrue(byDuel.isPresent());
        assertSame(byDuel.get(), fight);
        assertEquals(byShiaijo.size(), 1);
        assertSame(byShiaijo.get(0), fight);
    }

    @Test
    public void shouldReturnTrueWhenScoresGoFromNameToCenter() {
        final Tournament tournament = tournament();
        final Fight fight = Mockito.mock(Fight.class);
        final Duel duel = Mockito.mock(Duel.class);

        final List<Score> competitor1Scores = new ArrayList<>(Arrays.asList(null, Score.MEN));
        final List<Score> competitor2Scores = List.of(Score.KOTE);

        when(fightRepository.findByTournament(tournament)).thenReturn(List.of(fight));
        when(fight.getDuels()).thenReturn(List.of(duel));
        when(duel.getCompetitor1Score()).thenReturn(competitor1Scores);
        when(duel.getCompetitor2Score()).thenReturn(competitor2Scores);

        assertTrue(provider.scoresGoesFromCompetitorsNameToCenter(tournament));
    }

    @Test
    public void shouldReturnFalseWhenScoresDoNotGoFromNameToCenter() {
        final Tournament tournament = tournament();
        final Fight fight = Mockito.mock(Fight.class);
        final Duel duel = Mockito.mock(Duel.class);

        when(fightRepository.findByTournament(tournament)).thenReturn(List.of(fight));
        when(fight.getDuels()).thenReturn(List.of(duel));
        when(duel.getCompetitor1Score()).thenReturn(List.of(Score.MEN));
        when(duel.getCompetitor2Score()).thenReturn(List.of(Score.KOTE));

        assertFalse(provider.scoresGoesFromCompetitorsNameToCenter(tournament));
    }

    @Test
    public void shouldReturnFalseWhenDuelListIsNull() {
        final Tournament tournament = tournament();
        final Fight fight = Mockito.mock(Fight.class);
        when(fightRepository.findByTournament(tournament)).thenReturn(List.of(fight));
        when(fight.getDuels()).thenReturn(null);

        assertFalse(provider.scoresGoesFromCompetitorsNameToCenter(tournament));
    }

    private Tournament tournament() {
        final Tournament tournament = new Tournament("T", 1, 3, TournamentType.LEAGUE, "tester");
        tournament.setId(99);
        return tournament;
    }
}
