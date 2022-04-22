package com.softwaremagico.kt.core.controller;

import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.converters.IElementConverter;
import com.softwaremagico.kt.core.converters.models.ParticipantConverterRequest;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.persistence.entities.Participant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Collection;

@Controller
public class ParticipantController implements IElementConverter<Participant, ParticipantDTO, ParticipantConverterRequest> {
    private final ParticipantProvider participantProvider;


    @Autowired
    public ParticipantController(ParticipantProvider participantProvider) {
        this.participantProvider = participantProvider;
    }


    @Override
    public ParticipantDTO convert(ParticipantConverterRequest from) {
        return null;
    }

    @Override
    public Collection<ParticipantDTO> convertAll(Collection<ParticipantConverterRequest> from) {
        return null;
    }

    @Override
    public ParticipantConverterRequest reverse(ParticipantDTO to) {
        return null;
    }

    @Override
    public Collection<ParticipantConverterRequest> reverseAll(Collection<ParticipantDTO> to) {
        return null;
    }
}
