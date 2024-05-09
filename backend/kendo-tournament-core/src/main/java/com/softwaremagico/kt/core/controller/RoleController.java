package com.softwaremagico.kt.core.controller;

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

import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.ParticipantConverter;
import com.softwaremagico.kt.core.converters.RoleConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.models.RoleConverterRequest;
import com.softwaremagico.kt.core.exceptions.TournamentNotFoundException;
import com.softwaremagico.kt.core.exceptions.ValidateBadRequestException;
import com.softwaremagico.kt.core.providers.RoleProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.Role;
import com.softwaremagico.kt.persistence.repositories.RoleRepository;
import com.softwaremagico.kt.persistence.values.RoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Collection;
import java.util.List;

@Controller
public class RoleController extends BasicInsertableController<Role, RoleDTO, RoleRepository,
        RoleProvider, RoleConverterRequest, RoleConverter> {
    private final TournamentConverter tournamentConverter;
    private final TournamentProvider tournamentProvider;
    private final ParticipantConverter participantConverter;


    @Autowired
    public RoleController(RoleProvider provider, RoleConverter converter, TournamentConverter tournamentConverter,
                          TournamentProvider tournamentProvider, ParticipantConverter participantConverter) {
        super(provider, converter);
        this.tournamentConverter = tournamentConverter;
        this.tournamentProvider = tournamentProvider;
        this.participantConverter = participantConverter;
    }

    @Override
    protected RoleConverterRequest createConverterRequest(Role entity) {
        return new RoleConverterRequest(entity);
    }

    public List<RoleDTO> getByTournamentId(Integer tournamentId) {
        return convertAll(getProvider().getAll(tournamentProvider.get(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(getClass(), "Tournament with id '" + tournamentId + "' does not exists."))));
    }

    public List<RoleDTO> get(TournamentDTO tournamentDTO, List<ParticipantDTO> participantsDTOs) {
        return convertAll(getProvider().get(tournamentConverter.reverse(tournamentDTO), participantConverter.reverseAll(participantsDTOs)));
    }

    public RoleDTO get(TournamentDTO tournamentDTO, ParticipantDTO participantDTO) {
        return convert(getProvider().get(tournamentConverter.reverse(tournamentDTO), participantConverter.reverse(participantDTO)));
    }

    public List<RoleDTO> get(TournamentDTO tournamentDTO) {
        return convertAll(getProvider().getAll(tournamentConverter.reverse(tournamentDTO)));
    }

    public List<RoleDTO> get(TournamentDTO tournamentDTO, RoleType roleType) {
        return convertAll(getProvider().getAll(tournamentConverter.reverse(tournamentDTO), roleType));
    }

    public List<RoleDTO> get(Integer tournamentId, Collection<RoleType> roleTypes) {
        return convertAll(getProvider().getAll(tournamentProvider.get(tournamentId)
                        .orElseThrow(() -> new TournamentNotFoundException(getClass(), "Tournament with id '" + tournamentId + "' does not exists.")),
                roleTypes));
    }

    public List<RoleDTO> get(TournamentDTO tournamentDTO, Collection<RoleType> roleTypes) {
        return convertAll(getProvider().getAll(tournamentConverter.reverse(tournamentDTO), roleTypes));
    }

    public List<RoleDTO> getForAccreditations(TournamentDTO tournamentDTO, Boolean onlyNewAccreditations, Collection<RoleType> roleTypes) {
        return convertAll(getProvider().getAllForAccreditations(tournamentConverter.reverse(tournamentDTO), onlyNewAccreditations, roleTypes));
    }

    public List<RoleDTO> getForDiplomas(TournamentDTO tournamentDTO, Boolean onlyNewDiplomas, Collection<RoleType> roleTypes) {
        return convertAll(getProvider().getAllForDiplomas(tournamentConverter.reverse(tournamentDTO), onlyNewDiplomas, roleTypes));
    }

    public long count(TournamentDTO tournamentDTO) {
        return getProvider().count(tournamentConverter.reverse(tournamentDTO));
    }

    public void delete(ParticipantDTO participantDTO, TournamentDTO tournamentDTO) {
        getProvider().delete(participantConverter.reverse(participantDTO), tournamentConverter.reverse(tournamentDTO));
    }

    @Override
    public RoleDTO create(RoleDTO roleDTO, String username) {
        //Delete any previous role.
        delete(roleDTO.getParticipant(), roleDTO.getTournament());
        //Add the new role.
        return super.create(roleDTO, username);
    }

    @Override
    public void validate(RoleDTO roleDTO) throws ValidateBadRequestException {
        if (roleDTO == null || roleDTO.getTournament() == null || roleDTO.getParticipant() == null
                || roleDTO.getRoleType() == null) {
            throw new ValidateBadRequestException(getClass(), "Role data is missing");
        }
    }

    public void delete(TournamentDTO tournamentDTO) {
        getProvider().delete(tournamentConverter.reverse(tournamentDTO));
    }

}
