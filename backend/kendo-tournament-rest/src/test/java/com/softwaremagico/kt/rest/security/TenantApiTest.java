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

import com.softwaremagico.kt.core.providers.TenantDataCleanupProvider;
import com.softwaremagico.kt.persistence.entities.AuthenticatedUser;
import com.softwaremagico.kt.persistence.entities.IAuthenticatedUser;
import com.softwaremagico.kt.persistence.entities.Tenant;
import com.softwaremagico.kt.persistence.repositories.TenantRepository;
import com.softwaremagico.kt.rest.controllers.AuthenticatedUserController;
import com.softwaremagico.kt.rest.exceptions.InvalidRequestException;
import com.softwaremagico.kt.rest.security.dto.CreateTenantRequest;
import com.softwaremagico.kt.rest.security.dto.UpdateTenantRequest;
import com.softwaremagico.kt.security.AvailableRole;
import jakarta.servlet.http.HttpServletRequest;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Test(groups = {"tenantApi"})
public class TenantApiTest {

	@Mock
	private JwtTokenUtil jwtTokenUtil;
	@Mock
	private AuthenticatedUserController authenticatedUserController;
	@Mock
	private TenantRepository tenantRepository;
	@Mock
	private TenantDataCleanupProvider tenantDataCleanupProvider;
	@Mock
	private HttpServletRequest httpRequest;

	private TenantApi tenantApi;

