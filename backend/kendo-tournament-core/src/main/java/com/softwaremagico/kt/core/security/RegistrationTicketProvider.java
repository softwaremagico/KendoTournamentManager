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
 * Creates and validates the signed capability embedded in public registration
 * links. As with guest tickets, the tenant is encoded in the token so anonymous
 * callers cannot choose a tenant with a raw tournament id.
 */
public interface RegistrationTicketProvider {

    String createRegistrationTicket(Integer tournamentId, Integer tenantId, int version);

    /**
     * @return the decoded invitation, or {@code null} if the ticket is invalid,
     *         expired or was tampered with.
     */
    default RegistrationInvitation validateRegistrationTicket(String ticket) {
        return null;
    }
}
