package com.softwaremagico.kt.core.tournaments;

/*
 * #%L
 * KendoTournamentGenerator
 * %%
 * Copyright (C) 2008 - 2013 Softwaremagico
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


import com.softwaremagico.kt.core.exceptions.TournamentFinishedException;
import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;

import java.util.List;

/**
 * Strategy interface for all tournament management algorithms.
 * <p>
 * Each concrete implementation encodes a specific tournament format:
 * </p>
 * <ul>
 *   <li>{@link SimpleLeagueHandler} — round-robin league</li>
 *   <li>{@link LoopLeagueHandler} — continuous-loop league</li>
 *   <li>{@link CustomLeagueHandler} — administrator-defined fight order</li>
 *   <li>{@link BubbleSortTournamentHandler} — real-time ranking via bubble-sort</li>
 *   <li>{@link KingOfTheMountainHandler} — king-of-the-mountain elimination</li>
 *   <li>{@link SenbatsuTournamentHandler} — senbatsu (team selection) format</li>
 *   <li>{@link TreeTournamentHandler} — single-elimination bracket</li>
 * </ul>
 * <p>
 * The correct implementation for a given {@link Tournament} is selected at runtime
 * by {@link TournamentHandlerSelector}.
 * </p>
 */
public interface ITournamentManager {

    /**
     * Creates the full initial set of fights for the tournament at level 0,
     * using the configured team ordering strategy.
     *
     * @param tournament  the tournament to generate fights for
     * @param teamsOrder  the strategy used to determine the order of teams in fights
     * @param createdBy   the username of the user triggering the generation
     * @return the list of newly persisted {@link Fight} objects
     */
    List<Fight> createInitialFights(Tournament tournament, TeamsOrder teamsOrder, String createdBy);

    /**
     * Creates fights for the specified round level.
     *
     * @param tournament  the tournament to generate fights for
     * @param teamsOrder  the strategy used to determine the order of teams in fights
     * @param level       the zero-based round level (0 = first round)
     * @param createdBy   the username of the user triggering the generation
     * @return the list of newly persisted {@link Fight} objects
     */
    List<Fight> createFights(Tournament tournament, TeamsOrder teamsOrder, Integer level, String createdBy);

    /**
     * Generates the next batch of fights based on the current tournament state.
     * Used by dynamic formats (e.g. {@link BubbleSortTournamentHandler}) that
     * generate fights incrementally after each result is recorded.
     *
     * @param tournament the tournament whose next fights should be generated
     * @param createdBy  the username of the user triggering the generation
     * @return the list of newly persisted {@link Fight} objects, or an empty list
     *         if the tournament is complete
     */
    List<Fight> generateNextFights(Tournament tournament, String createdBy);

    /**
     * Returns all groups for the tournament across all levels.
     *
     * @param tournament the tournament whose groups to retrieve
     * @return ordered list of groups
     */
    List<Group> getGroups(Tournament tournament);

    /**
     * Returns all groups at the specified round level.
     *
     * @param tournament the tournament whose groups to retrieve
     * @param level      the zero-based round level
     * @return groups at the given level, or an empty list if the level does not exist
     */
    List<Group> getGroups(Tournament tournament, Integer level);

    /**
     * Returns all groups assigned to the specified shiaijo (fighting area).
     *
     * @param tournament the tournament to search within
     * @param shiaijo    zero-based shiaijo index
     * @return groups assigned to the given shiaijo
     */
    List<Group> getGroupsByShiaijo(Tournament tournament, Integer shiaijo);

    /**
     * Returns the group that contains the specified fight.
     *
     * @param tournament the tournament to search within
     * @param fight      the fight whose group should be found
     * @return the containing group, or {@code null} if not found
     */
    Group getGroup(Tournament tournament, Fight fight);

    /**
     * Adds a new group to the tournament, replacing any existing group configuration
     * if the format allows only a single group.
     *
     * @param tournament the tournament to add the group to
     * @param group      the group to persist
     * @return the persisted group
     */
    Group addGroup(Tournament tournament, Group group);

    /**
     * Returns the zero-based sequential index of the group across all levels.
     *
     * @param group the group whose index to determine
     * @return the index of the group
     */
    int getIndexOfGroup(Group group);

    /**
     * Removes the group at the specified level and index position.
     *
     * @param tournament the tournament containing the group
     * @param level      the round level of the group to remove
     * @param groupIndex the position index of the group within the level
     */
    void removeGroup(Tournament tournament, Integer level, Integer groupIndex);

    /**
     * Returns the flat sequential index for a group at the given level.
     *
     * @param level the round level
     * @param group the group to index
     * @return the flat index
     */
    int getIndex(Integer level, Group group);

    /**
     * Returns {@code true} if the specified team is registered in at least one group
     * of the tournament.
     *
     * @param tournament the tournament to check
     * @param team       the team to look for
     * @return {@code true} if the team exists in any group
     */
    boolean exist(Tournament tournament, Team team);

    /**
     * Removes all teams from all groups at the specified level.
     *
     * @param tournament the tournament whose groups to clear
     * @param level      the round level from which teams should be removed
     */
    void removeTeams(Tournament tournament, Integer level);

    /**
     * Removes all teams from all groups across all levels.
     *
     * @param tournament the tournament whose groups to clear
     */
    void removeTeams(Tournament tournament);

    /**
     * Distributes the tournament's groups evenly across the available shiaijos
     * (fighting areas) based on the tournament's configured shiaijo count.
     *
     * @param tournament the tournament whose groups to distribute
     */
    void setDefaultFightAreas(Tournament tournament);

    /**
     * Configures how many top-ranked teams from each group advance to the next
     * knockout round.
     *
     * @param winners number of teams that pass through from each group
     */
    void setHowManyTeamsOfGroupPassToTheTree(Integer winners);

    /**
     * Returns {@code true} if the tournament is currently in its final fight,
     * i.e. all preceding rounds have been completed and only one fight remains.
     *
     * @param tournament the tournament to check
     * @return {@code true} if this is the last fight of the tournament
     */
    boolean isTheLastFight(Tournament tournament);

    /**
     * Removes all fights from all groups in the tournament.
     * Typically called before regenerating the fight schedule.
     *
     * @param tournament the tournament whose fights to remove
     */
    void removeFights(Tournament tournament);

    /**
     * Advances the tournament to the next knockout round, creating a new set of
     * groups and populating them with the winners from the current level's groups.
     *
     * @param tournament the tournament to advance
     * @throws TournamentFinishedException if the tournament has no more rounds to generate
     */
    void createNextLevel(Tournament tournament) throws TournamentFinishedException;

    /**
     * Returns {@code true} if fights in the given group can end in a draw (tied score),
     * as opposed to requiring a decisive result.
     *
     * @param group the group whose draw policy to query
     * @return {@code true} if draws are permitted for fights in this group
     */
    boolean hasDrawScore(Group group);
}
