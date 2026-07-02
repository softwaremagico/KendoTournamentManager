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
import java.util.OptionalLong;
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
        final Map<Participant, Long> tournamentDuration = calculateTournamentDurations(tournament);

        final Set<Participant> alreadyWithNormalAchievements = getParticipantsWithAchievement(AchievementGrade.NORMAL);
        final Set<Participant> alreadyWithBronzeAchievements = getParticipantsWithAchievement(AchievementGrade.BRONZE);
        final Set<Participant> alreadyWithSilverAchievements = getParticipantsWithAchievement(AchievementGrade.SILVER);
        final Set<Participant> alreadyWithGoldAchievements = getParticipantsWithAchievement(AchievementGrade.GOLD);

        final Set<Participant> normalAchievements = new HashSet<>();
        final Set<Participant> bronzeAchievements = new HashSet<>();
        final Set<Participant> silverAchievements = new HashSet<>();
        final Set<Participant> goldAchievements = new HashSet<>();

        addQualifiedParticipants(tournamentDuration, LONG_PATH_NORMAL_DURATION, alreadyWithNormalAchievements, normalAchievements);
        addQualifiedParticipants(tournamentDuration, LONG_PATH_BRONZE_DURATION, alreadyWithBronzeAchievements, bronzeAchievements);
        addQualifiedParticipants(tournamentDuration, LONG_PATH_SILVER_DURATION, alreadyWithSilverAchievements, silverAchievements);
        addQualifiedParticipants(tournamentDuration, LONG_PATH_GOLD_DURATION, alreadyWithGoldAchievements, goldAchievements);

        achievements.addAll(generateAchievement(AchievementType.LONG_PATH, AchievementGrade.NORMAL, normalAchievements, tournament));
        achievements.addAll(generateAchievement(AchievementType.LONG_PATH, AchievementGrade.BRONZE, bronzeAchievements, tournament));
        achievements.addAll(generateAchievement(AchievementType.LONG_PATH, AchievementGrade.SILVER, silverAchievements, tournament));
        achievements.addAll(generateAchievement(AchievementType.LONG_PATH, AchievementGrade.GOLD, goldAchievements, tournament));

        return achievements;
    }

    private Map<Participant, Long> calculateTournamentDurations(Tournament tournament) {
        final List<Tournament> previousTournaments = tournamentProvider.getPreviousTo(tournament);
        previousTournaments.addFirst(tournament);
        final Map<Participant, Long> tournamentDuration = new HashMap<>();
        for (Tournament oldTournament : previousTournaments) {
            addTournamentDuration(tournament, oldTournament, tournamentDuration);
        }
        return tournamentDuration;
    }

    private void addTournamentDuration(Tournament tournament, Tournament oldTournament, Map<Participant, Long> tournamentDuration) {
        final List<Duel> duels = duelProvider.get(oldTournament);
        if (duels.isEmpty()) {
            return;
        }

        final OptionalLong durationOfTournament = getDurationOfTournament(duels);
        if (durationOfTournament.isEmpty()) {
            return;
        }

        final List<Participant> participants = participantProvider.get(tournament);
        participants.forEach(participant -> tournamentDuration.merge(participant, durationOfTournament.getAsLong(), Long::sum));
    }

    private OptionalLong getDurationOfTournament(List<Duel> duels) {
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
            return OptionalLong.of(endingTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    - startingTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        } catch (ArithmeticException e) {
            return OptionalLong.empty();
        }
    }

    private Set<Participant> getParticipantsWithAchievement(AchievementGrade achievementGrade) {
        return getAchievementProvider().get(AchievementType.LONG_PATH, achievementGrade)
                .stream().map(Achievement::getParticipant).collect(Collectors.toSet());
    }

    private void addQualifiedParticipants(Map<Participant, Long> tournamentDuration, long durationThreshold,
                                          Set<Participant> alreadyWithAchievements, Set<Participant> qualifiedParticipants) {
        for (Map.Entry<Participant, Long> duration : tournamentDuration.entrySet()) {
            if (duration.getValue() > durationThreshold && !alreadyWithAchievements.contains(duration.getKey())) {
                qualifiedParticipants.add(duration.getKey());
            }
        }
    }
}


