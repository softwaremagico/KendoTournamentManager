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

import com.softwaremagico.kt.core.exceptions.DataInputException;
import com.softwaremagico.kt.core.providers.FileProvider;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.persistence.entities.ParticipantImage;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequestMapping("/files")
public class FileServices {

    private final FileProvider fileProvider;
    private final ParticipantProvider participantProvider;

    @Autowired
    public FileServices(FileProvider fileProvider, ParticipantProvider participantProvider) {
        this.fileProvider = fileProvider;
        this.participantProvider = participantProvider;
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @ApiOperation(value = "Uploads a photo to a participant profile")
    @PostMapping(value = "/participants", produces = MediaType.APPLICATION_JSON_VALUE)
    public void upload(@RequestParam("file") MultipartFile file,
                       @RequestParam("participant") int participantId, HttpServletRequest request) {
        try {
            fileProvider.add(file, participantProvider.get(participantId));
        } catch (IOException e) {
            throw new DataInputException(this.getClass(), "File creation failed.");
        }
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @ApiOperation(value = "Gets an image from a participant")
    @GetMapping(value = "/participants", produces = MediaType.APPLICATION_JSON_VALUE)
    public ParticipantImage getParticipantImage(@RequestParam("participant") int participantId, HttpServletRequest request) {
        // return fileProvider.get(type, participantProvider.get(participantId));
        return null;
    }
}
