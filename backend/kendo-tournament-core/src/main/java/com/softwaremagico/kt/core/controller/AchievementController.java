package com.softwaremagico.kt.core.controller;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
 * %%
 * This software is designed by Jorge Hortelano Otero. Jorge Hortelano Otero
 * <softwaremagico@gmail.com> Valencia (Spain).
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.softwaremagico.kt.core.controller.models.AchievementDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.AchievementConverter;
import com.softwaremagico.kt.core.converters.ParticipantConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.models.AchievementConverterRequest;
import com.softwaremagico.kt.core.converters.models.TournamentConverterRequest;
import com.softwaremagico.kt.core.exceptions.ParticipantNotFoundException;
import com.softwaremagico.kt.core.exceptions.TournamentNotFoundException;
import com.softwaremagico.kt.core.providers.*;
import com.softwaremagico.kt.core.score.ScoreOfCompetitor;
import com.softwaremagico.kt.core.score.ScoreOfTeam;
import com.softwaremagico.kt.persistence.entities.*;
import com.softwaremagico.kt.persistence.repositories.AchievementRepository;
import com.softwaremagico.kt.persistence.values.AchievementGrade;
import com.softwaremagico.kt.persistence.values.AchievementType;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.Score;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AchievementController extends BasicInsertableController<Achievement, AchievementDTO, AchievementRepository,
        AchievementProvider, AchievementConverterRequest, AchievementConverter> {

    private static final int BILL_THE_KID_MAX_TIME = 10;

    private static final int WINNER_BRONZE_NUMBER = 3;
    private static final int WINNER_SILVER_NUMBER = 4;
    private static final int WINNER_GOLD_NUMBER = 5;

    private static final int DAYS_TO_CHECK_INCREMENTAL_ACHIEVEMENTS = 365;

    private final TournamentConverter tournamentConverter;

    private final TournamentProvider tournamentProvider;

    private final ParticipantProvider participantProvider;

    private final ParticipantConverter participantConverter;

    private final RoleProvider roleProvider;

    private final AchievementProvider achievementProvider;

    private final DuelProvider duelProvider;

    private final RankingProvider rankingProvider;

    private List<Role> rolesFromTournament;

    private List<Participant> participantsFromTournament;


    protected AchievementController(AchievementProvider provider, AchievementConverter converter,
                                    TournamentConverter tournamentConverter, TournamentProvider tournamentProvider,
                                    ParticipantProvider participantProvider, ParticipantConverter participantConverter,
                                    RoleProvider roleProvider, AchievementProvider achievementProvider, DuelProvider duelProvider,
                                    RankingProvider rankingProvider) {
        super(provider, converter);
        this.tournamentConverter = tournamentConverter;
        this.tournamentProvider = tournamentProvider;
        this.participantProvider = participantProvider;
        this.participantConverter = participantConverter;
        this.roleProvider = roleProvider;
        this.achievementProvider = achievementProvider;
        this.duelProvider = duelProvider;
        this.rankingProvider = rankingProvider;
    }

    @Override
    protected AchievementConverterRequest createConverterRequest(Achievement achievement) {
        return new AchievementConverterRequest(achievement);
    }

    private List<Role> getRolesFromTournament(Tournament tournament) {
        if (rolesFromTournament == null) {
            rolesFromTournament = roleProvider.getAll(tournament);
        }
        return rolesFromTournament;
    }

    private List<Participant> getParticipantsFromTournament(Tournament tournament) {
        if (participantsFromTournament == null) {
            participantsFromTournament = participantProvider.get(tournament);
        }
        return participantsFromTournament;
    }


    public List<AchievementDTO> getParticipantAchievements(Integer participantId) {
        final Participant participant = participantProvider.get(participantId)
                .orElseThrow(() -> new ParticipantNotFoundException(getClass(), "No participant found with id '" + participantId + "'."));
        return convertAll(provider.get(participant));
    }

    public List<AchievementDTO> getParticipantAchievements(ParticipantDTO participantDTO) {
        return convertAll(provider.get(participantConverter.reverse(participantDTO)));
    }

    public List<AchievementDTO> getAchievements(TournamentDTO tournamentDTO, AchievementType achievementType) {
        return convertAll(provider.get(tournamentConverter.reverse(tournamentDTO), achievementType));
    }

    public List<AchievementDTO> getAchievements(AchievementType achievementType) {
        return convertAll(provider.get(achievementType));
    }

    public List<AchievementDTO> getTournamentAchievements(Integer tournamentId) {
        final Tournament tournament = tournamentProvider.get(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(getClass(), "No tournament found with id '" + tournamentId + "'."));
        return convertAll(provider.get(tournament));
    }

    public List<AchievementDTO> regenerateAllAchievements() {
        final List<TournamentDTO> tournaments = tournamentConverter.convertAll(tournamentProvider.getAll().stream()
                .map(TournamentConverterRequest::new).collect(Collectors.toList()));
        final List<AchievementDTO> achievementsGenerated = new ArrayList<>();
        for (final TournamentDTO tournament : tournaments) {
            deleteAchievements(tournament);
            achievementsGenerated.addAll(generateAchievements(tournament));
        }
        return achievementsGenerated;
    }

    public List<AchievementDTO> regenerateAchievements(Integer tournamentId) {
        final TournamentDTO tournament = tournamentConverter.convert(new TournamentConverterRequest(tournamentProvider.get(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(getClass(), "No tournament found with id '" + tournamentId + "'."))));
        deleteAchievements(tournament);
        return generateAchievements(tournament);
    }

    private void deleteAchievements(TournamentDTO tournamentDTO) {
        provider.delete(tournamentConverter.reverse(tournamentDTO));
    }

    public List<AchievementDTO> generateAchievements(TournamentDTO tournamentDTO) {
        final Tournament tournament = tournamentConverter.reverse(tournamentDTO);
        //Remove any achievement already calculated.
        provider.delete(tournament);

        final List<Achievement> achievementsGenerated = new ArrayList<>();

        //Generate new ones.
        achievementsGenerated.addAll(generateBillyTheKidAchievement(tournament));
        achievementsGenerated.addAll(generateLethalWeaponAchievement(tournament));
        achievementsGenerated.addAll(generateTerminatorAchievement(tournament));
        achievementsGenerated.addAll(generateJuggernautAchievement(tournament));
        achievementsGenerated.addAll(generateTheKingAchievement(tournament));
        achievementsGenerated.addAll(generateLooksGoodFromFarAwayButAchievementBronze(tournament));
        achievementsGenerated.addAll(generateLooksGoodFromFarAwayButAchievementSilver(tournament));
        achievementsGenerated.addAll(generateLooksGoodFromFarAwayButAchievementGold(tournament));
        achievementsGenerated.addAll(generateILoveTheFlagsAchievementBronze(tournament));
        achievementsGenerated.addAll(generateILoveTheFlagsAchievementSilver(tournament));
        achievementsGenerated.addAll(generateILoveTheFlagsAchievementGold(tournament));
        achievementsGenerated.addAll(generateTheRockAchievement(tournament));
        achievementsGenerated.addAll(generateTheTowerAchievement(tournament));
        achievementsGenerated.addAll(generateTheCastleAchievement(tournament));
        achievementsGenerated.addAll(generateEntrenchedAchievement(tournament));
        achievementsGenerated.addAll(generateALittleOfEverythingAchievement(tournament));
        achievementsGenerated.addAll(generateALittleOfEverythingSilverAchievement(tournament));
        achievementsGenerated.addAll(generateALittleOfEverythingGoldenAchievement(tournament));
        achievementsGenerated.addAll(generateBoneBreakerAchievement(tournament));
        achievementsGenerated.addAll(generateWoodcutterAchievement(tournament));
        achievementsGenerated.addAll(generateFlexibleAsBambooAchievement(tournament));
        achievementsGenerated.addAll(generateSweatyTenuguiAchievement(tournament));
        achievementsGenerated.addAll(generateSweatyTenuguiAchievement(tournament));
        achievementsGenerated.addAll(generateTheWinnerTournament(tournament));
        achievementsGenerated.addAll(generateTheWinnerBronzeTournament(tournament));
        achievementsGenerated.addAll(generateTheWinnerSilverTournament(tournament));
        achievementsGenerated.addAll(generateTheWinnerGoldTournament(tournament));
        achievementsGenerated.addAll(generateTheWinnerTeamTournament(tournament));
        achievementsGenerated.addAll(generateTheWinnerTeamBronzeTournament(tournament));
        achievementsGenerated.addAll(generateTheWinnerTeamSilverTournament(tournament));
        achievementsGenerated.addAll(generateTheWinnerTeamGoldTournament(tournament));
        return convertAll(achievementsGenerated);
    }

    /**
     * Achievement for the quickest score in a tournament.
     *
     * @param tournament The tournament to check.
     */
    private List<Achievement> generateBillyTheKidAchievement(Tournament tournament) {
        final Set<Duel> duels = duelProvider.findByScorePerformedInLessThan(tournament, BILL_THE_KID_MAX_TIME);
        final Set<Participant> billies = new HashSet<>();
        duels.forEach(duel -> {
            duel.getCompetitor1ScoreTime().forEach(time -> {
                if (time <= BILL_THE_KID_MAX_TIME) {
                    billies.add(duel.getCompetitor1());
                }
            });
            duel.getCompetitor2ScoreTime().forEach(time -> {
                if (time <= BILL_THE_KID_MAX_TIME) {
                    billies.add(duel.getCompetitor2());
                }
            });

        });
        //Create new achievement for the participants.
        return generateAchievement(AchievementType.BILLY_THE_KID, AchievementGrade.NORMAL, billies, tournament);
    }

    /**
     * If somebody has done the maximum score on a tournament.
     *
     * @param tournament The tournament to check.
     */
    private List<Achievement> generateLethalWeaponAchievement(Tournament tournament) {
        return new ArrayList<>();
    }

    /**
     * If somebody has done the maximum score on two consecutive tournaments.
     *
     * @param tournament The tournament to check.
     */
    private List<Achievement> generateTerminatorAchievement(Tournament tournament) {
        return new ArrayList<>();
    }

    /**
     * If somebody has done the maximum score on three consecutive tournaments.
     *
     * @param tournament The tournament to check.
     */
    private List<Achievement> generateJuggernautAchievement(Tournament tournament) {
        return new ArrayList<>();
    }

    /**
     * The one that has last longer on the King of the Mountain mode.
     *
     * @param tournament The tournament to check.
     */
    private List<Achievement> generateTheKingAchievement(Tournament tournament) {
        return new ArrayList<>();
    }

    /**
     * When somebody is as an organizer for at least three consecutive tournaments.
     *
     * @param tournament The tournament to check.
     */
    private List<Achievement> generateLooksGoodFromFarAwayButAchievementBronze(Tournament tournament) {
        return new ArrayList<>();
    }

    /**
     * When somebody is as an organizer for at least five consecutive tournaments.
     *
     * @param tournament The tournament to check.
     */
    private List<Achievement> generateLooksGoodFromFarAwayButAchievementSilver(Tournament tournament) {
        return new ArrayList<>();
    }

    /**
     * When somebody is as an organizer for at least seven consecutive tournaments.
     *
     * @param tournament The tournament to check.
     */
    private List<Achievement> generateLooksGoodFromFarAwayButAchievementGold(Tournament tournament) {
        return new ArrayList<>();
    }

    /**
     * When somebody is a referee for at least three consecutive tournaments.
     *
     * @param tournament The tournament to check.
     */
    private List<Achievement> generateILoveTheFlagsAchievementBronze(Tournament tournament) {
        return new ArrayList<>();
    }

    /**
     * When somebody is a referee for at least four consecutive tournaments.
     *
     * @param tournament The tournament to check.
     */
    private List<Achievement> generateILoveTheFlagsAchievementSilver(Tournament tournament) {
        return new ArrayList<>();
    }

    /**
     * When somebody is a referee for at least five consecutive tournaments.
     *
     * @param tournament The tournament to check.
     */
    private List<Achievement> generateILoveTheFlagsAchievementGold(Tournament tournament) {
        return new ArrayList<>();
    }

    /**
     * When somebody has participated on a tournament and nobody has scored a hit against him/her.
     *
     * @param tournament The tournament to check.
     */
    private List<Achievement> generateTheRockAchievement(Tournament tournament) {
        return new ArrayList<>();
    }

    /**
     * When somebody has participated on two consecutive tournaments and nobody has scored a hit against him/her.
     *
     * @param tournament The tournament to check.
     */
    private List<Achievement> generateTheTowerAchievement(Tournament tournament) {
        return new ArrayList<>();
    }

    /**
     * When somebody has participated on three consecutive tournaments and nobody has scored a hit against him/her.
     *
     * @param tournament The tournament to check.
     */
    private List<Achievement> generateTheCastleAchievement(Tournament tournament) {
        return new ArrayList<>();
    }

    /**
     * When somebody has participated on a tournament and nobody has scored a hit against him/her and
     * he does neither score a hit against his opponents.
     *
     * @param tournament The tournament to check.
     */
    private List<Achievement> generateEntrenchedAchievement(Tournament tournament) {
        return new ArrayList<>();
    }

    /**
     * When all points are scored: Men, Kote, Do.
     *
     * @param tournament The tournament to check.
     */
    private List<Achievement> generateALittleOfEverythingAchievement(Tournament tournament) {
        return new ArrayList<>();
    }

    /**
     * When all points are scored: Men, Kote, Do and Hansoku.
     *
     * @param tournament The tournament to check.
     */
    private List<Achievement> generateALittleOfEverythingSilverAchievement(Tournament tournament) {
        return new ArrayList<>();
    }

    /**
     * When all points are scored: Men, Kote, Do and Hansoku and Ippon.
     *
     * @param tournament The tournament to check.
     */
    private List<Achievement> generateALittleOfEverythingGoldenAchievement(Tournament tournament) {
        return new ArrayList<>();
    }

    /**
     * When somebody loose a combat only by Hansokus.
     *
     * @param tournament The tournament to check.
     */
    private List<Achievement> generateBoneBreakerAchievement(Tournament tournament) {
        final Set<Duel> duels = duelProvider.findByOnlyScore(tournament, Score.HANSOKU);
        final Set<Participant> participants = new HashSet<>();
        duels.forEach(duel -> {
            if (duel.getCompetitor1Score().size() == 2 && duel.getCompetitor1Score().get(0) == Score.HANSOKU
                    && duel.getCompetitor1Score().get(1) == Score.HANSOKU) {
                participants.add(duel.getCompetitor2());
            }
            if (duel.getCompetitor2Score().size() == 2 && duel.getCompetitor2Score().get(0) == Score.HANSOKU
                    && duel.getCompetitor2Score().get(1) == Score.HANSOKU) {
                participants.add(duel.getCompetitor1());
            }
        });
        //Create new achievement for the participants.
        return generateAchievement(AchievementType.BONE_BREAKER, AchievementGrade.NORMAL, participants, tournament);
    }

    /**
     * When somebody only perform 'do' scores and win a duel.
     *
     * @param tournament The tournament to check.
     */
    private List<Achievement> generateWoodcutterAchievement(Tournament tournament) {
        final Set<Duel> duels = duelProvider.findByOnlyScore(tournament, Score.DO);
        final Set<Participant> woodcutters = new HashSet<>();
        duels.forEach(duel -> {
            if (duel.getCompetitor1Score().size() == 2 && duel.getCompetitor1Score().get(0) == Score.DO
                    && duel.getCompetitor1Score().get(1) == Score.DO) {
                woodcutters.add(duel.getCompetitor1());
            }
            if (duel.getCompetitor2Score().size() == 2 && duel.getCompetitor2Score().get(0) == Score.DO
                    && duel.getCompetitor2Score().get(1) == Score.DO) {
                woodcutters.add(duel.getCompetitor2());
            }
        });
        //Create new achievement for the participants.
        return generateAchievement(AchievementType.WOODCUTTER, AchievementGrade.NORMAL, woodcutters, tournament);
    }


    /**
     * When somebody has performed all the roles
     *
     * @param tournament The tournament to check.
     */
    private List<Achievement> generateFlexibleAsBambooAchievement(Tournament tournament) {
        //Get all participants from a tournament that has almost all roles in any tournament,
        final List<Participant> participants = participantProvider.get(tournament, RoleType.values().length / 2 + 1);
        //Remove the ones already have the achievement.
        participants.removeAll(participantProvider.getParticipantsWithAchievementFromList(AchievementType.FLEXIBLE_AS_BAMBOO, participants));
        //Create new achievement for the participants.
        return generateAchievement(AchievementType.FLEXIBLE_AS_BAMBOO, AchievementGrade.NORMAL, participants, tournament);
    }

    /**
     * First tournament as a competitor
     *
     * @param tournament The tournament to check.
     */
    private List<Achievement> generateSweatyTenuguiAchievement(Tournament tournament) {
        final List<Participant> participants = participantProvider.getParticipantFirstTimeCompetitors(tournament);
        return generateAchievement(AchievementType.SWEATY_TENUGUI, AchievementGrade.NORMAL, participants, tournament);
    }

    private List<Achievement> generateAchievement(AchievementType achievementType, AchievementGrade achievementGrade,
                                                  Collection<Participant> participants, Tournament tournament) {
        final List<Achievement> achievements = new ArrayList<>();
        participants.forEach(participant -> {
            final Achievement achievement = new Achievement(participant, tournament, achievementType, achievementGrade);
            //If achievements are redone, try to keep the dates.
            if (tournament.getFinishedAt() != null) {
                achievement.setCreatedAt(tournament.getFinishedAt());
            } else {
                achievement.setCreatedAt(tournament.getCreatedAt());
            }
            achievements.add(achievement);
        });
        return achievementProvider.saveAll(achievements);
    }

    /**
     * Winner of a tournament
     *
     * @param tournament The tournament to check.
     */
    private List<Achievement> generateTheWinnerTournament(Tournament tournament) {
        final List<ScoreOfCompetitor> scoreOfCompetitors = rankingProvider.getCompetitorsScoreRanking(tournament);
        if (!scoreOfCompetitors.isEmpty()) {
            return generateAchievement(AchievementType.THE_WINNER, AchievementGrade.NORMAL,
                    Collections.singletonList(scoreOfCompetitors.get(0).getCompetitor()), tournament);
        }
        return new ArrayList<>();
    }

    private List<Achievement> generateTheWinnerBronzeTournament(Tournament tournament) {
        return generateIncrementalGradedTournament(tournament, AchievementType.THE_WINNER, AchievementGrade.BRONZE,
                WINNER_BRONZE_NUMBER, DAYS_TO_CHECK_INCREMENTAL_ACHIEVEMENTS);
    }

    private List<Achievement> generateTheWinnerSilverTournament(Tournament tournament) {
        return generateIncrementalGradedTournament(tournament, AchievementType.THE_WINNER, AchievementGrade.SILVER,
                WINNER_SILVER_NUMBER, DAYS_TO_CHECK_INCREMENTAL_ACHIEVEMENTS);
    }

    private List<Achievement> generateTheWinnerGoldTournament(Tournament tournament) {
        return generateIncrementalGradedTournament(tournament, AchievementType.THE_WINNER, AchievementGrade.GOLD,
                WINNER_GOLD_NUMBER, DAYS_TO_CHECK_INCREMENTAL_ACHIEVEMENTS);
    }

    private List<Achievement> generateIncrementalGradedTournament(Tournament tournament, AchievementType type, AchievementGrade grade,
                                                                  Integer amount, Integer daysToCount) {
        final List<Achievement> winnersAchievements = achievementProvider.getAfter(tournament, type,
                AchievementGrade.NORMAL, tournament.getCreatedAt().minusDays(daysToCount));
        final List<Achievement> winnersGradeAchievements = achievementProvider.getAfter(tournament, type,
                grade, tournament.getCreatedAt().minusDays(daysToCount));
        final List<Participant> participantsWithAchievements = winnersAchievements.stream().map(Achievement::getParticipant).collect(Collectors.toList());
        final List<Achievement> generatedAchievements = new ArrayList<>();
        for (final Participant participant : participantsWithAchievements) {
            int counter = 0;
            for (final Achievement winnerAchievement : winnersAchievements) {
                if (Objects.equals(winnerAchievement.getParticipant(), participant) &&
                        //Check that does not exist already a bronze achievement assigned after this one.
                        winnersGradeAchievements.stream().filter(achievement ->
                                Objects.equals(achievement.getParticipant(), participant) &&
                                        achievement.getCreatedAt().isAfter(winnerAchievement.getCreatedAt())
                        ).findAny().isEmpty()) {
                    counter++;
                }
            }
            if (counter >= amount) {
                generatedAchievements.addAll(generateAchievement(AchievementType.THE_WINNER, grade,
                        Collections.singletonList(participant), tournament));
            }
        }
        return generatedAchievements;
    }

    private List<Achievement> generateTheWinnerTeamTournament(Tournament tournament) {
        final List<ScoreOfTeam> scoreOfTeams = rankingProvider.getTeamsScoreRanking(tournament);
        if (!scoreOfTeams.isEmpty()) {
            return generateAchievement(AchievementType.THE_WINNER_TEAM, AchievementGrade.NORMAL,
                    scoreOfTeams.get(0).getTeam().getMembers(), tournament);
        }
        return new ArrayList<>();
    }

    private List<Achievement> generateTheWinnerTeamBronzeTournament(Tournament tournament) {
        return generateIncrementalGradedTournament(tournament, AchievementType.THE_WINNER_TEAM, AchievementGrade.BRONZE,
                WINNER_BRONZE_NUMBER, DAYS_TO_CHECK_INCREMENTAL_ACHIEVEMENTS);
    }

    private List<Achievement> generateTheWinnerTeamSilverTournament(Tournament tournament) {
        return generateIncrementalGradedTournament(tournament, AchievementType.THE_WINNER_TEAM, AchievementGrade.SILVER,
                WINNER_SILVER_NUMBER, DAYS_TO_CHECK_INCREMENTAL_ACHIEVEMENTS);
    }

    private List<Achievement> generateTheWinnerTeamGoldTournament(Tournament tournament) {
        return generateIncrementalGradedTournament(tournament, AchievementType.THE_WINNER_TEAM, AchievementGrade.GOLD,
                WINNER_GOLD_NUMBER, DAYS_TO_CHECK_INCREMENTAL_ACHIEVEMENTS);
    }
}
