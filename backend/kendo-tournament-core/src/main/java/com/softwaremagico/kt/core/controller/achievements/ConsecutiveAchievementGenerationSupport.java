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
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.Achievement;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.AchievementGrade;
import com.softwaremagico.kt.persistence.values.AchievementType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Extends AchievementGenerationSupport with support for consecutive grade achievements
 * (e.g. bronze/silver/gold based on N consecutive tournament occurrences).
 */
abstract class ConsecutiveAchievementGenerationSupport extends AchievementGenerationSupport {

    private final TournamentProvider tournamentProvider;
    private final ParticipantProvider participantProvider;

    protected ConsecutiveAchievementGenerationSupport(AchievementProvider achievementProvider,
                                                      TournamentProvider tournamentProvider,
                                                      ParticipantProvider participantProvider) {
        super(achievementProvider);
        this.tournamentProvider = tournamentProvider;
        this.participantProvider = participantProvider;
    }

    protected TournamentProvider getTournamentProvider() {
        return this.tournamentProvider;
    }

    protected List<Achievement> generateConsecutiveGradeAchievements(Tournament tournament, int consecutiveTournaments,
                                                                     AchievementType achievementType,
                                                                     AchievementGrade achievementGrade) {
        if (achievementGrade == null || achievementGrade.equals(AchievementGrade.NORMAL)) {
            return new ArrayList<>();
        }
        final List<Tournament> previousTournaments = tournamentProvider.getPreviousTo(tournament, consecutiveTournaments - 1);
        previousTournaments.addFirst(tournament);

        if (previousTournaments.size() < consecutiveTournaments) {
            return new ArrayList<>();
        }

        final List<Participant> participants = participantProvider.get(tournament);
        final Map<Participant, List<Achievement>> achievementsByParticipant = getAchievementProvider().get(achievementType,
                        Collections.singletonList(AchievementGrade.NORMAL), participants, previousTournaments).stream()
                .collect(Collectors.groupingBy(Achievement::getParticipant));

        achievementsByParticipant.keySet().removeIf(participant ->
                achievementsByParticipant.get(participant).size() < consecutiveTournaments);

        previousTournaments.forEach(previousTournament -> getAchievementProvider().get(previousTournament, achievementType,
                achievementGrade.getGreaterEqualsThan()).forEach(
                achievementBetterGrade -> achievementsByParticipant.remove(achievementBetterGrade.getParticipant())));

        return generateAchievement(achievementType, achievementGrade, achievementsByParticipant.keySet(), tournament);
    }
}

