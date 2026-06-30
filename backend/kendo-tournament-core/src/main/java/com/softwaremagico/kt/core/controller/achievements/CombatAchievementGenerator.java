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
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.Achievement;
import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.AchievementGrade;
import com.softwaremagico.kt.persistence.values.AchievementType;
import com.softwaremagico.kt.persistence.values.Score;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CombatAchievementGenerator extends ConsecutiveAchievementGenerationSupport {

    private static final int DEFAULT_NUMBER_BRONZE = 2;
    private static final int DEFAULT_NUMBER_SILVER = 3;
    private static final int DEFAULT_NUMBER_GOLD = 5;
    private static final int DEFAULT_LONG_NUMBER_BRONZE = 3;
    private static final int DEFAULT_LONG_NUMBER_SILVER = 5;
    private static final int DEFAULT_LONG_NUMBER_GOLD = 7;

    private final FightProvider fightProvider;
    private final DuelProvider duelProvider;
    private final int lethalWeaponMaxTime;
    private final int minimumTournamentFights;

    public CombatAchievementGenerator(AchievementProvider achievementProvider, FightProvider fightProvider,
                                      DuelProvider duelProvider, TournamentProvider tournamentProvider,
                                      ParticipantProvider participantProvider,
                                      int lethalWeaponMaxTime, int minimumTournamentFights) {
        super(achievementProvider, tournamentProvider, participantProvider);
        this.fightProvider = fightProvider;
        this.duelProvider = duelProvider;
        this.lethalWeaponMaxTime = lethalWeaponMaxTime;
        this.minimumTournamentFights = minimumTournamentFights;
    }

    public List<Achievement> generateBillyTheKidAchievement(Tournament tournament) {
        if (this.fightProvider.getFights(tournament).size() < this.minimumTournamentFights) {
            return new ArrayList<>();
        }
        int minTime = tournament.getDuelsDuration();
        com.softwaremagico.kt.persistence.entities.Participant participant = null;
        for (final Duel duel : this.duelProvider.get(tournament)) {
            for (final Integer time : duel.getCompetitor1ScoreTime()) {
                if (time == null) {
                    continue;
                }
                if (time == minTime && !java.util.Objects.equals(participant, duel.getCompetitor1())) {
                    participant = null;
                } else if (time < minTime && time > Duel.DEFAULT_DURATION) {
                    participant = duel.getCompetitor1();
                    minTime = time;
                }
            }
            for (final Integer time : duel.getCompetitor2ScoreTime()) {
                if (time == null) {
                    continue;
                }
                if (time == minTime && !java.util.Objects.equals(participant, duel.getCompetitor2())) {
                    participant = null;
                } else if (time < minTime && time > Duel.DEFAULT_DURATION) {
                    participant = duel.getCompetitor2();
                    minTime = time;
                }
            }
        }
        if (participant != null) {
            return generateAchievement(AchievementType.BILLY_THE_KID, AchievementGrade.NORMAL, java.util.Collections.singleton(participant), tournament);
        }
        return new ArrayList<>();
    }

    public List<Achievement> generateLethalWeaponAchievement(Tournament tournament) {
        final Set<Duel> duels = this.duelProvider.findByScorePerformedInLessThan(tournament, this.lethalWeaponMaxTime);
        final Set<com.softwaremagico.kt.persistence.entities.Participant> participants = new HashSet<>();
        duels.forEach(duel -> {
            duel.getCompetitor1ScoreTime().forEach(time -> {
                if (time != null && time <= this.lethalWeaponMaxTime) {
                    participants.add(duel.getCompetitor1());
                }
            });
            duel.getCompetitor2ScoreTime().forEach(time -> {
                if (time != null && time <= this.lethalWeaponMaxTime) {
                    participants.add(duel.getCompetitor2());
                }
            });
        });
        return generateAchievement(AchievementType.LETHAL_WEAPON, AchievementGrade.NORMAL, participants, tournament);
    }

    public List<Achievement> generateTerminatorAchievement(Tournament tournament) {
        if (this.fightProvider.getFights(tournament).size() < this.minimumTournamentFights) {
            return new ArrayList<>();
        }
        final List<Duel> duels = this.duelProvider.get(tournament);
        final Set<com.softwaremagico.kt.persistence.entities.Participant> competitors = new HashSet<>();
        duels.forEach(duel -> competitors.addAll(duel.getCompetitors()));

        duels.forEach(duel -> {
            if (duel.getCompetitor1ScoreValue() < Duel.POINTS_TO_WIN) {
                competitors.remove(duel.getCompetitor1());
            }
            if (duel.getCompetitor2ScoreValue() < Duel.POINTS_TO_WIN) {
                competitors.remove(duel.getCompetitor2());
            }
        });
        return generateAchievement(AchievementType.TERMINATOR, AchievementGrade.NORMAL, competitors, tournament);
    }

    public List<Achievement> generateJuggernautAchievement(Tournament tournament) {
        if (this.fightProvider.getFights(tournament).size() < this.minimumTournamentFights) {
            return new ArrayList<>();
        }
        final List<Duel> duels = this.duelProvider.get(tournament);
        final Set<com.softwaremagico.kt.persistence.entities.Participant> competitors = new HashSet<>();
        duels.forEach(duel -> competitors.addAll(duel.getCompetitors()));

        duels.forEach(duel -> {
            if (duel.getCompetitor1ScoreValue() < Duel.POINTS_TO_WIN || duel.getCompetitor2ScoreValue() > 0) {
                competitors.remove(duel.getCompetitor1());
            }
            if (duel.getCompetitor2ScoreValue() < Duel.POINTS_TO_WIN || duel.getCompetitor1ScoreValue() > 0) {
                competitors.remove(duel.getCompetitor2());
            }
        });
        return generateAchievement(AchievementType.JUGGERNAUT, AchievementGrade.NORMAL, competitors, tournament);
    }

    public List<Achievement> generateTheCastleAchievement(Tournament tournament) {
        if (this.fightProvider.getFights(tournament).size() < this.minimumTournamentFights) {
            return new ArrayList<>();
        }
        final List<Duel> duels = this.duelProvider.get(tournament);
        final Set<com.softwaremagico.kt.persistence.entities.Participant> competitors = new HashSet<>();
        duels.forEach(duel -> competitors.addAll(duel.getCompetitors()));

        duels.forEach(duel -> {
            if (!duel.getCompetitor2Score().isEmpty()) {
                competitors.remove(duel.getCompetitor1());
            }
            if (!duel.getCompetitor1Score().isEmpty()) {
                competitors.remove(duel.getCompetitor2());
            }
        });
        return generateAchievement(AchievementType.THE_CASTLE, AchievementGrade.NORMAL, competitors, tournament);
    }

    public List<Achievement> generateEntrenchedAchievement(Tournament tournament) {
        if (this.fightProvider.getFights(tournament).size() < this.minimumTournamentFights) {
            return new ArrayList<>();
        }
        final List<Duel> duels = this.duelProvider.get(tournament);
        final Set<com.softwaremagico.kt.persistence.entities.Participant> competitors = new HashSet<>();
        duels.forEach(duel -> competitors.addAll(duel.getCompetitors()));

        duels.forEach(duel -> {
            if (!duel.getCompetitor1Score().isEmpty() || !duel.getCompetitor2Score().isEmpty()) {
                competitors.remove(duel.getCompetitor1());
                competitors.remove(duel.getCompetitor2());
            }
        });
        return generateAchievement(AchievementType.ENTRENCHED, AchievementGrade.NORMAL, competitors, tournament);
    }

    public List<Achievement> generateBoneBreakerAchievement(Tournament tournament) {
        final Set<Duel> duels = this.duelProvider.findByOnlyScore(tournament, Score.HANSOKU);
        final Set<com.softwaremagico.kt.persistence.entities.Participant> participants = new HashSet<>();
        duels.forEach(duel -> {
            if (duel.getCompetitor1Score().size() == 2 && duel.getCompetitor1Score().getFirst() == Score.HANSOKU
                    && duel.getCompetitor1Score().get(1) == Score.HANSOKU) {
                participants.add(duel.getCompetitor2());
            }
            if (duel.getCompetitor2Score().size() == 2 && duel.getCompetitor2Score().getFirst() == Score.HANSOKU
                    && duel.getCompetitor2Score().get(1) == Score.HANSOKU) {
                participants.add(duel.getCompetitor1());
            }
        });
        return generateAchievement(AchievementType.BONE_BREAKER, AchievementGrade.NORMAL, participants, tournament);
    }

    public List<Achievement> generateBillyTheKidAchievementBronze(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_NUMBER_BRONZE, AchievementType.BILLY_THE_KID, AchievementGrade.BRONZE);
    }

    public List<Achievement> generateBillyTheKidAchievementSilver(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_NUMBER_SILVER, AchievementType.BILLY_THE_KID, AchievementGrade.SILVER);
    }

    public List<Achievement> generateBillyTheKidAchievementGold(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_NUMBER_GOLD, AchievementType.BILLY_THE_KID, AchievementGrade.GOLD);
    }

    public List<Achievement> generateLethalWeaponAchievementBronze(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_LONG_NUMBER_BRONZE, AchievementType.LETHAL_WEAPON, AchievementGrade.BRONZE);
    }

    public List<Achievement> generateLethalWeaponAchievementSilver(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_LONG_NUMBER_SILVER, AchievementType.LETHAL_WEAPON, AchievementGrade.SILVER);
    }

    public List<Achievement> generateLethalWeaponAchievementGold(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_LONG_NUMBER_GOLD, AchievementType.LETHAL_WEAPON, AchievementGrade.GOLD);
    }

    public List<Achievement> generateTerminatorAchievementBronze(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_NUMBER_BRONZE, AchievementType.TERMINATOR, AchievementGrade.BRONZE);
    }

    public List<Achievement> generateTerminatorAchievementSilver(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_NUMBER_SILVER, AchievementType.TERMINATOR, AchievementGrade.SILVER);
    }

    public List<Achievement> generateTerminatorAchievementGold(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_NUMBER_GOLD, AchievementType.TERMINATOR, AchievementGrade.GOLD);
    }

    public List<Achievement> generateJuggernautAchievementBronze(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_NUMBER_BRONZE, AchievementType.JUGGERNAUT, AchievementGrade.BRONZE);
    }

    public List<Achievement> generateJuggernautAchievementSilver(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_NUMBER_SILVER, AchievementType.JUGGERNAUT, AchievementGrade.SILVER);
    }

    public List<Achievement> generateJuggernautAchievementGold(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_NUMBER_GOLD, AchievementType.JUGGERNAUT, AchievementGrade.GOLD);
    }

    public List<Achievement> generateTheCastleAchievementBronze(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_NUMBER_BRONZE, AchievementType.THE_CASTLE, AchievementGrade.BRONZE);
    }

    public List<Achievement> generateTheCastleAchievementSilver(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_NUMBER_SILVER, AchievementType.THE_CASTLE, AchievementGrade.SILVER);
    }

    public List<Achievement> generateTheCastleAchievementGold(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_NUMBER_GOLD, AchievementType.THE_CASTLE, AchievementGrade.GOLD);
    }

    public List<Achievement> generateEntrenchedAchievementBronze(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_LONG_NUMBER_BRONZE, AchievementType.ENTRENCHED, AchievementGrade.BRONZE);
    }

    public List<Achievement> generateEntrenchedAchievementSilver(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_LONG_NUMBER_SILVER, AchievementType.ENTRENCHED, AchievementGrade.SILVER);
    }

    public List<Achievement> generateEntrenchedAchievementGold(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_LONG_NUMBER_GOLD, AchievementType.ENTRENCHED, AchievementGrade.GOLD);
    }
}


