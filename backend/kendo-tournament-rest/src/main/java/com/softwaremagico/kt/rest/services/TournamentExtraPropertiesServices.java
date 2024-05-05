package com.softwaremagico.kt.rest.services;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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

import com.softwaremagico.kt.core.controller.TournamentExtraPropertyController;
import com.softwaremagico.kt.core.controller.models.TournamentExtraPropertyDTO;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tournaments/properties")
public class TournamentExtraPropertiesServices {
    private final TournamentExtraPropertyController tournamentExtraPropertyController;

    public TournamentExtraPropertiesServices(TournamentExtraPropertyController tournamentExtraPropertyController) {
        this.tournamentExtraPropertyController = tournamentExtraPropertyController;
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN', 'ROLE_GUEST')")
    @Operation(summary = "Gets tournament's properties.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/tournaments/{tournamentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TournamentExtraPropertyDTO> get(@Parameter(description = "Id of an existing tournament", required = true) @PathVariable("tournamentId")
                                                Integer tournamentId, Authentication authentication, HttpServletRequest request) {
        return tournamentExtraPropertyController.getByTournamentId(tournamentId);
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Gets tournament's properties by key.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/tournaments/{tournamentId}/key/{key}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TournamentExtraPropertyDTO getByKey(@Parameter(description = "Id of an existing tournament", required = true) @PathVariable("tournamentId")
                                               Integer tournamentId,
                                               @Parameter(description = "key of the property", required = true) @PathVariable("key")
                                               TournamentExtraPropertyKey key,
                                               Authentication authentication, HttpServletRequest request) {
        return tournamentExtraPropertyController.getByTournamentAndProperty(tournamentId, key);
    }

    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Creates a tournament property with some basic information.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public TournamentExtraPropertyDTO add(TournamentExtraPropertyDTO tournamentExtraPropertyDTO,
                                          Authentication authentication, HttpServletRequest request) {
        return tournamentExtraPropertyController.create(tournamentExtraPropertyDTO, authentication.getName());
    }

    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Updates a tournament property.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public TournamentExtraPropertyDTO update(@RequestBody TournamentExtraPropertyDTO tournamentExtraPropertyDTO,
                                             Authentication authentication, HttpServletRequest request) {
        tournamentExtraPropertyDTO.setCreatedBy(authentication.getName());
        return tournamentExtraPropertyController.update(tournamentExtraPropertyDTO, authentication.getName());
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Get latest selected properties from a user.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/latest", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TournamentExtraPropertyDTO> getLatest(Authentication authentication, HttpServletRequest request) {
        return tournamentExtraPropertyController.getLatest(authentication.getName());
    }

}