	@BeforeMethod(alwaysRun = true)
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		this.tenantApi = new TenantApi(this.jwtTokenUtil, this.authenticatedUserController, this.tenantRepository,
				this.tenantDataCleanupProvider);
	}

	// ========== Active Tenant Name Tests ==========

	@Test
	public void testGetActiveTenantNameWhenOnlyOneExists() {
		when(this.tenantRepository.findAllByActiveTrueOrderByNameAsc()).thenReturn(List.of(new Tenant("Alpha dojo")));

		assertThat(this.tenantApi.getActiveTenantNames()).containsExactly("Alpha dojo");
	}

	@Test
	public void testGetActiveTenantNamesDoesNotEnumerateMultipleTenants() {
		when(this.tenantRepository.findAllByActiveTrueOrderByNameAsc())
				.thenReturn(List.of(new Tenant("Alpha dojo"), new Tenant("Beta dojo")));

		assertThat(this.tenantApi.getActiveTenantNames()).isEmpty();
	}

	// ========== Tenant Creation Tests ==========

	@Test
	public void testCreateTenantReturnsCreatedAdminUser() {
		final CreateTenantRequest request = new CreateTenantRequest();
		request.setTenant("New tenant");
		request.setUsername("new.admin");
		request.setPassword("password");
		request.setName("New");
		request.setLastname("Admin");

		final Tenant savedTenant = mock(Tenant.class);
		when(savedTenant.getId()).thenReturn(100);
		final AuthenticatedUser admin = new AuthenticatedUser();
		admin.setUsername("new.admin");

		when(this.httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
		when(this.httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
		when(this.tenantRepository.findByNameAndActiveTrue("New tenant")).thenReturn(Optional.empty());
		when(this.tenantRepository.save(any(Tenant.class))).thenReturn(savedTenant);
		when(this.authenticatedUserController.createUser(null, "new.admin", "New", "Admin", "password",
				AvailableRole.ADMIN)).thenReturn(admin);
		when(this.jwtTokenUtil.getJwtExpirationTime()).thenReturn(3600000L);
		when(this.jwtTokenUtil.generateAccessToken(admin, "127.0.0.1")).thenReturn("jwt-token");
		when(this.jwtTokenUtil.getSession("jwt-token")).thenReturn("session-id");

		final ResponseEntity<IAuthenticatedUser> response = this.tenantApi.createTenant(request, this.httpRequest);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getHeaders().get(HttpHeaders.AUTHORIZATION)).containsExactly("jwt-token");
		assertThat(response.getHeaders().get(AuthApi.SESSION_HEADER)).containsExactly("session-id");
		verify(this.tenantRepository).save(any(Tenant.class));
	}

	@Test
	public void testCreateTenantIgnoresForwardedForHeader() {
		final CreateTenantRequest request = new CreateTenantRequest();
		request.setTenant("Tenant behind proxy");
		request.setUsername("proxy.admin");
		request.setPassword("password");

		final Tenant savedTenant = mock(Tenant.class);
		when(savedTenant.getId()).thenReturn(101);
		final AuthenticatedUser admin = new AuthenticatedUser();
		admin.setUsername("proxy.admin");

		when(this.httpRequest.getHeader("X-Forwarded-For")).thenReturn("203.0.113.7, 10.0.0.1");
		when(this.httpRequest.getRemoteAddr()).thenReturn("192.0.2.44");
		when(this.tenantRepository.save(any(Tenant.class))).thenReturn(savedTenant);
		when(this.authenticatedUserController.createUser(null, "proxy.admin", "", "", "password",
				AvailableRole.ADMIN)).thenReturn(admin);
		when(this.jwtTokenUtil.getJwtExpirationTime()).thenReturn(3600000L);
		when(this.jwtTokenUtil.generateAccessToken(admin, "192.0.2.44")).thenReturn("jwt-token");
		when(this.jwtTokenUtil.getSession("jwt-token")).thenReturn("session-id");

		this.tenantApi.createTenant(request, this.httpRequest);

		verify(this.jwtTokenUtil).generateAccessToken(admin, "192.0.2.44");
		verify(this.jwtTokenUtil, never()).generateAccessToken(admin, "203.0.113.7");
	}

	@Test
	public void testSelectTenantIgnoresForwardedForHeader() {
		final Tenant tenant = mock(Tenant.class);
		when(tenant.getId()).thenReturn(9);
		when(tenant.isActive()).thenReturn(true);
		final AuthenticatedUser member = new AuthenticatedUser();
		member.setUsername("tenant.member");
		final Authentication authentication = mock(Authentication.class);
		when(authentication.getName()).thenReturn("tenant.member");

		when(this.tenantRepository.findById(9)).thenReturn(Optional.of(tenant));
		when(this.authenticatedUserController.findAllByUsername("tenant.member")).thenReturn(List.of(member));
		when(this.authenticatedUserController.belongsToTenant(member, 9)).thenReturn(true);
		when(this.httpRequest.getHeader("X-Forwarded-For")).thenReturn("203.0.113.7");
		when(this.httpRequest.getRemoteAddr()).thenReturn("192.0.2.44");
		when(this.jwtTokenUtil.getJwtExpirationTime()).thenReturn(3600000L);
		when(this.jwtTokenUtil.generateAccessToken(member, "192.0.2.44")).thenReturn("jwt-token");
		when(this.jwtTokenUtil.getSession("jwt-token")).thenReturn("session-id");

		this.tenantApi.selectTenant(9, authentication, this.httpRequest);

		verify(this.jwtTokenUtil).generateAccessToken(member, "192.0.2.44");
		verify(this.jwtTokenUtil, never()).generateAccessToken(member, "203.0.113.7");
	}

	@Test
	public void testCreateTenantConflictsWithExistingTenant() {
		final CreateTenantRequest request = new CreateTenantRequest();
		request.setTenant("Existing tenant");
		when(this.tenantRepository.findByNameAndActiveTrue("Existing tenant"))
				.thenReturn(Optional.of(new Tenant("Existing tenant")));

		final ResponseEntity<IAuthenticatedUser> response = this.tenantApi.createTenant(request, this.httpRequest);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
	}

	// ========== Tenant Listing Tests ==========

	@Test
	public void testGetTenantsListsAllTenants() {
		final Tenant first = new Tenant("Tenant A");
		final Tenant second = new Tenant("Tenant B");
		when(this.tenantRepository.findAll()).thenReturn(List.of(first, second));

		final Collection<Tenant> tenants = this.tenantApi.getTenants();

		assertThat(tenants).hasSize(2).containsExactlyInAnyOrder(first, second);
	}

	// ========== Tenant Update Tests ==========

	@Test
	public void testUpdateTenantChangesNameAndActivity() {
		final Tenant tenant = new Tenant("Old name");
		final UpdateTenantRequest request = new UpdateTenantRequest();
		request.setName("New name");
		request.setActive(false);
		when(this.tenantRepository.findById(5)).thenReturn(Optional.of(tenant));
		when(this.tenantRepository.save(any(Tenant.class))).thenReturn(tenant);

		final Tenant updated = this.tenantApi.updateTenant(5, request);

		assertThat(updated.getName()).isEqualTo("New name");
		assertThat(updated.isActive()).isFalse();
	}

	@Test
	public void testUpdateTenantRejectsUnknownTenant() {
		final UpdateTenantRequest request = new UpdateTenantRequest();
		when(this.tenantRepository.findById(404)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> this.tenantApi.updateTenant(404, request))
				.isInstanceOf(InvalidRequestException.class).hasMessageContaining("Tenant not found.");
	}

	// ========== Tenant Data Deletion Tests ==========

	@Test
	public void testDeleteTenantDataDelegatesToProvider() {
		this.tenantApi.deleteTenantData(7);

		verify(this.tenantDataCleanupProvider).deleteAllData(7);
	}

	@Test
	public void testDeleteTenantDataRejectsProtectedTenant() {
		when(this.tenantDataCleanupProvider.deleteAllData(7))
				.thenThrow(new IllegalArgumentException("The last remaining tenant cannot be wiped."));

		assertThatThrownBy(() -> this.tenantApi.deleteTenantData(7))
				.isInstanceOf(InvalidRequestException.class)
				.hasMessageContaining("The last remaining tenant cannot be wiped.");
	}
}