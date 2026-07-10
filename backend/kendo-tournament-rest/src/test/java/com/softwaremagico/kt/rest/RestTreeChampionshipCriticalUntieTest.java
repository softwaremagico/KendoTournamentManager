package com.softwaremagico.kt.rest;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwaremagico.kt.core.controller.ClubController;
import com.softwaremagico.kt.core.controller.DuelController;
import com.softwaremagico.kt.core.controller.FightController;
import com.softwaremagico.kt.core.controller.GroupController;
import com.softwaremagico.kt.core.controller.ParticipantController;
import com.softwaremagico.kt.core.controller.RoleController;
import com.softwaremagico.kt.core.controller.TeamController;
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.TournamentExtraPropertyController;
import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.core.controller.models.DuelDTO;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.controller.models.TournamentExtraPropertyDTO;
import com.softwaremagico.kt.persistence.entities.DuelType;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import com.softwaremagico.kt.persistence.values.TournamentType;
import com.softwaremagico.kt.rest.controllers.AuthenticatedUserController;
import com.softwaremagico.kt.rest.security.dto.AuthRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Smoke / integration tests for the automatic critical-untie fight flow.
 *
 * <p>
 * Scenario: TREE tournament, 16 teams in 4 groups. Groups 0-2 produce clear
 * winners; group 3 ends in a draw at the deciding position. The test verifies:
 * <ol>
 * <li>Trying to advance levels while the draw exists is blocked (HTTP
 * 204).</li>
 * <li>The backend auto-creates an UNDRAW duel in the draw group.</li>
 * <li>The UNDRAW duel is visible in the GroupDTO returned by the REST API.</li>
 * <li>Resolving the UNDRAW duel via REST clears the draw.</li>
 * <li>After resolution, next-level fights can be generated successfully (HTTP
 * 2xx).</li>
 * <li>Level-1 groups contain fights, confirming bracket progression is
 * correct.</li>
 * </ol>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Test(groups = {"restTreeChampionshipCriticalUntieTest"})
public class RestTreeChampionshipCriticalUntieTest extends AbstractTestNGSpringContextTests {

	private static final String USER_NAME = "admin-untie-smoke";
	private static final String USER_FIRST_NAME = "Untie";
	private static final String USER_LAST_NAME = "Smoke";
	private static final String USER_PASSWORD = "asd123";
	private static final String[] USER_ROLES = {"admin", "viewer"};

	private static final int MEMBERS = 3;
	private static final int TEAMS = 16;
	private static final int GROUPS = 4;
	private static final String TOURNAMENT_NAME = "restTreeCriticalUntieSmoke";
	private static final String CLUB_NAME = "Untie Smoke Club";
	private static final String CLUB_CITY = "Bilbao";

	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private AuthenticatedUserController authenticatedUserController;
	@Autowired
	private GroupController groupController;
	@Autowired
	private FightController fightController;
	@Autowired
	private DuelController duelController;
	@Autowired
	private RoleController roleController;
	@Autowired
	private TeamController teamController;
	@Autowired
	private ParticipantController participantController;
	@Autowired
	private TournamentExtraPropertyController tournamentExtraPropertyController;
	@Autowired
	private TournamentController tournamentController;
	@Autowired
	private ClubController clubController;

	private String jwtToken;
	private ClubDTO clubDTO;
	private TournamentDTO tournamentDTO;
	/** ID of the group (level 0, index 3) that will end in a critical draw. */
	private Integer drawGroupId;

	// ─── Helpers ────────────────────────────────────────────────────────────────

	private <T> String toJson(T object) throws JsonProcessingException {
		return this.objectMapper.writeValueAsString(object);
	}

	private <T> T fromJson(String payload, Class<T> clazz) throws IOException {
		return this.objectMapper.readValue(payload, clazz);
	}

