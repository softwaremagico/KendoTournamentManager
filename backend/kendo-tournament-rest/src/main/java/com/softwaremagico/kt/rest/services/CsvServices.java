package com.softwaremagico.kt.rest.services;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
 * %%
 * Copyright (C) 2021 - 2025 Softwaremagico
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

import com.softwaremagico.kt.core.controller.CsvController;
import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/csv")
public class CsvServices {

    private final CsvController csvController;

    public CsvServices(CsvController csvController) {
        this.csvController = csvController;
    }


    @PreAuthorize("hasAnyAuthority(@securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Add clubs from a CSV file. Returns any failed club attempt.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/clubs", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ClubDTO> addClubs(@RequestParam("file") MultipartFile file,
                                  Authentication authentication, HttpServletRequest request) throws IOException {
        return csvController.addClubs(new String(file.getBytes(), StandardCharsets.UTF_8), authentication.getName());
    }


    @PreAuthorize("hasAnyAuthority(@securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Add participants from a CSV file. Returns any failed participant attempt.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/participants", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ParticipantDTO> addParticipants(@RequestParam("file") MultipartFile file,
                                                Authentication authentication, HttpServletRequest request) throws IOException {
        return csvController.addParticipants(new String(file.getBytes(), StandardCharsets.UTF_8), authentication.getName());
    }


    @PreAuthorize("hasAnyAuthority(@securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Add teams from a CSV file. Returns any failed team attempt.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/teams", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TeamDTO> addTeams(@RequestParam("file") MultipartFile file,
                                  Authentication authentication, HttpServletRequest request) throws IOException {
        return csvController.addTeams(new String(file.getBytes(), StandardCharsets.UTF_8), authentication.getName());
    }
}
