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

import java.util.List;

@Service
public class FightProvider {
    private final FightRepository fightRepository;

    @Autowired
    public FightProvider(FightRepository fightRepository) {
        this.fightRepository = fightRepository;
    }

    public List<Fight> getFights(Tournament tournament, Integer level) {
        return fightRepository.findByTournamentAndLevel(tournament, level);
    }

    public List<Fight> getFights(Tournament tournament) {
        return fightRepository.findByTournament(tournament);
    }

    public boolean areOver(Tournament tournament) {
        return fightRepository.countByTournamentAndFinishedAtIsNull(tournament) == 0;
    }

    public Fight getCurrentFight(Tournament tournament) {
        return fightRepository.findFirstByTournamentAndFinishedAtIsNullOrderByCreatedAtAsc(tournament);
    }

    public List<Fight> save(List<Fight> fights) {
        return fightRepository.saveAll(fights);
    }

    public Fight save(Fight fight) {
        return fightRepository.save(fight);
    }

}
