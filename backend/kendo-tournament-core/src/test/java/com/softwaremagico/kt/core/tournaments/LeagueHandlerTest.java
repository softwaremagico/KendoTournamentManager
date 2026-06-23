package com.softwaremagico.kt.core.tournaments;

import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.RankingProvider;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.core.providers.TournamentExtraPropertyProvider;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.values.LeagueFightsOrder;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

@Test(groups = {"leagueTest"})
public class LeagueHandlerTest {

    @Mock
    private GroupProvider groupProvider;

    @Mock
    private TeamProvider teamProvider;

    @Mock
    private RankingProvider rankingProvider;

    @Mock
    private TournamentExtraPropertyProvider tournamentExtraPropertyProvider;

    private TestLeagueHandler handler;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new TestLeagueHandler(groupProvider, teamProvider, rankingProvider, tournamentExtraPropertyProvider);
    }

    @Test
    public void shouldReturnFirstGroupWhenItAlreadyExists() {
        final Tournament tournament = tournament();
        final Group existingGroup = new Group(tournament, 0, 0);

        when(groupProvider.getGroups(tournament)).thenReturn(List.of(existingGroup));

        final Group result = handler.getFirstGroup(tournament);

        assertSame(result, existingGroup);
        verify(teamProvider, never()).getAll(tournament);
    }

    @Test
    public void shouldCreateFirstGroupWhenNoGroupsExist() {
        final Tournament tournament = tournament();
        final Team team1 = new Team("team-1", tournament);
        final Team team2 = new Team("team-2", tournament);

        when(groupProvider.getGroups(tournament)).thenReturn(List.of());
        when(teamProvider.getAll(tournament)).thenReturn(List.of(team1, team2));
        when(groupProvider.addGroup(eq(tournament), any(Group.class))).thenAnswer(invocation -> invocation.getArgument(1));

        final Group result = handler.getFirstGroup(tournament);

        assertEquals(result.getTournament(), tournament);
        assertEquals(result.getLevel(), 0);
        assertEquals(result.getIndex(), 0);
        assertEquals(result.getTeams().size(), 2);
        verify(groupProvider).addGroup(eq(tournament), any(Group.class));
    }

    @Test
    public void shouldCreateLeagueFightOrderWhenMissing() {
        final Tournament tournament = tournament();
        when(tournamentExtraPropertyProvider.getByTournamentAndProperty(tournament,
                TournamentExtraPropertyKey.LEAGUE_FIGHTS_ORDER_GENERATION)).thenReturn(null);
        when(tournamentExtraPropertyProvider.save(any(TournamentExtraProperty.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final TournamentExtraProperty property = handler.getLeagueFightsOrder(tournament);

        assertEquals(property.getPropertyKey(), TournamentExtraPropertyKey.LEAGUE_FIGHTS_ORDER_GENERATION);
        assertEquals(property.getPropertyValue(), LeagueFightsOrder.FIFO.name());
        verify(tournamentExtraPropertyProvider).save(any(TournamentExtraProperty.class));
    }

    @Test
    public void shouldRemoveGroupOnlyForRootLeagueGroup() {
        final Tournament tournament = tournament();

        handler.removeGroup(tournament, 0, 0);

        verify(groupProvider).delete(tournament);
    }

    @Test
    public void shouldIgnoreRemovalForNonRootGroup() {
        final Tournament tournament = tournament();

        handler.removeGroup(tournament, 1, 0);

        verify(groupProvider, never()).delete(tournament);
    }

    @Test
    public void shouldClearTeamsWhenRemovingTeamsFromLevel() {
        final Tournament tournament = tournament();
        final Group group = new Group(tournament, 0, 0);
        group.setTeams(new ArrayList<>(List.of(new Team("team-1", tournament))));

        when(groupProvider.getGroups(tournament)).thenReturn(List.of(group));
        when(groupProvider.save(group)).thenAnswer(invocation -> invocation.getArgument(0));

        handler.removeTeams(tournament, 1);

        assertTrue(group.getTeams().isEmpty());
        verify(groupProvider).save(group);
    }

    @Test
    public void shouldReturnTrueWhenTeamExistsInGroup() {
        final Tournament tournament = tournament();
        final Team team = new Team("team-1", tournament);
        final Group group = new Group(tournament, 0, 0);
        group.setTeams(new ArrayList<>(List.of(team)));

        when(groupProvider.getGroups(tournament)).thenReturn(List.of(group));

        assertTrue(handler.exist(tournament, team));
    }

    @Test
    public void shouldReturnFalseWhenTeamDoesNotExist() {
        final Tournament tournament = tournament();
        final Team team = new Team("team-1", tournament);

        when(groupProvider.getGroups(tournament)).thenReturn(List.of());

        assertFalse(handler.exist(tournament, team));
    }

    @Test
    public void shouldReturnTrueForLastFightWhenPreviousFightIsOver() {
        final Tournament tournament = tournament();
        final Group group = new Group(tournament, 0, 0);
        final Fight firstFight = new Fight();
        final com.softwaremagico.kt.persistence.entities.Duel finishedDuel = new com.softwaremagico.kt.persistence.entities.Duel();
        finishedDuel.setFinished(true);
        firstFight.setDuels(new ArrayList<>(List.of(finishedDuel)));
        final Fight secondFight = new Fight();
        secondFight.setDuels(new ArrayList<>(List.of(new com.softwaremagico.kt.persistence.entities.Duel())));
        group.setFights(new ArrayList<>(List.of(firstFight, secondFight)));
        group.setTeams(new ArrayList<>(List.of(new Team("team-1", tournament), new Team("team-2", tournament))));

        when(groupProvider.getGroups(tournament)).thenReturn(List.of(group));

        assertTrue(handler.isTheLastFight(tournament));
    }

    @Test
    public void shouldReturnFalseForLastFightWhenPreviousFightIsNotOver() {
        final Tournament tournament = tournament();
        final Group group = new Group(tournament, 0, 0);
        final Fight firstFight = new Fight();
        firstFight.setDuels(new ArrayList<>(List.of(new com.softwaremagico.kt.persistence.entities.Duel())));
        final Fight secondFight = new Fight();
        secondFight.setDuels(new ArrayList<>(List.of(new com.softwaremagico.kt.persistence.entities.Duel())));
        group.setFights(new ArrayList<>(List.of(firstFight, secondFight)));
        group.setTeams(new ArrayList<>(List.of(new Team("team-1", tournament), new Team("team-2", tournament))));

        when(groupProvider.getGroups(tournament)).thenReturn(List.of(group));

        assertFalse(handler.isTheLastFight(tournament));
    }

    @Test
    public void shouldReturnTrueWhenDrawTeamsArePresent() {
        final Group group = new Group(tournament(), 0, 0);
        when(rankingProvider.getFirstTeamsWithDrawScore(group, 1)).thenReturn(List.of(new Team("team-1", tournament())));

        assertTrue(handler.hasDrawScore(group));
    }

    @Test
    public void shouldReturnFalseWhenNoDrawTeamsArePresent() {
        final Group group = new Group(tournament(), 0, 0);
        when(rankingProvider.getFirstTeamsWithDrawScore(group, 1)).thenReturn(null);

        assertFalse(handler.hasDrawScore(group));
    }

    @Test
    public void shouldGenerateNoFightsForNextRoundInLeague() {
        final Tournament tournament = tournament();

        assertTrue(handler.generateNextFights(tournament, "user").isEmpty());
    }

    @Test
    public void shouldCreateInitialFightsUsingLevelZero() {
        final Tournament tournament = tournament();

        assertTrue(handler.createInitialFights(tournament, TeamsOrder.NONE, "user").isEmpty());
    }

    private Tournament tournament() {
        final Tournament tournament = new Tournament("Tournament", 1, 3, TournamentType.LEAGUE, "tester");
        tournament.setId(202);
        return tournament;
    }

    private static class TestLeagueHandler extends LeagueHandler {
        protected TestLeagueHandler(GroupProvider groupProvider, TeamProvider teamProvider, RankingProvider rankingProvider,
                                    TournamentExtraPropertyProvider tournamentExtraPropertyProvider) {
            super(groupProvider, teamProvider, rankingProvider, tournamentExtraPropertyProvider);
        }

        @Override
        public List<Fight> createFights(Tournament tournament, TeamsOrder teamsOrder, Integer level, String createdBy) {
            return new ArrayList<>();
        }
    }
}


