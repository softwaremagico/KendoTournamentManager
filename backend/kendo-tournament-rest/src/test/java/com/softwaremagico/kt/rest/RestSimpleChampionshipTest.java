package com.softwaremagico.kt.rest;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2022 Softwaremagico
 * %%
 * This software is designed by Jorge Hortelano Otero. Jorge Hortelano Otero
 * <softwaremagico@gmail.com> Valencia (Spain).
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwaremagico.kt.core.controller.FightController;
import com.softwaremagico.kt.core.controller.GroupController;
import com.softwaremagico.kt.core.controller.ParticipantController;
import com.softwaremagico.kt.core.controller.models.*;
import com.softwaremagico.kt.persistence.values.RoleType;
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;


@SpringBootTest
@Test(groups = {"restSimpleChampionshipTest"})
public class RestSimpleChampionshipTest extends AbstractTestNGSpringContextTests {

    private static final String USER_NAME = "admin";
    private static final String USER_FULL_NAME = "Test User";
    private static final String USER_PASSWORD = "asd123";
    private static final String[] USER_ROLES = new String[]{"admin", "viewer"};

    private static final Integer MEMBERS = 3;
    private static final Integer TEAMS = 6;
    private static final String TOURNAMENT_NAME = "simpleChampionshipTest";

    private static final String CLUB_NAME = "The Club";

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
    private ParticipantController participantController;


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
            fightController.generateDuels(fight);
        });
        group.getUnties().clear();
        groupController.update(group);
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
        authenticatedUserController.createUser(USER_NAME, USER_FULL_NAME, USER_PASSWORD, USER_ROLES);

        AuthRequest request = new AuthRequest();
        request.setUsername(USER_NAME);
        request.setPassword(USER_PASSWORD);

        MvcResult createResult = this.mockMvc
                .perform(post("/auth/public/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().exists(HttpHeaders.AUTHORIZATION))
                .andReturn();

        jwtToken = createResult.getResponse().getHeader(HttpHeaders.AUTHORIZATION);
        Assert.assertNotNull(jwtToken);
    }

    @Test
    public void addClub() throws Exception {
        MvcResult createResult = this.mockMvc
                .perform(post("/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(toJson(new ClubDTO(CLUB_NAME))))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();

        clubDTO = fromJson(createResult.getResponse().getContentAsString(), ClubDTO.class);
        Assert.assertEquals(clubDTO.getName(), CLUB_NAME);

        MvcResult countResult = this.mockMvc
                .perform(get("/clubs/count")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken))
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
                                    String.format("lastname%s", i), clubDTO))))
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
                        .header("Authorization", "Bearer " + jwtToken))
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
                        .content(toJson(new TournamentDTO(TOURNAMENT_NAME, 1, MEMBERS, TournamentType.LEAGUE))))
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
                        .header("Authorization", "Bearer " + jwtToken))
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
                            .content(toJson(new RoleDTO(tournamentDTO, competitor, RoleType.COMPETITOR))))
                    .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                    .andReturn();

            RoleDTO roleDTO = fromJson(createResult.getResponse().getContentAsString(), RoleDTO.class);
            Assert.assertEquals(roleDTO.getTournament(), tournamentDTO);
            Assert.assertEquals(roleDTO.getParticipant(), competitor);
            Assert.assertEquals(roleDTO.getRoleType(), RoleType.COMPETITOR);
        }

        MvcResult createResult = this.mockMvc
                .perform(get("/roles/count")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken))
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
                .perform(get("/participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();


        Collection<ParticipantDTO> participantDTOs = objectMapper.readValue(createResult.getResponse().getContentAsString(),
                new TypeReference<List<ParticipantDTO>>() {
                });

        for (ParticipantDTO competitor : participantController.get()) {
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
                            .content(toJson(team)))
                    .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                    .andReturn();

            team = fromJson(createResult.getResponse().getContentAsString(), TeamDTO.class);
            Assert.assertEquals(team.getTournament(), tournamentDTO);
            Assert.assertEquals(team.getName(), team.getName());

            teamMember++;

            // Team filled up, create a new team.
            if (teamMember >= MEMBERS) {
                team = null;
            }
        }

        //Assert.assertEquals((int) TEAMS, teamProvider.count(tournament));
    }
}
