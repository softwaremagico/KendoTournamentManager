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
import com.softwaremagico.kt.persistence.entities.Tenant;
import com.softwaremagico.kt.persistence.entities.TenantContext;
import com.softwaremagico.kt.persistence.repositories.TenantRepository;
import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.rest.controllers.AuthenticatedUserController;
import com.softwaremagico.kt.rest.security.dto.AuthRequest;
import com.softwaremagico.kt.rest.security.dto.CreateTenantRequest;
import com.softwaremagico.kt.rest.security.dto.UpdateTenantRequest;
import com.softwaremagico.kt.security.AvailableRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.Assert;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Test(groups = "tenantAdministration")
public class TenantAdministrationSecurityTest extends AbstractTestNGSpringContextTests {
    private static final String LEGACY_TENANT = "Legacy organization";
    private static final String PLATFORM_ADMIN = "platform.admin";
    private static final String TENANT_ADMIN = "tenant.admin";
    private static final String PASSWORD = "secure-password";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AuthenticatedUserController authenticatedUserController;
    @Autowired
    private TenantRepository tenantRepository;

    private String superAdminToken;
    private String tenantAdminToken;

    @BeforeClass
    public void setUpUsers() throws Exception {
        TenantContext.setTenantId(TenantContext.LEGACY_TENANT_ID);
        try {
            authenticatedUserController.createUser(null, PLATFORM_ADMIN, "Platform", "Admin", PASSWORD,
                    AvailableRole.SUPER_ADMIN);
            authenticatedUserController.createUser(null, TENANT_ADMIN, "Tenant", "Admin", PASSWORD,
                    AvailableRole.ADMIN);
        } finally {
            TenantContext.clear();
        }
        superAdminToken = login(PLATFORM_ADMIN, LEGACY_TENANT);
        tenantAdminToken = login(TENANT_ADMIN, LEGACY_TENANT);
    }

    @Test
    public void tenantAdminCannotAccessTenantAdministration() throws Exception {
        mockMvc.perform(get("/auth/tenants").header(HttpHeaders.AUTHORIZATION, "Bearer " + tenantAdminToken))
                .andExpect(status().isForbidden());
    }

    @Test(dependsOnMethods = "tenantAdminCannotAccessTenantAdministration")
    public void superAdminCanCreateAndUpdateTenant() throws Exception {
        final CreateTenantRequest createRequest = new CreateTenantRequest();
        createRequest.setTenant("Integration tenant");
        createRequest.setUsername("integration.admin");
        createRequest.setPassword(PASSWORD);
        createRequest.setName("Integration");
        createRequest.setLastname("Admin");

        mockMvc.perform(post("/auth/tenants")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(createRequest)).with(csrf()))
                .andExpect(status().isCreated());
        final Tenant tenant = tenantRepository.findByNameAndActiveTrue("Integration tenant").orElseThrow();

        final UpdateTenantRequest updateRequest = new UpdateTenantRequest();
        updateRequest.setName("Integration tenant disabled");
        updateRequest.setActive(false);
        mockMvc.perform(patch("/auth/tenants/{tenantId}", tenant.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateRequest)).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test(dependsOnMethods = "superAdminCanCreateAndUpdateTenant")
    public void disabledTenantCannotLogin() throws Exception {
        final AuthRequest request = new AuthRequest();
        request.setUsername("integration.admin");
        request.setPassword(PASSWORD);
        request.setTenant("Integration tenant disabled");

        mockMvc.perform(post("/auth/public/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)).with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test(dependsOnMethods = "disabledTenantCannotLogin")
    public void tenantsCannotReadEachOthersClubs() throws Exception {
        final Tenant firstTenant = tenantRepository.save(new Tenant("Isolation tenant A"));
        final Tenant secondTenant = tenantRepository.save(new Tenant("Isolation tenant B"));
        TenantContext.setTenantId(firstTenant.getId());
        try {
            authenticatedUserController.createUser(null, "isolation.admin.a", "Isolation", "A", PASSWORD,
                    AvailableRole.ADMIN);
        } finally {
            TenantContext.clear();
        }
        TenantContext.setTenantId(secondTenant.getId());
        try {
            authenticatedUserController.createUser(null, "isolation.admin.b", "Isolation", "B", PASSWORD,
                    AvailableRole.ADMIN);
        } finally {
            TenantContext.clear();
        }

        final String firstToken = login("isolation.admin.a", firstTenant.getName());
        final String secondToken = login("isolation.admin.b", secondTenant.getName());
        final ClubDTO sharedClub = new ClubDTO("Shared Club", "Madrid");

        mockMvc.perform(post("/clubs").header(HttpHeaders.AUTHORIZATION, "Bearer " + firstToken)
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(sharedClub)).with(csrf()))
                .andExpect(status().is2xxSuccessful());
        mockMvc.perform(post("/clubs").header(HttpHeaders.AUTHORIZATION, "Bearer " + secondToken)
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(sharedClub)).with(csrf()))
                .andExpect(status().is2xxSuccessful());

        final String firstClubs = mockMvc.perform(get("/clubs").header(HttpHeaders.AUTHORIZATION, "Bearer " + firstToken))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        final String secondClubs = mockMvc.perform(get("/clubs").header(HttpHeaders.AUTHORIZATION, "Bearer " + secondToken))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        Assert.assertEquals(objectMapper.readTree(firstClubs).size(), 1);
        Assert.assertEquals(objectMapper.readTree(secondClubs).size(), 1);
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
