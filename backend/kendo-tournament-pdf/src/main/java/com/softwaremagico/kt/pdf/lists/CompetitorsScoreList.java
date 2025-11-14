package com.softwaremagico.kt.pdf.lists;

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
import com.softwaremagico.kt.core.controller.models.ScoreOfCompetitorDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.pdf.BaseColor;
import com.softwaremagico.kt.pdf.ParentList;
import com.softwaremagico.kt.pdf.PdfTheme;
import com.softwaremagico.kt.utils.NameUtils;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Locale;

/**
 * Creates a sheet with the competitors ranking depending on the performance on the tournament.
 */
public class CompetitorsScoreList extends ParentList {

    private static final float[] TABLE_WIDTH = {0.50f, 0.20f, 0.20f, 0.20f};
    private static final float[] TABLE_WIDTH_WITH_CLASSIFICATION = {0.10f, 0.50f, 0.20f, 0.20f, 0.20f};
    private final List<ScoreOfCompetitorDTO> competitorTopTen;
    private final TournamentDTO tournament;
    private final MessageSource messageSource;
    private final Locale locale;

    private final boolean showClassificationOrder;

    public CompetitorsScoreList(MessageSource messageSource, Locale locale, TournamentDTO tournament, List<ScoreOfCompetitorDTO> competitorTopTen,
                                boolean showClassificationOrder) {
        this.tournament = tournament;
        this.competitorTopTen = competitorTopTen;
        this.messageSource = messageSource;
        this.locale = locale;
        this.showClassificationOrder = showClassificationOrder;
    }

    @Override
    public void createHeaderRow(Document document, PdfPTable mainTable, float width, float height, PdfWriter writer,
                                BaseFont font, int fontSize) {
        final PdfPCell cell;

        // Tournament name.
        if (tournament != null) {
            cell = new PdfPCell(new Paragraph(tournament.getName(), new Font(font, fontSize)));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        } else {
            // List name.
            cell = new PdfPCell(new Paragraph(
                    messageSource.getMessage("classification.competitors.title", null, locale), new Font(font, fontSize)));
        }

        cell.setColspan(getTableWidths().length);
        cell.setBorderWidth(HEADER_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setMinimumHeight(MIN_HEADER_HIGH);
        mainTable.addCell(cell);
    }

    @Override
    public void createBodyRows(Document document, PdfPTable mainTable, float width, float height, PdfWriter writer,
                               BaseFont font, int fontSize) {

        if (showClassificationOrder) {
            mainTable.addCell(getCell("", PdfTheme.getBasicFont(), 0, Element.ALIGN_CENTER, Font.BOLD));
        }
        mainTable.addCell(getCell(messageSource.getMessage("classification.competitors.competitor.name", null, locale),
                PdfTheme.getBasicFont(), 0, Element.ALIGN_CENTER, Font.BOLD));
        mainTable.addCell(getCell(messageSource.getMessage("classification.competitors.duels.won", null, locale),
                PdfTheme.getBasicFont(), 0, Element.ALIGN_CENTER, Font.BOLD));
        mainTable.addCell(getCell(messageSource.getMessage("classification.competitors.hits", null, locale),
                PdfTheme.getBasicFont(), 0, Element.ALIGN_CENTER, Font.BOLD));
        mainTable.addCell(getCell(messageSource.getMessage("classification.competitors.fights", null, locale),
                PdfTheme.getBasicFont(), 0, Element.ALIGN_CENTER, Font.BOLD));

        int counter = 1;
        for (final ScoreOfCompetitorDTO scoreOfCompetitor : competitorTopTen) {
            if (showClassificationOrder) {
                mainTable.addCell(getCell(String.valueOf(counter), PdfTheme.getHandwrittenFont(), 1, Element.ALIGN_LEFT));
                counter++;
            }
            mainTable.addCell(getCell(NameUtils.getLastnameName(scoreOfCompetitor.getCompetitor()), PdfTheme.getHandwrittenFont(), 1, Element.ALIGN_CENTER));
            mainTable.addCell(getCell(scoreOfCompetitor.getWonDuels() + (scoreOfCompetitor.getUntieDuels() > 0 ? "*" : "") + "/"
                    + scoreOfCompetitor.getDrawDuels(), PdfTheme.getHandwrittenFont(), 1, Element.ALIGN_CENTER));
            mainTable.addCell(getCell(scoreOfCompetitor.getHits() + (scoreOfCompetitor.getUntieHits() > 0 ? "*" : ""),
                    PdfTheme.getHandwrittenFont(), 1, Element.ALIGN_CENTER));
            mainTable.addCell(getCell(String.valueOf(scoreOfCompetitor.getDuelsDone()), PdfTheme.getHandwrittenFont(), 1, Element.ALIGN_CENTER));
        }
    }

    @Override
    public void createFooterRow(Document document, PdfPTable mainTable, float width, float height, PdfWriter writer,
                                BaseFont font, int fontSize) {
        mainTable.addCell(getEmptyRow());
    }

    @Override
    public float[] getTableWidths() {
        if (showClassificationOrder) {
            return TABLE_WIDTH_WITH_CLASSIFICATION;
        }
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
    protected void addDocumentWriterEvents(PdfWriter writer) {
        // No background.
    }

    @Override
    protected Rectangle getPageSize() {
        return PageSize.A4;
    }
}
