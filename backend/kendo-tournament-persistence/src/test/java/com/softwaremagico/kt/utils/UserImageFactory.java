package com.softwaremagico.kt.utils;

import com.softwaremagico.kt.persistence.entities.UserImage;
import com.softwaremagico.kt.persistence.entities.User;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class UserImageFactory {

    public UserImage createUserImage(String resource, User user) throws Exception {
        byte[] image = Files.readAllBytes(Paths.get(getClass().getClassLoader()
                .getResource(resource).toURI()));
        UserImage userImage = new UserImage();
        userImage.setUser(user);
        userImage.setData(image);

        return userImage;
    }
}
