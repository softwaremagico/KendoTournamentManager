package com.softwaremagico.kt.rest.services;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
 * %%
 * Copyright (C) 2021 - 2022 Softwaremagico
 * %%
 * This software is designed by Jorge Hortelano Otero. Jorge Hortelano Otero
 * <softwaremagico@gmail.com> Valencia (Spain).
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.softwaremagico.kt.core.controller.FightController;
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.logger.RestServerLogger;
import com.softwaremagico.kt.pdf.EmptyPdfBodyException;
import com.softwaremagico.kt.pdf.InvalidXmlElementException;
import com.softwaremagico.kt.pdf.controller.PdfController;
import com.softwaremagico.kt.rest.exceptions.BadRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/fights")
public class FightServices {
    private final FightController fightController;

    private final PdfController pdfController;

    private final TournamentController tournamentController;

    public FightServices(FightController fightProvider, PdfController pdfController, TournamentController tournamentController) {
        this.fightController = fightProvider;
        this.pdfController = pdfController;
        this.tournamentController = tournamentController;
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Gets all fights.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<FightDTO> getAll(HttpServletRequest request) {
        return fightController.get();
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Gets all fights on tournament.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/tournaments/{tournamentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FightDTO> getAll(@Parameter(description = "Id of an existing tournament", required = true) @PathVariable("tournamentId") Integer tournamentId,
                                 HttpServletRequest request) {
        return fightController.getByTournamentId(tournamentId);
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Gets all fights.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/tournaments", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FightDTO> getAll(@RequestBody TournamentDTO tournamentDto,
                                 HttpServletRequest request) {
        return fightController.get(tournamentDto);
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Gets all fights summary in a pdf file.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/tournaments/{tournamentId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public byte[] getTeamsScoreRankingFromTournamentAsPdf(@Parameter(description = "Id of an existing tournament", required = true)
                                                          @PathVariable("tournamentId") Integer tournamentId,
                                                          Locale locale, HttpServletResponse response, HttpServletRequest request) {
        final TournamentDTO tournamentDTO = tournamentController.get(tournamentId);
        try {
            final ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                    .filename(tournamentDTO.getName() + " - teams score.pdf").build();
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
            return pdfController.generateFightsSummaryList(locale, tournamentDTO).generate();
        } catch (InvalidXmlElementException | EmptyPdfBodyException e) {
            RestServerLogger.errorMessage(this.getClass(), e);
            throw new BadRequestException(this.getClass(), e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Gets a fight.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public FightDTO get(@Parameter(description = "Id of an existing fight", required = true) @PathVariable("id") Integer id,
                        HttpServletRequest request) {
        return fightController.get(id);
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Gets current fight.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/tournaments/{tournamentId}/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public FightDTO getCurrent(@Parameter(description = "Id of an existing tournament", required = true) @PathVariable("tournamentId") Integer tournamentId,
                               HttpServletRequest request) {
        return fightController.getCurrent(tournamentId);
    }

    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Creates a fight.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public FightDTO add(@RequestBody FightDTO newFightDTO, Authentication authentication, HttpServletRequest request) {
        if (newFightDTO == null) {
            throw new BadRequestException(getClass(), "Fight data is missing");
        }
        return fightController.create(newFightDTO, authentication.getName());
    }

    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Creates a set of fights.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FightDTO> add(@RequestBody Collection<FightDTO> fightDTOs, Authentication authentication, HttpServletRequest request) {
        if (fightDTOs == null || fightDTOs.isEmpty()) {
            throw new BadRequestException(getClass(), "Fight data is missing");
        }
        return fightController.create(fightDTOs, authentication.getName());
    }

    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Deletes a fight.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@Parameter(description = "Id of an existing fight", required = true) @PathVariable("id") Integer id,
                       HttpServletRequest request) {
        fightController.deleteById(id);
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Deletes a fight.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody FightDTO fightDto, HttpServletRequest request) {
        fightController.delete(fightDto);
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Deletes a collection of fights.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/delete/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody Collection<FightDTO> fightDtos, HttpServletRequest request) {
        fightController.delete(fightDtos);
    }

    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Deletes all fights from a tournament.", security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/delete/tournaments", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody TournamentDTO tournamentDto, HttpServletRequest request) {
        fightController.delete(tournamentDto);
    }

    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Updates a fight.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public FightDTO update(@RequestBody FightDTO fightDto, Authentication authentication, HttpServletRequest request) {
        if (fightDto == null) {
            throw new BadRequestException(getClass(), "Fight data is missing");
        }
        return fightController.update(fightDto, authentication.getName());
    }

    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Generate duels on a fight.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value = "/duels", produces = MediaType.APPLICATION_JSON_VALUE)
    public FightDTO generateDuels(@RequestBody FightDTO fightDto, Authentication authentication, HttpServletRequest request) {
        if (fightDto == null) {
            throw new BadRequestException(getClass(), "Fight data is missing");
        }
        return fightController.generateDuels(fightDto, authentication.getName());
    }

    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Updates a list of fights.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FightDTO> update(@RequestBody List<FightDTO> fightDTOs, Authentication authentication, HttpServletRequest request) {
        if (fightDTOs == null) {
            throw new BadRequestException(getClass(), "Fight data is missing");
        }
        return fightController.updateAll(fightDTOs, authentication.getName());
    }

    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Updates a fight.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value = "/create/tournaments/{tournamentId}/levels/{levelId}/maximize/{maximizeFights}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FightDTO> create(@Parameter(description = "Id of an existing tournament", required = true) @PathVariable("tournamentId") Integer tournamentId,
                                 @Parameter(description = "Create as much fights as possible", required = true) @PathVariable("maximizeFights") boolean maximizeFights,
                                 @Parameter(description = "Level of the tournament", required = true) @PathVariable("levelId") Integer levelId,
                                 Authentication authentication, HttpServletRequest request) {
        return fightController.createFights(tournamentId, TeamsOrder.NONE, maximizeFights, levelId, authentication.getName());
    }


}
