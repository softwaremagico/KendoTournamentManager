package com.softwaremagico.kt.rest;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
 * %%
 * Copyright (C) 2021 - 2025 Softwaremagico
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
import com.softwaremagico.kt.persistence.entities.Club;
import com.softwaremagico.kt.persistence.repositories.ClubRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@Test(groups = "userRegister")
public class UserRegisterTests extends AbstractTestNGSpringContextTests {

    private static final String USER_FIRST_NAME = "Test";
    private static final String USER_LAST_NAME = "User";

    private static final String USER_NAME = USER_FIRST_NAME + "." + USER_LAST_NAME;
    private static final String USER_PASSWORD = "password";
    private static final String[] USER_ROLES = new String[]{"admin", "viewer"};

    private static final String CLUB_NAME = "The Club";

    private static final String CLUB_CITY = "Valencia";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthenticatedUserController authenticatedUserController;

    @Autowired
    private ClubRepository clubRepository;

    private String jwtToken;

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
    public void whenCreatedClubThenCreatedByIsPopulated() throws Exception {
        Assert.assertNotNull(jwtToken);

        MvcResult createResult = this.mockMvc
                .perform(post("/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(toJson(new ClubDTO(CLUB_NAME, CLUB_CITY)))
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();

        ClubDTO clubDTO = fromJson(createResult.getResponse().getContentAsString(), ClubDTO.class);
        Assert.assertEquals(clubDTO.getCreatedBy(), USER_NAME);
    }

    @Test(dependsOnMethods = "whenCreatedClubThenCreatedByIsPopulated")
    public void createdByIsStoredOnDatabase() {
        Club club = clubRepository.findAll().get(0);
        Assert.assertNotNull(club);
        Assert.assertEquals(club.getCreatedBy(), USER_NAME);
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        clubRepository.deleteAll();
        authenticatedUserController.deleteAll();
    }
}
