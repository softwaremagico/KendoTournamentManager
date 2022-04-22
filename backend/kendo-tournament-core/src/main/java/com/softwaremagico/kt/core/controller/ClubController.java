package com.softwaremagico.kt.core.controller;

import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.core.converters.ClubConverter;
import com.softwaremagico.kt.core.converters.models.ClubConverterRequest;
import com.softwaremagico.kt.core.providers.ClubProvider;
import com.softwaremagico.kt.persistence.entities.Club;
import com.softwaremagico.kt.persistence.repositories.ClubRepository;
import org.springframework.stereotype.Controller;

@Controller
public class ClubController extends BasicInsertableController<Club, ClubDTO, ClubRepository, ClubProvider, ClubConverterRequest, ClubConverter> {

    public ClubController(ClubProvider provider, ClubConverter converter) {
        super(provider, converter);
    }

    @Override
    protected ClubConverterRequest createConverterRequest(Club club) {
        return new ClubConverterRequest(club);
    }

    public ClubDTO create(String name, String country, String city) {
        return converter.convert(new ClubConverterRequest(provider.add(name, country, city)));
    }
}
