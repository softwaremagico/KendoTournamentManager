package com.softwaremagico.kt.html.controller;

/*-
 * #%L
 * Kendo Tournament Manager (PDF)
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

import com.softwaremagico.kt.core.controller.RankingController;
import com.softwaremagico.kt.core.controller.models.ScoreOfCompetitorDTO;
import com.softwaremagico.kt.core.controller.models.ScoreOfTeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.logger.KendoTournamentLogger;
import com.softwaremagico.kt.pdf.EmptyPdfBodyException;
import com.softwaremagico.kt.pdf.InvalidXmlElementException;
import com.softwaremagico.kt.pdf.controller.PdfController;
import org.springframework.stereotype.Controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
public class ZipController {

    private final PdfController pdfController;
    private final HtmlController htmlController;

    private final RankingController rankingController;

    public ZipController(PdfController pdfController, HtmlController htmlController, RankingController rankingController) {
        this.pdfController = pdfController;
        this.htmlController = htmlController;
        this.rankingController = rankingController;
    }


    public byte[] createZipData(Locale locale, TournamentDTO tournament) throws IOException {
        final List<ZipContent> content = new ArrayList<>();
        //Role List
        try {
            content.add(new ZipContent("Role List - " + tournament.getName(), "pdf",
                    pdfController.generateClubList(locale, tournament).generate()));
        } catch (EmptyPdfBodyException | InvalidXmlElementException e) {
            KendoTournamentLogger.errorMessage(this.getClass(), e);
        }
        //Team List
        try {
            content.add(new ZipContent("Team List - " + tournament.getName(), "pdf",
                    pdfController.generateTeamList(tournament).generate()));
        } catch (EmptyPdfBodyException | InvalidXmlElementException e) {
            KendoTournamentLogger.errorMessage(this.getClass(), e);
        }
        //Fight List
        try {
            content.add(new ZipContent("Fight List - " + tournament.getName(), "pdf",
                    pdfController.generateFightsSummaryList(locale, tournament).generate()));
        } catch (EmptyPdfBodyException | InvalidXmlElementException e) {
            KendoTournamentLogger.errorMessage(this.getClass(), e);
        }
        //Team Ranking
        try {
            final List<ScoreOfTeamDTO> scores = rankingController.getTeamsScoreRanking(tournament);
            content.add(new ZipContent("Team Ranking - " + tournament.getName(), "pdf",
                    pdfController.generateTeamsScoreList(locale, tournament, scores).generate()));
        } catch (EmptyPdfBodyException | InvalidXmlElementException e) {
            KendoTournamentLogger.errorMessage(this.getClass(), e);
        }
        //Competitors Ranking
        try {
            final List<ScoreOfCompetitorDTO> scores = rankingController.getCompetitorsScoreRanking(tournament);
            content.add(new ZipContent("Competitors Ranking - " + tournament.getName(), "pdf",
                    pdfController.generateCompetitorsScoreList(locale, tournament, scores).generate()));
        } catch (EmptyPdfBodyException | InvalidXmlElementException e) {
            KendoTournamentLogger.errorMessage(this.getClass(), e);
        }
        //BlogCode
        content.add(new ZipContent("Wordpress code - " + tournament.getName(), "txt",
                htmlController.generateBlogCode(locale, tournament).getWordpressFormat().getBytes(StandardCharsets.UTF_8)));
        return createZipData(content);
    }


    public byte[] createZipData(List<ZipContent> content) throws IOException {
        if (content == null || content.isEmpty()) {
            return null;
        }
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
        for (ZipContent zipContent : content) {
            try {
                final ZipEntry entry = new ZipEntry(zipContent.getName() + "." + zipContent.getExtension());
                zipOutputStream.putNextEntry(entry);
                final byte[] data = zipContent.getContent();
                zipOutputStream.write(data, 0, data.length);
                zipOutputStream.closeEntry();
            } catch (IOException e) {
                KendoTournamentLogger.errorMessage(this.getClass(), e);
            }
        }
        zipOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }
}
