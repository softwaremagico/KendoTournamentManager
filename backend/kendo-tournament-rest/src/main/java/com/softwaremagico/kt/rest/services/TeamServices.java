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

import com.softwaremagico.kt.core.controller.TeamController;
import com.softwaremagico.kt.core.controller.models.*;
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
import java.util.List;

@RestController
@RequestMapping("/teams")
public class TeamServices {
    private final TeamController teamController;

    public TeamServices(TeamController teamController) {
        this.teamController = teamController;
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Gets all teams.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TeamDTO> getAll(HttpServletRequest request) {
        return teamController.get();
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Counts all teams.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public long count(HttpServletRequest request) {
        return teamController.count();
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Gets all teams from a tournament.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/tournaments/{tournamentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TeamDTO> getAll(@Parameter(description = "Id of an existing tournament", required = true) @PathVariable("tournamentId") Integer tournamentId,
                                Authentication authentication, HttpServletRequest request) {
        return teamController.getAllByTournament(tournamentId, authentication.getName());
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Counts all teams from a tournament.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/tournaments/{tournamentId}/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public long countByTournamentId(@Parameter(description = "Id of an existing tournament", required = true)
                                    @PathVariable("tournamentId") Integer tournamentId,
                                    Authentication authentication, HttpServletRequest request) {
        return teamController.countByTournament(tournamentId);
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Gets all teams.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/tournaments", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TeamDTO> getAll(@RequestBody TournamentDTO tournamentDto,
                                Authentication authentication, HttpServletRequest request) {
        return teamController.getAllByTournament(tournamentDto, authentication.getName());
    }


    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Generates default teams.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value = "/tournaments", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TeamDTO> create(@RequestBody TournamentDTO tournamentDto,
                                Authentication authentication, HttpServletRequest request) {
        return teamController.create(tournamentDto, authentication.getName());
    }

    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Deletes all teams from a tournament.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/tournaments/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody TournamentDTO tournamentDto, HttpServletRequest request) {
        teamController.delete(tournamentDto);
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Gets a team.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TeamDTO get(@Parameter(description = "Id of an existing team", required = true) @PathVariable("id") Integer id,
                       HttpServletRequest request) {
        return teamController.get(id);
    }

    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Creates a team.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public TeamDTO add(@RequestBody TeamDTO teamDTO, Authentication authentication, HttpServletRequest request) {
        if (teamDTO == null || teamDTO.getTournament() == null) {
            throw new BadRequestException(getClass(), "Team data is missing");
        }
        return teamController.create(teamDTO, authentication.getName());
    }

    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Defines the teams.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/set", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TeamDTO> set(@RequestBody List<TeamDTO> teamsDTOs, Authentication authentication, HttpServletRequest request) {
        if (teamsDTOs == null || teamsDTOs.isEmpty()) {
            throw new BadRequestException(getClass(), "Team data is missing");
        }
        teamsDTOs.forEach(teamDTO -> {
            if (teamDTO.getTournament() == null) {
                throw new BadRequestException(getClass(), "Team data is missing");
            }
        });
        teamController.delete(teamsDTOs.get(0).getTournament());
        return teamController.create(teamsDTOs, authentication.getName());
    }

    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Deletes a team.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@Parameter(description = "Id of an existing team", required = true) @PathVariable("id") Integer id,
                       HttpServletRequest request) {
        teamController.deleteById(id);
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Gets all teams.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody TeamDTO teamDto, HttpServletRequest request) {
        teamController.delete(teamDto);
    }

    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Deletes a member from any team.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/delete/members", produces = MediaType.APPLICATION_JSON_VALUE)
    public TeamDTO delete(@RequestBody ParticipantInTournamentDTO participantInTournament, HttpServletRequest request) {
        return teamController.delete(participantInTournament.getTournament(), participantInTournament.getParticipant());
    }

    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Deletes multiples member from any team.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/delete/members/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody ParticipantsInTournamentDTO participantsInTournaments, HttpServletRequest request) {
        for (final ParticipantDTO participantInTournament : participantsInTournaments.getParticipant()) {
            teamController.delete(participantsInTournaments.getTournament(), participantInTournament);
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Updates a team.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public TeamDTO update(@RequestBody TeamDTO teamDto, Authentication authentication, HttpServletRequest request) {
        return teamController.update(teamDto, authentication.getName());
    }
}
