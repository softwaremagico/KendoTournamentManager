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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JPA entity that represents a single fight between two {@link Team}s.
 * <p>
 * A fight contains one {@link Duel} per active member in the fight size. If the
 * tournament's {@code fightSize} is smaller than its {@code teamSize}, the last
 * duels in the list belong to substitute members and are not scored.
 * </p>
 * <p>
 * Fights are grouped into {@link Group}s and ordered by their {@code level} within
 * the tournament bracket. A {@code level} of 0 represents the first (initial) round;
 * higher levels correspond to later rounds in knockout-style formats.
 * </p>
 */
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "fights", indexes = {
        @Index(name = "ind_tournament", columnList = "tournament"),
        @Index(name = "ind_team1", columnList = "team1"),
        @Index(name = "ind_team2", columnList = "team2")
})
@SuppressWarnings("java:S2160")
public class Fight extends Element {

    /** The first (left / red) team competing in this fight. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team1", nullable = false)
    private Team team1;

    /** The second (right / white) team competing in this fight. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team2", nullable = false)
    private Team team2;

    /** The tournament this fight belongs to. Loaded lazily to avoid N+1 issues when listing fights. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament", nullable = false)
    private Tournament tournament;

    /** Zero-based index of the shiaijo (fighting area) on which this fight takes place. */
    @Column(name = "shiaijo", nullable = false)
    private Integer shiaijo = 0;

