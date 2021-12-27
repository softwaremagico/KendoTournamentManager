package com.softwaremagico.kt.persistence.repositories;

import com.softwaremagico.kt.persistence.entities.AuthenticatedUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthenticatedUserRepository extends JpaRepository<AuthenticatedUser, Integer> {

    Optional<AuthenticatedUser> findByUsername(String username);
}
