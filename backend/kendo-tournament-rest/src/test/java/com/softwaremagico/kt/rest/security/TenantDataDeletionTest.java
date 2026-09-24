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
import com.softwaremagico.kt.core.controller.ClubController;
import com.softwaremagico.kt.core.controller.FightController;
import com.softwaremagico.kt.core.controller.GroupController;
import com.softwaremagico.kt.core.controller.ParticipantController;
import com.softwaremagico.kt.core.controller.RoleController;
import com.softwaremagico.kt.core.controller.TeamController;
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.TournamentExtraPropertyController;
import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.controller.models.TournamentExtraPropertyDTO;
import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.persistence.entities.Tenant;
import com.softwaremagico.kt.persistence.entities.TenantContext;
import com.softwaremagico.kt.persistence.repositories.TenantRepository;
import com.softwaremagico.kt.persistence.values.LeagueFightsOrder;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import com.softwaremagico.kt.persistence.values.TournamentType;
import com.softwaremagico.kt.rest.controllers.AuthenticatedUserController;
import com.softwaremagico.kt.rest.security.dto.AuthRequest;
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
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Test(groups = "tenantDataDeletion")
public class TenantDataDeletionTest extends AbstractTestNGSpringContextTests {
    private static final String LEGACY_TENANT = "Legacy organization";
    private static final String PLATFORM_ADMIN = "platform.admin";
    private static final String TENANT_A_PREFIX = "tenantA";
    private static final String TENANT_B_PREFIX = "tenantB";
    private static final String TENANT_A = "Deletion tenant A";
    private static final String TENANT_B = "Deletion tenant B";
    private static final String PASSWORD = "secure-password";
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
    private AuthenticatedUserController authenticatedUserController;
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private ClubController clubController;
    @Autowired
    private ParticipantController participantController;
    @Autowired
    private TournamentController tournamentController;
    @Autowired
    private RoleController roleController;
    @Autowired
    private TeamController teamController;
    @Autowired
    private GroupController groupController;
    @Autowired
    private FightController fightController;
    @Autowired
    private TournamentExtraPropertyController tournamentExtraPropertyController;

    private String superAdminToken;
    private String tenantAAdminToken;
    private Tenant tenantA;
    private Tenant tenantB;

    @BeforeClass
    public void setUp() throws Exception {
        TenantContext.setTenantId(TenantContext.LEGACY_TENANT_ID);
        try {
            authenticatedUserController.createUser(null, PLATFORM_ADMIN, "Platform", "Admin", PASSWORD,
                    AvailableRole.SUPER_ADMIN);
        } finally {
            TenantContext.clear();
        }
        superAdminToken = login(PLATFORM_ADMIN, LEGACY_TENANT);

        tenantA = tenantRepository.save(new Tenant(TENANT_A));
        tenantB = tenantRepository.save(new Tenant(TENANT_B));
        populateTenant(tenantA.getId(), TENANT_A_PREFIX);
        populateTenant(tenantB.getId(), TENANT_B_PREFIX);
        tenantAAdminToken = login(TENANT_A_PREFIX + ".admin", TENANT_A);
    }

