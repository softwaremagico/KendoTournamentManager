package com.softwaremagico.kt.persistence.entities;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
 * %%
 * Copyright (C) 2021 - 2024 Softwaremagico
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

import com.softwaremagico.kt.persistence.encryption.LocalDateTimeCryptoConverter;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "fights", indexes = {
        @Index(name = "ind_tournament", columnList = "tournament"),
        @Index(name = "ind_team1", columnList = "team1"),
        @Index(name = "ind_team2", columnList = "team2")
})
public class Fight extends Element {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team1", nullable = false)
    private Team team1;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team2", nullable = false)
    private Team team2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament", nullable = false)
    private Tournament tournament;

    @Column(name = "shiaijo", nullable = false)
    private Integer shiaijo = 0;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "duels_by_fight", joinColumns = @JoinColumn(name = "fight_id"), inverseJoinColumns = @JoinColumn(name = "duel_id"))
    @OrderColumn(name = "duel_index")
    private List<Duel> duels = new ArrayList<>();

    @Column(name = "finished_at")
    @Convert(converter = LocalDateTimeCryptoConverter.class)
    private LocalDateTime finishedAt;

    @Column(name = "fight_level", nullable = false)
    private Integer level = 0;

    public Fight() {
        super();
    }

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

    public List<Duel> getDuels(Participant competitor) {
        return getDuels().stream().filter(duel -> Objects.equals(duel.getCompetitor1(), competitor)
                || Objects.equals(duel.getCompetitor2(), competitor)).toList();
    }

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
        // If are draw rounds, winner is who has more points.
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

    public Team getLooser() {
        final Team winner = getWinner();
        if (winner == null) {
            return null;
        }
        if (Objects.equals(winner, team1)) {
            return team2;
        }
        return team1;
    }

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

    public void generateDuels(String createdBy) {
        duels.clear();
        if (team1 != null && team2 != null) {
            for (int i = 0; i < Math.max(team1.getMembers().size(), team2.getMembers().size()); i++) {
                final Duel duel = new Duel(i < team1.getMembers().size() ? team1.getMembers().get(i) : null,
                        i < team2.getMembers().size() ? team2.getMembers().get(i) : null, tournament, createdBy);
                duel.setTotalDuration(tournament.getDuelsDuration());
                duels.add(duel);
            }
        }
    }

    public Integer getScore(Participant competitor) {
        int score = 0;
        score += getDuels().stream().filter(duel ->
                (Objects.equals(duel.getCompetitor1(), competitor))).mapToInt(duel -> duel.getCompetitor1Score().size()).sum();
        score += getDuels().stream().filter(duel ->
                (Objects.equals(duel.getCompetitor2(), competitor))).mapToInt(duel -> duel.getCompetitor2Score().size()).sum();
        return score;
    }

    public Integer getDrawDuels(Participant competitor) {
        return (int) getDuels().stream().filter(duel -> duel.getWinner() == 0
                && (Objects.equals(duel.getCompetitor1(), competitor) || Objects.equals(duel.getCompetitor2(), competitor))).count();
    }

    public Integer getDrawDuels(Team team) {
        int drawDuels = 0;
        if ((getTeam1().equals(team) || getTeam2().equals(team))) {
            drawDuels = (int) getDuels().stream().filter(duel -> duel.getWinner() == 0).count();
        }
        return drawDuels;
    }

    public Integer getDuelsWon(Participant competitor) {
        int numberOfDuels = 0;
        numberOfDuels += (int) getDuels().stream().filter(duel -> duel.getWinner() == -1
                && (Objects.equals(duel.getCompetitor1(), competitor))).count();
        numberOfDuels += (int) getDuels().stream().filter(duel -> duel.getWinner() == 1
                && (Objects.equals(duel.getCompetitor2(), competitor))).count();
        return numberOfDuels;
    }

    public boolean isWon(Participant competitor) {
        if (competitor != null) {
            if (team1.isMember(competitor) && Objects.equals(getWinner(), team1)) {
                return true;
            }
            return team2.isMember(competitor) && Objects.equals(getWinner(), team2);
        }
        return false;
    }

    public boolean isDrawFight() {
        return getWinner() == null;
    }

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

    public Integer getScoreTeam1() {
        return getDuels().stream().mapToInt(duel -> duel.getCompetitor1Score().size()).sum();
    }

    public Integer getScoreTeam2() {
        return getDuels().stream().mapToInt(duel -> duel.getCompetitor2Score().size()).sum();
    }
}

