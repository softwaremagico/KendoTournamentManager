package com.softwaremagico.kt.core.converters;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Tournament;
import org.hibernate.LazyInitializationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class FightConverter extends ElementConverter<Fight, FightDTO, FightConverterRequest> {
    private final TeamConverter teamConverter;
    private final TournamentConverter tournamentConverter;
    private final TournamentProvider tournamentProvider;
    private final DuelConverter duelConverter;

    @Autowired
    public FightConverter(TeamConverter teamConverter, TournamentConverter tournamentConverter, TournamentProvider tournamentProvider,
                          DuelConverter duelConverter) {
        this.teamConverter = teamConverter;
        this.tournamentConverter = tournamentConverter;
        this.tournamentProvider = tournamentProvider;
        this.duelConverter = duelConverter;
    }


    @Override
    protected FightDTO convertElement(FightConverterRequest from) {
        final FightDTO fightDTO = new FightDTO();
        BeanUtils.copyProperties(from.getEntity(), fightDTO, ConverterUtils.getNullPropertyNames(from.getEntity()));

        //Getting the tournamnet to send to duels and avoid hundreds of calls.
        Tournament tournament;
        try {
            tournament = from.getEntity().getTournament();
            fightDTO.setTournament(tournamentConverter.convert(new TournamentConverterRequest(tournament)));
        } catch (LazyInitializationException | InvalidPropertyException e) {
            tournament = tournamentProvider.get(from.getEntity().getTournament().getId()).orElse(null);
            fightDTO.setTournament(tournamentConverter.convert(new TournamentConverterRequest(tournament)));
        }

        fightDTO.setTeam1(teamConverter.convert(
                new TeamConverterRequest(from.getEntity().getTeam1(), tournament)));
        fightDTO.setTeam2(teamConverter.convert(
                new TeamConverterRequest(from.getEntity().getTeam2(), tournament)));

        fightDTO.setDuels(new ArrayList<>());
        final Tournament finalTournament = tournament;
        from.getEntity().getDuels().forEach(duel -> fightDTO.getDuels().add(duelConverter.convert(new DuelConverterRequest(duel, finalTournament))));
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
