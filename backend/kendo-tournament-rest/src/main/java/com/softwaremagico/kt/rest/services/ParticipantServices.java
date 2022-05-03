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

import com.softwaremagico.kt.core.controller.ParticipantController;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/participants")
public class ParticipantServices {
    private final ParticipantController participantController;

    public ParticipantServices(ParticipantController participantController) {
        this.participantController = participantController;
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets all participants.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ParticipantDTO> getAll(HttpServletRequest request) {
        return participantController.get();
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Counts all participants.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public long count(HttpServletRequest request) {
        return participantController.count();
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets a participant.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ParticipantDTO get(@Parameter(description = "Id of an existing participant", required = true) @PathVariable("id") Integer id,
                              HttpServletRequest request) {
        return participantController.get(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Creates a participant.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ParticipantDTO add(@RequestBody ParticipantDTO participantDTO, HttpServletRequest request) {
        return participantController.create(participantDTO);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Deletes a participant.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@Parameter(description = "Id of an existing participant", required = true) @PathVariable("id") Integer id,
                       HttpServletRequest request) {
        participantController.deleteById(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Deletes a participant.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody ParticipantDTO participantDTO, HttpServletRequest request) {
        participantController.delete(participantDTO);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Updates a participant.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ParticipantDTO update(@RequestBody ParticipantDTO participantDTO, HttpServletRequest request) {
        return participantController.update(participantDTO);
    }
}
