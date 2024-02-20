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

import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MinimumGroupFightManager {

    public List<Fight> createFights(Tournament tournament, List<Team> teams, TeamsOrder teamsOrder, Integer level, Integer shiaijo, String createdBy) {
        return createFightList(tournament, teams, teamsOrder, level, shiaijo, createdBy);
    }

    private Fight createFight(Tournament tournament, Team team1, Team team2, Integer shiaijo, Integer level, String createdBy) {
        return new Fight(tournament, team1, team2, shiaijo, level, createdBy);
    }

    /**
     * Create a list of fights where each team has only two fights in a Round Robin way.
     *
     * @param tournament
     * @param teams
     * @param teamsOrder
     * @return
     */
    protected List<Fight> createFightList(Tournament tournament, List<Team> teams, TeamsOrder teamsOrder, Integer level,
                                          Integer shiaijo, String createdBy) {
        if (teams == null || tournament == null || teams.size() < 2) {
            return new ArrayList<>();
        }
        final List<Fight> fights = new ArrayList<>();

        final List<Team> sortedTeams = TeamSelector.setOrder(teams, teamsOrder);
        Fight fight;

        for (int i = 0; i < sortedTeams.size(); i++) {
            if (i % 2 == 0) {
                fight = createFight(tournament, sortedTeams.get((i) % sortedTeams.size()), sortedTeams.get((i + 1) % sortedTeams.size()),
                        shiaijo, level, createdBy);
            } else {
                fight = createFight(tournament, sortedTeams.get((i + 1) % sortedTeams.size()), sortedTeams.get((i) % sortedTeams.size()),
                        shiaijo, level, createdBy);
            }
            fights.add(fight);

            //Groups with only two teams has only one fight.
            if (teams.size() == 2) {
                break;
            }
        }
        return fights;
    }
}
