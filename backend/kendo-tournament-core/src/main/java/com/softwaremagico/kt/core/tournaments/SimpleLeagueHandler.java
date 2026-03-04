package com.softwaremagico.kt.core.tournaments;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2026 Softwaremagico
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

import com.softwaremagico.kt.core.managers.CompleteGroupFightManager;
import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.RankingProvider;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.core.providers.TournamentExtraPropertyProvider;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.repositories.FightRepository;
import com.softwaremagico.kt.persistence.repositories.GroupRepository;
import com.softwaremagico.kt.persistence.values.LeagueFightsOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SimpleLeagueHandler extends LeagueHandler {

    private final CompleteGroupFightManager completeGroupFightManager;

    private final FightRepository fightRepository;
    private final GroupRepository groupRepository;


    @Autowired
    public SimpleLeagueHandler(GroupProvider groupProvider, CompleteGroupFightManager completeGroupFightManager,
                               TeamProvider teamProvider, RankingProvider rankingProvider,
                               TournamentExtraPropertyProvider tournamentExtraPropertyProvider,
                               FightRepository fightRepository, GroupRepository groupRepository) {
        super(groupProvider, teamProvider, rankingProvider, tournamentExtraPropertyProvider);
        this.completeGroupFightManager = completeGroupFightManager;
        this.fightRepository = fightRepository;
        this.groupRepository = groupRepository;
    }

    @Override
    public List<Fight> createFights(Tournament tournament, TeamsOrder teamsOrder, Integer level, String createdBy) {
        if (level != 0) {
            return new ArrayList<>();
        }
        //Automatically generates the group if needed in getGroup.
        final TournamentExtraProperty extraProperty = getLeagueFightsOrder(tournament);
        final List<Fight> fights = fightRepository.saveAll(completeGroupFightManager.createFights(tournament, getFirstGroup(tournament).getTeams(),
                TeamsOrder.NONE, level, 0, LeagueFightsOrder.get(extraProperty.getPropertyValue()) == LeagueFightsOrder.FIFO, createdBy));
        final Group group = getFirstGroup(tournament);
        group.setFights(fights);
        groupRepository.save(group);
        return fights;
    }
}
