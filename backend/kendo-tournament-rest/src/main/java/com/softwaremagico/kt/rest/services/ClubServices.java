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

import com.softwaremagico.kt.core.controller.ClubController;
import com.softwaremagico.kt.core.controller.models.ClubDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

@RestController
@RequestMapping("/clubs")
public class ClubServices {
    private final ClubController clubController;

    public ClubServices(ClubController clubController) {
        this.clubController = clubController;
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets all clubs.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<ClubDTO> getAll(HttpServletRequest request) {
        return clubController.get();
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets a club.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ClubDTO get(@Parameter(description = "Id of an existing club", required = true) @PathVariable("id") Integer id,
                       HttpServletRequest request) {
        return clubController.get(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Creates a club with some basic information.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/basic", produces = MediaType.APPLICATION_JSON_VALUE)
    public ClubDTO add(@Parameter(description = "Name of the new club", required = true) @RequestParam(name = "name") String name,
                       @Parameter(description = "Country where the club is located", required = true) @RequestParam(name = "country") String country,
                       @Parameter(description = "City where the club is located", required = true) @RequestParam(name = "city") String city,
                       HttpServletRequest request) {
        return clubController.create(name, country, city);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Creates a club with full information.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ClubDTO add(@RequestBody ClubDTO club, HttpServletRequest request) {
        return clubController.create(club);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Deletes a club.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@Parameter(description = "Id of an existing club", required = true) @PathVariable("id") Integer id,
                       HttpServletRequest request) {
        clubController.deleteById(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Deletes a club.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody ClubDTO club, HttpServletRequest request) {
        clubController.delete(club);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Updates a club.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ClubDTO update(@RequestBody ClubDTO club, HttpServletRequest request) {
        return clubController.update(club);
    }
}
