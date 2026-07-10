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
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
        final Map<Participant, Long> tournamentDuration = this.getAccumulatedDurations(tournament);
        final Set<Participant> normalAchievements = this.getParticipantsExceedingDuration(
                tournamentDuration,
                LONG_PATH_NORMAL_DURATION,
                this.getParticipantsWithAchievement(AchievementGrade.NORMAL));
        final Set<Participant> bronzeAchievements = this.getParticipantsExceedingDuration(
                tournamentDuration,
                LONG_PATH_BRONZE_DURATION,
                this.getParticipantsWithAchievement(AchievementGrade.BRONZE));
        final Set<Participant> silverAchievements = this.getParticipantsExceedingDuration(
                tournamentDuration,
                LONG_PATH_SILVER_DURATION,
                this.getParticipantsWithAchievement(AchievementGrade.SILVER));
        final Set<Participant> goldAchievements = this.getParticipantsExceedingDuration(
                tournamentDuration,
                LONG_PATH_GOLD_DURATION,
                this.getParticipantsWithAchievement(AchievementGrade.GOLD));

        final List<Achievement> achievements = new ArrayList<>();
        achievements.addAll(this.generateAchievement(AchievementType.LONG_PATH, AchievementGrade.NORMAL, normalAchievements,
                tournament));
        achievements.addAll(this.generateAchievement(AchievementType.LONG_PATH, AchievementGrade.BRONZE, bronzeAchievements,
                tournament));
        achievements.addAll(this.generateAchievement(AchievementType.LONG_PATH, AchievementGrade.SILVER, silverAchievements,
                tournament));
        achievements.addAll(this.generateAchievement(AchievementType.LONG_PATH, AchievementGrade.GOLD, goldAchievements,
                tournament));
        return achievements;
    }

    private Map<Participant, Long> getAccumulatedDurations(Tournament tournament) {
        final List<Tournament> previousTournaments = this.tournamentProvider.getPreviousTo(tournament);
        previousTournaments.addFirst(tournament);
        final Map<Participant, Long> tournamentDuration = new HashMap<>();
        for (final Tournament oldTournament : previousTournaments) {
            final Optional<Long> durationOfTournament = this.getTournamentDuration(oldTournament);
            if (durationOfTournament.isEmpty()) {
                continue;
            }
            final List<Participant> participants = this.participantProvider.get(tournament);
            participants.forEach(participant -> tournamentDuration.merge(participant, durationOfTournament.get(), Long::sum));
        }
        return tournamentDuration;
    }

    @SuppressWarnings("java:S7467")
    private Optional<Long> getTournamentDuration(Tournament tournament) {
        final List<Duel> duels = this.duelProvider.get(tournament);
        if (duels.isEmpty()) {
            return Optional.empty();
        }
        LocalDateTime startingTime = LocalDateTime.MAX;
        LocalDateTime endingTime = LocalDateTime.MIN;
        for (final Duel duel : duels) {
            if (duel.getStartedAt() != null && duel.getStartedAt().isBefore(startingTime)) {
                startingTime = duel.getStartedAt();
            }
            if (duel.getFinishedAt() != null && duel.getFinishedAt().isAfter(endingTime)) {
                endingTime = duel.getFinishedAt();
            }
        }
        try {
            return Optional.of(endingTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    - startingTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        } catch (final ArithmeticException ignoredException) {
            // Ignore invalid dates
            return Optional.empty();
        }
    }

    private Set<Participant> getParticipantsWithAchievement(AchievementGrade grade) {
        return this.getAchievementProvider().get(AchievementType.LONG_PATH, grade).stream()
                .map(Achievement::getParticipant)
                .collect(Collectors.toSet());
    }

    private Set<Participant> getParticipantsExceedingDuration(Map<Participant, Long> tournamentDuration,
                                                              long requiredDuration,
                                                              Set<Participant> alreadyAwardedParticipants) {
        final Set<Participant> participants = new HashSet<>();
        for (final Map.Entry<Participant, Long> duration : tournamentDuration.entrySet()) {
            if (duration.getValue() > requiredDuration && !alreadyAwardedParticipants.contains(duration.getKey())) {
                participants.add(duration.getKey());
            }
        }
        return participants;
    }
}
