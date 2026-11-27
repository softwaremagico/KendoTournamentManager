package com.softwaremagico.kt.rest.services;

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

import com.softwaremagico.kt.core.controller.RegistrationController;
import com.softwaremagico.kt.core.controller.models.RegistrationConfigurationDTO;
import com.softwaremagico.kt.core.controller.models.RegistrationRequestDTO;
import com.softwaremagico.kt.core.controller.models.RegistrationResponseDTO;
import com.softwaremagico.kt.core.security.RegistrationInvitation;
import com.softwaremagico.kt.core.security.RegistrationTicketProvider;
import com.softwaremagico.kt.persistence.entities.TenantContext;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.rest.exceptions.BadRequestException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Credential-free registration endpoints. All of them are under
 * {@code /registration/public/**}, already covered by the security whitelist.
 * The signed invitation is the only authority: it carries the tenant and the
 * tournament, and its version is checked against the stored one so renewed
 * links stop working immediately.
 */
@RestController
@RequestMapping("/registration")
public class RegistrationPublicService {

    private static final String CREATED_BY = "public-registration";

    private final RegistrationTicketProvider registrationTicketProvider;

    private final TournamentProvider tournamentProvider;

    private final RegistrationController registrationController;

    @Autowired
    public RegistrationPublicService(RegistrationTicketProvider registrationTicketProvider,
                                     TournamentProvider tournamentProvider,
                                     RegistrationController registrationController) {
        this.registrationTicketProvider = registrationTicketProvider;
        this.tournamentProvider = tournamentProvider;
        this.registrationController = registrationController;
    }

    @Operation(summary = "Gets the minimal configuration needed by the public registration form.")
    @GetMapping(value = "/public/configuration", produces = MediaType.APPLICATION_JSON_VALUE)
    public RegistrationConfigurationDTO getConfiguration(@RequestParam(name = "invitation") String invitation) {
        try {
            final RegistrationInvitation decoded = validate(invitation);
            return registrationController.getConfiguration(decoded.tournamentId());
        } finally {
            TenantContext.clear();
        }
    }

    @Operation(summary = "Registers a participant in a tournament using a signed invitation.")
    @PostMapping(value = "/public/register", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public RegistrationResponseDTO register(@RequestParam(name = "invitation") String invitation,
                                            @Valid @RequestBody RegistrationRequestDTO request) {
        try {
            final RegistrationInvitation decoded = validate(invitation);
            return registrationController.register(decoded.tournamentId(), request, CREATED_BY);
        } finally {
            TenantContext.clear();
        }
    }

    private RegistrationInvitation validate(String invitation) {
        final RegistrationInvitation decoded = registrationTicketProvider.validateRegistrationTicket(invitation);
        if (decoded == null) {
            throw new BadRequestException(getClass(), "Invalid registration invitation.");
        }
        // Set the trusted tenant before any repository access.
        TenantContext.setTenantId(decoded.tenantId());
        final Tournament tournament = tournamentProvider.get(decoded.tournamentId()).orElseThrow(() ->
                new BadRequestException(getClass(), "Invalid registration invitation."));
        if (tournament.getRegistrationVersion() != decoded.version()) {
            throw new BadRequestException(getClass(), "This registration invitation has been revoked.");
        }
        return decoded;
    }
}
