package com.softwaremagico.kt.core.tournaments;

/*
 * #%L
 * KendoTournamentGenerator
 * %%
 * Copyright (C) 2008 - 2013 Softwaremagico
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
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;

import java.util.List;

public interface ITournamentManager {


    List<Fight> createRandomFights(Tournament tournament, boolean maximizeFights, Integer level);

    List<Fight> createSortedFights(Tournament tournament, boolean maximizeFights, Integer level);

    List<Group> getGroups(Tournament tournament);

    List<Group> getGroups(Tournament tournament, Integer level);

    List<Group> getGroupsByShiaijo(Tournament tournament, Integer shiaijo);

    Group getGroup(Tournament tournament, Fight fight);

    Group addGroup(Tournament tournament, Group group);

    int getIndexOfGroup(Group group);

    void removeGroup(Tournament tournament, Integer level, Integer groupIndex);

    void removeGroup(Group group);

    void removeGroups(Tournament tournament, Integer level);

    int getIndex(Integer level, Group group);

    Level getLevel(Integer level);

    Integer getNumberOfLevels();

    Integer getLastLevelUsed();

    boolean exist(Tournament tournament, Team team);

    void removeTeams(Tournament tournament, Integer level);

    void removeTeams(Tournament tournament);

    /**
     * Divide groups into fight areas.
     */
    void setDefaultFightAreas(Tournament tournament);

    void setHowManyTeamsOfGroupPassToTheTree(Integer winners);

    /**
     * We are in the final fight of the tournament.
     *
     * @return
     */
    boolean isTheLastFight(Tournament tournament);

    /**
     * Remove all fights of all groups.
     */
    void removeFights(Tournament tournament);

    /**
     * Returns the level where still are fights not finished.
     *
     * @return
     */
    Level getCurrentLevel();

    List<Level> getLevels();

    Level getLastLevel();

    boolean isNewLevelNeeded();

    void createNextLevel() throws TournamentFinishedException;

    /**
     * Defines if a fight has a draw value or not.
     *
     * @param fight
     * @return
     */
    boolean hasDrawScore(Group group);
}
