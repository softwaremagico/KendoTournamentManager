package com.softwaremagico.kt.rest.security;

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

import com.softwaremagico.kt.core.providers.TenantDataCleanupProvider;
import com.softwaremagico.kt.persistence.entities.AuthenticatedUser;
import com.softwaremagico.kt.persistence.entities.IAuthenticatedUser;
import com.softwaremagico.kt.persistence.entities.Tenant;
import com.softwaremagico.kt.persistence.entities.TenantContext;
import com.softwaremagico.kt.persistence.repositories.TenantRepository;
import com.softwaremagico.kt.rest.controllers.AuthenticatedUserController;
import com.softwaremagico.kt.rest.exceptions.InvalidRequestException;
import com.softwaremagico.kt.rest.security.dto.CreateTenantRequest;
import com.softwaremagico.kt.rest.security.dto.UpdateTenantRequest;
import com.softwaremagico.kt.security.AvailableRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping(value = "/auth")
public class TenantApi {
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticatedUserController authenticatedUserController;
    private final TenantRepository tenantRepository;
    private final TenantDataCleanupProvider tenantDataCleanupProvider;

    @Autowired
    public TenantApi(JwtTokenUtil jwtTokenUtil, AuthenticatedUserController authenticatedUserController,
                     TenantRepository tenantRepository, TenantDataCleanupProvider tenantDataCleanupProvider) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.authenticatedUserController = authenticatedUserController;
        this.tenantRepository = tenantRepository;
        this.tenantDataCleanupProvider = tenantDataCleanupProvider;
    }

    @Operation(summary = "Returns the active tenant only when this installation has exactly one.")
    @GetMapping(path = "/public/tenants", produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<String> getActiveTenantNames() {
        final List<Tenant> activeTenants = tenantRepository.findAllByActiveTrueOrderByNameAsc();
        return activeTenants.size() == 1 ? List.of(activeTenants.getFirst().getName()) : List.of();
    }

    @Transactional
    @PreAuthorize("hasAuthority(@securityService.superAdminPrivilege)")
    @Operation(summary = "Creates a private organization and its first administrator.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(path = "/tenants", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IAuthenticatedUser> createTenant(@Valid @RequestBody CreateTenantRequest request,
                                                             HttpServletRequest httpRequest) {
        if (tenantRepository.findByNameAndActiveTrue(request.getTenant()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        final Tenant tenant = tenantRepository.save(new Tenant(request.getTenant()));
        TenantContext.setTenantId(tenant.getId());
        try {
            final AuthenticatedUser user = authenticatedUserController.createUser(null, request.getUsername(),
                    request.getName() != null ? request.getName() : "", request.getLastname() != null ? request.getLastname() : "",
                    request.getPassword(), AvailableRole.ADMIN);
            final String jwtToken = jwtTokenUtil.generateAccessToken(user, getClientIP(httpRequest));
            return ResponseEntity.status(HttpStatus.CREATED)
                    .headers(getLoginHeaders(jwtToken, jwtTokenUtil.getJwtExpirationTime(), jwtTokenUtil.getSession(jwtToken)))
                    .body(user);
        } finally {
            TenantContext.clear();
        }
    }

    @PreAuthorize("hasAuthority(@securityService.superAdminPrivilege)")
    @Operation(summary = "Lists all tenants.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(path = "/tenants", produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<Tenant> getTenants() {
        return tenantRepository.findAll();
    }

    @Transactional
    @PreAuthorize("hasAuthority(@securityService.superAdminPrivilege)")
    @Operation(summary = "Updates tenant name or activation state.", security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping(path = "/tenants/{tenantId}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Tenant updateTenant(@PathVariable Integer tenantId, @Valid @RequestBody UpdateTenantRequest request) {
        final Tenant tenant = tenantRepository.findById(tenantId).orElseThrow(() ->
                new InvalidRequestException(this.getClass(), "Tenant not found."));
        tenant.setName(request.getName());
        tenant.setActive(request.getActive());
        return tenantRepository.save(tenant);
    }

    @PreAuthorize("hasAuthority(@securityService.superAdminPrivilege)")
    @Operation(summary = "Deletes all data (clubs, users, tournaments, fights, etc.) of a tenant, "
            + "preserving the tenant registration.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/tenants/{tenantId}/data")
    public void deleteTenantData(@Parameter(description = "Identifier of the tenant whose data must be removed",
            required = true) @PathVariable Integer tenantId) {
        try {
            tenantDataCleanupProvider.deleteAllData(tenantId);
        } catch (IllegalArgumentException ex) {
            throw new InvalidRequestException(this.getClass(), ex.getMessage());
        }
    }

    private String getClientIP(HttpServletRequest httpRequest) {
        final String xfHeader = httpRequest.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return httpRequest.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    private HttpHeaders getLoginHeaders(String jwtToken, long expirationTime, String session) {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, jwtToken);
        headers.add(HttpHeaders.EXPIRES, String.valueOf(expirationTime));
        headers.add(AuthApi.SESSION_HEADER, session);
        headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, AuthApi.SESSION_HEADER);
        return headers;
    }
}
