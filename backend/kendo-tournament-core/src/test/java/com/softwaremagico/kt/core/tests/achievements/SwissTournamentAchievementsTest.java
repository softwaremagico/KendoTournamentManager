package com.softwaremagico.kt.core.tests.achievements;

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

import com.softwaremagico.kt.core.TournamentTestUtils;
import com.softwaremagico.kt.core.controller.AchievementController;
import com.softwaremagico.kt.core.controller.FightController;
import com.softwaremagico.kt.core.controller.TournamentExtraPropertyController;
import com.softwaremagico.kt.core.controller.models.AchievementDTO;
import com.softwaremagico.kt.core.controller.models.DuelDTO;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.controller.models.TournamentExtraPropertyDTO;
import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.persistence.values.AchievementType;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Test(groups = {"swissAchievementTests"})
public class SwissTournamentAchievementsTest extends TournamentTestUtils {

	private static final int MEMBERS = 1;
	private static final int TEAMS = 4;
	private static final int REFEREES = 0;
	private static final int ORGANIZER = 0;
	private static final int VOLUNTEER = 0;
	private static final int PRESS = 0;

	@Autowired
	private FightController fightController;

	@Autowired
	private AchievementController achievementController;

	@Autowired
	private TournamentExtraPropertyController tournamentExtraPropertyController;

	private TournamentDTO swissTournamentDTO;

	@BeforeClass
	public void prepareData() {
		this.addParticipants(MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 0);
	}

	@BeforeClass(dependsOnMethods = "prepareData")
	public void prepareSwissTournament() {
		// Swiss scenario focused on two specific achievements:
		// - SWISS_WINNER: tournament winner in Swiss mode.
		// - BUCHHOLZ_WHISPERER: winner decided by tie-break after equal Swiss points.
		this.swissTournamentDTO = this.addTournament("Swiss Achievement Tournament", MEMBERS, TEAMS, REFEREES,
				ORGANIZER, VOLUNTEER, PRESS, TournamentType.SWISS, 2);

		this.tournamentExtraPropertyController.create(
				new TournamentExtraPropertyDTO(this.swissTournamentDTO, TournamentExtraPropertyKey.SWISS_ROUNDS, "2"),
				null, null);

		final List<FightDTO> round1 = new ArrayList<>(
				this.fightController.createFights(this.swissTournamentDTO.getId(), TeamsOrder.SORTED, 0, null, null));

		// Round 1, match 1: force a clear winner (competitor1 - team1).
		// Purpose: create one of the two leaders for round 2.
		this.setCompetitor1Win(round1.get(0).getDuels().getFirst());
		round1.set(0, this.fightController.update(round1.get(0), null, null));

		// Round 1, match 2: force another clear winner (competitor1 - team3).
		// Purpose: create the second leader that will be paired against the first
		// leader in round 2.
		this.setCompetitor1Win(round1.get(1).getDuels().getFirst());
		round1.set(1, this.fightController.update(round1.get(1), null, null));

		final List<FightDTO> round2 = new ArrayList<>(
				this.fightController.createFights(this.swissTournamentDTO.getId(), TeamsOrder.SORTED, 1, null, null));

		// Round 2, leaders' match: force a draw between both leaders.
		// Purpose: keep both top teams tied on Swiss match points so final order
		// depends on tie-break.
		this.setDraw(round2.get(0).getDuels().getFirst());
		round2.set(0, this.fightController.update(round2.get(0), null, null));

		// Round 2, non-leaders' match: force a winner.
		// Purpose: increase Buchholz asymmetry between the tied leaders by changing
		// opponents' final points.
		this.setCompetitor1Win(round2.get(1).getDuels().getFirst());
		round2.set(1, this.fightController.update(round2.get(1), null, null));

		// So, winner is defined by Buchholz tie-breaker. The winner of the tournament
		// will be the team with the highest Buchholz score, which is determined by the
		// performance of their opponents in previous rounds.
		this.achievementController.generateAchievements(this.swissTournamentDTO);
	}

	@Test
	public void checkSwissWinnerAchievement() {
		// Expected: exactly one SWISS_WINNER because Swiss ranking must produce one
		// champion.
		final List<AchievementDTO> swissWinner = this.achievementController.getAchievements(this.swissTournamentDTO,
				AchievementType.SWISS_WINNER);
		Assert.assertEquals(swissWinner.size(), 1);
	}

	@Test
	public void checkBuchholzWhispererAchievement() {
		// Expected: exactly one BUCHHOLZ_WHISPERER because top teams are tied on Swiss
		// points
		// and the winner is decided by tie-break (Buchholz).
		final List<AchievementDTO> buchholzWhisperer = this.achievementController
				.getAchievements(this.swissTournamentDTO, AchievementType.BUCHHOLZ_WHISPERER);
		Assert.assertEquals(buchholzWhisperer.size(), 1);

		// Expected: both achievements belong to the same participant because tie-break
		// decides
		// the Swiss champion itself.
		final List<AchievementDTO> swissWinner = this.achievementController.getAchievements(this.swissTournamentDTO,
				AchievementType.SWISS_WINNER);
		Assert.assertEquals(buchholzWhisperer.getFirst().getParticipant(), swissWinner.getFirst().getParticipant());
	}

	private void setCompetitor1Win(DuelDTO duelDTO) {
		duelDTO.addCompetitor1Score(Score.MEN);
		duelDTO.addCompetitor1ScoreTime(10);
		duelDTO.addCompetitor1Score(Score.KOTE);
		duelDTO.addCompetitor1ScoreTime(20);
		duelDTO.setFinished(true);
	}

	private void setDraw(DuelDTO duelDTO) {
		duelDTO.addCompetitor1Score(Score.MEN);
		duelDTO.addCompetitor1ScoreTime(15);
		duelDTO.addCompetitor2Score(Score.KOTE);
		duelDTO.addCompetitor2ScoreTime(30);
		duelDTO.setFinished(true);
	}

	@Override
	@AfterClass(alwaysRun = true)
	public void wipeOut() {
		super.wipeOut();
	}
}
