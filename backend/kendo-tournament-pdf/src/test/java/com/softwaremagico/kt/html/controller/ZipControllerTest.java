package com.softwaremagico.kt.html.controller;

import com.softwaremagico.kt.core.controller.RankingController;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.html.lists.BlogExporter;
import com.softwaremagico.kt.pdf.EmptyPdfBodyException;
import com.softwaremagico.kt.pdf.controller.PdfController;
import com.softwaremagico.kt.pdf.lists.CompetitorsScoreList;
import com.softwaremagico.kt.pdf.lists.FightSummary;
import com.softwaremagico.kt.pdf.lists.RoleList;
import com.softwaremagico.kt.pdf.lists.TeamList;
import com.softwaremagico.kt.pdf.lists.TeamsScoreList;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ZipControllerTest {

    @Test(groups = {"blogTests"})
    public void shouldReturnEmptyArrayWhenZipContentIsNullOrEmpty() throws Exception {
        final ZipController zipController = new ZipController(mock(PdfController.class), mock(HtmlController.class),
                mock(RankingController.class));

        assertEquals(zipController.createZipData((List<ZipContent>) null).length, 0);
        assertEquals(zipController.createZipData(List.of()).length, 0);
    }

    @Test(groups = {"blogTests"})
    public void shouldCreateZipWithProvidedEntries() throws Exception {
        final ZipController zipController = new ZipController(mock(PdfController.class), mock(HtmlController.class),
                mock(RankingController.class));

        final List<ZipContent> content = List.of(
                new ZipContent("first", "txt", "one".getBytes(StandardCharsets.UTF_8)),
                new ZipContent("second", "pdf", "two".getBytes(StandardCharsets.UTF_8))
        );

        final List<String> names = new ArrayList<>();
        final List<String> values = new ArrayList<>();
        readZip(zipController.createZipData(content), names, values);

        assertEquals(names, List.of("first.txt", "second.pdf"));
        assertEquals(values, List.of("one", "two"));
    }

    @Test(groups = {"blogTests"})
    public void shouldSkipFailingPdfAndStillIncludeRemainingEntries() throws Exception {
        final PdfController pdfController = mock(PdfController.class);
        final HtmlController htmlController = mock(HtmlController.class);
        final RankingController rankingController = mock(RankingController.class);
        final TournamentDTO tournament = mock(TournamentDTO.class);
        final RoleList roleList = mock(RoleList.class);
        final TeamList teamList = mock(TeamList.class);
        final FightSummary fightSummary = mock(FightSummary.class);
        final TeamsScoreList teamsScoreList = mock(TeamsScoreList.class);
        final CompetitorsScoreList competitorsScoreList = mock(CompetitorsScoreList.class);
        final BlogExporter blogExporter = mock(BlogExporter.class);

        when(tournament.getName()).thenReturn("Autumn Cup");
        when(roleList.generate()).thenReturn("roles".getBytes(StandardCharsets.UTF_8));
        when(teamList.generate()).thenThrow(new EmptyPdfBodyException("team list error"));
        when(fightSummary.generate()).thenReturn("fights".getBytes(StandardCharsets.UTF_8));
        when(teamsScoreList.generate()).thenReturn("teams".getBytes(StandardCharsets.UTF_8));
        when(competitorsScoreList.generate()).thenReturn("competitors".getBytes(StandardCharsets.UTF_8));
        when(blogExporter.getWordpressFormat()).thenReturn("blog");

        when(pdfController.generateClubList(Locale.US, tournament)).thenReturn(roleList);
        when(pdfController.generateTeamList(tournament)).thenReturn(teamList);
        when(pdfController.generateFightsSummaryList(Locale.US, tournament)).thenReturn(fightSummary);
        when(rankingController.getTeamsScoreRanking(tournament)).thenReturn(List.of());
        when(pdfController.generateTeamsScoreList(Locale.US, tournament, List.of())).thenReturn(teamsScoreList);
        when(rankingController.getCompetitorsScoreRanking(tournament)).thenReturn(List.of());
        when(pdfController.generateCompetitorsScoreList(Locale.US, tournament, List.of())).thenReturn(competitorsScoreList);
        when(htmlController.generateBlogCode(Locale.US, tournament)).thenReturn(blogExporter);

        final ZipController zipController = new ZipController(pdfController, htmlController, rankingController);

        final List<String> names = new ArrayList<>();
        final List<String> values = new ArrayList<>();
        readZip(zipController.createZipData(Locale.US, tournament), names, values);

        assertEquals(names.size(), 5);
        assertTrue(names.contains("Role List - Autumn Cup.pdf"));
        assertTrue(names.contains("Fight List - Autumn Cup.pdf"));
        assertTrue(names.contains("Team Ranking - Autumn Cup.pdf"));
        assertTrue(names.contains("Competitors Ranking - Autumn Cup.pdf"));
        assertTrue(names.contains("Wordpress code - Autumn Cup.txt"));
        assertEquals(values.get(values.size() - 1), "blog");
    }

    private void readZip(byte[] zipData, List<String> names, List<String> values) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(zipData), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                names.add(entry.getName());
                values.add(new String(zipInputStream.readAllBytes(), StandardCharsets.UTF_8));
            }
        }
    }
}

