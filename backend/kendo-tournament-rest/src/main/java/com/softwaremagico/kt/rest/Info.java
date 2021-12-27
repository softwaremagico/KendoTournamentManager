package com.softwaremagico.kt.rest;

import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/info")
public class Info {

    @ApiOperation(value = "Basic method to check if the server is online.")
    @GetMapping(value = "/healthcheck")
    @ResponseStatus(HttpStatus.OK)
    public void healthCheck(HttpServletRequest httpRequest) {

    }

    @ApiIgnore
    @ApiOperation(value = "Redirects root address to API web site.")
    @GetMapping(value = "/")
    @ResponseStatus(HttpStatus.OK)
    public void root(HttpServletResponse response, HttpServletRequest httpRequest) throws IOException {
        response.sendRedirect("./swagger-ui/index.html");
    }
}
