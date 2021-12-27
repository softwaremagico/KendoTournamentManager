package com.softwaremagico.kt.rest;

import com.softwaremagico.kt.core.exceptions.DataInputException;
import com.softwaremagico.kt.persistence.entities.ImageType;
import com.softwaremagico.kt.persistence.entities.UserImage;
import com.softwaremagico.kt.core.providers.FileProvider;
import com.softwaremagico.kt.core.providers.UserProvider;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequestMapping("/files")
public class FileServices {

    private final FileProvider fileProvider;
    private final UserProvider userProvider;

    @Autowired
    public FileServices(FileProvider fileProvider, UserProvider userProvider) {
        this.fileProvider = fileProvider;
        this.userProvider = userProvider;
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @ApiOperation(value = "Uploads a photo to a user profile")
    @PostMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public void upload(@RequestParam("file") MultipartFile file, @RequestParam("type") ImageType type,
                       @RequestParam("user") int userId, HttpServletRequest request) {
        try {
            fileProvider.add(file, type, userProvider.get(userId));
        } catch (IOException e) {
            throw new DataInputException(this.getClass(), "File creation failed.");
        }
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @ApiOperation(value = "Gets an image from a user")
    @GetMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserImage getUserImage(@RequestParam("type") ImageType type, @RequestParam("user") int userId, HttpServletRequest request) {
       // return fileProvider.get(type, userProvider.get(userId));
        return null;
    }
}
