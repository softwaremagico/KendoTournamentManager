package com.softwaremagico.kt.rest;

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
import java.util.List;

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
    @ApiOperation(value = "Gets all users.")
    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<User> getAll(HttpServletRequest request) {
        return userProvider.getAll();
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @ApiOperation(value = "Gets a user.")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
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
    @ApiOperation(value = "Deletes a user.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody UserDto user, HttpServletRequest request) {
        userProvider.delete(modelMapper.map(user, User.class));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Updates a user.")
    @PutMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public User update(@RequestBody UserDto user, HttpServletRequest request) {
        return userProvider.update(modelMapper.map(user, User.class));
    }
}
