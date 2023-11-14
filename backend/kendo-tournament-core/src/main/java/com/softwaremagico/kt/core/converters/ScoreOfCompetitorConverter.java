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

import com.softwaremagico.kt.core.controller.models.ScoreOfCompetitorDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.models.DuelConverterRequest;
import com.softwaremagico.kt.core.converters.models.FightConverterRequest;
import com.softwaremagico.kt.core.converters.models.ParticipantConverterRequest;
import com.softwaremagico.kt.core.converters.models.ScoreOfCompetitorConverterRequest;
import com.softwaremagico.kt.core.converters.models.TournamentConverterRequest;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.core.score.ScoreOfCompetitor;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Tournament;
import org.hibernate.LazyInitializationException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ScoreOfCompetitorConverter extends ElementConverter<ScoreOfCompetitor, ScoreOfCompetitorDTO, ScoreOfCompetitorConverterRequest> {

    private final ParticipantConverter participantConverter;

    private final FightConverter fightConverter;

    private final DuelConverter duelConverter;

    private final TournamentConverter tournamentConverter;

    private final TournamentProvider tournamentProvider;

    public ScoreOfCompetitorConverter(ParticipantConverter participantConverter, FightConverter fightConverter,
                                      DuelConverter duelConverter, TournamentConverter tournamentConverter,
                                      TournamentProvider tournamentProvider) {
        this.participantConverter = participantConverter;
        this.fightConverter = fightConverter;
        this.duelConverter = duelConverter;
        this.tournamentConverter = tournamentConverter;
        this.tournamentProvider = tournamentProvider;
    }

    @Override
    protected ScoreOfCompetitorDTO convertElement(ScoreOfCompetitorConverterRequest from) {
        final ScoreOfCompetitorDTO scoreOfCompetitorDTO = new ScoreOfCompetitorDTO();
        BeanUtils.copyProperties(from.getEntity(), scoreOfCompetitorDTO, ConverterUtils.getNullPropertyNames(from.getEntity()));

        Set<Tournament> tournaments;
        try {
            if (from.getTournaments() != null) {
                tournaments = new HashSet<>(from.getTournaments());
            } else {
                tournaments = from.getEntity().getFights().stream().map(Fight::getTournament).collect(Collectors.toSet());
            }
        } catch (LazyInitializationException e) {
            tournaments = new HashSet<>(tournamentProvider.findByIdIn(from.getEntity().getFights().stream()
                    .map(fight -> fight.getTournament().getId()).collect(Collectors.toSet())));
        }

        final Map<Integer, TournamentDTO> tournamentDTOs = tournamentConverter.convertAll(tournaments.stream()
                        .map(TournamentConverterRequest::new).collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(TournamentDTO::getId, Function.identity()));

        scoreOfCompetitorDTO.setCompetitor(participantConverter.convert(new ParticipantConverterRequest(from.getEntity().getCompetitor())));
        scoreOfCompetitorDTO.setFights(fightConverter.convertAll(from.getEntity().getFights().stream()
                .map(fight -> new FightConverterRequest(fight, tournamentDTOs.get(fight.getTournament().getId()))).toList()));
        scoreOfCompetitorDTO.setUnties(duelConverter.convertAll(from.getEntity().getUnties().stream()
                .map(duel -> new DuelConverterRequest(duel, tournamentDTOs.get(duel.getTournament().getId()))).toList()));
        return scoreOfCompetitorDTO;
    }

    @Override
    public ScoreOfCompetitor reverse(ScoreOfCompetitorDTO to) {
        if (to == null) {
            return null;
        }
        final ScoreOfCompetitor scoreOfCompetitor = new ScoreOfCompetitor();
        BeanUtils.copyProperties(to, scoreOfCompetitor, ConverterUtils.getNullPropertyNames(to));
        scoreOfCompetitor.setCompetitor(participantConverter.reverse(to.getCompetitor()));
        scoreOfCompetitor.setFights(new ArrayList<>());
        to.getFights().forEach(fight -> scoreOfCompetitor.getFights().add(fightConverter.reverse(fight)));
        scoreOfCompetitor.setUnties(new ArrayList<>());
        to.getUnties().forEach(duel -> scoreOfCompetitor.getUnties().add(duelConverter.reverse(duel)));
        return scoreOfCompetitor;
    }
}
