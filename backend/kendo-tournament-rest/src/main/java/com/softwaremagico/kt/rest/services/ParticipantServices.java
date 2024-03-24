package com.softwaremagico.kt.rest.services;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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

import com.softwaremagico.kt.core.controller.ParticipantController;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.TemporalToken;
import com.softwaremagico.kt.core.controller.models.Token;
import com.softwaremagico.kt.core.converters.ParticipantConverter;
import com.softwaremagico.kt.core.converters.models.ParticipantConverterRequest;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.repositories.ParticipantRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@RestController
@RequestMapping("/participants")
public class ParticipantServices extends BasicServices<Participant, ParticipantDTO, ParticipantRepository,
        ParticipantProvider, ParticipantConverterRequest, ParticipantConverter, ParticipantController> {


    public ParticipantServices(ParticipantController participantController) {
        super(participantController);
    }


    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Creates a temporal token for a participant.")
    @PostMapping(value = "/temporal-token", produces = MediaType.APPLICATION_JSON_VALUE)
    public TemporalToken getTemporalToken(@RequestBody ParticipantDTO participantDTO,
                                          HttpServletRequest request) {
        return getController().generateTemporalToken(participantDTO);
    }


    @Operation(summary = "Creates a jwt token for a participant.")
    @GetMapping(value = "/public/token/{temporalToken}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ParticipantDTO> getToken(@Parameter(description = "Temporal Token that will be converted to a JWT token.", required = true)
                                                   @PathVariable("temporalToken") String temporalToken,
                                                   HttpServletRequest request) {
        final Token token = getController().generateToken(temporalToken);

        final ZonedDateTime zdt = token.getExpiration().atZone(ZoneId.systemDefault());
        final long milliseconds = zdt.toInstant().toEpochMilli();

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, token.getToken())
                .header(HttpHeaders.EXPIRES, String.valueOf(milliseconds))
                .body(token.getParticipant());
    }
}
