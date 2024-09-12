package com.softwaremagico.kt.rest;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2024 Softwaremagico
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
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantReducedDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.score.ScoreOfTeam;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.TournamentType;
import com.softwaremagico.kt.rest.controllers.AuthenticatedUserController;
import com.softwaremagico.kt.rest.security.dto.AuthRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;


@SpringBootTest
@Test(groups = {"restSimpleChampionshipTest"})
public class RestSimpleChampionshipTest extends AbstractTestNGSpringContextTests {

    private static final String USER_NAME = "admin";
    private static final String USER_FIRST_NAME = "Test";
    private static final String USER_LAST_NAME = "User";
    private static final String USER_PASSWORD = "asd123";
    private static final String[] USER_ROLES = new String[]{"admin", "viewer"};

    private static final Integer MEMBERS = 3;
    private static final Integer TEAMS = 6;
    private static final String TOURNAMENT_NAME = "simpleChampionshipTest";

    private static final String CLUB_NAME = "The Club";

    private static final String CLUB_CITY = "Valencia";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthenticatedUserController authenticatedUserController;

    @Autowired
    private AuthenticationManager authenticationManager;

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


    private MockMvc mockMvc;

    private String jwtToken;

    private ClubDTO clubDTO;

    private TournamentDTO tournamentDTO;

    public static int getNumberOfCombats(Integer numberOfTeams) {
        return factorial(numberOfTeams) / (2 * factorial(numberOfTeams - 2));
    }

    private static int factorial(Integer n) {
        int total = 1;
        while (n > 1) {
            total = total * n;
            n--;
        }
        return total;
    }

    private void resetGroup(GroupDTO group) {
        group.getFights().forEach(fight -> {
            fight.getDuels().clear();
            fight.setFinishedAt(null);
            fightController.generateDuels(fight, null);
        });
        group.getUnties().clear();
        groupController.update(group, null);
    }

    private void resetGroup(TournamentDTO tournamentDTO) {
        resetGroup(groupController.get(tournamentDTO).get(0));
    }

