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

import com.softwaremagico.kt.core.providers.ClubProvider;
import com.softwaremagico.kt.persistence.entities.Club;
import com.softwaremagico.kt.rest.model.ClubDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/clubs")
public class ClubServices {
    private final ClubProvider clubProvider;
    private final ModelMapper modelMapper;

    public ClubServices(ClubProvider clubProvider, ModelMapper modelMapper) {
        this.clubProvider = clubProvider;
        this.modelMapper = modelMapper;
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets all clubs.")
    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Club> getAll(HttpServletRequest request) {
        return clubProvider.getAll();
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets a club.")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Club get(@Parameter(description = "Id of an existing club", required = true) @PathVariable("id") Integer id,
                    HttpServletRequest request) {
        return clubProvider.get(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Creates a club with some basic information.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/basic", produces = MediaType.APPLICATION_JSON_VALUE)
    public Club add(@Parameter(description = "Name of the new club", required = true) @RequestParam(name = "name") String name,
                    @Parameter(description = "Country where the club is located", required = true) @RequestParam(name = "country") String country,
                    @Parameter(description = "City where the club is located", required = true) @RequestParam(name = "city") String city,
                    HttpServletRequest request) {
        return clubProvider.add(name, country, city);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Creates a club with full information.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Club add(@RequestBody ClubDto club, HttpServletRequest request) {
        return clubProvider.add(modelMapper.map(club, Club.class));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Deletes a club.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@Parameter(description = "Id of an existing club", required = true) @PathVariable("id") Integer id,
                       HttpServletRequest request) {
        clubProvider.delete(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Deletes a club.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody ClubDto club, HttpServletRequest request) {
        clubProvider.delete(modelMapper.map(club, Club.class));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Updates a club.")
    @PutMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public Club update(@RequestBody ClubDto club, HttpServletRequest request) {
        return clubProvider.update(modelMapper.map(club, Club.class));
    }
}
