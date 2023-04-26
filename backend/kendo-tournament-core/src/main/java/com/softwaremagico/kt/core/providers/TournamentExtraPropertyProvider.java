package com.softwaremagico.kt.core.providers;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import com.softwaremagico.kt.persistence.repositories.TournamentExtraPropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TournamentExtraPropertyProvider extends CrudProvider<TournamentExtraProperty, Integer, TournamentExtraPropertyRepository> {

    @Autowired
    public TournamentExtraPropertyProvider(TournamentExtraPropertyRepository repository) {
        super(repository);
    }

    public List<TournamentExtraProperty> getAll(Tournament tournament) {
        return repository.findByTournament(tournament);
    }

    public TournamentExtraProperty getByTournamentAndProperty(Tournament tournament, TournamentExtraPropertyKey key) {
        return repository.findByTournamentAndProperty(tournament, key);
    }

    public int deleteByTournamentAndProperty(Tournament tournament, TournamentExtraPropertyKey key) {
        return repository.deleteByTournamentAndProperty(tournament, key);
    }

    @Override
    public TournamentExtraProperty save(TournamentExtraProperty tournamentExtraProperty) {
        return repository.save(tournamentExtraProperty);
    }
}
