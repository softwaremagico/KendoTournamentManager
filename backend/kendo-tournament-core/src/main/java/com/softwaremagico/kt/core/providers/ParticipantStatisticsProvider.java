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

import com.softwaremagico.kt.core.statistics.ParticipantStatistics;
import com.softwaremagico.kt.core.statistics.ParticipantStatisticsRepository;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.utils.NameUtils;
import org.springframework.stereotype.Service;

@Service
public class ParticipantStatisticsProvider extends CrudProvider<ParticipantStatistics, Integer, ParticipantStatisticsRepository> {

    private final ParticipantFightStatisticsProvider fightStatisticsProvider;

    private final RoleProvider roleProvider;

    private final TournamentProvider tournamentProvider;

    protected ParticipantStatisticsProvider(ParticipantStatisticsRepository repository, ParticipantFightStatisticsProvider fightStatisticsProvider,
                                            RoleProvider roleProvider, TournamentProvider tournamentProvider) {
        super(repository);
        this.fightStatisticsProvider = fightStatisticsProvider;
        this.roleProvider = roleProvider;
        this.tournamentProvider = tournamentProvider;
    }

    public ParticipantStatistics get(Participant participant) {
        final ParticipantStatistics participantStatistics = new ParticipantStatistics();
        participantStatistics.setFightStatistics(fightStatisticsProvider.get(participant));
        participantStatistics.setParticipantId(participant.getId());
        participantStatistics.setParticipantName(NameUtils.getLastnameName(participant));
        for (final RoleType roleType : RoleType.values()) {
            participantStatistics.addRolePerformed(roleType, roleProvider.count(participant, roleType));
        }
        participantStatistics.setTournaments((int) participantStatistics.getRolesPerformed().values().stream().mapToDouble(d -> d).sum());
        participantStatistics.setTotalTournaments(tournamentProvider.countTournamentsAfter(participant.getCreatedAt()));
        participantStatistics.setParticipantCreatedAt(participant.getCreatedAt());
        return participantStatistics;
    }
}
