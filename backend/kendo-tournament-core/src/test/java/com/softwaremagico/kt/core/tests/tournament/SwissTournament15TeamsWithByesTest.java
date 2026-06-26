package com.softwaremagico.kt.core.tests.tournament;

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

import com.softwaremagico.kt.core.controller.ClubController;
import com.softwaremagico.kt.core.controller.DuelController;
import com.softwaremagico.kt.core.controller.FightController;
import com.softwaremagico.kt.core.controller.GroupController;
import com.softwaremagico.kt.core.controller.ParticipantController;
import com.softwaremagico.kt.core.controller.RoleController;
import com.softwaremagico.kt.core.controller.TeamController;
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.FightConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.models.FightConverterRequest;
import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.core.providers.RankingProvider;
import com.softwaremagico.kt.core.score.ScoreOfTeam;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SpringBootTest
@Test(groups = {"swissTournament15ByesTest"})
public class SwissTournament15TeamsWithByesTest extends AbstractTestNGSpringContextTests {

	private static final String CLUB_NAME = "Swiss15Club";
	private static final String CLUB_CITY = "Swiss15City";
	private static final int MEMBERS = 3;
	private static final int TEAMS = 15;
	private static final int ROUNDS = 4;
	private static final int FIGHTS_PER_ROUND = 7;
	private static final String TOURNAMENT_NAME = "SwissTournament15TeamsWithByesTest";
	private static final String EXPECTED_BYE_TEAM = "Team15";

	@Autowired
	private TournamentController tournamentController;

	@Autowired
	private TournamentConverter tournamentConverter;

	@Autowired
	private ParticipantController participantController;

	@Autowired
	private RoleController roleController;

	@Autowired
	private TeamController teamController;

	@Autowired
	private ClubController clubController;

	@Autowired
	private RankingProvider rankingProvider;

	@Autowired
	private GroupController groupController;

	@Autowired
	private FightController fightController;

	@Autowired
	private DuelController duelController;

	@Autowired
	private FightConverter fightConverter;

	private ClubDTO clubDTO;
	private TournamentDTO tournamentDTO;

	@Test
	public void addClub() {
		this.clubDTO = this.clubController.create(new ClubDTO(CLUB_NAME, CLUB_CITY), null, null);
	}

	@Test(dependsOnMethods = "addClub")
	public void addParticipants() {
		for (int i = 0; i < MEMBERS * TEAMS; i++) {
			this.participantController.create(new ParticipantDTO(String.format("S15-%04d", i),
					String.format("name%s", i), String.format("lastname%s", i), this.clubDTO), null, null);
		}
	}

	@Test(dependsOnMethods = "addParticipants")
	public void addTournament() {
		Assert.assertEquals(this.tournamentController.count(), 0);
		final TournamentDTO newTournament = new TournamentDTO(TOURNAMENT_NAME, 1, MEMBERS, TournamentType.SWISS);
		this.tournamentDTO = this.tournamentController.create(newTournament, null, null);
		Assert.assertEquals(this.tournamentController.count(), 1);
	}

	@Test(dependsOnMethods = "addTournament")
	public void addRoles() {
		for (final ParticipantDTO competitor : this.participantController.get()) {
			this.roleController.create(new RoleDTO(this.tournamentDTO, competitor, RoleType.COMPETITOR), null, null);
		}
		Assert.assertEquals(this.roleController.count(this.tournamentDTO), this.participantController.count());
	}

	@Test(dependsOnMethods = "addRoles")
	public void addTeams() {
		int teamIndex = 0;
		TeamDTO team = null;
		int teamMember = 0;

		final List<Group> groups = this.groupController.getGroups(this.tournamentDTO, 0);
		Assert.assertEquals(groups.size(), 1);

		for (final ParticipantDTO competitor : this.participantController.get()) {
			if (team == null) {
				teamIndex++;
				team = new TeamDTO("Team" + String.format("%02d", teamIndex), this.tournamentDTO);
				teamMember = 0;
			}

			team.addMember(competitor);
			team = this.teamController.create(team, null, null);

			if (teamMember == 0) {
				this.groupController.addTeams(groups.getFirst().getId(), Collections.singletonList(team), null, null);
			}

			teamMember++;
			if (teamMember >= MEMBERS) {
				team = null;
			}
		}

		Assert.assertEquals(this.teamController.count(this.tournamentDTO), TEAMS);
		Assert.assertEquals(this.groupController.getGroups(this.tournamentDTO, 0).getFirst().getTeams().size(), TEAMS);
	}

