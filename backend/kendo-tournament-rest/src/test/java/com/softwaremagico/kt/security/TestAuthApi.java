package com.softwaremagico.kt.security;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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
import com.softwaremagico.kt.persistence.entities.AuthenticatedUser;
import com.softwaremagico.kt.rest.controllers.AuthenticatedUserController;
import com.softwaremagico.kt.rest.security.dto.AuthRequest;
import com.softwaremagico.kt.rest.security.dto.CreateUserRequest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
@Test(groups = "authApi")
public class TestAuthApi extends AbstractTestNGSpringContextTests {
    private static final String USER_NAME = "user";
    private static final String USER_FIRST_NAME = "Test";
    private static final String USER_LAST_NAME = "User";

    private static final String USER_NEW_FIRST_NAME = "New Test";
    private static final String USER_NEW_LAST_NAME = "New User";
    private static final String USER_PASSWORD = "password";
    private static final String[] USER_ROLES = new String[]{"admin", "viewer"};

    private static final String USER2_NAME = "user2";
    private static final String USER2_FIRST_NAME = "Test2";
    private static final String USER2_LAST_NAME = "User2";
    private static final String USER2_PASSWORD = "password";

    private static final String USER2_NEW_FIRST_NAME = "New Test2";
    private static final String USER2_NEW_LAST_NAME = "New  User2";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthenticatedUserController authenticatedUserController;

    @Autowired
    private AuthenticationManager authenticationManager;

    private String jwtToken;

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

        authenticatedUserController.createUser(null, USER_NAME, USER_FIRST_NAME, USER_LAST_NAME, USER_PASSWORD, USER_ROLES);
    }

    @Test
    public void testAuthenticationToken() {
        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(USER_NAME, USER_PASSWORD));
    }

    @Test
    public void testLoginSuccess() throws Exception {
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

        AuthenticatedUser authenticatedUser = fromJson(createResult.getResponse().getContentAsString(), AuthenticatedUser.class);
        Assert.assertEquals(authenticatedUser.getAuthorities().size(), USER_ROLES.length);
        Assert.assertEquals(authenticatedUser.getUsername(), USER_NAME);
    }

    @Test
    public void testLoginFail() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setUsername(USER_NAME);
        request.setPassword("zxc");

        this.mockMvc
                .perform(post("/auth/public/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request))
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.header().doesNotExist(HttpHeaders.AUTHORIZATION))
                .andReturn();
    }

    @Test
    public void testJwt() throws Exception {
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

        //Check without header returns 401.
        this.mockMvc
                .perform(get("/dummy/test"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn();

        //With JWT header, must return 200.
        this.mockMvc
                .perform(get("/dummy/test").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
    }

    @Test
    public void testRegisterNotAuthorized() throws Exception {
        CreateUserRequest goodRequest = new CreateUserRequest();
        goodRequest.setUsername(String.format("%s_%d", USER_NAME + " A", System.currentTimeMillis()));
        goodRequest.setName(USER_NAME);
        goodRequest.setLastname(USER_LAST_NAME);
        goodRequest.setPassword(USER_PASSWORD);

        this.mockMvc
                .perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(goodRequest))
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn();
    }

    @Test(dependsOnMethods = "testJwt")
    public void testRegisterSuccess() throws Exception {
        CreateUserRequest goodRequest = new CreateUserRequest();
        goodRequest.setUsername(String.format("%s_%d", USER_NAME, System.currentTimeMillis()));
        goodRequest.setName(USER_NAME);
        goodRequest.setLastname(USER_LAST_NAME);
        goodRequest.setPassword(USER_PASSWORD);

        MvcResult createResult = this.mockMvc
                .perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(toJson(goodRequest))
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        AuthenticatedUser authenticatedUser = fromJson(createResult.getResponse().getContentAsString(), AuthenticatedUser.class);
        Assert.assertNotNull(authenticatedUser.getId());
        Assert.assertEquals(goodRequest.getLastname(), authenticatedUser.getLastname());
        Assert.assertEquals(goodRequest.getName(), authenticatedUser.getName());
    }

    @Test(dependsOnMethods = "testJwt")
    public void testRegisterFail() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("invalid.username");
        request.setName(USER_NAME);
        request.setLastname(USER_LAST_NAME);

        // Adding two times same user.
        this.mockMvc
                .perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(toJson(request))
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        System.out.println("***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***- Begin Expected Logged Exception ***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***-");
        this.mockMvc
                .perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(toJson(request))
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        System.out.println("***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***- End Expected Logged Exception ***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***-");
    }

    @Test(dependsOnMethods = "testJwt")
    public void testUpdateUser() throws Exception {
        CreateUserRequest updateRequest = new CreateUserRequest();
        updateRequest.setUsername(USER_NAME);
        updateRequest.setName(USER_NEW_FIRST_NAME);
        updateRequest.setLastname(USER_NEW_LAST_NAME);
        updateRequest.setPassword(USER_PASSWORD + "wrong");

        MvcResult createResult = this.mockMvc
                .perform(patch("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(toJson(updateRequest))
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        AuthenticatedUser authenticatedUser = fromJson(createResult.getResponse().getContentAsString(), AuthenticatedUser.class);
        Assert.assertNotNull(authenticatedUser.getId());
        Assert.assertEquals(updateRequest.getLastname(), authenticatedUser.getLastname());
        Assert.assertEquals(updateRequest.getName(), authenticatedUser.getName());
        Assert.assertNotEquals(updateRequest.getPassword(), authenticatedUser.getPassword());
    }


    @Test(dependsOnMethods = "testJwt")
    public void testUpdateUserPasswordNotChanged() throws Exception {

        authenticatedUserController.createUser(null, USER2_NAME, USER2_FIRST_NAME, USER2_LAST_NAME, USER2_PASSWORD, USER_ROLES);

        AuthRequest request = new AuthRequest();
        request.setUsername(USER2_NAME);
        request.setPassword(USER2_PASSWORD);

        MvcResult createResult = this.mockMvc
                .perform(post("/auth/public/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request))
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().exists(HttpHeaders.AUTHORIZATION))
                .andReturn();

        String newJwtToken = createResult.getResponse().getHeader(HttpHeaders.AUTHORIZATION);

        CreateUserRequest updateRequest = new CreateUserRequest();
        updateRequest.setUsername(USER2_NAME);
        updateRequest.setName(USER2_NEW_FIRST_NAME);
        updateRequest.setLastname(USER2_NEW_LAST_NAME);

        this.mockMvc
                .perform(patch("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + newJwtToken)
                        .content(toJson(updateRequest))
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();


        //Ensure that the password is not updated.
        request = new AuthRequest();
        request.setUsername(USER2_NAME);
        request.setPassword(USER2_PASSWORD);

        createResult = this.mockMvc
                .perform(post("/auth/public/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request))
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().exists(HttpHeaders.AUTHORIZATION))
                .andReturn();

        AuthenticatedUser authenticatedUser = fromJson(createResult.getResponse().getContentAsString(), AuthenticatedUser.class);
        Assert.assertEquals(authenticatedUser.getName(), USER2_NEW_FIRST_NAME);
    }
}
