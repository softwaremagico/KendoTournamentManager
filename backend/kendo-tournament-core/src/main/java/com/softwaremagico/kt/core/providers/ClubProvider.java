package com.softwaremagico.kt.core.providers;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2025 Softwaremagico
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.softwaremagico.kt.persistence.encryption.KeyProperty;
import com.softwaremagico.kt.persistence.entities.Club;
import com.softwaremagico.kt.persistence.repositories.ClubRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClubProvider extends CrudProvider<Club, Integer, ClubRepository> {

    @Autowired
    public ClubProvider(ClubRepository clubRepository) {
        super(clubRepository);
    }

    public Club add(String name, String country, String city) {
        return getRepository().save(new Club(name, country, city));
    }

    public Optional<Club> findBy(String name, String city) {
        //If encrypt is enabled.
        if (KeyProperty.getDatabaseEncryptionKey() != null && !KeyProperty.getDatabaseEncryptionKey().isBlank()) {
            final List<Club> clubs = getRepository().findAll();
            for (Club club : clubs) {
                if (club.getName().equalsIgnoreCase(name) && club.getCity().equalsIgnoreCase(city)) {
                    return Optional.of(club);
                }
            }
            return Optional.empty();
        }
        return getRepository().findByNameIgnoreCaseAndCityIgnoreCase(name, city);
    }
}
