package com.softwaremagico.kt;

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

import com.softwaremagico.kt.persistence.entities.Tenant;
import com.softwaremagico.kt.persistence.entities.TenantContext;
import com.softwaremagico.kt.persistence.repositories.AuthenticatedUserRepository;
import com.softwaremagico.kt.persistence.repositories.TenantRepository;
import com.softwaremagico.kt.rest.controllers.AuthenticatedUserController;
import com.softwaremagico.kt.security.AvailableRole;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.ApplicationArguments;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Test(groups = "superAdminBootstrap")
public class SuperAdminBootstrapTest {

	@Mock
	private TenantRepository tenantRepository;
	@Mock
	private AuthenticatedUserRepository authenticatedUserRepository;
	@Mock
	private AuthenticatedUserController authenticatedUserController;
	@Mock
	private ApplicationArguments arguments;

	private static final String USERNAME = "platform-admin@example.com";
	private static final String PASSWORD = "strong-unique-password";

	@BeforeMethod
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	private SuperAdminBootstrap newBootstrap(String username, String password) {
		return new SuperAdminBootstrap(username, password, tenantRepository, authenticatedUserRepository,
				authenticatedUserController);
	}

	@Test
	public void whenNoCredentialsConfiguredThenNoUserIsCreated() {
		newBootstrap("", "").run(arguments);

		verify(authenticatedUserRepository, never()).existsByRolesContaining(eq("super_admin"));
		verify(authenticatedUserController, never()).createUser(eq(null), eq(USERNAME), eq("Platform"), eq("Administrator"),
				eq(PASSWORD), eq(AvailableRole.SUPER_ADMIN));
	}

	@Test
	public void whenNoSuperAdminExistsThenItIsCreatedInsideTheLegacyTenant() {
		final Tenant legacyTenant = mock(Tenant.class);
		when(legacyTenant.getId()).thenReturn(TenantContext.LEGACY_TENANT_ID);
		when(tenantRepository.findById(TenantContext.LEGACY_TENANT_ID)).thenReturn(Optional.of(legacyTenant));
		when(authenticatedUserRepository.existsByRolesContaining("super_admin")).thenReturn(false);

		newBootstrap(USERNAME, PASSWORD).run(arguments);

		verify(authenticatedUserController).createUser(eq(null), eq(USERNAME), eq("Platform"), eq("Administrator"),
				eq(PASSWORD), eq(AvailableRole.SUPER_ADMIN));
		assertThat(TenantContext.getTenantId()).isNull();
	}

	@Test
	public void whenSuperAdminAlreadyExistsThenCreationIsSkipped() {
		when(authenticatedUserRepository.existsByRolesContaining("super_admin")).thenReturn(true);

		newSuperAdmin().run(arguments);

		verify(authenticatedUserController, never()).createUser(eq(null), eq(USERNAME), eq("Platform"),
				eq("Administrator"), eq(PASSWORD), eq(AvailableRole.SUPER_ADMIN));
	}

	@Test
	public void whenLegacyTenantIsMissingThenStartupFails() {
		when(tenantRepository.findById(TenantContext.LEGACY_TENANT_ID)).thenReturn(Optional.empty());

		final SuperAdminBootstrap bootstrap = newSuperAdmin();
		assertThatThrownBy(() -> bootstrap.run(arguments)).isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("Run the 3.6.0 tenancy migration");
	}

	private SuperAdminBootstrap newSuperAdmin() {
		return newBootstrap(USERNAME, PASSWORD);
	}
}