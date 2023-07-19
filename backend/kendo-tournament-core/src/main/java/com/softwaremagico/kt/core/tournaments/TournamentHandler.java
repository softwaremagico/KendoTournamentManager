package com.softwaremagico.kt.core.tournaments;

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

import com.softwaremagico.kt.core.controller.RankingController;
import com.softwaremagico.kt.core.converters.GroupConverter;
import com.softwaremagico.kt.core.managers.SimpleGroupFightManager;
import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.core.providers.FightProvider;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Tournament;

import java.util.List;

public class TournamentHandler extends LeagueHandler {

    private final SimpleGroupFightManager simpleGroupFightManager;
    private final FightProvider fightProvider;
    private final GroupProvider groupProvider;


    public TournamentHandler(GroupProvider groupProvider, TeamProvider teamProvider, GroupConverter groupConverter, RankingController rankingController,
                             SimpleGroupFightManager simpleGroupFightManager, FightProvider fightProvider) {
        super(groupProvider, teamProvider, groupConverter, rankingController);
        this.groupProvider = groupProvider;
        this.simpleGroupFightManager = simpleGroupFightManager;
        this.fightProvider = fightProvider;
    }

    @Override
    public List<Fight> createFights(Tournament tournament, TeamsOrder teamsOrder, Integer level, String createdBy) {
        return null;
    }
}
