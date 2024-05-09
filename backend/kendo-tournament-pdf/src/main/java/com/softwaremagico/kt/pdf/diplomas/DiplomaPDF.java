package com.softwaremagico.kt.pdf.diplomas;

/*-
 * #%L
 * Kendo Tournament Manager (PDF)
 * %%
 * Copyright (C) 2021 - 2024 Softwaremagico
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
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.logger.KendoTournamentLogger;
import com.softwaremagico.kt.pdf.PdfDocument;
import com.softwaremagico.kt.pdf.PdfTheme;
import com.softwaremagico.kt.utils.NameUtils;

import java.io.IOException;
import java.util.List;

public class DiplomaPDF extends PdfDocument {
    private static final int BORDER = 0;
    private final List<ParticipantDTO> participants;
    private final float nameHeight;
    private Image backgroundImage;

    public DiplomaPDF(List<ParticipantDTO> participants, byte[] backgroundImage, float nameHeight) {
        this.participants = participants;
        try {
            this.backgroundImage = Image.getInstance(backgroundImage);
        } catch (IOException e) {
            KendoTournamentLogger.severe(this.getClass().getName(), "No background image found");
            this.backgroundImage = null;
        }
        this.nameHeight = nameHeight;
    }

    @Override
    protected void createContent(Document document, PdfWriter writer) {
        pageTable(document, writer);
    }

    @Override
    protected void addDocumentWriterEvents(PdfWriter writer) {

    }

    @Override
    protected Rectangle getPageSize() {
        return PageSize.A4.rotate();
    }

    @Override
    protected void addEvent(PdfWriter writer) {
        //No Footer on Diplomas.
    }

    public void pageTable(Document document, PdfWriter writer) {
        for (final ParticipantDTO participant : participants) {
            diplomaTable(document, writer, participant);
        }
    }

    private void diplomaTable(Document document, PdfWriter writer, ParticipantDTO participant) {
        final PdfPTable mainTable = new PdfPTable(1);
        mainTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        mainTable.setTotalWidth(document.getPageSize().getWidth());

        document.newPage();
        addBackGroundImage(document);

        final Paragraph p = new Paragraph(NameUtils.getLastnameName(participant), new Font(PdfTheme.getLineFont(), PdfTheme.DIPLOMA_FONT_SIZE));
        final PdfPCell cell = new PdfPCell(p);
        cell.setBorderWidth(BORDER);
        cell.setColspan(1);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        mainTable.addCell(cell);

        mainTable.writeSelectedRows(0, -1, (float) 0, document.getPageSize().getHeight() * nameHeight + PdfTheme.DIPLOMA_FONT_SIZE / 2f,
                writer.getDirectContent());
        mainTable.flushContent();
        mainTable.setWidthPercentage(TOTAL_WIDTH);
        document.add(mainTable);
    }

    void addBackGroundImage(Document document) {
        if (backgroundImage != null) {
            final Image background = backgroundImage;
            background.setAlignment(Image.UNDERLYING);
            background.scaleToFit(document.getPageSize().getWidth(), document.getPageSize().getHeight());
            background.setAbsolutePosition(0, 0);
            document.add(background);
        }
    }

}
