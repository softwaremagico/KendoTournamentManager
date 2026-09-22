package com.softwaremagico.kt.persistence.entities;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
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

/** Holds the tenant for the lifetime of a single server request. */
public final class TenantContext {
    /** Tenant reserved for installations and background work created before tenancy. */
    public static final int LEGACY_TENANT_ID = 1;
    private static final ThreadLocal<Integer> TENANT_ID = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setTenantId(Integer tenantId) {
        if (tenantId == null || tenantId < 1) {
            throw new IllegalArgumentException("A valid tenant ID is required.");
        }
        TENANT_ID.set(tenantId);
    }

    public static int getRequiredTenantId() {
        final Integer tenantId = TENANT_ID.get();
        return tenantId != null ? tenantId : LEGACY_TENANT_ID;
    }

    public static Integer getTenantId() {
        return TENANT_ID.get();
    }

    public static void clear() {
        TENANT_ID.remove();
    }
}
