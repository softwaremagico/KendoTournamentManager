package com.softwaremagico.kt.core.managers;

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

import com.softwaremagico.kt.persistence.entities.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class TeamSelector {

    private final Random randomGenerator = new Random();

    private final List<Team> teams;
    private final Map<Team, List<Team>> combination;

    protected TeamSelector(List<Team> teams, TeamsOrder teamsOrder) {
        this.teams = setOrder(teams, teamsOrder);
        combination = getAdversaries();
    }

    public static List<Team> setOrder(List<Team> teams, TeamsOrder teamsOrder) {
        final List<Team> sortedTeams = new ArrayList<>(teams);
        switch (teamsOrder) {
            case SORTED -> Collections.sort(sortedTeams);
            case RANDOM -> Collections.shuffle(sortedTeams);
            default -> {
            }
        }
        return sortedTeams;
    }

    public List<Team> getAdversaries(Team team) {
        return combination.get(team);
    }

    public List<Team> getTeams() {
        return teams;
    }

    protected Map<Team, List<Team>> getAdversaries() {
        final Map<Team, List<Team>> combinations = new HashMap<>();
        for (int i = 0; i < getTeams().size(); i++) {
            final List<Team> otherTeams = new ArrayList<>();
            combinations.put(getTeams().get(i), otherTeams);

            for (int j = 0; j < getTeams().size(); j++) {
                if (i != j) {
                    otherTeams.add(getTeams().get(j));
                }
            }
        }
        return combinations;
    }

    public Team getTeamWithMoreAdversaries(TeamsOrder teamsOrder) {
        return getTeamWithMoreAdversaries(teams, teamsOrder);
    }

    public Team getTeamWithMoreAdversaries(List<Team> teamGroup, TeamsOrder teamsOrder) {
        int maxAdv = -1;
        // Get max Adversaries value:
        for (final Team team : teamGroup) {
            if (combination.get(team).size() > maxAdv) {
                maxAdv = combination.get(team).size();
            }
        }

        // Select one of the teams with max adversaries
        final List<Team> possibleAdversaries = new ArrayList<>();
        for (final Team team : teamGroup) {
            if (combination.get(team).size() == maxAdv) {
                // If no random, return the first one.
                if (teamsOrder != TeamsOrder.RANDOM) {
                    return team;
                } else {
                    possibleAdversaries.add(team);
                }
            }
        }

        if (!possibleAdversaries.isEmpty()) {
            return possibleAdversaries.get(randomGenerator.nextInt(possibleAdversaries.size()));
        }
        return null;
    }

    public Team getNextAdversary(Team team, TeamsOrder teamsOrder) {
        return getTeamWithMoreAdversaries(combination.get(team), teamsOrder);
    }

    public void removeAdversary(Team team, Team adversary) {
        combination.get(team).remove(adversary);
        combination.get(adversary).remove(team);
    }

    public boolean remainFights() {
        for (final Team team : teams) {
            if (!combination.get(team).isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
