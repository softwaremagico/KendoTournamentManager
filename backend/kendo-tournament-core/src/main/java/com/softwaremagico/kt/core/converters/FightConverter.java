package com.softwaremagico.kt.core.converters;

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

import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.converters.models.DuelConverterRequest;
import com.softwaremagico.kt.core.converters.models.FightConverterRequest;
import com.softwaremagico.kt.core.converters.models.TeamConverterRequest;
import com.softwaremagico.kt.core.converters.models.TournamentConverterRequest;
import com.softwaremagico.kt.persistence.entities.Fight;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class FightConverter extends ElementConverter<Fight, FightDTO, FightConverterRequest> {
    private final TeamConverter teamConverter;
    private final TournamentConverter tournamentConverter;
    private final DuelConverter duelConverter;

    @Autowired
    public FightConverter(TeamConverter teamConverter, TournamentConverter tournamentConverter, DuelConverter duelConverter) {
        this.teamConverter = teamConverter;
        this.tournamentConverter = tournamentConverter;
        this.duelConverter = duelConverter;
    }


    @Override
    public FightDTO convert(FightConverterRequest from) {
        final FightDTO fightDTO = new FightDTO();
        BeanUtils.copyProperties(from.getEntity(), fightDTO);
        fightDTO.setFinishedAt(from.getEntity().getFinishedAt());
        fightDTO.setTeam1(teamConverter.convert(
                new TeamConverterRequest(from.getEntity().getTeam1())));
        fightDTO.setTeam2(teamConverter.convert(
                new TeamConverterRequest(from.getEntity().getTeam2())));
        fightDTO.setTournament(tournamentConverter.convert(
                new TournamentConverterRequest(from.getEntity().getTournament())));
        fightDTO.setDuels(new ArrayList<>());
        from.getEntity().getDuels().forEach(duel -> {
            fightDTO.getDuels().add(duelConverter.convert(new DuelConverterRequest(duel)));
        });
        return fightDTO;
    }

    @Override
    public Fight reverse(FightDTO to) {
        if (to == null) {
            return null;
        }
        final Fight fight = new Fight();
        BeanUtils.copyProperties(to, fight);
        fight.setTeam1(teamConverter.reverse(to.getTeam1()));
        fight.setTeam2(teamConverter.reverse(to.getTeam2()));
        fight.setTournament(tournamentConverter.reverse(to.getTournament()));
        fight.setFinishedAt(to.getFinishedAt());
        fight.setDuels(new ArrayList<>());
        to.getDuels().forEach(duelDTO -> fight.getDuels().add(duelConverter.reverse(duelDTO)));
        return fight;
    }
}
