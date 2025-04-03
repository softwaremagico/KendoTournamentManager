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

import com.softwaremagico.kt.core.controller.FightStatisticsController;
import com.softwaremagico.kt.core.controller.ParticipantController;
import com.softwaremagico.kt.core.controller.ParticipantStatisticsController;
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.TournamentStatisticsController;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantStatisticsDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.controller.models.TournamentFightStatisticsDTO;
import com.softwaremagico.kt.core.controller.models.TournamentStatisticsDTO;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.logger.KendoTournamentLogger;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.rest.exceptions.InvalidRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/statistics")
public class StatisticsServices {

    private final TournamentController tournamentController;
    private final FightStatisticsController fightStatisticsController;
    private final TournamentStatisticsController tournamentStatisticsController;
    private final ParticipantStatisticsController participantStatisticsController;
    private final ParticipantController participantController;
    private final ParticipantProvider participantProvider;

    public StatisticsServices(TournamentController tournamentController, FightStatisticsController fightStatisticsController,
                              TournamentStatisticsController tournamentStatisticsController,
                              ParticipantStatisticsController participantStatisticsController, ParticipantController participantController,
                              ParticipantProvider participantProvider) {
        this.tournamentController = tournamentController;
        this.fightStatisticsController = fightStatisticsController;
        this.tournamentStatisticsController = tournamentStatisticsController;
        this.participantStatisticsController = participantStatisticsController;
        this.participantController = participantController;
        this.participantProvider = participantProvider;
    }

    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Gets fight statistics.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/tournaments/{tournamentId}/fights", produces = MediaType.APPLICATION_JSON_VALUE)
    public TournamentFightStatisticsDTO getStatisticsFromTournament(@Parameter(description = "Id of an existing tournament", required = true)
                                                                    @PathVariable("tournamentId") Integer tournamentId,
                                                                    @RequestParam(name = "calculateByTeams") Optional<Boolean> checkByTeams,
                                                                    @RequestParam(name = "calculateByMembers") Optional<Boolean> checkByMembers,
                                                                    HttpServletRequest request) {
        if (checkByMembers.isPresent() && Boolean.TRUE.equals(checkByMembers.get())) {
            KendoTournamentLogger.debug(this.getClass(), "Forcing statistics by members.");
            return fightStatisticsController.estimateByMembers(tournamentController.get(tournamentId));
        }
        if (checkByTeams.isPresent() && Boolean.TRUE.equals(checkByMembers.get())) {
            KendoTournamentLogger.debug(this.getClass(), "Forcing statistics by teams.");
            return fightStatisticsController.estimateByTeams(tournamentController.get(tournamentId));
        }
        return fightStatisticsController.estimate(tournamentController.get(tournamentId));
    }

    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Gets tournament statistics.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/tournaments/{tournamentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TournamentStatisticsDTO getStatisticsFromTournament(@Parameter(description = "Id of an existing tournament", required = true)
                                                               @PathVariable("tournamentId") Integer tournamentId,
                                                               Authentication authentication,
                                                               HttpServletRequest request) {
        final TournamentStatisticsDTO tournamentStatisticsDTO = tournamentStatisticsController.get(tournamentController.get(tournamentId));
        tournamentStatisticsDTO.setCreatedBy(authentication.getName());
        tournamentStatisticsDTO.setCreatedAt(LocalDateTime.now());
        return tournamentStatisticsDTO;
    }

    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege,"
            + " @securityService.participantPrivilege)")
    @Operation(summary = "Gets previous tournament statistics.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/tournaments/{tournamentId}/previous/{number}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TournamentStatisticsDTO> getStatisticsFromPreviousTournament(@Parameter(description = "Id of an existing tournament", required = true)
                                                                             @PathVariable("tournamentId") Integer tournamentId,
                                                                             @Parameter(description = "Number of tournaments statistics to retrieve")
                                                                             @PathVariable("number") Integer number,
                                                                             Authentication authentication,
                                                                             HttpServletRequest request) {
        if (number == null) {
            number = 1;
        }
        final List<TournamentStatisticsDTO> statisticsDTOS = new ArrayList<>();
        final List<TournamentDTO> tournamentsDTO = tournamentController.getPreviousTo(tournamentController.get(tournamentId), number);
        tournamentsDTO.forEach(tournamentDTO -> {
            final TournamentStatisticsDTO tournamentStatisticsDTO = tournamentStatisticsController.get(tournamentDTO);
            tournamentStatisticsDTO.setCreatedBy(authentication.getName());
            tournamentStatisticsDTO.setCreatedAt(LocalDateTime.now());
            statisticsDTOS.add(tournamentStatisticsDTO);
        });
        return statisticsDTOS;
    }

    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege,"
            + " @securityService.participantPrivilege)")
    @Operation(summary = "Gets participant statistics.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/participants/{participantId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ParticipantStatisticsDTO getStatisticsFromParticipant(@Parameter(description = "Id of an existing participant", required = true)
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
        final ParticipantStatisticsDTO participantStatisticsDTO = participantStatisticsController.get(participantController.get(participantId));
        participantStatisticsDTO.setCreatedBy(authentication != null ? authentication.getName() : null);
        participantStatisticsDTO.setCreatedAt(LocalDateTime.now());
        return participantStatisticsDTO;
    }


    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege, "
            + "@securityService.participantPrivilege)")
    @Operation(summary = "Gets participant worst nightmare.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/participants/your-worst-nightmare/{participantId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ParticipantDTO> getYourWorstNightmareFromParticipant(@Parameter(description = "Id of an existing participant", required = true)
                                                                     @PathVariable("participantId") Integer participantId,
                                                                     Authentication authentication,
                                                                     HttpServletRequest request) {
        //If is a participant guest, only its own statistics can see.
        if (authentication != null) {
            final Optional<Participant> participant = participantProvider.findByTokenUsername(authentication.getName());
            if (participant.isPresent() && !Objects.equals(participant.get().getId(), participantId)) {
                throw new InvalidRequestException(this.getClass(), "User '" + authentication.getName()
                        + "' is trying to access to statistics from user '" + participantId + "'.");
            }
        }
        return participantController.getYourWorstNightmare(participantController.get(participantId));
    }


    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege,"
            + " @securityService.participantPrivilege)")
    @Operation(summary = "Gets participant worst nightmare.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/participants/worst-nightmare-of/{participantId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ParticipantDTO> getWorstNightmareOf(@Parameter(description = "Id of an existing participant", required = true)
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
        return participantController.getYouAreTheWorstNightmareOf(participantController.get(participantId));
    }
}
