package com.softwaremagico.kt.core.tournaments;

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


import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Level {
    private final int levelIndex;
    private Tournament tournament;
    private List<Group> tournamentGroups;
    private Level nextLevel;
    private final Level previousLevel;

    protected Level(Tournament tournament, int levelIndex, Level nextLevel, Level previousLevel) {
        this.tournament = tournament;
        tournamentGroups = new ArrayList<>();
        this.levelIndex = levelIndex;
        this.nextLevel = nextLevel;
        this.previousLevel = previousLevel;
    }

    public Integer size() {
        return tournamentGroups.size();
    }

    public List<Group> geGroups() {
        return tournamentGroups;
    }

    /**
     * Return the last group of the level.
     *
     * @return
     */
    protected Group getLasGroupOfLevel() {
        if (tournamentGroups.size() > 0) {
            return tournamentGroups.get(tournamentGroups.size() - 1);
        } else {
            return null;
        }
    }

    public void removeTeams() {
        for (final Group group : tournamentGroups) {
            group.removeTeams();
        }
    }

    protected boolean isFightOfLevel(Fight fight) {
        for (final Group group : tournamentGroups) {
            if (group.isFightOfGroup(fight)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isGroupOfLevel(Group group) {
        for (final Group tournamentGroup : tournamentGroups) {
            if (group.equals(tournamentGroup)) {
                return true;
            }
        }
        return false;
    }

    public boolean isLevelFinished() {
        for (final Group group : tournamentGroups) {
            if (!group.areFightsOverOrNull()) {
                return false;
            }
        }
        return true;
    }

    public int getNumberOfTotalTeamsPassNextRound() {
        int teams = 0;
        for (final Group group : tournamentGroups) {
            teams += group.getNumberOfWinners();
        }
        return teams;
    }

    public void updateArenaOfGroups() {
        if (tournamentGroups.size() > 0) {
            // Divide groups by arena.
            final double groupsPerArena = Math.ceil((double) tournamentGroups.size() / (double) tournament.getShiaijos());
            for (int j = 0; j < tournamentGroups.size(); j++) {
                tournamentGroups.get(j).setShiaijo((j) / (int) groupsPerArena);
            }
        }
        if (nextLevel != null) {
            nextLevel.updateArenaOfGroups();
        }
    }

    protected Group geGroupOfFight(Fight fight) {
        for (final Group group : tournamentGroups) {
            if (group.getTeams().contains(fight.getTeam1()) && group.getTeams().contains(fight.getTeam2())) {
                return group;
            }
        }
        return null;
    }

    public Set<Team> getUsedTeams() {
        final Set<Team> usedTeams = new HashSet<>();
        for (final Group group : tournamentGroups) {
            usedTeams.addAll(group.getTeams());
        }
        return usedTeams;
    }

    public Integer getIndexOfGroup(Group group) {
        for (int i = 0; i < tournamentGroups.size(); i++) {
            if (tournamentGroups.get(i).equals(group)) {
                return i;
            }
        }
        return null;
    }

    public List<Group> getTournamentGroups() {
        return tournamentGroups;
    }

    public void setTournamentGroups(List<Group> tournamentGroups) {
        this.tournamentGroups = tournamentGroups;
    }

    public int getLevelIndex() {
        return levelIndex;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public Level getNextLevel() {
        return nextLevel;
    }

    public Level getPreviousLevel() {
        return previousLevel;
    }

    public void setNextLevel(Level nextLevel) {
        this.nextLevel = nextLevel;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public void update() {

    }

    public void removeGroups() {
        setTournamentGroups(new ArrayList<>());
        setNextLevel(null);
    }

    public void removeGroup(Group group) {
        if (getTournamentGroups().size() > 0) {
            getTournamentGroups().remove(group);
            if (getNextLevel() != null) {
                getNextLevel().updateGroupsSize();
                if (getNextLevel().size() == 0) {
                    setNextLevel(null);
                }
            }
        }
    }

    public void updateGroupsSize() {
        // At least, one group by level.
        if (geGroups().isEmpty()) {
            final Group group = new Group();
            group.setTournament(getTournament());
            addGroup(group);
        }

        updateArenaOfGroups();
    }

    public void addGroup(Group group) {
        getTournamentGroups().add(group);
    }

    protected Level createNewLevel(Tournament tournament, Integer level, Level nextLevel, Level previousLevel) {
        return new Level(tournament, level, nextLevel, previousLevel);
    }

    public Group geGroupDestinationOfWinner(Group group, Integer winner) {
        return getNextLevel().geGroups().get(geGroupIndexDestinationOfWinner(group, winner));
    }

    public Integer geGroupIndexDestinationOfWinner(Group group, Integer winner) {
        return 0;
    }

    public boolean hasFightsAssigned() {
        for (final Group group : getTournamentGroups()) {
            if (group.getFights() == null || group.getFights().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "Level: " + getLevelIndex();
    }
}
