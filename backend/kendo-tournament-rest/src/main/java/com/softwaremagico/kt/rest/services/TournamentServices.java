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

import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.persistence.values.TournamentType;
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
@RequestMapping("/tournaments")
public class TournamentServices {
    private final TournamentController tournamentController;

    public TournamentServices(TournamentController tournamentController) {
        this.tournamentController = tournamentController;
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets all tournament.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TournamentDTO> getAll(HttpServletRequest request) {
        return tournamentController.get();
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets all tournament.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public long count(HttpServletRequest request) {
        return tournamentController.count();
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets a tournament.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TournamentDTO get(@Parameter(description = "Id of an existing tournament", required = true) @PathVariable("id") Integer id,
                             HttpServletRequest request) {
        return tournamentController.get(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Creates a tournament with some basic information.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/basic", produces = MediaType.APPLICATION_JSON_VALUE)
    public TournamentDTO add(@Parameter(description = "Name of the new tournament", required = true) @RequestParam(name = "name") String name,
                             @Parameter(description = "Number of available shiaijos") @RequestParam(name = "shiaijos") Integer shiaijos,
                             @Parameter(description = "Members by team") @RequestParam(name = "teamSize") Integer teamSize,
                             @Parameter(description = "Type of tournament") @RequestParam(name = "type") TournamentType type,
                             HttpServletRequest request) {
        return tournamentController.create(name, shiaijos, teamSize, type);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Creates a tournament with full information.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public TournamentDTO add(@RequestBody TournamentDTO tournamentDTO, HttpServletRequest request) {
        return tournamentController.create(tournamentDTO);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Deletes a tournament.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@Parameter(description = "Id of an existing tournament", required = true) @PathVariable("id") Integer id,
                       HttpServletRequest request) {
        tournamentController.deleteById(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Deletes a tournament.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody TournamentDTO tournamentDTO, HttpServletRequest request) {
        tournamentController.delete(tournamentDTO);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Updates a tournament.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public TournamentDTO update(@RequestBody TournamentDTO tournament, HttpServletRequest request) {
        return tournamentController.update(tournament);
    }
}
