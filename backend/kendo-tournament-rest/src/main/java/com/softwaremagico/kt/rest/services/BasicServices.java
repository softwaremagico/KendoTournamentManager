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

import com.softwaremagico.kt.core.controller.BasicInsertableController;
import com.softwaremagico.kt.core.controller.models.ElementDTO;
import com.softwaremagico.kt.core.converters.ElementConverter;
import com.softwaremagico.kt.core.converters.models.ConverterRequest;
import com.softwaremagico.kt.core.providers.CrudProvider;
import com.softwaremagico.kt.rest.exceptions.BadRequestException;
import com.softwaremagico.kt.rest.security.AuthApi;
import com.softwaremagico.kt.rest.security.KendoSecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Collection;
import java.util.List;

/**
 * Abstract generic REST controller that provides standard CRUD HTTP endpoints for
 * any domain entity managed by a {@link BasicInsertableController}.
 * <p>
 * Concrete subclasses bind the seven generic type parameters to a specific domain
 * aggregate and inherit the following mapped endpoints:
 * <ul>
 *   <li>{@code GET /} — list all entities (VIEWER+)</li>
 *   <li>{@code GET /{id}} — get by ID (role configurable via {@link #requiredRoleForEntityById})</li>
 *   <li>{@code POST /} — create (EDITOR+)</li>
 *   <li>{@code PUT /} — update (EDITOR+)</li>
 *   <li>{@code DELETE /{id}} — delete by ID (ADMIN)</li>
 *   <li>{@code DELETE /delete} — delete by entity body (ADMIN)</li>
 * </ul>
 * </p>
 * <p>
 * Access control is enforced via {@link PreAuthorize} expressions that reference
 * privilege names resolved from {@link KendoSecurityService}. Subclasses may override
 * {@link #requiredRoleForEntityById()} to grant wider access to the {@code GET /{id}}
 * endpoint (e.g. GUEST access in {@link TournamentServices}).
 * </p>
 *
 * @param <ENTITY>            the JPA entity type
 * @param <DTO>               the DTO type exposed via the REST API
 * @param <REPOSITORY>        the JPA repository for the entity
 * @param <PROVIDER>          the CRUD provider delegating to the repository
 * @param <CONVERTER_REQUEST> the converter request wrapper
 * @param <CONVERTER>         the entity↔DTO converter
 * @param <CONTROLLER>        the business-logic controller
 */
