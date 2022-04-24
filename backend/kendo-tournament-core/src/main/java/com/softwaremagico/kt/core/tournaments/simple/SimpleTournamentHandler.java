package com.softwaremagico.kt.core.tournaments.simple;

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

import com.softwaremagico.kt.core.exceptions.TournamentFinishedException;
import com.softwaremagico.kt.core.managers.FightManager;
import com.softwaremagico.kt.core.providers.FightProvider;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.core.score.Ranking;
import com.softwaremagico.kt.core.tournaments.ITournamentManager;
import com.softwaremagico.kt.core.tournaments.Level;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SimpleTournamentHandler implements ITournamentManager {

    private final GroupProvider groupProvider;
    private final FightManager fightManager;
    private final FightProvider fightProvider;
    private final TeamProvider teamProvider;


    public SimpleTournamentHandler(GroupProvider groupProvider, FightManager fightManager, FightProvider fightProvider, TeamProvider teamProvider) {
        this.groupProvider = groupProvider;
        this.fightManager = fightManager;
        this.fightProvider = fightProvider;
        this.teamProvider = teamProvider;
    }

    protected Group getGroup(Tournament tournament) {
        final List<Group> groups = groupProvider.getGroups(tournament);
        if (groups.isEmpty()) {
            final Group group = new Group();
            group.setTournament(tournament);
            group.setLevel(0);
            group.setTeams(teamProvider.getAll(tournament));
            return addGroup(tournament, group);
        }
        return groups.get(0);
    }

    @Override
    public Integer getNumberOfLevels() {
        return 1;
    }

    @Override
    public List<Group> getGroups(Tournament tournament, Integer level) {
        if (level == 0) {
            return getGroups(tournament);
        }
        return null;
    }


    @Override
    public List<Group> getGroups(Tournament tournament) {
        final List<Group> groups = new ArrayList<>();
        groups.add(getGroup(tournament));
        return groups;
    }

    @Override
    public Group addGroup(Tournament tournament, Group group) {
        if (!groupProvider.getGroups(tournament).isEmpty()) {
            groupProvider.delete(tournament);
        }
        return groupProvider.addGroup(tournament, group);
    }

    @Override
    public void removeGroup(Tournament tournament, Integer level, Integer groupIndex) {
        if (level == 0 && groupIndex == 0) {
            groupProvider.delete(tournament);
        }
    }

    @Override
    public void removeGroup(Group group) {
        groupProvider.delete(group);
    }

    @Override
    public void removeGroups(Tournament tournament, Integer level) {
        groupProvider.delete(tournament, level);
    }

    @Override
    public Level getLevel(Integer level) {
        return null;
    }

    @Override
    public boolean exist(Tournament tournament, Team team) {
        final List<Group> groups = groupProvider.getGroups(tournament);
        if (!groups.isEmpty()) {
            return groups.get(0).getTeams().contains(team);
        }
        return false;
    }

    @Override
    public void removeTeams(Tournament tournament, Integer level) {
        final List<Group> groups = groupProvider.getGroups(tournament);
        if (!groups.isEmpty()) {
            groups.get(0).getTeams().clear();
            groupProvider.save(groups.get(0));
        }
    }

    @Override
    public void setDefaultFightAreas(Tournament tournament) {
        final Group group = getGroup(tournament);
        group.setShiaijo(0);
        groupProvider.save(group);
    }

    @Override
    public List<Fight> createRandomFights(Tournament tournament, boolean maximizeFights, Integer level) {
        if (level != 0) {
            return null;
        }

        //Automatically generates the group if needed in getGroup.
        final List<Fight> fights = fightProvider.saveAll(fightManager.createFights(tournament, getGroup(tournament).getTeams(), true, level));
        final Group group = getGroup(tournament);
        group.setFights(fights);
        groupProvider.save(group);
        return fights;
    }

    @Override
    public List<Fight> createSortedFights(Tournament tournament, boolean maximizeFights, Integer level) {
        if (level != 0) {
            return null;
        }
        //Automatically generates the group if needed in getGroup.
        final List<Fight> fights = fightProvider.saveAll(fightManager.createFights(tournament, getGroup(tournament).getTeams(), false, level));
        final Group group = getGroup(tournament);
        group.setFights(fights);
        groupProvider.save(group);
        return fights;
    }

    @Override
    public Group getGroup(Tournament tournament, Fight fight) {
        if (getGroup(tournament).isFightOfGroup(fight)) {
            return getGroup(tournament);
        }
        return null;
    }

    @Override
    public Integer getLastLevelUsed() {
        return 0;
    }

    @Override
    public void setHowManyTeamsOfGroupPassToTheTree(Integer winners) {
        // Do nothing.
    }

    @Override
    public int getIndex(Integer level, Group group) {
        return 0;
    }

    @Override
    public boolean isTheLastFight(Tournament tournament) {
        final List<Fight> fights = getGroup(tournament).getFights();
        return (fights.size() > 0) && (fights.size() == 1 || fights.get(fights.size() - 2).isOver());
    }

    @Override
    public void removeFights(Tournament tournament) {
        final Group group = getGroup(tournament);
        group.removeFights();
        groupProvider.save(group);
    }

    @Override
    public void removeTeams(Tournament tournament) {
        final Group group = getGroup(tournament);
        group.removeTeams();
        groupProvider.save(group);
    }

    @Override
    public int getIndexOfGroup(Group group) {
        return getIndex(0, group);
    }

    @Override
    public Level getCurrentLevel() {
        return null;
    }

    @Override
    public List<Group> getGroupsByShiaijo(Tournament tournament, Integer shiaijo) {
        return groupProvider.getGroups(tournament, shiaijo);
    }

    @Override
    public List<Level> getLevels() {
        return new ArrayList<>();
    }

    @Override
    public Level getLastLevel() {
        return getLevel(0);
    }

    @Override
    public boolean isNewLevelNeeded() {
        // Only one level is needed.
        return false;
    }

    @Override
    public void createNextLevel() throws TournamentFinishedException {
        // Only one level is needed.
    }

    @Override
    public boolean hasDrawScore(Group group) {
        final Ranking ranking = new Ranking(group);
        final List<Team> teamsInDraw = ranking.getFirstTeamsWithDrawScore(group.getNumberOfWinners());
        return (teamsInDraw != null);
    }
}
