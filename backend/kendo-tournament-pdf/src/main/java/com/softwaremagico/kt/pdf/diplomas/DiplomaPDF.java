package com.softwaremagico.kt.pdf.diplomas;

/*-
 * #%L
 * Kendo Tournament Manager (PDF)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.logger.KendoTournamentLogger;
import com.softwaremagico.kt.pdf.PdfDocument;
import com.softwaremagico.kt.pdf.PdfTheme;
import com.softwaremagico.kt.pdf.events.TableBackgroundEvent;
import com.softwaremagico.kt.utils.NameUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class DiplomaPDF extends PdfDocument {
    private static final int BORDER = 0;
    private final List<ParticipantDTO> participants;
    private final byte[] backgroundImage;

    public DiplomaPDF(List<ParticipantDTO> participants, byte[] backgroundImage) {
        this.participants = participants;
        this.backgroundImage = backgroundImage;
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

        mainTable.writeSelectedRows(0, -1, (float) 0, (float) (document.getPageSize().getHeight() / 2.0 + PdfTheme.DIPLOMA_FONT_SIZE + 10),
                writer.getDirectContent());
        mainTable.flushContent();
        mainTable.setWidthPercentage(100);
        document.add(mainTable);
    }

    void addBackGroundImage(Document document) {
        if (backgroundImage != null) {
            try {
                final Image background = Image.getInstance(backgroundImage);
                background.setAlignment(Image.UNDERLYING);
                background.scaleToFit(document.getPageSize().getWidth(), document.getPageSize().getHeight());
                background.setAbsolutePosition(0, 0);
                document.add(background);
            } catch (IOException e) {
                KendoTournamentLogger.warning(this.getClass(), "No background image found!");
            }
        } else {
            try (InputStream inputStream = TableBackgroundEvent.class.getResourceAsStream("/images/default-diploma.png");) {
                if (inputStream != null) {
                    final Image defaultBackgroundImage = Image.getInstance(inputStream.readAllBytes());
                    defaultBackgroundImage.setAlignment(Image.UNDERLYING);
                    defaultBackgroundImage.scaleToFit(document.getPageSize().getWidth(), document.getPageSize().getHeight());
                    defaultBackgroundImage.setAbsolutePosition(0, 0);
                    document.add(defaultBackgroundImage);
                }
            } catch (NullPointerException | BadElementException | IOException ex) {
                KendoTournamentLogger.severe(TableBackgroundEvent.class.getName(), "No default diploma image found!");
            }
        }
    }

}
