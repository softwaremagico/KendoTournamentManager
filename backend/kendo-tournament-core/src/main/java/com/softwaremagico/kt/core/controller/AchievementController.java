package com.softwaremagico.kt.core.controller;

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

import com.softwaremagico.kt.core.controller.achievements.CombatAchievementGenerator;
import com.softwaremagico.kt.core.controller.achievements.DefenseAchievementGenerator;
import com.softwaremagico.kt.core.controller.achievements.DurationAchievementGenerator;
import com.softwaremagico.kt.core.controller.achievements.FlexibleBambooAchievementGenerator;
import com.softwaremagico.kt.core.controller.achievements.LadderAchievementGenerator;
import com.softwaremagico.kt.core.controller.achievements.MembershipLongevityAchievementGenerator;
import com.softwaremagico.kt.core.controller.achievements.ParticipationAchievementGenerator;
import com.softwaremagico.kt.core.controller.achievements.RankingModeAchievementGenerator;
import com.softwaremagico.kt.core.controller.achievements.RivalryAchievementGenerator;
import com.softwaremagico.kt.core.controller.achievements.RoleBasedAchievementGenerator;
import com.softwaremagico.kt.core.controller.achievements.ScoreTechniqueAchievementGenerator;
import com.softwaremagico.kt.core.controller.achievements.ScoreVarietyAchievementGenerator;
import com.softwaremagico.kt.core.controller.achievements.SwissAchievementGenerator;
import com.softwaremagico.kt.core.controller.achievements.WinnerAchievementGenerator;
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
import com.softwaremagico.kt.core.tournaments.BubbleSortTournamentHandler;
import com.softwaremagico.kt.core.tournaments.SenbatsuTournamentHandler;
import com.softwaremagico.kt.logger.KendoTournamentLogger;
import com.softwaremagico.kt.persistence.entities.Achievement;
import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Role;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.AchievementRepository;
import com.softwaremagico.kt.persistence.values.AchievementGrade;
import com.softwaremagico.kt.persistence.values.AchievementType;
import com.softwaremagico.kt.persistence.values.Score;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class AchievementController
        extends BasicInsertableController<Achievement, AchievementDTO, AchievementRepository, AchievementProvider,
        AchievementConverterRequest, AchievementConverter> {

    private static final int MILLIS = 1000;

    private static final int LETHAL_WEAPON_MAX_TIME = 5;

    private static final int DEFAULT_OCCURRENCES_BY_YEAR_BRONZE = 3;
    private static final int DEFAULT_OCCURRENCES_BY_YEAR_SILVER = 4;
    private static final int DEFAULT_OCCURRENCES_BY_YEAR_GOLD = 5;

    private static final int PARTICIPANT_YEARS = 5;
    private static final int PARTICIPANT_YEARS_BRONZE = 10;
    private static final int PARTICIPANT_YEARS_SILVER = 15;
    private static final int PARTICIPANT_YEARS_GOLD = 20;

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

    private final WinnerAchievementGenerator winnerAchievementGenerator;

    private final SwissAchievementGenerator swissAchievementGenerator;

    private final ScoreTechniqueAchievementGenerator scoreTechniqueAchievementGenerator;

    private final CombatAchievementGenerator combatAchievementGenerator;

    private final RankingModeAchievementGenerator rankingModeAchievementGenerator;

    private final MembershipLongevityAchievementGenerator membershipLongevityAchievementGenerator;

    private final RivalryAchievementGenerator rivalryAchievementGenerator;

    private final LadderAchievementGenerator ladderAchievementGenerator;

    private final DurationAchievementGenerator durationAchievementGenerator;

    private final ParticipationAchievementGenerator participationAchievementGenerator;

    private final DefenseAchievementGenerator defenseAchievementGenerator;

    private final RoleBasedAchievementGenerator roleBasedAchievementGenerator;

    private final FlexibleBambooAchievementGenerator flexibleBambooAchievementGenerator;

    private final ScoreVarietyAchievementGenerator scoreVarietyAchievementGenerator;

    private Tournament tournament;
    private Map<Tournament, List<Participant>> participantsFromTournament = new HashMap<>();
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
            FightProvider fightProvider, DuelProvider duelProvider, RankingProvider rankingProvider,
            GroupProvider groupProvider, BubbleSortTournamentHandler bubbleSortTournamentHandler,
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
        this.winnerAchievementGenerator = new WinnerAchievementGenerator(achievementProvider, rankingProvider);
        this.swissAchievementGenerator = new SwissAchievementGenerator(achievementProvider, rankingProvider);
        this.scoreTechniqueAchievementGenerator = new ScoreTechniqueAchievementGenerator(achievementProvider,
                fightProvider, duelProvider, roleProvider, tournamentProvider, participantProvider,
                MIN_TOURNAMENT_FIGHTS);
        this.combatAchievementGenerator = new CombatAchievementGenerator(achievementProvider, fightProvider,
                duelProvider, tournamentProvider, participantProvider, LETHAL_WEAPON_MAX_TIME, MIN_TOURNAMENT_FIGHTS);
        this.rankingModeAchievementGenerator = new RankingModeAchievementGenerator(achievementProvider,
                rankingProvider);
        this.membershipLongevityAchievementGenerator = new MembershipLongevityAchievementGenerator(achievementProvider,
                participantProvider);
        this.rivalryAchievementGenerator = new RivalryAchievementGenerator(achievementProvider, duelProvider,
                tournamentProvider, participantProvider);
        this.ladderAchievementGenerator = new LadderAchievementGenerator(achievementProvider, groupProvider,
                bubbleSortTournamentHandler, senbatsuTournamentHandler);
        this.durationAchievementGenerator = new DurationAchievementGenerator(achievementProvider, tournamentProvider,
                participantProvider, duelProvider);
        this.participationAchievementGenerator = new ParticipationAchievementGenerator(achievementProvider,
                participantProvider, tournamentProvider, roleProvider);
        this.defenseAchievementGenerator = new DefenseAchievementGenerator(achievementProvider);
        this.roleBasedAchievementGenerator = new RoleBasedAchievementGenerator(achievementProvider, participantProvider,
                tournamentProvider);
        this.flexibleBambooAchievementGenerator = new FlexibleBambooAchievementGenerator(achievementProvider,
                participantProvider, tournamentProvider);
        this.scoreVarietyAchievementGenerator = new ScoreVarietyAchievementGenerator(achievementProvider);
    }

    public interface AchievementsGeneratedListener {
        void generated(List<AchievementDTO> achievementsGenerated, TournamentDTO tournament);
    }

    public interface AchievementsGeneratedAllTournamentsListener {
        void generated(List<AchievementDTO> achievementsGenerated, List<TournamentDTO> tournaments);
    }

    public void addAchievementsGeneratedListener(AchievementsGeneratedListener listener) {
        this.achievementsGeneratedListeners.add(listener);
    }

    public void addAchievementsGeneratedAllTournamentsListener(AchievementsGeneratedAllTournamentsListener listener) {
        this.achievementsGeneratedAllTournamentsListeners.add(listener);
    }

    @Override
    protected AchievementConverterRequest createConverterRequest(Achievement achievement) {
        return new AchievementConverterRequest(achievement);
    }

    private List<Participant> getParticipantsFromTournament(Tournament tournament) {
        if (this.participantsFromTournament == null) {
            this.participantsFromTournament = new HashMap<>();
        }
        return this.participantsFromTournament.computeIfAbsent(tournament, this.participantProvider::get);
    }

    private List<Duel> getDuelsFromTournament() {
        if (this.duelsFromTournament == null) {
            this.duelsFromTournament = this.duelProvider.get(this.tournament);
        }
        return this.duelsFromTournament;
    }

    private List<Team> getTeamsFromTournament() {
        if (this.teamsFromTournament == null) {
            this.teamsFromTournament = this.teamProvider.getAll(this.tournament);
        }
        return this.teamsFromTournament;
    }

    private List<Fight> getFightsFromTournament() {
        if (this.fightsFromTournament == null) {
            this.fightsFromTournament = this.fightProvider.getFights(this.tournament);
        }
        return this.fightsFromTournament;
    }

    private Map<Participant, List<Score>> getScoresByParticipant() {
        if (this.scoresByParticipant == null) {
            this.scoresByParticipant = new HashMap<>();
            this.getDuelsFromTournament().forEach(duel -> {
                this.scoresByParticipant.computeIfAbsent(duel.getCompetitor1(), k -> new ArrayList<>());
                this.scoresByParticipant.computeIfAbsent(duel.getCompetitor2(), k -> new ArrayList<>());
                duel.getCompetitor1Score()
                        .forEach(score -> this.scoresByParticipant.get(duel.getCompetitor1()).add(score));
                duel.getCompetitor2Score()
                        .forEach(score -> this.scoresByParticipant.get(duel.getCompetitor2()).add(score));
            });
        }
        return this.scoresByParticipant;
    }

    private Map<Participant, List<Score>> getScoresReceivedByParticipant() {
        if (this.scoresReceivedByParticipant == null) {
            this.scoresReceivedByParticipant = new HashMap<>();
            this.getDuelsFromTournament().forEach(duel -> {
                this.scoresReceivedByParticipant.computeIfAbsent(duel.getCompetitor1(), k -> new ArrayList<>());
                this.scoresReceivedByParticipant.computeIfAbsent(duel.getCompetitor2(), k -> new ArrayList<>());
                duel.getCompetitor1Score()
                        .forEach(score -> this.scoresReceivedByParticipant.get(duel.getCompetitor2()).add(score));
                duel.getCompetitor2Score()
                        .forEach(score -> this.scoresReceivedByParticipant.get(duel.getCompetitor1()).add(score));
            });
        }
        return this.scoresReceivedByParticipant;
    }

    private Map<Participant, Long> getTotalScoreFromParticipant() {
        if (this.totalScoreFromParticipant == null) {
            this.totalScoreFromParticipant = new HashMap<>();

            final List<Tournament> previousTournaments = this.tournamentProvider.getPreviousTo(this.tournament,
                    MAX_PREVIOUS_TOURNAMENTS);
            // Also current tournament!
            previousTournaments.addFirst(this.tournament);

            this.getParticipantsFromTournament(this.tournament).forEach(participant -> this.totalScoreFromParticipant
                    .put(participant, this.duelProvider.countScoreFromCompetitor(participant, previousTournaments)));
        }
        return this.totalScoreFromParticipant;
    }

    private Map<Participant, Long> getTotalScoreAgainstParticipant() {
        if (this.totalScoreAgainstParticipant == null) {
            this.totalScoreAgainstParticipant = new HashMap<>();

            final List<Tournament> previousTournaments = this.tournamentProvider.getPreviousTo(this.tournament,
                    MAX_PREVIOUS_TOURNAMENTS);
            // Also current tournament!
            previousTournaments.addFirst(this.tournament);

            this.getParticipantsFromTournament(this.tournament).forEach(participant -> this.totalScoreAgainstParticipant
                    .put(participant, this.duelProvider.countScoreAgainstCompetitor(participant, previousTournaments)));
        }
        return this.totalScoreAgainstParticipant;
    }

    private Map<Participant, List<Role>> getRolesByParticipant() {
        if (this.rolesByParticipant == null) {
            final List<Role> roles = this.roleProvider.getBy(this.getParticipantsFromTournament(this.tournament));
            this.rolesByParticipant = roles.stream().collect(Collectors.groupingBy(Role::getParticipant));
        }
        return this.rolesByParticipant;
    }

    private Map<Participant, List<Role>> getRolesByParticipantUntil(Tournament tournament) {
        final Map<Participant, List<Role>> roles = new HashMap<>();
        for (final Map.Entry<Participant, List<Role>> entry : this.getRolesByParticipant().entrySet()) {
            roles.put(entry.getKey(),
                    entry.getValue().stream()
                            .filter(role -> role.getTournament().getCreatedAt() != null
                                    && role.getTournament().getCreatedAt().isBefore(tournament.getCreatedAt()))
                            .toList());
        }
        return roles;
    }

    public List<AchievementDTO> getParticipantAchievements(Integer participantId) {
        final Participant participant = this.participantProvider.get(participantId)
                .orElseThrow(() -> new ParticipantNotFoundException(this.getClass(),
                        "No participant found with id '" + participantId + "'."));
        return this.convertAll(this.getProvider().get(participant));
    }

    public List<AchievementDTO> getParticipantAchievements(ParticipantDTO participantDTO) {
        return this.convertAll(this.getProvider().get(this.participantConverter.reverse(participantDTO)));
    }

    public List<AchievementDTO> getParticipantAchievements(TournamentDTO tournamentDTO, ParticipantDTO participantDTO) {
        return this.convertAll(this.getProvider().get(this.tournamentConverter.reverse(tournamentDTO),
                this.participantConverter.reverse(participantDTO)));
    }

    public List<AchievementDTO> getAchievements(TournamentDTO tournamentDTO, AchievementType achievementType) {
        return this
                .convertAll(this.getProvider().get(this.tournamentConverter.reverse(tournamentDTO), achievementType));
    }

    public List<AchievementDTO> getAchievements(TournamentDTO tournamentDTO) {
        return this.convertAll(this.getProvider().get(this.tournamentConverter.reverse(tournamentDTO)));
    }

    public List<AchievementDTO> getAchievements(TournamentDTO tournamentDTO, AchievementType achievementType,
            AchievementGrade achievementGrade) {
        return this.convertAll(this.getProvider().get(this.tournamentConverter.reverse(tournamentDTO), achievementType,
                achievementGrade));
    }

    public List<AchievementDTO> getAchievements(AchievementType achievementType) {
        return this.convertAll(this.getProvider().get(achievementType));
    }

    public List<AchievementDTO> getAchievements(AchievementType achievementType, AchievementGrade achievementGrade) {
        return this.convertAll(this.getProvider().get(achievementType, achievementGrade));
    }

    public List<AchievementDTO> getTournamentAchievements(Integer tournamentId) {
        final Tournament tournamentEntity = this.tournamentProvider.get(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(this.getClass(),
                        "No tournament found with id '" + tournamentId + "'."));
        return this.convertAll(this.getProvider().get(tournamentEntity));
    }

    public List<AchievementDTO> regenerateAllAchievements() {
        final long start = System.currentTimeMillis();
        final List<TournamentDTO> tournaments = this.tournamentConverter
                .convertAll(this.tournamentProvider.getAll().stream().map(TournamentConverterRequest::new).toList());
        // Inserted tournaments from previous version have same date in some cases.
        tournaments.sort(Comparator.comparing(TournamentDTO::getCreatedAt).thenComparing(TournamentDTO::getId));
        final List<AchievementDTO> achievementsGenerated = new ArrayList<>();
        this.achievementProvider.deleteAll();
        for (final TournamentDTO tournamentDTO : tournaments) {
            achievementsGenerated.addAll(this.generateAchievements(tournamentDTO));
        }
            for (final AchievementsGeneratedAllTournamentsListener listener
                : this.achievementsGeneratedAllTournamentsListeners) {
            listener.generated(achievementsGenerated, tournaments);
        }
        try {
            return achievementsGenerated;
        } finally {
            final long finish = System.currentTimeMillis();
            KendoTournamentLogger.info(this.getClass(), "Time needed for recalculating the achievements: '{}' secs.",
                    (finish - start) / MILLIS);
        }
    }

    public List<AchievementDTO> regenerateAchievements(Integer tournamentId) {
        final TournamentDTO tournamentDTO = this.tournamentConverter
                .convert(new TournamentConverterRequest(this.tournamentProvider.get(tournamentId)
                        .orElseThrow(() -> new TournamentNotFoundException(this.getClass(),
                                "No tournament found with id '" + tournamentId + "'."))));
        return this.regenerateAchievements(tournamentDTO);
    }

    public List<AchievementDTO> regenerateAchievements(TournamentDTO tournament) {
        this.deleteAchievements(tournament);
        final List<AchievementDTO> achievementsGenerated = this.generateAchievements(tournament);
        this.achievementsGeneratedListeners.forEach(achievementsGeneratedListener -> achievementsGeneratedListener
                .generated(achievementsGenerated, tournament));
        return achievementsGenerated;
    }

    private void deleteAchievements(TournamentDTO tournamentDTO) {
        this.getProvider().delete(this.tournamentConverter.reverse(tournamentDTO));
    }

    public Map<AchievementType, Map<AchievementGrade, Integer>> getAchievementsCount() {
        return this.getProvider().getAchievementsCount();
    }

    public int countAchievements(AchievementType achievementType) {
        return this.getProvider().countAchievements(achievementType);
    }

    public List<AchievementDTO> generateAchievements(TournamentDTO tournamentDTO) {
        this.tournament = this.tournamentConverter.reverse(tournamentDTO);
        this.duelsFromTournament = null;
        this.participantsFromTournament = null;
        this.scoresByParticipant = null;
        this.scoresReceivedByParticipant = null;
        this.totalScoreFromParticipant = null;
        this.totalScoreAgainstParticipant = null;
        this.rolesByParticipant = null;
        this.fightsFromTournament = null;
        this.teamsFromTournament = null;
        this.getProvider().delete(this.tournament);
        final List<Achievement> achievementsGenerated = new ArrayList<>();
        achievementsGenerated.addAll(this.combatAchievementGenerator.generateBillyTheKidAchievement(this.tournament));
        achievementsGenerated.addAll(this.combatAchievementGenerator.generateLethalWeaponAchievement(this.tournament));
        achievementsGenerated.addAll(this.combatAchievementGenerator.generateTerminatorAchievement(this.tournament));
        final List<Achievement> juggernautAchievements = this.combatAchievementGenerator
                .generateJuggernautAchievement(this.tournament);
        this.removeAchievements(achievementsGenerated, AchievementType.TERMINATOR,
                Collections.singletonList(AchievementGrade.NORMAL),
                juggernautAchievements.stream().map(Achievement::getParticipant).collect(Collectors.toSet()));
        achievementsGenerated.addAll(juggernautAchievements);
        achievementsGenerated.addAll(this.rankingModeAchievementGenerator.generateTheKingAchievement(this.tournament));
        achievementsGenerated
                .addAll(this.roleBasedAchievementGenerator.generateLooksGoodFromFarAwayButAchievement(this.tournament));
        achievementsGenerated
                .addAll(this.roleBasedAchievementGenerator.generateILoveTheFlagsAchievement(this.tournament));
        achievementsGenerated
                .addAll(this.roleBasedAchievementGenerator.generateLoveSharingAchievement(this.tournament));
        achievementsGenerated.addAll(this.membershipLongevityAchievementGenerator
                .generateNeverEndingStoryAchievement(this.tournament, AchievementGrade.NORMAL, PARTICIPANT_YEARS));
        achievementsGenerated
                .addAll(this.rankingModeAchievementGenerator.generateMasterTheLoopAchievement(this.tournament));
        achievementsGenerated.addAll(this.combatAchievementGenerator.generateTheCastleAchievement(this.tournament));
        this.removeAchievements(achievementsGenerated, AchievementType.THE_CASTLE,
                Collections.singletonList(AchievementGrade.NORMAL),
                juggernautAchievements.stream().map(Achievement::getParticipant).collect(Collectors.toSet()));
        final List<Achievement> entrenchedAchievements = this.combatAchievementGenerator
                .generateEntrenchedAchievement(this.tournament);
        achievementsGenerated.addAll(entrenchedAchievements);
        this.removeAchievements(achievementsGenerated, AchievementType.THE_CASTLE,
                Collections.singletonList(AchievementGrade.NORMAL),
                entrenchedAchievements.stream().map(Achievement::getParticipant).collect(Collectors.toSet()));
        achievementsGenerated.addAll(this.combatAchievementGenerator.generateBoneBreakerAchievement(this.tournament));
        achievementsGenerated
                .addAll(this.scoreTechniqueAchievementGenerator.generateWoodcutterAchievement(this.tournament));
        achievementsGenerated
                .addAll(this.scoreTechniqueAchievementGenerator.generateHeadShotAchievement(this.tournament));
        achievementsGenerated
                .addAll(this.scoreTechniqueAchievementGenerator.generateYouAreUnderArrestAchievement(this.tournament));
        achievementsGenerated
                .addAll(this.flexibleBambooAchievementGenerator.generateFlexibleAsBambooAchievement(this.tournament));
        achievementsGenerated
                .addAll(this.participationAchievementGenerator.generateSweatyTenuguiAchievement(this.tournament));
        achievementsGenerated.addAll(this.winnerAchievementGenerator.generateTheWinnerTournament(this.tournament));
        achievementsGenerated.addAll(this.winnerAchievementGenerator.generateTheWinnerTeamTournament(this.tournament));
        achievementsGenerated.addAll(this.swissAchievementGenerator.generateSwissWinnerAchievement(this.tournament));
        achievementsGenerated
                .addAll(this.swissAchievementGenerator.generateBuchholzWhispererAchievement(this.tournament));
        achievementsGenerated.addAll(this.defenseAchievementGenerator.generateTisButAScratchAchievement(this.tournament,
                this.getTotalScoreAgainstParticipant()));
        achievementsGenerated.addAll(this.defenseAchievementGenerator.generateFirstBloodAchievement(this.tournament,
                this.getTotalScoreFromParticipant()));
        achievementsGenerated.addAll(this.participationAchievementGenerator.generateDarumaAchievement(this.tournament));
        achievementsGenerated.addAll(this.participationAchievementGenerator.generateStormtrooperSyndromeAchievement(
                this.tournament, this.getFightsFromTournament(), this.getTeamsFromTournament()));
        achievementsGenerated.addAll(this.rivalryAchievementGenerator.generateVendettaAchievement(this.tournament,
                this.getFightsFromTournament()));
        achievementsGenerated
                .addAll(this.rivalryAchievementGenerator.generateSithApprenticesAlwaysKillTheirMasterAchievement(
                        this.tournament, this.getFightsFromTournament()));
        achievementsGenerated
                .addAll(this.ladderAchievementGenerator.generateDethroneTheKingAchievement(this.tournament));
        achievementsGenerated
                .addAll(this.ladderAchievementGenerator.generateClimbTheLadderAchievement(this.tournament));
        achievementsGenerated
                .addAll(this.combatAchievementGenerator.generateBillyTheKidAchievementBronze(this.tournament));
        achievementsGenerated
                .addAll(this.combatAchievementGenerator.generateBillyTheKidAchievementSilver(this.tournament));
        achievementsGenerated
                .addAll(this.combatAchievementGenerator.generateBillyTheKidAchievementGold(this.tournament));
        achievementsGenerated
                .addAll(this.combatAchievementGenerator.generateLethalWeaponAchievementBronze(this.tournament));
        achievementsGenerated
                .addAll(this.combatAchievementGenerator.generateLethalWeaponAchievementSilver(this.tournament));
        achievementsGenerated
                .addAll(this.combatAchievementGenerator.generateLethalWeaponAchievementGold(this.tournament));
        achievementsGenerated
                .addAll(this.combatAchievementGenerator.generateTerminatorAchievementBronze(this.tournament));
        achievementsGenerated
                .addAll(this.combatAchievementGenerator.generateTerminatorAchievementSilver(this.tournament));
        achievementsGenerated
                .addAll(this.combatAchievementGenerator.generateTerminatorAchievementGold(this.tournament));
        achievementsGenerated
                .addAll(this.combatAchievementGenerator.generateJuggernautAchievementBronze(this.tournament));
        achievementsGenerated
                .addAll(this.combatAchievementGenerator.generateJuggernautAchievementSilver(this.tournament));
        achievementsGenerated
                .addAll(this.combatAchievementGenerator.generateJuggernautAchievementGold(this.tournament));
        achievementsGenerated.addAll(this.rankingModeAchievementGenerator.generateTheKingAchievementGrade(
                this.tournament, AchievementGrade.BRONZE, DEFAULT_OCCURRENCES_BY_YEAR_BRONZE));
        achievementsGenerated.addAll(this.rankingModeAchievementGenerator.generateTheKingAchievementGrade(
                this.tournament, AchievementGrade.SILVER, DEFAULT_OCCURRENCES_BY_YEAR_SILVER));
        achievementsGenerated.addAll(this.rankingModeAchievementGenerator.generateTheKingAchievementGrade(
                this.tournament, AchievementGrade.GOLD, DEFAULT_OCCURRENCES_BY_YEAR_GOLD));
        achievementsGenerated.addAll(
                this.roleBasedAchievementGenerator.generateLooksGoodFromFarAwayButAchievementBronze(this.tournament));
        achievementsGenerated.addAll(
                this.roleBasedAchievementGenerator.generateLooksGoodFromFarAwayButAchievementSilver(this.tournament));
        achievementsGenerated.addAll(
                this.roleBasedAchievementGenerator.generateLooksGoodFromFarAwayButAchievementGold(this.tournament));
        achievementsGenerated
                .addAll(this.roleBasedAchievementGenerator.generateILoveTheFlagsAchievementBronze(this.tournament));
        achievementsGenerated
                .addAll(this.roleBasedAchievementGenerator.generateILoveTheFlagsAchievementSilver(this.tournament));
        achievementsGenerated
                .addAll(this.roleBasedAchievementGenerator.generateILoveTheFlagsAchievementGold(this.tournament));
        achievementsGenerated
                .addAll(this.roleBasedAchievementGenerator.generateLoveSharingAchievementBronze(this.tournament));
        achievementsGenerated
                .addAll(this.roleBasedAchievementGenerator.generateLoveSharingAchievementSilver(this.tournament));
        achievementsGenerated
                .addAll(this.roleBasedAchievementGenerator.generateLoveSharingAchievementGold(this.tournament));
        achievementsGenerated
                .addAll(this.combatAchievementGenerator.generateTheCastleAchievementBronze(this.tournament));
        achievementsGenerated
                .addAll(this.combatAchievementGenerator.generateTheCastleAchievementSilver(this.tournament));
        achievementsGenerated.addAll(this.combatAchievementGenerator.generateTheCastleAchievementGold(this.tournament));
        achievementsGenerated
                .addAll(this.combatAchievementGenerator.generateEntrenchedAchievementBronze(this.tournament));
        achievementsGenerated
                .addAll(this.combatAchievementGenerator.generateEntrenchedAchievementSilver(this.tournament));
        achievementsGenerated
                .addAll(this.combatAchievementGenerator.generateEntrenchedAchievementGold(this.tournament));
        return this.generateAchievementsGradesAndSpecials(this.tournament, achievementsGenerated);
    }

    private List<AchievementDTO> generateAchievementsGradesAndSpecials(Tournament tournament,
            List<Achievement> achievementsGenerated) {
        achievementsGenerated.addAll(this.scoreVarietyAchievementGenerator
                .generateALittleOfEverythingAchievement(tournament, this.getScoresByParticipant()));
        achievementsGenerated.addAll(this.scoreVarietyAchievementGenerator
                .generateALittleOfEverythingAchievementBronze(tournament, this.getScoresByParticipant()));
        final List<Achievement> aLittleOfEverythingSilver = this.scoreVarietyAchievementGenerator
                .generateALittleOfEverythingAchievementSilver(tournament, this.getScoresByParticipant());
        this.removeAchievements(achievementsGenerated, AchievementType.A_LITTLE_OF_EVERYTHING,
                AchievementGrade.SILVER.getLessThan(),
                aLittleOfEverythingSilver.stream().map(Achievement::getParticipant).collect(Collectors.toSet()));
        achievementsGenerated.addAll(aLittleOfEverythingSilver);
        final List<Achievement> aLittleOfEverythingGold = this.scoreVarietyAchievementGenerator
                .generateALittleOfEverythingAchievementGold(tournament, this.getScoresByParticipant());
        this.removeAchievements(achievementsGenerated, AchievementType.A_LITTLE_OF_EVERYTHING,
                AchievementGrade.GOLD.getLessThan(),
                aLittleOfEverythingSilver.stream().map(Achievement::getParticipant).collect(Collectors.toSet()));
        achievementsGenerated.addAll(aLittleOfEverythingGold);
        achievementsGenerated
                .addAll(this.participationAchievementGenerator.generateSweatyTenuguiAchievementBronze(tournament));
        achievementsGenerated
                .addAll(this.participationAchievementGenerator.generateSweatyTenuguiAchievementSilver(tournament));
        achievementsGenerated
                .addAll(this.participationAchievementGenerator.generateSweatyTenuguiAchievementGold(tournament));
        achievementsGenerated.addAll(this.winnerAchievementGenerator.generateTheWinnerAchievementGrade(tournament,
                AchievementGrade.BRONZE, DEFAULT_OCCURRENCES_BY_YEAR_BRONZE));
        achievementsGenerated.addAll(this.winnerAchievementGenerator.generateTheWinnerAchievementGrade(tournament,
                AchievementGrade.SILVER, DEFAULT_OCCURRENCES_BY_YEAR_SILVER));
        achievementsGenerated.addAll(this.winnerAchievementGenerator.generateTheWinnerAchievementGrade(tournament,
                AchievementGrade.GOLD, DEFAULT_OCCURRENCES_BY_YEAR_GOLD));
        achievementsGenerated.addAll(this.swissAchievementGenerator.generateSwissWinnerAchievementGrade(tournament,
                AchievementGrade.BRONZE, DEFAULT_OCCURRENCES_BY_YEAR_BRONZE));
        achievementsGenerated.addAll(this.swissAchievementGenerator.generateSwissWinnerAchievementGrade(tournament,
                AchievementGrade.SILVER, DEFAULT_OCCURRENCES_BY_YEAR_SILVER));
        achievementsGenerated.addAll(this.swissAchievementGenerator.generateSwissWinnerAchievementGrade(tournament,
                AchievementGrade.GOLD, DEFAULT_OCCURRENCES_BY_YEAR_GOLD));
        achievementsGenerated.addAll(this.swissAchievementGenerator.generateBuchholzWhispererAchievementGrade(
                tournament, AchievementGrade.BRONZE, DEFAULT_OCCURRENCES_BY_YEAR_BRONZE));
        achievementsGenerated.addAll(this.swissAchievementGenerator.generateBuchholzWhispererAchievementGrade(
                tournament, AchievementGrade.SILVER, DEFAULT_OCCURRENCES_BY_YEAR_SILVER));
        achievementsGenerated.addAll(this.swissAchievementGenerator.generateBuchholzWhispererAchievementGrade(
                tournament, AchievementGrade.GOLD, DEFAULT_OCCURRENCES_BY_YEAR_GOLD));
        achievementsGenerated.addAll(this.winnerAchievementGenerator.generateTheWinnerTeamAchievementGrade(tournament,
                AchievementGrade.BRONZE, DEFAULT_OCCURRENCES_BY_YEAR_BRONZE));
        achievementsGenerated.addAll(this.winnerAchievementGenerator.generateTheWinnerTeamAchievementGrade(tournament,
                AchievementGrade.SILVER, DEFAULT_OCCURRENCES_BY_YEAR_SILVER));
        achievementsGenerated.addAll(this.winnerAchievementGenerator.generateTheWinnerTeamAchievementGrade(tournament,
                AchievementGrade.GOLD, DEFAULT_OCCURRENCES_BY_YEAR_GOLD));
        achievementsGenerated.addAll(this.rankingModeAchievementGenerator.generateMasterTheLoopAchievementGrade(
                tournament, AchievementGrade.BRONZE, DEFAULT_OCCURRENCES_BY_YEAR_BRONZE));
        achievementsGenerated.addAll(this.rankingModeAchievementGenerator.generateMasterTheLoopAchievementGrade(
                tournament, AchievementGrade.SILVER, DEFAULT_OCCURRENCES_BY_YEAR_SILVER));
        achievementsGenerated.addAll(this.rankingModeAchievementGenerator.generateMasterTheLoopAchievementGrade(
                tournament, AchievementGrade.GOLD, DEFAULT_OCCURRENCES_BY_YEAR_GOLD));
        achievementsGenerated.addAll(this.membershipLongevityAchievementGenerator
                .generateNeverEndingStoryAchievement(tournament, AchievementGrade.BRONZE, PARTICIPANT_YEARS_BRONZE));
        achievementsGenerated.addAll(this.membershipLongevityAchievementGenerator
                .generateNeverEndingStoryAchievement(tournament, AchievementGrade.SILVER, PARTICIPANT_YEARS_SILVER));
        achievementsGenerated.addAll(this.membershipLongevityAchievementGenerator
                .generateNeverEndingStoryAchievement(tournament, AchievementGrade.GOLD, PARTICIPANT_YEARS_GOLD));
        achievementsGenerated.addAll(this.defenseAchievementGenerator
                .generateTisButAScratchAchievementBronze(tournament, this.getTotalScoreAgainstParticipant()));
        achievementsGenerated.addAll(this.defenseAchievementGenerator
                .generateTisButAScratchAchievementSilver(tournament, this.getTotalScoreAgainstParticipant()));
        achievementsGenerated.addAll(this.defenseAchievementGenerator.generateTisButAScratchAchievementGold(tournament,
                this.getTotalScoreAgainstParticipant()));
        achievementsGenerated.addAll(this.defenseAchievementGenerator.generateFirstBloodAchievementBronze(tournament,
                this.getTotalScoreFromParticipant()));
        achievementsGenerated.addAll(this.defenseAchievementGenerator.generateFirstBloodAchievementSilver(tournament,
                this.getTotalScoreFromParticipant()));
        achievementsGenerated.addAll(this.defenseAchievementGenerator.generateFirstBloodAchievementGold(tournament,
                this.getTotalScoreFromParticipant()));
        achievementsGenerated
                .addAll(this.scoreTechniqueAchievementGenerator.generateWoodcutterAchievementBronze(tournament));
        achievementsGenerated
                .addAll(this.scoreTechniqueAchievementGenerator.generateWoodcutterAchievementSilver(tournament));
        achievementsGenerated
                .addAll(this.scoreTechniqueAchievementGenerator.generateWoodcutterAchievementGold(tournament));
        achievementsGenerated
                .addAll(this.scoreTechniqueAchievementGenerator.generateHeadShotAchievementBronze(tournament));
        achievementsGenerated
                .addAll(this.scoreTechniqueAchievementGenerator.generateHeadShotAchievementSilver(tournament));
        achievementsGenerated
                .addAll(this.scoreTechniqueAchievementGenerator.generateHeadShotAchievementGold(tournament));
        achievementsGenerated
                .addAll(this.scoreTechniqueAchievementGenerator.generateYouAreUnderArrestAchievementBronze(tournament));
        achievementsGenerated
                .addAll(this.scoreTechniqueAchievementGenerator.generateYouAreUnderArrestAchievementSilver(tournament));
        achievementsGenerated
                .addAll(this.scoreTechniqueAchievementGenerator.generateYouAreUnderArrestAchievementGold(tournament));
        achievementsGenerated
                .addAll(this.flexibleBambooAchievementGenerator.generateFlexibleAsBambooAchievementBronze(tournament));
        achievementsGenerated
                .addAll(this.flexibleBambooAchievementGenerator.generateFlexibleAsBambooAchievementSilver(tournament));
        achievementsGenerated
                .addAll(this.flexibleBambooAchievementGenerator.generateFlexibleAsBambooAchievementGold(tournament));
        achievementsGenerated
                .addAll(this.participationAchievementGenerator.generateDarumaAchievementBronze(tournament));
        achievementsGenerated
                .addAll(this.participationAchievementGenerator.generateDarumaAchievementSilver(tournament));
        achievementsGenerated.addAll(this.participationAchievementGenerator.generateDarumaAchievementGold(tournament));
        achievementsGenerated.addAll(
                this.participationAchievementGenerator.generateStormtrooperSyndromeAchievementBronze(tournament));
        achievementsGenerated.addAll(
                this.participationAchievementGenerator.generateStormtrooperSyndromeAchievementSilver(tournament));
        achievementsGenerated
                .addAll(this.participationAchievementGenerator.generateStormtrooperSyndromeAchievementGold(tournament));
        achievementsGenerated.addAll(this.rivalryAchievementGenerator.generateVendettaAchievementBronze(tournament));
        achievementsGenerated.addAll(this.rivalryAchievementGenerator.generateVendettaAchievementSilver(tournament));
        achievementsGenerated.addAll(this.rivalryAchievementGenerator.generateVendettaAchievementGold(tournament));
        achievementsGenerated.addAll(this.durationAchievementGenerator.generateLongPathAchievement(tournament));
        return this.convertAll(achievementsGenerated);
    }

    private void removeAchievements(List<Achievement> achievements, AchievementType achievementType,
            Collection<AchievementGrade> grades, Collection<Participant> participants) {
        final List<Achievement> sourceAchievements = new ArrayList<>(achievements);
        achievements.removeIf(achievement -> grades.contains(achievement.getAchievementGrade())
                && achievement.getAchievementType() == achievementType
                && participants.contains(achievement.getParticipant()));
        sourceAchievements.removeAll(achievements);
        // Delete from database the ones that has been removed here.
        this.achievementProvider.delete(sourceAchievements);
    }
}
