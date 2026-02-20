package com.softwaremagico.kt.core.controller.models;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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

import com.softwaremagico.kt.persistence.values.AchievementGrade;
import com.softwaremagico.kt.persistence.values.AchievementType;
import jakarta.validation.constraints.NotNull;

import java.io.Serial;

public class AchievementDTO extends ElementDTO {

    @Serial
    private static final long serialVersionUID = 700852091905611286L;

    private static final int HASH_MAGIC = 31;

    @NotNull
    private ParticipantDTO participant;

    private TournamentDTO tournament;

    @NotNull
    private AchievementType achievementType;

    @NotNull
    private AchievementGrade achievementGrade;

    public ParticipantDTO getParticipant() {
        return participant;
    }

    public void setParticipant(ParticipantDTO participant) {
        this.participant = participant;
    }

    public TournamentDTO getTournament() {
        return tournament;
    }

    public void setTournament(TournamentDTO tournament) {
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
        this.achievementGrade = achievementGrade;
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

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof AchievementDTO that)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        return participant.equals(that.participant) && tournament.equals(that.tournament) && achievementType == that.achievementType
                && achievementGrade == that.achievementGrade;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = HASH_MAGIC * result + participant.hashCode();
        result = HASH_MAGIC * result + tournament.hashCode();
        result = HASH_MAGIC * result + achievementType.hashCode();
        result = HASH_MAGIC * result + achievementGrade.hashCode();
        return result;
    }
}
