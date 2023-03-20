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

import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.TournamentExtraPropertyRepository;
import com.softwaremagico.kt.persistence.repositories.TournamentRepository;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TournamentProvider extends CrudProvider<Tournament, Integer, TournamentRepository> {
    private final TournamentExtraPropertyRepository tournamentExtraPropertyRepository;

    @Autowired
    public TournamentProvider(TournamentRepository tournamentRepository, TournamentExtraPropertyRepository tournamentExtraPropertyRepository) {
        super(tournamentRepository);
        this.tournamentExtraPropertyRepository = tournamentExtraPropertyRepository;
    }

    public Tournament save(String name, Integer shiaijos, Integer teamSize, TournamentType type, String createdBy) {
        return repository.save(new Tournament(name, shiaijos != null ? shiaijos : 1, teamSize != null ? teamSize : 3,
                type != null ? type : TournamentType.LEAGUE, createdBy));
    }

    @Override
    public void delete(Tournament tournament) {
        tournamentExtraPropertyRepository.deleteByTournament(tournament);
        repository.delete(tournament);
    }

    @Override
    public Tournament update(Tournament tournament) {
        if (tournament.isLocked() && tournament.getLockedAt() == null) {
            tournament.setLockedAt(LocalDateTime.now());
        } else if (!tournament.isLocked()) {
            tournament.setLockedAt(null);
        }
        return super.update(tournament);
    }

    public List<Tournament> getPreviousTo(Tournament tournament, int elementsToRetrieve) {
        final Pageable pageable = PageRequest.of(0, elementsToRetrieve, Sort.Direction.DESC, "id");
        return repository.findByCreatedAtLessThanEqual(tournament.getCreatedAt(), pageable);
    }

}
