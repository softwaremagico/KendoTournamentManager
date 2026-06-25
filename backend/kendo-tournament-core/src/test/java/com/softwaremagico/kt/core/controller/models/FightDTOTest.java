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

import static org.testng.Assert.*;

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
		this.tournament = new TournamentDTO();
		this.tournament.setId(1);
		this.tournament.setName("Test");
		this.tournament.setType(TournamentType.LEAGUE);

		this.competitor1A = this.participant("Taro", "Yamada");
		this.competitor1B = this.participant("Jiro", "Suzuki");
		this.competitor2A = this.participant("Kenji", "Tanaka");
		this.competitor2B = this.participant("Shingo", "Ito");

		this.team1 = new TeamDTO();
		this.team1.setId(1);
		this.team1.setName("Team1");
		this.team1.setTournament(this.tournament);
		this.team1.setMembers(new ArrayList<>(List.of(this.competitor1A, this.competitor1B)));

		this.team2 = new TeamDTO();
		this.team2.setId(2);
		this.team2.setName("Team2");
		this.team2.setTournament(this.tournament);
		this.team2.setMembers(new ArrayList<>(List.of(this.competitor2A, this.competitor2B)));
	}

	private ParticipantDTO participant(String name, String lastname) {
		final ParticipantDTO p = new ParticipantDTO();
		p.setName(name);
		p.setLastname(lastname);
		return p;
	}

	private DuelDTO duelWithScores(ParticipantDTO c1, ParticipantDTO c2, List<Score> c1Scores, List<Score> c2Scores) {
		final DuelDTO duel = new DuelDTO(c1, c2, this.tournament, "admin");
		duel.setCompetitor1Score(new ArrayList<>(c1Scores));
		duel.setCompetitor2Score(new ArrayList<>(c2Scores));
		return duel;
	}

	@Test
	public void shouldCreateFightWithAllParams() {
		final FightDTO fight = new FightDTO(this.tournament, this.team1, this.team2, 0, 1);
		assertSame(fight.getTournament(), this.tournament);
		assertSame(fight.getTeam1(), this.team1);
		assertSame(fight.getTeam2(), this.team2);
		assertEquals(fight.getShiaijo(), 0);
		assertEquals(fight.getLevel(), 1);
	}

	@Test
	public void shouldBeOverWhenAtLeastOneDuelIsOver() {
		final FightDTO fight = new FightDTO(this.tournament, this.team1, this.team2, 0, 1);

		// duel that is over (2 points)
		final DuelDTO finishedDuel = this.duelWithScores(this.competitor1A, this.competitor2A,
				List.of(Score.MEN, Score.KOTE), List.of());
		fight.setDuels(List.of(finishedDuel));

		assertTrue(fight.isOver());
	}

	@Test
	public void shouldNotBeOverWhenNoDuelsAreOver() {
		final FightDTO fight = new FightDTO(this.tournament, this.team1, this.team2, 0, 1);

		// duels with no points - not finished
		final DuelDTO notFinished = this.duelWithScores(this.competitor1A, this.competitor2A, List.of(), List.of());
		fight.setDuels(List.of(notFinished));

		assertFalse(fight.isOver());
	}

	@Test
	public void shouldGetTeam1AsWinnerWhenTeam1WinsMoreDuels() {
		final FightDTO fight = new FightDTO(this.tournament, this.team1, this.team2, 0, 1);

		// Duel1: c1A wins (2-0)
		final DuelDTO duel1 = this.duelWithScores(this.competitor1A, this.competitor2A, List.of(Score.MEN, Score.KOTE),
				List.of());
		// Duel2: draw (1-1)
		final DuelDTO duel2 = this.duelWithScores(this.competitor1B, this.competitor2B, List.of(Score.MEN),
				List.of(Score.KOTE));
		fight.setDuels(List.of(duel1, duel2));

		// duel1 winner = -1 (team1), duel2 winner = 0 → total = -1 → team1 wins
		final TeamDTO winner = fight.getWinner();
		assertSame(winner, this.team1);
	}

	@Test
	public void shouldGetTeam2AsWinnerWhenTeam2WinsMoreDuels() {
		final FightDTO fight = new FightDTO(this.tournament, this.team1, this.team2, 0, 1);

		// Duel1: c2A wins (0-2)
		final DuelDTO duel1 = this.duelWithScores(this.competitor1A, this.competitor2A, List.of(),
				List.of(Score.MEN, Score.KOTE));
		// Duel2: draw (1-1)
		final DuelDTO duel2 = this.duelWithScores(this.competitor1B, this.competitor2B, List.of(Score.MEN),
				List.of(Score.KOTE));
		fight.setDuels(List.of(duel1, duel2));

		// duel1 winner = 1 (team2), duel2 winner = 0 → total = 1 → team2 wins
		final TeamDTO winner = fight.getWinner();
		assertSame(winner, this.team2);
	}

	@Test
	public void shouldReturnNullWinnerOnFullDraw() {
		final FightDTO fight = new FightDTO(this.tournament, this.team1, this.team2, 0, 1);

		// Both duels end in draw with equal total hits
		final DuelDTO duel1 = this.duelWithScores(this.competitor1A, this.competitor2A, List.of(Score.MEN),
				List.of(Score.MEN));
		final DuelDTO duel2 = this.duelWithScores(this.competitor1B, this.competitor2B, List.of(Score.KOTE),
				List.of(Score.KOTE));
		fight.setDuels(List.of(duel1, duel2));

		assertNull(fight.getWinner());
		assertTrue(fight.isDrawFight());
	}

	@Test
	public void shouldResolveDrawByTotalPoints() {
		final FightDTO fight = new FightDTO(this.tournament, this.team1, this.team2, 0, 1);

		// Both duels are tied in duel-wins, but team1 has more total hits
		final DuelDTO duel1 = this.duelWithScores(this.competitor1A, this.competitor2A, List.of(Score.MEN), List.of()); // team1
																														// wins
		// duel
		final DuelDTO duel2 = this.duelWithScores(this.competitor1B, this.competitor2B, List.of(), List.of(Score.MEN)); // team2
																														// wins
		// duel
		// Tied in wins, compare total: duel1=1+0, duel2=0+1 → 1 vs 1 → null (draw)
		fight.setDuels(List.of(duel1, duel2));

		assertNull(fight.getWinner());

		// Now add extra hit to team1 in duel1 → total team1=2, team2=1
		final DuelDTO duel1Extra = this.duelWithScores(this.competitor1A, this.competitor2A,
				List.of(Score.MEN, Score.DO), List.of());
		fight.setDuels(List.of(duel1Extra, duel2));

		assertSame(fight.getWinner(), this.team1);
	}

	@Test
	public void shouldCountWonDuelsPerTeam() {
		final FightDTO fight = new FightDTO(this.tournament, this.team1, this.team2, 0, 1);

		final DuelDTO duel1 = this.duelWithScores(this.competitor1A, this.competitor2A, List.of(Score.MEN, Score.KOTE),
				List.of()); // team1
		// wins
		final DuelDTO duel2 = this.duelWithScores(this.competitor1B, this.competitor2B, List.of(),
				List.of(Score.MEN, Score.DO)); // team2
		// wins
		fight.setDuels(List.of(duel1, duel2));

		assertEquals(fight.getWonDuels(this.team1), 1);
		assertEquals(fight.getWonDuels(this.team2), 1);
		assertEquals(fight.getWonDuels(new TeamDTO()), 0);
	}

	@Test
	public void shouldCountDrawDuelsPerTeam() {
		final FightDTO fight = new FightDTO(this.tournament, this.team1, this.team2, 0, 1);

		final DuelDTO duel1 = this.duelWithScores(this.competitor1A, this.competitor2A, List.of(Score.MEN),
				List.of(Score.MEN)); // draw
		fight.setDuels(List.of(duel1));

		assertEquals(fight.getDrawDuels(this.team1), 1);
		assertEquals(fight.getDrawDuels(this.team2), 1);
	}

	@Test
	public void shouldCountDrawDuelsPerCompetitor() {
		final FightDTO fight = new FightDTO(this.tournament, this.team1, this.team2, 0, 1);

		final DuelDTO duel1 = this.duelWithScores(this.competitor1A, this.competitor2A, List.of(Score.MEN),
				List.of(Score.MEN)); // draw
		fight.setDuels(List.of(duel1));

		assertEquals(fight.getDrawDuels(this.competitor1A), 1);
		assertEquals(fight.getDrawDuels(this.competitor2A), 1);
		assertEquals(fight.getDrawDuels(this.competitor1B), 0); // not in this duel
	}

	@Test
	public void shouldGetScorePerCompetitor() {
		final FightDTO fight = new FightDTO(this.tournament, this.team1, this.team2, 0, 1);

		final DuelDTO duel1 = this.duelWithScores(this.competitor1A, this.competitor2A, List.of(Score.MEN, Score.KOTE),
				List.of(Score.DO));
		fight.setDuels(List.of(duel1));

		assertEquals(fight.getScore(this.competitor1A), 2);
		assertEquals(fight.getScore(this.competitor2A), 1);
		assertEquals(fight.getScore(this.competitor1B), 0);
	}

	@Test
	public void shouldGetScorePerTeam() {
		final FightDTO fight = new FightDTO(this.tournament, this.team1, this.team2, 0, 1);

		final DuelDTO duel1 = this.duelWithScores(this.competitor1A, this.competitor2A, List.of(Score.MEN, Score.KOTE),
				List.of(Score.DO));
		fight.setDuels(List.of(duel1));

		assertEquals(fight.getScore(this.team1), 2);
		assertEquals(fight.getScore(this.team2), 1);
		assertEquals(fight.getScore(new TeamDTO()), 0);
	}

	@Test
	public void shouldGetTeam1AndTeam2ScoreTotals() {
		final FightDTO fight = new FightDTO(this.tournament, this.team1, this.team2, 0, 1);

		final DuelDTO duel1 = this.duelWithScores(this.competitor1A, this.competitor2A, List.of(Score.MEN),
				List.of(Score.KOTE, Score.DO));
		fight.setDuels(List.of(duel1));

		assertEquals(fight.getScoreTeam1(), 1);
		assertEquals(fight.getScoreTeam2(), 2);
	}

	@Test
	public void shouldGetDuelsWonPerCompetitor() {
		final FightDTO fight = new FightDTO(this.tournament, this.team1, this.team2, 0, 1);

		final DuelDTO duel1 = this.duelWithScores(this.competitor1A, this.competitor2A, List.of(Score.MEN, Score.KOTE),
				List.of()); // c1A
		// wins
		final DuelDTO duel2 = this.duelWithScores(this.competitor1B, this.competitor2B, List.of(),
				List.of(Score.MEN, Score.DO)); // c2B
		// wins
		fight.setDuels(List.of(duel1, duel2));

		assertEquals(fight.getDuelsWon(this.competitor1A), 1);
		assertEquals(fight.getDuelsWon(this.competitor2B), 1);
		assertEquals(fight.getDuelsWon(this.competitor1B), 0);
	}

	@Test
	public void shouldGetDuelListForCompetitor() {
		final FightDTO fight = new FightDTO(this.tournament, this.team1, this.team2, 0, 1);

		final DuelDTO duel1 = this.duelWithScores(this.competitor1A, this.competitor2A, List.of(), List.of());
		final DuelDTO duel2 = this.duelWithScores(this.competitor1B, this.competitor2B, List.of(), List.of());
		fight.setDuels(List.of(duel1, duel2));

		final List<DuelDTO> result = fight.getDuels(this.competitor1A);
		assertEquals(result.size(), 1);
		assertSame(result.get(0), duel1);
	}

	@Test
	public void shouldReturnIsWonTrueForCompetitorOnWinningTeam() {
		final FightDTO fight = new FightDTO(this.tournament, this.team1, this.team2, 0, 1);

		final DuelDTO duel1 = this.duelWithScores(this.competitor1A, this.competitor2A, List.of(Score.MEN, Score.KOTE),
				List.of());
		fight.setDuels(List.of(duel1));

		assertTrue(fight.isWon(this.competitor1A));
		assertFalse(fight.isWon(this.competitor2A));
	}

	@Test
	public void shouldReturnIsWonFalseForNullCompetitor() {
		final FightDTO fight = new FightDTO(this.tournament, this.team1, this.team2, 0, 1);
		assertFalse(fight.isWon(null));
	}

	@Test
	public void shouldReturnIsWonFalseForCompetitorNotInAnyTeam() {
		final FightDTO fight = new FightDTO(this.tournament, this.team1, this.team2, 0, 1);
		final ParticipantDTO outsider = this.participant("Out", "Side");

		final DuelDTO duel1 = this.duelWithScores(this.competitor1A, this.competitor2A, List.of(Score.MEN, Score.KOTE),
				List.of());
		fight.setDuels(List.of(duel1));

		assertFalse(fight.isWon(outsider));
	}

	@Test
	public void shouldSetAndGetFinishedAt() {
		final FightDTO fight = new FightDTO(this.tournament, this.team1, this.team2, 0, 1);
		final java.time.LocalDateTime now = java.time.LocalDateTime.now();
		fight.setFinishedAt(now);
		assertSame(fight.getFinishedAt(), now);
	}

	@Test
	public void shouldBeEqualWhenSameInstance() {
		final FightDTO fight = new FightDTO(this.tournament, this.team1, this.team2, 0, 1);
		fight.setId(1);
		final FightDTO sameReference = fight;
		assertEquals(sameReference, fight);
	}

	@Test
	public void shouldReturnIsWonTrueForCompetitorOnTeam2WhenTeam2Wins() {
		final FightDTO fight = new FightDTO(this.tournament, this.team1, this.team2, 0, 1);

		final DuelDTO duel1 = this.duelWithScores(this.competitor1A, this.competitor2A, List.of(),
				List.of(Score.MEN, Score.KOTE));
		fight.setDuels(List.of(duel1));

		assertTrue(fight.isWon(this.competitor2A));
		assertFalse(fight.isWon(this.competitor1A));
	}

	@Test
	public void shouldCountDrawDuelsForTeam2AndIgnoreNonDraws() {
		final FightDTO fight = new FightDTO(this.tournament, this.team1, this.team2, 0, 1);

		final DuelDTO draw = this.duelWithScores(this.competitor1A, this.competitor2A, List.of(Score.MEN),
				List.of(Score.KOTE));
		final DuelDTO nonDraw = this.duelWithScores(this.competitor1B, this.competitor2B,
				List.of(Score.MEN, Score.KOTE), List.of());
		fight.setDuels(List.of(draw, nonDraw));

		assertEquals(fight.getDrawDuels(this.team2), 1);
	}

	@Test
	public void shouldReturnOnlyDuelsForMatchingCompetitor() {
		final FightDTO fight = new FightDTO(this.tournament, this.team1, this.team2, 0, 1);
		final DuelDTO duel1 = this.duelWithScores(this.competitor1A, this.competitor2A, List.of(), List.of());
		final DuelDTO duel2 = this.duelWithScores(this.competitor1B, this.competitor2B, List.of(), List.of());
		fight.setDuels(List.of(duel1, duel2));

		assertEquals(fight.getDuels(this.competitor2B).size(), 1);
		assertSame(fight.getDuels(this.competitor2B).get(0), duel2);
	}

	@Test
	public void shouldBeEqualWhenAllRelevantFieldsMatch() {
		final FightDTO left = new FightDTO(this.tournament, this.team1, this.team2, 0, 1);
		left.setId(100);
		left.setCreatedAt(java.time.LocalDateTime.of(2026, 1, 1, 10, 0));
		final DuelDTO duel = this.duelWithScores(this.competitor1A, this.competitor2A, List.of(Score.MEN), List.of());
		left.setDuels(List.of(duel));

		final FightDTO right = new FightDTO(this.tournament, this.team1, this.team2, 0, 1);
		right.setId(100);
		right.setCreatedAt(java.time.LocalDateTime.of(2026, 1, 1, 10, 0));
		right.setDuels(List.of(duel));

		assertEquals(right, left);
		assertEquals(left.hashCode(), right.hashCode());
	}

	@Test
	public void shouldNotBeEqualForDifferentTypeOrDifferentFields() {
		final FightDTO base = new FightDTO(this.tournament, this.team1, this.team2, 0, 1);
		base.setId(200);
		base.setCreatedAt(java.time.LocalDateTime.of(2026, 1, 1, 10, 0));
		base.setDuels(List.of());

		final FightDTO differentTeam1 = new FightDTO(this.tournament, null, this.team2, 0, 1);
		differentTeam1.setId(200);
		differentTeam1.setCreatedAt(java.time.LocalDateTime.of(2026, 1, 1, 10, 0));
		assertNotEquals(differentTeam1, base);

		final FightDTO differentTeam2 = new FightDTO(this.tournament, this.team1, null, 0, 1);
		differentTeam2.setId(200);
		differentTeam2.setCreatedAt(java.time.LocalDateTime.of(2026, 1, 1, 10, 0));
		assertNotEquals(differentTeam2, base);

		final FightDTO differentTournament = new FightDTO(null, this.team1, this.team2, 0, 1);
		differentTournament.setId(200);
		differentTournament.setCreatedAt(java.time.LocalDateTime.of(2026, 1, 1, 10, 0));
		assertNotEquals(differentTournament, base);

		final FightDTO differentShiaijo = new FightDTO(this.tournament, this.team1, this.team2, 9, 1);
		differentShiaijo.setId(200);
		differentShiaijo.setCreatedAt(java.time.LocalDateTime.of(2026, 1, 1, 10, 0));
		assertNotEquals(differentShiaijo, base);

		final FightDTO differentLevel = new FightDTO(this.tournament, this.team1, this.team2, 0, 2);
		differentLevel.setId(200);
		differentLevel.setCreatedAt(java.time.LocalDateTime.of(2026, 1, 1, 10, 0));
		assertNotEquals(differentLevel, base);
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

		assertEquals(right, left);
	}

	@Test
	public void shouldNotBeEqualWhenDuelsOrFinishedAtDiffer() {
		final FightDTO base = new FightDTO(this.tournament, this.team1, this.team2, 0, 1);
		base.setId(301);
		base.setCreatedAt(java.time.LocalDateTime.of(2026, 1, 1, 10, 0));
		base.setDuels(
				List.of(this.duelWithScores(this.competitor1A, this.competitor2A, List.of(Score.MEN), List.of())));
		base.setFinishedAt(java.time.LocalDateTime.of(2026, 1, 1, 11, 0));

		final FightDTO differentDuels = new FightDTO(this.tournament, this.team1, this.team2, 0, 1);
		differentDuels.setId(301);
		differentDuels.setCreatedAt(java.time.LocalDateTime.of(2026, 1, 1, 10, 0));
		differentDuels.setDuels(List.of());
		differentDuels.setFinishedAt(java.time.LocalDateTime.of(2026, 1, 1, 11, 0));
		assertNotEquals(differentDuels, base);

		final FightDTO differentFinishedAt = new FightDTO(this.tournament, this.team1, this.team2, 0, 1);
		differentFinishedAt.setId(301);
		differentFinishedAt.setCreatedAt(java.time.LocalDateTime.of(2026, 1, 1, 10, 0));
		differentFinishedAt.setDuels(
				List.of(this.duelWithScores(this.competitor1A, this.competitor2A, List.of(Score.MEN), List.of())));
		differentFinishedAt.setFinishedAt(java.time.LocalDateTime.of(2026, 1, 1, 12, 0));
		assertNotEquals(differentFinishedAt, base);
	}
}
