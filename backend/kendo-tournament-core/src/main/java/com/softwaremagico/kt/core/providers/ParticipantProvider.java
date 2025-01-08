package com.softwaremagico.kt.core.providers;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2024 Softwaremagico
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

import com.softwaremagico.kt.core.controller.models.TemporalToken;
import com.softwaremagico.kt.logger.KendoTournamentLogger;
import com.softwaremagico.kt.persistence.entities.Club;
import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.DuelRepository;
import com.softwaremagico.kt.persistence.repositories.ParticipantRepository;
import com.softwaremagico.kt.persistence.values.AchievementGrade;
import com.softwaremagico.kt.persistence.values.AchievementType;
import com.softwaremagico.kt.persistence.values.RoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ParticipantProvider extends CrudProvider<Participant, Integer, ParticipantRepository> {

    public static final String TOKEN_NAME_SEPARATOR = "_";

    private final DuelRepository duelRepository;

    @Autowired
    public ParticipantProvider(ParticipantRepository repository, DuelRepository duelRepository) {
        super(repository);
        this.duelRepository = duelRepository;
    }

    public List<Participant> get(Tournament tournament) {
        return getRepository().findByTournament(tournament);
    }

    public List<Participant> get(Tournament tournament, RoleType roleType) {
        return getRepository().findByTournamentAndRoleType(tournament, roleType);
    }

    public List<Participant> get(Tournament tournament, int differentRoleTypes) {
        return getRepository().findParticipantsWithMoreRoleTypesThan(tournament, differentRoleTypes);
    }

    public List<Participant> getParticipantsWithAchievementFromList(AchievementType achievementType, List<Participant> participants) {
        return getRepository().findParticipantsWithAchievementFromList(achievementType, participants);
    }

    public List<Participant> getParticipantsWithAchievementFromList(AchievementType achievementType, AchievementGrade achievementGrade,
                                                                    List<Participant> participants) {
        return getRepository().findParticipantsWithAchievementAndGradeFromList(achievementType, achievementGrade, participants);
    }

    public List<Participant> getParticipantsWithAchievement(AchievementType achievementType, AchievementGrade achievementGrade) {
        return getRepository().findParticipantsWithAchievementAndGrade(achievementType, achievementGrade);
    }

    public List<Participant> findParticipantsWithRoleNotInTournaments(Tournament tournament, RoleType roleType, Collection<Tournament> olderTournaments) {
        return getRepository().findParticipantsWithRoleNotInTournaments(tournament, roleType, olderTournaments);
    }


    public List<Participant> getOriginalOrder(List<Integer> ids) {
        final List<Participant> databaseParticipants = getRepository().findAllById(ids);
        //JPA 'in' does not maintain the order. We need to sort them by the source list.
        final Map<Integer, Participant> participantsById = databaseParticipants.stream().collect(Collectors.toMap(Participant::getId, Function.identity()));
        final List<Participant> sortedParticipants = new ArrayList<>();
        for (final Integer id : ids) {
            sortedParticipants.add(participantsById.get(id));
        }
        return sortedParticipants;
    }

    public List<Participant> get(Club club) {
        return getRepository().findByClub(club);
    }

    public Participant getByIdCard(String idCard) {
        return getRepository().findByIdCard(idCard);
    }

    public TemporalToken generateTemporalToken(Participant participant) {
        do {
            participant.generateTemporalToken();
        } while (getRepository().countByTemporalToken(participant.getTemporalToken()) > 0);
        return new TemporalToken(save(participant));
    }

    public Participant generateToken(Participant participant) {
        participant.generateToken();
        return save(participant);
    }

    public Optional<Participant> findByTemporalToken(String token) {
        if (token != null) {
            return getRepository().findByTemporalToken(token);
        }
        return Optional.empty();
    }

    public Optional<Participant> findByTokenUsername(String tokenUsername) {
        if (tokenUsername == null) {
            return Optional.empty();
        }
        if (tokenUsername.contains(ParticipantProvider.TOKEN_NAME_SEPARATOR)) {
            final String[] fields = tokenUsername.split(ParticipantProvider.TOKEN_NAME_SEPARATOR);
            try {
                return getRepository().findById(Integer.parseInt(fields[0]));
            } catch (NumberFormatException ignore) {
            }
        }
        KendoTournamentLogger.warning(this.getClass(), "Invalid id obtained from '{}'.", tokenUsername.replaceAll("[\n\r\t]", "_"));
        return Optional.empty();
    }

    public Participant getYourWorstNightmare(Participant sourceParticipant) {
        if (sourceParticipant == null) {
            return null;
        }
        final List<Duel> duels = duelRepository.findByParticipant(sourceParticipant);
        final Map<Participant, Integer> lostBy = new HashMap<>();
        for (Duel duel : duels) {
            final Participant winner = duel.getCompetitorWinner();
            if (!Objects.equals(winner, sourceParticipant)) {
                lostBy.put(winner, lostBy.getOrDefault(winner, 0) + 1);
            }
        }
        final List<Map.Entry<Participant, Integer>> sortedLostBy = lostBy.entrySet().stream().sorted(Map.Entry.comparingByValue()).toList();
        if (!sortedLostBy.isEmpty()) {
            return sortedLostBy.get(0).getKey();
        }
        return null;
    }


    public Participant getYouAreTheWorstNightmareOf(Participant sourceParticipant) {
        if (sourceParticipant == null) {
            return null;
        }
        final List<Duel> duels = duelRepository.findByParticipant(sourceParticipant);
        final Map<Participant, Integer> lostBy = new HashMap<>();
        for (Duel duel : duels) {
            final Participant winner = duel.getCompetitorWinner();
            final Participant looser = duel.getCompetitorLooser();
            if (Objects.equals(winner, sourceParticipant)) {
                lostBy.put(looser, lostBy.getOrDefault(looser, 0) + 1);
            }
        }
        final List<Map.Entry<Participant, Integer>> sortedLostBy = lostBy.entrySet().stream().sorted(Map.Entry.comparingByValue()).toList();
        if (!sortedLostBy.isEmpty()) {
            return sortedLostBy.get(0).getKey();
        }
        return null;
    }
}
