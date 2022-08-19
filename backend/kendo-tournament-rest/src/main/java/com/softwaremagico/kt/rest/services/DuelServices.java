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

import com.softwaremagico.kt.core.controller.DuelController;
import com.softwaremagico.kt.core.controller.models.DuelDTO;
import com.softwaremagico.kt.rest.exceptions.BadRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/duels")
public class DuelServices {
    private final DuelController duelController;

    public DuelServices(DuelController duelController) {
        this.duelController = duelController;
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Updates a duel.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value = "/fights", produces = MediaType.APPLICATION_JSON_VALUE)
    public DuelDTO update(@RequestBody DuelDTO duelDTO, Authentication authentication, HttpServletRequest request) {
        if (duelDTO == null) {
            throw new BadRequestException(getClass(), "Duel data is missing");
        }
        return duelController.update(duelDTO, authentication.getName());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Gets all untie duel.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/groups/{groupId}/unties", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DuelDTO> getUntiesFromGroup(@Parameter(description = "Id of the group.", required = true) @PathVariable("groupId") Integer groupId,
                                            HttpServletRequest request) {
        return duelController.getUntiesFromGroup(groupId);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Gets all untie duel.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/tournaments/{tournamentId}/unties", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DuelDTO> getUntiesFromTournament(@Parameter(description = "Id of the tournament.", required = true) @PathVariable("tournamentId")
                                                 Integer tournamentId,
                                                 HttpServletRequest request) {
        return duelController.getUntiesFromTournament(tournamentId);
    }


}
