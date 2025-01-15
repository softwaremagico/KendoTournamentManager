package com.softwaremagico.kt.core.managers;

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

import com.softwaremagico.kt.persistence.entities.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoopTeamSelector extends TeamSelector {

    protected LoopTeamSelector(List<Team> teams, TeamsOrder teamsOrder) {
        super(teams, teamsOrder);
    }

    @Override
    protected Map<Team, List<Team>> getAdversaries() {
        final Map<Team, List<Team>> combinations = new HashMap<>();
        for (int i = 0; i < getTeams().size(); i++) {
            final List<Team> otherTeams = new ArrayList<>();
            combinations.put(getTeams().get(i), otherTeams);

            // Teams already in the loop are changed to last position.
            for (int j = i + 1; j < getTeams().size(); j++) {
                otherTeams.add(getTeams().get(j));
            }
            for (int j = 0; j < i; j++) {
                otherTeams.add(getTeams().get(j));
            }
        }
        return combinations;
    }
}
