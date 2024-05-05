package com.softwaremagico.kt.persistence.entities;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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

import com.softwaremagico.kt.persistence.encryption.BooleanCryptoConverter;
import com.softwaremagico.kt.persistence.encryption.IntegerCryptoConverter;
import com.softwaremagico.kt.persistence.encryption.LocalDateTimeCryptoConverter;
import com.softwaremagico.kt.persistence.encryption.StringCryptoConverter;
import com.softwaremagico.kt.persistence.values.Score;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "duels", indexes = {
        @Index(name = "ind_tournament", columnList = "tournament"),
        @Index(name = "ind_competitor1", columnList = "competitor1"),
        @Index(name = "ind_competitor2", columnList = "competitor2")
})
public class Duel extends Element {
    public static final int DEFAULT_DURATION = 1;
    public static final int POINTS_TO_WIN = 2;

    @ManyToOne
    @JoinColumn(name = "competitor1")
    private Participant competitor1;

    @ManyToOne
    @JoinColumn(name = "competitor2")
    private Participant competitor2;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "competitor_1_score")
    @Fetch(value = FetchMode.SUBSELECT)
    @Enumerated(EnumType.STRING)
    @OrderColumn(name = "score_index")
    private List<Score> competitor1Score = new ArrayList<>(); // M, K, T, D, H, I

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "competitor_2_score")
    @Fetch(value = FetchMode.SUBSELECT)
    @Enumerated(EnumType.STRING)
    @OrderColumn(name = "score_index")
    private List<Score> competitor2Score = new ArrayList<>(); // M, K, T, D, H, I

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "competitor_1_score_time")
    @Fetch(value = FetchMode.SUBSELECT)
    @OrderColumn(name = "score_index")
    private List<Integer> competitor1ScoreTime = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "competitor_2_score_time")
    @Fetch(value = FetchMode.SUBSELECT)
    @OrderColumn(name = "score_index")
    private List<Integer> competitor2ScoreTime = new ArrayList<>();

    @Column(name = "competitor_1_fault_time")
    @Convert(converter = IntegerCryptoConverter.class)
    private Integer competitor1FaultTime;

    @Column(name = "competitor_2_fault_time")
    @Convert(converter = IntegerCryptoConverter.class)
    private Integer competitor2FaultTime;

    @Column(name = "competitor_1_fault")
    @Convert(converter = BooleanCryptoConverter.class)
    private Boolean competitor1Fault = false;

    @Column(name = "competitor_2_fault")
    @Convert(converter = BooleanCryptoConverter.class)
    private Boolean competitor2Fault = false;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    @Convert(converter = StringCryptoConverter.class)
    private DuelType type;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "finished")
    @Convert(converter = BooleanCryptoConverter.class)
    private boolean finished = false;

    @Column(name = "total_duration")
    @Convert(converter = IntegerCryptoConverter.class)
    private Integer totalDuration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament", nullable = false)
    private Tournament tournament;

    @Column(name = "started_at")
    @Convert(converter = LocalDateTimeCryptoConverter.class)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    @Convert(converter = LocalDateTimeCryptoConverter.class)
    private LocalDateTime finishedAt;

    public Duel() {
        super();
        setType(DuelType.STANDARD);
    }

    public Duel(Participant competitor1, Participant competitor2, Tournament tournament, String createdBy) {
        this();
        setCompetitor1(competitor1);
        setCompetitor2(competitor2);
        setTournament(tournament);
        setCreatedBy(createdBy);
    }

    public Participant getCompetitor1() {
        return competitor1;
    }

    public void setCompetitor1(Participant competitor1) {
        this.competitor1 = competitor1;
    }

    public Participant getCompetitor2() {
        return competitor2;
    }

    public void setCompetitor2(Participant competitor2) {
        this.competitor2 = competitor2;
    }

    public void addCompetitor1Score(Score score) {
        if (this.competitor1Score == null) {
            this.competitor1Score = new ArrayList<>();
        }
        this.competitor1Score.add(score);
    }

    public List<Score> getCompetitor1Score() {
        return competitor1Score;
    }

    public void setCompetitor1Score(List<Score> competitor1Score) {
        this.competitor1Score = competitor1Score;
    }

    public List<Score> getCompetitor2Score() {
        return competitor2Score;
    }

    public void setCompetitor2Score(List<Score> competitor2Score) {
        this.competitor2Score = competitor2Score;
    }

    public void addCompetitor2Score(Score score) {
        if (this.competitor2Score == null) {
            this.competitor2Score = new ArrayList<>();
        }
        this.competitor2Score.add(score);
    }

    public Boolean getCompetitor1Fault() {
        return competitor1Fault;
    }

    public void setCompetitor1Fault(Boolean competitor1Fault) {
        this.competitor1Fault = competitor1Fault;
    }

    public Boolean getCompetitor2Fault() {
        return competitor2Fault;
    }

    public void setCompetitor2Fault(Boolean competitor2Fault) {
        this.competitor2Fault = competitor2Fault;
    }

    /**
     * Count the rounds and the score of each player to know if the duels is
     * over or not.
     *
     * @return true if the round is over.
     */
    public boolean isOver() {
        return getCompetitor1ScoreValue() >= POINTS_TO_WIN || getCompetitor2ScoreValue() >= POINTS_TO_WIN || finished;
    }

    /**
     * Gets the winner of the duel.
     *
     * @return -1 if player of first team, 0 if drawn, 1 if player of second team.
     */
    public int getWinner() {
        return Integer.compare(getCompetitor2ScoreValue(), getCompetitor1ScoreValue());
    }

    public Participant getCompetitorWinner() {
        if (getWinner() < 0) {
            return getCompetitor1();
        } else if (getWinner() > 0) {
            return getCompetitor2();
        }
        return null;
    }

    public Integer getCompetitor1ScoreValue() {
        return (int) competitor1Score.stream().filter(Score::isValidPoint).count();
    }

    public Integer getCompetitor2ScoreValue() {
        return (int) competitor2Score.stream().filter(Score::isValidPoint).count();
    }

    public DuelType getType() {
        return type;
    }

    public void setType(DuelType type) {
        this.type = type;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(Integer totalDuration) {
        this.totalDuration = totalDuration;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public List<Integer> getCompetitor1ScoreTime() {
        return competitor1ScoreTime;
    }

    public void setCompetitor1ScoreTime(List<Integer> competitor1ScoreTime) {
        this.competitor1ScoreTime = competitor1ScoreTime;
    }

    public List<Integer> getCompetitor2ScoreTime() {
        return competitor2ScoreTime;
    }

    public void setCompetitor2ScoreTime(List<Integer> competitor2ScoreTime) {
        this.competitor2ScoreTime = competitor2ScoreTime;
    }

    public Integer getCompetitor1FaultTime() {
        return competitor1FaultTime;
    }

    public void setCompetitor1FaultTime(Integer competitor1FaultTime) {
        this.competitor1FaultTime = competitor1FaultTime;
    }

    public Integer getCompetitor2FaultTime() {
        return competitor2FaultTime;
    }

    public void setCompetitor2FaultTime(Integer competitor2FaultTime) {
        this.competitor2FaultTime = competitor2FaultTime;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }
}
