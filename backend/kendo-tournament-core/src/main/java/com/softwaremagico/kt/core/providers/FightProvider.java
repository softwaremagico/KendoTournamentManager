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

import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.FightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class FightProvider extends CrudProvider<Fight, Integer, FightRepository> {

    @Autowired
    public FightProvider(FightRepository fightRepository) {
        super(fightRepository);
    }

    public List<Fight> getFights(Tournament tournament, Integer level) {
        final List<Fight> fights = getRepository().findByTournamentAndLevel(tournament, level);
        fights.forEach(f -> f.setTournament(tournament));
        return fights;
    }

    public List<Fight> getFights(Tournament tournament) {
        final List<Fight> fights = getRepository().findByTournament(tournament);
        fights.forEach(f -> {
            f.setTournament(tournament);
            f.getTeam1().setTournament(tournament);
            f.getTeam2().setTournament(tournament);
        });
        return fights;
    }

    public Fight getFirstNotOver(Tournament tournament) {
        for (final Fight fight : getFights(tournament)) {
            for (final Duel duel : fight.getDuels()) {
                if (!duel.isOver()) {
                    return fight;
                }
            }
        }
        return null;
    }

    public boolean areOver(Tournament tournament) {
        return getFirstNotOver(tournament) == null;
    }

    public Fight getCurrent(Tournament tournament) {
        final Fight fight = getFirstNotOver(tournament);
        fight.setTournament(tournament);
        fight.getTeam1().setTournament(tournament);
        fight.getTeam2().setTournament(tournament);
        return fight;
    }

    public List<Fight> get(Collection<Participant> participants) {
        return getRepository().findByParticipantIn(participants);
    }

    public long delete(Tournament tournament) {
        return getRepository().deleteByTournament(tournament);
    }

    public long count(Tournament tournament) {
        return getRepository().countByTournament(tournament);
    }

    /**
     * Count fights that all duels are finished
     *
     * @param tournament
     * @return
     */
    public long countByTournamentAndFinished(Tournament tournament) {
        return getRepository().countByTournamentAndFinishedNot(tournament, false);
    }

    public Integer getCurrentLevel(Tournament tournament) {
        final Optional<Fight> fight = getRepository().findFirstByTournamentOrderByLevelDesc(tournament);
        if (fight.isPresent()) {
            return fight.get().getLevel();
        }
        return 0;
    }
}
