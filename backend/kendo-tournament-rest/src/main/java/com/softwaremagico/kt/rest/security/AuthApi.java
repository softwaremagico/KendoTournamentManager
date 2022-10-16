package com.softwaremagico.kt.rest.security;

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

import com.softwaremagico.kt.logger.RestServerLogger;
import com.softwaremagico.kt.persistence.entities.AuthenticatedUser;
import com.softwaremagico.kt.rest.controllers.AuthenticatedUserController;
import com.softwaremagico.kt.rest.exceptions.InvalidRequestException;
import com.softwaremagico.kt.rest.exceptions.UserBlockedException;
import com.softwaremagico.kt.rest.security.dto.AuthRequest;
import com.softwaremagico.kt.rest.security.dto.CreateUserRequest;
import com.softwaremagico.kt.rest.security.dto.UpdatePasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Objects;
import java.util.Random;

@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class AuthApi {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticatedUserController authenticatedUserController;
    private final BruteForceService bruteForceService;

    private final Random random = new Random();

    @Autowired
    public AuthApi(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil,
                   AuthenticatedUserController authenticatedUserController, BruteForceService bruteForceService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.authenticatedUserController = authenticatedUserController;
        this.bruteForceService = bruteForceService;
    }


    @Operation(summary = "Gets the JWT Token into the headers.")
    @PostMapping(path = "/public/login")
    public ResponseEntity<AuthenticatedUser> login(@RequestBody AuthRequest request, HttpServletRequest httpRequest) {
        final String ip = getClientIP(httpRequest);
        try {
            //Check if the IP is blocked.
            if (bruteForceService.isBlocked(ip)) {
                Thread.sleep(random.nextInt(10) * 1000L);
                RestServerLogger.warning(this.getClass().getName(), "Too many attempts from IP '" + ip + "'.");
                throw new UserBlockedException(this.getClass(), "Too many attempts from IP '" + ip + "'.");

            }
            //We verify the provided credentials using the authentication manager
            final Authentication authenticate = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            final AuthenticatedUser user = (AuthenticatedUser) authenticate.getPrincipal();
            bruteForceService.loginSucceeded(ip);

            //We generate the JWT token and return it as a response header along with the user identity information in the response body.
            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, jwtTokenUtil.generateAccessToken(user, ip))
                    .body(user);
        } catch (BadCredentialsException ex) {
            RestServerLogger.warning(this.getClass().getName(), "Invalid credentials set from IP '" + ip + "'!");
            bruteForceService.loginFailed(ip);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (InterruptedException e) {
            RestServerLogger.warning(this.getClass().getName(), "Too many attempts from IP '" + ip + "'.");
            try {
                throw new UserBlockedException(this.getClass(), "Too many attempts from IP '" + ip + "'.");
            } finally {
                Thread.currentThread().interrupt();
            }
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Gets all users.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(path = "/register")
    public Collection<AuthenticatedUser> getAll(HttpServletRequest httpRequest) {
        return authenticatedUserController.findAll();
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Registers a user.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(path = "/register")
    public AuthenticatedUser register(@RequestBody CreateUserRequest request, Authentication authentication, HttpServletRequest httpRequest) {
        return authenticatedUserController.createUser(authentication.getName(), request);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Updates a user.", security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping(path = "/register")
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

    @Operation(summary = "Updates a password.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(path = "/password")
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public void updatePassword(@RequestBody UpdatePasswordRequest request, Authentication authentication, HttpServletRequest httpRequest)
            throws InterruptedException {
        Thread.sleep(random.nextInt(10) * 1000L);
        authenticatedUserController.updatePassword(authentication.getName(), request.getOldPassword(), request.getNewPassword());
    }


}
