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

import com.softwaremagico.kt.core.controller.ParticipantImageController;
import com.softwaremagico.kt.core.controller.models.ParticipantImageDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/files")
public class FileServices {

    private final ParticipantImageController participantImageController;

    @Autowired
    public FileServices(ParticipantImageController participantImageController) {
        this.participantImageController = participantImageController;
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Uploads a photo to a participant profile", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/participants", produces = MediaType.APPLICATION_JSON_VALUE)
    public void upload(@RequestParam("file") MultipartFile file,
                       @RequestParam("participant") int participantId, HttpServletRequest request) {
        participantImageController.add(file, participantId);

    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets an image from a participant", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/participants", produces = MediaType.APPLICATION_JSON_VALUE)
    public ParticipantImageDTO getParticipantImage(@RequestParam("participant") int participantId, HttpServletRequest request) {
        // return fileProvider.get(type, participantProvider.get(participantId));
        return null;
    }
}
