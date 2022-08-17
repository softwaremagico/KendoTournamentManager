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

import com.softwaremagico.kt.persistence.encryption.IntegerCryptoConverter;
import com.softwaremagico.kt.persistence.encryption.LocalDateTimeAttributeConverter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "fights")
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
    @Convert(converter = IntegerCryptoConverter.class)
    private Integer shiaijo = 0;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "duels_by_fight", joinColumns = @JoinColumn(name = "fight_id"), inverseJoinColumns = @JoinColumn(name = "duel_id"))
    @OrderColumn(name = "duel_index")
    private List<Duel> duels = new ArrayList<>();

    @Column(name = "finished_at")
    @Convert(converter = LocalDateTimeAttributeConverter.class)
    private LocalDateTime finishedAt;

    @Column(name = "fight_level", nullable = false)
    @Convert(converter = IntegerCryptoConverter.class)
    private Integer level = 0;

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
        generateDuels();
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

    public boolean isOver() {
        return duels.stream().anyMatch(Duel::isOver);
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
                append(team1.getName()).append("' vs '").append(team2.getName()).append("'\n");
        if (isOver()) {
            text.append(" [F]");
        }
        return text.toString();
    }

    public void generateDuels() {
        duels.clear();
        if (team1 != null && team2 != null) {
            for (int i = 0; i < Math.max(team1.getMembers().size(), team2.getMembers().size()); i++) {
                final Duel duel = new Duel(i < team1.getMembers().size() ? team1.getMembers().get(i) : null,
                        i < team2.getMembers().size() ? team2.getMembers().get(i) : null);
                duel.setTotalDuration(tournament.getDuelsDuration());
                duels.add(duel);
            }
        }
    }
}

