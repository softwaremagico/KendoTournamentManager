package com.softwaremagico.kt.core.converters;

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

    @Autowired
    public RoleConverter(TournamentConverter tournamentConverter, ParticipantConverter participantConverter) {
        this.tournamentConverter = tournamentConverter;
        this.participantConverter = participantConverter;
    }


    @Override
    public RoleDTO convert(RoleConverterRequest from) {
        final RoleDTO roleDTO = new RoleDTO();
        BeanUtils.copyProperties(from.getEntity(), roleDTO);
        roleDTO.setTournament(tournamentConverter.convert(
                new TournamentConverterRequest(from.getEntity().getTournament())));
        roleDTO.setParticipant(participantConverter.convert(
                new ParticipantConverterRequest(from.getEntity().getParticipant())));
        return roleDTO;
    }

    @Override
    public Role reverse(RoleDTO to) {
        if (to == null) {
            return null;
        }
        final Role role = new Role();
        BeanUtils.copyProperties(role, role);
        role.setTournament(tournamentConverter.reverse(to.getTournament()));
        role.setParticipant(participantConverter.reverse(to.getParticipant()));
        return role;
    }
}
