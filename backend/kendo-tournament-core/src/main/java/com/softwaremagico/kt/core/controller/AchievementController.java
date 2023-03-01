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
import com.softwaremagico.kt.core.exceptions.ParticipantNotFoundException;
import com.softwaremagico.kt.core.exceptions.TournamentNotFoundException;
import com.softwaremagico.kt.core.providers.*;
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
    private final TournamentConverter tournamentConverter;

    private final TournamentProvider tournamentProvider;

    private final ParticipantProvider participantProvider;

    private final ParticipantConverter participantConverter;

    private final RoleProvider roleProvider;

    private final AchievementProvider achievementProvider;

    private final DuelProvider duelProvider;

    private List<Role> rolesFromTournament;

    private List<Participant> participantsFromTournament;


    protected AchievementController(AchievementProvider provider, AchievementConverter converter,
                                    TournamentConverter tournamentConverter, TournamentProvider tournamentProvider,
                                    ParticipantProvider participantProvider, ParticipantConverter participantConverter,
                                    RoleProvider roleProvider, AchievementProvider achievementProvider, DuelProvider duelProvider) {
        super(provider, converter);
        this.tournamentConverter = tournamentConverter;
        this.tournamentProvider = tournamentProvider;
        this.participantProvider = participantProvider;
        this.participantConverter = participantConverter;
        this.roleProvider = roleProvider;
        this.achievementProvider = achievementProvider;
        this.duelProvider = duelProvider;
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
        return converter.convertAll(provider.get(participant).stream().map(this::createConverterRequest).collect(Collectors.toList()));
    }

    public List<AchievementDTO> getParticipantAchievements(ParticipantDTO participantDTO) {
        return converter.convertAll(provider.get(participantConverter.reverse(participantDTO))
                .stream().map(this::createConverterRequest).collect(Collectors.toList()));
    }

    public List<AchievementDTO> getAchievements(TournamentDTO tournamentDTO, AchievementType achievementType) {
        return converter.convertAll(provider.get(tournamentConverter.reverse(tournamentDTO), achievementType)
                .stream().map(this::createConverterRequest).collect(Collectors.toList()));
    }

    public List<AchievementDTO> getAchievements(AchievementType achievementType) {
        return converter.convertAll(provider.get(achievementType)
                .stream().map(this::createConverterRequest).collect(Collectors.toList()));
    }

    public List<AchievementDTO> getTournamentAchievements(Integer tournamentId) {
        final Tournament tournament = tournamentProvider.get(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(getClass(), "No tournament found with id '" + tournamentId + "'."));
        return converter.convertAll(provider.get(tournament).stream().map(this::createConverterRequest).collect(Collectors.toList()));
    }

    public void generateAchievements(TournamentDTO tournamentDTO) {
        final Tournament tournament = tournamentConverter.reverse(tournamentDTO);
        //Remove any achievement already calculated.
        provider.delete(tournament);

        //Generate new ones.
        generateBillyTheKidAchievement(tournament);
        generateLethalWeaponAchievement(tournament);
        generateTerminatorAchievement(tournament);
        generateJuggernautAchievement(tournament);
        generateTheKingAchievement(tournament);
        generateLooksGoodFromFarAwayButAchievementBronze(tournament);
        generateLooksGoodFromFarAwayButAchievementSilver(tournament);
        generateLooksGoodFromFarAwayButAchievementGold(tournament);
        generateILoveTheFlagsAchievementBronze(tournament);
        generateILoveTheFlagsAchievementSilver(tournament);
        generateILoveTheFlagsAchievementGold(tournament);
        generateTheRockAchievement(tournament);
        generateTheTowerAchievement(tournament);
        generateTheCastleAchievement(tournament);
        generateEntrenchedAchievement(tournament);
        generateALittleOfEverythingAchievement(tournament);
        generateALittleOfEverythingSilverAchievement(tournament);
        generateALittleOfEverythingGoldenAchievement(tournament);
        generateBoneBreakerAchievement(tournament);
        generateWoodcutterAchievement(tournament);
        generateFlexibleAsBambooAchievement(tournament);
        generateSweatyTenuguiAchievement(tournament);
    }

    /**
     * Achievement for the quickest score in a tournament.
     *
     * @param tournament The tournament to check.
     */
    private void generateBillyTheKidAchievement(Tournament tournament) {

    }

    /**
     * If somebody has done the maximum score on a tournament.
     *
     * @param tournament The tournament to check.
     */
    private void generateLethalWeaponAchievement(Tournament tournament) {

    }

    /**
     * If somebody has done the maximum score on two consecutive tournaments.
     *
     * @param tournament The tournament to check.
     */
    private void generateTerminatorAchievement(Tournament tournament) {

    }

    /**
     * If somebody has done the maximum score on three consecutive tournaments.
     *
     * @param tournament The tournament to check.
     */
    private void generateJuggernautAchievement(Tournament tournament) {

    }

    /**
     * The one that has last longer on the King of the Mountain mode.
     *
     * @param tournament The tournament to check.
     */
    private void generateTheKingAchievement(Tournament tournament) {

    }

    /**
     * When somebody is as an organizer for at least three consecutive tournaments.
     *
     * @param tournament The tournament to check.
     */
    private void generateLooksGoodFromFarAwayButAchievementBronze(Tournament tournament) {

    }

    /**
     * When somebody is as an organizer for at least five consecutive tournaments.
     *
     * @param tournament The tournament to check.
     */
    private void generateLooksGoodFromFarAwayButAchievementSilver(Tournament tournament) {

    }

    /**
     * When somebody is as an organizer for at least seven consecutive tournaments.
     *
     * @param tournament The tournament to check.
     */
    private void generateLooksGoodFromFarAwayButAchievementGold(Tournament tournament) {

    }

    /**
     * When somebody is a referee for at least three consecutive tournaments.
     *
     * @param tournament The tournament to check.
     */
    private void generateILoveTheFlagsAchievementBronze(Tournament tournament) {

    }

    /**
     * When somebody is a referee for at least four consecutive tournaments.
     *
     * @param tournament The tournament to check.
     */
    private void generateILoveTheFlagsAchievementSilver(Tournament tournament) {

    }

    /**
     * When somebody is a referee for at least five consecutive tournaments.
     *
     * @param tournament The tournament to check.
     */
    private void generateILoveTheFlagsAchievementGold(Tournament tournament) {

    }

    /**
     * When somebody has participated on a tournament and nobody has scored a hit against him/her.
     *
     * @param tournament The tournament to check.
     */
    private void generateTheRockAchievement(Tournament tournament) {

    }

    /**
     * When somebody has participated on two consecutive tournaments and nobody has scored a hit against him/her.
     *
     * @param tournament The tournament to check.
     */
    private void generateTheTowerAchievement(Tournament tournament) {

    }

    /**
     * When somebody has participated on three consecutive tournaments and nobody has scored a hit against him/her.
     *
     * @param tournament The tournament to check.
     */
    private void generateTheCastleAchievement(Tournament tournament) {

    }

    /**
     * When somebody has participated on a tournament and nobody has scored a hit against him/her and
     * he does neither score a hit against his opponents.
     *
     * @param tournament The tournament to check.
     */
    private void generateEntrenchedAchievement(Tournament tournament) {

    }

    /**
     * When all points are scored: Men, Kote, Do.
     *
     * @param tournament The tournament to check.
     */
    private void generateALittleOfEverythingAchievement(Tournament tournament) {

    }

    /**
     * When all points are scored: Men, Kote, Do and Hansoku.
     *
     * @param tournament The tournament to check.
     */
    private void generateALittleOfEverythingSilverAchievement(Tournament tournament) {

    }

    /**
     * When all points are scored: Men, Kote, Do and Hansoku and Ippon.
     *
     * @param tournament The tournament to check.
     */
    private void generateALittleOfEverythingGoldenAchievement(Tournament tournament) {

    }

    /**
     * When somebody loose a combat only by Hansokus.
     *
     * @param tournament The tournament to check.
     */
    private void generateBoneBreakerAchievement(Tournament tournament) {
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
        generateAchievement(AchievementType.BONE_BREAKER, AchievementGrade.NORMAL, participants, tournament);
    }

    /**
     * When somebody only perform 'do' scores and win a duel.
     *
     * @param tournament The tournament to check.
     */
    private void generateWoodcutterAchievement(Tournament tournament) {
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
        generateAchievement(AchievementType.WOODCUTTER, AchievementGrade.NORMAL, woodcutters, tournament);
    }


    /**
     * When somebody has performed all the roles
     *
     * @param tournament The tournament to check.
     */
    private void generateFlexibleAsBambooAchievement(Tournament tournament) {
        //Get all participants from a tournament that has almost all roles in any tournament,
        final List<Participant> participants = participantProvider.get(tournament, RoleType.values().length / 2 + 1);
        //Remove the ones already have the achievement.
        participants.removeAll(participantProvider.getParticipantsWithAchievementFromList(AchievementType.FLEXIBLE_AS_BAMBOO, participants));
        //Create new achievement for the participants.
        generateAchievement(AchievementType.FLEXIBLE_AS_BAMBOO, AchievementGrade.NORMAL, participants, tournament);
    }

    /**
     * First tournament as a competitor
     *
     * @param tournament The tournament to check.
     */
    private void generateSweatyTenuguiAchievement(Tournament tournament) {
        final List<Participant> participants = participantProvider.getParticipantFirstTimeCompetitors(tournament);
        generateAchievement(AchievementType.SWEATY_TENUGUI, AchievementGrade.NORMAL, participants, tournament);
    }

    private void generateAchievement(AchievementType achievementType, AchievementGrade achievementGrade,
                                     Collection<Participant> participants, Tournament tournament) {
        final List<Achievement> achievements = new ArrayList<>();
        participants.forEach(participant -> {
            achievements.add(new Achievement(participant, tournament, achievementType, achievementGrade));
        });
        achievementProvider.saveAll(achievements);
    }

}
