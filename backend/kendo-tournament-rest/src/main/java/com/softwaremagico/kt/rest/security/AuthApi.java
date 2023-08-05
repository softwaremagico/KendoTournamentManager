package com.softwaremagico.kt.rest.security;

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

import com.softwaremagico.kt.logger.JwtFilterLogger;
import com.softwaremagico.kt.logger.KendoTournamentLogger;
import com.softwaremagico.kt.logger.RestServerLogger;
import com.softwaremagico.kt.persistence.entities.AuthenticatedUser;
import com.softwaremagico.kt.persistence.repositories.AuthenticatedUserRepository;
import com.softwaremagico.kt.rest.controllers.AuthenticatedUserController;
import com.softwaremagico.kt.rest.exceptions.InvalidRequestException;
import com.softwaremagico.kt.rest.security.dto.AuthRequest;
import com.softwaremagico.kt.rest.security.dto.CreateUserRequest;
import com.softwaremagico.kt.rest.security.dto.UpdatePasswordRequest;
import com.softwaremagico.kt.security.AvailableRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

@RestController
@RequestMapping(value = "/auth")
public class AuthApi {
    private static final int MAX_WAITING_SECONDS = 10;
    private static final long MILLIS = 1000L;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticatedUserController authenticatedUserController;
    private final BruteForceService bruteForceService;
    private final AuthenticatedUserRepository authenticatedUserRepository;

    private final Random random = new Random();

    @Autowired
    public AuthApi(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil,
                   AuthenticatedUserController authenticatedUserController, BruteForceService bruteForceService,
                   AuthenticatedUserRepository authenticatedUserRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.authenticatedUserController = authenticatedUserController;
        this.bruteForceService = bruteForceService;
        this.authenticatedUserRepository = authenticatedUserRepository;
    }


