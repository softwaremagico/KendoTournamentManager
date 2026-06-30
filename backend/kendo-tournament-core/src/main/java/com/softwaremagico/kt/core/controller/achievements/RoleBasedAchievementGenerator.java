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
import com.softwaremagico.kt.persistence.values.RoleType;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RoleBasedAchievementGenerator extends ConsecutiveAchievementGenerationSupport {

    private static final int DEFAULT_LONG_NUMBER_BRONZE = 3;
    private static final int DEFAULT_LONG_NUMBER_SILVER = 5;
    private static final int DEFAULT_LONG_NUMBER_GOLD = 7;

    private final ParticipantProvider participantProviderField;

    public RoleBasedAchievementGenerator(AchievementProvider achievementProvider, ParticipantProvider participantProvider,
                                         TournamentProvider tournamentProvider) {
        super(achievementProvider, tournamentProvider, participantProvider);
        this.participantProviderField = participantProvider;
    }

    public List<Achievement> generateLooksGoodFromFarAwayButAchievement(Tournament tournament) {
        final List<Participant> participants = participantProviderField.get(tournament, RoleType.ORGANIZER);
        return generateAchievement(AchievementType.LOOKS_GOOD_FROM_FAR_AWAY_BUT, AchievementGrade.NORMAL, participants, tournament);
    }

    public List<Achievement> generateLooksGoodFromFarAwayButAchievementBronze(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_LONG_NUMBER_BRONZE,
                AchievementType.LOOKS_GOOD_FROM_FAR_AWAY_BUT, AchievementGrade.BRONZE);
    }

    public List<Achievement> generateLooksGoodFromFarAwayButAchievementSilver(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_LONG_NUMBER_SILVER,
                AchievementType.LOOKS_GOOD_FROM_FAR_AWAY_BUT, AchievementGrade.SILVER);
    }

    public List<Achievement> generateLooksGoodFromFarAwayButAchievementGold(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_LONG_NUMBER_GOLD,
                AchievementType.LOOKS_GOOD_FROM_FAR_AWAY_BUT, AchievementGrade.GOLD);
    }

    public List<Achievement> generateILoveTheFlagsAchievement(Tournament tournament) {
        final List<Participant> participants = participantProviderField.get(tournament, RoleType.REFEREE);
        return generateAchievement(AchievementType.I_LOVE_THE_FLAGS, AchievementGrade.NORMAL, participants, tournament);
    }

    public List<Achievement> generateILoveTheFlagsAchievementBronze(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_LONG_NUMBER_BRONZE,
                AchievementType.I_LOVE_THE_FLAGS, AchievementGrade.BRONZE);
    }

    public List<Achievement> generateILoveTheFlagsAchievementSilver(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_LONG_NUMBER_SILVER,
                AchievementType.I_LOVE_THE_FLAGS, AchievementGrade.SILVER);
    }

    public List<Achievement> generateILoveTheFlagsAchievementGold(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_LONG_NUMBER_GOLD,
                AchievementType.I_LOVE_THE_FLAGS, AchievementGrade.GOLD);
    }

    public List<Achievement> generateLoveSharingAchievement(Tournament tournament) {
        final List<Participant> participants = participantProviderField.get(tournament, RoleType.VOLUNTEER);
        return generateAchievement(AchievementType.LOVE_SHARING, AchievementGrade.NORMAL, participants, tournament);
    }

    public List<Achievement> generateLoveSharingAchievementBronze(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_LONG_NUMBER_BRONZE,
                AchievementType.LOVE_SHARING, AchievementGrade.BRONZE);
    }

    public List<Achievement> generateLoveSharingAchievementSilver(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_LONG_NUMBER_SILVER,
                AchievementType.LOVE_SHARING, AchievementGrade.SILVER);
    }

    public List<Achievement> generateLoveSharingAchievementGold(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_LONG_NUMBER_GOLD,
                AchievementType.LOVE_SHARING, AchievementGrade.GOLD);
    }
}

