package com.softwaremagico.kt.rest;

import com.softwaremagico.kt.core.providers.ClubProvider;
import com.softwaremagico.kt.persistence.entities.Club;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;

@RestController
@RequestMapping("/clubs")
public class ClubServices {
    private final ClubProvider clubProvider;
    private final ModelMapper modelMapper;

    public ClubServices(ClubProvider clubProvider, ModelMapper modelMapper) {
        this.clubProvider = clubProvider;
        this.modelMapper = modelMapper;
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @ApiOperation(value = "Creates a club with some basic information.")
    @PostMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Club get(@ApiParam(value = "Id of an existing club", required = true) @PathParam("id") Integer id,
                    HttpServletRequest request) {
        return clubProvider.get(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Creates a club with some basic information.")
    @PostMapping(value = "/basic", produces = MediaType.APPLICATION_JSON_VALUE)
    public void add(@ApiParam(value = "Name of the new club", required = true) @RequestParam(name = "name") String name,
                    @ApiParam(value = "Country where the club is located", required = true) @RequestParam(name = "country") String country,
                    @ApiParam(value = "City where the club is located", required = true) @RequestParam(name = "city") String city,
                    HttpServletRequest request) {
        clubProvider.add(name, country, city);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Creates a club with full information.")
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public void add(@RequestBody ClubDto club, HttpServletRequest request) {
        clubProvider.add(modelMapper.map(club, Club.class));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Deletes a club.")
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@ApiParam(value = "Id of an existing club", required = true) @PathParam("id") Integer id,
                       HttpServletRequest request) {
        clubProvider.delete(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Updates a club.")
    @PutMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public void update(@ApiParam(value = "Id of an existing club", required = true) @PathParam("id") Integer id,
                       HttpServletRequest request) {
        clubProvider.delete(id);
    }
}
