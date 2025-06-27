package com.softwaremagico.kt.core.controller.models;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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

import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.DuelType;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.utils.NameUtils;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DuelDTO extends ElementDTO {
    private static final int CHARACTERS_TO_SHOW = 10;

    private ParticipantDTO competitor1;
    private ParticipantDTO competitor2;

    @NotNull
    private TournamentDTO tournament;
    @Size(max = Duel.POINTS_TO_WIN)
    private List<Score> competitor1Score = new ArrayList<>(); // M, K, T, D, H, I
    @Size(max = Duel.POINTS_TO_WIN)
    private List<Score> competitor2Score = new ArrayList<>(); // M, K, T, D, H, I
    private Boolean competitor1Fault = false;
    private Boolean competitor2Fault = false;
    @Size(max = Duel.POINTS_TO_WIN)
    private List<Integer> competitor1ScoreTime = new ArrayList<>();
    @Size(max = Duel.POINTS_TO_WIN)
    private List<Integer> competitor2ScoreTime = new ArrayList<>();
    private Integer competitor1FaultTime;
    private Integer competitor2FaultTime;
    @NotNull
    private DuelType type;
    private boolean finished;
    private Integer duration;
    private Integer totalDuration;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    public DuelDTO() {
        super();
        setType(DuelType.STANDARD);
    }

    public DuelDTO(ParticipantDTO competitor1, ParticipantDTO competitor2, TournamentDTO tournament, String createdBy) {
        this();
        setCompetitor1(competitor1);
        setCompetitor2(competitor2);
        setTournament(tournament);
        setCreatedBy(createdBy);
    }

    public ParticipantDTO getCompetitor1() {
        return competitor1;
    }

    public void setCompetitor1(ParticipantDTO competitor1) {
        this.competitor1 = competitor1;
    }

    public ParticipantDTO getCompetitor2() {
        return competitor2;
    }

    public void setCompetitor2(ParticipantDTO competitor2) {
        this.competitor2 = competitor2;
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

    public DuelType getType() {
        return type;
    }

    public void setType(DuelType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder();
        if (competitor1 != null) {
            text.append(NameUtils.getShortLastnameName(competitor1, CHARACTERS_TO_SHOW)).append(" ");
            if (competitor1Fault != null && competitor1Fault) {
                text.append("^");
            }
            text.append("[");
            for (final Score hitsFromCompetitorA1 : competitor1Score) {
                if (hitsFromCompetitorA1 != null) {
                    text.append(hitsFromCompetitorA1.getAbbreviation());
                }
            }
            text.append("] ");
        } else {
            text.append("  <<Empty>>  []  ");
        }
        if (competitor2 != null) {
            text.append("[");
            for (final Score hitsFromCompetitorB1 : competitor2Score) {
                if (hitsFromCompetitorB1 != null) {
                    text.append(hitsFromCompetitorB1.getAbbreviation());
                }
            }
            text.append("]");
            if (competitor2Fault != null && competitor2Fault) {
                text.append("^");
            }
            text.append(" ");
            text.append(NameUtils.getShortLastnameName(competitor2, CHARACTERS_TO_SHOW));
        } else {
            text.append("[]  <<Empty>>  ");
        }

        return text.toString();
    }

    /**
     * Gets the winner of the duel.
     *
     * @return -1 if player of first team, 0 if draw, 1 if player of second
     * tiem.
     */
    public int getWinner() {
        return Integer.compare(getCompetitor2ScoreValue(), getCompetitor1ScoreValue());
    }

    public Integer getCompetitor1ScoreValue() {
        return (int) competitor1Score.stream().filter(Score::isValidPoint).count();
    }

    public Integer getCompetitor2ScoreValue() {
        return (int) competitor2Score.stream().filter(Score::isValidPoint).count();
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

    public boolean isOver() {
        return getCompetitor1ScoreValue() >= Duel.POINTS_TO_WIN || getCompetitor2ScoreValue() >= Duel.POINTS_TO_WIN || finished;
    }

    public TournamentDTO getTournament() {
        return tournament;
    }

    public void setTournament(TournamentDTO tournament) {
        this.tournament = tournament;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DuelDTO duelDTO)) {
            return false;
        }
        return Objects.equals(getCompetitor1(), duelDTO.getCompetitor1()) && Objects.equals(getCompetitor2(), duelDTO.getCompetitor2())
                && Objects.equals(getCompetitor1Score(), duelDTO.getCompetitor1Score()) && Objects.equals(getCompetitor2Score(),
                duelDTO.getCompetitor2Score()) && Objects.equals(getCompetitor1Fault(), duelDTO.getCompetitor1Fault())
                && Objects.equals(getCompetitor2Fault(), duelDTO.getCompetitor2Fault()) && getType() == duelDTO.getType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCompetitor1(), getCompetitor2(), getCompetitor1Score(), getCompetitor2Score(), getCompetitor1Fault(),
                getCompetitor2Fault(), getType());
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

    public void addCompetitor1Score(Score score) {
        if (this.competitor1Score == null) {
            this.competitor1Score = new ArrayList<>();
        }
        this.competitor1Score.add(score);
    }

    public void addCompetitor2Score(Score score) {
        if (this.competitor2Score == null) {
            this.competitor2Score = new ArrayList<>();
        }
        this.competitor2Score.add(score);
    }

    public void addCompetitor1ScoreTime(int time) {
        if (this.getCompetitor1ScoreTime() == null) {
            this.competitor1ScoreTime = new ArrayList<>();
        }
        this.competitor1ScoreTime.add(time);
    }

    public void addCompetitor2ScoreTime(int time) {
        if (this.getCompetitor2ScoreTime() == null) {
            this.competitor2ScoreTime = new ArrayList<>();
        }
        this.competitor2ScoreTime.add(time);
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
