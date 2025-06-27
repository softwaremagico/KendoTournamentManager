package com.softwaremagico.kt.persistence.entities;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
 * %%
 * Copyright (C) 2021 - 2025 Softwaremagico
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

import com.softwaremagico.kt.persistence.encryption.IntegerCryptoConverter;
import com.softwaremagico.kt.persistence.encryption.LocalDateTimeCryptoConverter;
import com.softwaremagico.kt.persistence.encryption.StringCryptoConverter;
import com.softwaremagico.kt.persistence.encryption.TournamentTypeCryptoConverter;
import com.softwaremagico.kt.persistence.values.ScoreType;
import com.softwaremagico.kt.persistence.values.TournamentType;
import com.softwaremagico.kt.utils.IName;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.LocalDateTime;

@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "tournaments")
public class Tournament extends Element implements IName {
    public static final int DEFAULT_DURATION = 180;

    @Column(name = "name", nullable = false)
    @Convert(converter = StringCryptoConverter.class)
    private String name;

    @Column(name = "shiaijos", nullable = false)
    @Convert(converter = IntegerCryptoConverter.class)
    private Integer shiaijos;

    @Column(name = "team_size", nullable = false)
    @Convert(converter = IntegerCryptoConverter.class)
    private Integer teamSize;

    @Column(name = "tournament_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @Convert(converter = TournamentTypeCryptoConverter.class)
    private TournamentType type;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "tournament_score")
    private TournamentScore tournamentScore;

    @Column(name = "duels_duration", nullable = false)
    @Convert(converter = IntegerCryptoConverter.class)
    private Integer duelsDuration = DEFAULT_DURATION;

    @Column(name = "locked", nullable = false)
    private boolean locked = false;

    @Column(name = "locked_at")
    @Convert(converter = LocalDateTimeCryptoConverter.class)
    private LocalDateTime lockedAt;

    @Column(name = "started_at")
    @Convert(converter = LocalDateTimeCryptoConverter.class)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    @Convert(converter = LocalDateTimeCryptoConverter.class)
    private LocalDateTime finishedAt;

    public Tournament() {
        super();
    }

    public Tournament(String name, int shiaijos, int teamSize, TournamentType type, String createdBy) {
        this(name, shiaijos, teamSize, type, createdBy, ScoreType.CLASSIC);
    }

    public Tournament(String name, int shiaijos, int teamSize, TournamentType type, String createdBy, ScoreType scoreType) {
        this();
        setName(name);
        setShiaijos(shiaijos);
        setTeamSize(teamSize);
        setType(type);
        setCreatedBy(createdBy);
        setTournamentScore(new TournamentScore(scoreType));
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTeamSize() {
        if (type != null && type != TournamentType.SENBATSU) {
            return teamSize;
        }
        return 1;
    }

    public void setTeamSize(Integer teamSize) {
        this.teamSize = teamSize;
    }

    public TournamentType getType() {
        return type;
    }

    public void setType(TournamentType type) {
        this.type = type;
    }

    public Integer getShiaijos() {
        return shiaijos;
    }

    public void setShiaijos(Integer shiaijos) {
        this.shiaijos = shiaijos;
    }

    public TournamentScore getTournamentScore() {
        return tournamentScore;
    }

    public void setTournamentScore(TournamentScore tournamentScore) {
        this.tournamentScore = tournamentScore;
    }

    public Integer getDuelsDuration() {
        return duelsDuration;
    }

    public void setDuelsDuration(Integer duelsDuration) {
        this.duelsDuration = duelsDuration;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public LocalDateTime getLockedAt() {
        return lockedAt;
    }

    public void setLockedAt(LocalDateTime lockedAt) {
        this.lockedAt = lockedAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public void updateFinishedAt(LocalDateTime finishedAt) {
        if (this.finishedAt == null) {
            this.finishedAt = finishedAt;
            //Reset if new fights are added
        } else if (finishedAt == null) {
            this.finishedAt = null;
        }
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
