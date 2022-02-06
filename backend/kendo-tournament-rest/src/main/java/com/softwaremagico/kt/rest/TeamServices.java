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
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.rest.exceptions.BadRequestException;
import com.softwaremagico.kt.rest.model.ParticipantDto;
import com.softwaremagico.kt.rest.model.TeamDto;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/teams")
public class TeamServices {
    private final ParticipantProvider participantProvider;
    private final TeamProvider teamProvider;
    private final TournamentProvider tournamentProvider;
    private final ModelMapper modelMapper;

    public TeamServices(ParticipantProvider participantProvider, TeamProvider teamProvider, TournamentProvider tournamentProvider, ModelMapper modelMapper) {
        this.participantProvider = participantProvider;
        this.teamProvider = teamProvider;
        this.tournamentProvider = tournamentProvider;
        this.modelMapper = modelMapper;
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @ApiOperation(value = "Gets all teams.")
    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Team> getAll(HttpServletRequest request) {
        return teamProvider.getAll();
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @ApiOperation(value = "Gets a team.")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Team get(@ApiParam(value = "Id of an existing team", required = true) @PathVariable("id") Integer id,
                    HttpServletRequest request) {
        return teamProvider.get(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Creates a team.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Team add(@RequestBody TeamDto teamDto, HttpServletRequest request) {
        if (teamDto == null || teamDto.getTournament() == null || teamDto.getMembers() == null) {
            throw new BadRequestException(getClass(), "Team data is missing");
        }
        final Team team = new Team();
        team.setName(teamDto.getName());
        team.setMembers(participantProvider.get(teamDto.getMembers().stream().map(ParticipantDto::getId)
                .collect(Collectors.toList())));
        team.setTournament(tournamentProvider.get(teamDto.getTournament().getId()));
        team.setGroup(teamDto.getGroup());
        return teamProvider.save(team);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Deletes a team.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@ApiParam(value = "Id of an existing team", required = true) @PathVariable("id") Integer id,
                       HttpServletRequest request) {
        teamProvider.delete(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Updates a team.")
    @PutMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public Team update(
            @RequestBody TeamDto teamDto, HttpServletRequest request) {
        final Team team = modelMapper.map(teamDto, Team.class);
        if (teamDto.getTournament() != null) {
            team.setTournament(tournamentProvider.get(teamDto.getTournament().getId()));
        }
        if (teamDto.getMembers() != null) {
            team.setMembers(participantProvider.get(teamDto.getMembers().stream().map(ParticipantDto::getId)
                    .collect(Collectors.toList())));
        }
        return teamProvider.update(team);
    }
}
