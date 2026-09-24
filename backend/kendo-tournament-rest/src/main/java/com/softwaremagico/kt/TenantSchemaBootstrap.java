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
import com.softwaremagico.kt.persistence.repositories.TenantRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the legacy tenant only for a genuinely new, empty installation.
 */
@Component
@Order(1)
public class TenantSchemaBootstrap implements ApplicationRunner {
    private final TenantRepository tenantRepository;
    private final JdbcTemplate jdbcTemplate;

    public TenantSchemaBootstrap(TenantRepository tenantRepository, JdbcTemplate jdbcTemplate) {
        this.tenantRepository = tenantRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (tenantRepository.count() != 0) {
            return;
        }
        final Long existingRecords = jdbcTemplate.queryForObject(
                "SELECT (SELECT COUNT(*) FROM authenticated_users) + (SELECT COUNT(*) FROM clubs) + (SELECT COUNT(*) FROM tournaments)",
                Long.class);
        if (existingRecords != null && existingRecords > 0) {
            throw new IllegalStateException("Existing data detected. Run the 3.6.0 tenancy migration before starting the application.");
        }
        tenantRepository.save(new Tenant("Legacy organization"));
    }
}
