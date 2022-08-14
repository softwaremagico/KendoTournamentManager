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

import com.softwaremagico.kt.core.controller.RoleController;
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.models.ParticipantInTournamentDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.logger.RestServerLogger;
import com.softwaremagico.kt.pdf.EmptyPdfBodyException;
import com.softwaremagico.kt.pdf.InvalidXmlElementException;
import com.softwaremagico.kt.pdf.controller.PdfController;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.rest.exceptions.BadRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/roles")
public class RoleServices {
    private final RoleController roleController;

    private final PdfController pdfController;

    private final TournamentController tournamentController;

    public RoleServices(RoleController roleController, PdfController pdfController, TournamentController tournamentController) {
        this.roleController = roleController;
        this.pdfController = pdfController;
        this.tournamentController = tournamentController;
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets all roles.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RoleDTO> getAll(HttpServletRequest request) {
        return roleController.get();
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Counts all roles.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public long count(HttpServletRequest request) {
        return roleController.count();
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets all roles from a tournament.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/tournaments/{tournamentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RoleDTO> getAllFromTournament(@Parameter(description = "Id of an existing tournament", required = true) @PathVariable("tournamentId")
                                                  Integer tournamentId,
                                              HttpServletRequest request) {
        return roleController.getByTournamentId(tournamentId);
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets all roles from a tournament.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/tournaments/{tournamentId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public byte[] getAllFromTournamentAsPdf(@Parameter(description = "Id of an existing tournament", required = true) @PathVariable("tournamentId")
                                                Integer tournamentId,
                                            Locale locale, HttpServletResponse response, HttpServletRequest request) {
        final TournamentDTO tournament = tournamentController.get(tournamentId);
        final ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(tournament.getName() + " - role list.pdf").build();
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
        try {
            return pdfController.generateClubList(locale, tournament).generate();
        } catch (InvalidXmlElementException | EmptyPdfBodyException e) {
            RestServerLogger.errorMessage(this.getClass(), e);
            throw new BadRequestException(this.getClass(), e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets all roles from a tournament.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/tournaments/{id}/types/{roleTypes}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RoleDTO> getAllFromTournament(@Parameter(description = "Id of an existing tournament", required = true) @PathVariable("id") Integer id,
                                              @Parameter(description = "Type of role") @PathVariable("roleTypes") Collection<RoleType> roleTypes,
                                              HttpServletRequest request) {
        return roleController.get(id, roleTypes);
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets a role.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RoleDTO get(@Parameter(description = "Id of an existing role", required = true) @PathVariable("id") Integer id,
                       HttpServletRequest request) {
        return roleController.get(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Creates a role.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public RoleDTO add(@RequestBody RoleDTO roleDto, Authentication authentication, HttpServletRequest request) {
        if (roleDto == null || roleDto.getTournament() == null || roleDto.getParticipant() == null ||
                roleDto.getRoleType() == null) {
            throw new BadRequestException(getClass(), "Role data is missing");
        }
        return roleController.create(roleDto, authentication.getName());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Deletes a role.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@Parameter(description = "Id of an existing role", required = true) @PathVariable("id") Integer id,
                       HttpServletRequest request) {
        roleController.deleteById(id);
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Deletes a role.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody RoleDTO role, HttpServletRequest request) {
        roleController.delete(role);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Deletes a role.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/delete/participants", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody ParticipantInTournamentDTO participantInTournament, HttpServletRequest request) {
        roleController.delete(participantInTournament.getParticipant(), participantInTournament.getTournament());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Updates a role.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public RoleDTO update(@RequestBody RoleDTO roleDto, Authentication authentication, HttpServletRequest request) {
        return roleController.update(roleDto, authentication.getName());
    }
}
