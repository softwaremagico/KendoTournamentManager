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

import com.softwaremagico.kt.persistence.values.AchievementGrade;
import com.softwaremagico.kt.persistence.values.AchievementType;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.Objects;

@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "achievements", indexes = {
        @Index(name = "ind_participant", columnList = "participant"),
        @Index(name = "ind_tournament", columnList = "tournament"),
})
public class Achievement extends Element {

    @ManyToOne
    @JoinColumn(name = "participant", nullable = false)
    private Participant participant;

    @ManyToOne
    @JoinColumn(name = "tournament", nullable = false)
    private Tournament tournament;

    @Column(name = "achievement_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AchievementType achievementType;

    @Column(name = "achievement_grade", nullable = false)
    @Enumerated(EnumType.STRING)
    private AchievementGrade achievementGrade;

    public Achievement() {
        super();
    }

    public Achievement(Participant participant, Tournament tournament, AchievementType achievementType, AchievementGrade achievementGrade) {
        this();
        setParticipant(participant);
        setTournament(tournament);
        setAchievementType(achievementType);
        setAchievementGrade(achievementGrade);
    }

    public Achievement(Participant participant, Tournament tournament, AchievementType achievementType) {
        this();
        this.participant = participant;
        this.tournament = tournament;
        this.achievementType = achievementType;
    }

    public Participant getParticipant() {
        return participant;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public AchievementType getAchievementType() {
        return achievementType;
    }

    public void setAchievementType(AchievementType achievementType) {
        this.achievementType = achievementType;
    }

    public AchievementGrade getAchievementGrade() {
        return achievementGrade;
    }

    public void setAchievementGrade(AchievementGrade achievementGrade) {
        this.achievementGrade = Objects.requireNonNullElse(achievementGrade, AchievementGrade.NORMAL);
    }

    @Override
    public String toString() {
        return "Achievement{"
                + "participant=" + participant
                + ", tournament=" + tournament
                + ", achievementType=" + achievementType
                + ", achievementGrade=" + achievementGrade
                + "} " + super.toString();
    }

    public String keyByUserAndType() {
        return participant.getId() + "_" + achievementType + "_" + achievementGrade;
    }
}
