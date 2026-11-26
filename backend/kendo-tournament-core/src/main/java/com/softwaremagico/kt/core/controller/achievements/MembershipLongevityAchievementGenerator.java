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
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.persistence.entities.Achievement;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.AchievementGrade;
import com.softwaremagico.kt.persistence.values.AchievementType;

import java.util.ArrayList;
import java.util.List;

public class MembershipLongevityAchievementGenerator extends AchievementGenerationSupport {

    private final ParticipantProvider participantProvider;

    public MembershipLongevityAchievementGenerator(AchievementProvider achievementProvider, ParticipantProvider participantProvider) {
        super(achievementProvider);
        this.participantProvider = participantProvider;
    }

    public List<Achievement> generateNeverEndingStoryAchievement(Tournament tournament, AchievementGrade achievementGrade, int years) {
        if (tournament.getCreatedAt() == null) {
            return new ArrayList<>();
        }
        final List<Participant> participants = this.participantProvider.get(tournament).stream().filter(participant ->
                participant.getCreatedAt().isBefore(tournament.getCreatedAt().minusYears(years))).toList();
        final List<Participant> eligibleParticipants = new ArrayList<>(participants);
        final List<Participant> participantsWithThisAchievement = getAchievementProvider().get(AchievementType.THE_NEVER_ENDING_STORY, achievementGrade)
                .stream().map(Achievement::getParticipant).toList();
        eligibleParticipants.removeAll(participantsWithThisAchievement);
        return generateAchievement(AchievementType.THE_NEVER_ENDING_STORY, achievementGrade, eligibleParticipants, tournament);
    }
}

