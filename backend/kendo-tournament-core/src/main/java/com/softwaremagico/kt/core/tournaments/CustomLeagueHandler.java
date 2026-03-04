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

import com.softwaremagico.kt.core.exceptions.CustomTournamentFightsException;
import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.RankingProvider;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.core.providers.TournamentExtraPropertyProvider;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Tournament;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomLeagueHandler extends LeagueHandler {


    public CustomLeagueHandler(GroupProvider groupProvider, TeamProvider teamProvider,
                               RankingProvider rankingProvider, TournamentExtraPropertyProvider tournamentExtraPropertyProvider) {
        super(groupProvider, teamProvider, rankingProvider, tournamentExtraPropertyProvider);
    }

    @Override
    public List<Fight> createFights(Tournament tournament, TeamsOrder teamsOrder, Integer level, String createdBy) {
        throw new CustomTournamentFightsException(this.getClass(), "This league cannot generate fights.");
    }
}
