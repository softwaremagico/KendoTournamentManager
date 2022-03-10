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

import com.softwaremagico.kt.core.providers.*;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.rest.exceptions.BadRequestException;
import com.softwaremagico.kt.rest.model.FightDto;
import com.softwaremagico.kt.rest.model.TournamentDto;
import com.softwaremagico.kt.rest.parsers.FightParser;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
    @ApiOperation(value = "Gets all fights.")
    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Fight> getAll(HttpServletRequest request) {
        return fightProvider.getFights();
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @ApiOperation(value = "Gets all fights on tournament.")
    @GetMapping(value = "/tournaments/{tournamentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Fight> getAll(@ApiParam(value = "Id of an existing tournament", required = true) @PathVariable("tournamentId") Integer tournamentId,
                              HttpServletRequest request) {
        return fightProvider.getFights(tournamentProvider.get(tournamentId));
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @ApiOperation(value = "Gets all fights.")
    @PostMapping(value = "/tournaments", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Fight> getAll(@RequestBody TournamentDto tournamentDto,
                              HttpServletRequest request) {
        return fightProvider.getFights(modelMapper.map(tournamentDto, Tournament.class));
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @ApiOperation(value = "Gets a fight.")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Fight get(@ApiParam(value = "Id of an existing fight", required = true) @PathVariable("id") Integer id,
                     HttpServletRequest request) {
        return fightProvider.getFight(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Creates a fight.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Fight add(@RequestBody FightDto fightDto, HttpServletRequest request) {
        if (fightDto == null) {
            throw new BadRequestException(getClass(), "Fight data is missing");
        }
        return fightProvider.save(fightParser.parse(fightDto));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Deletes a fight.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@ApiParam(value = "Id of an existing fight", required = true) @PathVariable("id") Integer id,
                       HttpServletRequest request) {
        fightProvider.delete(id);
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @ApiOperation(value = "Gets all fights.")
    @PostMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody FightDto fightDto, HttpServletRequest request) {
        fightProvider.delete(modelMapper.map(fightDto, Fight.class));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Deletes all fights from a tournament.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/delete/tournaments", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody TournamentDto tournamentDto, HttpServletRequest request) {
        fightProvider.delete(modelMapper.map(tournamentDto, Tournament.class));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Updates a fight.")
    @PutMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public Fight update(@RequestBody FightDto fightDto, HttpServletRequest request) {
        if (fightDto == null) {
            throw new BadRequestException(getClass(), "Fight data is missing");
        }
        return fightProvider.save(fightParser.parse(fightDto));
    }


}
