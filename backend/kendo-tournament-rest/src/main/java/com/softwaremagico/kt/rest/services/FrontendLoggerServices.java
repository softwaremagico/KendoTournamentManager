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

import com.softwaremagico.kt.logger.FrontendLogger;
import com.softwaremagico.kt.rest.model.LogDto;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/logger")
public class FrontendLoggerServices {

    @ApiOperation(value = "Register an action that must be logged.")
    @PostMapping(value = "/info")
    @ResponseStatus(HttpStatus.OK)
    public void info(@RequestBody LogDto log, HttpServletRequest request) {
        FrontendLogger.info(this.getClass(), log.getMessage());
    }

    @ApiOperation(value = "Register a warning that must be logged.")
    @PostMapping(value = "/warning")
    @ResponseStatus(HttpStatus.OK)
    public void warning(@RequestBody LogDto log, HttpServletRequest request) {
        FrontendLogger.warning(this.getClass(), log.getMessage());
    }

    @ApiOperation(value = "Register an error that must be logged.")
    @PostMapping(value = "/error")
    @ResponseStatus(HttpStatus.OK)
    public void error(@RequestBody LogDto log, HttpServletRequest request) {
        FrontendLogger.severe(this.getClass(), log.getMessage());
    }
}
