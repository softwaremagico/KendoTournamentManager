package com.softwaremagico.kt.persistence.entities;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "fights")
public class Fight extends Element {

    @ManyToOne
    @JoinColumn(name = "team1")
    private Team team1;

    @ManyToOne
    @JoinColumn(name = "team2")
    private Team team2;

    @ManyToOne
    @JoinColumn(name = "tournament")
    private Tournament tournament;

    @Column(name = "shiaijo")
    private Integer shiaijo;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "duels_by_fight", joinColumns = @JoinColumn(name = "fight_id"), inverseJoinColumns = @JoinColumn(name = "duel_id"))
    @OrderColumn(name = "index")
    private List<Duel> duels;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "level")
    private Integer level;

    public Fight() {
        super();
    }

    public Fight(Tournament tournament, Team team1, Team team2, Integer shiaijo, Integer level) {
        this();
        setTournament(tournament);
        setTeam1(team1);
        setTeam2(team2);
        setShiaijo(shiaijo);
        setLevel(level);
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

    public List<Duel> getDuels(Participant competitor) {
        return getDuels().stream().filter(duel -> Objects.equals(duel.getCompetitor1(), competitor) ||
                Objects.equals(duel.getCompetitor2(), competitor)).collect(Collectors.toList());
    }

    public void setDuels(List<Duel> duels) {
        this.duels = duels;
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
            pointLeft += getDuels().get(i).getCompetitor1Score();
            pointRight += getDuels().get(i).getCompetitor2Score();
        }
        if (pointLeft > pointRight) {
            return team1;
        }
        if (pointLeft < pointRight) {
            return team2;
        }
        return null;
    }

    public boolean isOver() {
        return getFinishedAt() != null;
    }

    public void setOver(boolean over) {
        if (over) {
            setFinishedAt(LocalDateTime.now());
        } else {
            setFinishedAt(null);
        }
    }

    /**
     * To win a fight, a team need to win more duels or do more points.
     *
     * @return
     */
    public boolean isDrawFight() {
        return getWinner() == null;
    }

    public Integer getDrawDuels(Team team) {
        int drawDuels = 0;
        if ((getTeam1().equals(team) || getTeam2().equals(team))) {
            drawDuels = (int) getDuels().stream().filter(duel -> duel.getWinner() == 0).count();
        }
        return drawDuels;
    }

    public Integer getDrawDuels(Participant competitor) {
        return (int) getDuels().stream().filter(duel -> duel.getWinner() == 0 &&
                (Objects.equals(duel.getCompetitor1(), competitor) || Objects.equals(duel.getCompetitor2(), competitor))).count();
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
        return (int) getDuels().stream().filter(duel -> duel.getWinner() == -1).count();
    }

    public Integer getScoreTeam2() {
        return (int) getDuels().stream().filter(duel -> duel.getWinner() == 1).count();
    }

    public Integer getScore(Participant competitor) {
        int drawDuels = 0;
        drawDuels += (int) getDuels().stream().filter(duel -> duel.getWinner() == -1 &&
                (Objects.equals(duel.getCompetitor1(), competitor))).count();
        drawDuels += (int) getDuels().stream().filter(duel -> duel.getWinner() == 1 &&
                (Objects.equals(duel.getCompetitor2(), competitor))).count();
        return drawDuels;
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

    public int getWonDuels(Team team) {
        if (Objects.equals(team1, team)) {
            return (int) getDuels().stream().filter(duel -> duel.getWinner() == -1).count();
        }
        if (Objects.equals(team2, team)) {
            return (int) getDuels().stream().filter(duel -> duel.getWinner() == 1).count();
        }
        return 0;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }
}

