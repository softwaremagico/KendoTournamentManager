package com.softwaremagico.kt.core.providers;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2022 Softwaremagico
 * %%
 * This software is designed by Jorge Hortelano Otero. Jorge Hortelano Otero
 * <softwaremagico@gmail.com> Valencia (Spain).
 *  
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *  
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.softwaremagico.kt.core.exceptions.ClubNotFoundException;
import com.softwaremagico.kt.persistence.entities.Club;
import com.softwaremagico.kt.persistence.repositories.ClubRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClubProvider {
    private final ClubRepository clubRepository;

    public ClubProvider(ClubRepository clubRepository) {
        this.clubRepository = clubRepository;
    }

    public Club get(Integer id) {
        return clubRepository.findById(id)
                .orElseThrow(() -> new ClubNotFoundException(getClass(), "Club with id '" + id + "' not found"));
    }

    public List<Club> getAll() {
        return clubRepository.findAll();
    }

    public Club add(Club club) {
        return clubRepository.save(club);
    }

    public Club add(String name, String country, String city) {
        final Club club = new Club(name, country, city);
        return clubRepository.save(club);
    }

    public Club update(Club club) {
        if (club.getId() == null) {
            throw new ClubNotFoundException(getClass(), "Club with null id does not exists.");
        }
        return clubRepository.save(club);
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
