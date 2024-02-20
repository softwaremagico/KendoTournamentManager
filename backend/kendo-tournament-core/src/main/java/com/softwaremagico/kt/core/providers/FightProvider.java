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
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.FightRepository;
import com.softwaremagico.kt.persistence.repositories.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FightProvider extends CrudProvider<Fight, Integer, FightRepository> {

    private final GroupRepository groupRepository;

    @Autowired
    public FightProvider(FightRepository fightRepository, GroupRepository groupRepository) {
        super(fightRepository);
        this.groupRepository = groupRepository;
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

    public void delete(Tournament tournament) {
        groupRepository.findByTournamentOrderByLevelAscIndexAsc(tournament).forEach(group -> {
            group.setFights(new ArrayList<>());
            groupRepository.save(group);
        });
        getRepository().deleteByTournament(tournament);
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

    @Override
    public void delete(Fight fight) {
        if (fight != null) {
            final Group group = groupRepository.findByFightsId(fight.getId()).orElse(null);
            if (group != null) {
                group.getFights().remove(fight);
                groupRepository.save(group);
            }
            super.delete(fight);
        }
    }

    @Override
    public void delete(Collection<Fight> fights) {
        if (fights != null) {
            final List<Group> groups = groupRepository.findDistinctByFightsIdIn(fights.stream().map(Fight::getId).collect(Collectors.toSet()));
            groups.forEach(group -> {
                group.getFights().removeAll(fights);
            });
            groupRepository.saveAll(groups);
            super.delete(fights);
        }
    }

}
