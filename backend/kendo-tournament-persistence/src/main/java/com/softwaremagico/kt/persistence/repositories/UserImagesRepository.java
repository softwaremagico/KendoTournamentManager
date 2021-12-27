package com.softwaremagico.kt.persistence.repositories;

import com.softwaremagico.kt.persistence.entities.ImageType;
import com.softwaremagico.kt.persistence.entities.User;
import com.softwaremagico.kt.persistence.entities.UserImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserImagesRepository extends JpaRepository<UserImage, Integer> {

    UserImage findByUserAndType(User user, ImageType type);

}
