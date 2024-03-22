package com.softwaremagico.kt.rest.services;

import com.softwaremagico.kt.core.controller.QrController;
import com.softwaremagico.kt.core.controller.models.QrCodeDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/qr")
public class QrService {

    private final QrController qrController;


    public QrService(QrController qrController) {
        this.qrController = qrController;
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Generates a QR code with the credentials to access as a guest for a tournament.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/guest/tournament/{tournamentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public QrCodeDTO generateGuestQrCodeForTournamentFights(@Parameter(description = "Id of an existing tournament", required = true)
                                                         @PathVariable("tournamentId") Integer tournamentId,
                                                            HttpServletResponse response, HttpServletRequest request) {

        final ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename("QR.png").build();
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());

        return qrController.generateGuestQrCodeForTournamentFights(tournamentId);
    }
}
