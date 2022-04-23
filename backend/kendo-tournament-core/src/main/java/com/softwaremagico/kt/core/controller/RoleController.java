package com.softwaremagico.kt.core.controller;

import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.ParticipantConverter;
import com.softwaremagico.kt.core.converters.RoleConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.models.RoleConverterRequest;
import com.softwaremagico.kt.core.exceptions.TournamentNotFoundException;
import com.softwaremagico.kt.core.providers.RoleProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.Role;
import com.softwaremagico.kt.persistence.repositories.RoleRepository;
import com.softwaremagico.kt.persistence.values.RoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
        return converter.convertAll(provider.getAll(tournamentProvider.get(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(getClass(), "Tournament with id '" + tournamentId + "' does not exists."))).stream()
                .map(this::createConverterRequest).collect(Collectors.toList()));
    }

    public List<RoleDTO> get(TournamentDTO tournamentDTO) {
        return converter.convertAll(provider.getAll(tournamentConverter.reverse(tournamentDTO)).stream()
                .map(this::createConverterRequest).collect(Collectors.toList()));
    }

    public List<RoleDTO> get(TournamentDTO tournamentDTO, RoleType roleType) {
        return converter.convertAll(provider.getAll(tournamentConverter.reverse(tournamentDTO), roleType).stream()
                .map(this::createConverterRequest).collect(Collectors.toList()));
    }

    public List<RoleDTO> get(Integer tournamentId, Collection<RoleType> roleTypes) {
        return converter.convertAll(provider.getAll(tournamentProvider.get(tournamentId)
                        .orElseThrow(() -> new TournamentNotFoundException(getClass(), "Tournament with id '" + tournamentId + "' does not exists.")),
                roleTypes).stream().map(this::createConverterRequest).collect(Collectors.toList()));
    }

    public List<RoleDTO> get(TournamentDTO tournamentDTO, Collection<RoleType> roleTypes) {
        return converter.convertAll(provider.getAll(tournamentConverter.reverse(tournamentDTO), roleTypes).stream()
                .map(this::createConverterRequest).collect(Collectors.toList()));
    }

    public long count(TournamentDTO tournamentDTO) {
        return provider.count(tournamentConverter.reverse(tournamentDTO));
    }

    public void delete(ParticipantDTO participantDTO, TournamentDTO tournamentDTO) {
        provider.delete(participantConverter.reverse(participantDTO), tournamentConverter.reverse(tournamentDTO));
    }

}
