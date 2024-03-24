package com.softwaremagico.kt.core.controller;

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

import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.TemporalToken;
import com.softwaremagico.kt.core.controller.models.Token;
import com.softwaremagico.kt.core.converters.ParticipantConverter;
import com.softwaremagico.kt.core.converters.models.ParticipantConverterRequest;
import com.softwaremagico.kt.core.exceptions.TokenExpiredException;
import com.softwaremagico.kt.core.exceptions.UserNotFoundException;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.repositories.ParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class ParticipantController extends BasicInsertableController<Participant, ParticipantDTO, ParticipantRepository,
        ParticipantProvider, ParticipantConverterRequest, ParticipantConverter> {


    @Autowired
    public ParticipantController(ParticipantProvider provider, ParticipantConverter converter) {
        super(provider, converter);
    }

    @Override
    protected ParticipantConverterRequest createConverterRequest(Participant participant) {
        return new ParticipantConverterRequest(participant);
    }

    public TemporalToken generateTemporalToken(ParticipantDTO participant) {
        return getProvider().generateTemporalToken(reverse(participant));
    }

    public Token generateToken(String temporalToken) {
        final Participant participant = getProvider().findByTemporalToken(temporalToken).orElseThrow(() ->
                new UserNotFoundException(this.getClass(), "No user found for the provided token!"));
        try {
            if (participant.getTemporalTokenExpiration().isBefore(LocalDateTime.now())) {
                throw new TokenExpiredException(this.getClass(), "Token has expired!");
            }
            final Token token = new Token(getProvider().generateToken(participant));
            token.setParticipant(convert(participant));
            return token;
        } finally {
            //Remove token to avoid reuse.
            participant.setTemporalToken(null);
            participant.setTemporalTokenExpiration(null);
            getProvider().save(participant);
        }
    }

}
