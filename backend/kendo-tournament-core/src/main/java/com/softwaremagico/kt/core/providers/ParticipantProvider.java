package com.softwaremagico.kt.core.providers;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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

import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.ParticipantRepository;
import com.softwaremagico.kt.persistence.values.AchievementGrade;
import com.softwaremagico.kt.persistence.values.AchievementType;
import com.softwaremagico.kt.persistence.values.RoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ParticipantProvider extends CrudProvider<Participant, Integer, ParticipantRepository> {

    @Autowired
    public ParticipantProvider(ParticipantRepository repository) {
        super(repository);
    }

    public List<Participant> get(List<Integer> ids) {
        return getRepository().findByIdIn(ids);
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

    public List<Participant> findParticipantsWithRoleNotInTournaments(Tournament tournament, RoleType roleType, Collection<Tournament> olderTournaments) {
        return getRepository().findParticipantsWithRoleNotInTournaments(tournament, roleType, olderTournaments);
    }


    public List<Participant> getOriginalOrder(List<Integer> ids) {
        final List<Participant> databaseParticipants = getRepository().findByIdIn(ids);
        //JPA 'in' does not maintain the order. We need to sort them by the source list.
        final Map<Integer, Participant> participantsById = databaseParticipants.stream().collect(Collectors.toMap(Participant::getId, Function.identity()));
        final List<Participant> sortedParticipants = new ArrayList<>();
        for (final Integer id : ids) {
            sortedParticipants.add(participantsById.get(id));
        }
        return sortedParticipants;
    }
}
