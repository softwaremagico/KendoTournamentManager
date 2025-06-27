package com.softwaremagico.kt.core.converters;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2025 Softwaremagico
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

import com.softwaremagico.kt.core.controller.models.DuelDTO;
import com.softwaremagico.kt.core.converters.models.DuelConverterRequest;
import com.softwaremagico.kt.core.converters.models.ParticipantConverterRequest;
import com.softwaremagico.kt.core.converters.models.TournamentConverterRequest;
import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.repositories.TournamentRepository;
import org.hibernate.LazyInitializationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DuelConverter extends ElementConverter<Duel, DuelDTO, DuelConverterRequest> {
    private final ParticipantReducedConverter participantReducedConverter;
    private final ParticipantConverter participantConverter;

    private final TournamentConverter tournamentConverter;

    private final TournamentRepository tournamentRepository;

    @Autowired
    public DuelConverter(ParticipantReducedConverter participantReducedConverter, ParticipantConverter participantConverter,
                         TournamentConverter tournamentConverter, TournamentRepository tournamentRepository) {
        this.participantReducedConverter = participantReducedConverter;
        this.participantConverter = participantConverter;
        this.tournamentConverter = tournamentConverter;
        this.tournamentRepository = tournamentRepository;
    }


    @Override
    protected DuelDTO convertElement(DuelConverterRequest from) {
        final DuelDTO duelDTO = new DuelDTO();
        BeanUtils.copyProperties(from.getEntity(), duelDTO, ConverterUtils.getNullPropertyNames(from.getEntity()));
        duelDTO.setCompetitor1(participantReducedConverter.convert(
                new ParticipantConverterRequest(from.getEntity().getCompetitor1())));
        duelDTO.setCompetitor2(participantReducedConverter.convert(
                new ParticipantConverterRequest(from.getEntity().getCompetitor2())));
        try {
            if (from.getTournamentDTO() != null) {
                duelDTO.setTournament(from.getTournamentDTO());
            } else if (from.getTournament() != null) {
                //Converter can have the tournament defined already.
                duelDTO.setTournament(tournamentConverter.convert(
                        new TournamentConverterRequest(from.getTournament())));
            } else {
                duelDTO.setTournament(tournamentConverter.convert(
                        new TournamentConverterRequest(from.getEntity().getTournament())));
            }
        } catch (LazyInitializationException | FatalBeanException e) {
            duelDTO.setTournament(tournamentConverter.convert(
                    new TournamentConverterRequest(tournamentRepository.findById(from.getEntity().getTournament().getId()).orElse(null))));
        }
        return duelDTO;
    }

    @Override
    public Duel reverse(DuelDTO to) {
        if (to == null) {
            return null;
        }
        final Duel duel = new Duel();
        BeanUtils.copyProperties(to, duel, ConverterUtils.getNullPropertyNames(to));
        duel.setCompetitor1(participantConverter.reverse(to.getCompetitor1()));
        duel.setCompetitor2(participantConverter.reverse(to.getCompetitor2()));
        duel.setTournament(tournamentConverter.reverse(to.getTournament()));
        return duel;
    }
}
