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

import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CompleteGroupFightManager {

    private record FightContext(Tournament tournament, Integer level, Integer shiaijo, String createdBy) {
    }

    public List<Fight> createFights(Tournament tournament, List<Team> teams, TeamsOrder teamsOrder, Integer level,
                                    Integer shiaijo, boolean fifo, String createdBy) {
        return this.createCompleteFightList(tournament, teams, teamsOrder, level, shiaijo, fifo, createdBy);
    }

    private Fight createFight(Tournament tournament, Team team1, Team team2, Integer shiaijo, Integer level, String createdBy) {
        return new Fight(tournament, team1, team2, shiaijo, level, createdBy);
    }

    /**
     * Create a list of fights where all teams fight versus all others.
     *
     * @param tournament tournament where the fights are generated
     * @param teams teams that must fight each other
     * @param teamsOrder strategy used to select the next team
     * @return generated fight list for the group
     */
    protected List<Fight> createCompleteFightList(Tournament tournament, List<Team> teams, TeamsOrder teamsOrder, Integer level, Integer shiaijo, boolean fifo,
                                                   String createdBy) {
        if (teams == null || tournament == null || teams.size() < 2) {
            return new ArrayList<>();
        }
        final List<Fight> fights = new ArrayList<>();
        final TeamSelector teamSelector = new TeamSelector(teams, teamsOrder);

        final FightContext fightContext = new FightContext(tournament, level, shiaijo, createdBy);
        Team team1 = teamSelector.getTeamWithMoreAdversaries(teamsOrder);
        Fight lastFight = null;
        while (teamSelector.remainFights()) {
            final Team team2 = teamSelector.getNextAdversary(team1, teamsOrder);
            if (team2 == null) {
                team1 = teamSelector.getTeamWithMoreAdversaries(teamsOrder);
                continue;
            }
            final Fight fight = determineFightOrder(team1, team2, lastFight, fights.size(), fightContext);
            fights.add(fight);
            lastFight = fight;
            teamSelector.removeAdversary(team1, team2);
            if (fifo || fights.size() != 1) {
                team1 = team2;
            }
        }
        return fights;
    }

    private Fight determineFightOrder(Team team1, Team team2, Fight lastFight, int fightCount, FightContext fightContext) {
        if (lastFight != null && (lastFight.getTeam1().equals(team2) || lastFight.getTeam2().equals(team1))) {
            return createFight(fightContext.tournament(), team2, team1, fightContext.shiaijo(), fightContext.level(),
                    fightContext.createdBy());
        } else if (lastFight != null && (lastFight.getTeam1().equals(team1) || lastFight.getTeam2().equals(team2))) {
            return createFight(fightContext.tournament(), team1, team2, fightContext.shiaijo(), fightContext.level(),
                    fightContext.createdBy());
        } else if (fightCount % 2 == 0) {
            return createFight(fightContext.tournament(), team1, team2, fightContext.shiaijo(), fightContext.level(),
                    fightContext.createdBy());
        } else {
            return createFight(fightContext.tournament(), team2, team1, fightContext.shiaijo(), fightContext.level(),
                    fightContext.createdBy());
        }
    }
}
