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

import com.softwaremagico.kt.core.controller.GroupController;
import com.softwaremagico.kt.core.controller.models.GroupDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/groups")
public class GroupServices {
    private final GroupController groupController;

    public GroupServices(GroupController groupController) {
        this.groupController = groupController;
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets all groups.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GroupDTO> getAll(HttpServletRequest request) {
        return groupController.get();
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets all groups.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/tournament/{tournamentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GroupDTO> getAll(@Parameter(description = "Id of an existing tournament", required = true) @PathVariable("tournamentId") Integer tournamentId,
                                 HttpServletRequest request) {
        return groupController.getFromTournament(tournamentId);
    }
}
