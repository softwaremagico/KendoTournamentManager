package com.softwaremagico.kt.core.tournaments;

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

import com.softwaremagico.kt.core.controller.RankingController;
import com.softwaremagico.kt.core.converters.GroupConverter;
import com.softwaremagico.kt.core.managers.SimpleGroupFightManager;
import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.core.providers.FightProvider;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Tournament;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SimpleLeagueHandler extends LeagueHandler {

    private final SimpleGroupFightManager simpleGroupFightManager;
    private final FightProvider fightProvider;
    private final GroupProvider groupProvider;


    @Autowired
    public SimpleLeagueHandler(GroupProvider groupProvider, SimpleGroupFightManager simpleGroupFightManager, FightProvider fightProvider,
                               TeamProvider teamProvider, GroupConverter groupConverter, RankingController rankingController) {
        super(groupProvider, teamProvider, groupConverter, rankingController);
        this.simpleGroupFightManager = simpleGroupFightManager;
        this.fightProvider = fightProvider;
        this.groupProvider = groupProvider;
    }

    @Override
    public List<Fight> createFights(Tournament tournament, TeamsOrder teamsOrder, boolean maximizeFights, Integer level, String createdBy) {
        if (level != 0) {
            return null;
        }
        //Automatically generates the group if needed in getGroup.
        final List<Fight> fights = fightProvider.saveAll(simpleGroupFightManager.createFights(tournament, getGroup(tournament).getTeams(),
                TeamsOrder.NONE, level, createdBy));
        final Group group = getGroup(tournament);
        group.setFights(fights);
        groupProvider.save(group);
        return fights;
    }
}