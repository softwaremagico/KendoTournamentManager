package com.softwaremagico.kt.core.tournaments;


import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Level implements Serializable {
    private static final long serialVersionUID = -1611900089563066556L;
    private final int levelIndex;
    private Tournament tournament;
    private List<Group> tournamenGroups;
    private Level nextLevel;
    private final Level previousLevel;

    protected Level(Tournament tournament, int levelIndex, Level nextLevel, Level previousLevel) {
        this.tournament = tournament;
        tournamenGroups = new ArrayList<>();
        this.levelIndex = levelIndex;
        this.nextLevel = nextLevel;
        this.previousLevel = previousLevel;
    }

    public Integer size() {
        return tournamenGroups.size();
    }

    public List<Group> geGroups() {
        return tournamenGroups;
    }

    /**
     * Return the last group of the level.
     *
     * @return
     */
    protected Group getLasGroupOfLevel() {
        if (tournamenGroups.size() > 0) {
            return tournamenGroups.get(tournamenGroups.size() - 1);
        } else {
            return null;
        }
    }

    public void removeTeams() {
        for (Group group : tournamenGroups) {
            group.removeTeams();
        }
    }

    protected boolean isFightOfLevel(Fight fight) {
        for (Group group : tournamenGroups) {
            if (group.isFightOfGroup(fight)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isGroupOfLevel(Group Group) {
        for (Group t : tournamenGroups) {
            if (Group.equals(t)) {
                return true;
            }
        }
        return false;
    }

    public boolean isLevelFinished() {
        for (Group group : tournamenGroups) {
            if (!group.areFightsOverOrNull()) {
                return false;
            }
        }
        return true;
    }

    public int getNumberOfTotalTeamsPassNextRound() {
        int teams = 0;
        for (Group group : tournamenGroups) {
            teams += group.getNumberOfWinners();
        }
        return teams;
    }

    public void updateArenaOfGroups() {
        if (tournamenGroups.size() > 0) {
            // Divide groups by arena.
            double groupsPerArena = Math.ceil((double) tournamenGroups.size() / (double) tournament.getShiaijos());
            for (int j = 0; j < tournamenGroups.size(); j++) {
                tournamenGroups.get(j).setShiaijo((j) / (int) groupsPerArena);
            }
        }
        if (nextLevel != null) {
            nextLevel.updateArenaOfGroups();
        }
    }

    protected Group geGroupOfFight(Fight fight) {
        for (Group group : tournamenGroups) {
            if (group.getTeams().contains(fight.getTeam1()) && group.getTeams().contains(fight.getTeam2())) {
                return group;
            }
        }
        return null;
    }

    public Set<Team> getUsedTeams() {
        Set<Team> usedTeams = new HashSet<>();
        for (Group group : tournamenGroups) {
            usedTeams.addAll(group.getTeams());
        }
        return usedTeams;
    }

    public Integer getIndexOfGroup(Group group) {
        for (int i = 0; i < tournamenGroups.size(); i++) {
            if (tournamenGroups.get(i).equals(group)) {
                return i;
            }
        }
        return null;
    }

    public List<Group> getTournamenGroups() {
        return tournamenGroups;
    }

    public void setTournamenGroups(List<Group> tournamenGroups) {
        this.tournamenGroups = tournamenGroups;
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
        setTournamenGroups(new ArrayList<>());
        setNextLevel(null);
    }

    public void removeGroup(Group group) {
        if (getTournamenGroups().size() > 0) {
            getTournamenGroups().remove(group);
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
            Group group = new Group();
            group.setTournament(getTournament());
            addGroup(group);
        }

        updateArenaOfGroups();
    }

    public void addGroup(Group group) {
        getTournamenGroups().add(group);
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
        for (Group group : getTournamenGroups()) {
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
