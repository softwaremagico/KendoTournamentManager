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
import com.softwaremagico.kt.core.providers.DuelProvider;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.Achievement;
import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.AchievementGrade;
import com.softwaremagico.kt.persistence.values.AchievementType;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class DurationAchievementGenerator extends AchievementGenerationSupport {

    private static final int LONG_PATH_NORMAL_DURATION = 100 * 60 * 60 * 1000;
    private static final int LONG_PATH_BRONZE_DURATION = 150 * 60 * 60 * 1000;
    private static final int LONG_PATH_SILVER_DURATION = 200 * 60 * 60 * 1000;
    private static final int LONG_PATH_GOLD_DURATION = 250 * 60 * 60 * 1000;

    private final TournamentProvider tournamentProvider;
    private final ParticipantProvider participantProvider;
    private final DuelProvider duelProvider;

    public DurationAchievementGenerator(AchievementProvider achievementProvider, TournamentProvider tournamentProvider,
                                        ParticipantProvider participantProvider, DuelProvider duelProvider) {
        super(achievementProvider);
        this.tournamentProvider = tournamentProvider;
        this.participantProvider = participantProvider;
        this.duelProvider = duelProvider;
    }

    public List<Achievement> generateLongPathAchievement(Tournament tournament) {
        final List<Achievement> achievements = new ArrayList<>();
        final List<Tournament> previousTournaments = tournamentProvider.getPreviousTo(tournament);
        previousTournaments.addFirst(tournament);
        final Map<Participant, Long> tournamentDuration = new HashMap<>();
        for (Tournament oldTournament : previousTournaments) {
            final List<Duel> duels = duelProvider.get(oldTournament);
            if (duels.isEmpty()) {
                continue;
            }
            LocalDateTime startingTime = LocalDateTime.MAX;
            LocalDateTime endingTime = LocalDateTime.MIN;
            for (Duel duel : duels) {
                if (duel.getStartedAt() != null && duel.getStartedAt().isBefore(startingTime)) {
                    startingTime = duel.getStartedAt();
                }
                if (duel.getFinishedAt() != null && duel.getFinishedAt().isAfter(endingTime)) {
                    endingTime = duel.getFinishedAt();
                }
            }
            try {
                final long durationOfTournament = endingTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        - startingTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                final List<Participant> participants = participantProvider.get(tournament);
                participants.forEach(participant -> tournamentDuration.merge(participant, durationOfTournament, Long::sum));
            } catch (ArithmeticException e) {
                // Ignore invalid dates
            }
        }

        final Set<Participant> alreadyWithNormalAchievements = getAchievementProvider().get(AchievementType.LONG_PATH, AchievementGrade.NORMAL)
                .stream().map(Achievement::getParticipant).collect(Collectors.toSet());
        final Set<Participant> alreadyWithBronzeAchievements = getAchievementProvider().get(AchievementType.LONG_PATH, AchievementGrade.BRONZE)
                .stream().map(Achievement::getParticipant).collect(Collectors.toSet());
        final Set<Participant> alreadyWithSilverAchievements = getAchievementProvider().get(AchievementType.LONG_PATH, AchievementGrade.SILVER)
                .stream().map(Achievement::getParticipant).collect(Collectors.toSet());
        final Set<Participant> alreadyWithGoldAchievements = getAchievementProvider().get(AchievementType.LONG_PATH, AchievementGrade.GOLD)
                .stream().map(Achievement::getParticipant).collect(Collectors.toSet());

        final Set<Participant> normalAchievements = new HashSet<>();
        final Set<Participant> bronzeAchievements = new HashSet<>();
        final Set<Participant> silverAchievements = new HashSet<>();
        final Set<Participant> goldAchievements = new HashSet<>();

        for (Map.Entry<Participant, Long> duration : tournamentDuration.entrySet()) {
            if (duration.getValue() > LONG_PATH_NORMAL_DURATION && !alreadyWithNormalAchievements.contains(duration.getKey())) {
                normalAchievements.add(duration.getKey());
            }
            if (duration.getValue() > LONG_PATH_BRONZE_DURATION && !alreadyWithBronzeAchievements.contains(duration.getKey())) {
                bronzeAchievements.add(duration.getKey());
            }
            if (duration.getValue() > LONG_PATH_SILVER_DURATION && !alreadyWithSilverAchievements.contains(duration.getKey())) {
                silverAchievements.add(duration.getKey());
            }
            if (duration.getValue() > LONG_PATH_GOLD_DURATION && !alreadyWithGoldAchievements.contains(duration.getKey())) {
                goldAchievements.add(duration.getKey());
            }
        }

        achievements.addAll(generateAchievement(AchievementType.LONG_PATH, AchievementGrade.NORMAL, normalAchievements, tournament));
        achievements.addAll(generateAchievement(AchievementType.LONG_PATH, AchievementGrade.BRONZE, bronzeAchievements, tournament));
        achievements.addAll(generateAchievement(AchievementType.LONG_PATH, AchievementGrade.SILVER, silverAchievements, tournament));
        achievements.addAll(generateAchievement(AchievementType.LONG_PATH, AchievementGrade.GOLD, goldAchievements, tournament));

        return achievements;
    }
}


