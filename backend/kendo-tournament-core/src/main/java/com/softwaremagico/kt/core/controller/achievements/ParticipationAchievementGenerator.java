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
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.core.providers.RoleProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.Achievement;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Role;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.AchievementGrade;
import com.softwaremagico.kt.persistence.values.AchievementType;
import com.softwaremagico.kt.persistence.values.RoleType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ParticipationAchievementGenerator extends ConsecutiveAchievementGenerationSupport {

    private static final int DEFAULT_NUMBER_BRONZE = 2;
    private static final int DEFAULT_NUMBER_SILVER = 3;
    private static final int DEFAULT_NUMBER_GOLD = 5;
    private static final int MIN_TOURNAMENT_FIGHTS = 5;
    private static final int DARUMA_TOURNAMENTS_NORMAL = 10;
    private static final int DARUMA_TOURNAMENTS_BRONZE = 20;
    private static final int DARUMA_TOURNAMENTS_SILVER = 30;
    private static final int DARUMA_TOURNAMENTS_GOLD = 50;
    private static final int SWEATY_TENUGUI_TOURNAMENTS_BRONZE = 10;
    private static final int SWEATY_TENUGUI_TOURNAMENTS_SILVER = 20;
    private static final int SWEATY_TENUGUI_TOURNAMENTS_GOLD = 30;

    private final ParticipantProvider participantProviderField;
    private final RoleProvider roleProvider;

    public ParticipationAchievementGenerator(AchievementProvider achievementProvider,
            ParticipantProvider participantProvider, TournamentProvider tournamentProvider, RoleProvider roleProvider) {
        super(achievementProvider, tournamentProvider, participantProvider);
        this.participantProviderField = participantProvider;
        this.roleProvider = roleProvider;
    }

    private Map<Participant, List<Role>> getRolesByParticipant(Tournament tournament) {
        final List<Role> roles = this.roleProvider.getBy(this.participantProviderField.get(tournament));
        return roles.stream().collect(Collectors.groupingBy(Role::getParticipant));
    }

    private Map<Participant, List<Role>> getRolesByParticipantUntil(Tournament tournament) {
        final Map<Participant, List<Role>> roles = new HashMap<>();
        for (final Map.Entry<Participant, List<Role>> entry : this.getRolesByParticipant(tournament).entrySet()) {
            roles.put(entry.getKey(), entry.getValue().stream().filter(role ->
                    role.getTournament().getCreatedAt() != null
                            && role.getTournament().getCreatedAt().isBefore(tournament.getCreatedAt())).toList());
        }
        return roles;
    }

    public List<Achievement> generateStormtrooperSyndromeAchievement(Tournament tournament,
            List<Fight> fightsFromTournament, List<Team> teamsFromTournament) {
        if (fightsFromTournament.size() < MIN_TOURNAMENT_FIGHTS) {
            return new ArrayList<>();
        }
        if (tournament.getTeamSize() < com.softwaremagico.kt.core.providers.TournamentProvider.DEFAULT_TEAM_SIZE) {
            return new ArrayList<>();
        }
        final List<Team> teams = new ArrayList<>(teamsFromTournament);
        final Set<Team> teamsWithFights = new HashSet<>();
        fightsFromTournament.forEach(fight -> {
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
        return this.generateAchievement(AchievementType.STORMTROOPER_SYNDROME, AchievementGrade.NORMAL,
                teams.stream().flatMap(team -> team.getMembers().stream()).toList(), tournament);
    }

    public List<Achievement> generateDarumaAchievement(Tournament tournament) {
        final List<Participant> participantsDaruma = new ArrayList<>();
        final List<Participant> alreadyDarumaAchievement = this.getAchievementProvider()
                .get(AchievementType.DARUMA, AchievementGrade.NORMAL).stream().map(Achievement::getParticipant)
                .toList();
        final Map<Participant, List<Role>> rolesByParticipant = this.getRolesByParticipant(tournament);
        this.participantProviderField.get(tournament).forEach(participant -> {
            if (rolesByParticipant.get(participant) != null
                    && rolesByParticipant.get(participant).size() >= DARUMA_TOURNAMENTS_NORMAL
                    && !alreadyDarumaAchievement.contains(participant)) {
                participantsDaruma.add(participant);
            }
        });
        return this.generateAchievement(AchievementType.DARUMA, AchievementGrade.NORMAL, participantsDaruma, tournament);
    }

    public List<Achievement> generateDarumaAchievementBronze(Tournament tournament) {
        final List<Participant> participantsDaruma = new ArrayList<>();
        final List<Tournament> previousTournaments = this.getTournamentProvider().getPreviousTo(tournament);
        previousTournaments.add(tournament);
        final List<Participant> alreadyDarumaAchievement = this.getAchievementProvider()
                .get(AchievementType.DARUMA, AchievementGrade.BRONZE).stream().map(Achievement::getParticipant)
                .toList();
        final Map<Participant, List<Role>> rolesByParticipant = this.getRolesByParticipant(tournament);
        this.participantProviderField.get(tournament).forEach(participant -> {
            if (rolesByParticipant.get(participant) != null) {
                final List<Role> previousRoles = new ArrayList<>(rolesByParticipant.get(participant)).stream()
                        .filter(role -> previousTournaments.contains(role.getTournament())).toList();
                if (previousRoles.size() >= DARUMA_TOURNAMENTS_BRONZE && !alreadyDarumaAchievement.contains(participant)) {
                    participantsDaruma.add(participant);
                }
            }
        });
        return this.generateAchievement(AchievementType.DARUMA, AchievementGrade.BRONZE, participantsDaruma, tournament);
    }

    public List<Achievement> generateDarumaAchievementSilver(Tournament tournament) {
        final List<Participant> participantsDaruma = new ArrayList<>();
        final List<Tournament> previousTournaments = this.getTournamentProvider().getPreviousTo(tournament);
        previousTournaments.add(tournament);
        final List<Participant> alreadyDarumaAchievement = this.getAchievementProvider()
                .get(AchievementType.DARUMA, AchievementGrade.SILVER).stream().map(Achievement::getParticipant)
                .toList();
        final Map<Participant, List<Role>> rolesByParticipant = this.getRolesByParticipant(tournament);
        this.participantProviderField.get(tournament).forEach(participant -> {
            if (rolesByParticipant.get(participant) != null) {
                final List<Role> previousRoles = new ArrayList<>(rolesByParticipant.get(participant)).stream()
                        .filter(role -> previousTournaments.contains(role.getTournament())).toList();
                if (previousRoles.size() >= DARUMA_TOURNAMENTS_SILVER && !alreadyDarumaAchievement.contains(participant)) {
                    participantsDaruma.add(participant);
                }
            }
        });
        return this.generateAchievement(AchievementType.DARUMA, AchievementGrade.SILVER, participantsDaruma, tournament);
    }

    public List<Achievement> generateDarumaAchievementGold(Tournament tournament) {
        final List<Participant> participantsDaruma = new ArrayList<>();
        final List<Tournament> previousTournaments = this.getTournamentProvider().getPreviousTo(tournament);
        previousTournaments.add(tournament);
        final List<Participant> alreadyDarumaAchievement = this.getAchievementProvider()
                .get(AchievementType.DARUMA, AchievementGrade.GOLD).stream().map(Achievement::getParticipant)
                .toList();
        final Map<Participant, List<Role>> rolesByParticipant = this.getRolesByParticipant(tournament);
        this.participantProviderField.get(tournament).forEach(participant -> {
            if (rolesByParticipant.get(participant) != null) {
                final List<Role> previousRoles = new ArrayList<>(rolesByParticipant.get(participant)).stream()
                        .filter(role -> previousTournaments.contains(role.getTournament())).toList();
                if (previousRoles.size() >= DARUMA_TOURNAMENTS_GOLD && !alreadyDarumaAchievement.contains(participant)) {
                    participantsDaruma.add(participant);
                }
            }
        });
        return this.generateAchievement(AchievementType.DARUMA, AchievementGrade.GOLD, participantsDaruma, tournament);
    }

    public List<Achievement> generateSweatyTenuguiAchievement(Tournament tournament) {
        final List<Participant> participants = this.participantProviderField.get(tournament, RoleType.COMPETITOR);
        this.participantProviderField.getParticipantsWithAchievement(AchievementType.SWEATY_TENUGUI, AchievementGrade.NORMAL)
                .forEach(participants::remove);
        return this.generateAchievement(AchievementType.SWEATY_TENUGUI, AchievementGrade.NORMAL, participants, tournament);
    }

    public List<Achievement> generateSweatyTenuguiAchievementBronze(Tournament tournament) {
        final Map<Participant, List<Role>> rolesByParticipantFromTournament = new HashMap<>(this.getRolesByParticipantUntil(tournament));
        final Set<Participant> participants = new HashSet<>(this.getRolesByParticipantUntil(tournament).keySet());
        rolesByParticipantFromTournament.forEach((participant, roles) -> {
            if (roles.stream().filter(role -> role.getRoleType() == RoleType.COMPETITOR)
                    .toList().size() < SWEATY_TENUGUI_TOURNAMENTS_BRONZE) {
                participants.remove(participant);
            }
        });
        this.participantProviderField.getParticipantsWithAchievement(AchievementType.SWEATY_TENUGUI, AchievementGrade.BRONZE)
                .forEach(participants::remove);
        return this.generateAchievement(AchievementType.SWEATY_TENUGUI, AchievementGrade.BRONZE, participants, tournament);
    }

    public List<Achievement> generateSweatyTenuguiAchievementSilver(Tournament tournament) {
        final Map<Participant, List<Role>> rolesByParticipantFromTournament = new HashMap<>(this.getRolesByParticipantUntil(tournament));
        final Set<Participant> participants = new HashSet<>(this.getRolesByParticipantUntil(tournament).keySet());
        rolesByParticipantFromTournament.forEach((participant, roles) -> {
            if (roles.stream().filter(role -> role.getRoleType() == RoleType.COMPETITOR)
                    .toList().size() < SWEATY_TENUGUI_TOURNAMENTS_SILVER) {
                participants.remove(participant);
            }
        });
        this.participantProviderField.getParticipantsWithAchievement(AchievementType.SWEATY_TENUGUI, AchievementGrade.SILVER)
                .forEach(participants::remove);
        return this.generateAchievement(AchievementType.SWEATY_TENUGUI, AchievementGrade.SILVER, participants, tournament);
    }

    public List<Achievement> generateSweatyTenuguiAchievementGold(Tournament tournament) {
        final Map<Participant, List<Role>> rolesByParticipantFromTournament = new HashMap<>(this.getRolesByParticipant(tournament));
        final Set<Participant> participants = new HashSet<>(this.getRolesByParticipant(tournament).keySet());
        rolesByParticipantFromTournament.forEach((participant, roles) -> {
            if (roles.stream().filter(role -> role.getRoleType() == RoleType.COMPETITOR)
                    .toList().size() < SWEATY_TENUGUI_TOURNAMENTS_GOLD) {
                participants.remove(participant);
            }
        });
        this.participantProviderField.getParticipantsWithAchievement(AchievementType.SWEATY_TENUGUI, AchievementGrade.GOLD)
                .forEach(participants::remove);
        return this.generateAchievement(AchievementType.SWEATY_TENUGUI, AchievementGrade.GOLD, participants, tournament);
    }

    public List<Achievement> generateStormtrooperSyndromeAchievementBronze(Tournament tournament) {
        return this.generateConsecutiveGradeAchievements(tournament, DEFAULT_NUMBER_BRONZE,
                AchievementType.STORMTROOPER_SYNDROME, AchievementGrade.BRONZE);
    }

    public List<Achievement> generateStormtrooperSyndromeAchievementSilver(Tournament tournament) {
        return this.generateConsecutiveGradeAchievements(tournament, DEFAULT_NUMBER_SILVER,
                AchievementType.STORMTROOPER_SYNDROME, AchievementGrade.SILVER);
    }

    public List<Achievement> generateStormtrooperSyndromeAchievementGold(Tournament tournament) {
        return this.generateConsecutiveGradeAchievements(tournament, DEFAULT_NUMBER_GOLD,
                AchievementType.STORMTROOPER_SYNDROME, AchievementGrade.GOLD);
    }
}
