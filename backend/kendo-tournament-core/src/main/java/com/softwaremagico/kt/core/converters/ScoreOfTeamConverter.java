package com.softwaremagico.kt.core.converters;

import com.softwaremagico.kt.core.controller.models.ScoreOfTeamDTO;
import com.softwaremagico.kt.core.converters.models.DuelConverterRequest;
import com.softwaremagico.kt.core.converters.models.FightConverterRequest;
import com.softwaremagico.kt.core.converters.models.ScoreOfTeamConverterRequest;
import com.softwaremagico.kt.core.converters.models.TeamConverterRequest;
import com.softwaremagico.kt.core.score.ScoreOfTeam;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Component

public class ScoreOfTeamConverter extends ElementConverter<ScoreOfTeam, ScoreOfTeamDTO, ScoreOfTeamConverterRequest> {

    private final TeamConverter teamConverter;

    private final FightConverter fightConverter;

    private final DuelConverter duelConverter;

    public ScoreOfTeamConverter(TeamConverter teamConverter, FightConverter fightConverter, DuelConverter duelConverter) {
        this.teamConverter = teamConverter;
        this.fightConverter = fightConverter;
        this.duelConverter = duelConverter;
    }

    @Override
    protected ScoreOfTeamDTO convertElement(ScoreOfTeamConverterRequest from) {
        final ScoreOfTeamDTO scoreOfTeamDTO = new ScoreOfTeamDTO();
        BeanUtils.copyProperties(from.getEntity(), scoreOfTeamDTO, ConverterUtils.getNullPropertyNames(from.getEntity()));
        scoreOfTeamDTO.setTeam(teamConverter.convert(new TeamConverterRequest(from.getEntity().getTeam())));
        scoreOfTeamDTO.setFights(fightConverter.convertAll(from.getEntity().getFights().stream()
                .map(FightConverterRequest::new).collect(Collectors.toList())));
        scoreOfTeamDTO.setUnties(duelConverter.convertAll(from.getEntity().getUnties().stream()
                .map(DuelConverterRequest::new).collect(Collectors.toList())));
        return scoreOfTeamDTO;
    }

    @Override
    public ScoreOfTeam reverse(ScoreOfTeamDTO to) {
        if (to == null) {
            return null;
        }
        final ScoreOfTeam scoreOfTeam = new ScoreOfTeam();
        BeanUtils.copyProperties(to, scoreOfTeam, ConverterUtils.getNullPropertyNames(to));
        scoreOfTeam.setTeam(teamConverter.reverse(to.getTeam()));
        scoreOfTeam.setFights(new ArrayList<>());
        to.getFights().forEach(fight -> scoreOfTeam.getFights().add(fightConverter.reverse(fight)));
        scoreOfTeam.setUnties(new ArrayList<>());
        to.getUnties().forEach(duel -> scoreOfTeam.getUnties().add(duelConverter.reverse(duel)));
        return scoreOfTeam;
    }
}
