package com.softwaremagico.kt.core.providers;

import com.softwaremagico.kt.core.exceptions.ClubNotFoundException;
import com.softwaremagico.kt.persistence.entities.Club;
import com.softwaremagico.kt.persistence.repositories.ClubRepository;
import org.springframework.stereotype.Service;

@Service
public class ClubProvider {
    private final ClubRepository clubRepository;

    public ClubProvider(ClubRepository clubRepository) {
        this.clubRepository = clubRepository;
    }

    public Club add(Club club) {
        return clubRepository.save(club);
    }

    public Club add(String name, String country, String city) {
        final Club club = new Club(name, country, city);
        return clubRepository.save(club);
    }

    public Club get(Integer id) {
        return clubRepository.findById(id)
                .orElseThrow(() -> new ClubNotFoundException(getClass(), "Club with id '" + id + "' not found"));
    }

    public void delete(Club club) {
        clubRepository.delete(club);
    }

    public void delete(Integer id) {
        if (clubRepository.existsById(id)) {
            clubRepository.deleteById(id);
        } else {
            throw new ClubNotFoundException(getClass(), "Club with id '" + id + "' not found");
        }
    }
}
