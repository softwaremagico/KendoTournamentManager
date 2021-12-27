package com.softwaremagico.kt.persistence.repositories;

import com.softwaremagico.kt.persistence.entities.UserImage;
import com.softwaremagico.kt.persistence.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserImageRepository extends JpaRepository<UserImage, Integer> {

    Optional<UserImage> findByUser(User user);

}
