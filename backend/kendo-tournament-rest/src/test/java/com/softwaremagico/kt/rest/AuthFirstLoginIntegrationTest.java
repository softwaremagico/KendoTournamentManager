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

import tools.jackson.databind.ObjectMapper;
import com.softwaremagico.kt.persistence.repositories.AuthenticatedUserRepository;
import com.softwaremagico.kt.persistence.repositories.TenantRepository;
import com.softwaremagico.kt.rest.security.dto.AuthRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@Test(groups = {"authFirstLoginIntegration"})
public class AuthFirstLoginIntegrationTest extends AbstractTestNGSpringContextTests {

    private static final String ATTACKER_USERNAME = "attacker";
    private static final String ATTACKER_PASSWORD = "chosenPassword123!";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthenticatedUserRepository authenticatedUserRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @BeforeMethod
    public void cleanDatabase() {
        // Clear database to simulate a fresh installation.
        authenticatedUserRepository.deleteAll();
        tenantRepository.deleteAll();
    }

    private <T> String toJson(T object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

    @Test
    public void testUnauthenticatedFirstLoginIsRejectedAndCreatesNothing() throws Exception {
        Assert.assertEquals(authenticatedUserRepository.count(), 0L, "Database should be empty before login");
        Assert.assertEquals(tenantRepository.count(), 0L, "No tenants should exist before login");

        final AuthRequest firstLoginRequest = new AuthRequest();
        firstLoginRequest.setUsername(ATTACKER_USERNAME);
        firstLoginRequest.setPassword(ATTACKER_PASSWORD);

        this.mockMvc.perform(post("/auth/public/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(firstLoginRequest))
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isServiceUnavailable())
                .andExpect(MockMvcResultMatchers.header().doesNotExist(HttpHeaders.AUTHORIZATION));

        Assert.assertEquals(authenticatedUserRepository.count(), 0L,
                "An anonymous caller must never create the first SUPER_ADMIN");
        Assert.assertEquals(tenantRepository.count(), 0L, "No tenant should be created by a failed login");
    }
}
