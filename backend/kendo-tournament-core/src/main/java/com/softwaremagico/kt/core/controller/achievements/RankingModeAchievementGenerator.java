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
import com.softwaremagico.kt.core.providers.RankingProvider;
import com.softwaremagico.kt.core.score.ScoreOfCompetitor;
import com.softwaremagico.kt.persistence.entities.Achievement;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.AchievementGrade;
import com.softwaremagico.kt.persistence.values.AchievementType;
import com.softwaremagico.kt.persistence.values.TournamentType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RankingModeAchievementGenerator extends AchievementGenerationSupport {

    private final RankingProvider rankingProvider;

    public RankingModeAchievementGenerator(AchievementProvider achievementProvider, RankingProvider rankingProvider) {
        super(achievementProvider);
        this.rankingProvider = rankingProvider;
    }

    public List<Achievement> generateTheKingAchievement(Tournament tournament) {
        if (tournament.getType() == TournamentType.KING_OF_THE_MOUNTAIN) {
            final List<ScoreOfCompetitor> scoreOfCompetitors = this.rankingProvider.getCompetitorsScoreRanking(tournament);
            if (!scoreOfCompetitors.isEmpty()) {
                return generateAchievement(AchievementType.THE_KING, AchievementGrade.NORMAL,
                        Collections.singletonList(scoreOfCompetitors.getFirst().getCompetitor()), tournament);
            }
        }
        return new ArrayList<>();
    }

    public List<Achievement> generateTheKingAchievementGrade(Tournament tournament, AchievementGrade achievementGrade, int amount) {
        return generateGradeAchievementsByDays(tournament, AchievementType.THE_KING, achievementGrade, amount, DAYS_WINDOW_YEAR);
    }

    public List<Achievement> generateMasterTheLoopAchievement(Tournament tournament) {
        if (tournament.getType() == TournamentType.LOOP) {
            final List<ScoreOfCompetitor> scoreOfCompetitors = this.rankingProvider.getCompetitorsScoreRanking(tournament);
            if (!scoreOfCompetitors.isEmpty()) {
                return generateAchievement(AchievementType.MASTER_THE_LOOP, AchievementGrade.NORMAL,
                        Collections.singletonList(scoreOfCompetitors.getFirst().getCompetitor()), tournament);
            }
        }
        return new ArrayList<>();
    }

    public List<Achievement> generateMasterTheLoopAchievementGrade(Tournament tournament, AchievementGrade achievementGrade, int amount) {
        return generateGradeAchievementsByDays(tournament, AchievementType.MASTER_THE_LOOP, achievementGrade, amount, DAYS_WINDOW_YEAR);
    }
}

