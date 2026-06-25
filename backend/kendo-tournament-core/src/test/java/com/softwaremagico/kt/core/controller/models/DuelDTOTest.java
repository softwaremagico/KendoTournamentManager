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

import com.softwaremagico.kt.persistence.entities.DuelType;
import com.softwaremagico.kt.persistence.values.Score;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.testng.Assert.*;

@Test(groups = {"scoreTests"})
public class DuelDTOTest {

	private ParticipantDTO competitor(String name, String lastname) {
		final ParticipantDTO p = new ParticipantDTO();
		p.setName(name);
		p.setLastname(lastname);
		return p;
	}

	private TournamentDTO tournament() {
		final TournamentDTO t = new TournamentDTO();
		t.setId(1);
		t.setName("TestTournament");
		return t;
	}

	@Test
	public void shouldCreateDefaultDuelWithStandardType() {
		final DuelDTO dto = new DuelDTO();
		assertEquals(dto.getType(), DuelType.STANDARD);
		assertFalse(dto.isFinished());
		assertFalse(dto.getCompetitor1Fault());
		assertFalse(dto.getCompetitor2Fault());
		assertNotNull(dto.getCompetitor1Score());
		assertNotNull(dto.getCompetitor2Score());
		assertEquals(dto.getCompetitor1Score().size(), 0);
		assertEquals(dto.getCompetitor2Score().size(), 0);
	}

	@Test
	public void shouldCreateDuelWithCompetitorsAndTournament() {
		final ParticipantDTO c1 = this.competitor("Taro", "Yamada");
		final ParticipantDTO c2 = this.competitor("Jiro", "Tanaka");
		final TournamentDTO t = this.tournament();

		final DuelDTO dto = new DuelDTO(c1, c2, t, "admin");
		assertSame(dto.getCompetitor1(), c1);
		assertSame(dto.getCompetitor2(), c2);
		assertSame(dto.getTournament(), t);
		assertEquals(dto.getCreatedBy(), "admin");
	}

	@Test
	public void shouldGetWinnerWhenCompetitor1Wins() {
		final DuelDTO dto = new DuelDTO();
		dto.setCompetitor1Score(List.of(Score.MEN, Score.KOTE));
		dto.setCompetitor2Score(List.of());

		// competitor1 has 2 points, competitor2 has 0 → winner = -1 (competitor1)
		assertEquals(dto.getWinner(), -1);
		assertEquals((int) dto.getCompetitor1ScoreValue(), 2);
		assertEquals((int) dto.getCompetitor2ScoreValue(), 0);
	}

	@Test
	public void shouldGetWinnerWhenCompetitor2Wins() {
		final DuelDTO dto = new DuelDTO();
		dto.setCompetitor1Score(List.of());
		dto.setCompetitor2Score(List.of(Score.MEN, Score.DO));

		// competitor2 has 2 points → winner = 1 (competitor2)
		assertEquals(dto.getWinner(), 1);
		assertEquals((int) dto.getCompetitor1ScoreValue(), 0);
		assertEquals((int) dto.getCompetitor2ScoreValue(), 2);
	}

	@Test
	public void shouldGetDrawWhenEqualScore() {
		final DuelDTO dto = new DuelDTO();
		dto.setCompetitor1Score(List.of(Score.MEN));
		dto.setCompetitor2Score(List.of(Score.KOTE));

		assertEquals(dto.getWinner(), 0);
		assertEquals((int) dto.getCompetitor1ScoreValue(), 1);
		assertEquals((int) dto.getCompetitor2ScoreValue(), 1);
	}

	@Test
	public void shouldBeOverWhenCompetitor1Reaches2Points() {
		final DuelDTO dto = new DuelDTO();
		dto.setCompetitor1Score(List.of(Score.MEN, Score.KOTE));
		dto.setCompetitor2Score(List.of());

		assertTrue(dto.isOver());
	}

	@Test
	public void shouldBeOverWhenCompetitor2Reaches2Points() {
		final DuelDTO dto = new DuelDTO();
		dto.setCompetitor1Score(List.of());
		dto.setCompetitor2Score(List.of(Score.MEN, Score.DO));

		assertTrue(dto.isOver());
	}

	@Test
	public void shouldBeOverWhenFinishedFlagSet() {
		final DuelDTO dto = new DuelDTO();
		dto.setCompetitor1Score(List.of());
		dto.setCompetitor2Score(List.of());
		dto.setFinished(true);

		assertTrue(dto.isOver());
	}

