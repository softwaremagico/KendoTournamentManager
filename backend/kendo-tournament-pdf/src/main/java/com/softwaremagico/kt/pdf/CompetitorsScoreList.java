package com.softwaremagico.kt.pdf;


import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.score.ScoreOfCompetitor;
import com.softwaremagico.kt.utils.NameUtils;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Locale;

public class CompetitorsScoreList extends ParentList {
    private final List<ScoreOfCompetitor> competitorTopTen;
    private final TournamentDTO tournament;
    private final MessageSource messageSource;
    private final Locale locale;

    public CompetitorsScoreList(MessageSource messageSource, Locale locale, TournamentDTO tournament, List<ScoreOfCompetitor> competitorTopTen) {
        this.tournament = tournament;
        this.competitorTopTen = competitorTopTen;
        this.messageSource = messageSource;
        this.locale = locale;
    }

    @Override
    public void createHeaderRow(Document document, PdfPTable mainTable, float width, float height, PdfWriter writer,
                                BaseFont font, int fontSize) {
        PdfPCell cell;

        // Tournament name.
        if (tournament != null) {
            final Paragraph p1 = new Paragraph(tournament.getName(), new Font(font, fontSize));
            cell = new PdfPCell(p1);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        } else {
            // List name.
            final Paragraph p2 = new Paragraph(
                    messageSource.getMessage("classification.competitors.title", null, locale), new Font(font, fontSize));
            cell = new PdfPCell(p2);
        }

        cell.setColspan(getTableWidths().length);
        cell.setBorderWidth(headerBorder);
        // cell.setBackgroundColor(new Color(255, 255, 255));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        mainTable.addCell(cell);

    }

    @Override
    public void createBodyRows(Document document, PdfPTable mainTable, float width, float height, PdfWriter writer,
                               BaseFont font, int fontSize) {

        mainTable.addCell(getCell(messageSource.getMessage("classification.competitors.competitor.name", null, locale), 0, Element.ALIGN_CENTER));
        mainTable.addCell(getCell(messageSource.getMessage("classification.competitors.duels.won", null, locale), 0, Element.ALIGN_CENTER));
        mainTable.addCell(getCell(messageSource.getMessage("classification.competitors.duels.draw", null, locale), 0, Element.ALIGN_CENTER));
        mainTable.addCell(getCell(messageSource.getMessage("classification.competitors.fights", null, locale), 0, Element.ALIGN_CENTER));

        for (final ScoreOfCompetitor scoreOfCompetitor : competitorTopTen) {
            mainTable.addCell(getCell(NameUtils.getLastnameName(scoreOfCompetitor.getCompetitor()), 1, Element.ALIGN_CENTER));
            mainTable.addCell(getCell(scoreOfCompetitor.getWonDuels() + "/"
                    + scoreOfCompetitor.getDrawDuels(), 1, Element.ALIGN_CENTER));
            mainTable.addCell(getCell("" + scoreOfCompetitor.getHits(), 1, Element.ALIGN_CENTER));

            mainTable.addCell(getCell("" + scoreOfCompetitor.getDuelsDone(), 1, Element.ALIGN_CENTER));
        }
    }

    @Override
    public void createFooterRow(Document document, PdfPTable mainTable, float width, float height, PdfWriter writer,
                                BaseFont font, int fontSize) {
        mainTable.addCell(getEmptyRow());
    }

    @Override
    public float[] getTableWidths() {
        return new float[]{0.50f, 0.20f, 0.20f, 0.20f};
    }

    @Override
    public void setTableProperties(PdfPTable mainTable) {
        mainTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
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
