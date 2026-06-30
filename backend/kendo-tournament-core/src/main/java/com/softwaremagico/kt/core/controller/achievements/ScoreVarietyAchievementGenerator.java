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
import com.softwaremagico.kt.persistence.values.Score;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ScoreVarietyAchievementGenerator extends AchievementGenerationSupport {

    public ScoreVarietyAchievementGenerator(AchievementProvider achievementProvider) {
        super(achievementProvider);
    }

    public List<Achievement> generateALittleOfEverythingAchievement(Tournament tournament,
                                                                    Map<Participant, List<Score>> scoresByParticipant) {
        final List<Participant> participants = new ArrayList<>();
        scoresByParticipant.keySet().forEach(participant -> {
            if (scoresByParticipant.get(participant).contains(Score.MEN)
                    && scoresByParticipant.get(participant).contains(Score.KOTE)
                    && scoresByParticipant.get(participant).contains(Score.DO)) {
                participants.add(participant);
            }
        });
        return generateAchievement(AchievementType.A_LITTLE_OF_EVERYTHING, AchievementGrade.BRONZE, participants, tournament);
    }

    public List<Achievement> generateALittleOfEverythingAchievementBronze(Tournament tournament,
                                                                          Map<Participant, List<Score>> scoresByParticipant) {
        final List<Participant> participants = new ArrayList<>();
        scoresByParticipant.keySet().forEach(participant -> {
            if (scoresByParticipant.get(participant).contains(Score.MEN)
                    && scoresByParticipant.get(participant).contains(Score.KOTE)
                    && scoresByParticipant.get(participant).contains(Score.DO)
                    && scoresByParticipant.get(participant).contains(Score.TSUKI)) {
                participants.add(participant);
            }
        });
        return generateAchievement(AchievementType.A_LITTLE_OF_EVERYTHING, AchievementGrade.BRONZE, participants, tournament);
    }

    public List<Achievement> generateALittleOfEverythingAchievementSilver(Tournament tournament,
                                                                          Map<Participant, List<Score>> scoresByParticipant) {
        final List<Participant> participants = new ArrayList<>();
        scoresByParticipant.keySet().forEach(participant -> {
            if (scoresByParticipant.get(participant).contains(Score.MEN)
                    && scoresByParticipant.get(participant).contains(Score.KOTE)
                    && scoresByParticipant.get(participant).contains(Score.DO)
                    && scoresByParticipant.get(participant).contains(Score.TSUKI)
                    && scoresByParticipant.get(participant).contains(Score.HANSOKU)) {
                participants.add(participant);
            }
        });
        return generateAchievement(AchievementType.A_LITTLE_OF_EVERYTHING, AchievementGrade.SILVER, participants, tournament);
    }

    public List<Achievement> generateALittleOfEverythingAchievementGold(Tournament tournament,
                                                                        Map<Participant, List<Score>> scoresByParticipant) {
        final List<Participant> participants = new ArrayList<>();
        scoresByParticipant.keySet().forEach(participant -> {
            if (scoresByParticipant.get(participant).contains(Score.MEN)
                    && scoresByParticipant.get(participant).contains(Score.KOTE)
                    && scoresByParticipant.get(participant).contains(Score.DO)
                    && scoresByParticipant.get(participant).contains(Score.TSUKI)
                    && scoresByParticipant.get(participant).contains(Score.HANSOKU)
                    && (scoresByParticipant.get(participant).contains(Score.IPPON)
                    || scoresByParticipant.get(participant).contains(Score.FUSEN_GACHI))) {
                participants.add(participant);
            }
        });
        return generateAchievement(AchievementType.A_LITTLE_OF_EVERYTHING, AchievementGrade.GOLD, participants, tournament);
    }
}

