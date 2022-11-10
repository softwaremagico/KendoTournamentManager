package com.softwaremagico.kt.core.controller;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2022 Softwaremagico
 * %%
 * This software is designed by Jorge Hortelano Otero. Jorge Hortelano Otero
 * <softwaremagico@gmail.com> Valencia (Spain).
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.models.TournamentConverterRequest;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.TournamentRepository;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class TournamentController extends BasicInsertableController<Tournament, TournamentDTO, TournamentRepository,
        TournamentProvider, TournamentConverterRequest, TournamentConverter> {

    private final GroupProvider groupProvider;


    @Autowired
    public TournamentController(TournamentProvider provider, TournamentConverter converter, GroupProvider groupProvider) {
        super(provider, converter);
        this.groupProvider = groupProvider;
    }

    @Override
    protected TournamentConverterRequest createConverterRequest(Tournament entity) {
        return new TournamentConverterRequest(entity);
    }

    @Override
    public TournamentDTO create(TournamentDTO tournamentDTO, String username) {
        final TournamentDTO createdTournamentDto = super.create(tournamentDTO, username);
        final Group group = new Group();
        group.setCreatedBy(username);
        groupProvider.addGroup(converter.reverse(createdTournamentDto), group);
        return createdTournamentDto;
    }

    public TournamentDTO create(String name, Integer shiaijos, Integer teamSize, TournamentType type, String username) {
        final TournamentDTO tournamentDTO = converter.convert(createConverterRequest(provider.save(new Tournament(name, shiaijos != null ? shiaijos : 1,
                teamSize != null ? teamSize : 3, type != null ? type : TournamentType.LEAGUE, username))));
        //Add default group:
        final Group group = new Group();
        group.setCreatedBy(username);
        groupProvider.addGroup(converter.reverse(tournamentDTO), group);
        return tournamentDTO;
    }

}
