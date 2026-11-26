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
import com.softwaremagico.kt.core.score.ScoreOfTeam;
import com.softwaremagico.kt.persistence.entities.Achievement;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.AchievementGrade;
import com.softwaremagico.kt.persistence.values.AchievementType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WinnerAchievementGenerator extends AchievementGenerationSupport {

    private final RankingProvider rankingProvider;

    public WinnerAchievementGenerator(AchievementProvider achievementProvider, RankingProvider rankingProvider) {
        super(achievementProvider);
        this.rankingProvider = rankingProvider;
    }

    public List<Achievement> generateTheWinnerTournament(Tournament tournament) {
        final List<ScoreOfCompetitor> scoreOfCompetitors = rankingProvider.getCompetitorsScoreRanking(tournament);
        if (!scoreOfCompetitors.isEmpty()) {
            return generateAchievement(AchievementType.THE_WINNER, AchievementGrade.NORMAL,
                    Collections.singletonList(scoreOfCompetitors.getFirst().getCompetitor()), tournament);
        }
        return new ArrayList<>();
    }

    public List<Achievement> generateTheWinnerAchievementGrade(Tournament tournament, AchievementGrade achievementGrade, int amount) {
        return generateGradeAchievementsByDays(tournament, AchievementType.THE_WINNER, achievementGrade, amount, DAYS_WINDOW_YEAR);
    }

    public List<Achievement> generateTheWinnerTeamTournament(Tournament tournament) {
        if (tournament.getTeamSize() > 1) {
            final List<ScoreOfTeam> scoreOfTeams = rankingProvider.getTeamsScoreRanking(tournament);
            if (!scoreOfTeams.isEmpty() && scoreOfTeams.getFirst().getTeam().getMembers().size() > 1) {
                return generateAchievement(AchievementType.THE_WINNER_TEAM, AchievementGrade.NORMAL,
                        scoreOfTeams.getFirst().getTeam().getMembers(), tournament);
            }
        }
        return new ArrayList<>();
    }

    public List<Achievement> generateTheWinnerTeamAchievementGrade(Tournament tournament, AchievementGrade achievementGrade, int amount) {
        return generateGradeAchievementsByDays(tournament, AchievementType.THE_WINNER_TEAM, achievementGrade, amount, DAYS_WINDOW_YEAR);
    }
}



