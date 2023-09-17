package com.softwaremagico.kt.core.providers;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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

import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.TournamentExtraPropertyRepository;
import com.softwaremagico.kt.persistence.repositories.TournamentRepository;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class TournamentProvider extends CrudProvider<Tournament, Integer, TournamentRepository> {
    public static final int DEFAULT_TEAM_SIZE = 3;
    private final TournamentExtraPropertyRepository tournamentExtraPropertyRepository;

    @Autowired
    public TournamentProvider(TournamentRepository tournamentRepository, TournamentExtraPropertyRepository tournamentExtraPropertyRepository) {
        super(tournamentRepository);
        this.tournamentExtraPropertyRepository = tournamentExtraPropertyRepository;
    }

    public Tournament save(String name, Integer shiaijos, Integer teamSize, TournamentType type, String createdBy) {
        return getRepository().save(new Tournament(name, shiaijos != null ? shiaijos : 1, teamSize != null ? teamSize : DEFAULT_TEAM_SIZE,
                type != null ? type : TournamentType.LEAGUE, createdBy));
    }

    @Override
    public void delete(Tournament tournament) {
        tournamentExtraPropertyRepository.deleteByTournament(tournament);
        getRepository().delete(tournament);
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
        if (tournament == null || tournament.getCreatedAt() == null) {
            return new ArrayList<>();
        }
        // Due to LocalDateTime encryption countByGreaterThan is not working very well.
        // final Pageable pageable = PageRequest.of(0, elementsToRetrieve, Sort.Direction.DESC, "createdAt");
        // return getRepository().findByCreatedAtLessThan(tournament.getCreatedAt(), pageable);
        final List<Tournament> tournaments = getRepository().findAll();
        tournaments.sort(Comparator.comparing(Tournament::getCreatedAt).reversed());
        return tournaments.subList(tournaments.indexOf(tournament) + 1, Math.min(tournaments.indexOf(tournament) + 1 + elementsToRetrieve, tournaments.size()));
    }

    public List<Tournament> getPreviousTo(Tournament tournament) {
        if (tournament == null || tournament.getCreatedAt() == null) {
            return new ArrayList<>();
        }
        final List<Tournament> tournaments = getRepository().findAll();
        tournaments.sort(Comparator.comparing(Tournament::getCreatedAt).reversed());
        return tournaments.subList(tournaments.indexOf(tournament) + 1, tournaments.size());
    }

    public long countTournamentsAfter(LocalDateTime createdAfter) {
        //Due to LocalDateTime encryption countByGreaterThan is not working very well.
        return getRepository().findAll().stream().filter(tournament -> tournament.getCreatedAt().isAfter(createdAfter.with(LocalTime.MIN))).count();
    }

    public void markAsFinished(Tournament tournament, boolean finish) {
        if (finish && tournament.getFinishedAt() == null) {
            tournament.updateFinishedAt(LocalDateTime.now());
            getRepository().save(tournament);
        } else if (!finish && tournament.getFinishedAt() != null) {
            tournament.updateFinishedAt(null);
            getRepository().save(tournament);
        }
    }

}