    @Operation(summary = "Gets the JWT Token into the headers.")
    @PostMapping(path = "/public/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthenticatedUser> login(@RequestBody AuthRequest request, HttpServletRequest httpRequest) {
        final String ip = getClientIP(httpRequest);
        try {
            //Check if the IP is blocked.
            if (bruteForceService.isBlocked(ip)) {
                try {
                    Thread.sleep(random.nextInt(MAX_WAITING_SECONDS) * MILLIS);
                    RestServerLogger.warning(this.getClass().getName(), "Too many attempts from IP '" + ip + "'.");
                    final HttpHeaders headers = new HttpHeaders();
                    headers.add(HttpHeaders.RETRY_AFTER, String.valueOf(bruteForceService.getElementsTime(ip)
                            + bruteForceService.getExpirationTime()));
                    return new ResponseEntity<>(headers, HttpStatus.LOCKED);
                } catch (InterruptedException e) {
                    RestServerLogger.warning(this.getClass().getName(), "Too many attempts from IP '" + ip + "'.");
                    try {
                        final HttpHeaders headers = new HttpHeaders();
                        headers.add(HttpHeaders.RETRY_AFTER, String.valueOf(bruteForceService.getElementsTime(ip)
                                + bruteForceService.getExpirationTime()));
                        return new ResponseEntity<>(headers, HttpStatus.LOCKED);
                    } finally {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            //We verify the provided credentials using the authentication manager
            RestServerLogger.debug(this.getClass().getName(), "Trying to log in with '" + request.getUsername() + "'.");
            final Authentication authenticate = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            RestServerLogger.debug(this.getClass().getName(), "User '" + request.getUsername().replaceAll("[\n\r\t]", "_") + "' authenticated.");

            try {
                final AuthenticatedUser user = authenticatedUserRepository.findByUsername(authenticate.getName()).orElseThrow(() ->
                        new UsernameNotFoundException(String.format("User '%s' not found!", authenticate.getName())));
                final long jwtExpiration = jwtTokenUtil.getJwtExpirationTime();
                final String jwtToken = jwtTokenUtil.generateAccessToken(user, ip);
                user.setPassword(jwtToken);
                bruteForceService.loginSucceeded(ip);

                //We generate the JWT token and return it as a response header along with the user identity information in the response body.
                return ResponseEntity.ok()
                        .header(HttpHeaders.AUTHORIZATION, jwtToken)
                        .header(HttpHeaders.EXPIRES, String.valueOf(jwtExpiration))
                        .body(user);
            } catch (UsernameNotFoundException e) {
                RestServerLogger.warning(this.getClass().getName(), "Bad credentials!.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (BadCredentialsException ex) {
            RestServerLogger.warning(this.getClass().getName(), "Invalid credentials set from IP '" + ip + "'!");
            //Create a default user if no user exists. Needed when database is encrypted.
            if (authenticatedUserController.countUsers() == 0) {
                RestServerLogger.info(this.getClass().getName(), "Creating default user '" + request.getUsername().replaceAll("[\n\r\t]", "_") + "'.");
                final AuthenticatedUser user = authenticatedUserController.createUser(
                        null, request.getUsername(), "Default", "Admin", request.getPassword(), AvailableRole.ROLE_ADMIN);
                final long jwtExpiration = jwtTokenUtil.getJwtExpirationTime();
                final String jwtToken = jwtTokenUtil.generateAccessToken(user, ip);
                user.setPassword(jwtToken);
                //We generate the JWT token and return it as a response header along with the user identity information in the response body.
                final HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.AUTHORIZATION, jwtToken);
                headers.add(HttpHeaders.EXPIRES, String.valueOf(jwtExpiration));
                return new ResponseEntity<>(user, headers, HttpStatus.CREATED);
            }
            bruteForceService.loginFailed(ip);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Gets all users.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(path = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<AuthenticatedUser> getAll(HttpServletRequest httpRequest) {
        return authenticatedUserController.findAll();
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Registers a user.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AuthenticatedUser register(@RequestBody CreateUserRequest request, Authentication authentication, HttpServletRequest httpRequest) {
        return authenticatedUserController.createUser(authentication.getName(), request);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Updates a user.", security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AuthenticatedUser update(@RequestBody CreateUserRequest request, Authentication authentication, HttpServletRequest httpRequest) {
        return authenticatedUserController.updateUser(authentication.getName(), request);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Deletes a user.", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping(path = "/register/{username}")
    public void delete(@Parameter(description = "Username of an existing user", required = true) @PathVariable("username") String username,
                       Authentication authentication, HttpServletRequest httpRequest) {
        if (Objects.equals(authentication.getName(), username)) {
            throw new InvalidRequestException(this.getClass(), "You cannot delete the current user!");
        }
        authenticatedUserController.deleteUser(authentication.getName(), username);
    }

    private String getClientIP(HttpServletRequest httpRequest) {
        final String xfHeader = httpRequest.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return httpRequest.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Updates a password.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(path = "/password", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public void updatePassword(@RequestBody UpdatePasswordRequest request, Authentication authentication, HttpServletRequest httpRequest)
            throws InterruptedException {
        Thread.sleep(random.nextInt(MAX_WAITING_SECONDS) * MILLIS);
        try {
            authenticatedUserController.updatePassword(authentication.getName(), request.getOldPassword(), request.getNewPassword());
        } catch (Exception e) {
            KendoTournamentLogger.errorMessage(this.getClass(), e);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Updates a password.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(path = "/{username}/password", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public void updateUserPassword(@Parameter(description = "username", required = true)
                                   @PathVariable("username") String username,
                                   @RequestBody UpdatePasswordRequest request, Authentication authentication, HttpServletRequest httpRequest)
            throws InterruptedException {
        Thread.sleep(random.nextInt(MAX_WAITING_SECONDS) * MILLIS);
        try {
            authenticatedUserController.updatePassword(username, request.getOldPassword(), request.getNewPassword());
        } catch (Exception e) {
            KendoTournamentLogger.errorMessage(this.getClass(), e);
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Get roles.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(path = "/roles", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public Set<String> getRoles(Authentication authentication, HttpServletRequest httpRequest) {
        return authenticatedUserController.getRoles(authentication.getName());
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Renew JWT Token.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(path = "/jwt/renew", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public ResponseEntity<Void> getNewJWT(Authentication authentication, HttpServletRequest httpRequest) {
        final AuthenticatedUser user = authenticatedUserRepository.findByUsername(authentication.getName()).orElseThrow(() ->
                new UsernameNotFoundException(String.format("User '%s' not found!", authentication.getName())));
        final String ip = getClientIP(httpRequest);
        final long jwtExpiration = jwtTokenUtil.getJwtExpirationTime();
        JwtFilterLogger.info(this.getClass(), "Renewing JWT token for '{}' expiring at '{}'.", authentication.getName(),
                new Date(jwtExpiration));
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, jwtTokenUtil.generateAccessToken(user, ip))
                .header(HttpHeaders.EXPIRES, String.valueOf(jwtExpiration))
                .build();
    }


}
