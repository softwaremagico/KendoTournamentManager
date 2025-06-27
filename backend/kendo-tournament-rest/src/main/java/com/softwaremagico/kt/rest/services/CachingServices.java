package com.softwaremagico.kt.rest.services;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
 * %%
 * Copyright (C) 2021 - 2024 Softwaremagico
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

import com.softwaremagico.kt.core.controller.CacheController;
import com.softwaremagico.kt.core.exceptions.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cache")
public class CachingServices {


    private final CacheController cacheController;

    public CachingServices(@Autowired(required = false) CacheController cacheController) {
        this.cacheController = cacheController;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Operation(summary = "Clears all cache areas from the server.", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping(value = "")
    @ResponseStatus(HttpStatus.OK)
    public void deleteAllCache(HttpServletResponse response, HttpServletRequest httpRequest) {
        if (cacheController != null) {
            cacheController.deleteAllCache();
        } else {
            throw new NotFoundException(this.getClass(),
                    "Bean 'CacheController' is not set! Do you have forgotten to include the bean on the @ComponentScan?'");
        }
    }
}
