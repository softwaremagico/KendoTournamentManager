package com.softwaremagico.kt.core.controller.models;

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

import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

@Test(groups = {"scoreTests"})
public class FightDTOTest {

    private ParticipantDTO competitor1A;
    private ParticipantDTO competitor1B;
    private ParticipantDTO competitor2A;
    private ParticipantDTO competitor2B;
    private TeamDTO team1;
    private TeamDTO team2;
    private TournamentDTO tournament;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        tournament = new TournamentDTO();
        tournament.setId(1);
        tournament.setName("Test");
        tournament.setType(TournamentType.LEAGUE);

        competitor1A = participant("Taro", "Yamada");
        competitor1B = participant("Jiro", "Suzuki");
        competitor2A = participant("Kenji", "Tanaka");
        competitor2B = participant("Shingo", "Ito");

        team1 = new TeamDTO();
        team1.setId(1);
        team1.setName("Team1");
        team1.setTournament(tournament);
        team1.setMembers(new ArrayList<>(List.of(competitor1A, competitor1B)));

        team2 = new TeamDTO();
        team2.setId(2);
        team2.setName("Team2");
        team2.setTournament(tournament);
        team2.setMembers(new ArrayList<>(List.of(competitor2A, competitor2B)));
    }

    private ParticipantDTO participant(String name, String lastname) {
        final ParticipantDTO p = new ParticipantDTO();
        p.setName(name);
        p.setLastname(lastname);
        return p;
    }

    private DuelDTO duelWithScores(ParticipantDTO c1, ParticipantDTO c2,
                                   List<Score> c1Scores, List<Score> c2Scores) {
        final DuelDTO duel = new DuelDTO(c1, c2, tournament, "admin");
        duel.setCompetitor1Score(new ArrayList<>(c1Scores));
        duel.setCompetitor2Score(new ArrayList<>(c2Scores));
        return duel;
    }

    @Test
    public void shouldCreateFightWithAllParams() {
        final FightDTO fight = new FightDTO(tournament, team1, team2, 0, 1);
        assertSame(fight.getTournament(), tournament);
        assertSame(fight.getTeam1(), team1);
        assertSame(fight.getTeam2(), team2);
        assertEquals((int) fight.getShiaijo(), 0);
        assertEquals((int) fight.getLevel(), 1);
    }

    @Test
    public void shouldBeOverWhenAtLeastOneDuelIsOver() {
        final FightDTO fight = new FightDTO(tournament, team1, team2, 0, 1);

        // duel that is over (2 points)
        final DuelDTO finishedDuel = duelWithScores(competitor1A, competitor2A,
                List.of(Score.MEN, Score.KOTE), List.of());
        fight.setDuels(List.of(finishedDuel));

        assertTrue(fight.isOver());
    }

    @Test
    public void shouldNotBeOverWhenNoDuelsAreOver() {
        final FightDTO fight = new FightDTO(tournament, team1, team2, 0, 1);

        // duels with no points - not finished
        final DuelDTO notFinished = duelWithScores(competitor1A, competitor2A,
                List.of(), List.of());
        fight.setDuels(List.of(notFinished));

        assertFalse(fight.isOver());
    }

    @Test
    public void shouldGetTeam1AsWinnerWhenTeam1WinsMoreDuels() {
        final FightDTO fight = new FightDTO(tournament, team1, team2, 0, 1);

        // Duel1: c1A wins (2-0)
        final DuelDTO duel1 = duelWithScores(competitor1A, competitor2A,
                List.of(Score.MEN, Score.KOTE), List.of());
        // Duel2: draw (1-1)
        final DuelDTO duel2 = duelWithScores(competitor1B, competitor2B,
                List.of(Score.MEN), List.of(Score.KOTE));
        fight.setDuels(List.of(duel1, duel2));

        // duel1 winner = -1 (team1), duel2 winner = 0 → total = -1 → team1 wins
        final TeamDTO winner = fight.getWinner();
        assertSame(winner, team1);
    }

    @Test
    public void shouldGetTeam2AsWinnerWhenTeam2WinsMoreDuels() {
        final FightDTO fight = new FightDTO(tournament, team1, team2, 0, 1);

        // Duel1: c2A wins (0-2)
        final DuelDTO duel1 = duelWithScores(competitor1A, competitor2A,
                List.of(), List.of(Score.MEN, Score.KOTE));
        // Duel2: draw (1-1)
        final DuelDTO duel2 = duelWithScores(competitor1B, competitor2B,
                List.of(Score.MEN), List.of(Score.KOTE));
        fight.setDuels(List.of(duel1, duel2));

        // duel1 winner = 1 (team2), duel2 winner = 0 → total = 1 → team2 wins
        final TeamDTO winner = fight.getWinner();
        assertSame(winner, team2);
    }

    @Test
    public void shouldReturnNullWinnerOnFullDraw() {
        final FightDTO fight = new FightDTO(tournament, team1, team2, 0, 1);

        // Both duels end in draw with equal total hits
        final DuelDTO duel1 = duelWithScores(competitor1A, competitor2A,
                List.of(Score.MEN), List.of(Score.MEN));
        final DuelDTO duel2 = duelWithScores(competitor1B, competitor2B,
                List.of(Score.KOTE), List.of(Score.KOTE));
        fight.setDuels(List.of(duel1, duel2));

        assertNull(fight.getWinner());
        assertTrue(fight.isDrawFight());
    }

    @Test
    public void shouldResolveDrawByTotalPoints() {
        final FightDTO fight = new FightDTO(tournament, team1, team2, 0, 1);

        // Both duels are tied in duel-wins, but team1 has more total hits
        final DuelDTO duel1 = duelWithScores(competitor1A, competitor2A,
                List.of(Score.MEN), List.of());  // team1 wins duel
        final DuelDTO duel2 = duelWithScores(competitor1B, competitor2B,
                List.of(), List.of(Score.MEN)); // team2 wins duel
        // Tied in wins, compare total: duel1=1+0, duel2=0+1 → 1 vs 1 → null (draw)
        fight.setDuels(List.of(duel1, duel2));

        assertNull(fight.getWinner());

        // Now add extra hit to team1 in duel1 → total team1=2, team2=1
        final DuelDTO duel1Extra = duelWithScores(competitor1A, competitor2A,
                List.of(Score.MEN, Score.DO), List.of());
        fight.setDuels(List.of(duel1Extra, duel2));

        assertSame(fight.getWinner(), team1);
    }

    @Test
    public void shouldCountWonDuelsPerTeam() {
        final FightDTO fight = new FightDTO(tournament, team1, team2, 0, 1);

        final DuelDTO duel1 = duelWithScores(competitor1A, competitor2A,
                List.of(Score.MEN, Score.KOTE), List.of());  // team1 wins
        final DuelDTO duel2 = duelWithScores(competitor1B, competitor2B,
                List.of(), List.of(Score.MEN, Score.DO));  // team2 wins
        fight.setDuels(List.of(duel1, duel2));

        assertEquals(fight.getWonDuels(team1), 1);
        assertEquals(fight.getWonDuels(team2), 1);
        assertEquals(fight.getWonDuels(new TeamDTO()), 0);
    }

    @Test
    public void shouldCountDrawDuelsPerTeam() {
        final FightDTO fight = new FightDTO(tournament, team1, team2, 0, 1);

        final DuelDTO duel1 = duelWithScores(competitor1A, competitor2A,
                List.of(Score.MEN), List.of(Score.MEN));  // draw
        fight.setDuels(List.of(duel1));

        assertEquals((int) fight.getDrawDuels(team1), 1);
        assertEquals((int) fight.getDrawDuels(team2), 1);
    }

    @Test
    public void shouldCountDrawDuelsPerCompetitor() {
        final FightDTO fight = new FightDTO(tournament, team1, team2, 0, 1);

        final DuelDTO duel1 = duelWithScores(competitor1A, competitor2A,
                List.of(Score.MEN), List.of(Score.MEN));  // draw
        fight.setDuels(List.of(duel1));

        assertEquals((int) fight.getDrawDuels(competitor1A), 1);
        assertEquals((int) fight.getDrawDuels(competitor2A), 1);
        assertEquals((int) fight.getDrawDuels(competitor1B), 0); // not in this duel
    }

    @Test
    public void shouldGetScorePerCompetitor() {
        final FightDTO fight = new FightDTO(tournament, team1, team2, 0, 1);

        final DuelDTO duel1 = duelWithScores(competitor1A, competitor2A,
                List.of(Score.MEN, Score.KOTE), List.of(Score.DO));
        fight.setDuels(List.of(duel1));

        assertEquals((int) fight.getScore(competitor1A), 2);
        assertEquals((int) fight.getScore(competitor2A), 1);
        assertEquals((int) fight.getScore(competitor1B), 0);
    }

    @Test
    public void shouldGetScorePerTeam() {
        final FightDTO fight = new FightDTO(tournament, team1, team2, 0, 1);

        final DuelDTO duel1 = duelWithScores(competitor1A, competitor2A,
                List.of(Score.MEN, Score.KOTE), List.of(Score.DO));
        fight.setDuels(List.of(duel1));

        assertEquals((int) fight.getScore(team1), 2);
        assertEquals((int) fight.getScore(team2), 1);
        assertEquals((int) fight.getScore(new TeamDTO()), 0);
    }

    @Test
    public void shouldGetTeam1AndTeam2ScoreTotals() {
        final FightDTO fight = new FightDTO(tournament, team1, team2, 0, 1);

        final DuelDTO duel1 = duelWithScores(competitor1A, competitor2A,
                List.of(Score.MEN), List.of(Score.KOTE, Score.DO));
        fight.setDuels(List.of(duel1));

        assertEquals((int) fight.getScoreTeam1(), 1);
        assertEquals((int) fight.getScoreTeam2(), 2);
    }

    @Test
    public void shouldGetDuelsWonPerCompetitor() {
        final FightDTO fight = new FightDTO(tournament, team1, team2, 0, 1);

        final DuelDTO duel1 = duelWithScores(competitor1A, competitor2A,
                List.of(Score.MEN, Score.KOTE), List.of()); // c1A wins
        final DuelDTO duel2 = duelWithScores(competitor1B, competitor2B,
                List.of(), List.of(Score.MEN, Score.DO)); // c2B wins
        fight.setDuels(List.of(duel1, duel2));

        assertEquals((int) fight.getDuelsWon(competitor1A), 1);
        assertEquals((int) fight.getDuelsWon(competitor2B), 1);
        assertEquals((int) fight.getDuelsWon(competitor1B), 0);
    }

    @Test
    public void shouldGetDuelListForCompetitor() {
        final FightDTO fight = new FightDTO(tournament, team1, team2, 0, 1);

        final DuelDTO duel1 = duelWithScores(competitor1A, competitor2A, List.of(), List.of());
        final DuelDTO duel2 = duelWithScores(competitor1B, competitor2B, List.of(), List.of());
        fight.setDuels(List.of(duel1, duel2));

        final List<DuelDTO> result = fight.getDuels(competitor1A);
        assertEquals(result.size(), 1);
        assertSame(result.get(0), duel1);
    }

    @Test
    public void shouldReturnIsWonTrueForCompetitorOnWinningTeam() {
        final FightDTO fight = new FightDTO(tournament, team1, team2, 0, 1);

        final DuelDTO duel1 = duelWithScores(competitor1A, competitor2A,
                List.of(Score.MEN, Score.KOTE), List.of());
        fight.setDuels(List.of(duel1));

        assertTrue(fight.isWon(competitor1A));
        assertFalse(fight.isWon(competitor2A));
    }

    @Test
    public void shouldReturnIsWonFalseForNullCompetitor() {
        final FightDTO fight = new FightDTO(tournament, team1, team2, 0, 1);
        assertFalse(fight.isWon(null));
    }

    @Test
    public void shouldReturnIsWonFalseForCompetitorNotInAnyTeam() {
        final FightDTO fight = new FightDTO(tournament, team1, team2, 0, 1);
        final ParticipantDTO outsider = participant("Out", "Side");

        final DuelDTO duel1 = duelWithScores(competitor1A, competitor2A,
                List.of(Score.MEN, Score.KOTE), List.of());
        fight.setDuels(List.of(duel1));

        assertFalse(fight.isWon(outsider));
    }

    @Test
    public void shouldSetAndGetFinishedAt() {
        final FightDTO fight = new FightDTO(tournament, team1, team2, 0, 1);
        final java.time.LocalDateTime now = java.time.LocalDateTime.now();
        fight.setFinishedAt(now);
        assertSame(fight.getFinishedAt(), now);
    }

    @Test
    public void shouldBeEqualWhenSameInstance() {
        final FightDTO fight = new FightDTO(tournament, team1, team2, 0, 1);
        fight.setId(1);
        final FightDTO sameReference = fight;
        assertTrue(fight.equals(sameReference));
    }

    @Test
    public void shouldReturnIsWonTrueForCompetitorOnTeam2WhenTeam2Wins() {
        final FightDTO fight = new FightDTO(tournament, team1, team2, 0, 1);

        final DuelDTO duel1 = duelWithScores(competitor1A, competitor2A,
                List.of(), List.of(Score.MEN, Score.KOTE));
        fight.setDuels(List.of(duel1));

        assertTrue(fight.isWon(competitor2A));
        assertFalse(fight.isWon(competitor1A));
    }

    @Test
    public void shouldCountDrawDuelsForTeam2AndIgnoreNonDraws() {
        final FightDTO fight = new FightDTO(tournament, team1, team2, 0, 1);

        final DuelDTO draw = duelWithScores(competitor1A, competitor2A,
                List.of(Score.MEN), List.of(Score.KOTE));
        final DuelDTO nonDraw = duelWithScores(competitor1B, competitor2B,
                List.of(Score.MEN, Score.KOTE), List.of());
        fight.setDuels(List.of(draw, nonDraw));

        assertEquals((int) fight.getDrawDuels(team2), 1);
    }

    @Test
    public void shouldReturnOnlyDuelsForMatchingCompetitor() {
        final FightDTO fight = new FightDTO(tournament, team1, team2, 0, 1);
        final DuelDTO duel1 = duelWithScores(competitor1A, competitor2A, List.of(), List.of());
        final DuelDTO duel2 = duelWithScores(competitor1B, competitor2B, List.of(), List.of());
        fight.setDuels(List.of(duel1, duel2));

        assertEquals(fight.getDuels(competitor2B).size(), 1);
        assertSame(fight.getDuels(competitor2B).get(0), duel2);
    }

    @Test
    public void shouldBeEqualWhenAllRelevantFieldsMatch() {
        final FightDTO left = new FightDTO(tournament, team1, team2, 0, 1);
        left.setId(100);
        left.setCreatedAt(java.time.LocalDateTime.of(2026, 1, 1, 10, 0));
        final DuelDTO duel = duelWithScores(competitor1A, competitor2A, List.of(Score.MEN), List.of());
        left.setDuels(List.of(duel));

        final FightDTO right = new FightDTO(tournament, team1, team2, 0, 1);
        right.setId(100);
        right.setCreatedAt(java.time.LocalDateTime.of(2026, 1, 1, 10, 0));
        right.setDuels(List.of(duel));

        assertTrue(left.equals(right));
        assertEquals(left.hashCode(), right.hashCode());
    }

    @Test
    public void shouldNotBeEqualForDifferentTypeOrDifferentFields() {
        final FightDTO base = new FightDTO(tournament, team1, team2, 0, 1);
        base.setId(200);
        base.setCreatedAt(java.time.LocalDateTime.of(2026, 1, 1, 10, 0));
        base.setDuels(List.of());


        final FightDTO differentTeam1 = new FightDTO(tournament, null, team2, 0, 1);
        differentTeam1.setId(200);
        differentTeam1.setCreatedAt(java.time.LocalDateTime.of(2026, 1, 1, 10, 0));
        assertFalse(base.equals(differentTeam1));

        final FightDTO differentTeam2 = new FightDTO(tournament, team1, null, 0, 1);
        differentTeam2.setId(200);
        differentTeam2.setCreatedAt(java.time.LocalDateTime.of(2026, 1, 1, 10, 0));
        assertFalse(base.equals(differentTeam2));

        final FightDTO differentTournament = new FightDTO(null, team1, team2, 0, 1);
        differentTournament.setId(200);
        differentTournament.setCreatedAt(java.time.LocalDateTime.of(2026, 1, 1, 10, 0));
        assertFalse(base.equals(differentTournament));

        final FightDTO differentShiaijo = new FightDTO(tournament, team1, team2, 9, 1);
        differentShiaijo.setId(200);
        differentShiaijo.setCreatedAt(java.time.LocalDateTime.of(2026, 1, 1, 10, 0));
        assertFalse(base.equals(differentShiaijo));

        final FightDTO differentLevel = new FightDTO(tournament, team1, team2, 0, 2);
        differentLevel.setId(200);
        differentLevel.setCreatedAt(java.time.LocalDateTime.of(2026, 1, 1, 10, 0));
        assertFalse(base.equals(differentLevel));
    }

    @Test
    public void shouldHandleEqualsWhenTeamsAndTournamentAreAllNull() {
        final FightDTO left = new FightDTO(null, null, null, 0, 1);
        left.setId(300);
        left.setCreatedAt(java.time.LocalDateTime.of(2026, 1, 1, 10, 0));
        left.setDuels(List.of());

        final FightDTO right = new FightDTO(null, null, null, 0, 1);
        right.setId(300);
        right.setCreatedAt(java.time.LocalDateTime.of(2026, 1, 1, 10, 0));
        right.setDuels(List.of());

        assertTrue(left.equals(right));
    }

    @Test
    public void shouldNotBeEqualWhenDuelsOrFinishedAtDiffer() {
        final FightDTO base = new FightDTO(tournament, team1, team2, 0, 1);
        base.setId(301);
        base.setCreatedAt(java.time.LocalDateTime.of(2026, 1, 1, 10, 0));
        base.setDuels(List.of(duelWithScores(competitor1A, competitor2A, List.of(Score.MEN), List.of())));
        base.setFinishedAt(java.time.LocalDateTime.of(2026, 1, 1, 11, 0));

        final FightDTO differentDuels = new FightDTO(tournament, team1, team2, 0, 1);
        differentDuels.setId(301);
        differentDuels.setCreatedAt(java.time.LocalDateTime.of(2026, 1, 1, 10, 0));
        differentDuels.setDuels(List.of());
        differentDuels.setFinishedAt(java.time.LocalDateTime.of(2026, 1, 1, 11, 0));
        assertFalse(base.equals(differentDuels));

        final FightDTO differentFinishedAt = new FightDTO(tournament, team1, team2, 0, 1);
        differentFinishedAt.setId(301);
        differentFinishedAt.setCreatedAt(java.time.LocalDateTime.of(2026, 1, 1, 10, 0));
        differentFinishedAt.setDuels(List.of(duelWithScores(competitor1A, competitor2A, List.of(Score.MEN), List.of())));
        differentFinishedAt.setFinishedAt(java.time.LocalDateTime.of(2026, 1, 1, 12, 0));
        assertFalse(base.equals(differentFinishedAt));
    }
}
