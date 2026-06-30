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
import com.softwaremagico.kt.core.score.ScoreOfTeam;
import com.softwaremagico.kt.persistence.entities.Achievement;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.AchievementGrade;
import com.softwaremagico.kt.persistence.values.AchievementType;
import com.softwaremagico.kt.persistence.values.TournamentType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SwissAchievementGenerator extends AchievementGenerationSupport {

    private static final int SWISS_WIN_POINTS = 3;
    private static final int SWISS_DRAW_POINTS = 1;

    private final RankingProvider rankingProvider;

    public SwissAchievementGenerator(AchievementProvider achievementProvider, RankingProvider rankingProvider) {
        super(achievementProvider);
        this.rankingProvider = rankingProvider;
    }

    public List<Achievement> generateSwissWinnerAchievement(Tournament tournament) {
        if (tournament.getType() == TournamentType.SWISS) {
            final List<ScoreOfTeam> scoreOfTeams = rankingProvider.getTeamsScoreRanking(tournament);
            if (!scoreOfTeams.isEmpty()) {
                return generateAchievement(AchievementType.SWISS_WINNER, AchievementGrade.NORMAL,
                        scoreOfTeams.getFirst().getTeam().getMembers(), tournament);
            }
        }
        return new ArrayList<>();
    }

    public List<Achievement> generateSwissWinnerAchievementGrade(Tournament tournament, AchievementGrade achievementGrade, int amount) {
        return generateGradeAchievementsByDays(tournament, AchievementType.SWISS_WINNER, achievementGrade, amount, DAYS_WINDOW_YEAR);
    }

    public List<Achievement> generateBuchholzWhispererAchievement(Tournament tournament) {
        if (tournament.getType() != TournamentType.SWISS) {
            return new ArrayList<>();
        }

        final List<ScoreOfTeam> scoreOfTeams = rankingProvider.getTeamsScoreRanking(tournament);
        if (scoreOfTeams.size() < 2) {
            return new ArrayList<>();
        }

        final ScoreOfTeam winner = scoreOfTeams.getFirst();
        final ScoreOfTeam runnerUp = scoreOfTeams.get(1);
        final int winnerSwissPoints = winner.getWonFights() * SWISS_WIN_POINTS + winner.getDrawFights() * SWISS_DRAW_POINTS;
        final int runnerUpSwissPoints = runnerUp.getWonFights() * SWISS_WIN_POINTS + runnerUp.getDrawFights() * SWISS_DRAW_POINTS;

        if (winnerSwissPoints == runnerUpSwissPoints && !Objects.equals(winner.getSwissTieBreakValue(), runnerUp.getSwissTieBreakValue())) {
            return generateAchievement(AchievementType.BUCHHOLZ_WHISPERER, AchievementGrade.NORMAL,
                    winner.getTeam().getMembers(), tournament);
        }

        return new ArrayList<>();
    }

    public List<Achievement> generateBuchholzWhispererAchievementGrade(Tournament tournament, AchievementGrade achievementGrade, int amount) {
        return generateGradeAchievementsByDays(tournament, AchievementType.BUCHHOLZ_WHISPERER, achievementGrade, amount, DAYS_WINDOW_YEAR);
    }
}



