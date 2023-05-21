package com.softwaremagico.kt.core.converters;

import com.softwaremagico.kt.core.controller.models.ScoreOfCompetitorDTO;
import com.softwaremagico.kt.core.converters.models.DuelConverterRequest;
import com.softwaremagico.kt.core.converters.models.FightConverterRequest;
import com.softwaremagico.kt.core.converters.models.ParticipantConverterRequest;
import com.softwaremagico.kt.core.converters.models.ScoreOfCompetitorConverterRequest;
import com.softwaremagico.kt.core.score.ScoreOfCompetitor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
public class ScoreOfCompetitorConverter extends ElementConverter<ScoreOfCompetitor, ScoreOfCompetitorDTO, ScoreOfCompetitorConverterRequest> {

    private final ParticipantConverter participantConverter;

    private final FightConverter fightConverter;

    private final DuelConverter duelConverter;

    public ScoreOfCompetitorConverter(ParticipantConverter participantConverter, FightConverter fightConverter,
                                      DuelConverter duelConverter) {
        this.participantConverter = participantConverter;
        this.fightConverter = fightConverter;
        this.duelConverter = duelConverter;
    }

    @Override
    protected ScoreOfCompetitorDTO convertElement(ScoreOfCompetitorConverterRequest from) {
        final ScoreOfCompetitorDTO scoreOfCompetitorDTO = new ScoreOfCompetitorDTO();
        BeanUtils.copyProperties(from.getEntity(), scoreOfCompetitorDTO, ConverterUtils.getNullPropertyNames(from.getEntity()));
        scoreOfCompetitorDTO.setCompetitor(participantConverter.convert(new ParticipantConverterRequest(from.getEntity().getCompetitor())));
        scoreOfCompetitorDTO.setFights(fightConverter.convertAll(from.getEntity().getFights().stream()
                .map(FightConverterRequest::new).collect(Collectors.toList())));
        scoreOfCompetitorDTO.setUnties(duelConverter.convertAll(from.getEntity().getUnties().stream()
                .map(DuelConverterRequest::new).collect(Collectors.toList())));
        return scoreOfCompetitorDTO;
    }

    @Override
    public ScoreOfCompetitor reverse(ScoreOfCompetitorDTO to) {
        if (to == null) {
            return null;
        }
        final ScoreOfCompetitor scoreOfCompetitor = new ScoreOfCompetitor();
        BeanUtils.copyProperties(to, scoreOfCompetitor, ConverterUtils.getNullPropertyNames(to));
        scoreOfCompetitor.setCompetitor(participantConverter.reverse(to.getCompetitor()));
        scoreOfCompetitor.setFights(new ArrayList<>());
        to.getFights().forEach(fight -> scoreOfCompetitor.getFights().add(fightConverter.reverse(fight)));
        scoreOfCompetitor.setUnties(new ArrayList<>());
        to.getUnties().forEach(duel -> scoreOfCompetitor.getUnties().add(duelConverter.reverse(duel)));
        return scoreOfCompetitor;
    }
}
