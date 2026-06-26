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
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

@SpringBootTest
@Test(groups = {"swissTournament10NoDrawsTest"})
public class SwissTournament10TeamsNoDrawsTest extends AbstractTestNGSpringContextTests {

	private static final String CLUB_NAME = "Swiss10Club";
	private static final String CLUB_CITY = "Swiss10City";
	private static final int MEMBERS = 3;
	private static final int TEAMS = 10;
	private static final int ROUNDS = 4;
	private static final int FIGHTS_PER_ROUND = 5;
	private static final String TOURNAMENT_NAME = "SwissTournament10TeamsNoDrawsTest";

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
		clubDTO = clubController.create(new ClubDTO(CLUB_NAME, CLUB_CITY), null, null);
	}

	@Test(dependsOnMethods = "addClub")
	public void addParticipants() {
		for (int i = 0; i < MEMBERS * TEAMS; i++) {
			participantController.create(new ParticipantDTO(String.format("S10-%04d", i), String.format("name%s", i),
					String.format("lastname%s", i), clubDTO), null, null);
		}
	}

	@Test(dependsOnMethods = "addParticipants")
	public void addTournament() {
		Assert.assertEquals(tournamentController.count(), 0);
		final TournamentDTO newTournament = new TournamentDTO(TOURNAMENT_NAME, 1, MEMBERS, TournamentType.SWISS);
		tournamentDTO = tournamentController.create(newTournament, null, null);
		Assert.assertEquals(tournamentController.count(), 1);
	}

	@Test(dependsOnMethods = "addTournament")
	public void addRoles() {
		for (ParticipantDTO competitor : participantController.get()) {
			roleController.create(new RoleDTO(tournamentDTO, competitor, RoleType.COMPETITOR), null, null);
		}
		Assert.assertEquals(roleController.count(tournamentDTO), participantController.count());
	}

	@Test(dependsOnMethods = "addRoles")
	public void addTeams() {
		int teamIndex = 0;
		TeamDTO team = null;
		int teamMember = 0;

		final List<Group> groups = groupController.getGroups(tournamentDTO, 0);
		Assert.assertEquals(groups.size(), 1);

		for (ParticipantDTO competitor : participantController.get()) {
			if (team == null) {
				teamIndex++;
				team = new TeamDTO("Team" + String.format("%02d", teamIndex), tournamentDTO);
				teamMember = 0;
			}

			team.addMember(competitor);
			team = teamController.create(team, null, null);

			if (teamMember == 0) {
				groupController.addTeams(groups.getFirst().getId(), Collections.singletonList(team), null, null);
			}

			teamMember++;
			if (teamMember >= MEMBERS) {
				team = null;
			}
		}

		Assert.assertEquals(teamController.count(tournamentDTO), TEAMS);
		Assert.assertEquals(groupController.getGroups(tournamentDTO, 0).getFirst().getTeams().size(), TEAMS);
	}

	@Test(dependsOnMethods = "addTeams")
	public void createAndAdvanceSwissRoundsWithoutDrawFights() {
		// Swiss flow by round (no draws in this scenario):
		// R0 starts with all teams tied; after finishing all fights in a round,
		// winners are promoted to higher score groups and losers remain/lower,
		// and the next round pairings are generated from those new score groups.
		// For 10 teams and 4 rounds (all decisive fights), groups evolve as:
		// - End of R0: 5 teams at 1W-0L, 5 teams at 0W-1L.
		// 1W-0L: Team01, Team03, Team05, Team07, Team09.
		// 0W-1L: Team02, Team04, Team06, Team08, Team10.
		// - End of R1: 3 teams at 2W-0L, 4 teams at 1W-1L, 3 teams at 0W-2L.
		// 2W-0L: Team01, Team05, Team09.
		// 1W-1L: Team03, Team04, Team07, Team08.
		// 0W-2L: Team02, Team06, Team10.
		// - End of R2: 2 teams at 3W-0L, 3 teams at 2W-1L, 3 teams at 1W-2L, 2 teams at
		// 0W-3L.
		// 3W-0L: Team01, Team09.
		// 2W-1L: Team04, Team05, Team08.
		// 1W-2L: Team03, Team06, Team07.
		// 0W-3L: Team02, Team10.
		// - End of R3: final ranking is resolved from 4W-0L down to 0W-4L (sin
		// empates).
		// 4W-0L: Team01.
		// 3W-1L: Team04, Team08, Team09.
		// 2W-2L: Team05, Team06.
		// 1W-3L: Team02, Team03, Team07.
		// 0W-4L: Team10.
		for (int level = 0; level < ROUNDS; level++) {
			final int roundLevel = level;
			Assert.assertEquals(getAllFights().stream().filter(Fight::isOver).count(),
					(long) roundLevel * FIGHTS_PER_ROUND);

			final List<FightDTO> createdFights = fightController.createFights(tournamentDTO.getId(), TeamsOrder.NONE,
					level, null, null);
			Assert.assertEquals(createdFights.size(), FIGHTS_PER_ROUND);

			final List<Group> roundGroups = groupController.getGroups(tournamentDTO, roundLevel);
			Assert.assertTrue(!roundGroups.isEmpty());
			final List<Fight> fightsInRound = roundGroups.stream().flatMap(group -> group.getFights().stream()).toList();
			Assert.assertEquals(fightsInRound.size(), FIGHTS_PER_ROUND);

			for (Fight fight : fightsInRound) {
				fight.getDuels().getFirst().addCompetitor1Score(Score.MEN);
				fight.getDuels().getFirst().addCompetitor1Score(Score.MEN);
				fight.getDuels().forEach(duel -> duel.setFinished(true));
				fightController.update(fightConverter.convert(new FightConverterRequest(fight)), null, null);
			}

			final List<Fight> updatedFightsInRound = groupController.getGroups(tournamentDTO, roundLevel).stream()
					.flatMap(group -> group.getFights().stream()).toList();
			Assert.assertTrue(updatedFightsInRound.stream().allMatch(Fight::isOver));
			Assert.assertTrue(updatedFightsInRound.stream().noneMatch(Fight::isDrawFight));

			// Next round is generated only after finishing all fights from current round.
			if (roundLevel < ROUNDS - 1) {
				Assert.assertEquals(
						groupController.getGroups(tournamentDTO, roundLevel + 1).stream()
								.flatMap(group -> group.getFights().stream()).count(),
						0);
			}
		}
	}

	@Test(dependsOnMethods = "createAndAdvanceSwissRoundsWithoutDrawFights")
	public void checkGroupsPerSwissRound() {
		final List<Integer> expectedGroupsByLevel = List.of(1, 2, 3, 4);
		for (int level = 0; level < ROUNDS; level++) {
			final List<Group> roundGroups = groupController.getGroups(tournamentDTO, level);
			Assert.assertEquals(roundGroups.size(), (int) expectedGroupsByLevel.get(level));
			Assert.assertEquals(roundGroups.stream().map(Group::getIndex).sorted().toList(),
					IntStream.range(0, expectedGroupsByLevel.get(level)).boxed().toList());
			Assert.assertEquals(roundGroups.stream().flatMap(group -> group.getFights().stream()).count(), FIGHTS_PER_ROUND);
		}
	}

	@Test(dependsOnMethods = "checkGroupsPerSwissRound")
	public void checkFinalRanking() {
		final List<ScoreOfTeam> ranking = rankingProvider
				.getTeamsScoreRanking(tournamentConverter.reverse(tournamentDTO));
		Assert.assertEquals(ranking.size(), TEAMS);
		Assert.assertNotNull(ranking.getFirst().getTeam());

		final List<Fight> allFights = getAllFights();
		Assert.assertEquals(allFights.size(), ROUNDS * FIGHTS_PER_ROUND);
		Assert.assertTrue(allFights.stream().allMatch(Fight::isOver));
		Assert.assertTrue(allFights.stream().noneMatch(Fight::isDrawFight));

		final Map<String, Integer> fightsByTeamName = new HashMap<>();
		allFights.forEach(fight -> {
			fightsByTeamName.merge(fight.getTeam1().getName(), 1, Integer::sum);
			fightsByTeamName.merge(fight.getTeam2().getName(), 1, Integer::sum);
		});

		Assert.assertEquals(fightsByTeamName.size(), TEAMS);
		ranking.forEach(score -> Assert.assertEquals((int) fightsByTeamName.get(score.getTeam().getName()), ROUNDS));

		// Ranking groups must match the exact team distribution documented in R3
		// comments.
		assertTeamsWithWins(ranking, 4, List.of("Team01"));
		assertTeamsWithWins(ranking, 3, List.of("Team04", "Team08", "Team09"));
		assertTeamsWithWins(ranking, 2, List.of("Team05", "Team06"));
		assertTeamsWithWins(ranking, 1, List.of("Team02", "Team03", "Team07"));
		assertTeamsWithWins(ranking, 0, List.of("Team10"));
	}

	private void assertTeamsWithWins(List<ScoreOfTeam> ranking, int wins, List<String> expectedTeamNames) {
		final List<String> actualTeamNames = ranking.stream().filter(score -> score.getWonFights() == wins)
				.map(score -> score.getTeam().getName()).sorted().toList();
		final List<String> expectedSorted = expectedTeamNames.stream().sorted().collect(Collectors.toList());
		Assert.assertEquals(actualTeamNames, expectedSorted, "Actual teams for wins=" + wins + ": " + actualTeamNames);
	}

	private List<Fight> getAllFights() {
		final List<Fight> fights = new ArrayList<>();
		for (int level = 0; level < ROUNDS; level++) {
			fights.addAll(groupController.getGroups(tournamentDTO, level).stream().flatMap(group -> group.getFights().stream()).toList());
		}
		return fights;
	}

	@AfterClass(alwaysRun = true)
	public void deleteTournament() {
		if (tournamentDTO != null) {
			groupController.delete(tournamentDTO);
			fightController.delete(tournamentDTO);
			duelController.delete(tournamentDTO);
			teamController.delete(tournamentDTO);
			roleController.delete(tournamentDTO);
			tournamentController.delete(tournamentDTO, null, null);
		}
		participantController.deleteAll();
		if (clubDTO != null) {
			clubController.delete(clubDTO, null, null);
		}
		Assert.assertEquals(fightController.count(), 0);
		Assert.assertEquals(duelController.count(), 0);
	}
}