	@Test(dependsOnMethods = "addTeams")
	public void createAndAdvanceSwissRoundsWithByes() {
		// Swiss flow by round with odd number of teams:
		// each round has one bye (1 team without fight), and pairings are generated
		// with the rest.
		// In this deterministic scenario (team1 always wins), Team15 is always the
		// lowest-ranked team
		// at pairing time and receives the bye in every round.
		// For 15 teams and 4 rounds, score groups evolve like this (W=win, L=loss):
		// - End of R0: 7 teams at 1W-0L, 7 teams at 0W-1L, 1 team with bye.
		// 1W-0L: Team01, Team03, Team05, Team07, Team09, Team11, Team13.
		// 0W-1L: Team02, Team04, Team06, Team08, Team10, Team12, Team14.
		// Bye in R0: Team15 (remains at 0W-0L).
		// - End of R1: 4 teams at 2W-0L, 6 teams at 1W-1L, 4 teams at 0W-2L, 1 team
		// with bye.
		// 2W-0L: Team01, Team05, Team09, Team13.
		// 1W-1L: Team03, Team04, Team07, Team08, Team11, Team12.
		// 0W-2L: Team02, Team06, Team10, Team14.
		// Bye in R1: Team15 (still 0W-0L).
		// - End of R2: 2 teams at 3W-0L, 5 teams at 2W-1L, 3 teams at 1W-2L, 4 teams at
		// 0W-3L, 1 team with bye.
		// 3W-0L: Team01, Team09.
		// 2W-1L: Team03, Team04, Team05, Team11, Team12.
		// 1W-2L: Team07, Team08, Team10.
		// 0W-3L: Team02, Team06, Team13, Team14.
		// Bye in R2: Team15 (still 0W-0L).
		// - End of R3: final distribution with byes considered.
		// 4W-0L: Team01.
		// 3W-1L: Team03, Team04, Team09, Team12.
		// 2W-2L: Team05, Team07, Team08, Team11, Team13.
		// 1W-3L: Team06, Team10.
		// 0W-4L: Team02, Team14.
		// Bye in R3: Team15 (finishes 0W-0L with 4 byes).
		final List<String> allTeamNames = this.groupController.getGroups(this.tournamentDTO, 0).getFirst().getTeams()
				.stream().map(Team::getName).sorted().toList();

		for (int level = 0; level < ROUNDS; level++) {
			final int roundLevel = level;
			final Group groupBeforeRound = this.groupController.getGroups(this.tournamentDTO, 0).getFirst();
			Assert.assertEquals(groupBeforeRound.getFights().stream().filter(Fight::isOver).count(),
					(long) roundLevel * FIGHTS_PER_ROUND);

			final List<FightDTO> createdFights = this.fightController.createFights(this.tournamentDTO.getId(),
					TeamsOrder.NONE, level, null, null);
			Assert.assertEquals(createdFights.size(), FIGHTS_PER_ROUND);

			final Group group = this.groupController.getGroups(this.tournamentDTO, 0).getFirst();
			final List<Fight> fightsInRound = group.getFights().stream().filter(fight -> fight.getLevel() == roundLevel)
					.toList();
			Assert.assertEquals(fightsInRound.size(), FIGHTS_PER_ROUND);

			final String byeTeamName = this.getByeTeamName(allTeamNames, fightsInRound);
			Assert.assertEquals(byeTeamName, EXPECTED_BYE_TEAM);

			for (final Fight fight : fightsInRound) {
				Assert.assertNotNull(fight.getTeam1());
				Assert.assertNotNull(fight.getTeam2());
				fight.getDuels().getFirst().addCompetitor1Score(Score.MEN);
				fight.getDuels().getFirst().addCompetitor1Score(Score.MEN);
				fight.getDuels().forEach(duel -> duel.setFinished(true));
				this.fightController.update(this.fightConverter.convert(new FightConverterRequest(fight)), null, null);
			}

			final Group updatedGroup = this.groupController.getGroups(this.tournamentDTO, 0).getFirst();
			Assert.assertEquals(
					updatedGroup.getFights().stream().filter(fight -> fight.getLevel() == roundLevel).count(),
					FIGHTS_PER_ROUND);

			// Next round is generated only after finishing all fights from current round.
			if (roundLevel < ROUNDS - 1) {
				Assert.assertEquals(
						updatedGroup.getFights().stream().filter(fight -> fight.getLevel() == roundLevel + 1).count(),
						0);
			}
		}
	}

