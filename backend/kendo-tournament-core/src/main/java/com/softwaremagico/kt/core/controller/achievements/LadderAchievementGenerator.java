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
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.tournaments.BubbleSortTournamentHandler;
import com.softwaremagico.kt.core.tournaments.SenbatsuTournamentHandler;
import com.softwaremagico.kt.persistence.entities.Achievement;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.AchievementGrade;
import com.softwaremagico.kt.persistence.values.AchievementType;
import com.softwaremagico.kt.persistence.values.TournamentType;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class LadderAchievementGenerator extends AchievementGenerationSupport {

    private static final int DETHRONE_THE_KING_NORMAL = 2;
    private static final int DETHRONE_THE_KING_BRONZE = 3;
    private static final int DETHRONE_THE_KING_SILVER = 5;
    private static final int DETHRONE_THE_KING_GOLD = 7;

    private static final int SENBATSU_RUNGS_NORMAL = 3;
    private static final int SENBATSU_RUNGS_BRONZE = 4;
    private static final int SENBATSU_RUNGS_SILVER = 5;
    private static final int SENBATSU_RUNGS_GOLD = 7;

    private final GroupProvider groupProvider;
    private final BubbleSortTournamentHandler bubbleSortTournamentHandler;
    private final SenbatsuTournamentHandler senbatsuTournamentHandler;

    public LadderAchievementGenerator(AchievementProvider achievementProvider, GroupProvider groupProvider,
                                      BubbleSortTournamentHandler bubbleSortTournamentHandler,
                                      SenbatsuTournamentHandler senbatsuTournamentHandler) {
        super(achievementProvider);
        this.groupProvider = groupProvider;
        this.bubbleSortTournamentHandler = bubbleSortTournamentHandler;
        this.senbatsuTournamentHandler = senbatsuTournamentHandler;
    }

    public List<Achievement> generateDethroneTheKingAchievement(Tournament tournament) {
        if (tournament.getType() == TournamentType.BUBBLE_SORT) {
            final List<Group> groups = groupProvider.getGroups(tournament);
            if (groups.size() > 1) {
                final List<Team> startingRanking = groups.getFirst().getTeams();
                final List<Team> endingRanking = bubbleSortTournamentHandler.getTeamsOrderedByRanks(tournament, groups.getLast(),
                        bubbleSortTournamentHandler.getDrawResolution(tournament));

                final Team kingTeam = endingRanking.getLast();
                final int startingPosition = startingRanking.indexOf(kingTeam);
                final int kingPosition = endingRanking.size() - 1;

                if (kingPosition - startingPosition >= DETHRONE_THE_KING_GOLD) {
                    return generateAchievement(AchievementType.DETHRONE_THE_KING, AchievementGrade.GOLD,
                            kingTeam.getMembers(), tournament);
                } else if (kingPosition - startingPosition >= DETHRONE_THE_KING_SILVER) {
                    return generateAchievement(AchievementType.DETHRONE_THE_KING, AchievementGrade.SILVER,
                            kingTeam.getMembers(), tournament);
                } else if (kingPosition - startingPosition >= DETHRONE_THE_KING_BRONZE) {
                    return generateAchievement(AchievementType.DETHRONE_THE_KING, AchievementGrade.BRONZE,
                            kingTeam.getMembers(), tournament);
                } else if (kingPosition - startingPosition >= DETHRONE_THE_KING_NORMAL) {
                    return generateAchievement(AchievementType.DETHRONE_THE_KING, AchievementGrade.NORMAL,
                            kingTeam.getMembers(), tournament);
                }
            }
        }
        return new ArrayList<>();
    }

    public List<Achievement> generateClimbTheLadderAchievement(Tournament tournament) {
        final List<Achievement> achievements = new ArrayList<>();
        if (tournament.getType() == TournamentType.SENBATSU) {
            final List<Group> groups = groupProvider.getGroups(tournament);
            final List<Team> startingRanking = groups.getFirst().getTeams();
            final List<Team> endingRanking = senbatsuTournamentHandler.getFinalRanking(tournament);

            startingRanking.forEach(team -> {
                final int startingPosition = startingRanking.indexOf(team);
                final int endingPosition = endingRanking.indexOf(team);
                if (endingPosition - startingPosition >= SENBATSU_RUNGS_GOLD) {
                    achievements.addAll(generateAchievement(AchievementType.CLIMB_THE_LADDER, AchievementGrade.GOLD,
                            team.getMembers(), tournament));
                } else if (endingPosition - startingPosition >= SENBATSU_RUNGS_SILVER) {
                    achievements.addAll(generateAchievement(AchievementType.CLIMB_THE_LADDER, AchievementGrade.SILVER,
                            team.getMembers(), tournament));
                } else if (endingPosition - startingPosition >= SENBATSU_RUNGS_BRONZE) {
                    achievements.addAll(generateAchievement(AchievementType.CLIMB_THE_LADDER, AchievementGrade.BRONZE,
                            team.getMembers(), tournament));
                } else if (endingPosition - startingPosition >= SENBATSU_RUNGS_NORMAL) {
                    achievements.addAll(generateAchievement(AchievementType.CLIMB_THE_LADDER, AchievementGrade.NORMAL,
                            team.getMembers(), tournament));
                }
            });
        }
        return achievements;
    }
}

