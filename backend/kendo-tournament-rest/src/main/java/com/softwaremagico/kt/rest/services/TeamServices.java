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

import com.softwaremagico.kt.core.controller.TeamController;
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantInTournamentDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantsInTournamentDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.TeamConverter;
import com.softwaremagico.kt.core.converters.models.TeamConverterRequest;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.logger.RestServerLogger;
import com.softwaremagico.kt.pdf.EmptyPdfBodyException;
import com.softwaremagico.kt.pdf.InvalidXmlElementException;
import com.softwaremagico.kt.pdf.controller.PdfController;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.repositories.TeamRepository;
import com.softwaremagico.kt.rest.exceptions.BadRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/teams")
public class TeamServices extends BasicServices<Team, TeamDTO, TeamRepository,
        TeamProvider, TeamConverterRequest, TeamConverter, TeamController> {

    private final TournamentController tournamentController;

    private final PdfController pdfController;

    public TeamServices(TeamController teamController, TournamentController tournamentController, PdfController pdfController) {
        super(teamController);
        this.tournamentController = tournamentController;
        this.pdfController = pdfController;
    }


    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN', 'ROLE_GUEST')")
    @Operation(summary = "Gets all teams from a tournament.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/tournaments/{tournamentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TeamDTO> getAll(@Parameter(description = "Id of an existing tournament", required = true) @PathVariable("tournamentId") Integer tournamentId,
                                Authentication authentication, HttpServletRequest request) {
        return getController().getAllByTournament(tournamentId, authentication.getName());
    }


    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN', 'ROLE_GUEST')")
    @Operation(summary = "Gets teams not disqualified from a tournament. Only for Senbatsu mode", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/remaining/tournaments/{tournamentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TeamDTO> getAllRemaining(@Parameter(description = "Id of an existing tournament", required = true)
                                         @PathVariable("tournamentId") Integer tournamentId,
                                         Authentication authentication, HttpServletRequest request) {
        return getController().getAllRemainingByTournament(tournamentId, authentication.getName());
    }


    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Counts all teams from a tournament.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/tournaments/{tournamentId}/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public long countByTournamentId(@Parameter(description = "Id of an existing tournament", required = true)
                                    @PathVariable("tournamentId") Integer tournamentId,
                                    Authentication authentication, HttpServletRequest request) {
        return getController().countByTournament(tournamentId);
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Gets all teams.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/tournaments", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TeamDTO> getAll(@RequestBody TournamentDTO tournamentDto,
                                Authentication authentication, HttpServletRequest request) {
        return getController().getAllByTournament(tournamentDto, authentication.getName());
    }


    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Generates default teams.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value = "/tournaments", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TeamDTO> create(@RequestBody TournamentDTO tournamentDto,
                                Authentication authentication, HttpServletRequest request) {
        return getController().create(tournamentDto, authentication.getName());
    }

    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Deletes all teams from a tournament.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/tournaments/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody TournamentDTO tournamentDto, HttpServletRequest request) {
        getController().delete(tournamentDto);
    }

    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Defines the teams.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/set", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TeamDTO> set(@RequestBody List<TeamDTO> teamsDTOs, Authentication authentication, HttpServletRequest request) {
        if (teamsDTOs == null || teamsDTOs.isEmpty()) {
            throw new BadRequestException(getClass(), "Team data is missing");
        }
        teamsDTOs.forEach(teamDTO -> {
            if (teamDTO.getTournament() == null) {
                throw new BadRequestException(getClass(), "Team data is missing");
            }
        });
        getController().delete(teamsDTOs.get(0).getTournament());
        return getController().create(teamsDTOs, authentication.getName());
    }

    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Deletes a member from any team.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/delete/members", produces = MediaType.APPLICATION_JSON_VALUE)
    public TeamDTO delete(@RequestBody ParticipantInTournamentDTO participantInTournament, HttpServletRequest request) {
        return getController().delete(participantInTournament.getTournament(), participantInTournament.getParticipant());
    }

    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Deletes multiples member from any team.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/delete/members/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody ParticipantsInTournamentDTO participantsInTournaments, HttpServletRequest request) {
        for (final ParticipantDTO participantInTournament : participantsInTournaments.getParticipant()) {
            getController().delete(participantsInTournaments.getTournament(), participantInTournament);
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Gets all teams from a tournament.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/tournaments/{tournamentId}/pdf", produces = {MediaType.APPLICATION_PDF_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public byte[] getAllFromTournamentAsPdf(@Parameter(description = "Id of an existing tournament", required = true) @PathVariable("tournamentId")
                                            Integer tournamentId,
                                            Locale locale, HttpServletResponse response, HttpServletRequest request) {
        final TournamentDTO tournament = tournamentController.get(tournamentId);
        try {
            final byte[] bytes = pdfController.generateTeamList(tournament).generate();
            final ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                    .filename(tournament.getName() + " - teams list.pdf").build();
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
            return bytes;
        } catch (InvalidXmlElementException | EmptyPdfBodyException e) {
            RestServerLogger.errorMessage(this.getClass(), e);
            throw new BadRequestException(this.getClass(), e.getMessage());
        }
    }
}