    @Test
    public void tenantAdminCannotDeleteTenantData() throws Exception {
        mockMvc.perform(delete("/auth/tenants/{tenantId}/data", tenantA.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tenantAAdminToken).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test(dependsOnMethods = "tenantAdminCannotDeleteTenantData")
    public void cannotDeleteLegacyTenantData() throws Exception {
        mockMvc.perform(delete("/auth/tenants/{tenantId}/data", TenantContext.LEGACY_TENANT_ID)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + superAdminToken).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test(dependsOnMethods = "cannotDeleteLegacyTenantData")
    public void cannotDeleteUnknownTenantData() throws Exception {
        mockMvc.perform(delete("/auth/tenants/{tenantId}/data", Integer.MAX_VALUE)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + superAdminToken).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test(dependsOnMethods = "cannotDeleteUnknownTenantData")
    public void deletingTenantDataOnlyRemovesTargetTenantData() throws Exception {
        final Map<String, Long> countsBeforeTenantA = countRowsPerTable(tenantA.getId());
        final Map<String, Long> countsBeforeTenantB = countRowsPerTable(tenantB.getId());
        final long tenantsBefore = tenantRepository.count();

        mockMvc.perform(delete("/auth/tenants/{tenantId}/data", tenantA.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + superAdminToken).with(csrf()))
                .andExpect(status().isNoContent());

        final Map<String, Long> countsAfterTenantA = countRowsPerTable(tenantA.getId());
        final Map<String, Long> countsAfterTenantB = countRowsPerTable(tenantB.getId());

        for (Map.Entry<String, Long> entry : countsAfterTenantA.entrySet()) {
            Assert.assertEquals(entry.getValue(), 0L, "Row(s) left in table '" + entry.getKey() + "' for tenant '"
                    + TENANT_A + "'");
        }
        for (Map.Entry<String, Long> entry : countsBeforeTenantB.entrySet()) {
            Assert.assertEquals(countsAfterTenantB.get(entry.getKey()), entry.getValue(),
                    "Table '" + entry.getKey() + "' of tenant '" + TENANT_B + "' was affected by the deletion");
        }

        Assert.assertTrue(tenantRepository.findById(tenantA.getId()).isPresent(), "Tenant registration must be kept");
        Assert.assertTrue(tenantRepository.existsByIdAndActiveTrue(tenantA.getId()),
                "Tenant registration must remain active");
        Assert.assertEquals(tenantRepository.count(), tenantsBefore, "No tenant registration may be deleted");

        final AuthRequest request = new AuthRequest();
        request.setUsername(TENANT_A_PREFIX + ".admin");
        request.setPassword(PASSWORD);
        request.setTenant(TENANT_A);
        mockMvc.perform(post("/auth/public/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)).with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    private void populateTenant(Integer tenantId, String prefix) {
        TenantContext.setTenantId(tenantId);
        try {
            authenticatedUserController.createUser(null, prefix + ".admin", "Tenant", "Admin", PASSWORD,
                    AvailableRole.ADMIN);
            final ClubDTO club = clubController.create(prefix + " Club", "Country", "City", null, null);
            final List<ParticipantDTO> participants = new ArrayList<>();
            for (int i = 0; i < TEAM_SIZE * TEAMS; i++) {
                participants.add(participantController.create(
                        new ParticipantDTO(prefix + ".driver." + i, prefix + ".name" + i, prefix + ".lastname" + i, club),
                        null, null));
            }
            final TournamentDTO tournament = tournamentController.create(
                    new TournamentDTO(prefix + " Tournament", 1, TEAM_SIZE, TournamentType.LEAGUE), null, null);
            tournamentExtraPropertyController.create(new TournamentExtraPropertyDTO(tournament,
                    TournamentExtraPropertyKey.LEAGUE_FIGHTS_ORDER_GENERATION, LeagueFightsOrder.FIFO.name()), null, null);
            for (ParticipantDTO participant : participants) {
                roleController.create(new RoleDTO(tournament, participant, RoleType.COMPETITOR), null, null);
            }
            addTeams(tournament, participants);
            fightController.createFights(tournament.getId(), TeamsOrder.SORTED, 0, null, null);
        } finally {
            TenantContext.clear();
        }
    }

    private void addTeams(TournamentDTO tournament, List<ParticipantDTO> participants) {
        final GroupDTO groupDTO = groupController.get(tournament).getFirst();
        TeamDTO teamDTO = null;
        int teamIndex = 0;
        int teamMember = 0;
        for (ParticipantDTO participant : participants) {
            if (teamDTO == null) {
                teamIndex++;
                teamDTO = new TeamDTO("Team" + String.format("%02d", teamIndex), tournament);
                teamMember = 0;
            }
            teamDTO.addMember(participant);
            teamDTO = teamController.update(teamDTO, null, null);
            if (teamMember == 0) {
                groupController.addTeams(groupDTO.getId(), java.util.Collections.singletonList(teamDTO), null, null);
            }
            teamMember++;
            if (teamMember >= TEAM_SIZE) {
                teamDTO = null;
            }
        }
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

    private String login(String username, String tenant) throws Exception {
        final AuthRequest request = new AuthRequest();
        request.setUsername(username);
        request.setPassword(PASSWORD);
        request.setTenant(tenant);
        return mockMvc.perform(post("/auth/public/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)).with(csrf()))
                .andExpect(status().isOk()).andReturn().getResponse().getHeader(HttpHeaders.AUTHORIZATION);
    }
}