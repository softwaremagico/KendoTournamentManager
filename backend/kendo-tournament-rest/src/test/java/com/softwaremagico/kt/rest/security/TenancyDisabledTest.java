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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwaremagico.kt.persistence.entities.TenantContext;
import com.softwaremagico.kt.rest.controllers.AuthenticatedUserController;
import com.softwaremagico.kt.rest.security.dto.AuthRequest;
import com.softwaremagico.kt.rest.security.dto.CreateTenantRequest;
import com.softwaremagico.kt.security.AvailableRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end test of the {@code enable.tenancy=false} mode: the whole application behaves as a
 * single-organization installation using the Legacy organization, and every tenant administration
 * endpoint is rejected.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(properties = "enable.tenancy=false")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Test(groups = "tenancyDisabled")
public class TenancyDisabledTest extends AbstractTestNGSpringContextTests {
	private static final String LEGACY_TENANT = "Legacy organization";
	private static final String PLATFORM_ADMIN = "platform.single.admin";
	private static final String LEGACY_ADMIN = "legacy.single.admin";
	private static final String PASSWORD = "secure-password";

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private AuthenticatedUserController authenticatedUserController;

	private String superAdminToken;

	@BeforeClass
	public void bootstrapAdministrators() throws Exception {
		TenantContext.setTenantId(TenantContext.LEGACY_TENANT_ID);
		try {
			authenticatedUserController.createUser(null, PLATFORM_ADMIN, "Platform", "Admin", PASSWORD,
					AvailableRole.SUPER_ADMIN);
			authenticatedUserController.createUser(null, LEGACY_ADMIN, "Legacy", "Admin", PASSWORD,
					AvailableRole.ADMIN);
		} finally {
			TenantContext.clear();
		}
		superAdminToken = login(PLATFORM_ADMIN, "Any Unknown Tenant Name");
		Assert.assertNotNull(superAdminToken, "Login must work even with tenant tenancy disabled.");
	}

	@Test
	public void appConfigReportsTenancyDisabled() throws Exception {
		final String payload = mockMvc.perform(get("/info/app-config"))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		final Map<String, Boolean> config = objectMapper.readValue(payload, new TypeReference<>() {
		});
		Assert.assertEquals(config.get("tenancyEnabled"), Boolean.FALSE);
	}

	@Test(dependsOnMethods = "appConfigReportsTenancyDisabled")
	public void loginIgnoresTheRequestedTenant() throws Exception {
		// With tenancy disabled any requested tenant is mapped to the Legacy organization.
		final String token = login(LEGACY_ADMIN, "Another Non Existing Tenant");
		Assert.assertNotNull(token, "Login must ignore the requested tenant when tenancy is disabled.");

		// The token belongs to the Legacy organization and regular data endpoints work.
		mockMvc.perform(get("/clubs").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk());
	}

	@Test(dependsOnMethods = "loginIgnoresTheRequestedTenant")
	public void onlyTheLegacyTenantIsReportedPublicly() throws Exception {
		final String payload = mockMvc.perform(get("/auth/public/tenants"))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		final List<String> tenants = objectMapper.readValue(payload, new TypeReference<>() {
		});
		Assert.assertEquals(tenants, List.of(LEGACY_TENANT));
	}

	@Test(dependsOnMethods = "onlyTheLegacyTenantIsReportedPublicly")
	public void tenantAdministrationEndpointsAreRejected() throws Exception {
		mockMvc.perform(get("/auth/tenants").header(HttpHeaders.AUTHORIZATION, "Bearer " + superAdminToken))
				.andExpect(status().isNotFound());
		mockMvc.perform(patch("/auth/tenants/{tenantId}", TenantContext.LEGACY_TENANT_ID)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + superAdminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"" + LEGACY_TENANT + "\",\"active\":true}").with(csrf()))
				.andExpect(status().isNotFound());
		mockMvc.perform(delete("/auth/tenants/{tenantId}/data", TenantContext.LEGACY_TENANT_ID)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + superAdminToken).with(csrf()))
				.andExpect(status().isNotFound());

		final CreateTenantRequest request = new CreateTenantRequest();
		request.setTenant("Forbidden Tenant");
		request.setUsername("forbidden.admin");
		request.setPassword(PASSWORD);
		mockMvc.perform(post("/auth/tenants").header(HttpHeaders.AUTHORIZATION, "Bearer " + superAdminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)).with(csrf()))
				.andExpect(status().isNotFound());
	}

	private String login(String username, String tenant) throws Exception {
		final AuthRequest authRequest = new AuthRequest();
		authRequest.setUsername(username);
		authRequest.setPassword(PASSWORD);
		authRequest.setTenant(tenant);
		return mockMvc.perform(post("/auth/public/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(authRequest)).with(csrf()))
				.andExpect(status().isOk()).andReturn().getResponse().getHeader(HttpHeaders.AUTHORIZATION);
	}
}