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

import com.softwaremagico.kt.core.score.CompetitorRanking;
import com.softwaremagico.kt.core.score.ScoreOfCompetitor;
import com.softwaremagico.kt.core.score.ScoreOfTeam;
import com.softwaremagico.kt.persistence.entities.Club;
import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.TournamentRepository;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.ScoreType;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test(groups = {"scoreTests"})
public class RankingProviderTest {

    @Mock
    private FightProvider fightProvider;
    @Mock
    private DuelProvider duelProvider;
    @Mock
    private ParticipantProvider participantProvider;
    @Mock
    private TournamentRepository tournamentRepository;
    @Mock
    private GroupProvider groupProvider;
    @Mock
    private RoleProvider roleProvider;
    @Mock
    private TeamProvider teamProvider;

    private RankingProvider provider;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        provider = new RankingProvider(fightProvider, duelProvider, participantProvider, tournamentRepository,
                groupProvider, roleProvider, teamProvider);
    }

    @Test
    public void shouldFilterCompetitorsWithoutRecentFightsWhenNoCompetitorListIsProvided() {
        final Participant p1 = participant(1, "A", "One");
        final Participant p2 = participant(2, "B", "Two");
        final Participant p3 = participant(3, "C", "Three");

        final Fight recentFight = fight(List.of(p1), List.of(p2), LocalDateTime.now().minusDays(2));
        final Fight oldFight = fight(List.of(p2), List.of(p3), LocalDateTime.now().minusDays(90));

        when(participantProvider.getAll()).thenReturn(new ArrayList<>(List.of(p1, p2, p3)));
        when(fightProvider.getBy(any(Collection.class))).thenReturn(List.of(recentFight, oldFight));
        when(duelProvider.getUnties(any(Collection.class))).thenReturn(List.of());

        final List<ScoreOfCompetitor> ranking = provider.getCompetitorsGlobalScoreRanking(null, ScoreType.DEFAULT, 30);

        assertEquals(ranking.size(), 2);
        final List<Participant> competitors = ranking.stream().map(ScoreOfCompetitor::getCompetitor).toList();
        assertTrue(competitors.contains(p1));
        assertTrue(competitors.contains(p2));
        assertTrue(!competitors.contains(p3));
    }

    @Test
    public void shouldReturnLastPositionWhenCompetitorIsNotFoundInGlobalRanking() {
        final RankingProvider spyProvider = Mockito.spy(provider);
        final Participant p1 = participant(1, "A", "One");
        final Participant p2 = participant(2, "B", "Two");
        final Participant missing = participant(3, "C", "Three");

        final ScoreOfCompetitor score1 = new ScoreOfCompetitor();
        score1.setCompetitor(p1);
        final ScoreOfCompetitor score2 = new ScoreOfCompetitor();
        score2.setCompetitor(p2);

        doReturn(List.of(score1, score2)).when(spyProvider).getCompetitorGlobalRanking(ScoreType.DEFAULT);

        final CompetitorRanking competitorRanking = spyProvider.getCompetitorRanking(missing);

        assertEquals(competitorRanking.getTotal(), 2);
        assertEquals(competitorRanking.getRanking(), 1);
    }

    @Test
    public void shouldGroupTeamsByPositionWhenScoresAreTied() {
        final RankingProvider spyProvider = Mockito.spy(provider);
        final Tournament tournament = tournament(TournamentType.LEAGUE);
        final Group group = new Group(tournament, 0, 0);

        final Team teamA = team(1, "A", tournament);
        final Team teamB = team(2, "B", tournament);
        final Team teamC = team(3, "C", tournament);
        final Team teamD = team(4, "D", tournament);

        final ScoreOfTeam scoreA = score(teamA, 3, 0, 6, 12, 0, 0);
        final ScoreOfTeam scoreB = score(teamB, 2, 0, 4, 8, 0, 0);
        final ScoreOfTeam scoreC = score(teamC, 2, 0, 4, 8, 0, 0);
        final ScoreOfTeam scoreD = score(teamD, 1, 0, 2, 4, 0, 0);

        doReturn(List.of(scoreA, scoreB, scoreC, scoreD)).when(spyProvider).getTeamsScoreRanking(group);

        final Map<Integer, List<Team>> teamsByPosition = spyProvider.getTeamsByPosition(group);

        assertEquals(teamsByPosition.size(), 3);
        assertEquals(teamsByPosition.get(0), List.of(teamA));
        assertEquals(teamsByPosition.get(1), List.of(teamB, teamC));
        assertEquals(teamsByPosition.get(2), List.of(teamD));
    }

    @Test
    public void shouldCountNotOverFightsOnlyForKingOfTheMountain() {
        final Participant p1 = participant(11, "Ken", "Do");
        final Participant p2 = participant(12, "Ryu", "Gi");

        final Fight notFinishedFight = fight(List.of(p1), List.of(p2), LocalDateTime.now().minusDays(1));
        notFinishedFight.getDuels().get(0).addCompetitor1Score(Score.MEN);

        final Tournament leagueTournament = tournament(TournamentType.LEAGUE);
        final Tournament kingTournament = tournament(TournamentType.KING_OF_THE_MOUNTAIN);

        final List<ScoreOfCompetitor> leagueRanking = provider.getCompetitorsScoreRanking(List.of(p1, p2),
                List.of(notFinishedFight), List.of(), leagueTournament);
        final List<ScoreOfCompetitor> kingRanking = provider.getCompetitorsScoreRanking(List.of(p1, p2),
                List.of(notFinishedFight), List.of(), kingTournament);

        final ScoreOfCompetitor leagueP1 = leagueRanking.stream()
                .filter(score -> score.getCompetitor().equals(p1)).findFirst().orElseThrow();
        final ScoreOfCompetitor kingP1 = kingRanking.stream()
                .filter(score -> score.getCompetitor().equals(p1)).findFirst().orElseThrow();

        assertEquals(leagueP1.getWonDuels().intValue(), 0);
        assertEquals(kingP1.getWonDuels().intValue(), 1);
    }

    private Tournament tournament(TournamentType type) {
        final Tournament tournament = new Tournament("T", 1, 1, type, "tester", ScoreType.INTERNATIONAL);
        tournament.setId(100);
        return tournament;
    }

    private Participant participant(int id, String name, String lastname) {
        final Club club = new Club("Club " + id, "ES", "City");
        club.setId(id);
        final Participant participant = new Participant("ID" + id, name, lastname, club);
        participant.setId(id);
        return participant;
    }

    private Team team(int id, String name, Tournament tournament) {
        final Team team = new Team(name, tournament);
        team.setId(id);
        return team;
    }

    private Fight fight(List<Participant> members1, List<Participant> members2, LocalDateTime createdAt) {
        final Tournament tournament = tournament(TournamentType.LEAGUE);
        final Team team1 = team(200 + members1.get(0).getId(), "T1-" + members1.get(0).getId(), tournament);
        final Team team2 = team(300 + members2.get(0).getId(), "T2-" + members2.get(0).getId(), tournament);
        team1.setMembers(new ArrayList<>(members1));
        team2.setMembers(new ArrayList<>(members2));
        final Fight fight = new Fight(tournament, team1, team2, 0, 0, "tester");
        fight.setCreatedAt(createdAt);
        return fight;
    }

    private ScoreOfTeam score(Team team, int wonFights, int drawFights, int wonDuels, int hits, int unties, int level) {
        final ScoreOfTeam score = new ScoreOfTeam();
        score.setTeam(team);
        score.setWonFights(wonFights);
        score.setDrawFights(drawFights);
        score.setWonDuels(wonDuels);
        score.setHits(hits);
        score.setUntieDuels(unties);
        score.setLevel(level);
        return score;
    }
}