	@Test
	public void shouldNotBeOverWhenNoPointsAndNotFinished() {
		final DuelDTO dto = new DuelDTO();
		assertFalse(dto.isOver());
	}

	@Test
	public void shouldCountOnlyValidPointsForScore() {
		final DuelDTO dto = new DuelDTO();
		// EMPTY and FAULT are not valid points; MEN, HANSOKU, KOTE are valid
		dto.setCompetitor1Score(List.of(Score.MEN, Score.EMPTY, Score.FAULT));
		dto.setCompetitor2Score(List.of(Score.HANSOKU, Score.DRAW));

		// MEN is valid, EMPTY and FAULT are not → competitor1 has 1 valid point
		assertEquals((int) dto.getCompetitor1ScoreValue(), 1);
		// HANSOKU is valid, DRAW is not → competitor2 has 1 valid point
		assertEquals((int) dto.getCompetitor2ScoreValue(), 1);
	}

	@Test
	public void shouldSetAndGetAllFields() {
		final DuelDTO dto = new DuelDTO();
		dto.setDuration(120);
		dto.setTotalDuration(300);
		dto.setType(DuelType.STANDARD);
		dto.setCompetitor1Fault(true);
		dto.setCompetitor2Fault(true);
		dto.setCompetitor1FaultTime(30);
		dto.setCompetitor2FaultTime(60);
		dto.setSubstitute(true);

		final LocalDateTime start = LocalDateTime.now().minusMinutes(5);
		final LocalDateTime end = LocalDateTime.now();
		dto.setStartedAt(start);
		dto.setFinishedAt(end);

		assertEquals((int) dto.getDuration(), 120);
		assertEquals((int) dto.getTotalDuration(), 300);
		assertEquals(dto.getType(), DuelType.STANDARD);
		assertTrue(dto.getCompetitor1Fault());
		assertTrue(dto.getCompetitor2Fault());
		assertEquals((int) dto.getCompetitor1FaultTime(), 30);
		assertEquals((int) dto.getCompetitor2FaultTime(), 60);
		assertTrue(dto.getSubstitute());
		assertSame(dto.getStartedAt(), start);
		assertSame(dto.getFinishedAt(), end);
	}

	@Test
	public void shouldProduceToStringWithBothCompetitors() {
		final ParticipantDTO c1 = this.competitor("Taro", "Yamada");
		final ParticipantDTO c2 = this.competitor("Jiro", "Tanaka");
		final DuelDTO dto = new DuelDTO(c1, c2, this.tournament(), "admin");
		dto.setCompetitor1Score(List.of(Score.MEN));
		dto.setCompetitor2Score(List.of(Score.KOTE));

		final String result = dto.toString();
		assertNotNull(result);
		assertTrue(result.contains("M"));
		assertTrue(result.contains("K"));
	}

	@Test
	public void shouldProduceToStringWithNullCompetitor1() {
		final DuelDTO dto = new DuelDTO();
		dto.setCompetitor2(this.competitor("Jiro", "Tanaka"));

		final String result = dto.toString();
		assertTrue(result.contains("Empty") || result.contains("empty") || result.contains("<<Empty>>"));
	}

	@Test
	public void shouldProduceToStringWithNullCompetitor2() {
		final DuelDTO dto = new DuelDTO();
		dto.setCompetitor1(this.competitor("Taro", "Yamada"));

		final String result = dto.toString();
		assertTrue(result.contains("Empty") || result.contains("empty") || result.contains("<<Empty>>"));
	}

	@Test
	public void shouldProduceToStringWithFaults() {
		final ParticipantDTO c1 = this.competitor("Taro", "Yamada");
		final ParticipantDTO c2 = this.competitor("Jiro", "Tanaka");
		final DuelDTO dto = new DuelDTO(c1, c2, this.tournament(), "admin");
		dto.setCompetitor1Fault(true);
		dto.setCompetitor2Fault(true);

		final String result = dto.toString();
		assertNotNull(result);
	}

	@Test
	public void shouldBeEqualWhenSameInstance() {
		final DuelDTO dto = new DuelDTO();
		dto.setId(1);

		assertEquals(dto, dto);
	}

	@Test
	public void shouldSetScoreTimeLists() {
		final DuelDTO dto = new DuelDTO();
		dto.setCompetitor1ScoreTime(List.of(10, 20));
		dto.setCompetitor2ScoreTime(List.of(30));

		assertEquals(dto.getCompetitor1ScoreTime().size(), 2);
		assertEquals(dto.getCompetitor2ScoreTime().size(), 1);
	}

