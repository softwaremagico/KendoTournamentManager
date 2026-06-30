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
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.AchievementGrade;
import com.softwaremagico.kt.persistence.values.AchievementType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class RivalryAchievementGenerator extends ConsecutiveAchievementGenerationSupport {

    private static final int DEFAULT_NUMBER_BRONZE = 2;
    private static final int DEFAULT_NUMBER_SILVER = 3;
    private static final int DEFAULT_NUMBER_GOLD = 5;
    private static final int MINIMUM_LOST_SITH_NORMAL = 3;
    private static final int MINIMUM_LOST_SITH_BRONZE = 5;
    private static final int MINIMUM_LOST_SITH_SILVER = 7;
    private static final int MINIMUM_LOST_SITH_GOLD = 10;

    private final DuelProvider duelProvider;

    public RivalryAchievementGenerator(AchievementProvider achievementProvider, DuelProvider duelProvider,
                                       TournamentProvider tournamentProvider, ParticipantProvider participantProvider) {
        super(achievementProvider, tournamentProvider, participantProvider);
        this.duelProvider = duelProvider;
    }

    public List<Achievement> generateVendettaAchievement(Tournament tournament, List<Fight> fightsFromTournament) {
        final List<Participant> participants = new ArrayList<>();
        fightsFromTournament.forEach(fight -> fight.getDuels().forEach(duel -> {
            if (!duel.getCompetitor1ScoreTime().isEmpty() && !duel.getCompetitor2ScoreTime().isEmpty()
                    && duel.getCompetitor1ScoreTime().getFirst() != null && duel.getCompetitor2ScoreTime().getFirst() != null
                    && duel.getCompetitor1ScoreTime().getFirst() < duel.getCompetitor2ScoreTime().getFirst()
                    && duel.getWinner() == 2) {
                participants.add(duel.getCompetitor2());
            }
            if (!duel.getCompetitor1ScoreTime().isEmpty() && !duel.getCompetitor2ScoreTime().isEmpty()
                    && duel.getCompetitor1ScoreTime().getFirst() != null && duel.getCompetitor2ScoreTime().getFirst() != null
                    && duel.getCompetitor2ScoreTime().getFirst() < duel.getCompetitor1ScoreTime().getFirst()
                    && duel.getWinner() == 1) {
                participants.add(duel.getCompetitor1());
            }
        }));
        return generateAchievement(AchievementType.V_FOR_VENDETTA, AchievementGrade.NORMAL,
                participants, tournament);
    }

    public List<Achievement> generateSithApprenticesAlwaysKillTheirMasterAchievement(Tournament tournament,
                                                                                       List<Fight> fightsFromTournament) {
        final List<Achievement> achievements = new ArrayList<>();
        fightsFromTournament.forEach(fight -> {
            for (Duel duel : fight.getDuels()) {
                final List<Duel> previousDuels = duelProvider.getWhenBothAreInvolved(duel.getCompetitor1(), duel.getCompetitor2());
                boolean isApprentice = true;
                int numberOfPreviousDuels = 0;
                for (Duel previousDuel : previousDuels) {
                    if (previousDuel.getCreatedAt().isBefore(tournament.getCreatedAt())) {
                        numberOfPreviousDuels++;
                        if (Objects.equals(duel.getCompetitorWinner(), previousDuel.getCompetitorWinner()) || previousDuel.getWinner() == 0) {
                            isApprentice = false;
                            break;
                        }
                    }
                }
                if (isApprentice && duel.getCompetitorWinner() != null) {
                    if (numberOfPreviousDuels >= MINIMUM_LOST_SITH_NORMAL && numberOfPreviousDuels < MINIMUM_LOST_SITH_BRONZE) {
                        achievements.add(new Achievement(duel.getCompetitorWinner(), tournament, AchievementType.SITH_APPRENTICES_ALWAYS_KILL_THEIR_MASTER,
                                AchievementGrade.NORMAL));
                    } else if (numberOfPreviousDuels >= MINIMUM_LOST_SITH_BRONZE && numberOfPreviousDuels < MINIMUM_LOST_SITH_SILVER) {
                        achievements.add(new Achievement(duel.getCompetitorWinner(), tournament, AchievementType.SITH_APPRENTICES_ALWAYS_KILL_THEIR_MASTER,
                                AchievementGrade.BRONZE));
                    } else if (numberOfPreviousDuels >= MINIMUM_LOST_SITH_SILVER && numberOfPreviousDuels < MINIMUM_LOST_SITH_GOLD) {
                        achievements.add(new Achievement(duel.getCompetitorWinner(), tournament, AchievementType.SITH_APPRENTICES_ALWAYS_KILL_THEIR_MASTER,
                                AchievementGrade.SILVER));
                    } else if (numberOfPreviousDuels >= MINIMUM_LOST_SITH_GOLD) {
                        achievements.add(new Achievement(duel.getCompetitorWinner(), tournament, AchievementType.SITH_APPRENTICES_ALWAYS_KILL_THEIR_MASTER,
                                AchievementGrade.GOLD));
                    }
                }
            }
        });
        return getAchievementProvider().saveAll(achievements);
    }

    public List<Achievement> generateVendettaAchievementBronze(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_NUMBER_BRONZE, AchievementType.V_FOR_VENDETTA, AchievementGrade.BRONZE);
    }

    public List<Achievement> generateVendettaAchievementSilver(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_NUMBER_SILVER, AchievementType.V_FOR_VENDETTA, AchievementGrade.SILVER);
    }

    public List<Achievement> generateVendettaAchievementGold(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_NUMBER_GOLD, AchievementType.V_FOR_VENDETTA, AchievementGrade.GOLD);
    }
}





