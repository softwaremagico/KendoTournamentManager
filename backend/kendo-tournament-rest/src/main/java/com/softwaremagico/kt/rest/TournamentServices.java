package com.softwaremagico.kt.rest;

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

import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.TournamentType;
import com.softwaremagico.kt.rest.model.TournamentDto;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
import java.util.List;

@RestController
@RequestMapping("/tournament")
public class TournamentServices {
    private final TournamentProvider tournamentProvider;
    private final ModelMapper modelMapper;

    public TournamentServices(TournamentProvider tournamentProvider, ModelMapper modelMapper) {
        this.tournamentProvider = tournamentProvider;
        this.modelMapper = modelMapper;
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @ApiOperation(value = "Gets all tournament.")
    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Tournament> getAll(HttpServletRequest request) {
        return tournamentProvider.getAll();
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @ApiOperation(value = "Gets a tournament.")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Tournament get(@ApiParam(value = "Id of an existing tournament", required = true) @PathParam("id") Integer id,
                          HttpServletRequest request) {
        return tournamentProvider.get(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Creates a tournament with some basic information.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/basic", produces = MediaType.APPLICATION_JSON_VALUE)
    public Tournament add(@ApiParam(value = "Name of the new tournament", required = true) @RequestParam(name = "name") String name,
                          @ApiParam(value = "Number of available shiaijos") @RequestParam(name = "shiaijos") Integer shiaijos,
                          @ApiParam(value = "Members by team") @RequestParam(name = "teamSize") Integer teamSize,
                          @ApiParam(value = "Type of tournament") @RequestParam(name = "type") TournamentType type,
                          HttpServletRequest request) {
        return tournamentProvider.add(name, shiaijos, teamSize, type);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Creates a tournament with full information.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Tournament add(@RequestBody TournamentDto tournament, HttpServletRequest request) {
        return tournamentProvider.add(modelMapper.map(tournament, Tournament.class));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Deletes a tournament.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@ApiParam(value = "Id of an existing tournament", required = true) @PathParam("id") Integer id,
                       HttpServletRequest request) {
        tournamentProvider.delete(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Deletes a tournament.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody TournamentDto tournament, HttpServletRequest request) {
        tournamentProvider.delete(modelMapper.map(tournament, Tournament.class));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Updates a tournament.")
    @PutMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public Tournament update(@RequestBody TournamentDto tournament, HttpServletRequest request) {
        return tournamentProvider.update(modelMapper.map(tournament, Tournament.class));
    }
}
