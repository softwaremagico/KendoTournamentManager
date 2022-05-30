package com.softwaremagico.kt.security;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
@Test(groups = "authApi")
public class TestAuthApi extends AbstractTestNGSpringContextTests {
    private static final String USER_NAME = "user";
    private final static String USER_FIRST_NAME = "Test";
    private final static String USER_LAST_NAME = "User";
    private static final String USER_PASSWORD = "password";

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
    }

    @BeforeMethod
    public void clearDatabase() {
        authenticatedUserController.findAll().forEach(authenticatedUser -> {
            authenticatedUserController.delete(authenticatedUser);
        });
    }

    @Test
    public void testAuthenticationToken() {
        authenticatedUserController.createUser(USER_NAME, USER_FIRST_NAME, USER_LAST_NAME, USER_PASSWORD);

        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(USER_NAME, USER_PASSWORD));
    }

    @Test
    public void testLoginSuccess() throws Exception {
        AuthenticatedUser authenticatedUser = authenticatedUserController.createUser(USER_NAME, USER_FIRST_NAME, USER_LAST_NAME, USER_PASSWORD);

        AuthRequest request = new AuthRequest();
        request.setUsername(authenticatedUser.getUsername());
        request.setPassword(USER_PASSWORD);

        MvcResult createResult = this.mockMvc
                .perform(post("/auth/public/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().exists(HttpHeaders.AUTHORIZATION))
                .andReturn();

        AuthenticatedUser authUserView = fromJson(createResult.getResponse().getContentAsString(), AuthenticatedUser.class);
        Assert.assertEquals(authenticatedUser.getId(), authUserView.getId());
    }

    @Test
    public void testLoginFail() throws Exception {
        AuthenticatedUser authenticatedUser = authenticatedUserController.createUser(String.format(USER_NAME, System.currentTimeMillis()),
                USER_FIRST_NAME, USER_LAST_NAME, USER_PASSWORD);

        AuthRequest request = new AuthRequest();
        request.setUsername(authenticatedUser.getUsername());
        request.setPassword("zxc");

        this.mockMvc
                .perform(post("/auth/public/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.header().doesNotExist(HttpHeaders.AUTHORIZATION))
                .andReturn();
    }

    @Test
    public void testJwt() throws Exception {
        //Login as user
        AuthenticatedUser authenticatedUser = authenticatedUserController.createUser(USER_NAME, USER_FIRST_NAME, USER_LAST_NAME, USER_PASSWORD);

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

        //Check without header returns 401.
        this.mockMvc
                .perform(get("/dummy/test"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn();

        //With JWT header, must return 200.
        MvcResult jwtResult = this.mockMvc
                .perform(get("/dummy/test").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
    }

    @Test
    public void testRegisterNotAuthorized() throws Exception {
        CreateUserRequest goodRequest = new CreateUserRequest();
        goodRequest.setUsername(String.format(USER_NAME + " A", System.currentTimeMillis()));
        goodRequest.setName(USER_NAME);
        goodRequest.setLastName(USER_LAST_NAME);
        goodRequest.setPassword(USER_PASSWORD);
        goodRequest.setRePassword(USER_PASSWORD);

        MvcResult createResult = this.mockMvc
                .perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(goodRequest)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn();
    }

    @Test(dependsOnMethods = "testJwt")
    public void testRegisterSuccess() throws Exception {
        CreateUserRequest goodRequest = new CreateUserRequest();
        goodRequest.setUsername(String.format(USER_NAME + " A", System.currentTimeMillis()));
        goodRequest.setName(USER_NAME);
        goodRequest.setLastName(USER_LAST_NAME);
        goodRequest.setPassword(USER_PASSWORD);
        goodRequest.setRePassword(USER_PASSWORD);

        MvcResult createResult = this.mockMvc
                .perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(toJson(goodRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        AuthenticatedUser authenticatedUser = fromJson(createResult.getResponse().getContentAsString(), AuthenticatedUser.class);
        Assert.assertNotNull(authenticatedUser.getId());
        Assert.assertEquals(goodRequest.getLastName(), authenticatedUser.getLastname());
        Assert.assertEquals(goodRequest.getName(), authenticatedUser.getName());
    }

    @Test(dependsOnMethods = "testJwt")
    public void testRegisterFail() throws Exception {
        CreateUserRequest badRequest = new CreateUserRequest();
        badRequest.setUsername("invalid.username");
        badRequest.setName(USER_NAME);
        badRequest.setLastName(USER_LAST_NAME);

        // Adding two times same user.
        this.mockMvc
                .perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(toJson(badRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk());

        System.out.println("------------------------- Begin Expected Logged Exception -------------------------");
        this.mockMvc
                .perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(toJson(badRequest)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        System.out.println("------------------------- End Expected Logged Exception -------------------------");
    }
}
