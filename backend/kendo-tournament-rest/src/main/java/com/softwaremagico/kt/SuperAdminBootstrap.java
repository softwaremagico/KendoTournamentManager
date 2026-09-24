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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the one platform administrator only when explicit bootstrap credentials are configured.
 */
@Component
public class SuperAdminBootstrap implements ApplicationRunner {
    private final String username;
    private final String password;
    private final TenantRepository tenantRepository;
    private final AuthenticatedUserRepository authenticatedUserRepository;
    private final AuthenticatedUserController authenticatedUserController;

    public SuperAdminBootstrap(@Value("${bootstrap.super-admin.username:}") String username,
                               @Value("${bootstrap.super-admin.password:}") String password,
                               TenantRepository tenantRepository, AuthenticatedUserRepository authenticatedUserRepository,
                               AuthenticatedUserController authenticatedUserController) {
        this.username = username;
        this.password = password;
        this.tenantRepository = tenantRepository;
        this.authenticatedUserRepository = authenticatedUserRepository;
        this.authenticatedUserController = authenticatedUserController;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (username.isBlank() || password.isBlank()
                || authenticatedUserRepository.existsByRolesContaining(AvailableRole.SUPER_ADMIN.name())) {
            return;
        }
        final Tenant legacyTenant = tenantRepository.findById(TenantContext.LEGACY_TENANT_ID).orElseThrow(() ->
                new IllegalStateException("Run the 3.6.0 tenancy migration before bootstrapping a super administrator."));
        if (legacyTenant.getId() == null || legacyTenant.getId() != TenantContext.LEGACY_TENANT_ID) {
            throw new IllegalStateException("Legacy organization must have tenant ID 1.");
        }
        TenantContext.setTenantId(legacyTenant.getId());
        try {
            authenticatedUserController.createUser(null, username, "Platform", "Administrator", password,
                    AvailableRole.SUPER_ADMIN);
        } finally {
            TenantContext.clear();
        }
    }
}
