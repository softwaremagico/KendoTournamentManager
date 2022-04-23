package com.softwaremagico.kt.core.controller;

import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.models.TournamentConverterRequest;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.TournamentRepository;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class TournamentController extends BasicInsertableController<Tournament, TournamentDTO, TournamentRepository,
        TournamentProvider, TournamentConverterRequest, TournamentConverter> {


    @Autowired
    public TournamentController(TournamentProvider provider, TournamentConverter converter) {
        super(provider, converter);
    }

    @Override
    protected TournamentConverterRequest createConverterRequest(Tournament entity) {
        return new TournamentConverterRequest(entity);
    }

    public TournamentDTO create(String name, Integer shiaijos, Integer teamSize, TournamentType type) {
        return converter.convert(createConverterRequest(provider.save(new Tournament(name, shiaijos != null ? shiaijos : 1, teamSize != null ? teamSize : 3,
                type != null ? type : TournamentType.LEAGUE))));
    }

}