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
import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.TemporalToken;
import com.softwaremagico.kt.core.providers.ClubProvider;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.rest.controllers.AuthenticatedUserController;
import com.softwaremagico.kt.rest.security.dto.AuthRequest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@Test(groups = "participantAccess")
public class ParticipantAccessTests extends AbstractTestNGSpringContextTests {

    private static final String USER_FIRST_NAME = "Test";
    private static final String USER_LAST_NAME = "User";

    private static final String USER_NAME = USER_FIRST_NAME + "." + USER_LAST_NAME;
    private static final String USER_PASSWORD = "password";
    private static final String[] USER_ROLES = new String[]{"admin", "viewer"};

    private static final String CLUB_NAME = "Club";

    private static final String PARTICIPANT_NAME = "Participant Name";
    private static final String PARTICIPANT_LASTNAME = "Participant Last Name";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthenticatedUserController authenticatedUserController;

    @Autowired
    private ClubProvider clubProvider;

    @Autowired
    private ParticipantProvider participantProvider;

    @Autowired
    private MockMvc mockMvc;

    private String jwtToken;
    private String participantJwtToken;

    private ClubDTO clubDTO;

    private ParticipantDTO participantDTO;
    private TemporalToken temporalToken;

    private <T> String toJson(T object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    private <T> T fromJson(String payload, Class<T> clazz) throws IOException {
        return objectMapper.readValue(payload, clazz);
    }

    @BeforeClass
    public void setUp() throws Exception {
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
    public void createClub() throws Exception {
        ClubDTO clubDTO = new ClubDTO();
        clubDTO.setName(CLUB_NAME);

        MvcResult createResult = this.mockMvc
                .perform(post("/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(toJson(clubDTO))
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();

        this.clubDTO = fromJson(createResult.getResponse().getContentAsString(), ClubDTO.class);

        Assert.assertEquals(this.clubDTO.getName(), CLUB_NAME);
    }

    @Test(dependsOnMethods = "createClub")
    public void createParticipant() throws Exception {
        ParticipantDTO participantDTO = new ParticipantDTO();
        participantDTO.setClub(this.clubDTO);
        participantDTO.setName(PARTICIPANT_NAME);
        participantDTO.setLastname(PARTICIPANT_LASTNAME);

        MvcResult createResult = this.mockMvc
                .perform(post("/participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(toJson(participantDTO))
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();

        this.participantDTO = fromJson(createResult.getResponse().getContentAsString(), ParticipantDTO.class);

        Assert.assertEquals(this.participantDTO.getName(), PARTICIPANT_NAME);
    }

    @Test(dependsOnMethods = "createParticipant")
    public void generateTemporalTokenParticipant() throws Exception {
        MvcResult createResult = this.mockMvc
                .perform(post("/participants/temporal-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(toJson(participantDTO))
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();

        this.temporalToken = fromJson(createResult.getResponse().getContentAsString(), TemporalToken.class);

        Assert.assertNotNull(temporalToken);
    }

    @Test(dependsOnMethods = "generateTemporalTokenParticipant")
    public void generateToken() throws Exception {
        MvcResult createResult = this.mockMvc
                .perform(post("/auth/public/participant/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(this.temporalToken))
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.header().exists(HttpHeaders.AUTHORIZATION))
                .andReturn();

        ParticipantDTO participantDTO = fromJson(createResult.getResponse().getContentAsString(), ParticipantDTO.class);

        this.participantJwtToken = createResult.getResponse().getHeader(HttpHeaders.AUTHORIZATION);

        Assert.assertEquals(participantDTO.getName(), PARTICIPANT_NAME);
    }

    @Test(dependsOnMethods = "generateToken")
    public void canAccessToItsOwnStatistics() throws Exception {
        this.mockMvc
                .perform(get("/statistics/participants/" + participantDTO.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + participantJwtToken)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
    }

    @Test(dependsOnMethods = "generateToken")
    public void cannotAccessToOthersStatistics() throws Exception {
        System.out.println("------------------------- Begin Expected Logged Exception -------------------------");
        this.mockMvc
                .perform(get("/statistics/participants/" + 25)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + participantJwtToken)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();
        System.out.println("------------------------- Begin Expected Logged Exception -------------------------");
    }

    @Test(dependsOnMethods = "generateToken")
    public void standardUserCanAccessToOthersStatistics() throws Exception {
        this.mockMvc
                .perform(get("/statistics/participants/" +  participantDTO.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
    }

    @Test(dependsOnMethods = "generateToken")
    public void cannotAccessToOtherServices() throws Exception {
        System.out.println("------------------------- Begin Expected Logged Exception -------------------------");
        this.mockMvc
                .perform(get("/participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + participantJwtToken)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andReturn();
        System.out.println("------------------------- End Expected Logged Exception -------------------------");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        participantProvider.deleteAll();
        clubProvider.deleteAll();
        authenticatedUserController.deleteAll();
    }
}
