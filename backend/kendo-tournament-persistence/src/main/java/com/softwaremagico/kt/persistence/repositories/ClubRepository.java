package com.softwaremagico.kt.persistence.repositories;

import com.softwaremagico.kt.persistence.entities.Club;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubRepository extends JpaRepository<Club, Integer> {
}
