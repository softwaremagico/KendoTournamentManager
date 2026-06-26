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
import com.softwaremagico.kt.core.controller.DuelController;
import com.softwaremagico.kt.core.controller.FightController;
import com.softwaremagico.kt.core.controller.GroupController;
import com.softwaremagico.kt.core.controller.ParticipantController;
import com.softwaremagico.kt.core.controller.RoleController;
import com.softwaremagico.kt.core.controller.TeamController;
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.TournamentExtraPropertyController;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.controller.models.TournamentExtraPropertyDTO;
import com.softwaremagico.kt.persistence.values.SwissTieBreakRule;
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
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@SpringBootTest
@AutoConfigureMockMvc
@Test(groups = {"restSwissEndpoints"})
public class RestSwissEndpointsTest extends AbstractTestNGSpringContextTests {

    private static final String USER_NAME = "swiss.admin";
    private static final String USER_FIRST_NAME = "Swiss";
    private static final String USER_LAST_NAME = "Admin";
    private static final String USER_PASSWORD = "asd123";
    private static final String[] USER_ROLES = new String[]{"admin", "viewer"};

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthenticatedUserController authenticatedUserController;

    @Autowired
    private TeamController teamController;

    @Autowired
    private GroupController groupController;

    @Autowired
    private FightController fightController;

    @Autowired
    private DuelController duelController;

    @Autowired
    private RoleController roleController;

    @Autowired
    private ParticipantController participantController;

    @Autowired
    private TournamentExtraPropertyController tournamentExtraPropertyController;

    @Autowired
    private TournamentController tournamentController;

    private String jwtToken;

    private TournamentDTO tournamentDTO;

    private <T> String toJson(T object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    private <T> T fromJson(String payload, Class<T> clazz) throws IOException {
        return objectMapper.readValue(payload, clazz);
    }

    private MvcResult createSwissProperty(TournamentExtraPropertyKey key, String value) throws Exception {
        final TournamentExtraPropertyDTO propertyDTO = new TournamentExtraPropertyDTO(tournamentDTO, key, value);
        return this.mockMvc
                .perform(post("/tournaments/properties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(toJson(propertyDTO))
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
    }

    @BeforeClass
    public void setAuthentication() throws Exception {
        authenticatedUserController.createUser(null, USER_NAME, USER_FIRST_NAME, USER_LAST_NAME, USER_PASSWORD, USER_ROLES);

        final AuthRequest request = new AuthRequest();
        request.setUsername(USER_NAME);
        request.setPassword(USER_PASSWORD);

        final MvcResult createResult = this.mockMvc
                .perform(post("/auth/public/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request))
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().exists(HttpHeaders.AUTHORIZATION))
                .andReturn();

        jwtToken = createResult.getResponse().getHeader(HttpHeaders.AUTHORIZATION);
        Assert.assertNotNull(jwtToken);
    }

    @Test
    public void shouldCreateSwissTournamentViaApi() throws Exception {
        final TournamentDTO swissTournament = new TournamentDTO("Swiss endpoint test", 1, 1, TournamentType.SWISS, null);

        final MvcResult createResult = this.mockMvc
                .perform(post("/tournaments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(toJson(swissTournament))
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();

        tournamentDTO = fromJson(createResult.getResponse().getContentAsString(), TournamentDTO.class);
        Assert.assertNotNull(tournamentDTO.getId());
        Assert.assertEquals(tournamentDTO.getType(), TournamentType.SWISS);
    }

    @Test(dependsOnMethods = "shouldCreateSwissTournamentViaApi")
    public void shouldConfigureSwissPropertiesViaApi() throws Exception {
        createSwissProperty(TournamentExtraPropertyKey.SWISS_ROUNDS, "4");
        createSwissProperty(TournamentExtraPropertyKey.SWISS_TIE_BREAK_RULE, SwissTieBreakRule.SONNEBORN_BERGER.name());
        createSwissProperty(TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS, "false");

        final MvcResult roundsResult = this.mockMvc
                .perform(get("/tournaments/properties/tournaments/{tournamentId}/key/{key}",
                                tournamentDTO.getId(), TournamentExtraPropertyKey.SWISS_ROUNDS.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        final TournamentExtraPropertyDTO roundsProperty = fromJson(roundsResult.getResponse().getContentAsString(),
                TournamentExtraPropertyDTO.class);
        Assert.assertEquals(roundsProperty.getPropertyValue(), "4");

        final MvcResult tieBreakResult = this.mockMvc
                .perform(get("/tournaments/properties/tournaments/{tournamentId}/key/{key}",
                                tournamentDTO.getId(), TournamentExtraPropertyKey.SWISS_TIE_BREAK_RULE.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        final TournamentExtraPropertyDTO tieBreakProperty = fromJson(tieBreakResult.getResponse().getContentAsString(),
                TournamentExtraPropertyDTO.class);
        Assert.assertEquals(tieBreakProperty.getPropertyValue(), SwissTieBreakRule.SONNEBORN_BERGER.name());

        final MvcResult repeatedResult = this.mockMvc
                .perform(get("/tournaments/properties/tournaments/{tournamentId}/key/{key}",
                                tournamentDTO.getId(), TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        final TournamentExtraPropertyDTO repeatedProperty = fromJson(repeatedResult.getResponse().getContentAsString(),
                TournamentExtraPropertyDTO.class);
        Assert.assertEquals(repeatedProperty.getPropertyValue(), "false");
    }

    @Test(dependsOnMethods = "shouldConfigureSwissPropertiesViaApi")
    public void shouldGenerateSwissRoundViaApi() throws Exception {
        final TeamDTO teamA = teamController.update(new TeamDTO("Team A", tournamentDTO), USER_NAME, null);
        final TeamDTO teamB = teamController.update(new TeamDTO("Team B", tournamentDTO), USER_NAME, null);
        final GroupDTO group = groupController.get(tournamentDTO).get(0);
        groupController.addTeams(group.getId(), List.of(teamA, teamB), USER_NAME, null);

        final MvcResult createResult = this.mockMvc
                .perform(put("/fights/create/tournaments/{tournamentId}/levels/{levelId}", tournamentDTO.getId(), 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();

        final List<FightDTO> fights = Arrays.asList(objectMapper.readValue(createResult.getResponse().getContentAsString(), FightDTO[].class));
        Assert.assertEquals(fights.size(), 1);
        Assert.assertEquals(fights.get(0).getTeam1().getTournament(), tournamentDTO);
        Assert.assertEquals(fights.get(0).getTeam2().getTournament(), tournamentDTO);
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        groupController.deleteAll();
        fightController.deleteAll();
        duelController.deleteAll();
        teamController.deleteAll();
        roleController.deleteAll();
        tournamentExtraPropertyController.deleteAll();
        tournamentController.deleteAll();
        participantController.deleteAll();
        authenticatedUserController.deleteAll();
    }
}


