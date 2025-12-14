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

import com.softwaremagico.kt.core.controller.QrController;
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.models.QrCodeDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.logger.RestServerLogger;
import com.softwaremagico.kt.pdf.EmptyPdfBodyException;
import com.softwaremagico.kt.pdf.InvalidXmlElementException;
import com.softwaremagico.kt.pdf.controller.PdfController;
import com.softwaremagico.kt.rest.exceptions.BadRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;

@RestController
@RequestMapping("/qr")
public class QrService {

    private final QrController qrController;

    private final PdfController pdfController;

    private final TournamentController tournamentController;


    public QrService(QrController qrController, PdfController pdfController, TournamentController tournamentController) {
        this.qrController = qrController;
        this.pdfController = pdfController;
        this.tournamentController = tournamentController;
    }

    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Generates a QR code with the credentials to access as a guest for a tournament.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/guest/tournament/{tournamentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public QrCodeDTO generateGuestQrCodeForTournamentFights(@Parameter(description = "Id of an existing tournament", required = true)
                                                            @PathVariable("tournamentId") Integer tournamentId,
                                                            @RequestParam(name = "nightMode", required = false) Optional<Boolean> nightMode,
                                                            HttpServletResponse response, HttpServletRequest request) {
        return qrController.generateGuestQrCodeForTournamentFights(tournamentId, null, nightMode.orElse(false));
    }


    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Generates a QR code with the credentials to access as a guest for a tournament.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/guest/tournament/{tournamentId}/png", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] generateGuestQrCodeForTournamentFightsImage(@Parameter(description = "Id of an existing tournament", required = true)
                                                              @PathVariable("tournamentId") Integer tournamentId,
                                                              @RequestParam(name = "nightMode", required = false) Optional<Boolean> nightMode,
                                                              HttpServletResponse response, HttpServletRequest request) {

        final byte[] bytes = qrController.generateGuestQrCodeForTournamentFights(tournamentId, null, nightMode.orElse(false)).getData();
        final ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename("Tournament - QR.png").build();
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
        return bytes;
    }


    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Generates a QR code with the credentials to access as a guest for a tournament.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/guest/tournament/{tournamentId}/port/{port}", produces = MediaType.APPLICATION_JSON_VALUE)
    public QrCodeDTO generateGuestQrCodeForTournamentFights(@Parameter(description = "Id of an existing tournament", required = true)
                                                            @PathVariable("tournamentId") Integer tournamentId,
                                                            @Parameter(description = "Frontend port")
                                                            @PathVariable("port") Integer port,
                                                            @RequestParam(name = "nightMode", required = false) Optional<Boolean> nightMode,
                                                            HttpServletResponse response, HttpServletRequest request) {

        return qrController.generateGuestQrCodeForTournamentFights(tournamentId, port, nightMode.orElse(false));
    }


    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Generates a QR code with the credentials to access as a guest for a tournament.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/guest/tournament/{tournamentId}/port/{port}/png", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] generateGuestQrCodeForTournamentFightsImage(@Parameter(description = "Id of an existing tournament", required = true)
                                                              @PathVariable("tournamentId") Integer tournamentId,
                                                              @Parameter(description = "Frontend port")
                                                              @PathVariable("port") Integer port,
                                                              @RequestParam(name = "nightMode", required = false) Optional<Boolean> nightMode,
                                                              HttpServletResponse response, HttpServletRequest request) {

        final byte[] bytes = qrController.generateGuestQrCodeForTournamentFights(tournamentId, port, nightMode.orElse(false)).getData();
        final ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename("Tournament - QR.png").build();
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
        return bytes;
    }


    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Generates a QR code with the credentials to access as a guest for a tournament.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/guest/tournament/{tournamentId}/pdf", produces = {MediaType.APPLICATION_PDF_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public byte[] getParticipantDiplomaFromTournamentAsPdf(@Parameter(description = "Id of an existing tournament", required = true)
                                                           @PathVariable("tournamentId") Integer tournamentId,
                                                           Locale locale, HttpServletResponse response, HttpServletRequest request) {
        final TournamentDTO tournament = tournamentController.get(tournamentId);

        try {
            final byte[] bytes = pdfController.generateTournamentQr(locale, tournament, null).generate();
            final ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                    .filename(tournament.getName() + " - qr.pdf").build();
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
            return bytes;
        } catch (InvalidXmlElementException | EmptyPdfBodyException e) {
            RestServerLogger.errorMessage(this.getClass(), e);
            throw new BadRequestException(this.getClass(), e.getMessage());
        }
    }


    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Generates a QR code with the credentials to access as a guest for a tournament.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/guest/tournament/{tournamentId}/pdf/port/{port}", produces = {MediaType.APPLICATION_PDF_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public byte[] getParticipantDiplomaFromTournamentAsPdf(@Parameter(description = "Id of an existing tournament", required = true)
                                                           @PathVariable("tournamentId") Integer tournamentId,
                                                           @Parameter(description = "Frontend port")
                                                           @PathVariable("port") Integer port,
                                                           Locale locale, HttpServletResponse response, HttpServletRequest request) {
        final TournamentDTO tournament = tournamentController.get(tournamentId);

        try {
            final byte[] bytes = pdfController.generateTournamentQr(locale, tournament, null).generate();
            final ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                    .filename(tournament.getName() + " - qr.pdf").build();
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
            return bytes;
        } catch (InvalidXmlElementException | EmptyPdfBodyException e) {
            RestServerLogger.errorMessage(this.getClass(), e);
            throw new BadRequestException(this.getClass(), e.getMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority(@securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Generates a QR code with the credentials to access as a guest for a tournament.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/participant/{participantId}/statistics", produces = MediaType.APPLICATION_JSON_VALUE)
    public QrCodeDTO generateParticipantQrCodeForStatistics(@Parameter(description = "Id of an existing participant", required = true)
                                                            @PathVariable("participantId") Integer participantId,
                                                            @RequestParam(name = "nightMode", required = false) Optional<Boolean> nightMode,
                                                            HttpServletResponse response, HttpServletRequest request) {
        return qrController.generateParticipantQrCodeForStatistics(participantId, null, nightMode.orElse(false));
    }


    @PreAuthorize("hasAnyAuthority(@securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Generates a QR code with the credentials to access as a guest for a tournament.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/participant/{participantId}/statistics/port/{port}", produces = MediaType.APPLICATION_JSON_VALUE)
    public QrCodeDTO generateParticipantQrCodeForStatisticsWithPort(@Parameter(description = "Id of an existing participant", required = true)
                                                                    @PathVariable("participantId") Integer participantId,
                                                                    @Parameter(description = "Frontend port")
                                                                    @PathVariable("port") Integer port,
                                                                    @RequestParam(name = "nightMode", required = false) Optional<Boolean> nightMode,
                                                                    HttpServletResponse response, HttpServletRequest request) {
        return qrController.generateParticipantQrCodeForStatistics(participantId, port, nightMode.orElse(false));
    }


    @PreAuthorize("hasAnyAuthority(@securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Generates a QR code with the content.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "", produces = MediaType.IMAGE_PNG_VALUE, consumes = MediaType.TEXT_PLAIN_VALUE)
    public byte[] generateQrForAttendanceImage(@NotBlank @RequestBody String content,
                                               @RequestParam(name = "nightMode", required = false) Optional<Boolean> nightMode,
                                               HttpServletResponse response, HttpServletRequest request) {

        final ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename("QR.png").build();
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());

        return qrController.generateQrCode(content, nightMode.orElse(false)).getData();
    }


    @PreAuthorize("hasAnyAuthority(@securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Generates a QR as SVG image with the content.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/svg", consumes = MediaType.TEXT_PLAIN_VALUE, produces = {MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public String generateQrForAttendanceSvg(@NotBlank @RequestBody String content,
                                             @RequestParam(name = "nightMode", required = false) Optional<Boolean> nightMode,
                                             HttpServletResponse response, HttpServletRequest request) {

        final ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename("QR.svg").build();
        response.setHeader(HttpHeaders.CONTENT_TYPE, "image/svg+xml");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());

        return new String(qrController.generateQrCodeAsSvg(content, nightMode.orElse(false)).getData(), StandardCharsets.UTF_8);
    }
}
