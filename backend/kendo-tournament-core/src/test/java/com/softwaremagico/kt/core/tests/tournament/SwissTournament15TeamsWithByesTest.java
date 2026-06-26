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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

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
	private static final List<String> EXPECTED_BYES_PER_ROUND = List.of("Team15", "Team14", "Team12", "Team08");

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
		// Swiss flow with odd number of teams and rotating byes (no repeated bye until
		// needed).
		// In this deterministic scenario (team1 always wins), the bye goes to:
		// R0 -> Team15, R1 -> Team14, R2 -> Team12, R3 -> Team08.
		// That means each of those teams receives exactly one bye, and byes count as
		// Swiss wins.
		// Final points groups are therefore influenced by byes and end as:
		// 4W: Team01.
		// 3W: Team02, Team06, Team09, Team13, Team14.
		// 2W: Team05, Team07, Team10, Team12.
		// 1W: Team03, Team04, Team08, Team11, Team15.
		final List<String> allTeamNames = this.groupController.getGroups(this.tournamentDTO, 0).getFirst().getTeams()
				.stream().map(Team::getName).sorted().toList();
		final List<String> byeTeamsByRound = new ArrayList<>();

		for (int level = 0; level < ROUNDS; level++) {
			final int roundLevel = level;
			Assert.assertEquals(this.getAllFights().stream().filter(Fight::isOver).count(),
					(long) roundLevel * FIGHTS_PER_ROUND);

			final List<FightDTO> createdFights = this.fightController.createFights(this.tournamentDTO.getId(),
					TeamsOrder.NONE, level, null, null);
			Assert.assertEquals(createdFights.size(), FIGHTS_PER_ROUND);

			final List<Group> roundGroups = this.groupController.getGroups(this.tournamentDTO, roundLevel);
			Assert.assertTrue(!roundGroups.isEmpty());
			final List<Fight> fightsInRound = roundGroups.stream().flatMap(group -> group.getFights().stream()).toList();
			Assert.assertEquals(fightsInRound.size(), FIGHTS_PER_ROUND);

			final String byeTeamName = this.getByeTeamName(allTeamNames, fightsInRound);
			byeTeamsByRound.add(byeTeamName);

			for (final Fight fight : fightsInRound) {
				Assert.assertNotNull(fight.getTeam1());
				Assert.assertNotNull(fight.getTeam2());
				fight.getDuels().getFirst().addCompetitor1Score(Score.MEN);
				fight.getDuels().getFirst().addCompetitor1Score(Score.MEN);
				fight.getDuels().forEach(duel -> duel.setFinished(true));
				this.fightController.update(this.fightConverter.convert(new FightConverterRequest(fight)), null, null);
			}

			Assert.assertEquals(
					this.groupController.getGroups(this.tournamentDTO, roundLevel).stream().flatMap(group -> group.getFights().stream()).count(),
					FIGHTS_PER_ROUND);

			// Next round is generated only after finishing all fights from current round.
			if (roundLevel < ROUNDS - 1) {
				Assert.assertEquals(
						this.groupController.getGroups(this.tournamentDTO, roundLevel + 1).stream()
								.flatMap(group -> group.getFights().stream()).count(),
						0);
			}
		}

		Assert.assertEquals(new HashSet<>(byeTeamsByRound).size(), ROUNDS);
		Assert.assertEquals(byeTeamsByRound, EXPECTED_BYES_PER_ROUND, "Actual byes per round: " + byeTeamsByRound);
	}

	@Test(dependsOnMethods = "createAndAdvanceSwissRoundsWithByes")
	public void checkGroupsPerSwissRound() {
		final List<Integer> expectedGroupsByLevel = List.of(1, 2, 3, 4);
		for (int level = 0; level < ROUNDS; level++) {
			final List<Group> roundGroups = this.groupController.getGroups(this.tournamentDTO, level);
			Assert.assertEquals(roundGroups.size(), (int) expectedGroupsByLevel.get(level));
			Assert.assertEquals(roundGroups.stream().map(Group::getIndex).sorted().toList(),
					IntStream.range(0, expectedGroupsByLevel.get(level)).boxed().toList());
			Assert.assertEquals(roundGroups.stream().flatMap(group -> group.getFights().stream()).count(), FIGHTS_PER_ROUND);
		}
	}

	@Test(dependsOnMethods = "checkGroupsPerSwissRound")
	public void checkFinalRanking() {
		final List<ScoreOfTeam> ranking = this.rankingProvider
				.getTeamsScoreRanking(this.tournamentConverter.reverse(this.tournamentDTO));
		Assert.assertEquals(ranking.size(), TEAMS);

		final List<Fight> allFights = this.getAllFights();
		Assert.assertEquals(allFights.size(), ROUNDS * FIGHTS_PER_ROUND);

		final Map<String, Integer> fightsByTeamName = new HashMap<>();
		ranking.forEach(score -> fightsByTeamName.put(score.getTeam().getName(), 0));
		allFights.forEach(fight -> {
			fightsByTeamName.merge(fight.getTeam1().getName(), 1, Integer::sum);
			fightsByTeamName.merge(fight.getTeam2().getName(), 1, Integer::sum);
		});

		final Map<String, Integer> byeCountByTeam = this.getByeCountByTeam(allFights);
		ranking.forEach(score -> Assert.assertEquals((int) fightsByTeamName.get(score.getTeam().getName())
				+ byeCountByTeam.getOrDefault(score.getTeam().getName(), 0), ROUNDS));

		// Ranking groups must match the exact final distribution for this deterministic
		// setup.
		this.assertTeamsWithWins(ranking, 4, List.of("Team01"));
		this.assertTeamsWithWins(ranking, 3, List.of("Team02", "Team06", "Team09", "Team13", "Team14"));
		this.assertTeamsWithWins(ranking, 2, List.of("Team05", "Team07", "Team10", "Team12"));
		this.assertTeamsWithWins(ranking, 1, List.of("Team03", "Team04", "Team08", "Team11", "Team15"));
		this.assertTeamsWithWins(ranking, 0, List.of());

		final Map<String, Integer> teamsWithBye = byeCountByTeam.entrySet().stream()
				.filter(entry -> entry.getValue() > 0)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		Assert.assertEquals(teamsWithBye, Map.of("Team08", 1, "Team12", 1, "Team14", 1, "Team15", 1));
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

	private Map<String, Integer> getByeCountByTeam(List<Fight> allFights) {
		final Map<Integer, Set<String>> teamsByRound = allFights.stream()
				.collect(Collectors.groupingBy(Fight::getLevel, Collectors.flatMapping(
						fight -> java.util.stream.Stream.of(fight.getTeam1().getName(), fight.getTeam2().getName()),
						Collectors.toSet())));
		final Set<String> allTeams = this.groupController.getGroups(this.tournamentDTO, 0).stream()
				.flatMap(group -> group.getTeams().stream()).map(Team::getName).collect(Collectors.toSet());
		final Map<String, Integer> byeCountByTeam = new HashMap<>();
		allTeams.forEach(team -> byeCountByTeam.put(team, 0));

		for (final Set<String> teamsInRound : teamsByRound.values()) {
			final Set<String> byeTeams = new HashSet<>(allTeams);
			byeTeams.removeAll(teamsInRound);
			Assert.assertEquals(byeTeams.size(), 1);
			final String byeTeam = byeTeams.iterator().next();
			byeCountByTeam.computeIfPresent(byeTeam, (ignoredTeam, value) -> value + 1);
		}
		return byeCountByTeam;
	}

	private void assertTeamsWithWins(List<ScoreOfTeam> ranking, int wins, List<String> expectedTeamNames) {
		final List<String> actualTeamNames = ranking.stream().filter(score -> score.getWonFights() == wins)
				.map(score -> score.getTeam().getName()).sorted().toList();
		final List<String> expectedSorted = expectedTeamNames.stream().sorted().toList();
		Assert.assertEquals(actualTeamNames, expectedSorted);
	}

	private List<Fight> getAllFights() {
		final List<Fight> fights = new ArrayList<>();
		for (int level = 0; level < ROUNDS; level++) {
			fights.addAll(this.groupController.getGroups(this.tournamentDTO, level).stream().flatMap(group -> group.getFights().stream()).toList());
		}
		return fights;
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
