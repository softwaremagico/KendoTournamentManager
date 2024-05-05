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
public class KingOfTheMountainFightManager {

    public List<Fight> createFights(Tournament tournament, List<Team> teams, Integer level, String createdBy) {
        return createOneFightsForEachTeam(tournament, teams, level, createdBy);
    }

    private Fight createFight(Tournament tournament, Team team1, Team team2, Integer shiaijo, Integer level, String createdBy) {
        return new Fight(tournament, team1, team2, shiaijo, level, createdBy);
    }

    private List<Fight> createOneFightsForEachTeam(Tournament tournament, List<Team> teams, int level, String createdBy) {
        if (teams == null || tournament == null || teams.size() < 2) {
            return new ArrayList<>();
        }
        final List<Fight> fights = new ArrayList<>();

        // If only exists two teams, there are only one fight. If no, as many
        // fights as teams
        for (int i = 0; i < (teams.size() > 2 ? teams.size() : 1); i++) {
            final Team team1 = teams.get(i);
            final Team team2 = teams.get((i + 1) % teams.size());

            if (fights.size() % 2 == 0) {
                fights.add(createFight(tournament, team1, team2, 0, level, createdBy));
            } else {
                fights.add(createFight(tournament, team2, team1, 0, level, createdBy));
            }
        }
        return fights;
    }
}
