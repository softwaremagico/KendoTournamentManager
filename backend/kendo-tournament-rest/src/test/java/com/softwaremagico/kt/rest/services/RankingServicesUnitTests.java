package com.softwaremagico.kt.rest.services;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
 * %%
 * Copyright (C) 2021 - 2026 SoftwareMagico
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
import com.softwaremagico.kt.html.lists.BlogExporter;
import com.softwaremagico.kt.pdf.EmptyPdfBodyException;
import com.softwaremagico.kt.pdf.InvalidXmlElementException;
import com.softwaremagico.kt.pdf.controller.PdfController;
import com.softwaremagico.kt.pdf.lists.CompetitorsScoreList;
import com.softwaremagico.kt.pdf.lists.TeamsScoreList;
import com.softwaremagico.kt.rest.exceptions.BadRequestException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.expectThrows;

@Test(groups = "restServicesUnit")
public class RankingServicesUnitTests {

    private RankingController rankingController;
    private TournamentController tournamentController;
    private ParticipantController participantController;
    private PdfController pdfController;
    private com.softwaremagico.kt.html.controller.HtmlController htmlController;
    private GroupController groupController;
    private com.softwaremagico.kt.html.controller.ZipController zipController;

    private RankingServices rankingServices;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        rankingController = mock(RankingController.class);
        tournamentController = mock(TournamentController.class);
        participantController = mock(ParticipantController.class);
        pdfController = mock(PdfController.class);
        htmlController = mock(com.softwaremagico.kt.html.controller.HtmlController.class);
        groupController = mock(GroupController.class);
        zipController = mock(com.softwaremagico.kt.html.controller.ZipController.class);

