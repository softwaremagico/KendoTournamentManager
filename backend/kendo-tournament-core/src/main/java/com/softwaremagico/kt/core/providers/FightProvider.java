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

import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.FightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class FightProvider extends CrudProvider<Fight, Integer, FightRepository> {

    @Autowired
    public FightProvider(FightRepository fightRepository) {
        super(fightRepository);
    }

    public List<Fight> getFights(Tournament tournament, Integer level) {
        final List<Fight> fights = repository.findByTournamentAndLevel(tournament, level);
        fights.forEach(f -> f.setTournament(tournament));
        return fights;
    }

    public Collection<Fight> getFights(Tournament tournament) {
        final List<Fight> fights = repository.findByTournament(tournament);
        fights.forEach(f -> {
            f.setTournament(tournament);
            f.getTeam1().setTournament(tournament);
            f.getTeam2().setTournament(tournament);
            f.getTeam2().setTournament(tournament);
        });
        return fights;
    }

    public boolean areOver(Tournament tournament) {
        return repository.countByTournamentAndFinishedAtIsNull(tournament) == 0;
    }

    public Fight getCurrentFight(Tournament tournament) {
        return repository.findFirstByTournamentAndFinishedAtIsNullOrderByCreatedAtAsc(tournament);
    }

    public void delete(Tournament tournament) {
        repository.deleteByTournament(tournament);
    }
}
