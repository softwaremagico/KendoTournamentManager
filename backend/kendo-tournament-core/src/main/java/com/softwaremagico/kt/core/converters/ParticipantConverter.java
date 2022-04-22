package com.softwaremagico.kt.core.converters;

import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.converters.models.ClubConverterRequest;
import com.softwaremagico.kt.core.converters.models.ParticipantConverterRequest;
import com.softwaremagico.kt.persistence.entities.Participant;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ParticipantConverter extends ElementConverter<Participant, ParticipantDTO, ParticipantConverterRequest> {
    private final ClubConverter clubConverter;

    @Autowired
    public ParticipantConverter(ClubConverter clubConverter) {
        this.clubConverter = clubConverter;
    }


    @Override
    public ParticipantDTO convert(ParticipantConverterRequest from) {
        final ParticipantDTO participantDTO = new ParticipantDTO();
        BeanUtils.copyProperties(from.getEntity(), participantDTO);
        participantDTO.setClub(clubConverter.convert(new ClubConverterRequest(from.getEntity().getClub())));
        return participantDTO;
    }

    @Override
    public Participant reverse(ParticipantDTO to) {
        if (to == null) {
            return null;
        }
        final Participant participant = new Participant();
        participant.setClub(clubConverter.reverse(to.getClub()));
        BeanUtils.copyProperties(to, participant);
        return participant;
    }
}
