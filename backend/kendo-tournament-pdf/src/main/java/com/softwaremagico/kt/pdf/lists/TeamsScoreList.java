package com.softwaremagico.kt.pdf.lists;

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

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.softwaremagico.kt.core.controller.models.ScoreOfTeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.pdf.BaseColor;
import com.softwaremagico.kt.pdf.ParentList;
import com.softwaremagico.kt.pdf.PdfTheme;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Locale;

/**
 * Creates a sheet with the teams ranking depending on the performance on the tournament.
 */
public class TeamsScoreList extends ParentList {
    private static final float[] TABLE_WIDTH = {0.40f, 0.20f, 0.20f, 0.20f, 0.20f};

    private final TournamentDTO tournament;
    private final List<ScoreOfTeamDTO> teamTopTen;

    private final MessageSource messageSource;

    private final Locale locale;

    public TeamsScoreList(MessageSource messageSource, Locale locale, TournamentDTO tournament, List<ScoreOfTeamDTO> teamTopTen) {
        this.tournament = tournament;
        this.teamTopTen = teamTopTen;
        this.messageSource = messageSource;
        this.locale = locale;
    }

    @Override
    public void createBodyRows(Document document, PdfPTable mainTable, float width, float height, PdfWriter writer,
                               BaseFont font, int fontSize) {
        mainTable.addCell(getCell(messageSource.getMessage("classification.team.name", null, locale),
                PdfTheme.getBasicFont(), 0, Element.ALIGN_CENTER, Font.BOLD));
        mainTable.addCell(getCell(messageSource.getMessage("classification.teams.fights.won", null, locale),
                PdfTheme.getBasicFont(), 0, Element.ALIGN_CENTER, Font.BOLD));
        mainTable.addCell(getCell(messageSource.getMessage("classification.teams.duels.won", null, locale),
                PdfTheme.getBasicFont(), 0, Element.ALIGN_CENTER, Font.BOLD));
        mainTable.addCell(getCell(messageSource.getMessage("classification.teams.hits", null, locale),
                PdfTheme.getBasicFont(), 0, Element.ALIGN_CENTER, Font.BOLD));
        mainTable.addCell(getCell(messageSource.getMessage("classification.teams.fights", null, locale),
                PdfTheme.getBasicFont(), 0, Element.ALIGN_CENTER, Font.BOLD));

        for (final ScoreOfTeamDTO scoreOfTeam : teamTopTen) {
            mainTable.addCell(getCell(scoreOfTeam.getTeam().getName(), PdfTheme.getHandwrittenFont(), 1, Element.ALIGN_CENTER));
            mainTable.addCell(getCell(scoreOfTeam.getWonFights() + "/" + scoreOfTeam.getDrawFights(), PdfTheme.getHandwrittenFont(), 1,
                    Element.ALIGN_CENTER));
            mainTable.addCell(getCell(scoreOfTeam.getWonDuels() + "/" + scoreOfTeam.getDrawDuels(), PdfTheme.getHandwrittenFont(), 1,
                    Element.ALIGN_CENTER));
            mainTable.addCell(getCell(scoreOfTeam.getHits() + (scoreOfTeam.getUntieDuels() > 0 ? "*" : ""),
                    PdfTheme.getHandwrittenFont(), 1, Element.ALIGN_CENTER));
            mainTable.addCell(getCell(String.valueOf(scoreOfTeam.getFightsDone()), PdfTheme.getHandwrittenFont(), 1, Element.ALIGN_CENTER));
        }
    }

    @Override
    public float[] getTableWidths() {
        return TABLE_WIDTH;
    }

    @Override
    public void setTableProperties(PdfPTable mainTable) {
        mainTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        mainTable.getDefaultCell().setBorder(TABLE_BORDER);
        mainTable.getDefaultCell().setBorderColor(BaseColor.BLACK);
        mainTable.setWidthPercentage(TOTAL_WIDTH);
    }

    @Override
    public void createHeaderRow(Document document, PdfPTable mainTable, float width, float height, PdfWriter writer,
                                BaseFont font, int fontSize) {
        final PdfPCell cell = new PdfPCell(new Paragraph(tournament.getName(), new Font(font, fontSize)));
        cell.setColspan(getTableWidths().length);
        cell.setBorderWidth(HEADER_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setMinimumHeight(MIN_HEADER_HIGH);
        mainTable.addCell(cell);
    }

    @Override
    public void createFooterRow(Document document, PdfPTable mainTable, float width, float height, PdfWriter writer,
                                BaseFont font, int fontSize) {
        mainTable.addCell(getEmptyRow());
    }

    @Override
    protected void addDocumentWriterEvents(PdfWriter writer) {
        // No background.
    }

    @Override
    protected Rectangle getPageSize() {
        return PageSize.A4;
    }
}
