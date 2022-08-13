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

import com.softwaremagico.kt.core.controller.RankingController;
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.score.ScoreOfCompetitor;
import com.softwaremagico.kt.core.score.ScoreOfTeam;
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
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/rankings")
public class RankingServices {

    private final RankingController rankingController;

    private final TournamentController tournamentController;

    private final PdfController pdfController;

    public RankingServices(RankingController rankingController, PdfController pdfController, TournamentController tournamentController) {
        this.rankingController = rankingController;
        this.tournamentController = tournamentController;
        this.pdfController = pdfController;
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets participants' ranking.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/competitors/group/{groupId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ScoreOfCompetitor> getCompetitorsScoreRankingGroup(@Parameter(description = "Id of an existing group", required = true)
                                                                   @PathVariable("groupId") Integer groupId,
                                                                   HttpServletRequest request) {
        return rankingController.getCompetitorsScoreRankingFromGroup(groupId);
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets participants' ranking.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/competitors/tournament/{tournamentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ScoreOfCompetitor> getCompetitorsScoreRankingTournament(@Parameter(description = "Id of an existing tournament", required = true)
                                                                        @PathVariable("tournamentId") Integer tournamentId,
                                                                        HttpServletRequest request) {
        return rankingController.getCompetitorsScoreRankingFromTournament(tournamentId);
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets participants' ranking in a pdf file.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/competitors/tournament/{tournamentId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public byte[] getCompetitorsScoreRankingTournamentAsPdf(@Parameter(description = "Id of an existing tournament", required = true)
                                                            @PathVariable("tournamentId") Integer tournamentId,
                                                            Locale locale, HttpServletResponse response, HttpServletRequest request) {
        final TournamentDTO tournament = tournamentController.get(tournamentId);
        final List<ScoreOfCompetitor> scores = rankingController.getCompetitorsScoreRanking(tournament);
        try {
            final ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                    .filename(tournament.getName() + " - competitors score.pdf").build();
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
            return pdfController.generateCompetitorsScoreList(locale, tournament, scores).generate();
        } catch (InvalidXmlElementException | EmptyPdfBodyException e) {
            RestServerLogger.errorMessage(this.getClass(), e);
            throw new BadRequestException(this.getClass(), e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets teams' ranking.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/teams/group/{groupId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ScoreOfTeam> getTeamsScoreRankingFromGroup(@Parameter(description = "Id of an existing group", required = true)
                                                           @PathVariable("groupId") Integer groupId,
                                                           HttpServletRequest request) {
        return rankingController.getTeamsScoreRankingFromGroup(groupId);
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets teams' ranking.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/teams/tournament/{tournamentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ScoreOfTeam> getTeamsScoreRankingFromTournament(@Parameter(description = "Id of an existing tournament", required = true)
                                                                @PathVariable("tournamentId") Integer tournamentId,
                                                                HttpServletRequest request) {
        return rankingController.getTeamsScoreRankingFromTournament(tournamentId);
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets teams' ranking in a pdf file.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/teams/tournament/{tournamentId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public byte[] getTeamsScoreRankingFromTournamentAsPdf(@Parameter(description = "Id of an existing tournament", required = true)
                                                          @PathVariable("tournamentId") Integer tournamentId,
                                                          Locale locale, HttpServletResponse response, HttpServletRequest request) {
        final TournamentDTO tournament = tournamentController.get(tournamentId);
        final List<ScoreOfTeam> scores = rankingController.getTeamsScoreRanking(tournament);
        try {
            final ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                    .filename(tournament.getName() + " - teams score.pdf").build();
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
            return pdfController.generateTeamsScoreList(locale, tournament, scores).generate();
        } catch (InvalidXmlElementException | EmptyPdfBodyException e) {
            RestServerLogger.errorMessage(this.getClass(), e);
            throw new BadRequestException(this.getClass(), e.getMessage());
        }
    }
}
