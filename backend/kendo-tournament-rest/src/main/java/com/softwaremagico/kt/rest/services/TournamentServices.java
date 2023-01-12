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

import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.models.TournamentConverterRequest;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.logger.RestServerLogger;
import com.softwaremagico.kt.pdf.EmptyPdfBodyException;
import com.softwaremagico.kt.pdf.InvalidXmlElementException;
import com.softwaremagico.kt.pdf.controller.PdfController;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.TournamentRepository;
import com.softwaremagico.kt.persistence.values.TournamentType;
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
import java.util.Locale;

@RestController
@RequestMapping("/tournaments")
public class TournamentServices extends BasicServices<Tournament, TournamentDTO, TournamentRepository,
        TournamentProvider, TournamentConverterRequest, TournamentConverter, TournamentController> {
    private final PdfController pdfController;

    public TournamentServices(TournamentController tournamentController, PdfController pdfController) {
        super(tournamentController);
        this.pdfController = pdfController;
    }

    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Creates a tournament with some basic information.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/basic", produces = MediaType.APPLICATION_JSON_VALUE)
    public TournamentDTO add(@Parameter(description = "Name of the new tournament", required = true) @RequestParam(name = "name") String name,
                             @Parameter(description = "Number of available shiaijos") @RequestParam(name = "shiaijos") Integer shiaijos,
                             @Parameter(description = "Members by team") @RequestParam(name = "teamSize") Integer teamSize,
                             @Parameter(description = "Type of tournament") @RequestParam(name = "type") TournamentType type,
                             Authentication authentication, HttpServletRequest request) {
        return getController().create(name, shiaijos, teamSize, type, authentication.getName());
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Gets all accreditations from a tournament.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "{tournamentId}/accreditations", produces = MediaType.APPLICATION_PDF_VALUE)
    public byte[] getAllAccreditationsFromTournamentAsPdf(@Parameter(description = "Id of an existing tournament", required = true)
                                                          @PathVariable("tournamentId") Integer tournamentId,
                                                          Locale locale, HttpServletResponse response, HttpServletRequest request) {
        final TournamentDTO tournament = getController().get(tournamentId);
        final ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(tournament.getName() + " - accreditations.pdf").build();
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
        try {
            return pdfController.generateTournamentAccreditations(locale, tournament).generate();
        } catch (InvalidXmlElementException | EmptyPdfBodyException e) {
            RestServerLogger.errorMessage(this.getClass(), e);
            throw new BadRequestException(this.getClass(), e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Gets all diplomas from a tournament.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "{tournamentId}/diplomas", produces = MediaType.APPLICATION_PDF_VALUE)
    public byte[] getAllDiplomasFromTournamentAsPdf(@Parameter(description = "Id of an existing tournament", required = true)
                                                    @PathVariable("tournamentId") Integer tournamentId,
                                                    Locale locale, HttpServletResponse response, HttpServletRequest request) {
        final TournamentDTO tournament = getController().get(tournamentId);
        final ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(tournament.getName() + " - diplomas.pdf").build();
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
        try {
            return pdfController.generateTournamentDiplomas(tournament).generate();
        } catch (InvalidXmlElementException | EmptyPdfBodyException e) {
            RestServerLogger.errorMessage(this.getClass(), e);
            throw new BadRequestException(this.getClass(), e.getMessage());
        }
    }
}
