package com.softwaremagico.kt.core.converters;

import com.softwaremagico.kt.core.controller.models.DuelDTO;
import com.softwaremagico.kt.core.converters.models.DuelConverterRequest;
import com.softwaremagico.kt.core.converters.models.ParticipantConverterRequest;
import com.softwaremagico.kt.persistence.entities.Duel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DuelConverter extends ElementConverter<Duel, DuelDTO, DuelConverterRequest> {
    private final ParticipantConverter participantConverter;

    @Autowired
    public DuelConverter(ParticipantConverter participantConverter) {
        this.participantConverter = participantConverter;
    }


    @Override
    public DuelDTO convert(DuelConverterRequest from) {
        final DuelDTO duelDTO = new DuelDTO();
        BeanUtils.copyProperties(from.getEntity(), duelDTO);
        duelDTO.setCompetitor1(participantConverter.convert(
                new ParticipantConverterRequest(from.getEntity().getCompetitor1())));
        duelDTO.setCompetitor2(participantConverter.convert(
                new ParticipantConverterRequest(from.getEntity().getCompetitor2())));
        return duelDTO;
    }

    @Override
    public Duel reverse(DuelDTO to) {
        if (to == null) {
            return null;
        }
        final Duel duel = new Duel();
        BeanUtils.copyProperties(duel, duel);
        duel.setCompetitor1(participantConverter.reverse(to.getCompetitor1()));
        duel.setCompetitor2(participantConverter.reverse(to.getCompetitor2()));
        return duel;
    }
}
