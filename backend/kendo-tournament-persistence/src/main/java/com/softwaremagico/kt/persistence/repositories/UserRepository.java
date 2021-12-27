package com.softwaremagico.kt.persistence.repositories;

import com.softwaremagico.kt.persistence.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {


}
