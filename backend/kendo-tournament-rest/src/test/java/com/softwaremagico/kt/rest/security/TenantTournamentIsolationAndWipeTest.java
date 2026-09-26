package com.softwaremagico.kt.rest.security;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.core.controller.models.DuelDTO;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.ScoreOfTeamDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.persistence.entities.Tenant;
import com.softwaremagico.kt.persistence.entities.TenantContext;
import com.softwaremagico.kt.persistence.repositories.TenantRepository;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.TournamentType;
import com.softwaremagico.kt.rest.controllers.AuthenticatedUserController;
import com.softwaremagico.kt.rest.security.dto.AuthRequest;
import com.softwaremagico.kt.rest.security.dto.CreateTenantRequest;
import com.softwaremagico.kt.security.AvailableRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end tenant isolation and cleanup test, driven entirely through the REST API.
 *
 * <p>
 * Scenario:
 * <ol>
 * <li>Two tenants (A and B) are created through {@code POST /auth/tenants}; each
 * gets its own administrator user.</li>
 * <li>Each tenant builds a complete tournament through REST: club, participants,
 * tournament, roles, teams, fights and duel scores (points).</li>
 * <li>Tenant isolation is verified both ways: every list endpoint (clubs,
 * tournaments, participants, teams, groups, roles, fights) only contains the
 * caller's own data, and every resource of one tenant (club, tournament,
 * participant, team, group, fight, role, duel, ranking, unties) is unreachable
 * with the other tenant's token.</li>
 * <li>Tenant A data is wiped via {@code DELETE /auth/tenants/{id}/data}. Only A's
 * rows disappear (verified table by table), the tenant registration remains, and
 * tenant B keeps seeing all of its data through REST.</li>
 * </ol>
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Test(groups = "tenantDataDeletion")
public class TenantTournamentIsolationAndWipeTest extends AbstractTestNGSpringContextTests {
	private static final String LEGACY_TENANT = "Legacy organization";
	private static final String PLATFORM_ADMIN = "platform.admin";
	private static final String PASSWORD = "secure-password";
	private static final String TENANT_A = "Isolation Tournament Tenant A";
	private static final String TENANT_B = "Isolation Tournament Tenant B";
	private static final String ADMIN_A = "isolation.tournament.admin.a";
	private static final String ADMIN_B = "isolation.tournament.admin.b";
	private static final String PREFIX_A = "WipeA";
	private static final String PREFIX_B = "WipeB";
	private static final int TEAM_SIZE = 2;
	private static final int TEAMS = 4;

	private static final List<String> TENANT_SCOPED_TABLES = List.of(
			"achievements", "authenticated_users", "clubs", "duels", "fights", "groups_links", "participants",
			"participant_image", "roles", "teams", "tournament_extra_properties", "tournament_groups",
			"tournament_image", "tournaments", "tournament_scores");

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private TenantRepository tenantRepository;
	@Autowired
	private AuthenticatedUserController authenticatedUserController;

	private String superAdminToken;
	private String tenantAToken;
	private String tenantBToken;
	private Integer tenantAId;
	private Integer tenantBId;
	private Integer tournamentAId;
	private Integer tournamentBId;
	private TenantResourceIds tenantResourcesA;
	private TenantResourceIds tenantResourcesB;

	@BeforeClass
	public void bootstrapPlatformAdministrator() throws Exception {
		TenantContext.setTenantId(TenantContext.LEGACY_TENANT_ID);
		try {
			authenticatedUserController.createUser(null, PLATFORM_ADMIN, "Platform", "Admin", PASSWORD,
					AvailableRole.SUPER_ADMIN);
		} finally {
			TenantContext.clear();
		}
		superAdminToken = login(PLATFORM_ADMIN, LEGACY_TENANT);
	}

	@Test
	public void createBothTenantsAndTheirAdminsThroughRest() throws Exception {
		createTenant(TENANT_A, ADMIN_A);
		createTenant(TENANT_B, ADMIN_B);

		final Tenant[] tenants = fromJson(
				mockMvc.perform(get("/auth/tenants").header(HttpHeaders.AUTHORIZATION, "Bearer " + superAdminToken))
						.andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), Tenant[].class);
		tenantAId = findTenantId(tenants, TENANT_A);
		tenantBId = findTenantId(tenants, TENANT_B);