	private List<GroupDTO> getLevel0Groups() throws Exception {
		final MvcResult result = this.mockMvc
				.perform(get("/groups/tournaments/{id}", this.tournamentDTO.getId())
						.contentType(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + this.jwtToken))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();
		return Arrays.stream(this.objectMapper.readValue(result.getResponse().getContentAsString(), GroupDTO[].class))
				.filter(g -> Integer.valueOf(0).equals(g.getLevel()))
				.sorted(Comparator.comparingInt(GroupDTO::getIndex)).toList();
	}

	// ─── Setup ──────────────────────────────────────────────────────────────────

	@BeforeClass
	public void setAuthentication() throws Exception {
		this.authenticatedUserController.createUser(null, USER_NAME, USER_FIRST_NAME, USER_LAST_NAME, USER_PASSWORD,
				USER_ROLES);
		final AuthRequest request = new AuthRequest();
		request.setUsername(USER_NAME);
		request.setPassword(USER_PASSWORD);
		final MvcResult loginResult = this.mockMvc
				.perform(post("/auth/public/login").contentType(MediaType.APPLICATION_JSON)
						.content(this.toJson(request)).with(csrf()))
				.andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
		this.jwtToken = loginResult.getResponse().getHeader(HttpHeaders.AUTHORIZATION);
		Assert.assertNotNull(this.jwtToken, "JWT token must be obtained after login");
	}

	// ─── Data preparation ───────────────────────────────────────────────────────

	@Test
	public void addClub() throws Exception {
		final MvcResult result = this.mockMvc
				.perform(post("/clubs").contentType(MediaType.APPLICATION_JSON)
						.header("Authorization", "Bearer " + this.jwtToken)
						.content(this.toJson(new ClubDTO(CLUB_NAME, CLUB_CITY))).with(csrf()))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();
		this.clubDTO = this.fromJson(result.getResponse().getContentAsString(), ClubDTO.class);
		Assert.assertEquals(this.clubDTO.getName(), CLUB_NAME);
	}

	@Test(dependsOnMethods = "addClub")
	public void addParticipants() throws Exception {
		for (int i = 0; i < MEMBERS * TEAMS; i++) {
			this.mockMvc.perform(post("/participants").contentType(MediaType.APPLICATION_JSON)
					.header("Authorization", "Bearer " + this.jwtToken)
					.content(this.toJson(new ParticipantDTO(String.format("9000%s", i), String.format("smoke%s", i),
							String.format("untie%s", i), this.clubDTO)))
					.with(csrf())).andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
		}
	}

