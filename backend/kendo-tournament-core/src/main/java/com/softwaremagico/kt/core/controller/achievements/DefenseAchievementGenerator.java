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
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DefenseAchievementGenerator extends AchievementGenerationSupport {

    private static final int DEFAULT_SCORE_BRONZE = 30;
    private static final int DEFAULT_SCORE_SILVER = 60;
    private static final int DEFAULT_SCORE_GOLD = 100;

    public DefenseAchievementGenerator(AchievementProvider achievementProvider) {
        super(achievementProvider);
    }

    public List<Achievement> generateTisButAScratchAchievement(Tournament tournament,
                                                               Map<Participant, Long> totalScoreAgainstParticipant) {
        final List<Participant> participantsFirstScore = new ArrayList<>();
        final List<Participant> alreadyTisButAScratchAchievements = getAchievementProvider().get(AchievementType.TIS_BUT_A_SCRATCH, AchievementGrade.NORMAL)
                .stream().map(Achievement::getParticipant).toList();
        totalScoreAgainstParticipant.forEach((participant, score) -> {
            if (score > 0 && !alreadyTisButAScratchAchievements.contains(participant)) {
                participantsFirstScore.add(participant);
            }
        });
        return generateAchievement(AchievementType.TIS_BUT_A_SCRATCH, AchievementGrade.NORMAL, participantsFirstScore, tournament);
    }

    public List<Achievement> generateTisButAScratchAchievementBronze(Tournament tournament,
                                                                     Map<Participant, Long> totalScoreAgainstParticipant) {
        final List<Participant> participantsFirstScore = new ArrayList<>();
        final List<Participant> alreadyTisButAScratchAchievements = getAchievementProvider().get(AchievementType.TIS_BUT_A_SCRATCH, AchievementGrade.BRONZE)
                .stream().map(Achievement::getParticipant).toList();
        totalScoreAgainstParticipant.forEach((participant, score) -> {
            if (score >= DEFAULT_SCORE_BRONZE && !alreadyTisButAScratchAchievements.contains(participant)) {
                participantsFirstScore.add(participant);
            }
        });
        return generateAchievement(AchievementType.TIS_BUT_A_SCRATCH, AchievementGrade.BRONZE, participantsFirstScore, tournament);
    }

    public List<Achievement> generateTisButAScratchAchievementSilver(Tournament tournament,
                                                                     Map<Participant, Long> totalScoreAgainstParticipant) {
        final List<Participant> participantsFirstScore = new ArrayList<>();
        final List<Participant> alreadyTisButAScratchAchievements = getAchievementProvider().get(AchievementType.TIS_BUT_A_SCRATCH, AchievementGrade.SILVER)
                .stream().map(Achievement::getParticipant).toList();
        totalScoreAgainstParticipant.forEach((participant, score) -> {
            if (score >= DEFAULT_SCORE_SILVER && !alreadyTisButAScratchAchievements.contains(participant)) {
                participantsFirstScore.add(participant);
            }
        });
        return generateAchievement(AchievementType.TIS_BUT_A_SCRATCH, AchievementGrade.SILVER, participantsFirstScore, tournament);
    }

    public List<Achievement> generateTisButAScratchAchievementGold(Tournament tournament,
                                                                   Map<Participant, Long> totalScoreAgainstParticipant) {
        final List<Participant> participantsFirstScore = new ArrayList<>();
        final List<Participant> alreadyTisButAScratchAchievements = getAchievementProvider().get(AchievementType.TIS_BUT_A_SCRATCH, AchievementGrade.GOLD)
                .stream().map(Achievement::getParticipant).toList();
        totalScoreAgainstParticipant.forEach((participant, score) -> {
            if (score >= DEFAULT_SCORE_GOLD && !alreadyTisButAScratchAchievements.contains(participant)) {
                participantsFirstScore.add(participant);
            }
        });
        return generateAchievement(AchievementType.TIS_BUT_A_SCRATCH, AchievementGrade.GOLD, participantsFirstScore, tournament);
    }

    public List<Achievement> generateFirstBloodAchievement(Tournament tournament, Map<Participant, Long> totalScoreFromParticipant) {
        final List<Participant> participantsFirstScore = new ArrayList<>();
        final List<Participant> alreadyFirstBloodAchievement = getAchievementProvider().get(AchievementType.FIRST_BLOOD, AchievementGrade.NORMAL)
                .stream().map(Achievement::getParticipant).toList();
        totalScoreFromParticipant.forEach((participant, score) -> {
            if (score > 0 && !alreadyFirstBloodAchievement.contains(participant)) {
                participantsFirstScore.add(participant);
            }
        });
        return generateAchievement(AchievementType.FIRST_BLOOD, AchievementGrade.NORMAL, participantsFirstScore, tournament);
    }

    public List<Achievement> generateFirstBloodAchievementBronze(Tournament tournament,
                                                                 Map<Participant, Long> totalScoreFromParticipant) {
        final List<Participant> participantsFirstScore = new ArrayList<>();
        final List<Participant> alreadyFirstBloodAchievement = getAchievementProvider().get(AchievementType.FIRST_BLOOD, AchievementGrade.BRONZE)
                .stream().map(Achievement::getParticipant).toList();
        totalScoreFromParticipant.forEach((participant, score) -> {
            if (score >= DEFAULT_SCORE_BRONZE && !alreadyFirstBloodAchievement.contains(participant)) {
                participantsFirstScore.add(participant);
            }
        });
        return generateAchievement(AchievementType.FIRST_BLOOD, AchievementGrade.BRONZE, participantsFirstScore, tournament);
    }

    public List<Achievement> generateFirstBloodAchievementSilver(Tournament tournament,
                                                                 Map<Participant, Long> totalScoreFromParticipant) {
        final List<Participant> participantsFirstScore = new ArrayList<>();
        final List<Participant> alreadyFirstBloodAchievement = getAchievementProvider().get(AchievementType.FIRST_BLOOD, AchievementGrade.SILVER)
                .stream().map(Achievement::getParticipant).toList();
        totalScoreFromParticipant.forEach((participant, score) -> {
            if (score >= DEFAULT_SCORE_SILVER && !alreadyFirstBloodAchievement.contains(participant)) {
                participantsFirstScore.add(participant);
            }
        });
        return generateAchievement(AchievementType.FIRST_BLOOD, AchievementGrade.SILVER, participantsFirstScore, tournament);
    }

    public List<Achievement> generateFirstBloodAchievementGold(Tournament tournament,
                                                               Map<Participant, Long> totalScoreFromParticipant) {
        final List<Participant> participantsFirstScore = new ArrayList<>();
        final List<Participant> alreadyFirstBloodAchievement = getAchievementProvider().get(AchievementType.FIRST_BLOOD, AchievementGrade.GOLD)
                .stream().map(Achievement::getParticipant).toList();
        totalScoreFromParticipant.forEach((participant, score) -> {
            if (score >= DEFAULT_SCORE_GOLD && !alreadyFirstBloodAchievement.contains(participant)) {
                participantsFirstScore.add(participant);
            }
        });
        return generateAchievement(AchievementType.FIRST_BLOOD, AchievementGrade.GOLD, participantsFirstScore, tournament);
    }
}


