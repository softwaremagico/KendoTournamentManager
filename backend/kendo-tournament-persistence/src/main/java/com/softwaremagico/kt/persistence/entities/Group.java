package com.softwaremagico.kt.persistence.entities;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
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

import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity that represents a pool or bracket node within a tournament.
 * <p>
 * In league-style tournaments there is a single group containing all teams.
 * In championship (tree) tournaments the bracket is represented as a set of groups
 * arranged by {@code level} and {@code index}: level 0 holds the initial pools,
 * level 1 holds the second-round pools, and so on.
 * </p>
 * <p>
 * Each group is associated with a shiaijo so that parallel fighting areas can
 * operate independently.
 * </p>
 */
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "tournament_groups", indexes = {
        @Index(name = "ind_tournament", columnList = "tournament"),
})
public class Group extends Element {

    /** The tournament this group belongs to. */
    @ManyToOne
    @JoinColumn(name = "tournament", nullable = false)
    private Tournament tournament;

    /**
     * Ordered list of teams in this group.
     * The order determines the team's seeding position within the group.
     */
    @ManyToMany
    @Fetch(FetchMode.JOIN)
    @JoinTable(name = "teams_by_group", joinColumns = @JoinColumn(name = "group_id"), inverseJoinColumns = @JoinColumn(name = "team_id"))
    @OrderColumn(name = "group_index")
    private List<Team> teams;

    /** Zero-based index of the shiaijo (fighting area) assigned to this group. */
    @Column(name = "shiaijo", nullable = false)
    private Integer shiaijo = 0;

    /**
     * Round level of this group within the tournament bracket.
     * Level 0 is the initial round; each subsequent level is a later knockout round.
     */
    @Column(name = "group_level", nullable = false)
    private Integer level = 0;

    /** Zero-based position of this group among all groups at the same {@code level}. */
    @Column(name = "group_index", nullable = false)
    private Integer index = 0;

    /**
     * All scheduled fights within this group, in the order they were generated.
     * Orphan fights are removed automatically when the group is saved.
     */
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "fights_by_group", joinColumns = @JoinColumn(name = "group_id"), inverseJoinColumns = @JoinColumn(name = "fight_id"))
    @OrderColumn(name = "group_index")
    private List<Fight> fights;

    /**
     * How many top-ranked teams from this group advance to the next round.
     * Typically 1, but can be set to 2 for larger championships.
     */
    @Column(name = "number_of_winners", nullable = false)
    private int numberOfWinners = 1;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "unties", joinColumns = @JoinColumn(name = "group_id"), inverseJoinColumns = @JoinColumn(name = "duel_id"))
    private List<Duel> unties = new ArrayList<>();

    public Group() {
        super();
    }

    /**
     * Creates a group with the specified tournament, round level and position index.
     *
     * @param tournament the tournament this group belongs to
     * @param level      zero-based round level within the tournament bracket
     * @param index      zero-based position among all groups at the same level
     */
    public Group(Tournament tournament, int level, int index) {
        super();
        setTournament(tournament);
        setLevel(level);
        setIndex(index);
    }

    /**
     * Returns {@code true} if all fights in the provided list are finished, or the list is empty.
     *
     * @param fights the list of fights to evaluate
     * @return {@code true} if every fight is over or no fights exist
     */
    public static boolean areFightsOverOrNull(List<Fight> fights) {
        if (!fights.isEmpty()) {
            for (final Fight fight : fights) {
                if (!fight.isOver()) {
                    return false;
                }
            }
            return true;
        }
        return true;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public Integer getShiaijo() {
        return shiaijo;
    }

    public void setShiaijo(Integer shiaijo) {
        this.shiaijo = shiaijo;
    }

    public List<Fight> getFights() {
        return fights;
    }

    /**
     * Replaces the current fight list with the given one.
     * The underlying collection is reused to preserve JPA entity references;
     * the list is cleared and then re-populated.
     *
     * @param fights the new list of fights to assign to this group
     */
    public void setFights(List<Fight> fights) {
        if (this.fights == null) {
            this.fights = new ArrayList<>();
        }
        this.fights.clear();
        this.fights.addAll(fights);
    }

    /**
     * Removes all teams from this group.
     */
    public void removeTeams() {
        teams.clear();
    }

    /**
     * Removes all fights from this group.
     */
    public void removeFights() {
        fights.clear();
    }

    /**
     * Returns {@code true} if the given fight belongs to this group.
     *
     * @param fight the fight to look up
     * @return {@code true} if the fight is in this group's fight list
     */
    public boolean isFightOfGroup(Fight fight) {
        return fights.contains(fight);
    }

    /**
     * Returns {@code true} if all fights in this group are finished, or if the group
     * has fewer than two teams (no fights needed).
     *
     * @return {@code true} when every fight is over or no fights are required
     */
    public boolean areFightsOverOrNull() {
        if (teams.size() < 2) {
            return true;
        }
        return areFightsOverOrNull(getFights());
    }

    public int getNumberOfWinners() {
        return numberOfWinners;
    }

    public void setNumberOfWinners(int numberOfWinners) {
        this.numberOfWinners = numberOfWinners;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    /**
     * Creates a new untie duel between two competitors and adds it to the untie list.
     * <p>
     * The duel is assigned the tournament's configured duel duration and marked as
     * type {@link DuelType#UNDRAW}. Untie duels are used to resolve a draw when
     * the standard fights cannot determine a winner.
     * </p>
     *
     * @param competitor1 the first competitor in the untie duel
     * @param competitor2 the second competitor in the untie duel
     * @param createdBy   the username of the user creating the duel
     */
    public void createUntieDuel(Participant competitor1, Participant competitor2, String createdBy) {
        final Duel untie = new Duel(competitor1, competitor2, tournament, createdBy);
        untie.setTotalDuration(tournament.getDuelsDuration());
        untie.setType(DuelType.UNDRAW);
        unties.add(untie);
    }

    public List<Duel> getUnties() {
        return unties;
    }

    /**
     * Replaces the current untie duel list with the given one.
     * The underlying collection is reused to preserve JPA entity references;
     * the list is cleared and then re-populated.
     *
     * @param unties the new list of untie duels to assign to this group
     */
    public void setUnties(List<Duel> unties) {
        if (this.unties == null) {
            this.unties = new ArrayList<>();
        }
        this.unties.clear();
        this.unties.addAll(unties);
    }

    @Override
    public String toString() {
        return "Group{"
                + "tournament=" + tournament
                + ", shiaijo=" + shiaijo
                + ", level=" + level
                + ", index=" + index
                + '}';
    }
}
