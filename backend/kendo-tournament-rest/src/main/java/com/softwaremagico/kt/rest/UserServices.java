package com.softwaremagico.kt.rest;

import com.softwaremagico.kt.core.providers.UserProvider;
import com.softwaremagico.kt.persistence.entities.User;
import com.softwaremagico.kt.rest.model.UserDto;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;

@RestController
@RequestMapping("/users")
public class UserServices {
    private final UserProvider userProvider;
    private final ModelMapper modelMapper;

    public UserServices(UserProvider userProvider, ModelMapper modelMapper) {
        this.userProvider = userProvider;
        this.modelMapper = modelMapper;
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @ApiOperation(value = "Gets a user.")
    @PostMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public User get(@ApiParam(value = "Id of an existing user", required = true) @PathParam("id") Integer id,
                    HttpServletRequest request) {
        return userProvider.get(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Creates a user.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public User add(@RequestBody UserDto user, HttpServletRequest request) {
        return userProvider.add(modelMapper.map(user, User.class));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Deletes a user.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@ApiParam(value = "Id of an existing user", required = true) @PathParam("id") Integer id,
                       HttpServletRequest request) {
        userProvider.delete(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Updates a user.")
    @PutMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public User update(@RequestBody UserDto user, HttpServletRequest request) {
        return userProvider.update(modelMapper.map(user, User.class));
    }
}