    /**
     * Individual duels between the members of the two teams.
     * The list is ordered by member position (index).
     * Duels beyond {@code tournament.fightSize} belong to substitutes and have no scoring.
     */
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "duels_by_fight", joinColumns = @JoinColumn(name = "fight_id"), inverseJoinColumns = @JoinColumn(name = "duel_id"))
    @OrderColumn(name = "duel_index")
    private List<Duel> duels = new ArrayList<>();

    /**
     * Round level within the tournament bracket.
     * Level 0 is the initial round; each subsequent level is a later knockout round.
     */
    @Column(name = "fight_level", nullable = false)
    private Integer level = 0;

    public Fight() {
        super();
    }

    /**
     * Creates a fully initialised fight and immediately generates the individual duels
     * for each member pair using the tournament's team size and fight size configuration.
     *
     * @param tournament the tournament this fight belongs to
     * @param team1      the first (left / red) competing team
     * @param team2      the second (right / white) competing team
     * @param shiaijo    zero-based index of the shiaijo where the fight takes place
     * @param level      zero-based round level within the tournament bracket
     * @param createdBy  the username of the user creating this fight
     */
    public Fight(Tournament tournament, Team team1, Team team2, Integer shiaijo, Integer level, String createdBy) {
        this();
        setTournament(tournament);
        setTeam1(team1);
        setTeam2(team2);
        setShiaijo(shiaijo);
        setLevel(level);
        generateDuels(createdBy);
        setCreatedBy(createdBy);
    }

    public Team getTeam1() {
        return team1;
    }

    public void setTeam1(Team team1) {
        this.team1 = team1;
    }

    public Team getTeam2() {
        return team2;
    }

    public void setTeam2(Team team2) {
        this.team2 = team2;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public Integer getShiaijo() {
        return shiaijo;
    }

    public void setShiaijo(Integer shiaijo) {
        this.shiaijo = shiaijo;
    }

    public List<Duel> getDuels() {
        return duels;
    }

    public void setDuels(List<Duel> duels) {
        this.duels = duels;
    }

    /**
     * Returns all duels in this fight in which the specified competitor participates,
     * either as competitor 1 or competitor 2.
     *
     * @param competitor the participant to filter by
     * @return list of duels involving the competitor; empty list if the competitor is not part of this fight
     */
    public List<Duel> getDuels(Participant competitor) {
        return getDuels().stream().filter(duel -> Objects.equals(duel.getCompetitor1(), competitor)
                || Objects.equals(duel.getCompetitor2(), competitor)).toList();
    }

    /**
     * Determines the winning team of this fight.
     * <p>
     * The winner is the team whose members won the most individual duels. If both
     * teams have the same number of duel victories, total ippon points are compared
     * as a tiebreaker.
     * </p>
     *
     * @return the winning team, or {@code null} if the fight is a draw
     */
    public Team getWinner() {
        int points = 0;
        for (int i = 0; i < getDuels().size(); i++) {
            points += getDuels().get(i).getWinner();
        }
        if (points < 0) {
            return team1;
        }
        if (points > 0) {
            return team2;
        }
        // If are draw rounds, the winner is who has more points.
        int pointLeft = 0;
        int pointRight = 0;
        for (int i = 0; i < getDuels().size(); i++) {
            pointLeft += getDuels().get(i).getCompetitor1ScoreValue();
            pointRight += getDuels().get(i).getCompetitor2ScoreValue();
        }
        if (pointLeft > pointRight) {
            return team1;
        }
        if (pointLeft < pointRight) {
            return team2;
        }
        return null;
    }

    /**
     * Returns the losing team of this fight.
     *
     * @return the losing team, or {@code null} if the fight is a draw
     */
    public Team getLoser() {
        final Team winner = getWinner();
        if (winner == null) {
            return null;
        }
        if (Objects.equals(winner, team1)) {
            return team2;
        }
        return team1;
    }

    /**
     * Returns {@code true} if all duels in this fight are finished.
     *
     * @return {@code true} when every duel has been completed
     */
    public boolean isOver() {
        return duels.stream().allMatch(Duel::isOver);
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder();
        text.append("Tournament: ").append(tournament.getId()).append(", Shiaijo: ").append(shiaijo).append(", Teams: '").
                append(team1.getName()).append("' vs '").append(team2.getName()).append("'");
        if (isOver()) {
            text.append(" [F]");
        }
        return text.append("\n").toString();
    }

    /**
     * Populates the duel list from the current team member rosters.
     * <p>
     * One duel is created for each position up to {@code tournament.teamSize}.
     * Duels at positions equal to or beyond {@code tournament.fightSize} are marked
     * as substitute duels and are automatically set to finished (no scoring impact).
     * Any previously existing duels are cleared before generation.
     * </p>
     *
     * @param createdBy the username of the user triggering the generation
     */
    public void generateDuels(String createdBy) {
        duels.clear();
        if (team1 != null && team2 != null) {
            for (int i = 0; i < tournament.getTeamSize(); i++) {
                final Duel duel = new Duel(i < team1.getMembers().size() ? team1.getMembers().get(i) : null,
                        i < team2.getMembers().size() ? team2.getMembers().get(i) : null, tournament, createdBy);
                duel.setTotalDuration(tournament.getDuelsDuration());
                //Substitute fights are marked as over.
                duel.setSubstitute(i >= tournament.getFightSize());
                duels.add(duel);
            }
        }
    }

    /**
     * Returns the total number of valid ippon points scored by the specified competitor
     * across all duels in this fight.
     *
     * @param competitor the participant whose score is requested
     * @return total ippon-equivalent points scored by the competitor
     */
    public Integer getScore(Participant competitor) {
        int score = 0;
        score += getDuels().stream().filter(duel ->
                (Objects.equals(duel.getCompetitor1(), competitor))).mapToInt(Duel::getCompetitor1ScoreValue).sum();
        score += getDuels().stream().filter(duel ->
                (Objects.equals(duel.getCompetitor2(), competitor))).mapToInt(Duel::getCompetitor2ScoreValue).sum();
        return score;
    }

    /**
     * Returns the total number of ippon points scored against the specified competitor
     * (i.e. points conceded) across all duels in this fight.
     *
     * @param competitor the participant whose points conceded are requested
     * @return total ippon-equivalent points conceded by the competitor
     */
    public Integer getScoreAgainst(Participant competitor) {
        int score = 0;
        score += getDuels().stream().filter(duel ->
                (Objects.equals(duel.getCompetitor1(), competitor))).mapToInt(Duel::getCompetitor2ScoreValue).sum();
        score += getDuels().stream().filter(duel ->
                (Objects.equals(duel.getCompetitor2(), competitor))).mapToInt(Duel::getCompetitor1ScoreValue).sum();
        return score;
    }

    /**
     * Returns the number of duels that ended in a draw and involved the specified competitor.
     *
     * @param competitor the participant to count draw duels for
     * @return number of draw duels for the competitor in this fight
     */
    public Integer getDrawDuels(Participant competitor) {
        return (int) getDuels().stream().filter(duel -> duel.getWinner() == 0
                && (Objects.equals(duel.getCompetitor1(), competitor) || Objects.equals(duel.getCompetitor2(), competitor))).count();
    }

    /**
     * Returns the number of drawn duels for the specified team in this fight.
     *
     * @param team the team to count draw duels for
     * @return number of draw duels, or {@code 0} if the team is not part of this fight
     */
    public Integer getDrawDuels(Team team) {
        int drawDuels = 0;
        if ((getTeam1().equals(team) || getTeam2().equals(team))) {
            drawDuels = (int) getDuels().stream().filter(duel -> duel.getWinner() == 0).count();
        }
        return drawDuels;
    }

    /**
     * Returns the number of duels won by the specified competitor in this fight.
     *
     * @param competitor the participant to count victories for
     * @return number of duels won by the competitor
     */
    public Integer getDuelsWon(Participant competitor) {
        int numberOfDuels = 0;
        numberOfDuels += (int) getDuels().stream().filter(duel -> duel.getWinner() == -1
                && (Objects.equals(duel.getCompetitor1(), competitor))).count();
        numberOfDuels += (int) getDuels().stream().filter(duel -> duel.getWinner() == 1
                && (Objects.equals(duel.getCompetitor2(), competitor))).count();
        return numberOfDuels;
    }

    /**
     * Returns {@code true} if the specified competitor's team won this fight.
     *
     * @param competitor the participant to check
     * @return {@code true} if the competitor belongs to the winning team
     */
    public boolean isWon(Participant competitor) {
        if (competitor != null) {
            if (team1.isMember(competitor) && Objects.equals(getWinner(), team1)) {
                return true;
            }
            return team2.isMember(competitor) && Objects.equals(getWinner(), team2);
        }
        return false;
    }

    /**
     * Returns {@code true} if this fight ended without a winner (both teams tied).
     *
     * @return {@code true} if the fight is a draw
     */
    public boolean isDrawFight() {
        return getWinner() == null;
    }

    /**
     * Returns the number of duels won by the specified team in this fight.
     *
     * @param team the team to count victories for
     * @return number of duels won, or {@code 0} if the team is not part of this fight
     */
    public int getWonDuels(Team team) {
        if (Objects.equals(team1, team)) {
            return (int) getDuels().stream().filter(duel -> duel.getWinner() == -1).count();
        }
        if (Objects.equals(team2, team)) {
            return (int) getDuels().stream().filter(duel -> duel.getWinner() == 1).count();
        }
        return 0;
    }

    public Integer getScore(Team team) {
        if (Objects.equals(team1, team)) {
            return getScoreTeam1();
        }
        if (Objects.equals(team2, team)) {
            return getScoreTeam2();
        }
        return 0;
    }

    public Integer getScoreAgainst(Team team) {
        if (Objects.equals(team1, team)) {
            return getScoreTeam2();
        }
        if (Objects.equals(team2, team)) {
            return getScoreTeam1();
        }
        return 0;
    }

    public Integer getScoreTeam1() {
        return getDuels().stream().mapToInt(Duel::getCompetitor1ScoreValue).sum();
    }

    public Integer getScoreTeam2() {
        return getDuels().stream().mapToInt(Duel::getCompetitor2ScoreValue).sum();
    }
}
