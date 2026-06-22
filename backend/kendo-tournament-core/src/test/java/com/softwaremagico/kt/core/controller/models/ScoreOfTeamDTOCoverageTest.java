
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
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

@Test(groups = "scoreTests")
public class ScoreOfTeamDTOCoverageTest {

	@Test
	public void shouldUpdateAndComputeTeamStats() {
		final TournamentDTO tournament = this.tournament();
		final TeamDTO team1 = this.team(1, "Alpha", tournament);
		final TeamDTO team2 = this.team(2, "Beta", tournament);
		final ParticipantDTO p1 = this.participant(11, "P1", "One", this.club(101, "ClubA"));
		final ParticipantDTO p2 = this.participant(12, "P2", "Two", this.club(102, "ClubB"));
		team1.addMember(p1);
		team2.addMember(p2);

		final DuelDTO winningDuel = this.duel(team1, team2, p1, p2, List.of(Score.MEN, Score.MEN), List.of(), true);
		final DuelDTO drawDuel = this.duel(team1, team2, p1, p2, List.of(), List.of(), true);
		final FightDTO wonFight = this.fight(tournament, team1, team2, 1, List.of(winningDuel));
		final FightDTO drawFight = this.fight(tournament, team1, team2, 1, List.of(drawDuel));
		final DuelDTO untie = this.duel(team1, team2, p1, p2, List.of(Score.MEN, Score.MEN), List.of(), true);

		final ScoreOfTeamDTO dto = new ScoreOfTeamDTO(team1, List.of(wonFight, drawFight), List.of(untie));

		assertEquals(dto.getTeam(), team1);
		assertEquals(dto.getWonFights(), Integer.valueOf(1));
		assertEquals(dto.getDrawFights(), Integer.valueOf(1));
		assertEquals(dto.getFightsDone(), Integer.valueOf(2));
		assertEquals(dto.getWonDuels(), Integer.valueOf(1));
		assertEquals(dto.getDrawDuels(), Integer.valueOf(1));
		assertEquals(dto.getUntieDuels(), Integer.valueOf(1));
		assertEquals(dto.getHits(), Integer.valueOf(2));
		assertEquals(dto.getLevel(), Integer.valueOf(1));
	}

	@Test
	public void shouldExposeMutatorsAndEqualityBasedOnElementData() {
		final TournamentDTO tournament = this.tournament();
		final TeamDTO team = this.team(10, "Gamma", tournament);
		final ScoreOfTeamDTO dto = new ScoreOfTeamDTO();
		dto.setTeam(team);
		dto.setWonFights(2);
		dto.setDrawFights(3);
		dto.setFightsDone(5);
		dto.setWonDuels(4);
		dto.setDrawDuels(1);
		dto.setUntieDuels(0);
		dto.setHits(12);
		dto.setHitsLost(7);
		dto.setLevel(2);
		dto.setSortingIndex(1);
		dto.setId(99);
		dto.setCreatedAt(LocalDateTime.now().withNano(0));
		dto.setCreatedBy("creator");
		dto.setUpdatedAt(LocalDateTime.now().withNano(0));
		dto.setUpdatedBy("updater");
		dto.setVersion(3);

		final ScoreOfTeamDTO copy = new ScoreOfTeamDTO();
		copy.setTeam(team);
		copy.setId(dto.getId());
		copy.setCreatedAt(dto.getCreatedAt());

		assertEquals(dto.getTeam(), team);
		assertEquals(dto.getSortingIndex(), Integer.valueOf(1));
		assertEquals(dto.getHitsLost(), Integer.valueOf(7));
		assertEquals(copy.getTeam(), team);
		assertEquals(copy.getId(), dto.getId());
		assertNotNull(dto.toString());
		assertNull(new ScoreOfTeamDTO().getTournament());
	}

	private TournamentDTO tournament() {
		final TournamentDTO tournament = new TournamentDTO("Tournament", 1, 1, TournamentType.LEAGUE);
		tournament.setId(1);
		return tournament;
	}

	private TeamDTO team(int id, String name, TournamentDTO tournament) {
		final TeamDTO team = new TeamDTO(name, tournament);
		team.setId(id);
		return team;
	}

	private ClubDTO club(int id, String name) {
		final ClubDTO club = new ClubDTO(name, "City");
		club.setId(id);
		return club;
	}

	private ParticipantDTO participant(int id, String name, String lastname, ClubDTO club) {
		final ParticipantDTO participant = new ParticipantDTO();
		participant.setId(id);
		participant.setName(name);
		participant.setLastname(lastname);
		participant.setClub(club);
		return participant;
	}

	private DuelDTO duel(TeamDTO team1, TeamDTO team2, ParticipantDTO p1, ParticipantDTO p2,
			List<Score> competitor1Score, List<Score> competitor2Score, boolean finished) {
		final DuelDTO duel = new DuelDTO();
		duel.setCompetitor1(p1);
		duel.setCompetitor2(p2);
		duel.setTournament(team1.getTournament());
		duel.setCompetitor1Score(competitor1Score);
		duel.setCompetitor2Score(competitor2Score);
		duel.setFinished(finished);
		return duel;
	}

	private FightDTO fight(TournamentDTO tournament, TeamDTO team1, TeamDTO team2, int level, List<DuelDTO> duels) {
		final FightDTO fight = new FightDTO();
		fight.setTournament(tournament);
		fight.setTeam1(team1);
		fight.setTeam2(team2);
		fight.setLevel(level);
		fight.setDuels(duels);
		return fight;
	}
}


