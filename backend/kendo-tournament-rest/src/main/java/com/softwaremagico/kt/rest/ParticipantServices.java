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

import com.softwaremagico.kt.core.providers.ClubProvider;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.persistence.entities.Club;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.rest.model.ParticipantDto;
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
@RequestMapping("/participants")
public class ParticipantServices {
    private final ParticipantProvider participantProvider;
    private final ClubProvider clubProvider;
    private final ModelMapper modelMapper;

    public ParticipantServices(ParticipantProvider participantProvider, ClubProvider clubProvider, ModelMapper modelMapper) {
        this.participantProvider = participantProvider;
        this.clubProvider = clubProvider;
        this.modelMapper = modelMapper;
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @ApiOperation(value = "Gets all participants.")
    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Participant> getAll(HttpServletRequest request) {
        return participantProvider.getAll();
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @ApiOperation(value = "Gets a participant.")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Participant get(@ApiParam(value = "Id of an existing participant", required = true) @PathParam("id") Integer id,
                           HttpServletRequest request) {
        return participantProvider.get(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Creates a participant.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Participant add(@RequestBody ParticipantDto participantDto, HttpServletRequest request) {
        final Club club;
        if (participantDto.getClub() != null) {
            club = clubProvider.get(participantDto.getClub().getId());
        } else {
            club = null;
        }
        final Participant participant = modelMapper.map(participantDto, Participant.class);
        participant.setClub(club);
        return participantProvider.save(participant);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Deletes a participant.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@ApiParam(value = "Id of an existing participant", required = true) @PathParam("id") Integer id,
                       HttpServletRequest request) {
        participantProvider.delete(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Deletes a participant.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody ParticipantDto participant, HttpServletRequest request) {
        participantProvider.delete(modelMapper.map(participant, Participant.class));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Updates a participant.")
    @PutMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public Participant update(@RequestBody ParticipantDto participantDto, HttpServletRequest request) {
        final Club club;
        if (participantDto.getClub() != null) {
            club = clubProvider.get(participantDto.getClub().getId());
        } else {
            club = null;
        }
        final Participant participant = modelMapper.map(participantDto, Participant.class);
        participant.setClub(club);
        return participantProvider.update(modelMapper.map(participantDto, Participant.class));
    }
}
