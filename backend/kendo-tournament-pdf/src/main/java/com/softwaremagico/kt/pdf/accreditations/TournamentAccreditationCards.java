package com.softwaremagico.kt.pdf.accreditations;

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


import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantImageDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.pdf.PdfDocument;
import com.softwaremagico.kt.pdf.events.TableBackgroundEvent;
import org.springframework.context.MessageSource;

import java.util.Locale;
import java.util.Map;


public class TournamentAccreditationCards extends PdfDocument {

    private static final int BORDER = 1;

    private final MessageSource messageSource;
    private final Locale locale;

    private final TournamentDTO tournament;
    private final Map<ParticipantDTO, RoleDTO> competitorsRoles;
    private final byte[] banner;
    private final Map<ParticipantDTO, ParticipantImageDTO> participantImages;


    public TournamentAccreditationCards(MessageSource messageSource, Locale locale, TournamentDTO tournament, Map<ParticipantDTO, RoleDTO> competitorsRoles,
                                        Map<ParticipantDTO, ParticipantImageDTO> participantImages, byte[] banner) {
        this.messageSource = messageSource;
        this.locale = locale;
        this.tournament = tournament;
        this.competitorsRoles = competitorsRoles;
        this.banner = banner;
        this.participantImages = participantImages;
    }

    @Override
    protected void createContent(Document document, PdfWriter writer) {
        final PdfPTable table = pageTable(document);
        table.setWidthPercentage(100);
        document.add(table);
    }

    @Override
    protected void addDocumentWriterEvents(PdfWriter writer) {

    }

    @Override
    protected Rectangle getPageSize() {
        return PageSize.A4;
    }

    private PdfPTable pageTable(Document document) {
        PdfPCell cell;
        final float[] widths = {0.50f, 0.50f};
        final PdfPTable mainTable = new PdfPTable(widths);
        mainTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        mainTable.setTotalWidth(document.getPageSize().getWidth());

        for (final Map.Entry<ParticipantDTO, RoleDTO> entry : competitorsRoles.entrySet()) {
            final ParticipantImageDTO participantImageDTO = participantImages.get(entry.getKey());
            final ParticipantAccreditationCard competitorPDF = new ParticipantAccreditationCard(messageSource, locale, tournament,
                    entry.getKey(), entry.getValue(), participantImageDTO != null ? participantImageDTO.getData() : null, banner);
            final PdfPTable competitorTable = competitorPDF.pageTable(document.getPageSize().getWidth() / 2,
                    document.getPageSize().getHeight() / 2);
            competitorTable.setTableEvent(new TableBackgroundEvent());
            cell = new PdfPCell(competitorTable);
            cell.setBorderWidth(BORDER);
            cell.setColspan(1);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.addElement(competitorTable);
            mainTable.addCell(cell);
        }
        mainTable.completeRow();
        return mainTable;
    }
}
