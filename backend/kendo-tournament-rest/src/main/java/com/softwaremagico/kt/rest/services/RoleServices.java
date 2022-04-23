package com.softwaremagico.kt.rest.services;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
 * %%
 * Copyright (C) 2021 - 2022 Softwaremagico
 * %%
 * This software is designed by Jorge Hortelano Otero. Jorge Hortelano Otero
 * <softwaremagico@gmail.com> Valencia (Spain).
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.softwaremagico.kt.core.controller.RoleController;
import com.softwaremagico.kt.core.controller.models.ParticipantInTournamentDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.rest.exceptions.BadRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/roles")
public class RoleServices {
    private final RoleController roleController;

    public RoleServices(RoleController roleController) {
        this.roleController = roleController;
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets all roles.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RoleDTO> getAll(HttpServletRequest request) {
        return roleController.get();
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets all roles from a tournament.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/tournaments/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RoleDTO> getAllFromTournament(@Parameter(description = "Id of an existing tournament", required = true) @PathVariable("id") Integer id,
                                              HttpServletRequest request) {
        return roleController.getByTournamentId(id);
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets all roles from a tournament.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/tournaments/{id}/types/{roleTypes}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RoleDTO> getAllFromTournament(@Parameter(description = "Id of an existing tournament", required = true) @PathVariable("id") Integer id,
                                              @Parameter(description = "Type of role") @PathVariable("roleTypes") Collection<RoleType> roleTypes,
                                              HttpServletRequest request) {
        return roleController.get(id, roleTypes);
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets a role.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RoleDTO get(@Parameter(description = "Id of an existing role", required = true) @PathVariable("id") Integer id,
                       HttpServletRequest request) {
        return roleController.get(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Creates a role.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public RoleDTO add(@RequestBody RoleDTO roleDto, HttpServletRequest request) {
        if (roleDto == null || roleDto.getTournament() == null || roleDto.getParticipant() == null ||
                roleDto.getRoleType() == null) {
            throw new BadRequestException(getClass(), "Role data is missing");
        }
        return roleController.create(roleDto);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Deletes a role.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@Parameter(description = "Id of an existing role", required = true) @PathVariable("id") Integer id,
                       HttpServletRequest request) {
        roleController.deleteById(id);
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Deletes a role.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody RoleDTO role, HttpServletRequest request) {
        roleController.delete(role);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Deletes a role.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/delete/participants", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody ParticipantInTournamentDTO participantInTournament, HttpServletRequest request) {
        roleController.delete(participantInTournament.getParticipant(), participantInTournament.getTournament());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Updates a role.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public RoleDTO update(@RequestBody RoleDTO roleDto, HttpServletRequest request) {
        return roleController.update(roleDto);
    }
}
