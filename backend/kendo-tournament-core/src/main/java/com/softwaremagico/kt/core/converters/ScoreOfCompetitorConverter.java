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

import com.softwaremagico.kt.core.controller.models.ScoreOfCompetitorDTO;
import com.softwaremagico.kt.core.converters.models.ParticipantConverterRequest;
import com.softwaremagico.kt.core.converters.models.ScoreOfCompetitorConverterRequest;
import com.softwaremagico.kt.core.score.ScoreOfCompetitor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class ScoreOfCompetitorConverter extends SimpleConverter<ScoreOfCompetitor, ScoreOfCompetitorDTO, ScoreOfCompetitorConverterRequest> {

    private final ParticipantConverter participantConverter;
    private final ParticipantReducedConverter participantReducedConverter;

    public ScoreOfCompetitorConverter(ParticipantConverter participantConverter, ParticipantReducedConverter participantReducedConverter) {
        this.participantConverter = participantConverter;
        this.participantReducedConverter = participantReducedConverter;
    }

    @Override
    protected ScoreOfCompetitorDTO convertElement(ScoreOfCompetitorConverterRequest from) {
        final ScoreOfCompetitorDTO scoreOfCompetitorDTO = new ScoreOfCompetitorDTO();
        BeanUtils.copyProperties(from.getEntity(), scoreOfCompetitorDTO, ConverterUtils.getNullPropertyNames(from.getEntity()));
        scoreOfCompetitorDTO.setCompetitor(participantReducedConverter.convert(new ParticipantConverterRequest(from.getEntity().getCompetitor())));
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
        scoreOfCompetitor.setUnties(new ArrayList<>());
        return scoreOfCompetitor;
    }
}
