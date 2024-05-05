package com.softwaremagico.kt.core.converters;

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

import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.converters.models.ParticipantConverterRequest;
import com.softwaremagico.kt.core.converters.models.RoleConverterRequest;
import com.softwaremagico.kt.core.converters.models.TournamentConverterRequest;
import com.softwaremagico.kt.persistence.entities.Role;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RoleConverter extends ElementConverter<Role, RoleDTO, RoleConverterRequest> {
    private final TournamentConverter tournamentConverter;
    private final ParticipantConverter participantConverter;
    private final ParticipantReducedConverter participantReducedConverter;

    @Autowired
    public RoleConverter(TournamentConverter tournamentConverter, ParticipantConverter participantConverter,
                         ParticipantReducedConverter participantReducedConverter) {
        this.tournamentConverter = tournamentConverter;
        this.participantConverter = participantConverter;
        this.participantReducedConverter = participantReducedConverter;
    }


    @Override
    protected RoleDTO convertElement(RoleConverterRequest from) {
        final RoleDTO roleDTO = new RoleDTO();
        BeanUtils.copyProperties(from.getEntity(), roleDTO, ConverterUtils.getNullPropertyNames(from.getEntity()));
        roleDTO.setTournament(tournamentConverter.convert(
                new TournamentConverterRequest(from.getEntity().getTournament())));
        roleDTO.setParticipant(participantReducedConverter.convert(
                new ParticipantConverterRequest(from.getEntity().getParticipant())));
        return roleDTO;
    }

    @Override
    public Role reverse(RoleDTO to) {
        if (to == null) {
            return null;
        }
        final Role role = new Role();
        BeanUtils.copyProperties(to, role, ConverterUtils.getNullPropertyNames(to));
        role.setTournament(tournamentConverter.reverse(to.getTournament()));
        role.setParticipant(participantConverter.reverse(to.getParticipant()));
        return role;
    }
}
