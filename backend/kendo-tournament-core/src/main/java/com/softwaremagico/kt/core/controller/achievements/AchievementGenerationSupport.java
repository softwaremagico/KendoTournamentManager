package com.softwaremagico.kt.core.controller.achievements;

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

import com.softwaremagico.kt.core.providers.AchievementProvider;
import com.softwaremagico.kt.persistence.entities.Achievement;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.AchievementGrade;
import com.softwaremagico.kt.persistence.values.AchievementType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

abstract class AchievementGenerationSupport {

    protected static final int DAYS_WINDOW_YEAR = 365;

    private final AchievementProvider achievementProvider;

    protected AchievementGenerationSupport(AchievementProvider achievementProvider) {
        this.achievementProvider = achievementProvider;
    }

    protected AchievementProvider getAchievementProvider() {
        return this.achievementProvider;
    }

    protected List<Achievement> generateGradeAchievementsByDays(Tournament tournament, AchievementType achievementType,
                                                                AchievementGrade achievementGrade, int amount, int daysWindow) {
        if (achievementGrade == null || achievementGrade.equals(AchievementGrade.NORMAL) || tournament.getCreatedAt() == null) {
            return new ArrayList<>();
        }
        final List<Achievement> normalAchievements = getAchievementProvider().getAfter(tournament, achievementType,
                AchievementGrade.NORMAL, tournament.getCreatedAt().minusDays(daysWindow));
        final List<Achievement> gradeAchievements = getAchievementProvider().getAfter(tournament, achievementType,
                achievementGrade, tournament.getCreatedAt().minusDays(daysWindow));
        final List<Participant> participantsWithAchievements = normalAchievements.stream().map(Achievement::getParticipant).toList();
        final List<Achievement> generatedAchievements = new ArrayList<>();
        for (final Participant participant : participantsWithAchievements) {
            int counter = 0;
            for (final Achievement normalAchievement : normalAchievements) {
                if (Objects.equals(normalAchievement.getParticipant(), participant)
                        && gradeAchievements.stream().filter(achievement -> Objects.equals(achievement.getParticipant(), participant)
                        && achievement.getCreatedAt().isAfter(normalAchievement.getCreatedAt())).findAny().isEmpty()) {
                    counter++;
                }
            }
            if (counter >= amount) {
                generatedAchievements.addAll(generateAchievement(achievementType, achievementGrade, List.of(participant), tournament));
            }
        }
        return generatedAchievements;
    }

    protected List<Achievement> generateAchievement(AchievementType achievementType, AchievementGrade achievementGrade,
                                                    Collection<Participant> participants, Tournament tournament) {
        if (participants == null || participants.isEmpty()) {
            return new ArrayList<>();
        }
        final List<Achievement> achievements = new ArrayList<>();
        participants.forEach(participant -> {
            final Achievement achievement = new Achievement(participant, tournament, achievementType, achievementGrade);
            if (tournament.getFinishedAt() != null) {
                achievement.setCreatedAt(tournament.getFinishedAt());
            } else {
                achievement.setCreatedAt(tournament.getCreatedAt());
            }
            achievements.add(achievement);
        });
        return getAchievementProvider().saveAll(achievements);
    }
}


