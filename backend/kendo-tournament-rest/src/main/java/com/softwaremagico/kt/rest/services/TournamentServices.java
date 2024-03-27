package com.softwaremagico.kt.rest.services;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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

import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.models.TournamentConverterRequest;
import com.softwaremagico.kt.core.exceptions.NoContentException;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.logger.RestServerLogger;
import com.softwaremagico.kt.pdf.EmptyPdfBodyException;
import com.softwaremagico.kt.pdf.InvalidXmlElementException;
import com.softwaremagico.kt.pdf.controller.PdfController;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.TournamentRepository;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.TournamentType;
import com.softwaremagico.kt.rest.exceptions.BadRequestException;
import com.softwaremagico.kt.rest.exceptions.InvalidRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
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

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Gets all", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TournamentDTO> getAll(HttpServletRequest request) {
        final List<TournamentDTO> tournaments = super.getAll(request);
        tournaments.sort(Comparator.comparing(TournamentDTO::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())).reversed());
        return tournaments;
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
                                                          @Parameter(description = "Filter by roles")
                                                          @RequestParam(name = "roles", required = false) RoleType[] roles,
                                                          @Parameter(description = "Not printed before")
                                                          @RequestParam(name = "onlyNew", required = false) Boolean onlyNew,
                                                          Locale locale, HttpServletResponse response, Authentication authentication,
                                                          HttpServletRequest request) throws NoContentException {
        final TournamentDTO tournament = getController().get(tournamentId);
        final ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(tournament.getName() + " - accreditations.pdf").build();
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
        try {
            return pdfController.generateTournamentAccreditations(locale, tournament, onlyNew != null ? onlyNew : false, authentication.getName(),
                    roles).generate();
        } catch (InvalidXmlElementException | EmptyPdfBodyException e) {
            RestServerLogger.errorMessage(this.getClass(), e);
            throw new BadRequestException(this.getClass(), e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Gets an accreditations from a participant of a tournament.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "{tournamentId}/accreditations/{roleType}", produces = MediaType.APPLICATION_PDF_VALUE)
    public byte[] getParticipantAccreditationFromTournamentAsPdf(@Parameter(description = "Id of an existing tournament", required = true)
                                                                 @PathVariable("tournamentId") Integer tournamentId,
                                                                 @PathVariable("roleType") RoleType roleType,
                                                                 @RequestBody ParticipantDTO participant,
                                                                 Locale locale, HttpServletResponse response, Authentication authentication,
                                                                 HttpServletRequest request) {
        if (participant == null) {
            throw new InvalidRequestException(this.getClass(), "No participant provided!");
        }
        final TournamentDTO tournament = getController().get(tournamentId);
        final ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(tournament.getName() + " - accreditations.pdf").build();
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
        try {
            return pdfController.generateTournamentAccreditations(locale, tournament, participant, roleType, authentication.getName()).generate();
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
                                                    @Parameter(description = "Filter by roles")
                                                    @RequestParam(name = "roles", required = false) RoleType[] roles,
                                                    @Parameter(description = "Not printed before")
                                                    @RequestParam(name = "onlyNew", required = false) Boolean onlyNew,
                                                    Locale locale, HttpServletResponse response, Authentication authentication,
                                                    HttpServletRequest request) throws NoContentException {
        final TournamentDTO tournament = getController().get(tournamentId);
        final ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(tournament.getName() + " - diplomas.pdf").build();
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
        try {
            return pdfController.generateTournamentDiplomas(tournament, onlyNew != null ? onlyNew : false, authentication.getName(), roles).generate();
        } catch (InvalidXmlElementException | EmptyPdfBodyException e) {
            RestServerLogger.errorMessage(this.getClass(), e);
            throw new BadRequestException(this.getClass(), e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Gets a diploma from a participant of a tournament.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "{tournamentId}/diplomas", produces = MediaType.APPLICATION_PDF_VALUE)
    public byte[] getParticipantDiplomaFromTournamentAsPdf(@Parameter(description = "Id of an existing tournament", required = true)
                                                           @PathVariable("tournamentId") Integer tournamentId,
                                                           @RequestBody ParticipantDTO participant,
                                                           Locale locale, HttpServletResponse response, HttpServletRequest request) {
        if (participant == null) {
            throw new InvalidRequestException(this.getClass(), "No participant provided!");
        }
        final TournamentDTO tournament = getController().get(tournamentId);
        final ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(participant.getName() + " " + participant.getLastname() + " - diplomas.pdf").build();
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
        try {
            return pdfController.generateTournamentDiploma(tournament, participant).generate();
        } catch (InvalidXmlElementException | EmptyPdfBodyException e) {
            RestServerLogger.errorMessage(this.getClass(), e);
            throw new BadRequestException(this.getClass(), e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Creates a new tournament from the selected one.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "{tournamentId}/clone", produces = MediaType.APPLICATION_JSON_VALUE)
    public TournamentDTO clone(@Parameter(description = "Id of an existing tournament", required = true)
                               @PathVariable("tournamentId") Integer tournamentId,
                               Authentication authentication, HttpServletRequest request) {
        return getController().clone(tournamentId, authentication.getName());
    }

    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Set the number of winners that pass from level one to level two.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value = "{tournamentId}/winners/{numberOfWinners}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public void numberOfWinners(@Parameter(description = "Id of an existing tournament", required = true)
                                @PathVariable("tournamentId") Integer tournamentId,
                                @Parameter(description = "Number of winners", required = true)
                                @PathVariable("numberOfWinners") Integer numberOfWinners,
                                Authentication authentication, HttpServletRequest request) {
        getController().setNumberOfWinners(tournamentId, numberOfWinners, authentication.getName());
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN', 'ROLE_GUEST')")
    @Operation(summary = "Return the last unlocked tournament.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/unlocked/lasts", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public TournamentDTO getLastUnlockedTournament() {
        return getController().getLatestUnlocked();
    }
}
