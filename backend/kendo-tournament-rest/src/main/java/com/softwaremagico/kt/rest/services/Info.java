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

import com.softwaremagico.kt.core.controller.VersionController;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;


@RestController
@RequestMapping("/info")
public class Info {

    private static final int CHECKING_VERSION_TIME_MINUTES = 30;

    private final VersionController versionController;
    private String latestVersion;
    private LocalDateTime checkedVersionAt;

    public Info(VersionController versionController) {
        this.versionController = versionController;
    }

    @Operation(summary = "Basic method to check if the server is online.")
    @GetMapping(value = "/health-check")
    @ResponseStatus(HttpStatus.OK)
    public void healthCheck(HttpServletRequest httpRequest) {
        //Not needed.
    }


    @Operation(summary = "Basic method that checks the latest deployed version of the software.")
    @GetMapping(value = "/latest-version", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getLatestVersion(HttpServletRequest httpRequest) {
        //To no request too much to github.
        if (checkedVersionAt != null && checkedVersionAt.isBefore(LocalDateTime.now().plusMinutes(CHECKING_VERSION_TIME_MINUTES))) {
            return ResponseEntity.ok().body(latestVersion);
        }
        try {
            latestVersion = versionController.getLatestVersionFromGithub();
            checkedVersionAt = LocalDateTime.now();
            return ResponseEntity.ok().body(latestVersion);
        } catch (Exception ex) {
            return ResponseEntity.ok().body("");
        }
    }
}
