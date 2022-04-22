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

import com.softwaremagico.kt.core.providers.FightProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.rest.exceptions.BadRequestException;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.rest.parsers.FightParser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/fights")
public class FightServices {
    private final TournamentProvider tournamentProvider;
    private final FightProvider fightProvider;
    private final FightParser fightParser;
    private final ModelMapper modelMapper;

    public FightServices(TournamentProvider tournamentProvider, FightProvider fightProvider, FightParser fightParser, ModelMapper modelMapper) {
        this.tournamentProvider = tournamentProvider;
        this.fightProvider = fightProvider;
        this.fightParser = fightParser;
        this.modelMapper = modelMapper;
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets all fights.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Fight> getAll(HttpServletRequest request) {
        return fightProvider.getFights();
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets all fights on tournament.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/tournaments/{tournamentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Fight> getAll(@Parameter(description = "Id of an existing tournament", required = true) @PathVariable("tournamentId") Integer tournamentId,
                              HttpServletRequest request) {
        return fightProvider.getFights(tournamentProvider.get(tournamentId));
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets all fights.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/tournaments", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Fight> getAll(@RequestBody TournamentDTO tournamentDto,
                              HttpServletRequest request) {
        return fightProvider.getFights(modelMapper.map(tournamentDto, Tournament.class));
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets a fight.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Fight get(@Parameter(description = "Id of an existing fight", required = true) @PathVariable("id") Integer id,
                     HttpServletRequest request) {
        return fightProvider.getFight(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Creates a fight.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Fight add(@RequestBody FightDTO fightDto, HttpServletRequest request) {
        if (fightDto == null) {
            throw new BadRequestException(getClass(), "Fight data is missing");
        }
        return fightProvider.save(fightParser.parse(fightDto));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Deletes a fight.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@Parameter(description = "Id of an existing fight", required = true) @PathVariable("id") Integer id,
                       HttpServletRequest request) {
        fightProvider.delete(id);
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets all fights.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody FightDTO fightDto, HttpServletRequest request) {
        fightProvider.delete(modelMapper.map(fightDto, Fight.class));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Deletes all fights from a tournament.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/delete/tournaments", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody TournamentDTO tournamentDto, HttpServletRequest request) {
        fightProvider.delete(modelMapper.map(tournamentDto, Tournament.class));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Updates a fight.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public Fight update(@RequestBody FightDTO fightDto, HttpServletRequest request) {
        if (fightDto == null) {
            throw new BadRequestException(getClass(), "Fight data is missing");
        }
        return fightProvider.save(fightParser.parse(fightDto));
    }


}
