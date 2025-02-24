package com.softwaremagico.kt.core.controller.models;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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

import java.io.Serial;

public class AchievementDTO extends ElementDTO {

    @Serial
    private static final long serialVersionUID = 700852091905611286L;

    private ParticipantDTO participant;

    private TournamentDTO tournament;

    private AchievementType achievementType;

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
}
