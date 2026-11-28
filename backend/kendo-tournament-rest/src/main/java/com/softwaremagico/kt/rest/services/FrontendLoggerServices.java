package com.softwaremagico.kt.rest.services;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
 * %%
 * Copyright (C) 2021 - 2026 Softwaremagico
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

import com.softwaremagico.kt.core.controller.models.LogDTO;
import com.softwaremagico.kt.logger.FrontendLogger;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Front-end logging endpoint. It is intentionally public because credential-free
 * pages (public registration) report their errors through it.
 */
@RestController
@RequestMapping("/logger")
public class FrontendLoggerServices {

    private static String sanitize(Object parameter) {
        return parameter.toString().replaceAll("[\n\r\t]", "_");
    }

    @Operation(summary = "Register an action that must be logged.")
    @PostMapping(value = "/info")
    @ResponseStatus(HttpStatus.OK)
    public void info(@RequestBody LogDTO log, HttpServletRequest request) {
        FrontendLogger.info(this.getClass(), sanitize(log.getMessage()));
    }

    @Operation(summary = "Register a warning that must be logged.")
    @PostMapping(value = "/warning")
    @ResponseStatus(HttpStatus.OK)
    public void warning(@RequestBody LogDTO log, HttpServletRequest request) {
        FrontendLogger.warning(this.getClass(), sanitize(log.getMessage()));
    }

    @Operation(summary = "Register an error that must be logged.")
    @PostMapping(value = "/error")
    @ResponseStatus(HttpStatus.OK)
    public void error(@RequestBody LogDTO log, HttpServletRequest request) {
        FrontendLogger.severe(this.getClass(), sanitize(log.getMessage()));
    }
}
