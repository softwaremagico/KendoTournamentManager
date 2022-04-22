package com.softwaremagico.kt.core.converters;

import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.converters.models.DuelConverterRequest;
import com.softwaremagico.kt.core.converters.models.FightConverterRequest;
import com.softwaremagico.kt.core.converters.models.TeamConverterRequest;
import com.softwaremagico.kt.core.converters.models.TournamentConverterRequest;
import com.softwaremagico.kt.persistence.entities.Fight;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class FightConverter extends ElementConverter<Fight, FightDTO, FightConverterRequest> {
    private final TeamConverter teamConverter;
    private final TournamentConverter tournamentConverter;
    private final DuelConverter duelConverter;

    @Autowired
    public FightConverter(TeamConverter teamConverter, TournamentConverter tournamentConverter, DuelConverter duelConverter) {
        this.teamConverter = teamConverter;
        this.tournamentConverter = tournamentConverter;
        this.duelConverter = duelConverter;
    }


    @Override
    public FightDTO convert(FightConverterRequest from) {
        final FightDTO fightDTO = new FightDTO();
        BeanUtils.copyProperties(from.getEntity(), fightDTO);
        fightDTO.setTeam1(teamConverter.convert(
                new TeamConverterRequest(from.getEntity().getTeam1())));
        fightDTO.setTeam2(teamConverter.convert(
                new TeamConverterRequest(from.getEntity().getTeam2())));
        fightDTO.setTournament(tournamentConverter.convert(
                new TournamentConverterRequest(from.getEntity().getTournament())));
        fightDTO.setDuels(new ArrayList<>());
        from.getEntity().getDuels().forEach(duel -> {
            fightDTO.getDuels().add(duelConverter.convert(new DuelConverterRequest(duel)));
        });
        return fightDTO;
    }

    @Override
    public Fight reverse(FightDTO to) {
        if (to == null) {
            return null;
        }
        final Fight fight = new Fight();
        BeanUtils.copyProperties(fight, fight);
        fight.setTeam1(teamConverter.reverse(to.getTeam1()));
        fight.setTeam2(teamConverter.reverse(to.getTeam2()));
        fight.setTournament(tournamentConverter.reverse(to.getTournament()));
        fight.setDuels(new ArrayList<>());
        to.getDuels().forEach(duelDTO -> fight.getDuels().add(duelConverter.reverse(duelDTO)));
        return fight;
    }
}
