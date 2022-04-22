package com.softwaremagico.kt.core.controller;

import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.converters.ParticipantConverter;
import com.softwaremagico.kt.core.converters.models.ParticipantConverterRequest;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.repositories.ParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class ParticipantController extends BasicInsertableController<Participant, ParticipantDTO, ParticipantRepository,
        ParticipantProvider, ParticipantConverterRequest, ParticipantConverter> {


    @Autowired
    public ParticipantController(ParticipantProvider provider, ParticipantConverter converter) {
        super(provider, converter);
    }

    @Override
    protected ParticipantConverterRequest createConverterRequest(Participant participant) {
        return new ParticipantConverterRequest(participant);
    }

}
