package com.softwaremagico.kt.rest.services;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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

import com.softwaremagico.kt.core.controller.AchievementController;
import com.softwaremagico.kt.core.controller.models.AchievementDTO;
import com.softwaremagico.kt.core.converters.AchievementConverter;
import com.softwaremagico.kt.core.converters.models.AchievementConverterRequest;
import com.softwaremagico.kt.core.providers.AchievementProvider;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.persistence.entities.Achievement;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.repositories.AchievementRepository;
import com.softwaremagico.kt.persistence.values.AchievementGrade;
import com.softwaremagico.kt.persistence.values.AchievementType;
import com.softwaremagico.kt.rest.exceptions.InvalidRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/achievements")
public class AchievementServices extends BasicServices<Achievement, AchievementDTO, AchievementRepository, AchievementProvider,
        AchievementConverterRequest, AchievementConverter, AchievementController> {

    private final ParticipantProvider participantProvider;

    public AchievementServices(AchievementController achievementController, ParticipantProvider participantProvider) {
        super(achievementController);
        this.participantProvider = participantProvider;
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN', 'ROLE_PARTICIPANT')")
    @Operation(summary = "Gets participant's achievement.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/participants/{participantId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AchievementDTO> getParticipantAchievements(@Parameter(description = "Id of an existing participant", required = true)
                                                           @PathVariable("participantId") Integer participantId,
                                                           Authentication authentication,
                                                           HttpServletRequest request) {
        //If is a participant guest, only its own statistics can see.
        if (authentication != null) {
            final Optional<Participant> participant = participantProvider.findByTokenUsername(authentication.getName());
            if (participant.isPresent()) {
                if (!Objects.equals(participant.get().getId(), participantId)) {
                    throw new InvalidRequestException(this.getClass(), "User '" + authentication.getName()
                            + "' is trying to access to statistics from user '" + participantId + "'.");
                }
            }
        }
        return getController().getParticipantAchievements(participantId);
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Gets tournament's achievement.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/tournaments/{tournamentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AchievementDTO> getTournamentAchievements(@Parameter(description = "Id of an existing tournament", required = true)
                                                          @PathVariable("tournamentId") Integer tournamentId,
                                                          HttpServletRequest request) {
        return getController().getTournamentAchievements(tournamentId);
    }

    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Regenerates tournament's achievement.", security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping(value = "/tournaments/{tournamentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AchievementDTO> regenerateTournamentAchievements(@Parameter(description = "Id of an existing tournament", required = true)
                                                                 @PathVariable("tournamentId") Integer tournamentId,
                                                                 HttpServletRequest request) {
        return getController().regenerateAchievements(tournamentId);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Operation(summary = "Regenerates all tournament's achievement.", security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping(value = "/tournaments/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AchievementDTO> regenerateAllTournamentAchievements(HttpServletRequest request) {
        return getController().regenerateAllAchievements();
    }


    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Gets total achievements by type.", description = "Not includes duplicates",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/count/types", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<AchievementType, Map<AchievementGrade, Integer>> countByType(HttpServletRequest request) {
        return getController().getAchievementsCount();
    }


    @PreAuthorize("hasAnyRole('ROLE_GUEST', 'ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Gets total achievements by type.", description = "Not includes duplicates",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/count/types/{achievementType}", produces = MediaType.APPLICATION_JSON_VALUE)
    public long count(@Parameter(description = "Type to count", required = true)
                      @PathVariable("achievementType") AchievementType achievementType,
                      HttpServletRequest request) {
        return getController().countAchievements(achievementType);
    }
}
