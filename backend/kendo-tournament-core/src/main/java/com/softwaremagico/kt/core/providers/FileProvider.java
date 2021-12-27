package com.softwaremagico.kt.core.providers;

import com.softwaremagico.kt.persistence.entities.ImageType;
import com.softwaremagico.kt.persistence.entities.User;
import com.softwaremagico.kt.persistence.entities.UserImage;
import com.softwaremagico.kt.persistence.repositories.UserImagesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class FileProvider {

    private final UserImagesRepository userImagesRepository;

    @Autowired
    public FileProvider(UserImagesRepository userImagesRepository) {
        this.userImagesRepository = userImagesRepository;
    }

    public void add(MultipartFile file, ImageType imageType, User user) throws IOException {
        final UserImage userImage = new UserImage();
        userImage.setType(imageType);
        userImage.setUser(user);
        userImage.setData(file.getBytes());

        userImagesRepository.save(userImage);
    }

    public UserImage get(ImageType imageType, User user) {
        return userImagesRepository.findByUserAndType(user, imageType);
    }
}
