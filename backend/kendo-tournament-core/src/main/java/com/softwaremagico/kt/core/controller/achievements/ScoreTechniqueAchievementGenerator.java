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
import com.softwaremagico.kt.core.providers.FightProvider;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.core.providers.RoleProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.Achievement;
import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.AchievementGrade;
import com.softwaremagico.kt.persistence.values.AchievementType;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.Score;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScoreTechniqueAchievementGenerator extends ConsecutiveAchievementGenerationSupport {

    private static final int DEFAULT_NUMBER_BRONZE = 2;
    private static final int DEFAULT_NUMBER_SILVER = 3;
    private static final int DEFAULT_NUMBER_GOLD = 5;

    private final FightProvider fightProvider;
    private final DuelProvider duelProvider;
    private final RoleProvider roleProvider;
    private final int minimumTournamentFights;

    public ScoreTechniqueAchievementGenerator(AchievementProvider achievementProvider, FightProvider fightProvider,
                                              DuelProvider duelProvider, RoleProvider roleProvider,
                                              TournamentProvider tournamentProvider, ParticipantProvider participantProvider,
                                              int minimumTournamentFights) {
        super(achievementProvider, tournamentProvider, participantProvider);
        this.fightProvider = fightProvider;
        this.duelProvider = duelProvider;
        this.roleProvider = roleProvider;
        this.minimumTournamentFights = minimumTournamentFights;
    }

    public List<Achievement> generateWoodcutterAchievement(Tournament tournament) {
        return generateAchievement(AchievementType.WOODCUTTER, AchievementGrade.NORMAL, generateScoreBasedAchievement(tournament, Score.DO), tournament);
    }

    public List<Achievement> generateHeadShotAchievement(Tournament tournament) {
        return generateAchievement(AchievementType.HEAD_SHOT, AchievementGrade.NORMAL, generateScoreBasedAchievement(tournament, Score.MEN), tournament);
    }

    public List<Achievement> generateYouAreUnderArrestAchievement(Tournament tournament) {
        return generateAchievement(AchievementType.YOU_ARE_UNDER_ARREST, AchievementGrade.NORMAL,
                generateScoreBasedAchievement(tournament, Score.KOTE), tournament);
    }

    private Set<Participant> generateScoreBasedAchievement(Tournament tournament, Score scoreToCompare) {
        if (this.fightProvider.getFights(tournament).size() < this.minimumTournamentFights) {
            return new HashSet<>();
        }
        final List<Duel> duels = new ArrayList<>(this.duelProvider.get(tournament));
        final Set<Participant> participantsWithScore = new HashSet<>();
        duels.forEach(duel -> participantsWithScore.addAll(duel.getCompetitors()));

        this.roleProvider.getAll(tournament).forEach(role -> {
            if (role.getRoleType() != RoleType.COMPETITOR) {
                participantsWithScore.remove(role.getParticipant());
            }
        });

        duels.forEach(duel -> {
            if (duel.getCompetitor1Score().isEmpty()) {
                participantsWithScore.remove(duel.getCompetitor1());
            }
            duel.getCompetitor1Score().forEach(score -> {
                if (score != scoreToCompare) {
                    participantsWithScore.remove(duel.getCompetitor1());
                }
            });
            if (duel.getCompetitor2Score().isEmpty()) {
                participantsWithScore.remove(duel.getCompetitor2());
            }
            duel.getCompetitor2Score().forEach(score -> {
                if (score != scoreToCompare) {
                    participantsWithScore.remove(duel.getCompetitor2());
                }
            });
        });

        return participantsWithScore;
    }

    public List<Achievement> generateWoodcutterAchievementBronze(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_NUMBER_BRONZE, AchievementType.WOODCUTTER, AchievementGrade.BRONZE);
    }

    public List<Achievement> generateWoodcutterAchievementSilver(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_NUMBER_SILVER, AchievementType.WOODCUTTER, AchievementGrade.SILVER);
    }

    public List<Achievement> generateWoodcutterAchievementGold(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_NUMBER_GOLD, AchievementType.WOODCUTTER, AchievementGrade.GOLD);
    }

    public List<Achievement> generateHeadShotAchievementBronze(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_NUMBER_BRONZE, AchievementType.HEAD_SHOT, AchievementGrade.BRONZE);
    }

    public List<Achievement> generateHeadShotAchievementSilver(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_NUMBER_SILVER, AchievementType.HEAD_SHOT, AchievementGrade.SILVER);
    }

    public List<Achievement> generateHeadShotAchievementGold(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_NUMBER_GOLD, AchievementType.HEAD_SHOT, AchievementGrade.GOLD);
    }

    public List<Achievement> generateYouAreUnderArrestAchievementBronze(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_NUMBER_BRONZE, AchievementType.YOU_ARE_UNDER_ARREST, AchievementGrade.BRONZE);
    }

    public List<Achievement> generateYouAreUnderArrestAchievementSilver(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_NUMBER_SILVER, AchievementType.YOU_ARE_UNDER_ARREST, AchievementGrade.SILVER);
    }

    public List<Achievement> generateYouAreUnderArrestAchievementGold(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_NUMBER_GOLD, AchievementType.YOU_ARE_UNDER_ARREST, AchievementGrade.GOLD);
    }
}

