package com.softwaremagico.kt.core.controller;

import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.ParticipantConverter;
import com.softwaremagico.kt.core.converters.TeamConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.models.TeamConverterRequest;
import com.softwaremagico.kt.core.exceptions.TournamentNotFoundException;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.repositories.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class TeamController extends BasicInsertableController<Team, TeamDTO, TeamRepository,
        TeamProvider, TeamConverterRequest, TeamConverter> {
    private final TournamentProvider tournamentProvider;
    private final TournamentConverter tournamentConverter;
    private final ParticipantConverter participantConverter;


    @Autowired
    public TeamController(TeamProvider provider, TeamConverter converter, TournamentProvider tournamentProvider, TournamentConverter tournamentConverter, ParticipantConverter participantConverter) {
        super(provider, converter);
        this.tournamentProvider = tournamentProvider;
        this.tournamentConverter = tournamentConverter;
        this.participantConverter = participantConverter;
    }

    @Override
    protected TeamConverterRequest createConverterRequest(Team entity) {
        return new TeamConverterRequest(entity);
    }

    public List<TeamDTO> getAllByTournament(TournamentDTO tournamentDTO) {
        return converter.convertAll(provider.getAll(tournamentConverter.reverse(tournamentDTO)).stream()
                .map(this::createConverterRequest).collect(Collectors.toList()));
    }

    public List<TeamDTO> getAllByTournament(Integer tournamentId) {
        return converter.convertAll(provider.getAll(tournamentProvider.get(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(getClass(), "No tournamnet found with id '" + tournamentId + "'.")))
                .stream().map(this::createConverterRequest).collect(Collectors.toList()));
    }

    @Override
    public TeamDTO create(TeamDTO teamDTO) {
        if (teamDTO.getGroup() == null) {
            teamDTO.setGroup(1);
        }
        if (teamDTO.getName() == null) {
            teamDTO.setName(provider.getNextDefaultName(tournamentConverter.reverse(teamDTO.getTournament())));
        }
        return super.create(teamDTO);
    }

    public TeamDTO delete(TournamentDTO tournamentDTO, ParticipantDTO member) {
        Team team = provider.delete(tournamentConverter.reverse(tournamentDTO), participantConverter.reverse(member)).orElse(null);
        if (team != null) {
            return converter.convert(new TeamConverterRequest(team));
        }
        return null;
    }

    public void delete(TournamentDTO tournamentDTO) {
        provider.delete(tournamentConverter.reverse(tournamentDTO));
    }

    @Override
    public TeamDTO update(TeamDTO teamDTO) {
        if (teamDTO.getGroup() == null) {
            teamDTO.setGroup(1);
        }
        return super.update(teamDTO);
    }


}
