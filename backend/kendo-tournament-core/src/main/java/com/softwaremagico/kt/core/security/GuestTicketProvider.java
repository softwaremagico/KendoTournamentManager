package com.softwaremagico.kt.core.security;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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

/**
 * Creates and verifies the signed capability embedded in public guest QR links.
 * The ticket binds a tournament identifier to the tenant that generated it so an
 * anonymous caller cannot use a raw tournament ID as a cross-tenant selector.
 */
public interface GuestTicketProvider {

    String createGuestTicket(Integer tournamentId, Integer tenantId);

    /**
     * @return the tenant ID encoded in the ticket, or {@code null} if the ticket is
     *         invalid or was issued for a different tournament.
     */
    default Integer validateGuestTicket(String ticket, Integer tournamentId) {
        return null;
    }
}