    private <T> String toJson(T object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    private <T> T fromJson(String payload, Class<T> clazz) throws IOException {
        return objectMapper.readValue(payload, clazz);
    }

    @BeforeClass
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @BeforeClass(dependsOnMethods = "setUp")
    public void setAuthentication() throws Exception {
        //Create the admin user
        authenticatedUserController.createUser(null, USER_NAME, USER_FIRST_NAME, USER_LAST_NAME, USER_PASSWORD, USER_ROLES);

        AuthRequest request = new AuthRequest();
        request.setUsername(USER_NAME);
        request.setPassword(USER_PASSWORD);

        MvcResult createResult = this.mockMvc
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
    public void addClub() throws Exception {
        Assert.assertNotNull(jwtToken);

        MvcResult createResult = this.mockMvc
                .perform(post("/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(toJson(new ClubDTO(CLUB_NAME, CLUB_CITY)))
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();

        clubDTO = fromJson(createResult.getResponse().getContentAsString(), ClubDTO.class);
        Assert.assertEquals(clubDTO.getName(), CLUB_NAME);

        MvcResult countResult = this.mockMvc
                .perform(get("/clubs/count")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        Assert.assertEquals(fromJson(countResult.getResponse().getContentAsString(), Integer.class), Integer.valueOf(1));
    }

    @Test(dependsOnMethods = "addClub")
    public void addParticipants() throws Exception {
        for (int i = 0; i < MEMBERS * TEAMS; i++) {
            MvcResult createResult = this.mockMvc
                    .perform(post("/participants")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + jwtToken)
                            .content(toJson(new ParticipantDTO(String.format("0000%s", i), String.format("name%s", i),
                                    String.format("lastname%s", i), clubDTO)))
                            .with(csrf()))
                    .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                    .andReturn();

            ParticipantDTO participantDTO = fromJson(createResult.getResponse().getContentAsString(), ParticipantDTO.class);
            Assert.assertEquals(participantDTO.getName(), String.format("Name%s", i));
            Assert.assertEquals(participantDTO.getLastname(), String.format("Lastname%s", i));
            Assert.assertEquals(participantDTO.getIdCard(), String.format("0000%s", i));
        }

        MvcResult createResult = this.mockMvc
                .perform(get("/participants/count")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        Assert.assertEquals(fromJson(createResult.getResponse().getContentAsString(), Integer.class), Integer.valueOf(MEMBERS * TEAMS));
    }

    @Test(dependsOnMethods = "addParticipants")
    public void addTournament() throws Exception {
        MvcResult createResult = this.mockMvc
                .perform(post("/tournaments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(toJson(new TournamentDTO(TOURNAMENT_NAME, 1, MEMBERS, TournamentType.LEAGUE)))
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();

        tournamentDTO = fromJson(createResult.getResponse().getContentAsString(), TournamentDTO.class);
        Assert.assertEquals(tournamentDTO.getName(), TOURNAMENT_NAME);
        Assert.assertEquals(tournamentDTO.getShiaijos(), Integer.valueOf(1));
        Assert.assertEquals(tournamentDTO.getTeamSize(), MEMBERS);
        Assert.assertEquals(tournamentDTO.getType(), TournamentType.LEAGUE);

        MvcResult countResult = this.mockMvc
                .perform(get("/tournaments/count")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        Assert.assertEquals(fromJson(countResult.getResponse().getContentAsString(), Integer.class), Integer.valueOf(1));
    }

    @Test(dependsOnMethods = {"addTournament"})
    public void addRoles() throws Exception {
        for (ParticipantDTO competitor : participantController.get()) {
            MvcResult createResult = this.mockMvc
                    .perform(post("/roles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + jwtToken)
                            .content(toJson(new RoleDTO(tournamentDTO, competitor, RoleType.COMPETITOR)))
                            .with(csrf()))
                    .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                    .andReturn();

            RoleDTO roleDTO = fromJson(createResult.getResponse().getContentAsString(), RoleDTO.class);
            Assert.assertEquals(roleDTO.getTournament(), tournamentDTO);
            Assert.assertEquals(new ParticipantReducedDTO(roleDTO.getParticipant()), new ParticipantReducedDTO(competitor));
            Assert.assertEquals(roleDTO.getRoleType(), RoleType.COMPETITOR);
        }

        MvcResult createResult = this.mockMvc
                .perform(get("/roles/count")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        Assert.assertEquals(fromJson(createResult.getResponse().getContentAsString(), Integer.class), Integer.valueOf(MEMBERS * TEAMS));
    }

    @Test(dependsOnMethods = {"addRoles"})
    public void addTeams() throws Exception {
        int teamIndex = 0;
        TeamDTO team = null;
        int teamMember = 0;

        MvcResult createResult = this.mockMvc
                .perform(get("/groups/tournaments/" + tournamentDTO.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();

        List<GroupDTO> groupsDTO = Arrays.asList(objectMapper.readValue(createResult.getResponse().getContentAsString(), GroupDTO[].class));

        createResult = this.mockMvc
                .perform(get("/participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();

        ParticipantDTO[] participantDTOs = objectMapper.readValue(createResult.getResponse().getContentAsString(), ParticipantDTO[].class);

        for (ParticipantDTO competitor : participantDTOs) {
            // Create a new team.
            if (team == null) {
                teamIndex++;
                team = new TeamDTO("Team" + String.format("%02d", teamIndex), tournamentDTO);
                teamMember = 0;
            }

            // Add member.
            team.getMembers().add(competitor);

            createResult = this.mockMvc
                    .perform(put("/teams")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + jwtToken)
                            .content(toJson(team))
                            .with(csrf()))
                    .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                    .andReturn();

            team = fromJson(createResult.getResponse().getContentAsString(), TeamDTO.class);
            Assert.assertEquals(team.getTournament(), tournamentDTO);
            Assert.assertEquals(team.getName(), team.getName());

            //First member of the team, add team to group
            if (teamMember == 0) {
                this.mockMvc
                        .perform(patch("/groups/" + groupsDTO.get(0).getId() + "/teams/add")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + jwtToken)
                                .content(toJson(Collections.singleton(team)))
                                .with(csrf()))
                        .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                        .andReturn();
            }

            teamMember++;

            // Team filled up, create a new team.
            if (teamMember >= MEMBERS) {
                team = null;
            }
        }

        createResult = this.mockMvc
                .perform(get("/teams/tournaments/{tournamentId}/count", tournamentDTO.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(toJson(team)))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();


        Assert.assertEquals(Integer.parseInt(createResult.getResponse().getContentAsString()), (int) TEAMS);
    }

    @Test(dependsOnMethods = {"addTeams"})
    public void createFights() throws Exception {

        MvcResult createResult = this.mockMvc
                .perform(put("/fights/create/tournaments/{tournamentId}/levels/{levelId}", tournamentDTO.getId(), 0, true)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();

        List<FightDTO> tournamentFights = Arrays.asList(objectMapper.readValue(createResult.getResponse().getContentAsString(), FightDTO[].class));

        createResult = this.mockMvc
                .perform(get("/groups/tournaments/{tournamentId}", tournamentDTO.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();

        List<GroupDTO> tournamentGroups = Arrays.asList(objectMapper.readValue(createResult.getResponse().getContentAsString(), GroupDTO[].class));

        //Check group has been created.
        Assert.assertEquals(tournamentGroups.size(), 1);
        Assert.assertEquals(tournamentGroups.get(0).getFights().size(), tournamentFights.size());

        Assert.assertEquals(tournamentFights.size(), getNumberOfCombats(TEAMS));

        // Checks that teams have not crossed colors.
        for (int i = 0; i < tournamentFights.size() - 1; i++) {
            Assert.assertNotEquals(tournamentFights.get(i + 1).getTeam2(), tournamentFights.get(i).getTeam1());
            Assert.assertNotEquals(tournamentFights.get(i + 1).getTeam1(), tournamentFights.get(i).getTeam2());
        }
    }

    @Test(dependsOnMethods = {"createFights"})
    public void testSimpleWinner() throws Exception {

        while (!fightController.areOver(tournamentDTO)) {
            MvcResult createResult = this.mockMvc
                    .perform(get("/fights/tournaments/{tournamentId}/current", tournamentDTO.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + jwtToken)
                            .with(csrf()))
                    .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                    .andReturn();
            FightDTO currentFight = fromJson(createResult.getResponse().getContentAsString(), FightDTO.class);

            // First duel won
            currentFight.getDuels().get(0).getCompetitor1Score().add(Score.MEN);
            currentFight.getDuels().get(0).getCompetitor1Score().add(Score.MEN);
            currentFight.getDuels().forEach(duel -> duel.setFinished(true));

            //Update the fight.
            this.mockMvc
                    .perform(put("/fights")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + jwtToken)
                            .content(toJson(currentFight))
                            .with(csrf()))
                    .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                    .andReturn();
        }

        MvcResult createResult = this.mockMvc
                .perform(get("/groups/tournaments/{tournamentId}", tournamentDTO.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();

        List<GroupDTO> tournamentGroups = Arrays.asList(objectMapper.readValue(createResult.getResponse().getContentAsString(), GroupDTO[].class));

        Assert.assertEquals(tournamentGroups.size(), 1);

        MvcResult rankingResult = this.mockMvc
                .perform(get("/rankings/teams/tournaments/{tournamentId}", tournamentDTO.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();

        List<ScoreOfTeam> scores = Arrays.asList(objectMapper.readValue(createResult.getResponse().getContentAsString(), ScoreOfTeam[].class));


        for (int i = 0; i < scores.size() - 1; i++) {
            Assert.assertTrue(scores.get(i).getWonFights() >= scores.get(i + 1).getWonFights());
            Assert.assertTrue(scores.get(i).getWonDuels() >= scores.get(i + 1).getWonDuels());
            Assert.assertTrue(scores.get(i).getHits() >= scores.get(i + 1).getHits());
        }

        resetGroup(tournamentDTO);
    }

    @Test(dependsOnMethods = "testSimpleWinner")
    public void ensureParticipantsAreNotModifiedWhenUsingreduceParticipantDTO() {
        participantController.get().forEach(participantDTO -> {
            Assert.assertNotNull(participantDTO.getIdCard());
            Assert.assertTrue(participantDTO.getIdCard().startsWith("0000"));
        });
    }

    @AfterClass(alwaysRun = true)
    public void deleteTournament() throws Exception {
        this.mockMvc
                .perform(delete("/tournaments/{tournamentId}", tournamentDTO.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();

        Assert.assertEquals(fightController.count(tournamentDTO), 0);
        Assert.assertEquals(duelController.count(tournamentDTO), 0);
        Assert.assertEquals(groupController.count(tournamentDTO), 0);
        Assert.assertEquals(roleController.count(tournamentDTO), 0);
        Assert.assertEquals(teamController.count(tournamentDTO), 0);

    }

    @AfterClass(alwaysRun = true, dependsOnMethods = "deleteTournament")
    public void cleanUp() {
        groupController.deleteAll();
        fightController.deleteAll();
        duelController.deleteAll();
        teamController.deleteAll();
        roleController.deleteAll();
        tournamentExtraPropertyController.deleteAll();
        tournamentController.deleteAll();

        participantController.deleteAll();
        clubController.deleteAll();
        authenticatedUserController.deleteAll();
    }

}
