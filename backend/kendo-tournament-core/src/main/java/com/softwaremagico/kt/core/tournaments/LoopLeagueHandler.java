package com.softwaremagico.kt.core.tournaments;

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

import com.softwaremagico.kt.core.converters.GroupConverter;
import com.softwaremagico.kt.core.managers.LoopGroupFightManager;
import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.core.providers.FightProvider;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.RankingProvider;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.core.providers.TournamentExtraPropertyProvider;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.GroupRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoopLeagueHandler extends LeagueHandler {

    private final LoopGroupFightManager loopGroupFightManager;
    private final FightProvider fightProvider;
    private final GroupRepository groupRepository;


    public LoopLeagueHandler(GroupProvider groupProvider, LoopGroupFightManager loopGroupFightManager, FightProvider fightProvider,
                             TeamProvider teamProvider, GroupConverter groupConverter, RankingProvider rankingProvider,
                             TournamentExtraPropertyProvider tournamentExtraPropertyProvider, GroupRepository groupRepository) {
        super(groupProvider, teamProvider, rankingProvider, tournamentExtraPropertyProvider);
        this.loopGroupFightManager = loopGroupFightManager;
        this.fightProvider = fightProvider;
        this.groupRepository = groupRepository;
    }

    @Override
    public List<Fight> createFights(Tournament tournament, TeamsOrder teamsOrder, Integer level, String createdBy) {
        if (level != 0) {
            return null;
        }
        //Automatically generates the group if needed in getGroup.
        final Group group = getFirstGroup(tournament);
        final List<Fight> fights = fightProvider.saveAll(loopGroupFightManager.createFights(tournament, group.getTeams(),
                TeamsOrder.NONE, level, createdBy));
        group.setFights(fights);
        groupRepository.save(group);
        return fights;
    }

}
