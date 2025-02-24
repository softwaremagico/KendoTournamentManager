package com.softwaremagico.kt.core.controller;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2025 Softwaremagico
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
import com.softwaremagico.kt.core.providers.AchievementProvider;
import com.softwaremagico.kt.core.providers.DuelProvider;
import com.softwaremagico.kt.core.providers.FightProvider;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.core.providers.RankingProvider;
import com.softwaremagico.kt.core.providers.RoleProvider;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.core.score.ScoreOfCompetitor;
import com.softwaremagico.kt.core.score.ScoreOfTeam;
import com.softwaremagico.kt.core.tournaments.BubbleSortTournamentHandler;
import com.softwaremagico.kt.core.tournaments.SenbatsuTournamentHandler;
import com.softwaremagico.kt.persistence.entities.Achievement;
import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Role;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.AchievementRepository;
import com.softwaremagico.kt.persistence.values.AchievementGrade;
import com.softwaremagico.kt.persistence.values.AchievementType;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class AchievementController extends BasicInsertableController<Achievement, AchievementDTO, AchievementRepository,
        AchievementProvider, AchievementConverterRequest, AchievementConverter> {

    private static final int LETHAL_WEAPON_MAX_TIME = 5;

    private static final int DAYS_TO_CHECK_INCREMENTAL_ACHIEVEMENTS = 365;

    private static final int DEFAULT_TOURNAMENT_NUMBER_BRONZE = 2;
    private static final int DEFAULT_TOURNAMENT_NUMBER_SILVER = 3;
    private static final int DEFAULT_TOURNAMENT_NUMBER_GOLD = 5;

    private static final int DEFAULT_TOURNAMENT_LONG_NUMBER_BRONZE = 3;
    private static final int DEFAULT_TOURNAMENT_LONG_NUMBER_SILVER = 5;
    private static final int DEFAULT_TOURNAMENT_LONG_NUMBER_GOLD = 7;

    private static final int DEFAULT_TOURNAMENT_VERY_LONG_NUMBER_BRONZE = 10;
    private static final int DEFAULT_TOURNAMENT_VERY_LONG_NUMBER_SILVER = 20;
    private static final int DEFAULT_TOURNAMENT_VERY_LONG_NUMBER_GOLD = 30;

    private static final int DEFAULT_OCCURRENCES_BY_YEAR_BRONZE = 3;
    private static final int DEFAULT_OCCURRENCES_BY_YEAR_SILVER = 4;
    private static final int DEFAULT_OCCURRENCES_BY_YEAR_GOLD = 5;

    private static final int DEFAULT_SCORE_BRONZE = 20;
    private static final int DEFAULT_SCORE_SILVER = 50;
    private static final int DEFAULT_SCORE_GOLD = 100;

    private static final int MINIMUM_ROLES_BAMBOO_NORMAL = 2;
    private static final int MINIMUM_ROLES_BAMBOO_BRONZE = 3;
    private static final int MINIMUM_ROLES_BAMBOO_SILVER = 4;
    private static final int MINIMUM_ROLES_BAMBOO_GOLD = 5;

    private static final int MINIMUM_LOST_SITH_NORMAL = 3;

    private static final int MINIMUM_LOST_SITH_BRONZE = 5;
    private static final int MINIMUM_LOST_SITH_SILVER = 7;
    private static final int MINIMUM_LOST_SITH_GOLD = 10;

    private static final int PARTICIPANT_YEARS = 5;
    private static final int PARTICIPANT_YEARS_BRONZE = 10;
    private static final int PARTICIPANT_YEARS_SILVER = 15;
    private static final int PARTICIPANT_YEARS_GOLD = 20;

    private static final int DARUMA_TOURNAMENTS_NORMAL = 10;
    private static final int DARUMA_TOURNAMENTS_BRONZE = 20;
    private static final int DARUMA_TOURNAMENTS_SILVER = 30;
    private static final int DARUMA_TOURNAMENTS_GOLD = 50;

    private static final int DETHRONE_THE_KING_NORMAL = 2;
    private static final int DETHRONE_THE_KING_BRONZE = 3;
    private static final int DETHRONE_THE_KING_SILVER = 5;
    private static final int DETHRONE_THE_KING_GOLD = 7;

    private static final int SENBATSU_RUNGS_NORMAL = 3;
    private static final int SENBATSU_RUNGS_BRONZE = 4;
    private static final int SENBATSU_RUNGS_SILVER = 5;
    private static final int SENBATSU_RUNGS_GOLD = 7;

    private static final int MAX_PREVIOUS_TOURNAMENTS = 100;
    private static final int MIN_TOURNAMENT_FIGHTS = 5;


    private final TournamentConverter tournamentConverter;

    private final TournamentProvider tournamentProvider;

    private final ParticipantProvider participantProvider;

    private final ParticipantConverter participantConverter;

    private final RoleProvider roleProvider;

    private final TeamProvider teamProvider;

    private final AchievementProvider achievementProvider;

    private final FightProvider fightProvider;

    private final DuelProvider duelProvider;

    private final RankingProvider rankingProvider;

    private final GroupProvider groupProvider;

    private final BubbleSortTournamentHandler bubbleSortTournamentHandler;

    private final SenbatsuTournamentHandler senbatsuTournamentHandler;

    private Tournament tournament;

    private List<Role> rolesFromTournament;

    private List<Participant> participantsFromTournament;

    private List<Duel> duelsFromTournament;

    private List<Team> teamsFromTournament;

    private List<Fight> fightsFromTournament;

    private Map<Participant, List<Score>> scoresByParticipant = null;
    private Map<Participant, List<Score>> scoresReceivedByParticipant = null;
    private Map<Participant, Long> totalScoreFromParticipant = null;
    private Map<Participant, Long> totalScoreAgainstParticipant = null;

    private Map<Participant, List<Role>> rolesByParticipant = null;

    private final Set<AchievementsGeneratedListener> achievementsGeneratedListeners = new HashSet<>();
    private final Set<AchievementsGeneratedAllTournamentsListener> achievementsGeneratedAllTournamentsListeners = new HashSet<>();


    protected AchievementController(AchievementProvider provider, AchievementConverter converter,
                                    TournamentConverter tournamentConverter, TournamentProvider tournamentProvider,
                                    ParticipantProvider participantProvider, ParticipantConverter participantConverter,
                                    RoleProvider roleProvider, TeamProvider teamProvider, AchievementProvider achievementProvider,
                                    FightProvider fightProvider, DuelProvider duelProvider,
                                    RankingProvider rankingProvider, GroupProvider groupProvider,
                                    BubbleSortTournamentHandler bubbleSortTournamentHandler,
                                    SenbatsuTournamentHandler senbatsuTournamentHandler) {
        super(provider, converter);
        this.tournamentConverter = tournamentConverter;
        this.tournamentProvider = tournamentProvider;
        this.participantProvider = participantProvider;
        this.participantConverter = participantConverter;
        this.roleProvider = roleProvider;
        this.teamProvider = teamProvider;
        this.achievementProvider = achievementProvider;
        this.fightProvider = fightProvider;
        this.duelProvider = duelProvider;
        this.rankingProvider = rankingProvider;
        this.groupProvider = groupProvider;
        this.bubbleSortTournamentHandler = bubbleSortTournamentHandler;
        this.senbatsuTournamentHandler = senbatsuTournamentHandler;
    }

    public interface AchievementsGeneratedListener {
        void generated(List<AchievementDTO> achievementsGenerated, TournamentDTO tournament);
    }

    public interface AchievementsGeneratedAllTournamentsListener {
        void generated(List<AchievementDTO> achievementsGenerated, List<TournamentDTO> tournaments);
    }

    public void addAchievementsGeneratedListener(AchievementsGeneratedListener listener) {
        achievementsGeneratedListeners.add(listener);
    }

    public void addAchievementsGeneratedAllTournamentsListener(AchievementsGeneratedAllTournamentsListener listener) {
        achievementsGeneratedAllTournamentsListeners.add(listener);
    }

    @Override
    protected AchievementConverterRequest createConverterRequest(Achievement achievement) {
        return new AchievementConverterRequest(achievement);
    }

    private List<Role> getRolesFromTournament() {
        if (rolesFromTournament == null) {
            rolesFromTournament = roleProvider.getAll(tournament);
        }
        return rolesFromTournament;
    }

    private List<Participant> getParticipantsFromTournament() {
        if (participantsFromTournament == null) {
            participantsFromTournament = participantProvider.get(tournament);
        }
        return participantsFromTournament;
    }

    private List<Duel> getDuelsFromTournament() {
        if (duelsFromTournament == null) {
            duelsFromTournament = duelProvider.get(tournament);
        }
        return duelsFromTournament;
    }

    private List<Team> getTeamsFromTournament() {
        if (teamsFromTournament == null) {
            teamsFromTournament = teamProvider.getAll(tournament);
        }
        return teamsFromTournament;
    }

    private List<Fight> getFightsFromTournament() {
        if (fightsFromTournament == null) {
            fightsFromTournament = fightProvider.getFights(tournament);
        }
        return fightsFromTournament;
    }

    private Map<Participant, List<Score>> getScoresByParticipant() {
        if (scoresByParticipant == null) {
            scoresByParticipant = new HashMap<>();
            getDuelsFromTournament().forEach(duel -> {
                scoresByParticipant.computeIfAbsent(duel.getCompetitor1(), k -> new ArrayList<>());
                scoresByParticipant.computeIfAbsent(duel.getCompetitor2(), k -> new ArrayList<>());
                duel.getCompetitor1Score().forEach(score -> {
                    scoresByParticipant.get(duel.getCompetitor1()).add(score);
                });
                duel.getCompetitor2Score().forEach(score -> {
                    scoresByParticipant.get(duel.getCompetitor2()).add(score);
                });
            });
        }
        return scoresByParticipant;
    }

    private Map<Participant, List<Score>> getScoresReceivedByParticipant() {
        if (scoresReceivedByParticipant == null) {
            scoresReceivedByParticipant = new HashMap<>();
            getDuelsFromTournament().forEach(duel -> {
                scoresReceivedByParticipant.computeIfAbsent(duel.getCompetitor1(), k -> new ArrayList<>());
                scoresReceivedByParticipant.computeIfAbsent(duel.getCompetitor2(), k -> new ArrayList<>());
                duel.getCompetitor1Score().forEach(score -> {
                    scoresReceivedByParticipant.get(duel.getCompetitor2()).add(score);
                });
                duel.getCompetitor2Score().forEach(score -> {
                    scoresReceivedByParticipant.get(duel.getCompetitor1()).add(score);
                });
            });
        }
        return scoresReceivedByParticipant;
    }

    private Map<Participant, Long> getTotalScoreFromParticipant() {
        if (totalScoreFromParticipant == null) {
            totalScoreFromParticipant = new HashMap<>();

            final List<Tournament> previousTournaments = tournamentProvider.getPreviousTo(tournament, MAX_PREVIOUS_TOURNAMENTS);
            //Also current tournament!
            previousTournaments.add(0, tournament);

            getParticipantsFromTournament().forEach(participant ->
                    totalScoreFromParticipant.put(participant, duelProvider.countScoreFromCompetitor(participant, previousTournaments)));
        }
        return totalScoreFromParticipant;
    }

    private Map<Participant, Long> getTotalScoreAgainstParticipant() {
        if (totalScoreAgainstParticipant == null) {
            totalScoreAgainstParticipant = new HashMap<>();

            final List<Tournament> previousTournaments = tournamentProvider.getPreviousTo(tournament, MAX_PREVIOUS_TOURNAMENTS);
            //Also current tournament!
            previousTournaments.add(0, tournament);

            getParticipantsFromTournament().forEach(participant ->
                    totalScoreAgainstParticipant.put(participant, duelProvider.countScoreAgainstCompetitor(participant, previousTournaments)));
        }
        return totalScoreAgainstParticipant;
    }

    private Map<Participant, List<Role>> getRolesByParticipant() {
        if (rolesByParticipant == null) {
            final List<Role> roles = roleProvider.getBy(getParticipantsFromTournament());
            rolesByParticipant = roles.stream().collect(Collectors.groupingBy(Role::getParticipant));
        }
        return rolesByParticipant;
    }

    private Map<Participant, List<Role>> getRolesByParticipantUntil(Tournament tournament) {
        final Map<Participant, List<Role>> roles = new HashMap<>();
        for (Map.Entry<Participant, List<Role>> entry : getRolesByParticipant().entrySet()) {
            roles.put(entry.getKey(), entry.getValue().stream().filter(role ->
                    role.getTournament().getCreatedAt() != null && role.getTournament().getCreatedAt().isBefore(tournament.getCreatedAt())).toList());
        }
        return roles;
    }


    public List<AchievementDTO> getParticipantAchievements(Integer participantId) {
        final Participant participant = participantProvider.get(participantId)
                .orElseThrow(() -> new ParticipantNotFoundException(getClass(), "No participant found with id '" + participantId + "'."));
        return convertAll(getProvider().get(participant));
    }

    public List<AchievementDTO> getParticipantAchievements(ParticipantDTO participantDTO) {
        return convertAll(getProvider().get(participantConverter.reverse(participantDTO)));
    }

    public List<AchievementDTO> getParticipantAchievements(TournamentDTO tournamentDTO, ParticipantDTO participantDTO) {
        return convertAll(getProvider().get(tournamentConverter.reverse(tournamentDTO), participantConverter.reverse(participantDTO)));
    }

    public List<AchievementDTO> getAchievements(TournamentDTO tournamentDTO, AchievementType achievementType) {
        return convertAll(getProvider().get(tournamentConverter.reverse(tournamentDTO), achievementType));
    }

    public List<AchievementDTO> getAchievements(TournamentDTO tournamentDTO) {
        return convertAll(getProvider().get(tournamentConverter.reverse(tournamentDTO)));
    }

    public List<AchievementDTO> getAchievements(TournamentDTO tournamentDTO, AchievementType achievementType, AchievementGrade achievementGrade) {
        return convertAll(getProvider().get(tournamentConverter.reverse(tournamentDTO), achievementType, achievementGrade));
    }

    public List<AchievementDTO> getAchievements(AchievementType achievementType) {
        return convertAll(getProvider().get(achievementType));
    }

    public List<AchievementDTO> getAchievements(AchievementType achievementType, AchievementGrade achievementGrade) {
        return convertAll(getProvider().get(achievementType, achievementGrade));
    }

    public List<AchievementDTO> getTournamentAchievements(Integer tournamentId) {
        final Tournament tournament = tournamentProvider.get(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(getClass(), "No tournament found with id '" + tournamentId + "'."));
        return convertAll(getProvider().get(tournament));
    }

    public List<AchievementDTO> regenerateAllAchievements() {
        final List<TournamentDTO> tournaments = tournamentConverter.convertAll(tournamentProvider.getAll().stream()
                .map(TournamentConverterRequest::new).collect(Collectors.toList()));
        tournaments.sort(Comparator.comparing(TournamentDTO::getCreatedAt));
        final List<AchievementDTO> achievementsGenerated = new ArrayList<>();
        achievementProvider.deleteAll();
        for (final TournamentDTO tournament : tournaments) {
            achievementsGenerated.addAll(generateAchievements(tournament));
        }
        for (AchievementsGeneratedAllTournamentsListener achievementsGeneratedAllTournamentsListener : achievementsGeneratedAllTournamentsListeners) {
            achievementsGeneratedAllTournamentsListener.generated(achievementsGenerated, tournaments);
        }
        return achievementsGenerated;
    }

    public List<AchievementDTO> regenerateAchievements(Integer tournamentId) {
        final TournamentDTO tournament = tournamentConverter.convert(new TournamentConverterRequest(tournamentProvider.get(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(getClass(), "No tournament found with id '" + tournamentId + "'."))));
        deleteAchievements(tournament);
        final List<AchievementDTO> achievementsGenerated = generateAchievements(tournament);
        achievementsGeneratedListeners.forEach(achievementsGeneratedListener
                -> achievementsGeneratedListener.generated(achievementsGenerated, tournament));
        return achievementsGenerated;
    }

    private void deleteAchievements(TournamentDTO tournamentDTO) {
        getProvider().delete(tournamentConverter.reverse(tournamentDTO));
    }

    public List<AchievementDTO> generateAchievements(TournamentDTO tournamentDTO) {
        tournament = tournamentConverter.reverse(tournamentDTO);
        this.duelsFromTournament = null;
        this.rolesFromTournament = null;
        this.participantsFromTournament = null;
        this.scoresByParticipant = null;
        this.scoresReceivedByParticipant = null;
        this.totalScoreFromParticipant = null;
        this.totalScoreAgainstParticipant = null;
        this.rolesByParticipant = null;
        this.fightsFromTournament = null;
        this.teamsFromTournament = null;

        //Remove any achievement already calculated.
        getProvider().delete(tournament);

        final List<Achievement> achievementsGenerated = new ArrayList<>();

        //Generate first the normal ones.
        achievementsGenerated.addAll(generateBillyTheKidAchievement(tournament));
        achievementsGenerated.addAll(generateLethalWeaponAchievement(tournament));
        achievementsGenerated.addAll(generateTerminatorAchievement(tournament));
        final List<Achievement> juggernautAchievements = generateJuggernautAchievement(tournament);
        //Juggernaut includes Terminator.
        removeAchievements(achievementsGenerated, AchievementType.TERMINATOR, Collections.singletonList(AchievementGrade.NORMAL),
                juggernautAchievements.stream().map(Achievement::getParticipant).collect(Collectors.toSet()));
        achievementsGenerated.addAll(juggernautAchievements);
        achievementsGenerated.addAll(generateTheKingAchievement(tournament));
        achievementsGenerated.addAll(generateLooksGoodFromFarAwayButAchievement(tournament));
        achievementsGenerated.addAll(generateILoveTheFlagsAchievement(tournament));
        achievementsGenerated.addAll(generateLoveSharingAchievement(tournament));
        achievementsGenerated.addAll(generateTheNeverEndingStoryAchievement(tournament));
        achievementsGenerated.addAll(generateMasterTheLoopAchievement(tournament));
        achievementsGenerated.addAll(generateTheCastleAchievement(tournament));
        //Juggernaut includes The Castle.
        removeAchievements(achievementsGenerated, AchievementType.THE_CASTLE, Collections.singletonList(AchievementGrade.NORMAL),
                juggernautAchievements.stream().map(Achievement::getParticipant).collect(Collectors.toSet()));
        final List<Achievement> entrenchedAchievements = generateEntrenchedAchievement(tournament);
        achievementsGenerated.addAll(entrenchedAchievements);
        //Entrenchment includes the castle.
        removeAchievements(achievementsGenerated, AchievementType.THE_CASTLE, Collections.singletonList(AchievementGrade.NORMAL),
                entrenchedAchievements.stream().map(Achievement::getParticipant).collect(Collectors.toSet()));
        achievementsGenerated.addAll(generateBoneBreakerAchievement(tournament));
        achievementsGenerated.addAll(generateWoodcutterAchievement(tournament));
        achievementsGenerated.addAll(generateFlexibleAsBambooAchievement(tournament));
        achievementsGenerated.addAll(generateSweatyTenuguiAchievement(tournament));
        achievementsGenerated.addAll(generateTheWinnerTournament(tournament));
        achievementsGenerated.addAll(generateTheWinnerTeamTournament(tournament));
        achievementsGenerated.addAll(generateTisButAScratchAchievement(tournament));
        achievementsGenerated.addAll(generateFirstBloodAchievement(tournament));
        achievementsGenerated.addAll(generateDarumaAchievement(tournament));
        achievementsGenerated.addAll(generateStormtrooperSyndromeAchievement(tournament));
        achievementsGenerated.addAll(generateVendettaAchievement(tournament));
        achievementsGenerated.addAll(generateSithApprenticesAlwaysKillTheirMasterAchievement(tournament));
        achievementsGenerated.addAll(generateDethroneTheKingAchievement(tournament));
        achievementsGenerated.addAll(generateClimbTheLadderAchievement(tournament));

        // Now generate extra grades.
        achievementsGenerated.addAll(generateBillyTheKidAchievementBronze(tournament));
        achievementsGenerated.addAll(generateBillyTheKidAchievementSilver(tournament));
        achievementsGenerated.addAll(generateBillyTheKidAchievementGold(tournament));
        achievementsGenerated.addAll(generateLethalWeaponAchievementBronze(tournament));
        achievementsGenerated.addAll(generateLethalWeaponAchievementSilver(tournament));
        achievementsGenerated.addAll(generateLethalWeaponAchievementGold(tournament));
        achievementsGenerated.addAll(generateTerminatorAchievementBronze(tournament));
        achievementsGenerated.addAll(generateTerminatorAchievementSilver(tournament));
        achievementsGenerated.addAll(generateTerminatorAchievementGold(tournament));
        achievementsGenerated.addAll(generateJuggernautAchievementBronze(tournament));
        achievementsGenerated.addAll(generateJuggernautAchievementSilver(tournament));
        achievementsGenerated.addAll(generateJuggernautAchievementGold(tournament));
        achievementsGenerated.addAll(generateTheKingAchievementBronze(tournament));
        achievementsGenerated.addAll(generateTheKingAchievementSilver(tournament));
        achievementsGenerated.addAll(generateTheKingAchievementGold(tournament));
        achievementsGenerated.addAll(generateLooksGoodFromFarAwayButAchievementBronze(tournament));
        achievementsGenerated.addAll(generateLooksGoodFromFarAwayButAchievementSilver(tournament));
        achievementsGenerated.addAll(generateLooksGoodFromFarAwayButAchievementGold(tournament));
        achievementsGenerated.addAll(generateILoveTheFlagsAchievementBronze(tournament));
        achievementsGenerated.addAll(generateILoveTheFlagsAchievementSilver(tournament));
        achievementsGenerated.addAll(generateILoveTheFlagsAchievementGold(tournament));
        achievementsGenerated.addAll(generateLoveSharingAchievementBronze(tournament));
        achievementsGenerated.addAll(generateLoveSharingAchievementSilver(tournament));
        achievementsGenerated.addAll(generateLoveSharingAchievementGold(tournament));
        achievementsGenerated.addAll(generateTheCastleAchievementBronze(tournament));
        achievementsGenerated.addAll(generateTheCastleAchievementSilver(tournament));
        achievementsGenerated.addAll(generateTheCastleAchievementGold(tournament));
        achievementsGenerated.addAll(generateEntrenchedAchievementBronze(tournament));
        achievementsGenerated.addAll(generateEntrenchedAchievementSilver(tournament));
        achievementsGenerated.addAll(generateEntrenchedAchievementGold(tournament));
        achievementsGenerated.addAll(generateALittleOfEverythingAchievement(tournament));
        achievementsGenerated.addAll(generateALittleOfEverythingAchievementBronze(tournament));
        final List<Achievement> aLittleOfEverythingSilver = generateALittleOfEverythingAchievementSilver(tournament);
        //Delete the Bronzes, as Silver includes them
        removeAchievements(achievementsGenerated, AchievementType.A_LITTLE_OF_EVERYTHING, AchievementGrade.SILVER.getLessThan(),
                aLittleOfEverythingSilver.stream().map(Achievement::getParticipant).collect(Collectors.toSet()));
        achievementsGenerated.addAll(aLittleOfEverythingSilver);
        final List<Achievement> aLittleOfEverythingGold = generateALittleOfEverythingAchievementGold(tournament);
        //Delete the Silver, as Gold includes them
        removeAchievements(achievementsGenerated, AchievementType.A_LITTLE_OF_EVERYTHING, AchievementGrade.GOLD.getLessThan(),
                aLittleOfEverythingSilver.stream().map(Achievement::getParticipant).collect(Collectors.toSet()));
        achievementsGenerated.addAll(aLittleOfEverythingGold);
        achievementsGenerated.addAll(generateSweatyTenuguiAchievementBronze(tournament));
        achievementsGenerated.addAll(generateSweatyTenuguiAchievementSilver(tournament));
        achievementsGenerated.addAll(generateSweatyTenuguiAchievementGold(tournament));
        achievementsGenerated.addAll(generateTheWinnerAchievementBronze(tournament));
        achievementsGenerated.addAll(generateTheWinnerAchievementSilver(tournament));
        achievementsGenerated.addAll(generateTheWinnerAchievementGold(tournament));
        achievementsGenerated.addAll(generateTheWinnerTeamAchievementBronze(tournament));
        achievementsGenerated.addAll(generateTheWinnerTeamAchievementSilver(tournament));
        achievementsGenerated.addAll(generateTheWinnerTeamAchievementGold(tournament));
        achievementsGenerated.addAll(generateMasterTheLoopAchievementBronze(tournament));
        achievementsGenerated.addAll(generateMasterTheLoopAchievementSilver(tournament));
        achievementsGenerated.addAll(generateMasterTheLoopAchievementGold(tournament));
        achievementsGenerated.addAll(generateTheNeverEndingStoryAchievementBronze(tournament));
        achievementsGenerated.addAll(generateTheNeverEndingStoryAchievementSilver(tournament));
        achievementsGenerated.addAll(generateTheNeverEndingStoryAchievementGold(tournament));
        achievementsGenerated.addAll(generateTisButAScratchAchievementBronze(tournament));
        achievementsGenerated.addAll(generateTisButAScratchAchievementSilver(tournament));
        achievementsGenerated.addAll(generateTisButAScratchAchievementGold(tournament));
        achievementsGenerated.addAll(generateFirstBloodAchievementBronze(tournament));
        achievementsGenerated.addAll(generateFirstBloodAchievementSilver(tournament));
        achievementsGenerated.addAll(generateFirstBloodAchievementGold(tournament));
        achievementsGenerated.addAll(generateWoodcutterAchievementBronze(tournament));
        achievementsGenerated.addAll(generateWoodcutterAchievementSilver(tournament));
        achievementsGenerated.addAll(generateWoodcutterAchievementGold(tournament));
        achievementsGenerated.addAll(generateFlexibleAsBambooAchievementBronze(tournament));
        achievementsGenerated.addAll(generateFlexibleAsBambooAchievementSilver(tournament));
        achievementsGenerated.addAll(generateFlexibleAsBambooAchievementGold(tournament));
        achievementsGenerated.addAll(generateDarumaAchievementBronze(tournament));
        achievementsGenerated.addAll(generateDarumaAchievementSilver(tournament));
        achievementsGenerated.addAll(generateDarumaAchievementGold(tournament));
        achievementsGenerated.addAll(generateStormtrooperSyndromeAchievementBronze(tournament));
        achievementsGenerated.addAll(generateStormtrooperSyndromeAchievementSilver(tournament));
        achievementsGenerated.addAll(generateStormtrooperSyndromeAchievementGold(tournament));
        achievementsGenerated.addAll(generateVendettaAchievementBronze(tournament));
        achievementsGenerated.addAll(generateVendettaAchievementSilver(tournament));
        achievementsGenerated.addAll(generateVendettaAchievementGold(tournament));
        return convertAll(achievementsGenerated);
    }

    private void removeAchievements(List<Achievement> achievements, AchievementType achievementType,
                                    Collection<AchievementGrade> grades, Collection<Participant> participants) {
        final List<Achievement> sourceAchievements = new ArrayList<>(achievements);
        achievements.removeIf(achievement -> grades.contains(achievement.getAchievementGrade())
                && achievement.getAchievementType() == achievementType && participants.contains(achievement.getParticipant()));
        sourceAchievements.removeAll(achievements);
        //Delete from database the ones that has been removed here.
        achievementProvider.delete(sourceAchievements);
    }

    /**
     * Generate achievements based on normal achievements grades already on database. A new grade is generated if some consecutive
     * tournaments achievements exists. Note that a grade cannot be a multiplier of the previous
     * grade level to work properly
     *
     * @param tournament             the tournament to check
     * @param consecutiveTournaments the total consecutive tournaments needed to have the achievement.
     * @param achievementType        the achievement to check
     * @param achievementGrade       the grade to generate.
     * @return a list of new achievements.
     */
    private List<Achievement> generateConsecutiveGradeAchievements(Tournament tournament, int consecutiveTournaments,
                                                                   AchievementType achievementType, AchievementGrade achievementGrade) {
        if (achievementGrade == null || achievementGrade.equals(AchievementGrade.NORMAL)) {
            return new ArrayList<>();
        }
        final List<Tournament> previousTournaments = tournamentProvider.getPreviousTo(tournament, consecutiveTournaments - 1);
        //Also current tournament!
        previousTournaments.add(0, tournament);

        if (previousTournaments.size() < consecutiveTournaments) {
            return new ArrayList<>();
        }

        final Map<Participant, List<Achievement>> achievementsByParticipant = achievementProvider.get(achievementType,
                        Collections.singletonList(AchievementGrade.NORMAL), getParticipantsFromTournament(), previousTournaments).stream()
                .collect(Collectors.groupingBy(Achievement::getParticipant));

        //Remove the ones that does not have all required achievements
        achievementsByParticipant.keySet().removeIf(participant ->
                achievementsByParticipant.get(participant).size() < consecutiveTournaments);

        //Check if already a higher achievement grade has been granted.
        previousTournaments.forEach(previousTournament -> achievementProvider.get(previousTournament, achievementType,
                achievementGrade.getGreaterEqualsThan()).forEach(
                achievementBetterGrade -> achievementsByParticipant.remove(achievementBetterGrade.getParticipant())));

        //Generate desired grade.
        return generateAchievement(achievementType, achievementGrade, achievementsByParticipant.keySet(), tournament);
    }

    /**
     * Generate achievements based on normal achievements grades already on database. Count them in an interval and create
     * an achievement achievementGrade depending on the number of repetitions. Note that a achievementGrade cannot be a multiplier of the previous
     * achievementGrade level to work properly
     *
     * @param tournament       the tournament to check.
     * @param achievementType  achievement to check
     * @param achievementGrade current achievementGrade to check
     * @param amount           number of required achievements to be present on the time range
     * @return a list of achievements
     */
    private List<Achievement> generateGradeAchievementsByDays(Tournament tournament, AchievementType achievementType, AchievementGrade achievementGrade,
                                                              Integer amount) {
        if (achievementGrade == null || achievementGrade.equals(AchievementGrade.NORMAL) || tournament.getCreatedAt() == null) {
            return new ArrayList<>();
        }
        final List<Achievement> winnersAchievements = achievementProvider.getAfter(tournament, achievementType,
                AchievementGrade.NORMAL, tournament.getCreatedAt().minusDays(DAYS_TO_CHECK_INCREMENTAL_ACHIEVEMENTS));
        final List<Achievement> winnersGradeAchievements = achievementProvider.getAfter(tournament, achievementType,
                achievementGrade, tournament.getCreatedAt().minusDays(DAYS_TO_CHECK_INCREMENTAL_ACHIEVEMENTS));
        final List<Participant> participantsWithAchievements = winnersAchievements.stream().map(Achievement::getParticipant).toList();
        final List<Achievement> generatedAchievements = new ArrayList<>();
        for (final Participant participant : participantsWithAchievements) {
            int counter = 0;
            for (final Achievement winnerAchievement : winnersAchievements) {
                if (Objects.equals(winnerAchievement.getParticipant(), participant)
                        //Check that does not exist already a bronze achievement assigned after this one.
                        && winnersGradeAchievements.stream().filter(achievement ->
                        Objects.equals(achievement.getParticipant(), participant)
                                && achievement.getCreatedAt().isAfter(winnerAchievement.getCreatedAt())
                ).findAny().isEmpty()) {
                    counter++;
                }
            }
            if (counter >= amount) {
                generatedAchievements.addAll(generateAchievement(AchievementType.THE_WINNER, achievementGrade,
                        Collections.singletonList(participant), tournament));
            }
        }
        return generatedAchievements;
    }

    /**
     * Achievement for the quickest score in a tournament.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateBillyTheKidAchievement(Tournament tournament) {
        if (getFightsFromTournament().size() < MIN_TOURNAMENT_FIGHTS) {
            return new ArrayList<>();
        }
        int minTime = tournament.getDuelsDuration();
        Participant participant = null;
        for (final Duel duel : getDuelsFromTournament()) {
            for (final Integer time : duel.getCompetitor1ScoreTime()) {
                if (time == null) {
                    continue;
                }
                //Billy cannot be a draw time.
                if (time == minTime && !Objects.equals(participant, duel.getCompetitor1())) {
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
                //Billy cannot be a draw time.
                if (time == minTime && !Objects.equals(participant, duel.getCompetitor2())) {
                    participant = null;
                } else if (time < minTime && time > Duel.DEFAULT_DURATION) {
                    participant = duel.getCompetitor2();
                    minTime = time;
                }
            }
        }
        //Create new achievement for the participants.
        if (participant != null) {
            return generateAchievement(AchievementType.BILLY_THE_KID, AchievementGrade.NORMAL, Collections.singleton(participant), tournament);
        }
        return new ArrayList<>();
    }

    /**
     * If somebody has performed a score in less than 5 seconds.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateLethalWeaponAchievement(Tournament tournament) {
        final Set<Duel> duels = duelProvider.findByScorePerformedInLessThan(tournament, LETHAL_WEAPON_MAX_TIME);
        final Set<Participant> participants = new HashSet<>();
        duels.forEach(duel -> {
            duel.getCompetitor1ScoreTime().forEach(time -> {
                if (time != null && time <= LETHAL_WEAPON_MAX_TIME) {
                    participants.add(duel.getCompetitor1());
                }
            });
            duel.getCompetitor2ScoreTime().forEach(time -> {
                if (time != null && time <= LETHAL_WEAPON_MAX_TIME) {
                    participants.add(duel.getCompetitor2());
                }
            });

        });
        //Create new achievement for the participants.
        return generateAchievement(AchievementType.LETHAL_WEAPON, AchievementGrade.NORMAL, participants, tournament);
    }

    /**
     * Achievement for the quickest score in two tournaments.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateBillyTheKidAchievementBronze(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_NUMBER_BRONZE, AchievementType.BILLY_THE_KID, AchievementGrade.BRONZE);
    }

    /**
     * Achievement for the quickest score in three tournaments.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateBillyTheKidAchievementSilver(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_NUMBER_SILVER, AchievementType.BILLY_THE_KID, AchievementGrade.SILVER);
    }

    /**
     * Achievement for the quickest score in five tournaments.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateBillyTheKidAchievementGold(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_NUMBER_GOLD, AchievementType.BILLY_THE_KID, AchievementGrade.GOLD);
    }

    /**
     * If somebody has performed a score in less than 5 seconds for at least two consecutive tournaments.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateLethalWeaponAchievementBronze(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_LONG_NUMBER_BRONZE, AchievementType.LETHAL_WEAPON, AchievementGrade.BRONZE);
    }

    /**
     * If somebody has performed a score in less than 5 seconds for at least three consecutive tournaments.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateLethalWeaponAchievementSilver(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_LONG_NUMBER_SILVER, AchievementType.LETHAL_WEAPON, AchievementGrade.SILVER);
    }

    /**
     * If somebody has performed a score in less than 5 seconds for at least five consecutive tournaments.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateLethalWeaponAchievementGold(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_LONG_NUMBER_GOLD, AchievementType.LETHAL_WEAPON, AchievementGrade.GOLD);
    }

    /**
     * If somebody has done the maximum possible score on a tournament.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateTerminatorAchievement(Tournament tournament) {
        if (getFightsFromTournament().size() < MIN_TOURNAMENT_FIGHTS) {
            return new ArrayList<>();
        }
        final List<Participant> competitors = participantProvider.get(tournament, RoleType.COMPETITOR);
        getDuelsFromTournament().forEach(duel -> {
            if (duel.getCompetitor1Score().size() < Duel.POINTS_TO_WIN) {
                competitors.remove(duel.getCompetitor1());
            }
            if (duel.getCompetitor2Score().size() < Duel.POINTS_TO_WIN) {
                competitors.remove(duel.getCompetitor2());
            }
        });
        return generateAchievement(AchievementType.TERMINATOR, AchievementGrade.NORMAL, competitors, tournament);
    }

    /**
     * If somebody has done the maximum possible score on a tournament for at least two consecutive tournaments.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateTerminatorAchievementBronze(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_NUMBER_BRONZE, AchievementType.TERMINATOR, AchievementGrade.BRONZE);
    }

    /**
     * If somebody has done the maximum possible score on a tournament for at least three consecutive tournaments.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateTerminatorAchievementSilver(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_NUMBER_SILVER, AchievementType.TERMINATOR, AchievementGrade.SILVER);
    }

    /**
     * If somebody has done the maximum possible score on a tournament for at least five consecutive tournaments.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateTerminatorAchievementGold(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_NUMBER_GOLD, AchievementType.TERMINATOR, AchievementGrade.GOLD);
    }

    /**
     * If somebody has done the maximum possible score on a tournament with no score against him.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateJuggernautAchievement(Tournament tournament) {
        final List<Participant> competitors = participantProvider.get(tournament, RoleType.COMPETITOR);
        if (getFightsFromTournament().size() < MIN_TOURNAMENT_FIGHTS) {
            return new ArrayList<>();
        }
        getDuelsFromTournament().forEach(duel -> {
            //Max score competitor 1.
            if (duel.getCompetitor1Score().size() < Duel.POINTS_TO_WIN) {
                competitors.remove(duel.getCompetitor1());
            }
            //No hits against him
            if (!duel.getCompetitor2Score().isEmpty()) {
                competitors.remove(duel.getCompetitor1());
            }
            //Max score competitor 2.
            if (duel.getCompetitor2Score().size() < Duel.POINTS_TO_WIN) {
                competitors.remove(duel.getCompetitor2());
            }
            //No hits against him
            if (!duel.getCompetitor1Score().isEmpty()) {
                competitors.remove(duel.getCompetitor2());
            }
        });
        return generateAchievement(AchievementType.JUGGERNAUT, AchievementGrade.NORMAL, competitors, tournament);
    }

    /**
     * If somebody has done the maximum possible score on a tournament with no score against him for at least two consecutive tournaments
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateJuggernautAchievementBronze(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_NUMBER_BRONZE, AchievementType.JUGGERNAUT, AchievementGrade.BRONZE);
    }

    /**
     * If somebody has done the maximum possible score on a tournament with no score against him for at least three consecutive tournaments
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateJuggernautAchievementSilver(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_NUMBER_SILVER, AchievementType.JUGGERNAUT, AchievementGrade.SILVER);
    }

    /**
     * If somebody has done the maximum possible score on a tournament with no score against him for at least five consecutive tournaments
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateJuggernautAchievementGold(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_NUMBER_GOLD, AchievementType.JUGGERNAUT, AchievementGrade.GOLD);
    }

    /**
     * The one that has last longer on the King of the Mountain mode.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateTheKingAchievement(Tournament tournament) {
        if (tournament.getType() == TournamentType.KING_OF_THE_MOUNTAIN) {
            final List<ScoreOfCompetitor> scoreOfCompetitors = rankingProvider.getCompetitorsScoreRanking(tournament);
            if (!scoreOfCompetitors.isEmpty()) {
                return generateAchievement(AchievementType.THE_KING, AchievementGrade.NORMAL,
                        Collections.singletonList(scoreOfCompetitors.get(0).getCompetitor()), tournament);
            }
        }
        return new ArrayList<>();
    }

    /**
     * Be The king of 3 tournaments in a year
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateTheKingAchievementBronze(Tournament tournament) {
        return generateGradeAchievementsByDays(tournament, AchievementType.THE_KING, AchievementGrade.BRONZE,
                DEFAULT_OCCURRENCES_BY_YEAR_BRONZE);
    }

    /**
     * Be The king of 4 tournaments in a year
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateTheKingAchievementSilver(Tournament tournament) {
        return generateGradeAchievementsByDays(tournament, AchievementType.THE_KING, AchievementGrade.SILVER,
                DEFAULT_OCCURRENCES_BY_YEAR_SILVER);
    }

    /**
     * Be The king of 5 tournaments in a year
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateTheKingAchievementGold(Tournament tournament) {
        return generateGradeAchievementsByDays(tournament, AchievementType.THE_KING, AchievementGrade.GOLD,
                DEFAULT_OCCURRENCES_BY_YEAR_GOLD);
    }

    /**
     * Win a Loop tournament
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateMasterTheLoopAchievement(Tournament tournament) {
        if (tournament.getType() == TournamentType.LOOP) {
            final List<ScoreOfCompetitor> scoreOfCompetitors = rankingProvider.getCompetitorsScoreRanking(tournament);
            if (!scoreOfCompetitors.isEmpty()) {
                return generateAchievement(AchievementType.MASTER_THE_LOOP, AchievementGrade.NORMAL,
                        Collections.singletonList(scoreOfCompetitors.get(0).getCompetitor()), tournament);
            }
        }
        return new ArrayList<>();
    }

    /**
     * Master the loop during 3 tournaments in a year
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateMasterTheLoopAchievementBronze(Tournament tournament) {
        return generateGradeAchievementsByDays(tournament, AchievementType.MASTER_THE_LOOP, AchievementGrade.BRONZE,
                DEFAULT_OCCURRENCES_BY_YEAR_BRONZE);
    }

    /**
     * Master the loop during 4 tournaments in a year
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateMasterTheLoopAchievementSilver(Tournament tournament) {
        return generateGradeAchievementsByDays(tournament, AchievementType.MASTER_THE_LOOP, AchievementGrade.SILVER,
                DEFAULT_OCCURRENCES_BY_YEAR_SILVER);
    }

    /**
     * Master the loop during 5 tournaments in a year
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateMasterTheLoopAchievementGold(Tournament tournament) {
        return generateGradeAchievementsByDays(tournament, AchievementType.MASTER_THE_LOOP, AchievementGrade.GOLD,
                DEFAULT_OCCURRENCES_BY_YEAR_GOLD);
    }

    /**
     * Be a member for more than 5 years.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateTheNeverEndingStoryAchievement(Tournament tournament) {
        //Get older of 10 years
        final List<Participant> participants = getParticipantsFromTournament().stream().filter(participant ->
                        tournament.getCreatedAt() != null && participant.getCreatedAt().isBefore(tournament.getCreatedAt().minusYears(PARTICIPANT_YEARS)))
                .collect(Collectors.toList());
        //Remove the ones already have this achievement.
        final List<Participant> participantsWithThisAchievement = achievementProvider.get(AchievementType.THE_NEVER_ENDING_STORY, AchievementGrade.NORMAL)
                .stream().map(Achievement::getParticipant).toList();
        participants.removeAll(participantsWithThisAchievement);
        return generateAchievement(AchievementType.THE_NEVER_ENDING_STORY, AchievementGrade.NORMAL, participants, tournament);
    }

    /**
     * Be a member for more than 10 years.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateTheNeverEndingStoryAchievementBronze(Tournament tournament) {
        //Get older of 10 years
        if (tournament.getCreatedAt() == null) {
            return new ArrayList<>();
        }
        final List<Participant> participants = getParticipantsFromTournament().stream().filter(participant ->
                participant.getCreatedAt().isBefore(tournament.getCreatedAt().minusYears(PARTICIPANT_YEARS_BRONZE))).collect(Collectors.toList());
        //Remove the ones already have this achievement.
        final List<Participant> participantsWithThisAchievement = achievementProvider.get(AchievementType.THE_NEVER_ENDING_STORY, AchievementGrade.BRONZE)
                .stream().map(Achievement::getParticipant).toList();
        participants.removeAll(participantsWithThisAchievement);
        return generateAchievement(AchievementType.THE_NEVER_ENDING_STORY, AchievementGrade.BRONZE, participants, tournament);
    }

    /**
     * Be a member for more than 15 years.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateTheNeverEndingStoryAchievementSilver(Tournament tournament) {
        //Get older of 10 years
        if (tournament.getCreatedAt() == null) {
            return new ArrayList<>();
        }
        final List<Participant> participants = getParticipantsFromTournament().stream().filter(participant ->
                participant.getCreatedAt().isBefore(tournament.getCreatedAt().minusYears(PARTICIPANT_YEARS_SILVER))).collect(Collectors.toList());
        //Remove the ones already have this achievement.
        final List<Participant> participantsWithThisAchievement = achievementProvider.get(AchievementType.THE_NEVER_ENDING_STORY, AchievementGrade.SILVER)
                .stream().map(Achievement::getParticipant).toList();
        participants.removeAll(participantsWithThisAchievement);
        return generateAchievement(AchievementType.THE_NEVER_ENDING_STORY, AchievementGrade.SILVER, participants, tournament);
    }

    /**
     * Be a member for more than 20 years.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateTheNeverEndingStoryAchievementGold(Tournament tournament) {
        //Get older of 10 years
        if (tournament.getCreatedAt() == null) {
            return new ArrayList<>();
        }
        final List<Participant> participants = getParticipantsFromTournament().stream().filter(participant ->
                participant.getCreatedAt().isBefore(tournament.getCreatedAt().minusYears(PARTICIPANT_YEARS_GOLD))).collect(Collectors.toList());
        //Remove the ones already have this achievement.
        final List<Participant> participantsWithThisAchievement = achievementProvider.get(AchievementType.THE_NEVER_ENDING_STORY, AchievementGrade.GOLD
                )
                .stream().map(Achievement::getParticipant).toList();
        participants.removeAll(participantsWithThisAchievement);
        return generateAchievement(AchievementType.THE_NEVER_ENDING_STORY, AchievementGrade.GOLD, participants, tournament);
    }

    /**
     * Be a tournament organizer
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateLooksGoodFromFarAwayButAchievement(Tournament tournament) {
        final List<Participant> participants = participantProvider.get(tournament, RoleType.ORGANIZER);
        return generateAchievement(AchievementType.LOOKS_GOOD_FROM_FAR_AWAY_BUT, AchievementGrade.NORMAL, participants, tournament);
    }

    /**
     * When somebody is as an organizer for at least two consecutive tournaments.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateLooksGoodFromFarAwayButAchievementBronze(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_LONG_NUMBER_BRONZE, AchievementType.LOOKS_GOOD_FROM_FAR_AWAY_BUT,
                AchievementGrade.BRONZE);
    }

    /**
     * When somebody is as an organizer for at least three consecutive tournaments.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateLooksGoodFromFarAwayButAchievementSilver(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_LONG_NUMBER_SILVER, AchievementType.LOOKS_GOOD_FROM_FAR_AWAY_BUT,
                AchievementGrade.SILVER);
    }

    /**
     * When somebody is as an organizer for at least five consecutive tournaments.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateLooksGoodFromFarAwayButAchievementGold(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_LONG_NUMBER_GOLD,
                AchievementType.LOOKS_GOOD_FROM_FAR_AWAY_BUT, AchievementGrade.GOLD);
    }

    /**
     * First tournament as a referee
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateILoveTheFlagsAchievement(Tournament tournament) {
        final List<Participant> participants = participantProvider.get(tournament, RoleType.REFEREE);
        return generateAchievement(AchievementType.I_LOVE_THE_FLAGS, AchievementGrade.NORMAL, participants, tournament);
    }

    /**
     * When somebody is a referee for at least two consecutive tournaments.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateILoveTheFlagsAchievementBronze(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_LONG_NUMBER_BRONZE, AchievementType.I_LOVE_THE_FLAGS,
                AchievementGrade.BRONZE);
    }

    /**
     * When somebody is a referee for at least three consecutive tournaments.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateILoveTheFlagsAchievementSilver(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_LONG_NUMBER_SILVER, AchievementType.I_LOVE_THE_FLAGS,
                AchievementGrade.SILVER);
    }

    /**
     * When somebody is a referee for at least five consecutive tournaments.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateILoveTheFlagsAchievementGold(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_LONG_NUMBER_GOLD, AchievementType.I_LOVE_THE_FLAGS,
                AchievementGrade.GOLD);
    }

    /**
     * Be a volunteer in a tournament.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateLoveSharingAchievement(Tournament tournament) {
        final List<Participant> participants = participantProvider.get(tournament, RoleType.VOLUNTEER);
        return generateAchievement(AchievementType.LOVE_SHARING, AchievementGrade.NORMAL, participants, tournament);
    }

    /**
     * When somebody is a volunteer for at least three consecutive tournaments.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateLoveSharingAchievementBronze(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_LONG_NUMBER_BRONZE, AchievementType.LOVE_SHARING, AchievementGrade.BRONZE);
    }

    /**
     * When somebody is a volunteer for at least five consecutive tournaments.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateLoveSharingAchievementSilver(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_LONG_NUMBER_SILVER, AchievementType.LOVE_SHARING, AchievementGrade.SILVER);
    }

    /**
     * When somebody is a volunteer for at least seven consecutive tournaments.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateLoveSharingAchievementGold(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_LONG_NUMBER_GOLD, AchievementType.LOVE_SHARING, AchievementGrade.GOLD);
    }


    /**
     * When somebody has participated on a tournament and nobody has scored a hit against him/her.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateTheCastleAchievement(Tournament tournament) {
        if (getFightsFromTournament().size() < MIN_TOURNAMENT_FIGHTS) {
            return new ArrayList<>();
        }
        final List<Participant> competitors = participantProvider.get(tournament, RoleType.COMPETITOR);
        getDuelsFromTournament().forEach(duel -> {
            if (!duel.getCompetitor2Score().isEmpty()) {
                competitors.remove(duel.getCompetitor1());
            }
            if (!duel.getCompetitor1Score().isEmpty()) {
                competitors.remove(duel.getCompetitor2());
            }
        });
        return generateAchievement(AchievementType.THE_CASTLE, AchievementGrade.NORMAL, competitors, tournament);
    }

    /**
     * When somebody has participated on a tournament for at least two consecutive tournaments.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateTheCastleAchievementBronze(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_NUMBER_BRONZE, AchievementType.THE_CASTLE, AchievementGrade.BRONZE);
    }

    /**
     * When somebody has participated on a tournament for at least three consecutive tournaments.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateTheCastleAchievementSilver(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_NUMBER_SILVER, AchievementType.THE_CASTLE, AchievementGrade.SILVER);

    }

    /**
     * When somebody has participated on a tournament for at least five consecutive tournaments.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateTheCastleAchievementGold(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_NUMBER_GOLD, AchievementType.THE_CASTLE, AchievementGrade.GOLD);
    }

    /**
     * When somebody has participated on a tournament and nobody has scored a hit against him/her, and
     * he does neither score a hit against his opponents.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateEntrenchedAchievement(Tournament tournament) {
        if (getFightsFromTournament().size() < MIN_TOURNAMENT_FIGHTS) {
            return new ArrayList<>();
        }
        final List<Participant> competitors = participantProvider.get(tournament, RoleType.COMPETITOR);
        getDuelsFromTournament().forEach(duel -> {
            if (!duel.getCompetitor1Score().isEmpty() || !duel.getCompetitor2Score().isEmpty()) {
                competitors.remove(duel.getCompetitor1());
                competitors.remove(duel.getCompetitor2());
            }
        });
        return generateAchievement(AchievementType.ENTRENCHED, AchievementGrade.NORMAL, competitors, tournament);
    }

    /**
     * When somebody has participated on three tournaments and nobody has scored a hit against him/her, and
     * he does neither score a hit against his opponents.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateEntrenchedAchievementBronze(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_LONG_NUMBER_BRONZE, AchievementType.ENTRENCHED, AchievementGrade.BRONZE);
    }

    /**
     * When somebody has participated on four tournaments and nobody has scored a hit against him/her, and
     * he does neither score a hit against his opponents.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateEntrenchedAchievementSilver(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_LONG_NUMBER_SILVER, AchievementType.ENTRENCHED, AchievementGrade.SILVER);
    }

    /**
     * When somebody has participated on five tournaments and nobody has scored a hit against him/her, and
     * he does neither score a hit against his opponents.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateEntrenchedAchievementGold(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_LONG_NUMBER_GOLD, AchievementType.ENTRENCHED, AchievementGrade.GOLD);
    }

    /**
     * When all points are scored: Men, Kote, Do.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateALittleOfEverythingAchievement(Tournament tournament) {
        final List<Participant> participants = new ArrayList<>();
        getScoresByParticipant().keySet().forEach(participant -> {
            if (getScoresByParticipant().get(participant).contains(Score.MEN)
                    && getScoresByParticipant().get(participant).contains(Score.KOTE)
                    && getScoresByParticipant().get(participant).contains(Score.DO)) {
                participants.add(participant);
            }
        });
        return generateAchievement(AchievementType.A_LITTLE_OF_EVERYTHING, AchievementGrade.BRONZE, participants, tournament);
    }


    /**
     * When all points are scored: Men, Kote, Do and Tsuki.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateALittleOfEverythingAchievementBronze(Tournament tournament) {
        final List<Participant> participants = new ArrayList<>();
        getScoresByParticipant().keySet().forEach(participant -> {
            if (getScoresByParticipant().get(participant).contains(Score.MEN)
                    && getScoresByParticipant().get(participant).contains(Score.KOTE)
                    && getScoresByParticipant().get(participant).contains(Score.DO)
                    && getScoresByParticipant().get(participant).contains(Score.TSUKI)) {
                participants.add(participant);
            }
        });
        return generateAchievement(AchievementType.A_LITTLE_OF_EVERYTHING, AchievementGrade.BRONZE, participants, tournament);
    }

    /**
     * When all points are scored: Men, Kote, Do, Tsuki and Hansoku.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateALittleOfEverythingAchievementSilver(Tournament tournament) {
        final List<Participant> participants = new ArrayList<>();
        getScoresByParticipant().keySet().forEach(participant -> {
            if (getScoresByParticipant().get(participant).contains(Score.MEN)
                    && getScoresByParticipant().get(participant).contains(Score.KOTE)
                    && getScoresByParticipant().get(participant).contains(Score.DO)
                    && getScoresByParticipant().get(participant).contains(Score.TSUKI)
                    && getScoresByParticipant().get(participant).contains(Score.HANSOKU)) {
                participants.add(participant);
            }
        });
        return generateAchievement(AchievementType.A_LITTLE_OF_EVERYTHING, AchievementGrade.SILVER, participants, tournament);
    }

    /**
     * When all points are scored: Men, Kote, Do, Tsuki, Hansoku and Ippon/Fusen-gachi.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateALittleOfEverythingAchievementGold(Tournament tournament) {
        final List<Participant> participants = new ArrayList<>();
        getScoresByParticipant().keySet().forEach(participant -> {
            if (getScoresByParticipant().get(participant).contains(Score.MEN)
                    && getScoresByParticipant().get(participant).contains(Score.KOTE)
                    && getScoresByParticipant().get(participant).contains(Score.DO)
                    && getScoresByParticipant().get(participant).contains(Score.TSUKI)
                    && getScoresByParticipant().get(participant).contains(Score.HANSOKU)
                    && (getScoresByParticipant().get(participant).contains(Score.IPPON)
                    || getScoresByParticipant().get(participant).contains(Score.FUSEN_GACHI))) {
                participants.add(participant);
            }
        });
        return generateAchievement(AchievementType.A_LITTLE_OF_EVERYTHING, AchievementGrade.GOLD, participants, tournament);
    }

    /**
     * When somebody loses a combat only by Hansokus.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
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
     * When somebody only performs 'do' scores in a tournament and all fights has at least a 'do'.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateWoodcutterAchievement(Tournament tournament) {
        if (getFightsFromTournament().size() < MIN_TOURNAMENT_FIGHTS) {
            return new ArrayList<>();
        }
        final List<Duel> duels = new ArrayList<>(getDuelsFromTournament());
        final List<Participant> woodcutters = new ArrayList<>(getParticipantsFromTournament());
        //Only applied to competitors.
        getRolesFromTournament().forEach(role -> {
            if (role.getRoleType() != RoleType.COMPETITOR) {
                woodcutters.remove(role.getParticipant());
            }
        });
        //Must have at least one hit, and cannot have something different that a 'Do'.
        duels.forEach(duel -> {
            if (duel.getCompetitor1Score().isEmpty()) {
                woodcutters.remove(duel.getCompetitor1());
            }
            duel.getCompetitor1Score().forEach(score -> {
                if (score != Score.DO) {
                    woodcutters.remove(duel.getCompetitor1());
                }
            });
            if (duel.getCompetitor2Score().isEmpty()) {
                woodcutters.remove(duel.getCompetitor2());
            }
            duel.getCompetitor2Score().forEach(score -> {
                if (score != Score.DO) {
                    woodcutters.remove(duel.getCompetitor2());
                }
            });
        });
        //Create new achievement for the participants.
        return generateAchievement(AchievementType.WOODCUTTER, AchievementGrade.NORMAL, woodcutters, tournament);
    }

    /**
     * When somebody only performs 'do' scores in a tournament and all fights has at least a 'do' in at least two consecutive tournaments.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateWoodcutterAchievementBronze(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_NUMBER_BRONZE, AchievementType.WOODCUTTER, AchievementGrade.BRONZE);
    }

    /**
     * When somebody only performs 'do' scores in a tournament and all fights has at least a 'do' in at least three consecutive tournaments.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateWoodcutterAchievementSilver(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_NUMBER_SILVER, AchievementType.WOODCUTTER, AchievementGrade.SILVER);
    }

    /**
     * When somebody only performs 'do' scores in a tournament and all fights has at least a 'do' in at least five consecutive tournaments.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateWoodcutterAchievementGold(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_NUMBER_GOLD, AchievementType.WOODCUTTER, AchievementGrade.GOLD);
    }


    /**
     * When somebody has performed two different roles.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateFlexibleAsBambooAchievement(Tournament tournament) {
        //Get all participants from a tournament that has almost all roles in any tournament,
        final List<Participant> participants = participantProvider.get(tournament, MINIMUM_ROLES_BAMBOO_NORMAL);
        //Remove the ones already have the achievement.
        participants.removeAll(participantProvider.getParticipantsWithAchievementFromList(AchievementType.FLEXIBLE_AS_BAMBOO, participants));
        //Create new achievement for the participants.
        return generateAchievement(AchievementType.FLEXIBLE_AS_BAMBOO, AchievementGrade.NORMAL, participants, tournament);
    }

    /**
     * When somebody has performed three different roles.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateFlexibleAsBambooAchievementBronze(Tournament tournament) {
        //Get all participants from a tournament that has almost all roles in any tournament,
        final List<Participant> participants = participantProvider.get(tournament, MINIMUM_ROLES_BAMBOO_BRONZE);
        //Remove the ones already have the achievement.
        participants.removeAll(participantProvider.getParticipantsWithAchievementFromList(AchievementType.FLEXIBLE_AS_BAMBOO,
                AchievementGrade.BRONZE, participants));
        //Create new achievement for the participants.
        return generateAchievement(AchievementType.FLEXIBLE_AS_BAMBOO, AchievementGrade.BRONZE, participants, tournament);
    }

    /**
     * When somebody has performed three different roles.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateFlexibleAsBambooAchievementSilver(Tournament tournament) {
        //Get all participants from a tournament that has almost all roles in any tournament,
        final List<Participant> participants = participantProvider.get(tournament, MINIMUM_ROLES_BAMBOO_SILVER);
        //Remove the ones already have the achievement.
        participants.removeAll(participantProvider.getParticipantsWithAchievementFromList(AchievementType.FLEXIBLE_AS_BAMBOO,
                AchievementGrade.SILVER, participants));
        //Create new achievement for the participants.
        return generateAchievement(AchievementType.FLEXIBLE_AS_BAMBOO, AchievementGrade.SILVER, participants, tournament);
    }

    /**
     * When somebody has performed three different roles.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateFlexibleAsBambooAchievementGold(Tournament tournament) {
        //Get all participants from a tournament that has almost all roles in any tournament,
        final List<Participant> participants = participantProvider.get(tournament, MINIMUM_ROLES_BAMBOO_GOLD);
        //Remove the ones already have the achievement.
        participants.removeAll(participantProvider.getParticipantsWithAchievementFromList(AchievementType.FLEXIBLE_AS_BAMBOO,
                AchievementGrade.GOLD, participants));
        //Create new achievement for the participants.
        return generateAchievement(AchievementType.FLEXIBLE_AS_BAMBOO, AchievementGrade.GOLD, participants, tournament);
    }

    /**
     * First tournament as a competitor
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateSweatyTenuguiAchievement(Tournament tournament) {
        final List<Participant> participants = participantProvider.get(tournament, RoleType.COMPETITOR);
        //Remove the ones already have the achievement.
        participantProvider.getParticipantsWithAchievement(AchievementType.SWEATY_TENUGUI, AchievementGrade.NORMAL)
                .forEach(participants::remove);
        return generateAchievement(AchievementType.SWEATY_TENUGUI, AchievementGrade.NORMAL, participants, tournament);
    }

    /**
     * First ten tournaments as a competitor
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateSweatyTenuguiAchievementBronze(Tournament tournament) {
        final Map<Participant, List<Role>> rolesByParticipant = new HashMap<>(getRolesByParticipantUntil(tournament));
        //Remove the ones that has no the required number of tournaments.
        final Set<Participant> participants = new HashSet<>(getRolesByParticipantUntil(tournament).keySet());
        rolesByParticipant.forEach((participant, roles) -> {
            if (roles.stream().filter(role -> role.getRoleType() == RoleType.COMPETITOR)
                    .toList().size() < DEFAULT_TOURNAMENT_VERY_LONG_NUMBER_BRONZE) {
                participants.remove(participant);
            }
        });
        //Remove the ones already have the achievement.
        participantProvider.getParticipantsWithAchievement(AchievementType.SWEATY_TENUGUI, AchievementGrade.BRONZE)
                .forEach(participants::remove);
        return generateAchievement(AchievementType.SWEATY_TENUGUI, AchievementGrade.BRONZE, participants, tournament);
    }

    /**
     * First twenty tournaments as a competitor
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateSweatyTenuguiAchievementSilver(Tournament tournament) {
        final Map<Participant, List<Role>> rolesByParticipant = new HashMap<>(getRolesByParticipantUntil(tournament));
        //Remove the ones that has no the required number of tournaments.
        final Set<Participant> participants = new HashSet<>(getRolesByParticipantUntil(tournament).keySet());
        rolesByParticipant.forEach((participant, roles) -> {
            if (roles.stream().filter(role -> role.getRoleType() == RoleType.COMPETITOR)
                    .toList().size() < DEFAULT_TOURNAMENT_VERY_LONG_NUMBER_SILVER) {
                participants.remove(participant);
            }
        });
        //Remove the ones already have the achievement.
        participantProvider.getParticipantsWithAchievement(AchievementType.SWEATY_TENUGUI, AchievementGrade.SILVER)
                .forEach(participants::remove);
        return generateAchievement(AchievementType.SWEATY_TENUGUI, AchievementGrade.SILVER, participants, tournament);
    }

    /**
     * First twenty tournaments as a competitor
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateSweatyTenuguiAchievementGold(Tournament tournament) {
        final Map<Participant, List<Role>> rolesByParticipant = new HashMap<>(getRolesByParticipant());
        //Remove the ones that has no the required number of tournaments.
        final Set<Participant> participants = new HashSet<>(getRolesByParticipant().keySet());
        rolesByParticipant.forEach((participant, roles) -> {
            if (roles.stream().filter(role -> role.getRoleType() == RoleType.COMPETITOR)
                    .toList().size() < DEFAULT_TOURNAMENT_VERY_LONG_NUMBER_GOLD) {
                participants.remove(participant);
            }
        });
        //Remove the ones already have the achievement.
        participantProvider.getParticipantsWithAchievement(AchievementType.SWEATY_TENUGUI, AchievementGrade.GOLD)
                .forEach(participants::remove);
        return generateAchievement(AchievementType.SWEATY_TENUGUI, AchievementGrade.GOLD, participants, tournament);
    }

    private List<Achievement> generateAchievement(AchievementType achievementType, AchievementGrade achievementGrade,
                                                  Collection<Participant> participants, Tournament tournament) {
        if (participants == null || participants.isEmpty()) {
            return new ArrayList<>();
        }
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
     * @return a list of new achievements.
     */
    private List<Achievement> generateTheWinnerTournament(Tournament tournament) {
        final List<ScoreOfCompetitor> scoreOfCompetitors = rankingProvider.getCompetitorsScoreRanking(tournament);
        if (!scoreOfCompetitors.isEmpty()) {
            return generateAchievement(AchievementType.THE_WINNER, AchievementGrade.NORMAL,
                    Collections.singletonList(scoreOfCompetitors.get(0).getCompetitor()), tournament);
        }
        return new ArrayList<>();
    }

    /**
     * Winner of 3 tournaments in a year
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateTheWinnerAchievementBronze(Tournament tournament) {
        return generateGradeAchievementsByDays(tournament, AchievementType.THE_WINNER, AchievementGrade.BRONZE,
                DEFAULT_OCCURRENCES_BY_YEAR_BRONZE);
    }

    /**
     * Winner of 4 tournaments in a year
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateTheWinnerAchievementSilver(Tournament tournament) {
        return generateGradeAchievementsByDays(tournament, AchievementType.THE_WINNER, AchievementGrade.SILVER,
                DEFAULT_OCCURRENCES_BY_YEAR_SILVER);
    }

    /**
     * Winner of 5 tournaments in a year
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateTheWinnerAchievementGold(Tournament tournament) {
        return generateGradeAchievementsByDays(tournament, AchievementType.THE_WINNER, AchievementGrade.GOLD,
                DEFAULT_OCCURRENCES_BY_YEAR_GOLD);
    }

    /**
     * Winner of a tournament as a team member
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateTheWinnerTeamTournament(Tournament tournament) {
        if (tournament.getTeamSize() > 1) {
            final List<ScoreOfTeam> scoreOfTeams = rankingProvider.getTeamsScoreRanking(tournament);
            if (!scoreOfTeams.isEmpty() && scoreOfTeams.get(0).getTeam().getMembers().size() > 1) {
                return generateAchievement(AchievementType.THE_WINNER_TEAM, AchievementGrade.NORMAL,
                        scoreOfTeams.get(0).getTeam().getMembers(), tournament);
            }
        }
        return new ArrayList<>();
    }

    /**
     * Winner of 3 tournaments as a team member in a year
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateTheWinnerTeamAchievementBronze(Tournament tournament) {
        return generateGradeAchievementsByDays(tournament, AchievementType.THE_WINNER_TEAM, AchievementGrade.BRONZE,
                DEFAULT_OCCURRENCES_BY_YEAR_BRONZE);
    }

    /**
     * Winner of 4 tournaments as a team member in a year
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateTheWinnerTeamAchievementSilver(Tournament tournament) {
        return generateGradeAchievementsByDays(tournament, AchievementType.THE_WINNER_TEAM, AchievementGrade.SILVER,
                DEFAULT_OCCURRENCES_BY_YEAR_SILVER);
    }

    /**
     * Winner of 5 tournaments as a team member in a year
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateTheWinnerTeamAchievementGold(Tournament tournament) {
        return generateGradeAchievementsByDays(tournament, AchievementType.THE_WINNER_TEAM, AchievementGrade.GOLD,
                DEFAULT_OCCURRENCES_BY_YEAR_GOLD);
    }

    /***
     * Receive your first hit against you.
     * @param tournament The tournament to check.
     * @return the generated achievements.
     */
    private List<Achievement> generateTisButAScratchAchievement(Tournament tournament) {
        final List<Participant> participantsFirstScore = new ArrayList<>();
        //Already achievements granted
        final List<Participant> alreadyTisButAScratchAchievements = getProvider().get(AchievementType.TIS_BUT_A_SCRATCH, AchievementGrade.NORMAL).stream().map(
                Achievement::getParticipant).toList();
        getTotalScoreAgainstParticipant().forEach((participant, score) -> {
            if (score > 0 && !alreadyTisButAScratchAchievements.contains(participant)) {
                participantsFirstScore.add(participant);
            }
        });
        return generateAchievement(AchievementType.TIS_BUT_A_SCRATCH, AchievementGrade.NORMAL, participantsFirstScore, tournament);
    }

    /***
     * Receive your first 20 hits against you.
     * @param tournament The tournament to check.
     * @return the generated achievements.
     */
    private List<Achievement> generateTisButAScratchAchievementBronze(Tournament tournament) {
        final List<Participant> participantsFirstScore = new ArrayList<>();
        //Already achievements granted
        final List<Participant> alreadyTisButAScratchAchievements = getProvider().get(AchievementType.TIS_BUT_A_SCRATCH, AchievementGrade.BRONZE).stream().map(
                Achievement::getParticipant).toList();
        getTotalScoreAgainstParticipant().forEach((participant, score) -> {
            if (score >= DEFAULT_SCORE_BRONZE && !alreadyTisButAScratchAchievements.contains(participant)) {
                participantsFirstScore.add(participant);
            }
        });
        return generateAchievement(AchievementType.TIS_BUT_A_SCRATCH, AchievementGrade.BRONZE, participantsFirstScore, tournament);
    }

    /***
     * Receive your first 50 hits against you.
     * @param tournament The tournament to check.
     * @return the generated achievements.
     */
    private List<Achievement> generateTisButAScratchAchievementSilver(Tournament tournament) {
        final List<Participant> participantsFirstScore = new ArrayList<>();
        //Already achievements granted
        final List<Participant> alreadyTisButAScratchAchievements = getProvider().get(AchievementType.TIS_BUT_A_SCRATCH, AchievementGrade.SILVER).stream().map(
                Achievement::getParticipant).toList();
        getTotalScoreAgainstParticipant().forEach((participant, score) -> {
            if (score >= DEFAULT_SCORE_SILVER && !alreadyTisButAScratchAchievements.contains(participant)) {
                participantsFirstScore.add(participant);
            }
        });
        return generateAchievement(AchievementType.TIS_BUT_A_SCRATCH, AchievementGrade.SILVER, participantsFirstScore, tournament);
    }

    /***
     * Receive your first 100 hits against you.
     * @param tournament The tournament to check.
     * @return the generated achievements.
     */
    private List<Achievement> generateTisButAScratchAchievementGold(Tournament tournament) {
        final List<Participant> participantsFirstScore = new ArrayList<>();
        //Already achievements granted
        final List<Participant> alreadyTisButAScratchAchievements = getProvider().get(AchievementType.TIS_BUT_A_SCRATCH, AchievementGrade.GOLD).stream().map(
                Achievement::getParticipant).toList();
        getTotalScoreAgainstParticipant().forEach((participant, score) -> {
            if (score >= DEFAULT_SCORE_GOLD && !alreadyTisButAScratchAchievements.contains(participant)) {
                participantsFirstScore.add(participant);
            }
        });
        return generateAchievement(AchievementType.TIS_BUT_A_SCRATCH, AchievementGrade.GOLD, participantsFirstScore, tournament);
    }

    /***
     * Receive your first hit against you.
     * @param tournament The tournament to check.
     * @return the generated achievements.
     */
    private List<Achievement> generateFirstBloodAchievement(Tournament tournament) {
        final List<Participant> participantsFirstScore = new ArrayList<>();
        //Already achievements granted
        final List<Participant> alreadyFirstBloodAchievement = getProvider().get(AchievementType.FIRST_BLOOD, AchievementGrade.NORMAL).stream().map(
                Achievement::getParticipant).toList();
        getTotalScoreFromParticipant().forEach((participant, score) -> {
            if (score > 0 && !alreadyFirstBloodAchievement.contains(participant)) {
                participantsFirstScore.add(participant);
            }
        });
        return generateAchievement(AchievementType.FIRST_BLOOD, AchievementGrade.NORMAL, participantsFirstScore, tournament);
    }

    /***
     * Receive your first 20 hits against you.
     * @param tournament The tournament to check.
     * @return the generated achievements.
     */
    private List<Achievement> generateFirstBloodAchievementBronze(Tournament tournament) {
        final List<Participant> participantsFirstScore = new ArrayList<>();
        //Already achievements granted
        final List<Participant> alreadyFirstBloodAchievement = getProvider().get(AchievementType.FIRST_BLOOD, AchievementGrade.BRONZE).stream().map(
                Achievement::getParticipant).toList();
        getTotalScoreFromParticipant().forEach((participant, score) -> {
            if (score >= DEFAULT_SCORE_BRONZE && !alreadyFirstBloodAchievement.contains(participant)) {
                participantsFirstScore.add(participant);
            }
        });
        return generateAchievement(AchievementType.FIRST_BLOOD, AchievementGrade.BRONZE, participantsFirstScore, tournament);
    }

    /***
     * Receive your first 50 hits against you.
     * @param tournament The tournament to check.
     * @return the generated achievements.
     */
    private List<Achievement> generateFirstBloodAchievementSilver(Tournament tournament) {
        final List<Participant> participantsFirstScore = new ArrayList<>();
        //Already achievements granted
        final List<Participant> alreadyFirstBloodAchievement = getProvider().get(AchievementType.FIRST_BLOOD, AchievementGrade.SILVER).stream().map(
                Achievement::getParticipant).toList();
        getTotalScoreFromParticipant().forEach((participant, score) -> {
            if (score >= DEFAULT_SCORE_SILVER && !alreadyFirstBloodAchievement.contains(participant)) {
                participantsFirstScore.add(participant);
            }
        });
        return generateAchievement(AchievementType.FIRST_BLOOD, AchievementGrade.SILVER, participantsFirstScore, tournament);
    }

    /***
     * Receive your first 100 hits against you.
     * @param tournament The tournament to check.
     * @return the generated achievements.
     */
    private List<Achievement> generateFirstBloodAchievementGold(Tournament tournament) {
        final List<Participant> participantsFirstScore = new ArrayList<>();
        //Already achievements granted
        final List<Participant> alreadyFirstBloodAchievement = getProvider().get(AchievementType.FIRST_BLOOD, AchievementGrade.GOLD).stream().map(
                Achievement::getParticipant).toList();
        getTotalScoreFromParticipant().forEach((participant, score) -> {
            if (score >= DEFAULT_SCORE_GOLD && !alreadyFirstBloodAchievement.contains(participant)) {
                participantsFirstScore.add(participant);
            }
        });
        return generateAchievement(AchievementType.FIRST_BLOOD, AchievementGrade.GOLD, participantsFirstScore, tournament);
    }

    /***
     * Assist at least to 10 tournaments.
     * @param tournament The tournament to check.
     * @return the generated achievements.
     */
    private List<Achievement> generateDarumaAchievement(Tournament tournament) {
        final List<Participant> participantsDaruma = new ArrayList<>();
        //Already achievements granted
        final List<Participant> alreadyDarumaAchievement = getProvider().get(AchievementType.DARUMA, AchievementGrade.NORMAL).stream().map(
                Achievement::getParticipant).toList();
        getTotalScoreFromParticipant().forEach((participant, score) -> {
            if (getRolesByParticipant().get(participant).size() >= DARUMA_TOURNAMENTS_NORMAL && !alreadyDarumaAchievement.contains(participant)) {
                participantsDaruma.add(participant);
            }
        });
        return generateAchievement(AchievementType.DARUMA, AchievementGrade.NORMAL, participantsDaruma, tournament);
    }

    /***
     * Assist at least to 20 tournaments.
     * @param tournament The tournament to check.
     * @return the generated achievements.
     */
    private List<Achievement> generateDarumaAchievementBronze(Tournament tournament) {
        final List<Participant> participantsDaruma = new ArrayList<>();
        //Already achievements granted
        final List<Participant> alreadyDarumaAchievement = getProvider().get(AchievementType.DARUMA, AchievementGrade.BRONZE).stream().map(
                Achievement::getParticipant).toList();
        getTotalScoreFromParticipant().forEach((participant, score) -> {
            if (getRolesByParticipant().get(participant) != null && getRolesByParticipant().get(participant).size() >= DARUMA_TOURNAMENTS_BRONZE
                    && !alreadyDarumaAchievement.contains(participant)) {
                participantsDaruma.add(participant);
            }
        });
        return generateAchievement(AchievementType.DARUMA, AchievementGrade.BRONZE, participantsDaruma, tournament);
    }

    /***
     * Assist at least to 30 tournaments.
     * @param tournament The tournament to check.
     * @return the generated achievements.
     */
    private List<Achievement> generateDarumaAchievementSilver(Tournament tournament) {
        final List<Participant> participantsDaruma = new ArrayList<>();
        //Already achievements granted
        final List<Participant> alreadyDarumaAchievement = getProvider().get(AchievementType.DARUMA, AchievementGrade.SILVER).stream().map(
                Achievement::getParticipant).toList();
        getTotalScoreFromParticipant().forEach((participant, score) -> {
            if (getRolesByParticipant().get(participant) != null && getRolesByParticipant().get(participant).size() >= DARUMA_TOURNAMENTS_SILVER
                    && !alreadyDarumaAchievement.contains(participant)) {
                participantsDaruma.add(participant);
            }
        });
        return generateAchievement(AchievementType.DARUMA, AchievementGrade.SILVER, participantsDaruma, tournament);
    }

    /***
     * Assist at least to 50 tournaments.
     * @param tournament The tournament to check.
     * @return the generated achievements.
     */
    private List<Achievement> generateDarumaAchievementGold(Tournament tournament) {
        final List<Participant> participantsDaruma = new ArrayList<>();
        //Already achievements granted
        final List<Participant> alreadyDarumaAchievement = getProvider().get(AchievementType.DARUMA, AchievementGrade.GOLD).stream().map(
                Achievement::getParticipant).toList();
        getTotalScoreFromParticipant().forEach((participant, score) -> {
            if (getRolesByParticipant().get(participant) != null && getRolesByParticipant().get(participant).size() >= DARUMA_TOURNAMENTS_GOLD
                    && !alreadyDarumaAchievement.contains(participant)) {
                participantsDaruma.add(participant);
            }
        });
        return generateAchievement(AchievementType.DARUMA, AchievementGrade.GOLD, participantsDaruma, tournament);
    }

    /***
     * Be in a team, and all members of the teams does no score in the entire tournament.
     * @param tournament The tournament to check.
     * @return the generated achievements.
     */
    private List<Achievement> generateStormtrooperSyndromeAchievement(Tournament tournament) {
        if (getFightsFromTournament().size() < MIN_TOURNAMENT_FIGHTS) {
            return new ArrayList<>();
        }
        if (tournament.getTeamSize() < TournamentProvider.DEFAULT_TEAM_SIZE) {
            return new ArrayList<>();
        }
        final List<Team> teams = new ArrayList<>(getTeamsFromTournament());
        //Ensure that team is in fights.
        final Set<Team> teamsWithFights = new HashSet<>();
        getFightsFromTournament().forEach(fight -> {
            //Remove a tournament if it has some score.
            fight.getDuels().forEach(duel -> {
                if (!duel.getCompetitor1Score().isEmpty()) {
                    teams.remove(fight.getTeam1());
                }
                if (!duel.getCompetitor2Score().isEmpty()) {
                    teams.remove(fight.getTeam2());
                }
            });
            teamsWithFights.add(fight.getTeam1());
            teamsWithFights.add(fight.getTeam2());
        });
        teams.retainAll(teamsWithFights);
        return generateAchievement(AchievementType.STORMTROOPER_SYNDROME, AchievementGrade.NORMAL,
                teams.stream().flatMap(team -> team.getMembers().stream()).toList(), tournament);
    }

    /**
     * Somebody wins a fight despite the opponent has scored first.
     *
     * @param tournament
     */
    private List<Achievement> generateVendettaAchievement(Tournament tournament) {
        final List<Participant> participants = new ArrayList<>();
        getFightsFromTournament().forEach(fight -> fight.getDuels().forEach(duel -> {
            if (!duel.getCompetitor1ScoreTime().isEmpty() && !duel.getCompetitor2ScoreTime().isEmpty()
                    && duel.getCompetitor1ScoreTime().get(0) != null && duel.getCompetitor2ScoreTime().get(0) != null
                    && duel.getCompetitor1ScoreTime().get(0) < duel.getCompetitor2ScoreTime().get(0)
                    && duel.getWinner() == 2) {
                participants.add(duel.getCompetitor2());
            }
            if (!duel.getCompetitor1ScoreTime().isEmpty() && !duel.getCompetitor2ScoreTime().isEmpty()
                    && duel.getCompetitor1ScoreTime().get(0) != null && duel.getCompetitor2ScoreTime().get(0) != null
                    && duel.getCompetitor2ScoreTime().get(0) < duel.getCompetitor1ScoreTime().get(0)
                    && duel.getWinner() == 1) {
                participants.add(duel.getCompetitor1());
            }
        }));
        return generateAchievement(AchievementType.V_FOR_VENDETTA, AchievementGrade.NORMAL,
                participants, tournament);
    }

    /**
     * Somebody wins a fight against other participant that is always winning him.
     *
     * @param tournament
     */
    private List<Achievement> generateSithApprenticesAlwaysKillTheirMasterAchievement(Tournament tournament) {
        final List<Achievement> achievements = new ArrayList<>();
        getFightsFromTournament().forEach(fight -> {
            for (Duel duel : fight.getDuels()) {
                final List<Duel> previousDuels = duelProvider.getWhenBothAreInvolved(duel.getCompetitor1(), duel.getCompetitor2());
                boolean isApprentice = true;
                int numberOfPreviousDuels = 0;
                for (Duel previousDuel : previousDuels) {
                    if (previousDuel.getCreatedAt().isBefore(tournament.getCreatedAt())) {
                        numberOfPreviousDuels++;
                        //Check if he has already won vs the master
                        if (Objects.equals(duel.getCompetitorWinner(), previousDuel.getCompetitorWinner()) || previousDuel.getWinner() == 0) {
                            isApprentice = false;
                            break;
                        }
                    }
                }
                if (isApprentice && duel.getCompetitorWinner() != null) {
                    //Generate achievement depending on the number of fights.
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
        return achievementProvider.saveAll(achievements);
    }

    /**
     * Somebody wins a fight despite the opponent has scored first.
     *
     * @param tournament
     */
    private List<Achievement> generateDethroneTheKingAchievement(Tournament tournament) {
        if (tournament.getType() == TournamentType.BUBBLE_SORT) {
            final List<Group> groups = groupProvider.getGroups(tournament);
            if (groups.size() > 1) {
                final List<Team> startingRanking = groups.get(0).getTeams();
                final List<Team> endingRanking = bubbleSortTournamentHandler.getTeamsOrderedByRanks(tournament, groups.get(groups.size() - 1),
                        bubbleSortTournamentHandler.getDrawResolution(tournament));

                //The King is the last one.
                final Team kingTeam = endingRanking.get(endingRanking.size() - 1);

                //How much have fought to be the king:
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

    /**
     * Somebody wins a fight despite the opponent has scored first.
     *
     * @param tournament
     */
    private List<Achievement> generateClimbTheLadderAchievement(Tournament tournament) {
        final List<Achievement> achievements = new ArrayList<>();
        if (tournament.getType() == TournamentType.SENBATSU) {

            final List<Group> groups = groupProvider.getGroups(tournament);
            final List<Team> startingRanking = groups.get(0).getTeams();
            final List<Team> endingRanking = senbatsuTournamentHandler.getFinalRanking(tournament);

            //Hay many rungs have you climbed?
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


    /**
     * Achievement for the stormtrooper syndrome in two tournaments.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateStormtrooperSyndromeAchievementBronze(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_NUMBER_BRONZE,
                AchievementType.STORMTROOPER_SYNDROME, AchievementGrade.BRONZE);
    }

    /***
     * Achievement for the stormtrooper syndrome in three tournaments.
     * @param tournament The tournament to check.
     * @return the generated achievements.
     */
    private List<Achievement> generateStormtrooperSyndromeAchievementSilver(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_NUMBER_SILVER,
                AchievementType.STORMTROOPER_SYNDROME, AchievementGrade.SILVER);
    }

    /***
     * Achievement for the stormtrooper syndrome in five tournaments.
     * @param tournament The tournament to check.
     * @return the generated achievements.
     */
    private List<Achievement> generateStormtrooperSyndromeAchievementGold(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_NUMBER_GOLD,
                AchievementType.STORMTROOPER_SYNDROME, AchievementGrade.GOLD);
    }

    /**
     * Achievement for v from vendetta in two tournaments.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateVendettaAchievementBronze(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_NUMBER_BRONZE, AchievementType.V_FOR_VENDETTA, AchievementGrade.BRONZE);
    }

    /**
     * Achievement for v from vendetta in three tournaments.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateVendettaAchievementSilver(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_NUMBER_SILVER, AchievementType.V_FOR_VENDETTA, AchievementGrade.SILVER);
    }

    /**
     * Achievement for v from vendetta in five tournaments.
     *
     * @param tournament The tournament to check.
     * @return a list of new achievements.
     */
    private List<Achievement> generateVendettaAchievementGold(Tournament tournament) {
        return generateConsecutiveGradeAchievements(tournament, DEFAULT_TOURNAMENT_NUMBER_GOLD, AchievementType.V_FOR_VENDETTA, AchievementGrade.GOLD);
    }
}
