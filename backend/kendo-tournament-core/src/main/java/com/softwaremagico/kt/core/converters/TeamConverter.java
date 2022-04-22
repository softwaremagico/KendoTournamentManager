package com.softwaremagico.kt.core.converters;

import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.converters.models.ParticipantConverterRequest;
import com.softwaremagico.kt.core.converters.models.TeamConverterRequest;
import com.softwaremagico.kt.core.converters.models.TournamentConverterRequest;
import com.softwaremagico.kt.persistence.entities.Team;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class TeamConverter extends ElementConverter<Team, TeamDTO, TeamConverterRequest> {
    private final TournamentConverter tournamentConverter;
    private final ParticipantConverter participantConverter;

    @Autowired
    public TeamConverter(TournamentConverter tournamentConverter, ParticipantConverter participantConverter) {
        this.tournamentConverter = tournamentConverter;
        this.participantConverter = participantConverter;
    }


    @Override
    public TeamDTO convert(TeamConverterRequest from) {
        final TeamDTO teamDTO = new TeamDTO();
        BeanUtils.copyProperties(from.getEntity(), teamDTO);
        teamDTO.setMembers(new ArrayList<>());
        teamDTO.setTournament(tournamentConverter.convert(
                new TournamentConverterRequest(from.getEntity().getTournament())));
        from.getEntity().getMembers().forEach(member ->
                teamDTO.getMembers().add(participantConverter.convert(new ParticipantConverterRequest(member))));
        return teamDTO;
    }

    @Override
    public Team reverse(TeamDTO to) {
        if (to == null) {
            return null;
        }
        final Team team = new Team();
        BeanUtils.copyProperties(team, team);
        team.setTournament(tournamentConverter.reverse(to.getTournament()));
        team.setMembers(new ArrayList<>());
        to.getMembers().forEach(member -> team.getMembers().add(participantConverter.reverse(member)));
        return team;
    }
}
