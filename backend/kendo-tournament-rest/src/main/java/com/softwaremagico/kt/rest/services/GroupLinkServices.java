package com.softwaremagico.kt.rest.services;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
 * %%
 * Copyright (C) 2021 - 2024 Softwaremagico
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.softwaremagico.kt.core.controller.GroupLinkController;
import com.softwaremagico.kt.core.controller.models.GroupLinkDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/groups-links")
public class GroupLinkServices {

    private final GroupLinkController groupController;

    public GroupLinkServices(GroupLinkController groupController) {
        this.groupController = groupController;
    }


    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN', 'ROLE_GUEST')")
    @Operation(summary = "Gets all groups links.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/tournament/{tournamentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GroupLinkDTO> getAll(@Parameter(description = "Id of an existing tournament", required = true)
                                     @PathVariable("tournamentId") Integer tournamentId,
                                     HttpServletRequest request) {
        return groupController.getLinks(tournamentId);
    }
}
