package com.softwaremagico.kt.rest.security;

import com.softwaremagico.kt.persistence.entities.AuthenticatedUser;
import com.softwaremagico.kt.rest.controllers.AuthenticatedUserController;
import com.softwaremagico.kt.rest.security.dto.AuthRequest;
import com.softwaremagico.kt.rest.security.dto.CreateUserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "api/public", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class AuthApi {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticatedUserController authenticatedUserController;

    @Autowired
    public AuthApi(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil, AuthenticatedUserController authenticatedUserController) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.authenticatedUserController = authenticatedUserController;
    }


    @PostMapping(path = "login")
    public ResponseEntity<AuthenticatedUser> login(@RequestBody AuthRequest request, HttpServletRequest httpRequest) {
        try {
            //We verify the provided credentials using the authentication manager
            final Authentication authenticate = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            final AuthenticatedUser user = (AuthenticatedUser) authenticate.getPrincipal();

            //We generate the JWT token and return it as a response header along with the user identity information in the response body.
            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, jwtTokenUtil.generateAccessToken(user))
                    .body(user);
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


    @PostMapping(path = "register")
    public AuthenticatedUser register(@RequestBody CreateUserRequest request) {
        return authenticatedUserController.createUser(request);
    }


}
