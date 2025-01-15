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

import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.models.TournamentConverterRequest;
import com.softwaremagico.kt.core.converters.models.TournamentScoreConverterRequest;
import com.softwaremagico.kt.core.exceptions.UnexpectedValueException;
import com.softwaremagico.kt.persistence.entities.Tournament;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TournamentConverter extends ElementConverter<Tournament, TournamentDTO, TournamentConverterRequest> {
    private final TournamentScoreConverter tournamentScoreConverter;

    @Autowired
    public TournamentConverter(TournamentScoreConverter tournamentScoreConverter) {
        this.tournamentScoreConverter = tournamentScoreConverter;
    }


    @Override
    protected TournamentDTO convertElement(TournamentConverterRequest from) {
        final TournamentDTO tournamentDTO = new TournamentDTO();
        BeanUtils.copyProperties(from.getEntity(), tournamentDTO, ConverterUtils.getNullPropertyNames(from.getEntity()));
        try {
            if (from.getEntity().getTournamentScore() != null) {
                tournamentDTO.setTournamentScore(tournamentScoreConverter.convert(
                        new TournamentScoreConverterRequest(from.getEntity().getTournamentScore())));
            }
        } catch (UnexpectedValueException e) {
            tournamentDTO.setTournamentScore(null);
        }
        return tournamentDTO;
    }

    @Override
    public Tournament reverse(TournamentDTO to) {
        if (to == null) {
            return null;
        }
        final Tournament tournament = new Tournament();
        BeanUtils.copyProperties(to, tournament, ConverterUtils.getNullPropertyNames(to));
        tournament.setTournamentScore(tournamentScoreConverter.reverse(to.getTournamentScore()));
        return tournament;
    }
}
