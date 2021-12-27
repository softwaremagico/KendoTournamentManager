package com.softwaremagico.kt.core.providers;

import com.softwaremagico.kt.persistence.entities.UserImage;
import com.softwaremagico.kt.persistence.entities.User;
import com.softwaremagico.kt.persistence.repositories.UserImageRepository;
import org.hibernate.type.ImageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Component
public class FileProvider {

    private final UserImageRepository photoRepository;

    @Autowired
    public FileProvider(UserImageRepository photoRepository) {
        this.photoRepository = photoRepository;
    }

    public void add(MultipartFile file, User user) throws IOException {
        final UserImage userImage = new UserImage();
        userImage.setUser(user);
        userImage.setData(file.getBytes());
        photoRepository.save(userImage);
    }

    public Optional<UserImage> get(ImageType imageType, User user) {
        return photoRepository.findByUser(user);
    }
}
