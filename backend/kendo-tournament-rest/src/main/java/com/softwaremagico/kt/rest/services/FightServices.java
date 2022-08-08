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

import com.softwaremagico.kt.core.controller.FightController;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.rest.exceptions.BadRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/fights")
public class FightServices {
    private final FightController fightController;

    public FightServices(FightController fightProvider) {
        this.fightController = fightProvider;
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets all fights.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<FightDTO> getAll(HttpServletRequest request) {
        return fightController.get();
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets all fights on tournament.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/tournaments/{tournamentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FightDTO> getAll(@Parameter(description = "Id of an existing tournament", required = true) @PathVariable("tournamentId") Integer tournamentId,
                                 HttpServletRequest request) {
        return fightController.getByTournamentId(tournamentId);
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets all fights.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/tournaments", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FightDTO> getAll(@RequestBody TournamentDTO tournamentDto,
                                 HttpServletRequest request) {
        return fightController.get(tournamentDto);
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets a fight.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public FightDTO get(@Parameter(description = "Id of an existing fight", required = true) @PathVariable("id") Integer id,
                        HttpServletRequest request) {
        return fightController.get(id);
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets current fight.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/tournaments/{tournamentId}/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public FightDTO getCurrent(@Parameter(description = "Id of an existing tournament", required = true) @PathVariable("tournamentId") Integer tournamentId,
                               HttpServletRequest request) {
        return fightController.getCurrent(tournamentId);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Creates a fight.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public FightDTO add(@RequestBody FightDTO fightDto, Authentication authentication, HttpServletRequest request) {
        if (fightDto == null) {
            throw new BadRequestException(getClass(), "Fight data is missing");
        }
        return fightController.create(fightDto, authentication.getName());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Creates a set of fights.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FightDTO> add(@RequestBody Collection<FightDTO> fightDtos, Authentication authentication, HttpServletRequest request) {
        if (fightDtos == null || fightDtos.isEmpty()) {
            throw new BadRequestException(getClass(), "Fight data is missing");
        }
        return fightController.create(fightDtos, authentication.getName());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Deletes a fight.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@Parameter(description = "Id of an existing fight", required = true) @PathVariable("id") Integer id,
                       HttpServletRequest request) {
        fightController.deleteById(id);
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Deletes a fight.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody FightDTO fightDto, HttpServletRequest request) {
        fightController.delete(fightDto);
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Deletes a collection of fights.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/delete/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody Collection<FightDTO> fightDtos, HttpServletRequest request) {
        fightController.delete(fightDtos);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Deletes all fights from a tournament.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/delete/tournaments", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody TournamentDTO tournamentDto, HttpServletRequest request) {
        fightController.delete(tournamentDto);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Updates a fight.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public FightDTO update(@RequestBody FightDTO fightDto, Authentication authentication, HttpServletRequest request) {
        if (fightDto == null) {
            throw new BadRequestException(getClass(), "Fight data is missing");
        }
        return fightController.update(fightDto, authentication.getName());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Updates a fight.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value = "/create/tournaments/{tournamentId}/levels/{levelId}/maximize/{maximizeFights}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FightDTO> create(@Parameter(description = "Id of an existing tournament", required = true) @PathVariable("tournamentId") Integer tournamentId,
                                 @Parameter(description = "Create as much fights as possible", required = true) @PathVariable("maximizeFights") boolean maximizeFights,
                                 @Parameter(description = "Level of the tournament", required = true) @PathVariable("levelId") Integer levelId,
                                 HttpServletRequest request) {
        return fightController.createFights(tournamentId, maximizeFights, levelId);
    }


}
