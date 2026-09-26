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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwaremagico.kt.persistence.repositories.AuthenticatedUserRepository;
import com.softwaremagico.kt.persistence.repositories.TenantRepository;
import com.softwaremagico.kt.persistence.entities.TenantContext;
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@Test(groups = {"authFirstLoginIntegration"})
public class AuthFirstLoginIntegrationTest extends AbstractTestNGSpringContextTests {

    private static final String FIRST_ADMIN_USERNAME = "bootstrap.admin";
    private static final String FIRST_ADMIN_PASSWORD = "initialPassword123!";
    private static final String TENANT_NAME = TenantContext.LEGACY_TENANT_NAME;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthenticatedUserRepository authenticatedUserRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private String jwtToken;

    @BeforeClass
    public void cleanDatabase() {
        // Clear database to simulate fresh installation
        authenticatedUserRepository.deleteAll();
        tenantRepository.deleteAll();
    }

    private <T> String toJson(T object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

    @Test
    public void testFirstLoginCreatesAdminUserAndTenantAndCanLoginAgain() throws Exception {
        // Verify that the database is empty before the test
        Assert.assertEquals(authenticatedUserRepository.count(), 0L, "Database should be empty before first login");
        Assert.assertEquals(tenantRepository.count(), 0L, "No tenants should exist before first login");

        // Step 1: First login attempt with new credentials
        // This should trigger the creation of default tenant and admin user
        final AuthRequest firstLoginRequest = new AuthRequest();
        firstLoginRequest.setUsername(FIRST_ADMIN_USERNAME);
        firstLoginRequest.setPassword(FIRST_ADMIN_PASSWORD);
//		firstLoginRequest.setTenant(TENANT_NAME);

        final MvcResult firstLoginResult = this.mockMvc.perform(post("/auth/public/login").contentType(MediaType.APPLICATION_JSON).content(toJson(firstLoginRequest)).with(csrf())).andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.header().exists(HttpHeaders.AUTHORIZATION)).andReturn();

        // Verify that the JWT token was generated
        jwtToken = firstLoginResult.getResponse().getHeader(HttpHeaders.AUTHORIZATION);
        Assert.assertNotNull(jwtToken, "JWT token should be generated on first login");
        Assert.assertTrue(jwtToken.length() > 0, "JWT token should not be empty");

        // Verify that the session header is present
        final String sessionHeader = firstLoginResult.getResponse().getHeader("X-Session");
        Assert.assertNotNull(sessionHeader, "Session header should be present");

        // Verify that tenant was created
        Assert.assertEquals(tenantRepository.count(), 1L, "Default tenant should be created");
        Assert.assertNotNull(tenantRepository.findByNameAndActiveTrue(TENANT_NAME), "Default tenant 'Legacy organization' should exist");

        // Verify that the user was created
        Assert.assertEquals(authenticatedUserRepository.count(), 1L, "Admin user should be created");
        Assert.assertNotNull(authenticatedUserRepository.findByUsername(FIRST_ADMIN_USERNAME), "Admin user should be created with provided username");

        // Step 2: Try to login again with the same credentials
        // This should succeed without creating new users or tenants
        final AuthRequest secondLoginRequest = new AuthRequest();
        secondLoginRequest.setUsername(FIRST_ADMIN_USERNAME);
        secondLoginRequest.setPassword(FIRST_ADMIN_PASSWORD);

        final MvcResult secondLoginResult = this.mockMvc.perform(post("/auth/public/login").contentType(MediaType.APPLICATION_JSON).content(toJson(secondLoginRequest)).with(csrf())).andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.header().exists(HttpHeaders.AUTHORIZATION)).andReturn();

        // Verify that the JWT token was generated for the second login
        final String secondJwtToken = secondLoginResult.getResponse().getHeader(HttpHeaders.AUTHORIZATION);
        Assert.assertNotNull(secondJwtToken, "JWT token should be generated on second login");
        Assert.assertTrue(secondJwtToken.length() > 0, "JWT token should not be empty");

        // Verify that no new tenant or user was created on second login
        Assert.assertEquals(tenantRepository.count(), 1L, "Only one tenant should exist after second login");
        Assert.assertEquals(authenticatedUserRepository.count(), 1L, "Only one user should exist after second login");

        // Step 3: Verify that the JWT token can be used to access protected endpoints
        // Try to get the user's roles (which requires authentication)
        this.mockMvc.perform(get("/auth/roles").contentType(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + secondJwtToken).with(csrf())).andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();
    }
}