        rankingServices = new RankingServices(rankingController, pdfController, tournamentController,
                participantController, htmlController, groupController, zipController);
    }

    @Test
    public void shouldReturnCompetitorsRankingByGroup() {
        final List<ScoreOfCompetitorDTO> expected = List.of(new ScoreOfCompetitorDTO(), new ScoreOfCompetitorDTO());
        when(rankingController.getCompetitorsScoreRankingFromGroup(3)).thenReturn(expected);

        final List<ScoreOfCompetitorDTO> result = rankingServices.getCompetitorsScoreRankingGroup(3, mock(HttpServletRequest.class));

        assertEquals(result.size(), 2);
    }

    @Test
    public void shouldUseEmptyParticipantsWhenBodyIsNull() {
        when(rankingController.getCompetitorsGlobalScoreRanking(any(), eq(null))).thenReturn(List.of());

        final List<ScoreOfCompetitorDTO> result = rankingServices.getCompetitorsGlobalScoreRanking(Optional.empty(), null,
                mock(HttpServletRequest.class));

        assertNotNull(result);
        verify(rankingController).getCompetitorsGlobalScoreRanking(eq(List.of()), eq(null));
    }

    @Test
    public void shouldGenerateCompetitorsGlobalPdfAndSetHeaders() throws Exception {
        when(rankingController.getCompetitorsGlobalScoreRanking(any(), eq(1))).thenReturn(List.of(new ScoreOfCompetitorDTO()));
        final CompetitorsScoreList competitorsScoreList = mock(CompetitorsScoreList.class);
        when(pdfController.generateCompetitorsScoreList(any(Locale.class), eq(null), any())).thenReturn(competitorsScoreList);
        when(competitorsScoreList.generate()).thenReturn(new byte[]{1, 2, 3});

        final HttpServletResponse response = mock(HttpServletResponse.class);
        final byte[] bytes = rankingServices.getCompetitorsGlobalScoreRankingAsPdf(Optional.of(1), Set.of(), Locale.ENGLISH,
                response, mock(HttpServletRequest.class));

        assertEquals(bytes, new byte[]{1, 2, 3});
        verify(response).setHeader(eq(HttpHeaders.CONTENT_DISPOSITION), any());
    }

    @Test(expectedExceptions = BadRequestException.class)
    public void shouldWrapInvalidXmlExceptionInBadRequest() throws Exception {
        when(rankingController.getCompetitorsGlobalScoreRanking(any(), eq(null))).thenReturn(List.of(new ScoreOfCompetitorDTO()));
        final CompetitorsScoreList competitorsScoreList = mock(CompetitorsScoreList.class);
        when(pdfController.generateCompetitorsScoreList(any(Locale.class), eq(null), any())).thenReturn(competitorsScoreList);
        when(competitorsScoreList.generate()).thenThrow(new InvalidXmlElementException("invalid"));

        rankingServices.getCompetitorsGlobalScoreRankingAsPdf(Optional.empty(), Set.of(), Locale.ENGLISH,
                mock(HttpServletResponse.class), mock(HttpServletRequest.class));
    }

    @Test
    public void shouldDelegateCompetitorTournamentRanking() {
        when(rankingController.getCompetitorsScoreRankingFromTournament(9)).thenReturn(List.of(new ScoreOfCompetitorDTO()));

        final List<ScoreOfCompetitorDTO> result = rankingServices.getCompetitorsScoreRankingTournament(9, mock(HttpServletRequest.class));

        assertEquals(result.size(), 1);
        verify(rankingController).getCompetitorsScoreRankingFromTournament(9);
    }

    @Test
    public void shouldDelegateCompetitorRankingById() {
        final ParticipantDTO participant = new ParticipantDTO();
        final CompetitorRanking competitorRanking = new CompetitorRanking(5, 12);
        when(participantController.get(7)).thenReturn(participant);
        when(rankingController.getCompetitorRanking(participant)).thenReturn(competitorRanking);

        final CompetitorRanking result = rankingServices.getCompetitorsRanking(7, mock(HttpServletRequest.class));

        assertNotNull(result);
        verify(rankingController).getCompetitorRanking(participant);
    }

    @Test
    public void shouldDelegateGlobalRankingByClub() {
        when(rankingController.getCompetitorsGlobalScoreRankingByClub(4)).thenReturn(List.of(new ScoreOfCompetitorDTO()));

        final List<ScoreOfCompetitorDTO> result = rankingServices.getCompetitorsGlobalScoreRanking(4, mock(HttpServletRequest.class));

        assertEquals(result.size(), 1);
        verify(rankingController).getCompetitorsGlobalScoreRankingByClub(4);
    }

    @Test
    public void shouldGenerateCompetitorsByClubPdf() throws Exception {
        when(rankingController.getCompetitorsGlobalScoreRankingByClub(4)).thenReturn(List.of(new ScoreOfCompetitorDTO()));
        final CompetitorsScoreList competitorsScoreList = mock(CompetitorsScoreList.class);
        when(pdfController.generateCompetitorsScoreList(any(Locale.class), eq(null), any())).thenReturn(competitorsScoreList);
        when(competitorsScoreList.generate()).thenReturn(new byte[]{9, 8, 7});

        final HttpServletResponse response = mock(HttpServletResponse.class);
        final byte[] bytes = rankingServices.getCompetitorsGlobalScoreRankingByClubAsPdf(4, Locale.ENGLISH, response, mock(HttpServletRequest.class));

        assertEquals(bytes, new byte[]{9, 8, 7});
        verify(response).setHeader(eq(HttpHeaders.CONTENT_DISPOSITION), any());
    }

    @Test
    public void shouldWrapEmptyPdfBodyExceptionInBadRequest() throws Exception {
        when(rankingController.getCompetitorsGlobalScoreRanking(any(), eq(null))).thenReturn(List.of(new ScoreOfCompetitorDTO()));
        final CompetitorsScoreList competitorsScoreList = mock(CompetitorsScoreList.class);
        when(pdfController.generateCompetitorsScoreList(any(Locale.class), eq(null), any())).thenReturn(competitorsScoreList);
        when(competitorsScoreList.generate()).thenThrow(new EmptyPdfBodyException("empty"));

        expectThrows(BadRequestException.class, () -> rankingServices.getCompetitorsGlobalScoreRankingAsPdf(Optional.empty(), Set.of(), Locale.ENGLISH,
                mock(HttpServletResponse.class), mock(HttpServletRequest.class)));
    }

    @Test
    public void shouldDelegateTeamsRankingByGroupAndTournament() {
        when(rankingController.getTeamsScoreRankingFromGroup(1)).thenReturn(List.of(new ScoreOfTeamDTO()));
        when(rankingController.getTeamsScoreRankingFromTournament(2)).thenReturn(List.of(new ScoreOfTeamDTO()));

        final List<ScoreOfTeamDTO> fromGroup = rankingServices.getTeamsScoreRankingFromGroup(1, mock(HttpServletRequest.class));
        final List<ScoreOfTeamDTO> fromTournament = rankingServices.getTeamsScoreRankingFromTournament(2, mock(HttpServletRequest.class));

        assertEquals(fromGroup.size(), 1);
        assertEquals(fromTournament.size(), 1);
    }

    @Test
    public void shouldGenerateTeamTournamentPdf() throws Exception {
        final TournamentDTO tournament = new TournamentDTO();
        tournament.setName("Autumn Cup");
        when(tournamentController.get(6)).thenReturn(tournament);
        when(rankingController.getTeamsScoreRanking(tournament)).thenReturn(List.of(new ScoreOfTeamDTO()));
        final TeamsScoreList teamsScoreList = mock(TeamsScoreList.class);
        when(pdfController.generateTeamsScoreList(any(Locale.class), eq(tournament), any())).thenReturn(teamsScoreList);
        when(teamsScoreList.generate()).thenReturn(new byte[]{1, 1, 2, 3});

        final HttpServletResponse response = mock(HttpServletResponse.class);
        final byte[] bytes = rankingServices.getTeamsScoreRankingFromTournamentAsPdf(6, Locale.ENGLISH, response, mock(HttpServletRequest.class));

        assertEquals(bytes, new byte[]{1, 1, 2, 3});
        verify(response).setHeader(eq(HttpHeaders.CONTENT_DISPOSITION), any());
    }

    @Test
    public void shouldGenerateTeamsGroupPdf() throws Exception {
        final TournamentDTO tournament = new TournamentDTO();
        tournament.setName("Spring Cup");
        final GroupDTO groupDTO = new GroupDTO();
        groupDTO.setTournament(tournament);
        groupDTO.setLevel(2);
        groupDTO.setIndex(1);
        when(groupController.get(5)).thenReturn(groupDTO);
        when(rankingController.getTeamsScoreRanking(groupDTO)).thenReturn(List.of(new ScoreOfTeamDTO()));
        final TeamsScoreList teamsScoreList = mock(TeamsScoreList.class);
        when(pdfController.generateTeamsScoreList(any(Locale.class), eq(tournament), any())).thenReturn(teamsScoreList);
        when(teamsScoreList.generate()).thenReturn(new byte[]{4, 5, 6});

        final HttpServletResponse response = mock(HttpServletResponse.class);
        final byte[] bytes = rankingServices.getTeamsScoreRankingFromGroupAsPdf(5, Locale.ENGLISH, response, mock(HttpServletRequest.class));

        assertEquals(bytes, new byte[]{4, 5, 6});
        verify(response).setHeader(eq(HttpHeaders.CONTENT_DISPOSITION), any());
    }

    @Test
    public void shouldGenerateTournamentSummaryAsHtml() {
        final TournamentDTO tournament = new TournamentDTO();
        tournament.setName("Summary Cup");
        when(tournamentController.get(3)).thenReturn(tournament);
        final BlogExporter blogExporter = mock(BlogExporter.class);
        when(htmlController.generateBlogCode(any(Locale.class), eq(tournament))).thenReturn(blogExporter);
        when(blogExporter.getWordpressFormat()).thenReturn("blog content");

        final HttpServletResponse response = mock(HttpServletResponse.class);
        final byte[] bytes = rankingServices.getTournamentsSummaryAsHtml(3, Locale.ENGLISH, response, mock(HttpServletRequest.class));

        assertNotNull(bytes);
        verify(response).setHeader(eq(HttpHeaders.CONTENT_DISPOSITION), any());
    }

    @Test
    public void shouldCreateZipAndSetHeader() throws Exception {
        final TournamentDTO tournament = new TournamentDTO();
        tournament.setName("Zip Cup");
        when(tournamentController.get(8)).thenReturn(tournament);
        when(zipController.createZipData(any(Locale.class), eq(tournament))).thenReturn(new byte[]{7, 7, 7});

        final HttpServletResponse response = mock(HttpServletResponse.class);
        final byte[] bytes = rankingServices.startByEmail(8, Locale.ENGLISH, null, response, mock(HttpServletRequest.class));

        assertEquals(bytes, new byte[]{7, 7, 7});
        verify(response).setHeader(eq(HttpHeaders.CONTENT_DISPOSITION), any());
    }
}

