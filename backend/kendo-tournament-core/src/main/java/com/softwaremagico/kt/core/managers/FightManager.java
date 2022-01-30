package com.softwaremagico.kt.core.managers;

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

import com.softwaremagico.kt.core.providers.DuelProvider;
import com.softwaremagico.kt.core.providers.FightProvider;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FightManager {
    private final FightProvider fightProvider;
    private final DuelProvider duelProvider;

    @Autowired
    public FightManager(FightProvider fightProvider, DuelProvider duelProvider) {
        this.fightProvider = fightProvider;
        this.duelProvider = duelProvider;
    }


    public List<Fight> createFights(Tournament tournament, List<Team> teams, boolean random, Integer level) {
        return createCompleteFightList(tournament, teams, random, level);
    }

    private Fight createFight(Tournament tournament, Team team1, Team team2, Integer shiaijo, Integer level) {
        return new Fight(tournament, team1, team2, shiaijo, level);
    }

    /**
     * Create a list of fights where all teams fight versus all others.
     *
     * @param tournament
     * @param teams
     * @param random
     * @return
     */
    protected List<Fight> createCompleteFightList(Tournament tournament, List<Team> teams, boolean random, Integer level) {
        if (teams == null || tournament == null || teams.size() < 2) {
            return null;
        }
        final List<Fight> fights = new ArrayList<>();
        final TeamSelector teamSelector = new TeamSelector(teams);

        Team team1 = teamSelector.getTeamWithMoreAdversaries(random);
        Fight fight, lastFight = null;
        while (teamSelector.remainFights()) {
            final Team team2 = teamSelector.getNextAdversary(team1, random);
            // Team1 has no more adversaries. Use another one.
            if (team2 == null) {
                team1 = teamSelector.getTeamWithMoreAdversaries(random);
                continue;
            }
            // Remaining fights sometimes repeat team. Align them.
            if (lastFight != null && (lastFight.getTeam1().equals(team2) || lastFight.getTeam2().equals(team1))) {
                fight = createFight(tournament, team2, team1, 0, level);
            } else if (lastFight != null && (lastFight.getTeam1().equals(team1) || lastFight.getTeam2().equals(team2))) {
                fight = createFight(tournament, team1, team2, 0, level);
            } else if (fights.size() % 2 == 0) {
                fight = createFight(tournament, team1, team2, 0, level);
            } else {
                fight = createFight(tournament, team2, team1, 0, level);
            }
            fights.add(fight);
            lastFight = fight;
            teamSelector.removeAdversary(team1, team2);
            team1 = team2;
        }
        return fights;
    }

    /**
     * All teams fights agains the next and previous team of the list.
     *
     * @param tournament
     * @param teams
     * @param fightArea
     * @param level
     * @param index
     * @return
     */
    protected List<Fight> createTwoFightsForEachTeam(Tournament tournament, List<Team> teams, int fightArea, int level, int index) {
        if (teams == null || tournament == null || teams.size() < 2) {
            return null;
        }
        final List<Fight> fights = new ArrayList<>();

        // If only exists two teams, there are only one fight. If no, as many
        // fights as teams
        for (int i = 0; i < (teams.size() > 2 ? teams.size() : 1); i++) {
            Fight fight;
            final Team team1 = teams.get(i);
            final Team team2 = teams.get((i + 1) % teams.size());

            if (fights.size() % 2 == 0) {
                fight = createFight(tournament, team1, team2, fightArea, level);
            } else {
                fight = createFight(tournament, team2, team1, fightArea, level);
            }
            fights.add(fight);
        }
        return fights;
    }
}