	@Test(dependsOnMethods = "addParticipants")
	public void addTournament() throws Exception {
		final MvcResult result = this.mockMvc.perform(post("/tournaments").contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + this.jwtToken)
				.content(this.toJson(new TournamentDTO(TOURNAMENT_NAME, 1, MEMBERS, TournamentType.TREE, null)))
				.with(csrf())).andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();
		this.tournamentDTO = this.fromJson(result.getResponse().getContentAsString(), TournamentDTO.class);
		Assert.assertEquals(this.tournamentDTO.getType(), TournamentType.TREE);
		// Reduce fight count so the draw is deterministic
		this.tournamentExtraPropertyController.create(
				new TournamentExtraPropertyDTO(this.tournamentDTO, TournamentExtraPropertyKey.MAXIMIZE_FIGHTS, "false"),
				null, null);
	}

	@Test(dependsOnMethods = "addTournament")
	public void addRoles() throws Exception {
		for (final ParticipantDTO competitor : this.participantController.get()) {
			this.mockMvc.perform(post("/roles").contentType(MediaType.APPLICATION_JSON)
					.header("Authorization", "Bearer " + this.jwtToken)
					.content(this.toJson(new RoleDTO(this.tournamentDTO, competitor, RoleType.COMPETITOR)))
					.with(csrf())).andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
		}
	}

	@Test(dependsOnMethods = "addTournament")
	public void add4Groups() throws Exception {
		// The tournament auto-creates one group at level 0; add 3 more to make 4.
		for (int i = 1; i < GROUPS; i++) {
			final GroupDTO groupDTO = new GroupDTO();
			groupDTO.setTournament(this.tournamentDTO);
			groupDTO.setIndex(i);
			groupDTO.setLevel(0);
			groupDTO.setShiaijo(0);
			groupDTO.setTeams(new java.util.ArrayList<>());
			groupDTO.setFights(new java.util.ArrayList<>());
			this.mockMvc.perform(post("/groups").contentType(MediaType.APPLICATION_JSON)
					.header("Authorization", "Bearer " + this.jwtToken).content(this.toJson(groupDTO)).with(csrf()))
					.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
		}
		Assert.assertEquals(this.getLevel0Groups().size(), GROUPS,
				"There must be exactly " + GROUPS + " groups at level 0");
	}

	@Test(dependsOnMethods = {"addRoles", "add4Groups"})
	public void addTeams() throws Exception {
		final List<GroupDTO> level0Groups = this.getLevel0Groups();

		final MvcResult participantsResult = this.mockMvc
				.perform(get("/participants").contentType(MediaType.APPLICATION_JSON).header("Authorization",
						"Bearer " + this.jwtToken))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();
		final ParticipantDTO[] participants = this.objectMapper
				.readValue(participantsResult.getResponse().getContentAsString(), ParticipantDTO[].class);

		int teamIndex = 0;
		TeamDTO team = null;
		int teamMember = 0;

		for (final ParticipantDTO competitor : participants) {
			if (team == null) {
				teamIndex++;
				team = new TeamDTO("SmokeTeam" + String.format("%02d", teamIndex), this.tournamentDTO);
				teamMember = 0;
			}
			team.getMembers().add(competitor);

			final MvcResult teamResult = this.mockMvc
					.perform(put("/teams").contentType(MediaType.APPLICATION_JSON)
							.header("Authorization", "Bearer " + this.jwtToken).content(this.toJson(team)).with(csrf()))
					.andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();
			team = this.fromJson(teamResult.getResponse().getContentAsString(), TeamDTO.class);

			if (teamMember == 0) {
				final int groupIndex = (teamIndex - 1) / (TEAMS / GROUPS);
				this.mockMvc.perform(patch("/groups/{groupId}/teams/add", level0Groups.get(groupIndex).getId())
						.contentType(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + this.jwtToken)
						.content(this.toJson(Collections.singleton(team))).with(csrf()))
						.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
			}
			teamMember++;
			if (teamMember >= MEMBERS) {
				team = null;
			}
		}
		Assert.assertEquals(this.teamController.count(this.tournamentDTO), TEAMS);
	}

	@Test(dependsOnMethods = "addTeams")
	public void createFightsLevel0() throws Exception {
		final MvcResult result = this.mockMvc
				.perform(
						put("/fights/create/tournaments/{tournamentId}/levels/{levelId}", this.tournamentDTO.getId(), 0)
								.contentType(MediaType.APPLICATION_JSON)
								.header("Authorization", "Bearer " + this.jwtToken).with(csrf()))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();
		final FightDTO[] fights = this.objectMapper.readValue(result.getResponse().getContentAsString(),
				FightDTO[].class);
		Assert.assertEquals(fights.length, 4 * GROUPS, "Expected 4 fights per group × " + GROUPS + " groups");
	}

	/**
	 * Groups 0-2 get a clear winner (competitor1 of their first fight wins 2-0).
	 * Group 3 ends in a critical draw: fight[0].team1 and fight[1].team1 each win
	 * their respective fight with identical stats (1 fight win, 1 duel win, 2
	 * hits). All remaining fights end 0-0. This guarantees EXACTLY two teams tied
	 * at sortingIndex=0 regardless of the specific fight pairing produced by the
	 * engine.
	 */
	@Test(dependsOnMethods = "createFightsLevel0")
	public void solveFightsWithDrawInLastGroup() throws Exception {
		final List<GroupDTO> level0Groups = this.getLevel0Groups();

		// Groups 0, 1, 2 — clear winner via competitor1 scoring twice in first fight
		for (int gi = 0; gi < 3; gi++) {
			final List<FightDTO> fights = level0Groups.get(gi).getFights();
			Assert.assertFalse(fights.isEmpty(), "Group " + gi + " must have fights");
			Assert.assertFalse(fights.getFirst().getDuels().isEmpty(), "Fight must have duels");
			fights.getFirst().getDuels().getFirst().getCompetitor1Score().add(Score.MEN);
			fights.getFirst().getDuels().getFirst().getCompetitor1Score().add(Score.MEN);
			for (final FightDTO fight : fights) {
				fight.getDuels().forEach(d -> d.setFinished(true));
				this.mockMvc.perform(put("/fights").contentType(MediaType.APPLICATION_JSON)
						.header("Authorization", "Bearer " + this.jwtToken).content(this.toJson(fight)).with(csrf()))
						.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
			}
		}

		// Group 3 — critical draw:
		// fight[0].team1 wins fight[0] with 2 MEN (identical to fight[1].team1 winning
		// fight[1]).
		// All other fights draw 0-0. Result: exactly 2 teams at sortingIndex=0.
		final List<FightDTO> drawFights = level0Groups.get(3).getFights();
		Assert.assertTrue(drawFights.size() >= 2, "Group 3 must have at least 2 fights");
		Assert.assertFalse(drawFights.getFirst().getDuels().isEmpty(), "Fight 0 must have duels");
		Assert.assertFalse(drawFights.get(1).getDuels().isEmpty(), "Fight 1 must have duels");

		// fight[0]: team1 wins 2-0 → fight[0].team1 gets 1 fight win, 2 hits
		drawFights.getFirst().getDuels().getFirst().getCompetitor1Score().add(Score.MEN);
		drawFights.getFirst().getDuels().getFirst().getCompetitor1Score().add(Score.MEN);
		// fight[1]: team1 wins 2-0 → fight[1].team1 gets 1 fight win, 2 hits (different
		// team!)
		drawFights.get(1).getDuels().getFirst().getCompetitor1Score().add(Score.MEN);
		drawFights.get(1).getDuels().getFirst().getCompetitor1Score().add(Score.MEN);

		this.drawGroupId = level0Groups.get(3).getId();
		for (final FightDTO fight : drawFights) {
			fight.getDuels().forEach(d -> d.setFinished(true));
			this.mockMvc.perform(put("/fights").contentType(MediaType.APPLICATION_JSON)
					.header("Authorization", "Bearer " + this.jwtToken).content(this.toJson(fight)).with(csrf()))
					.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
		}
	}

	/**
	 * Intermediate verification: confirms that group 3's ranking shows exactly 2
	 * teams tied at sortingIndex=0 before the smoke tests run.
	 */
	@Test(dependsOnMethods = "solveFightsWithDrawInLastGroup")
	public void verifyGroup3HasExactly2TeamsTiedAtPosition0() throws Exception {
		Assert.assertNotNull(this.drawGroupId, "drawGroupId must be set");
		final MvcResult result = this.mockMvc
				.perform(get("/rankings/teams/groups/{groupId}", this.drawGroupId)
						.contentType(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + this.jwtToken))
				.andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
		final com.softwaremagico.kt.core.controller.models.ScoreOfTeamDTO[] scores = this.objectMapper.readValue(
				result.getResponse().getContentAsString(),
				com.softwaremagico.kt.core.controller.models.ScoreOfTeamDTO[].class);
		final long teamsAtPosition0 = Arrays.stream(scores).filter(s -> Integer.valueOf(0).equals(s.getSortingIndex()))
				.count();
		Assert.assertEquals(teamsAtPosition0, 2L,
				"Exactly 2 teams must be tied at sortingIndex=0 for the critical-draw trigger.");
	}

	// ─── Smoke tests ────────────────────────────────────────────────────────────

	/**
	 * SMOKE-1 — Critical draw blocks next-level generation.
	 *
	 * <p>
	 * When a critical draw exists, {@code PUT /fights/create/tournaments/{id}/next}
	 * must return HTTP 204 (NO_CONTENT) because {@code LevelNotFinishedException}
	 * is mapped to that status by {@code ExceptionControllerAdvice}.
	 */
	@Test(dependsOnMethods = "verifyGroup3HasExactly2TeamsTiedAtPosition0")
	public void smokeCriticalDrawBlocksNextLevel() throws Exception {
		this.mockMvc.perform(put("/fights/create/tournaments/{id}/next", this.tournamentDTO.getId())
				.contentType(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + this.jwtToken)
				.with(csrf())).andExpect(MockMvcResultMatchers.status().isNoContent());
	}

	/**
	 * SMOKE-2 — Backend auto-creates an UNDRAW duel for the draw group.
	 *
	 * <p>
	 * After the blocked attempt, {@code GET /duels/groups/{groupId}/unties} must
	 * return exactly 1 duel of type {@code DuelType.UNDRAW}.
	 */
	@Test(dependsOnMethods = "smokeCriticalDrawBlocksNextLevel")
	public void smokeAutoCreatedUntieExistsForDrawGroup() throws Exception {
		Assert.assertNotNull(this.drawGroupId, "Draw group ID must be captured in solveFightsWithDrawInLastGroup");

		final MvcResult result = this.mockMvc
				.perform(get("/duels/groups/{groupId}/unties", this.drawGroupId).contentType(MediaType.APPLICATION_JSON)
						.header("Authorization", "Bearer " + this.jwtToken))
				.andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

				final DuelDTO[] unties = this.objectMapper.readValue(result.getResponse().getContentAsString(),
				DuelDTO[].class);
		Assert.assertEquals(unties.length, 1,
				"Exactly 1 UNDRAW duel must be auto-created when a critical draw blocks next level");
		Assert.assertEquals(unties[0].getType(), DuelType.UNDRAW, "The auto-created duel must have type UNDRAW");
	}

	/**
	 * SMOKE-3 — GroupDTO exposes the UNDRAW duel so the frontend can display it.
	 *
	 * <p>
	 * {@code GET /groups/tournaments/{id}/level/0/index/3} must include the untie
	 * in its {@code unties} list.
	 */
	@Test(dependsOnMethods = "smokeAutoCreatedUntieExistsForDrawGroup")
	public void smokeDrawGroupExposeUntieInGroupDTO() throws Exception {
		final MvcResult result = this.mockMvc
				.perform(get("/groups/tournaments/{id}/level/{level}/index/{index}", this.tournamentDTO.getId(), 0, 3)
						.contentType(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + this.jwtToken))
				.andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

		final GroupDTO drawGroup = this.fromJson(result.getResponse().getContentAsString(), GroupDTO.class);
		Assert.assertNotNull(drawGroup.getUnties(), "GroupDTO#unties must not be null");
		Assert.assertEquals(drawGroup.getUnties().size(), 1, "GroupDTO must expose exactly 1 untie duel via REST");
		Assert.assertEquals(drawGroup.getUnties().getFirst().getType(), DuelType.UNDRAW,
				"The untie duel in GroupDTO must have type UNDRAW");
	}

	/**
	 * SMOKE-4 — Resolving the UNDRAW duel via {@code PUT /duels} persists the
	 * result.
	 *
	 * <p>
	 * Competitor 1 wins 2 MEN to 0.
	 */
	@Test(dependsOnMethods = "smokeDrawGroupExposeUntieInGroupDTO")
	public void smokeResolveUntieDuel() throws Exception {
		// Fetch the untie duel from the group endpoint
		final MvcResult untiesResult = this.mockMvc
				.perform(get("/duels/groups/{groupId}/unties", this.drawGroupId).contentType(MediaType.APPLICATION_JSON)
						.header("Authorization", "Bearer " + this.jwtToken))
				.andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

		final DuelDTO[] unties = this.objectMapper.readValue(untiesResult.getResponse().getContentAsString(),
				DuelDTO[].class);
		final DuelDTO untie = unties[0];

		untie.getCompetitor1Score().add(Score.MEN);
		untie.getCompetitor1Score().add(Score.MEN);
		untie.setFinished(true);

		final MvcResult updateResult = this.mockMvc
				.perform(put("/duels").contentType(MediaType.APPLICATION_JSON)
						.header("Authorization", "Bearer " + this.jwtToken).content(this.toJson(untie)).with(csrf()))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();

		final DuelDTO updated = this.fromJson(updateResult.getResponse().getContentAsString(), DuelDTO.class);
		Assert.assertTrue(updated.isFinished(), "Untie duel must be marked as finished after update");
		// getWinner() = Integer.compare(comp2Score, comp1Score): -1 → comp1 wins, 0 →
		// draw, 1 → comp2 wins
		Assert.assertEquals(updated.getWinner(), -1, "Competitor 1 wins — winner() must be -1 after scoring 2 MEN");
	}

	/**
	 * SMOKE-5 — After untie resolution, next-level fights are generated (draw is
	 * gone).
	 *
	 * <p>
	 * {@code PUT /fights/create/tournaments/{id}/next} must now return HTTP 2xx and
	 * a non-empty list of fights.
	 */
	@Test(dependsOnMethods = "smokeResolveUntieDuel")
	public void smokeNextLevelFightsCreatedAfterUntieResolution() throws Exception {
		final MvcResult result = this.mockMvc
				.perform(put("/fights/create/tournaments/{id}/next", this.tournamentDTO.getId())
						.contentType(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + this.jwtToken)
						.with(csrf()))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();

		final FightDTO[] nextFights = this.objectMapper.readValue(result.getResponse().getContentAsString(),
				FightDTO[].class);
		Assert.assertTrue(nextFights.length > 0, "Next-level fights must be generated once the draw is resolved");
		Assert.assertTrue(Arrays.stream(nextFights).allMatch(f -> f.getLevel() == 1),
				"All generated fights must belong to level 1");
	}

	/**
	 * SMOKE-6 — Level-1 groups contain fights, confirming bracket progression.
	 *
	 * <p>
	 * Winners from level-0 groups must have been assigned to level-1 groups and
	 * those groups must have fights.
	 */
	@Test(dependsOnMethods = "smokeNextLevelFightsCreatedAfterUntieResolution")
	public void smokeLevel1GroupsHaveCorrectFights() throws Exception {
		final MvcResult result = this.mockMvc
				.perform(get("/groups/tournaments/{id}", this.tournamentDTO.getId())
						.contentType(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + this.jwtToken))
				.andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

		final List<GroupDTO> level1Groups = Arrays
				.stream(this.objectMapper.readValue(result.getResponse().getContentAsString(), GroupDTO[].class))
				.filter(g -> Integer.valueOf(1).equals(g.getLevel())).toList();

		Assert.assertFalse(level1Groups.isEmpty(), "Level-1 groups must exist after next-level fights are generated");
		level1Groups.forEach(g -> Assert.assertFalse(g.getFights().isEmpty(),
				"Every level-1 group must contain at least one fight"));
	}

	// ─── Cleanup ────────────────────────────────────────────────────────────────

	@AfterClass(alwaysRun = true)
	public void deleteTournament() throws Exception {
		if (this.tournamentDTO != null) {
			this.mockMvc.perform(
					delete("/tournaments/{id}", this.tournamentDTO.getId()).contentType(MediaType.APPLICATION_JSON)
							.header("Authorization", "Bearer " + this.jwtToken).with(csrf()))
					.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
		}
		this.groupController.deleteAll();
		this.fightController.deleteAll();
		this.duelController.deleteAll();
		this.teamController.deleteAll();
		this.roleController.deleteAll();
		this.tournamentExtraPropertyController.deleteAll();
		this.tournamentController.deleteAll();
		this.participantController.deleteAll();
		this.clubController.deleteAll();
		this.authenticatedUserController.deleteAll();
	}
}
