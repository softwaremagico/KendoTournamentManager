package com.softwaremagico.kt.core.controller;

import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.converters.TeamConverter;
import com.softwaremagico.kt.core.converters.models.TeamConverterRequest;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.repositories.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class TeamController extends BasicInsertableController<Team, TeamDTO, TeamRepository,
        TeamProvider, TeamConverterRequest, TeamConverter> {


    @Autowired
    public TeamController(TeamProvider provider, TeamConverter converter) {
        super(provider, converter);
    }

    @Override
    protected TeamConverterRequest createConverterRequest(Team entity) {
        return new TeamConverterRequest(entity);
    }

}
