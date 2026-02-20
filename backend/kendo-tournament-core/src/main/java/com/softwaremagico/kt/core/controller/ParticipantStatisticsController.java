package com.softwaremagico.kt.core.controller;

/*-
 * #%L
 * Kendo Participant Manager (Core)
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

import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantStatisticsDTO;
import com.softwaremagico.kt.core.converters.ParticipantConverter;
import com.softwaremagico.kt.core.converters.ParticipantStatisticsConverter;
import com.softwaremagico.kt.core.converters.models.ParticipantStatisticsConverterRequest;
import com.softwaremagico.kt.core.providers.ParticipantFightStatisticsProvider;
import com.softwaremagico.kt.core.providers.ParticipantStatisticsProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.core.statistics.ParticipantStatistics;
import com.softwaremagico.kt.core.statistics.ParticipantStatisticsRepository;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.repositories.RoleRepository;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.utils.NameUtils;
import org.springframework.stereotype.Controller;

@Controller
public class ParticipantStatisticsController extends BasicInsertableController<ParticipantStatistics, ParticipantStatisticsDTO,
        ParticipantStatisticsRepository, ParticipantStatisticsProvider, ParticipantStatisticsConverterRequest,
        ParticipantStatisticsConverter> {


    private final ParticipantConverter participantConverter;

    private final ParticipantFightStatisticsProvider fightStatisticsProvider;

    private final RoleRepository roleRepository;

    private final TournamentProvider tournamentProvider;

    protected ParticipantStatisticsController(ParticipantStatisticsProvider provider, ParticipantStatisticsConverter converter,
                                              ParticipantConverter participantConverter, ParticipantFightStatisticsProvider fightStatisticsProvider,
                                              RoleRepository roleRepository, TournamentProvider tournamentProvider) {
        super(provider, converter);
        this.participantConverter = participantConverter;
        this.fightStatisticsProvider = fightStatisticsProvider;
        this.roleRepository = roleRepository;
        this.tournamentProvider = tournamentProvider;
    }

    @Override
    protected ParticipantStatisticsConverterRequest createConverterRequest(ParticipantStatistics participantStatistics) {
        return new ParticipantStatisticsConverterRequest(participantStatistics);
    }

    public ParticipantStatisticsDTO get(ParticipantDTO participantDTO) {
        final Participant participant = participantConverter.reverse(participantDTO);
        final ParticipantStatistics participantStatistics = new ParticipantStatistics();
        participantStatistics.setFightStatistics(fightStatisticsProvider.get(participant));
        participantStatistics.setParticipantId(participant.getId());
        participantStatistics.setParticipantName(NameUtils.getLastnameName(participant));
        for (final RoleType roleType : RoleType.values()) {
            participantStatistics.addRolePerformed(roleType, roleRepository.countByParticipantAndRoleType(participant, roleType));
        }
        participantStatistics.setTournaments((int) participantStatistics.getRolesPerformed().values().stream().mapToDouble(d -> d).sum());
        participantStatistics.setTotalTournaments(tournamentProvider.countTournamentsAfter(participant.getCreatedAt()));
        participantStatistics.setParticipantCreatedAt(participant.getCreatedAt());
        return getConverter().convert(new ParticipantStatisticsConverterRequest(participantStatistics));
    }
}
