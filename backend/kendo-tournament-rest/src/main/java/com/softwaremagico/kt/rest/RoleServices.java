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

import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.core.providers.RoleProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.Role;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.rest.exceptions.BadRequestException;
import com.softwaremagico.kt.rest.model.RoleDto;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.modelmapper.ModelMapper;
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
    private final ParticipantProvider participantProvider;
    private final RoleProvider roleProvider;
    private final TournamentProvider tournamentProvider;
    private final ModelMapper modelMapper;

    public RoleServices(ParticipantProvider participantProvider, RoleProvider roleProvider, TournamentProvider tournamentProvider, ModelMapper modelMapper) {
        this.participantProvider = participantProvider;
        this.roleProvider = roleProvider;
        this.tournamentProvider = tournamentProvider;
        this.modelMapper = modelMapper;
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @ApiOperation(value = "Gets all roles.")
    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Role> getAll(HttpServletRequest request) {
        return roleProvider.getAll();
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @ApiOperation(value = "Gets all roles from a tournament.")
    @GetMapping(value = "/tournaments/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Role> getAllFromTournament(@ApiParam(value = "Id of an existing tournament", required = true) @PathVariable("id") Integer id,
                                           HttpServletRequest request) {
        return roleProvider.getAll(tournamentProvider.get(id));
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @ApiOperation(value = "Gets all roles from a tournament.")
    @GetMapping(value = "/tournaments/{id}/types/{roleTypes}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Role> getAllFromTournament(@ApiParam(value = "Id of an existing tournament", required = true) @PathVariable("id") Integer id,
                                           @ApiParam(value = "Type of role") @PathVariable("roleTypes") Collection<RoleType> roleTypes,
                                           HttpServletRequest request) {
        return roleProvider.getAll(tournamentProvider.get(id), roleTypes);
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @ApiOperation(value = "Gets a role.")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Role get(@ApiParam(value = "Id of an existing role", required = true) @PathVariable("id") Integer id,
                    HttpServletRequest request) {
        return roleProvider.get(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Creates a role.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Role add(@RequestBody RoleDto roleDto, HttpServletRequest request) {
        if (roleDto == null || roleDto.getTournament() == null || roleDto.getParticipant() == null ||
                roleDto.getRoleType() == null) {
            throw new BadRequestException(getClass(), "Role data is missing");
        }
        final Role role = new Role();
        role.setParticipant(participantProvider.get(roleDto.getParticipant().getId()));
        role.setTournament(tournamentProvider.get(roleDto.getTournament().getId()));
        role.setType(roleDto.getRoleType());
        return roleProvider.save(role);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Deletes a role.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@ApiParam(value = "Id of an existing role", required = true) @PathVariable("id") Integer id,
                       HttpServletRequest request) {
        roleProvider.delete(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Updates a role.")
    @PutMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public Role update(
            @RequestBody RoleDto roleDto, HttpServletRequest request) {
        final Role role = modelMapper.map(roleDto, Role.class);
        if (roleDto.getTournament() != null) {
            role.setTournament(tournamentProvider.get(roleDto.getTournament().getId()));
        }
        if (roleDto.getParticipant() != null) {
            role.setParticipant(participantProvider.get(roleDto.getParticipant().getId()));
        }
        return roleProvider.update(role);
    }
}
