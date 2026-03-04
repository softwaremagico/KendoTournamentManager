package com.softwaremagico.kt.pdf;

/*-
 * #%L
 * Kendo Tournament Manager (PDF)
 * %%
 * Copyright (C) 2021 - 2026 Softwaremagico
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

import com.softwaremagico.kt.core.controller.RankingController;
import com.softwaremagico.kt.core.controller.models.ScoreOfCompetitorDTO;
import com.softwaremagico.kt.core.controller.models.ScoreOfTeamDTO;
import com.softwaremagico.kt.pdf.controller.PdfController;
import com.softwaremagico.kt.utils.BasicDataTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

@SpringBootTest
@Test(groups = {"scoreListPdf"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ListTests extends BasicDataTest {
    private static final String PDF_PATH_OUTPUT = System.getProperty("java.io.tmpdir") + File.separator + "PdfTests" + File.separator;

    @Autowired
    private PdfController pdfController;

    @Autowired
    private RankingController rankingController;

    @BeforeClass
    public void prepareFolder() throws IOException {
        Files.createDirectories(Paths.get(PDF_PATH_OUTPUT));
    }

    @BeforeClass
    public void prepareData() {
        populateData();
        resolveFights();
    }

    @Test
    public void generateCompetitorsListPdf() {
        List<ScoreOfCompetitorDTO> competitorTopTen = rankingController.getCompetitorsScoreRankingFromTournament(tournament.getId());
        Assert.assertEquals(pdfController.generateCompetitorsScoreList(Locale.getDefault(), tournament, competitorTopTen)
                .createFile(PDF_PATH_OUTPUT + "CompetitorsList.pdf"), 2); // No clue why are 2 pages and not 1.
    }

    @Test
    public void generateTeamsScoreListPdf() {
        List<ScoreOfTeamDTO> teamsTopTen = rankingController.getTeamsScoreRanking(tournament);
        Assert.assertEquals(pdfController.generateTeamsScoreList(Locale.getDefault(), tournament, teamsTopTen)
                .createFile(PDF_PATH_OUTPUT + "TeamsScoreList.pdf"), 2); // No clue why are 2 pages and not 1.
    }

    @Test
    public void generateClubListPdf() {
        Assert.assertEquals(pdfController.generateClubList(new Locale("es", "ES"), tournament)
                .createFile(PDF_PATH_OUTPUT + "ClubList.pdf"), 2); // No clue why are 2 pages and not 1.
    }

    @Test
    public void generateTeamListPdf() {
        Assert.assertEquals(pdfController.generateTeamList(tournament)
                .createFile(PDF_PATH_OUTPUT + "TeamList.pdf"), 2); // No clue why are 2 pages and not 1.
    }

    @Test
    public void generateFightListPdf() {
        Assert.assertEquals(pdfController.generateFightsList(Locale.getDefault(), tournament)
                .createFile(PDF_PATH_OUTPUT + "FightList.pdf"), 2); // No clue why are 2 pages and not 1.
    }

    @Test
    public void generateFightSummaryPdf() {
        Assert.assertEquals(pdfController.generateFightsSummaryList(Locale.getDefault(), tournament)
                .createFile(PDF_PATH_OUTPUT + "FightSummaryList.pdf"), 2); // No clue why are 2 pages and not 1.
    }

    @AfterClass
    public void removeFolder() {
        Assert.assertTrue(deleteDirectory(new File(PDF_PATH_OUTPUT)));
    }
}
