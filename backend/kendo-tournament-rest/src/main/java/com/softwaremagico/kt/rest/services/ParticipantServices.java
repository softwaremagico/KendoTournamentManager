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

import com.softwaremagico.kt.core.controller.ParticipantController;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.TemporalToken;
import com.softwaremagico.kt.core.converters.ParticipantConverter;
import com.softwaremagico.kt.core.converters.models.ParticipantConverterRequest;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.repositories.ParticipantRepository;
import com.softwaremagico.kt.rest.security.KendoSecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/participants")
public class ParticipantServices extends BasicServices<Participant, ParticipantDTO, ParticipantRepository,
        ParticipantProvider, ParticipantConverterRequest, ParticipantConverter, ParticipantController> {

    private final KendoSecurityService kendoSecurityService;

    public ParticipantServices(ParticipantController participantController, KendoSecurityService kendoSecurityService) {
        super(participantController, kendoSecurityService);
        this.kendoSecurityService = kendoSecurityService;
    }

    /**
     * This method is done due to @PreAuthorize cannot be overridden. TournamentService need to set a GUEST permission to it.
     *
     * @return an array of roles.
     */
    @Override
    public String[] requiredRoleForEntityById() {
        return new String[]{kendoSecurityService.getParticipantPrivilege(),
                kendoSecurityService.getViewerPrivilege(), kendoSecurityService.getEditorPrivilege(), kendoSecurityService.getAdminPrivilege()};
    }


    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege,"
            + " @securityService.guestPrivilege, @securityService.participantPrivilege)")
    @Operation(summary = "Gets the participant data from the jwt token username.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/jwt", produces = MediaType.APPLICATION_JSON_VALUE)
    public ParticipantDTO getByUsername(Authentication authentication,
                                        HttpServletRequest request) {
        return getController().getByUserName(authentication.getName());
    }


    @PreAuthorize("hasAnyAuthority(@securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Creates a temporal token for a participant.")
    @PostMapping(value = "/temporal-token", produces = MediaType.APPLICATION_JSON_VALUE)
    public TemporalToken getTemporalToken(@RequestBody ParticipantDTO participantDTO,
                                          HttpServletRequest request) {
        return getController().generateTemporalToken(participantDTO);
    }
}