		tenantAToken = login(ADMIN_A, TENANT_A);
		tenantBToken = login(ADMIN_B, TENANT_B);
	}

	@Test(dependsOnMethods = "createBothTenantsAndTheirAdminsThroughRest")
	public void tenantABuildsCompleteTournament() throws Exception {
		tournamentAId = buildCompleteTournament(tenantAToken, PREFIX_A).getId();
	}

	@Test(dependsOnMethods = "createBothTenantsAndTheirAdminsThroughRest")
	public void tenantBBuildsCompleteTournament() throws Exception {
		tournamentBId = buildCompleteTournament(tenantBToken, PREFIX_B).getId();
	}

	@Test(dependsOnMethods = {"tenantABuildsCompleteTournament", "tenantBBuildsCompleteTournament"})
	public void eachTenantOnlyCollectsItsOwnResources() throws Exception {
		tenantResourcesA = collectTenantResources(tenantAToken, tournamentAId, PREFIX_A);
		tenantResourcesB = collectTenantResources(tenantBToken, tournamentBId, PREFIX_B);
	}

	@Test(dependsOnMethods = {"eachTenantOnlyCollectsItsOwnResources"})
	public void tenantUsersCannotAccessEachOthersResourcesById() throws Exception {
		// A cannot reach any resource of B
		assertForeignHidden(tenantResourcesB, tenantAToken);
		// B cannot reach any resource of A
		assertForeignHidden(tenantResourcesA, tenantBToken);

		// And each tenant still sees its own tournament with fights and points.
		assertFightsWithPoints(tenantAToken, tournamentAId);
		assertFightsWithPoints(tenantBToken, tournamentBId);
	}

	@Test(dependsOnMethods = "tenantUsersCannotAccessEachOthersResourcesById")
	public void wipingTenantADataOnlyRemovesTenantARows() throws Exception {
		final Map<String, Long> countsBeforeA = countRowsPerTable(tenantAId);
		final Map<String, Long> countsBeforeB = countRowsPerTable(tenantBId);
		final long tenantsBefore = tenantRepository.count();

		mockMvc.perform(delete("/auth/tenants/{tenantId}/data", tenantAId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + superAdminToken).with(csrf()))
				.andExpect(status().isNoContent());

		final Map<String, Long> countsAfterA = countRowsPerTable(tenantAId);
		final Map<String, Long> countsAfterB = countRowsPerTable(tenantBId);

		for (Map.Entry<String, Long> entry : countsAfterA.entrySet()) {
			Assert.assertEquals(entry.getValue(), 0L, "Row(s) left in table '" + entry.getKey() + "' for tenant '"
					+ TENANT_A + "'");
		}
		for (Map.Entry<String, Long> entry : countsBeforeB.entrySet()) {
			Assert.assertEquals(countsAfterB.get(entry.getKey()), entry.getValue(),
					"Table '" + entry.getKey() + "' of tenant '" + TENANT_B + "' was affected by the deletion");
		}

		Assert.assertTrue(tenantRepository.findById(tenantAId).isPresent(), "Tenant registration must be kept");
		Assert.assertTrue(tenantRepository.existsByIdAndActiveTrue(tenantAId),
				"Tenant registration must remain active");
		Assert.assertEquals(tenantRepository.count(), tenantsBefore, "No tenant registration may be deleted");
	}

	@Test(dependsOnMethods = "wipingTenantADataOnlyRemovesTenantARows")
	public void unaffectedTenantKeepsSeeingItsData() throws Exception {
		// The wiped tenant lost its users: login must be rejected.
		final AuthRequest request = new AuthRequest();
		request.setUsername(ADMIN_A);
		request.setPassword(PASSWORD);
		request.setTenant(TENANT_A);
		mockMvc.perform(post("/auth/public/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)).with(csrf())).andExpect(status().isUnauthorized());

		// Tenant B admin still sees its club, tournament, fights and points.
		final TournamentDTO[] tournaments = fromJson(get2xx("/tournaments", tenantBToken)
				.getResponse().getContentAsString(), TournamentDTO[].class);
		Assert.assertEquals(tournaments.length, 1, "Tenant B must still see its tournament");
		Assert.assertEquals(tournaments[0].getName(), PREFIX_B + " Tournament");
		assertFightsWithPoints(tenantBToken, tournamentBId);
		// And the wiped tenant's resources are no longer reachable.
		assertNotFound(get("/tournaments/{id}", tournamentAId), tenantBToken);
	}

	private void createTenant(String tenantName, String adminUsername) throws Exception {
		final CreateTenantRequest request = new CreateTenantRequest();
		request.setTenant(tenantName);
		request.setUsername(adminUsername);
		request.setPassword(PASSWORD);
		request.setName(adminUsername);
		request.setLastname("Admin");
		mockMvc.perform(post("/auth/tenants")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + superAdminToken)
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)).with(csrf()))
				.andExpect(status().isCreated());
	}

	/**
	 * Builds a complete tournament of a tenant through pure REST calls: club,
	 * participants, tournament, roles, teams, fights and duel scores.
	 */
	private TournamentDTO buildCompleteTournament(String token, String prefix) throws Exception {
		final ClubDTO club = fromJson(post2xx("/clubs", token, new ClubDTO(prefix + " Club", "Bilbao"),
				new Object[0]).getResponse().getContentAsString(), ClubDTO.class);

		final List<ParticipantDTO> participants = new ArrayList<>();
		for (int i = 0; i < TEAM_SIZE * TEAMS; i++) {
			participants.add(fromJson(post2xx("/participants", token,
					new ParticipantDTO(String.format("%s.driver.%02d", prefix, i), String.format("%s.name%d", prefix, i),
							String.format("%s.lastname%d", prefix, i), club),
					new Object[0]).getResponse().getContentAsString(), ParticipantDTO.class));
		}

		final TournamentDTO tournament = fromJson(post2xx("/tournaments", token,
				new TournamentDTO(prefix + " Tournament", 1, TEAM_SIZE, TournamentType.LEAGUE),
				new Object[0]).getResponse().getContentAsString(), TournamentDTO.class);

		for (ParticipantDTO participant : participants) {
			post2xx("/roles", token, new RoleDTO(tournament, participant, RoleType.COMPETITOR), new Object[0]);
		}

		final GroupDTO group = fromJson(
				get2xx("/groups/tournaments/{id}", token, tournament.getId()).getResponse().getContentAsString(),
				GroupDTO[].class)[0];
		addTeamsToGroup(token, prefix, tournament, participants, group);

		final List<FightDTO> fights = iterateFights(putNoBody("/fights/create/tournaments/{id}/levels/{level}", token,
				tournament.getId(), 0));
		Assert.assertFalse(fights.isEmpty(), "Fights must be created for the tournament");
		solveFightsWithPoints(token, fights);

		return tournament;
	}

	private void addTeamsToGroup(String token, String prefix, TournamentDTO tournament,
			List<ParticipantDTO> participants, GroupDTO group) throws Exception {
		TeamDTO team = null;
		int teamIndex = 0;
		int teamMember = 0;
		for (ParticipantDTO participant : participants) {
			if (team == null) {
				teamIndex++;
				team = new TeamDTO(prefix + "Team" + String.format("%02d", teamIndex), tournament);
				teamMember = 0;
			}
			team.getMembers().add(participant);
			team = fromJson(put2xx("/teams", token, team).getResponse().getContentAsString(), TeamDTO.class);
			if (teamMember == 0) {
				patch2xx("/groups/{groupId}/teams/add", token, Collections.singletonList(team), group.getId());
			}
			teamMember++;
			if (teamMember >= TEAM_SIZE) {
				team = null;
			}
		}
	}

	private void solveFightsWithPoints(String token, List<FightDTO> fights) throws Exception {
		for (FightDTO fight : fights) {
			for (DuelDTO duel : fight.getDuels()) {
				duel.getCompetitor1Score().add(Score.MEN);
				duel.getCompetitor1Score().add(Score.MEN);
				duel.setFinished(true);
			}
			put2xx("/fights", token, fight);
		}
	}

	private List<FightDTO> iterateFights(MvcResult result) throws Exception {
		return Arrays.asList(objectMapper.readValue(result.getResponse().getContentAsString(), FightDTO[].class));
	}

	private void assertFightsWithPoints(String token, Integer tournamentId) throws Exception {
		final FightDTO[] fights = fromJson(
				get2xx("/fights/tournaments/{id}", token, tournamentId).getResponse().getContentAsString(),
				FightDTO[].class);
		Assert.assertTrue(fights.length > 0, "The tenant must keep its fights");

		final ScoreOfTeamDTO[] ranking = fromJson(
				get2xx("/rankings/teams/tournaments/{id}", token, tournamentId).getResponse().getContentAsString(),
				ScoreOfTeamDTO[].class);
		Assert.assertTrue(Arrays.stream(ranking).mapToInt(ScoreOfTeamDTO::getHits).sum() > 0,
				"The tenant must keep its points (scored hits)");
	}

	/**
	 * Reads every resource the tenant owns through REST and checks that the list
	 * endpoints only expose its own tenant data.
	 */
	private TenantResourceIds collectTenantResources(String token, Integer tournamentId, String prefix)
			throws Exception {
		final TenantResourceIds ids = new TenantResourceIds();
		ids.tournamentId = tournamentId;

		final ClubDTO[] clubs = fromJson(get2xx("/clubs", token).getResponse().getContentAsString(), ClubDTO[].class);
		Assert.assertEquals(clubs.length, 1, "Tenant '" + prefix + "' must see exactly its own club");
		Assert.assertTrue(clubs[0].getName().equalsIgnoreCase(prefix + " Club"));
		ids.clubId = clubs[0].getId();

		final TournamentDTO[] tournaments = fromJson(get2xx("/tournaments", token).getResponse().getContentAsString(),
				TournamentDTO[].class);
		Assert.assertEquals(tournaments.length, 1, "Tenant '" + prefix + "' must see exactly its own tournament");
		Assert.assertEquals(tournaments[0].getName(), prefix + " Tournament");
		ids.tournamentId = tournaments[0].getId();

		final ParticipantDTO[] participants = fromJson(get2xx("/participants", token).getResponse().getContentAsString(),
				ParticipantDTO[].class);
		Assert.assertEquals(participants.length, TEAM_SIZE * TEAMS,
				"Tenant '" + prefix + "' must see exactly its own participants");
		ids.participantId = participants[0].getId();

		final TeamDTO[] teams = fromJson(get2xx("/teams/tournaments/{id}", token, tournamentId)
				.getResponse().getContentAsString(), TeamDTO[].class);
		Assert.assertEquals(teams.length, TEAMS, "Tenant '" + prefix + "' must see exactly its own teams");
		ids.teamId = teams[0].getId();

		final GroupDTO[] groups = fromJson(get2xx("/groups/tournaments/{id}", token, tournamentId)
				.getResponse().getContentAsString(), GroupDTO[].class);
		Assert.assertEquals(groups.length, 1, "Tenant '" + prefix + "' must see exactly its own group");
		ids.groupId = groups[0].getId();

		final RoleDTO[] roles = fromJson(get2xx("/roles/tournaments/{id}", token, tournamentId)
				.getResponse().getContentAsString(), RoleDTO[].class);
		Assert.assertEquals(roles.length, TEAM_SIZE * TEAMS, "Tenant '" + prefix + "' must see its own roles");
		ids.roleId = roles[0].getId();

		final FightDTO[] fights = fromJson(get2xx("/fights/tournaments/{id}", token, tournamentId)
				.getResponse().getContentAsString(), FightDTO[].class);
		Assert.assertTrue(fights.length > 0, "Tenant '" + prefix + "' must see its own fights");
		ids.fightId = fights[0].getId();
		Assert.assertFalse(fights[0].getDuels().isEmpty(), "Fight must have duels");
		ids.duelId = fights[0].getDuels().getFirst().getId();

		return ids;
	}

	/**
	 * Verifies that none of the caller's resources, addressed by id or by
	 * tournament, are reachable with a token of another tenant.
	 */
	private void assertForeignHidden(TenantResourceIds foreign, String foreignToken) throws Exception {
		assertNotFound(get("/clubs/{id}", foreign.clubId), foreignToken);
		assertNotFound(get("/tournaments/{id}", foreign.tournamentId), foreignToken);
		assertNotFound(get("/participants/{id}", foreign.participantId), foreignToken);
		assertNotFound(get("/teams/{id}", foreign.teamId), foreignToken);
		assertNotFound(get("/groups/{id}", foreign.groupId), foreignToken);
		assertNotFound(get("/fights/{id}", foreign.fightId), foreignToken);
		assertNotFound(get("/roles/{id}", foreign.roleId), foreignToken);
		assertNotFound(get("/duels/{id}", foreign.duelId), foreignToken);

		assertNotFound(get("/teams/tournaments/{id}", foreign.tournamentId), foreignToken);
		assertNotFound(get("/groups/tournaments/{id}", foreign.tournamentId), foreignToken);
		assertNotFound(get("/roles/tournaments/{id}", foreign.tournamentId), foreignToken);
		assertNotFound(get("/fights/tournaments/{id}", foreign.tournamentId), foreignToken);
		assertNotFound(get("/rankings/teams/tournaments/{id}", foreign.tournamentId), foreignToken);
		assertNotFound(get("/groups/tournaments/{id}/level/{level}/index/{index}", foreign.tournamentId, 0, 0),
				foreignToken);

		// Untie duels of a foreign tournament must resolve to an empty list.
		final DuelDTO[] foreignUnties = fromJson(get2xx("/duels/tournaments/{id}/unties", foreignToken,
				foreign.tournamentId).getResponse().getContentAsString(), DuelDTO[].class);
		Assert.assertTrue(foreignUnties.length == 0,
				"Foreign untie duels must not be visible to the caller tenant");
	}

	/** Ids of the resources created by a tenant, collected through REST. */
	private static final class TenantResourceIds {
		private Integer clubId;
		private Integer tournamentId;
		private Integer participantId;
		private Integer teamId;
		private Integer groupId;
		private Integer roleId;
		private Integer fightId;
		private Integer duelId;
	}

	private static Integer findTenantId(Tenant[] tenants, String name) {
		return Arrays.stream(tenants).filter(t -> t.getName().equals(name)).map(Tenant::getId).findFirst()
				.orElseThrow(() -> new AssertionError("Tenant '" + name + "' not found"));
	}

	private MvcResult get2xx(String path, String token, Object... vars) throws Exception {
		return mockMvc.perform(get(path, vars).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().is2xxSuccessful()).andReturn();
	}

	private MvcResult post2xx(String path, String token, Object body, Object... vars) throws Exception {
		return mockMvc.perform(post(path, vars).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body == null ? "{}" : objectMapper.writeValueAsString(body)).with(csrf()))
				.andExpect(status().is2xxSuccessful()).andReturn();
	}

	private MvcResult put2xx(String path, String token, Object body, Object... vars) throws Exception {
		return mockMvc.perform(put(path, vars).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body)).with(csrf()))
				.andExpect(status().is2xxSuccessful()).andReturn();
	}

	private MvcResult putNoBody(String path, String token, Object... vars) throws Exception {
		return mockMvc.perform(put(path, vars).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON).with(csrf()))
				.andExpect(status().is2xxSuccessful()).andReturn();
	}

	private MvcResult patch2xx(String path, String token, Object body, Object... vars) throws Exception {
		return mockMvc.perform(patch(path, vars).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body)).with(csrf()))
				.andExpect(status().is2xxSuccessful()).andReturn();
	}

	private void assertNotFound(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder builder,
			String token) throws Exception {
		mockMvc.perform(builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)).andExpect(status().isNotFound());
	}

	private String login(String username, String tenant) throws Exception {
		final AuthRequest request = new AuthRequest();
		request.setUsername(username);
		request.setPassword(PASSWORD);
		request.setTenant(tenant);
		return mockMvc.perform(post("/auth/public/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)).with(csrf()))
				.andExpect(status().isOk()).andReturn().getResponse().getHeader(HttpHeaders.AUTHORIZATION);
	}

	private <T> T fromJson(String payload, Class<T> clazz) throws Exception {
		return objectMapper.readValue(payload, clazz);
	}

	private Map<String, Long> countRowsPerTable(Integer tenantId) {
		final Map<String, Long> counts = new LinkedHashMap<>();
		for (String table : TENANT_SCOPED_TABLES) {
			counts.put(table, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table + " WHERE tenant_id = ?",
					Long.class, tenantId));
		}
		counts.put("duels_by_fight", countJoinRows(
				"SELECT COUNT(*) FROM duels_by_fight WHERE fight_id IN (SELECT id FROM fights WHERE tenant_id = ?)", tenantId));
		counts.put("fights_by_group", countJoinRows(
				"SELECT COUNT(*) FROM fights_by_group WHERE group_id IN (SELECT id FROM tournament_groups WHERE tenant_id = ?)", tenantId));
		counts.put("teams_by_group", countJoinRows(
				"SELECT COUNT(*) FROM teams_by_group WHERE group_id IN (SELECT id FROM tournament_groups WHERE tenant_id = ?)", tenantId));
		counts.put("members_of_team", countJoinRows(
				"SELECT COUNT(*) FROM members_of_team WHERE team_id IN (SELECT id FROM teams WHERE tenant_id = ?)", tenantId));
		counts.put("unties", countJoinRows(
				"SELECT COUNT(*) FROM unties WHERE group_id IN (SELECT id FROM tournament_groups WHERE tenant_id = ?)", tenantId));
		counts.put("authenticated_user_roles", countJoinRows(
				"SELECT COUNT(*) FROM authenticated_user_roles WHERE authenticated_user IN (SELECT id FROM authenticated_users WHERE tenant_id = ?)", tenantId));
		return counts;
	}

	private long countJoinRows(String sql, Integer tenantId) {
		return jdbcTemplate.queryForObject(sql, Long.class, tenantId);
	}
}
