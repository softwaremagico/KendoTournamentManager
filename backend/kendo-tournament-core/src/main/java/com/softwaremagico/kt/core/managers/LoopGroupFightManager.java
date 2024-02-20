package com.softwaremagico.kt.core.managers;

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

import com.softwaremagico.kt.core.providers.TournamentExtraPropertyProvider;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LoopGroupFightManager {
    private final TournamentExtraPropertyProvider tournamentExtraPropertyProvider;


    @Autowired
    public LoopGroupFightManager(TournamentExtraPropertyProvider tournamentExtraPropertyProvider) {
        this.tournamentExtraPropertyProvider = tournamentExtraPropertyProvider;
    }


    public List<Fight> createFights(Tournament tournament, List<Team> teams, TeamsOrder teamsOrder, Integer level, String createdBy) {
        return createCompleteFightList(tournament, teams, teamsOrder, level, createdBy);
    }

    private Fight createFight(Tournament tournament, Team team1, Team team2, Integer shiaijo, Integer level, String createdBy) {
        return new Fight(tournament, team1, team2, shiaijo, level, createdBy);
    }

    /**
     * Create a list of fights where all teams fight versus all others.
     */
    protected List<Fight> createCompleteFightList(Tournament tournament, List<Team> teams, TeamsOrder teamsOrder, Integer level,
                                                  String createdBy) {
        if (teams.size() < 2) {
            return new ArrayList<>();
        }
        final List<Fight> fights = new ArrayList<>();
        final TeamSelector remainingFights = new LoopTeamSelector(teams, teamsOrder);

        final List<Team> remainingTeams = remainingFights.getTeams();

        final TournamentExtraProperty property = tournamentExtraPropertyProvider.getByTournamentAndProperty(tournament,
                TournamentExtraPropertyKey.AVOID_DUPLICATES);
        final boolean maximizeFights = property != null && !Boolean.parseBoolean(property.getPropertyValue());

        for (final Team team : remainingTeams) {
            for (final Team adversary : remainingFights.getAdversaries(team)) {
                //Avoid repeat fights between the same teams.
                if (maximizeFights || fights.stream().
                        noneMatch(fight -> ((fight.getTeam1() == team && fight.getTeam2() == adversary)
                                || (fight.getTeam2() == team && fight.getTeam1() == adversary)))) {
                    fights.add(createFight(tournament, team, adversary, 0, level, createdBy));
                }
            }
        }

        return fights;
    }
}
