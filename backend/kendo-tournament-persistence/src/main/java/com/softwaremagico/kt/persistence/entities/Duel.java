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

import com.softwaremagico.kt.persistence.encryption.IntegerCryptoConverter;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JPA entity that represents a single match between two individual competitors within a {@link Fight}.
 * <p>
 * In kendo team tournaments a {@link Fight} between two {@link Team}s is composed of
 * several duels — one per member pair. Each duel runs for at most
 * {@code tournament.duelsDuration} seconds. The first competitor to score
 * {@link #POINTS_TO_WIN} ippon-equivalent points wins the duel.
 * </p>
 * <p>
 * Scores are stored as ordered lists of {@link Score} values, where each element
 * corresponds to one ippon scored by the respective competitor. Hansoku (penalty)
 * points are recorded in the same lists using {@link Score#HANSOKU}.
 * Score timestamps (in seconds from duel start) are stored in parallel lists so
 * that the time of each score can be replayed or audited.
 * </p>
 * <p>
 * A duel can be played as an untie duel (type = {@link com.softwaremagico.kt.persistence.values.DuelType#UNDECIDED})
 * when the parent fight is tied and an additional match is needed to determine a winner.
 * </p>
 */
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "duels", indexes = {
        @Index(name = "ind_tournament", columnList = "tournament"),
        @Index(name = "ind_competitor1", columnList = "competitor1"),
        @Index(name = "ind_competitor2", columnList = "competitor2")
})
public class Duel extends Element {
    /** Default duel duration indicator when set individually (typically 1 minute for untie duels). */
    public static final int DEFAULT_DURATION = 1;
    /** Number of points required to win a duel outright. */
    public static final int POINTS_TO_WIN = 2;

    /** The first (left / red) competitor in the duel. */
    @ManyToOne
    @JoinColumn(name = "competitor1")
    private Participant competitor1;

    /** The second (right / white) competitor in the duel. */
    @ManyToOne
    @JoinColumn(name = "competitor2")
    private Participant competitor2;

    /**
     * Ordered list of scores earned by competitor 1.
     * Valid values are the {@link Score} enum entries: M (Men), K (Kote), T (Do), D (Tsuki), H (Hansoku), I (Invalid).
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "competitor_1_score")
    @Fetch(value = FetchMode.SUBSELECT)
    @Enumerated(EnumType.STRING)
    @OrderColumn(name = "score_index")
    private List<Score> competitor1Score = new ArrayList<>(); // M, K, T, D, H, I

    /**
     * Ordered list of scores earned by competitor 2.
     * @see #competitor1Score
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "competitor_2_score")
    @Fetch(value = FetchMode.SUBSELECT)
    @Enumerated(EnumType.STRING)
    @OrderColumn(name = "score_index")
    private List<Score> competitor2Score = new ArrayList<>(); // M, K, T, D, H, I

    /**
     * Timestamps (in seconds from duel start) of each score in {@link #competitor1Score}.
     * Parallel list — index {@code i} in this list corresponds to index {@code i} in {@code competitor1Score}.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "competitor_1_score_time")
    @Fetch(value = FetchMode.SUBSELECT)
    @OrderColumn(name = "score_index")
    private List<Integer> competitor1ScoreTime = new ArrayList<>();

    /**
     * Timestamps (in seconds from duel start) of each score in {@link #competitor2Score}.
     * @see #competitor1ScoreTime
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "competitor_2_score_time")
    @Fetch(value = FetchMode.SUBSELECT)
    @OrderColumn(name = "score_index")
    private List<Integer> competitor2ScoreTime = new ArrayList<>();

    /** Time (in seconds) at which competitor 1 received a fault (hansoku-make), or {@code null} if none. */
    @Column(name = "competitor_1_fault_time")
    @Convert(converter = IntegerCryptoConverter.class)
    private Integer competitor1FaultTime;

    @Column(name = "competitor_2_fault_time")
    @Convert(converter = IntegerCryptoConverter.class)
    private Integer competitor2FaultTime;

    @Column(name = "competitor_1_fault")
    private Boolean competitor1Fault = false;

    @Column(name = "competitor_2_fault")
    private Boolean competitor2Fault = false;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private DuelType type;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "finished")
    private boolean finished = false;

    @Column(name = "total_duration")
    @Convert(converter = IntegerCryptoConverter.class)
    private Integer totalDuration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament", nullable = false)
    private Tournament tournament;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "substitute")
    private Boolean substitute = false;

    public Duel() {
        super();
        setType(DuelType.STANDARD);
    }

    /**
     * Creates a fully initialised duel between two competitors within the given tournament.
     *
     * @param competitor1 the first (left / red) competitor; may be {@code null} if the position is empty
     * @param competitor2 the second (right / white) competitor; may be {@code null} if the position is empty
     * @param tournament  the tournament this duel belongs to
     * @param createdBy   the username of the user creating this duel
     */
    public Duel(Participant competitor1, Participant competitor2, Tournament tournament, String createdBy) {
        this();
        setCompetitor1(competitor1);
        setCompetitor2(competitor2);
        setTournament(tournament);
        setCreatedBy(createdBy);
    }

    /**
     * Returns both competitors of this duel as a set.
     * Competitors with a {@code null} value are excluded from the result.
     *
     * @return a set containing the non-null participants in this duel
     */
    public Set<Participant> getCompetitors() {
        final Set<Participant> competitors = new HashSet<>();
        if (competitor1 != null) {
            competitors.add(competitor1);
        }
        if (competitor2 != null) {
            competitors.add(competitor2);
        }
        return competitors;
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

    /**
     * Appends a score entry for competitor 1, lazily initialising the score list if necessary.
     *
     * @param score the score to add
     */
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

    /**
     * Appends a score entry for competitor 2, lazily initialising the score list if necessary.
     *
     * @param score the score to add
     */
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
        return getCompetitor1ScoreValue() >= POINTS_TO_WIN || getCompetitor2ScoreValue() >= POINTS_TO_WIN || finished || getSubstitute();
    }

    /**
     * Gets the winner of the duel.
     *
     * @return -1 if player of first team, 0 if drawn, 1 if player of second team.
     */
    public int getWinner() {
        return Integer.compare(getCompetitor2ScoreValue(), getCompetitor1ScoreValue());
    }

    /**
     * Returns the winning competitor of this duel, or {@code null} if the duel is a draw.
     *
     * @return the winning {@link Participant}, or {@code null} on a draw
     */
    public Participant getCompetitorWinner() {
        if (getWinner() < 0) {
            return getCompetitor1();
        } else if (getWinner() > 0) {
            return getCompetitor2();
        }
        return null;
    }

    /**
     * Returns the losing competitor of this duel, or {@code null} if the duel is a draw.
     *
     * @return the losing {@link Participant}, or {@code null} on a draw
     */
    public Participant getCompetitorLooser() {
        if (getWinner() < 0) {
            return getCompetitor2();
        } else if (getWinner() > 0) {
            return getCompetitor1();
        }
        return null;
    }

    /**
     * Returns the number of valid ippon points scored by competitor 1.
     * Scores of type {@link Score#HANSOKU} and other non-valid entries are excluded.
     *
     * @return valid ippon count for competitor 1
     */
    public Integer getCompetitor1ScoreValue() {
        return (int) competitor1Score.stream().filter(Score::isValidPoint).count();
    }

    /**
     * Returns the number of valid ippon points scored by competitor 2.
     *
     * @return valid ippon count for competitor 2
     */
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

    /**
     * Returns whether this duel involves a substitute member.
     * Substitute duels are always marked as finished and do not affect rankings.
     *
     * @return {@code true} if this is a substitute duel; never {@code null}
     */
    public Boolean getSubstitute() {
        if (substitute == null) {
            return false;
        }
        return substitute;
    }

    public void setSubstitute(Boolean substitute) {
        this.substitute = substitute;
    }
}
