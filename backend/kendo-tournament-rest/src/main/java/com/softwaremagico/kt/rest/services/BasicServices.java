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

import com.softwaremagico.kt.core.controller.BasicInsertableController;
import com.softwaremagico.kt.core.controller.models.ElementDTO;
import com.softwaremagico.kt.core.converters.ElementConverter;
import com.softwaremagico.kt.core.converters.models.ConverterRequest;
import com.softwaremagico.kt.core.providers.CrudProvider;
import com.softwaremagico.kt.rest.exceptions.BadRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Collection;
import java.util.List;

public abstract class BasicServices<ENTITY, DTO extends ElementDTO, REPOSITORY extends JpaRepository<ENTITY, Integer>,
        PROVIDER extends CrudProvider<ENTITY, Integer, REPOSITORY>, CONVERTER_REQUEST extends ConverterRequest<ENTITY>,
        CONVERTER extends ElementConverter<ENTITY, DTO, CONVERTER_REQUEST>,
        CONTROLLER extends BasicInsertableController<ENTITY, DTO, REPOSITORY, PROVIDER, CONVERTER_REQUEST, CONVERTER>> {
    private final CONTROLLER controller;

    protected BasicServices(CONTROLLER controller) {
        this.controller = controller;
    }

    protected CONTROLLER getController() {
        return controller;
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Gets all", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DTO> getAll(HttpServletRequest request) {
        return controller.get();
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Counts all entities.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public long count(HttpServletRequest request) {
        return controller.count();
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Gets an entity.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public DTO get(@Parameter(description = "Id of an existing application", required = true) @PathVariable("id") Integer id,
                   HttpServletRequest request) {
        return controller.get(id);
    }

    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Creates an entity.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public DTO add(@RequestBody DTO dto, Authentication authentication, HttpServletRequest request) {
        return controller.create(dto, authentication.getName());
    }

    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Creates a set of entities.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DTO> add(@RequestBody Collection<DTO> dtos, Authentication authentication, HttpServletRequest request) {
        if (dtos == null || dtos.isEmpty()) {
            throw new BadRequestException(getClass(), "Data is missing");
        }
        return getController().create(dtos, authentication.getName());
    }

    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Deletes an entity.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@Parameter(description = "Id of an existing application", required = true) @PathVariable("id") Integer id,
                       Authentication authentication, HttpServletRequest request) {
        controller.deleteById(id, authentication.getName());
    }

    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Deletes an entity.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody DTO dto, Authentication authentication, HttpServletRequest request) {
        controller.delete(dto, authentication.getName());
    }

    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Deletes a collection of entities.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/delete/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody Collection<DTO> dtos, Authentication authentication, HttpServletRequest request) {
        getController().delete(dtos, authentication.getName());
    }

    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Updates a entity.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public DTO update(@RequestBody DTO dto, Authentication authentication, HttpServletRequest request) {
        return controller.update(dto, authentication.getName());
    }

    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Updates a list of fights.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DTO> update(@RequestBody List<DTO> dtos, Authentication authentication, HttpServletRequest request) {
        if (dtos == null) {
            throw new BadRequestException(getClass(), "Data is missing");
        }
        return getController().updateAll(dtos, authentication.getName());
    }
}
