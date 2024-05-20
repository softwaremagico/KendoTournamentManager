package com.softwaremagico.kt.core.converters;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2024 Softwaremagico
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.converters.models.DuelConverterRequest;
import com.softwaremagico.kt.core.converters.models.FightConverterRequest;
import com.softwaremagico.kt.core.converters.models.TeamConverterRequest;
import com.softwaremagico.kt.core.converters.models.TournamentConverterRequest;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.repositories.TournamentRepository;
import org.hibernate.LazyInitializationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class FightConverter extends ElementConverter<Fight, FightDTO, FightConverterRequest> {
    private final TeamConverter teamConverter;
    private final TournamentConverter tournamentConverter;
    private final TournamentRepository tournamentRepository;
    private final DuelConverter duelConverter;

    @Autowired
    public FightConverter(TeamConverter teamConverter, TournamentConverter tournamentConverter, TournamentRepository tournamentRepository,
                          DuelConverter duelConverter) {
        this.teamConverter = teamConverter;
        this.tournamentConverter = tournamentConverter;
        this.tournamentRepository = tournamentRepository;
        this.duelConverter = duelConverter;
    }


    @Override
    protected FightDTO convertElement(FightConverterRequest from) {
        final FightDTO fightDTO = new FightDTO();
        BeanUtils.copyProperties(from.getEntity(), fightDTO, ConverterUtils.getNullPropertyNames(from.getEntity()));

        try {
            if (from.getTournamentDTO() != null) {
                fightDTO.setTournament(from.getTournamentDTO());
            } else if (from.getTournament() != null) {
                //Converter can have the tournament defined already.
                fightDTO.setTournament(tournamentConverter.convert(
                        new TournamentConverterRequest(from.getTournament())));
            } else {
                fightDTO.setTournament(tournamentConverter.convert(
                        new TournamentConverterRequest(from.getEntity().getTournament())));
            }
        } catch (LazyInitializationException | FatalBeanException e) {
            fightDTO.setTournament(tournamentConverter.convert(
                    new TournamentConverterRequest(tournamentRepository.findById(from.getEntity().getTournament().getId()).orElse(null))));
        }

        //Getting the tournament to send to duels and avoid hundreds of calls.

        fightDTO.setTeam1(teamConverter.convert(
                new TeamConverterRequest(from.getEntity().getTeam1(), fightDTO.getTournament())));
        fightDTO.setTeam2(teamConverter.convert(
                new TeamConverterRequest(from.getEntity().getTeam2(), fightDTO.getTournament())));

        fightDTO.setDuels(new ArrayList<>());
        from.getEntity().getDuels().forEach(duel -> fightDTO.getDuels().add(duelConverter.convert(
                new DuelConverterRequest(duel, fightDTO.getTournament()))));
        return fightDTO;
    }

    @Override
    public Fight reverse(FightDTO to) {
        if (to == null) {
            return null;
        }
        final Fight fight = new Fight();
        BeanUtils.copyProperties(to, fight, ConverterUtils.getNullPropertyNames(to));
        fight.setTeam1(teamConverter.reverse(to.getTeam1()));
        fight.setTeam2(teamConverter.reverse(to.getTeam2()));
        fight.setTournament(tournamentConverter.reverse(to.getTournament()));
        fight.setDuels(new ArrayList<>());
        to.getDuels().forEach(duelDTO -> fight.getDuels().add(duelConverter.reverse(duelDTO)));
        return fight;
    }
}