	@Test(dependsOnMethods = "createAndAdvanceSwissRoundsWithByes")
	public void checkFinalRanking() {
		final List<ScoreOfTeam> ranking = this.rankingProvider
				.getTeamsScoreRanking(this.tournamentConverter.reverse(this.tournamentDTO));
		Assert.assertEquals(ranking.size(), TEAMS);
		Assert.assertNotNull(ranking.getFirst().getTeam());
		Assert.assertNotNull(ranking.getFirst().getTeam().getName());

		final Group group = this.groupController.getGroups(this.tournamentDTO, 0).getFirst();
		Assert.assertEquals(group.getFights().size(), ROUNDS * FIGHTS_PER_ROUND);
		Assert.assertTrue(
				group.getFights().stream().allMatch(fight -> fight.getTeam1() != null && fight.getTeam2() != null));

		final Map<String, Integer> fightsByTeamName = new HashMap<>();
		ranking.forEach(score -> fightsByTeamName.put(score.getTeam().getName(), 0));
		group.getFights().forEach(fight -> {
			fightsByTeamName.merge(fight.getTeam1().getName(), 1, Integer::sum);
			fightsByTeamName.merge(fight.getTeam2().getName(), 1, Integer::sum);
		});

		Assert.assertEquals(fightsByTeamName.size(), TEAMS);
		ranking.forEach(score -> {
			final int expectedFights = EXPECTED_BYE_TEAM.equals(score.getTeam().getName()) ? 0 : ROUNDS;
			Assert.assertEquals((int) fightsByTeamName.get(score.getTeam().getName()), expectedFights);
		});

		// Ranking groups must match the exact team distribution documented in R3
		// comments.
		this.assertTeamsWithWins(ranking, 4, List.of("Team01"));
		this.assertTeamsWithWins(ranking, 3, List.of("Team03", "Team04", "Team09", "Team12"));
		this.assertTeamsWithWins(ranking, 2, List.of("Team05", "Team07", "Team08", "Team11", "Team13"));
		this.assertTeamsWithWins(ranking, 1, List.of("Team06", "Team10"));
		this.assertTeamsWithWins(ranking, 0, List.of("Team02", "Team14", "Team15"));

		final ScoreOfTeam byeTeamScore = ranking.stream()
				.filter(score -> EXPECTED_BYE_TEAM.equals(score.getTeam().getName())).findFirst().orElseThrow();
		Assert.assertEquals((int) byeTeamScore.getFightsDone(), 0);
	}

	private String getByeTeamName(List<String> allTeamNames, List<Fight> fightsInRound) {
		final Set<String> teamsInRound = new HashSet<>();
		fightsInRound.forEach(fight -> {
			teamsInRound.add(fight.getTeam1().getName());
			teamsInRound.add(fight.getTeam2().getName());
		});

		final List<String> byeTeams = allTeamNames.stream().filter(teamName -> !teamsInRound.contains(teamName))
				.toList();
		Assert.assertEquals(byeTeams.size(), 1);
		return byeTeams.getFirst();
	}

	private void assertTeamsWithWins(List<ScoreOfTeam> ranking, int wins, List<String> expectedTeamNames) {
		final List<String> actualTeamNames = ranking.stream().filter(score -> score.getWonFights() == wins)
				.map(score -> score.getTeam().getName()).sorted().toList();
		final List<String> expectedSorted = expectedTeamNames.stream().sorted().toList();
		Assert.assertEquals(actualTeamNames, expectedSorted);
	}

	@AfterClass(alwaysRun = true)
	public void deleteTournament() {
		if (this.tournamentDTO != null) {
			this.groupController.delete(this.tournamentDTO);
			this.fightController.delete(this.tournamentDTO);
			this.duelController.delete(this.tournamentDTO);
			this.teamController.delete(this.tournamentDTO);
			this.roleController.delete(this.tournamentDTO);
			this.tournamentController.delete(this.tournamentDTO, null, null);
		}
		this.participantController.deleteAll();
		if (this.clubDTO != null) {
			this.clubController.delete(this.clubDTO, null, null);
		}
		Assert.assertEquals(this.fightController.count(), 0);
		Assert.assertEquals(this.duelController.count(), 0);
	}
}
