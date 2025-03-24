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

import com.softwaremagico.kt.core.controller.FightController;
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.FightConverter;
import com.softwaremagico.kt.core.converters.models.FightConverterRequest;
import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.core.providers.FightProvider;
import com.softwaremagico.kt.logger.RestServerLogger;
import com.softwaremagico.kt.pdf.EmptyPdfBodyException;
import com.softwaremagico.kt.pdf.InvalidXmlElementException;
import com.softwaremagico.kt.pdf.controller.PdfController;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.repositories.FightRepository;
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
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/fights")
public class FightServices extends BasicServices<Fight, FightDTO, FightRepository,
        FightProvider, FightConverterRequest, FightConverter, FightController> {

    private final PdfController pdfController;

    private final TournamentController tournamentController;

    public FightServices(FightController fightController, PdfController pdfController, TournamentController tournamentController) {
        super(fightController);
        this.pdfController = pdfController;
        this.tournamentController = tournamentController;
    }


    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Gets all fights from a tournament.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/tournaments/{tournamentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FightDTO> getAll(@Parameter(description = "Id of an existing tournament", required = true) @PathVariable("tournamentId") Integer tournamentId,
                                 HttpServletRequest request) {
        return getController().getByTournamentId(tournamentId);
    }


    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Gets all fights.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/tournaments", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FightDTO> getAll(@RequestBody TournamentDTO tournamentDTO,
                                 HttpServletRequest request) {
        return getController().get(tournamentDTO);
    }


    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Gets all fights summary in a pdf file.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/tournaments/{tournamentId}/pdf", produces = {MediaType.APPLICATION_PDF_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public byte[] getTeamsScoreRankingFromTournamentAsPdf(@Parameter(description = "Id of an existing tournament", required = true)
                                                          @PathVariable("tournamentId") Integer tournamentId,
                                                          Locale locale, HttpServletResponse response, HttpServletRequest request) {
        final TournamentDTO tournamentDTO = tournamentController.get(tournamentId);
        try {
            final byte[] bytes = pdfController.generateFightsSummaryList(locale, tournamentDTO).generate();
            final ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                    .filename(tournamentDTO.getName() + " - teams score.pdf").build();
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
            return bytes;
        } catch (InvalidXmlElementException | EmptyPdfBodyException e) {
            RestServerLogger.errorMessage(this.getClass(), e);
            throw new BadRequestException(this.getClass(), e.getMessage());
        }
    }


    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Gets current fight.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/tournaments/{tournamentId}/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public FightDTO getCurrent(@Parameter(description = "Id of an existing tournament", required = true) @PathVariable("tournamentId") Integer tournamentId,
                               HttpServletRequest request) {
        return getController().getCurrent(tournamentId);
    }


    @Operation(summary = "Deletes a fight.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@Parameter(description = "Id of an existing fight", required = true) @PathVariable("id") Integer id,
                       Authentication authentication, HttpServletRequest request) {
        getController().deleteById(id, authentication.getName());
    }


    @PreAuthorize("hasAnyAuthority(@securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Deletes all fights from a tournament.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/delete/tournaments", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody TournamentDTO tournamentDTO, HttpServletRequest request) {
        getController().delete(tournamentDTO);
    }


    @PreAuthorize("hasAnyAuthority(@securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Generate duels on a fight.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value = "/duels", produces = MediaType.APPLICATION_JSON_VALUE)
    public FightDTO generateDuels(@RequestBody FightDTO fightDto, Authentication authentication, HttpServletRequest request) {
        if (fightDto == null) {
            throw new BadRequestException(getClass(), "Fight data is missing");
        }
        fightDto = getController().update(fightDto, authentication.getName());
        if (!fightDto.getDuels().isEmpty()) {
            return fightDto;
        }
        return getController().generateDuels(fightDto, authentication.getName());
    }


    @PreAuthorize("hasAnyAuthority(@securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Updates a fight.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value = "/create/tournaments/{tournamentId}/levels/{levelId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FightDTO> create(@Parameter(description = "Id of an existing tournament", required = true) @PathVariable("tournamentId") Integer tournamentId,
                                 @Parameter(description = "Level of the tournament", required = true) @PathVariable("levelId") Integer levelId,
                                 Authentication authentication, HttpServletRequest request) {
        return getController().createFights(tournamentId, TeamsOrder.NONE, levelId, authentication.getName());
    }


    @PreAuthorize("hasAnyAuthority(@securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Creates next fights. Returns 204 if are not created due to a draw result.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping(value = "/create/tournaments/{tournamentId}/next", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FightDTO> createNext(@Parameter(description = "Id of an existing tournament", required = true) @PathVariable("tournamentId")
                                     Integer tournamentId,
                                     Authentication authentication, HttpServletRequest request) {
        return getController().createNextFights(tournamentId, authentication.getName());
    }


    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege,"
            + " @securityService.participantPrivilege)")
    @Operation(summary = "Gets all fights from competitor.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/competitor/{competitorId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FightDTO> getByCompetitor(@Parameter(description = "Id of the competitor.", required = true)
                                          @PathVariable("competitorId")
                                          Integer competitorId,
                                          HttpServletRequest request) {
        return getController().getBy(competitorId);
    }


}
