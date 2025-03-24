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

import com.softwaremagico.kt.core.controller.GroupController;
import com.softwaremagico.kt.core.controller.ParticipantController;
import com.softwaremagico.kt.core.controller.RankingController;
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.ScoreOfCompetitorDTO;
import com.softwaremagico.kt.core.controller.models.ScoreOfTeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.score.CompetitorRanking;
import com.softwaremagico.kt.html.controller.HtmlController;
import com.softwaremagico.kt.html.controller.ZipController;
import com.softwaremagico.kt.logger.RestServerLogger;
import com.softwaremagico.kt.pdf.EmptyPdfBodyException;
import com.softwaremagico.kt.pdf.InvalidXmlElementException;
import com.softwaremagico.kt.pdf.controller.PdfController;
import com.softwaremagico.kt.rest.exceptions.BadRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/rankings")
public class RankingServices {

    private final RankingController rankingController;

    private final TournamentController tournamentController;

    private final ParticipantController participantController;

    private final PdfController pdfController;

    private final HtmlController htmlController;

    private final GroupController groupController;

    private final ZipController zipController;

    public RankingServices(RankingController rankingController, PdfController pdfController, TournamentController tournamentController,
                           ParticipantController participantController, HtmlController htmlController, GroupController groupController,
                           ZipController zipController) {
        this.rankingController = rankingController;
        this.tournamentController = tournamentController;
        this.pdfController = pdfController;
        this.participantController = participantController;
        this.htmlController = htmlController;
        this.groupController = groupController;
        this.zipController = zipController;
    }


    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Gets participants' ranking.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/competitors/groups/{groupId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ScoreOfCompetitorDTO> getCompetitorsScoreRankingGroup(@Parameter(description = "Id of an existing group", required = true)
                                                                      @PathVariable("groupId") Integer groupId,
                                                                      HttpServletRequest request) {
        return rankingController.getCompetitorsScoreRankingFromGroup(groupId);
    }


    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege,"
            + " @securityService.guestPrivilege)")
    @Operation(summary = "Gets participants' ranking.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/competitors/tournaments/{tournamentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ScoreOfCompetitorDTO> getCompetitorsScoreRankingTournament(@Parameter(description = "Id of an existing tournament", required = true)
                                                                           @PathVariable("tournamentId") Integer tournamentId,
                                                                           HttpServletRequest request) {
        return rankingController.getCompetitorsScoreRankingFromTournament(tournamentId);
    }


    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Gets participants' global ranking.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/competitors", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ScoreOfCompetitorDTO> getCompetitorsGlobalScoreRanking(@RequestParam(name = "from") Optional<Integer> from,
                                                                       @RequestBody(required = false) Set<ParticipantDTO> participants,
                                                                       HttpServletRequest request) {
        return rankingController.getCompetitorsGlobalScoreRanking(participants != null ? participants : new ArrayList<>(), from.orElse(null));
    }


    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege,"
            + " @securityService.participantPrivilege)")
    @Operation(summary = "Gets participant global ranking.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/competitors/{competitorId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompetitorRanking getCompetitorsRanking(@PathVariable("competitorId") Integer competitorId, HttpServletRequest request) {
        return rankingController.getCompetitorRanking(participantController.get(competitorId));
    }


    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Gets participants' global ranking.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/competitors/pdf", produces = MediaType.APPLICATION_JSON_VALUE)
    public byte[] getCompetitorsGlobalScoreRankingAsPdf(@RequestParam(name = "from") Optional<Integer> from,
                                                        @RequestBody(required = false) Set<ParticipantDTO> participants,
                                                        Locale locale, HttpServletResponse response, HttpServletRequest request) {
        final List<ScoreOfCompetitorDTO> scores = rankingController.getCompetitorsGlobalScoreRanking(participants != null ? participants : new ArrayList<>(),
                from.orElse(null));
        try {
            final byte[] bytes = pdfController.generateCompetitorsScoreList(locale, null, scores).generate();
            final ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                    .filename("competitors score.pdf").build();
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
            return bytes;
        } catch (InvalidXmlElementException | EmptyPdfBodyException e) {
            RestServerLogger.errorMessage(this.getClass(), e);
            throw new BadRequestException(this.getClass(), e.getMessage());
        }
    }


    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Gets participants' global ranking.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/competitors/clubs/{clubId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ScoreOfCompetitorDTO> getCompetitorsGlobalScoreRanking(@Parameter(description = "Id of an existing club", required = true)
                                                                       @PathVariable("clubId") Integer clubId,
                                                                       HttpServletRequest request) {
        return rankingController.getCompetitorsGlobalScoreRankingByClub(clubId);
    }


    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Gets participants' global ranking.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/competitors/clubs/{clubId}/pdf", produces = MediaType.APPLICATION_JSON_VALUE)
    public byte[] getCompetitorsGlobalScoreRankingByClubAsPdf(@Parameter(description = "Id of an existing club", required = true)
                                                              @PathVariable("clubId") Integer clubId,
                                                              Locale locale, HttpServletResponse response, HttpServletRequest request) {
        final List<ScoreOfCompetitorDTO> scores = rankingController.getCompetitorsGlobalScoreRankingByClub(clubId);
        try {
            final byte[] bytes = pdfController.generateCompetitorsScoreList(locale, null, scores).generate();
            final ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                    .filename("club score.pdf").build();
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
            return bytes;
        } catch (InvalidXmlElementException | EmptyPdfBodyException e) {
            RestServerLogger.errorMessage(this.getClass(), e);
            throw new BadRequestException(this.getClass(), e.getMessage());
        }
    }


    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Gets participants' ranking in a pdf file.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/competitors/tournaments/{tournamentId}/pdf", produces = {MediaType.APPLICATION_PDF_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public byte[] getCompetitorsScoreRankingTournamentAsPdf(@Parameter(description = "Id of an existing tournament", required = true)
                                                            @PathVariable("tournamentId") Integer tournamentId,
                                                            Locale locale, HttpServletResponse response, HttpServletRequest request) {
        final TournamentDTO tournament = tournamentController.get(tournamentId);
        final List<ScoreOfCompetitorDTO> scores = rankingController.getCompetitorsScoreRanking(tournament);
        try {
            final byte[] bytes = pdfController.generateCompetitorsScoreList(locale, tournament, scores).generate();
            final ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                    .filename(tournament.getName() + " - competitors score.pdf").build();
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
            return bytes;
        } catch (InvalidXmlElementException | EmptyPdfBodyException e) {
            RestServerLogger.errorMessage(this.getClass(), e);
            throw new BadRequestException(this.getClass(), e.getMessage());
        }
    }


    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Gets teams' ranking.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/teams/groups/{groupId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ScoreOfTeamDTO> getTeamsScoreRankingFromGroup(@Parameter(description = "Id of an existing group", required = true)
                                                              @PathVariable("groupId") Integer groupId,
                                                              HttpServletRequest request) {
        return rankingController.getTeamsScoreRankingFromGroup(groupId);
    }


    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege,"
            + " @securityService.guestPrivilege)")
    @Operation(summary = "Gets teams' ranking.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/teams/tournaments/{tournamentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ScoreOfTeamDTO> getTeamsScoreRankingFromTournament(@Parameter(description = "Id of an existing tournament", required = true)
                                                                   @PathVariable("tournamentId") Integer tournamentId,
                                                                   HttpServletRequest request) {
        return rankingController.getTeamsScoreRankingFromTournament(tournamentId);
    }


    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Gets teams' ranking in a pdf file.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/teams/tournaments/{tournamentId}/pdf", produces = {MediaType.APPLICATION_PDF_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public byte[] getTeamsScoreRankingFromTournamentAsPdf(@Parameter(description = "Id of an existing tournament", required = true)
                                                          @PathVariable("tournamentId") Integer tournamentId,
                                                          Locale locale, HttpServletResponse response, HttpServletRequest request) {
        final TournamentDTO tournament = tournamentController.get(tournamentId);
        final List<ScoreOfTeamDTO> scores = rankingController.getTeamsScoreRanking(tournament);
        try {
            final byte[] bytes = pdfController.generateTeamsScoreList(locale, tournament, scores).generate();
            final ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                    .filename(tournament.getName() + " - teams score.pdf").build();
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
            return bytes;
        } catch (InvalidXmlElementException | EmptyPdfBodyException e) {
            RestServerLogger.errorMessage(this.getClass(), e);
            throw new BadRequestException(this.getClass(), e.getMessage());
        }
    }


    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Gets teams' ranking in a pdf file.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/teams/groups/{groupId}/pdf", produces = {MediaType.APPLICATION_PDF_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public byte[] getTeamsScoreRankingFromGroupAsPdf(@Parameter(description = "Id of an existing group", required = true)
                                                     @PathVariable("groupId") Integer groupId,
                                                     Locale locale, HttpServletResponse response, HttpServletRequest request) {
        final GroupDTO groupDTO = groupController.get(groupId);
        final List<ScoreOfTeamDTO> scores = rankingController.getTeamsScoreRanking(groupDTO);
        try {
            final byte[] bytes = pdfController.generateTeamsScoreList(locale, groupDTO.getTournament(), scores).generate();
            final ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                    .filename(groupDTO.getTournament().getName() + "_" + groupDTO.getLevel() + "-" + groupDTO.getIndex()
                            + " - teams score.pdf").build();
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
            return bytes;
        } catch (InvalidXmlElementException | EmptyPdfBodyException e) {
            RestServerLogger.errorMessage(this.getClass(), e);
            throw new BadRequestException(this.getClass(), e.getMessage());
        }
    }


    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Gets complete tournament summary as html", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/summary/{tournamentId}/html", produces = MediaType.TEXT_PLAIN_VALUE)
    public byte[] getTournamentsSummaryAsHtml(@Parameter(description = "Id of an existing tournament", required = true)
                                              @PathVariable("tournamentId") Integer tournamentId,
                                              Locale locale, HttpServletResponse response, HttpServletRequest request) {
        final TournamentDTO tournament = tournamentController.get(tournamentId);

        final byte[] bytes = htmlController.generateBlogCode(locale, tournament).getWordpressFormat().getBytes(StandardCharsets.UTF_8);
        final ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(tournament.getName() + ".txt").build();
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
        return bytes;
    }


    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Download all files as a zip", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/tournament/{tournamentId}/zip")
    public byte[] startByEmail(@Parameter(description = "Id of an existing tournament", required = true)
                               @PathVariable("tournamentId") Integer tournamentId, Locale locale,
                               Authentication authentication, HttpServletResponse response, HttpServletRequest request) throws IOException {
        final TournamentDTO tournament = tournamentController.get(tournamentId);
        final byte[] bytes = zipController.createZipData(locale, tournament);
        final ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(tournament.getName() + ".zip").build();
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
        return bytes;
    }
}