@Validated
public abstract class BasicServices<ENTITY, DTO extends ElementDTO, REPOSITORY extends JpaRepository<ENTITY, Integer>,
        PROVIDER extends CrudProvider<ENTITY, Integer, REPOSITORY>, CONVERTER_REQUEST extends ConverterRequest<ENTITY>,
        CONVERTER extends ElementConverter<ENTITY, DTO, CONVERTER_REQUEST>,
        CONTROLLER extends BasicInsertableController<ENTITY, DTO, REPOSITORY, PROVIDER, CONVERTER_REQUEST, CONVERTER>> {
    private final CONTROLLER controller;

    private final KendoSecurityService kendoSecurityService;

    protected BasicServices(CONTROLLER controller, KendoSecurityService kendoSecurityService) {
        this.controller = controller;
        this.kendoSecurityService = kendoSecurityService;
    }

    protected CONTROLLER getController() {
        return controller;
    }

    /**
     * This method is done due to @PreAuthorize cannot be overridden. TournamentService need to set a GUEST permission to it.
     *
     * @return an array of roles.
     */
    public String[] requiredRoleForEntityById() {
        return new String[]{kendoSecurityService.getViewerPrivilege(), kendoSecurityService.getEditorPrivilege(), kendoSecurityService.getAdminPrivilege()};
    }

    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Gets all", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    /**
     * Returns all entities of this resource type.
     * <p>Requires at least VIEWER role.</p>
     *
     * @param request the HTTP request (used for audit logging)
     * @return list of all entities as DTOs
     */
    public List<DTO> getAll(HttpServletRequest request) {
        return controller.get();
    }


    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Gets all", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/ids", produces = MediaType.APPLICATION_JSON_VALUE)
    /**
     * Returns all entities whose IDs are contained in the provided collection.
     * <p>Requires at least VIEWER role.</p>
     *
     * @param ids     the set of entity IDs to retrieve
     * @param request the HTTP request (used for audit logging)
     * @return list of matching entities as DTOs
     */
    public List<DTO> getAll(@RequestBody Collection<Integer> ids, HttpServletRequest request) {
        return controller.get(ids);
    }


    @PreAuthorize("hasAnyAuthority(@securityService.guestPrivilege, @securityService.viewerPrivilege, @securityService.editorPrivilege, "
            + "@securityService.adminPrivilege)")
    @Operation(summary = "Counts all entities.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
    /**
     * Returns the total number of persisted entities.
     * <p>Requires at least GUEST role.</p>
     *
     * @param request the HTTP request (used for audit logging)
     * @return total entity count
     */
    public long count(HttpServletRequest request) {
        return controller.count();
    }


    @PreAuthorize("hasAnyAuthority(#root.this.requiredRoleForEntityById())")
    @Operation(summary = "Gets an entity.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    /**
     * Returns the entity with the given primary key.
     * <p>Required role is determined by {@link #requiredRoleForEntityById()}.</p>
     *
     * @param id      the primary key of the entity to retrieve
     * @param request the HTTP request (used for audit logging)
     * @return the entity as a DTO
     */
    public DTO get(@Parameter(description = "Id of an existing entity", required = true) @PathVariable("id") Integer id,
                   HttpServletRequest request) {
        return controller.get(id);
    }


    @PreAuthorize("hasAnyAuthority(@securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Creates an entity.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    /**
     * Creates a single entity and returns it with HTTP 201.
     * <p>Requires at least EDITOR role.</p>
     *
     * @param dto            the entity data to create
     * @param authentication the Spring Security authentication context
     * @param session        the client session identifier for WebSocket notifications
     * @param request        the HTTP request (used for audit logging)
     * @return the persisted entity as a DTO
     */
    public DTO add(@Valid @RequestBody DTO dto, Authentication authentication,
                   @RequestHeader(value = AuthApi.SESSION_HEADER, required = false) String session,
                   HttpServletRequest request) {
        return controller.create(dto, authentication.getName(), session);
    }


    @PreAuthorize("hasAnyAuthority(@securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Creates a set of entities.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    /**
     * Creates a collection of entities and returns them with HTTP 201.
     * <p>Requires at least EDITOR role.</p>
     *
     * @param dtos           the entities to create
     * @param authentication the Spring Security authentication context
     * @param session        the client session identifier for WebSocket notifications
     * @param request        the HTTP request (used for audit logging)
     * @return the persisted entities as a list of DTOs
     */
    public List<DTO> add(@Valid @RequestBody Collection<DTO> dtos, Authentication authentication,
                         @RequestHeader(value = AuthApi.SESSION_HEADER, required = false) String session,
                         HttpServletRequest request) {
        if (dtos == null || dtos.isEmpty()) {
            throw new BadRequestException(getClass(), "Data is missing");
        }
        return getController().create(dtos, authentication.getName(), session);
    }


    @PreAuthorize("hasAnyAuthority(@securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Deletes an entity.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    /**
     * Deletes the entity identified by the given primary key, returning HTTP 204.
     * <p>Requires at least EDITOR role.</p>
     *
     * @param id             the primary key of the entity to delete
     * @param authentication the Spring Security authentication context
     * @param session        the client session identifier for WebSocket notifications
     * @param request        the HTTP request (used for audit logging)
     */
    public void delete(@Parameter(description = "Id of an existing entity", required = true) @PathVariable("id") Integer id,
                       Authentication authentication,
                       @RequestHeader(value = AuthApi.SESSION_HEADER, required = false) String session,
                       HttpServletRequest request) {
        controller.deleteById(id, authentication.getName(), session);
    }


    @PreAuthorize("hasAnyAuthority(@securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Deletes an entity.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    /**
     * Deletes the entity represented by the given DTO, returning HTTP 204.
     * <p>Requires at least EDITOR role.</p>
     *
     * @param dto            the entity to delete
     * @param session        the client session identifier for WebSocket notifications
     * @param authentication the Spring Security authentication context
     * @param request        the HTTP request (used for audit logging)
     */
    public void delete(@RequestBody DTO dto,
                       @RequestHeader(value = AuthApi.SESSION_HEADER, required = false) String session,
                       Authentication authentication, HttpServletRequest request) {
        controller.delete(dto, authentication.getName(), session);
    }


    @PreAuthorize("hasAnyAuthority(@securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Deletes a collection of entities.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/delete/list", produces = MediaType.APPLICATION_JSON_VALUE)
    /**
     * Deletes a collection of entities.
     * <p>Requires at least EDITOR role.</p>
     *
     * @param dtos           the entities to delete
     * @param authentication the Spring Security authentication context
     * @param session        the client session identifier for WebSocket notifications
     * @param request        the HTTP request (used for audit logging)
     */
    public void delete(@RequestBody Collection<DTO> dtos, Authentication authentication,
                       @RequestHeader(value = AuthApi.SESSION_HEADER, required = false) String session,
                       HttpServletRequest request) {
        getController().delete(dtos, authentication.getName(), session);
    }

    @PreAuthorize("hasAnyAuthority(@securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Updates a entity.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    /**
     * Updates a single entity.
     * <p>Requires at least EDITOR role.</p>
     *
     * @param dto            the entity data to update
     * @param authentication the Spring Security authentication context
     * @param session        the client session identifier for WebSocket notifications
     * @param request        the HTTP request (used for audit logging)
     * @return the updated entity as a DTO
     */
    public DTO update(@Valid @RequestBody DTO dto, Authentication authentication,
                      @RequestHeader(value = AuthApi.SESSION_HEADER, required = false) String session,
                      HttpServletRequest request) {
        return controller.update(dto, authentication.getName(), session);
    }


    @PreAuthorize("hasAnyAuthority(@securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Updates a list of fights.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    /**
     * Updates a list of entities.
     * <p>Requires at least EDITOR role.</p>
     *
     * @param dtos           the list of entities to update
     * @param authentication the Spring Security authentication context
     * @param session        the client session identifier for WebSocket notifications
     * @param request        the HTTP request (used for audit logging)
     * @return the updated entities as a list of DTOs
     */
    public List<DTO> update(@Valid @RequestBody List<DTO> dtos, Authentication authentication,
                            @RequestHeader(value = AuthApi.SESSION_HEADER, required = false) String session,
                            HttpServletRequest request) {
        if (dtos == null) {
            throw new BadRequestException(getClass(), "Data is missing");
        }
        return getController().updateAll(dtos, authentication.getName(), session);
    }

}