	@Test
	public void shouldHandleAddScoreAndTimeWhenInternalListsAreNull() {
		final DuelDTO dto = new DuelDTO();
		dto.setCompetitor1Score(null);
		dto.setCompetitor2Score(null);
		dto.setCompetitor1ScoreTime(null);
		dto.setCompetitor2ScoreTime(null);

		dto.addCompetitor1Score(Score.MEN);
		dto.addCompetitor2Score(Score.KOTE);
		dto.addCompetitor1ScoreTime(15);
		dto.addCompetitor2ScoreTime(20);

		assertEquals(dto.getCompetitor1Score().size(), 1);
		assertEquals(dto.getCompetitor2Score().size(), 1);
		assertEquals(dto.getCompetitor1ScoreTime().size(), 1);
		assertEquals(dto.getCompetitor2ScoreTime().size(), 1);
	}

	@Test
	public void shouldProduceToStringWhenFaultFlagsAreNull() {
		final DuelDTO dto = new DuelDTO(this.competitor("A", "One"), this.competitor("B", "Two"), this.tournament(),
				"admin");
		dto.setCompetitor1Fault(null);
		dto.setCompetitor2Fault(null);
		dto.setCompetitor1Score(new java.util.ArrayList<>(java.util.Arrays.asList(Score.MEN, null)));
		dto.setCompetitor2Score(new java.util.ArrayList<>(java.util.Arrays.asList(Score.KOTE, null)));

		final String result = dto.toString();
		assertNotNull(result);
		assertTrue(result.contains("M"));
		assertTrue(result.contains("K"));
	}

	@Test
	public void shouldEvaluateEqualsForDifferentBranches() {
		final ParticipantDTO c1 = this.competitor("Taro", "Yamada");
		final ParticipantDTO c2 = this.competitor("Jiro", "Tanaka");

		final DuelDTO base = new DuelDTO(c1, c2, this.tournament(), "admin");
		base.setCompetitor1Score(List.of(Score.MEN));
		base.setCompetitor2Score(List.of());
		base.setCompetitor1Fault(false);
		base.setCompetitor2Fault(false);

		assertEquals(base, base);
		assertNotEquals(base, "other");

		final DuelDTO same = new DuelDTO(c1, c2, this.tournament(), "admin");
		same.setCompetitor1Score(List.of(Score.MEN));
		same.setCompetitor2Score(List.of());
		same.setCompetitor1Fault(false);
		same.setCompetitor2Fault(false);
		assertEquals(same, base);

		final DuelDTO differentCompetitor1 = new DuelDTO(this.competitor("X", "Y"), c2, this.tournament(), "admin");
		differentCompetitor1.setCompetitor1Score(List.of(Score.MEN));
		differentCompetitor1.setCompetitor2Score(List.of());
		differentCompetitor1.setCompetitor1Fault(false);
		differentCompetitor1.setCompetitor2Fault(false);
		assertNotEquals(differentCompetitor1, base);

		final DuelDTO differentCompetitor2 = new DuelDTO(c1, this.competitor("X", "Y"), this.tournament(), "admin");
		differentCompetitor2.setCompetitor1Score(List.of(Score.MEN));
		differentCompetitor2.setCompetitor2Score(List.of());
		differentCompetitor2.setCompetitor1Fault(false);
		differentCompetitor2.setCompetitor2Fault(false);
		assertNotEquals(differentCompetitor2, base);

		final DuelDTO differentScore = new DuelDTO(c1, c2, this.tournament(), "admin");
		differentScore.setCompetitor1Score(List.of());
		differentScore.setCompetitor2Score(List.of(Score.MEN));
		differentScore.setCompetitor1Fault(false);
		differentScore.setCompetitor2Fault(false);
		assertNotEquals(differentScore, base);

		final DuelDTO differentFaults = new DuelDTO(c1, c2, this.tournament(), "admin");
		differentFaults.setCompetitor1Score(List.of(Score.MEN));
		differentFaults.setCompetitor2Score(List.of());
		differentFaults.setCompetitor1Fault(true);
		differentFaults.setCompetitor2Fault(false);
		assertNotEquals(differentFaults, base);

		final DuelDTO differentType = new DuelDTO(c1, c2, this.tournament(), "admin");
		differentType.setCompetitor1Score(List.of(Score.MEN));
		differentType.setCompetitor2Score(List.of());
		differentType.setCompetitor1Fault(false);
		differentType.setCompetitor2Fault(false);
		differentType.setType(DuelType.UNDRAW);
		assertNotEquals(differentType, base);
	}
}
