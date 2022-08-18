package com.softwaremagico.kt.pdf.lists;

/*-
 * #%L
 * Kendo Tournament Manager (PDF)
 * %%
 * Copyright (C) 2021 - 2022 Softwaremagico
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
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.pdf.BaseColor;
import com.softwaremagico.kt.pdf.EmptyPdfBodyException;
import com.softwaremagico.kt.pdf.ParentList;
import com.softwaremagico.kt.pdf.PdfTheme;
import com.softwaremagico.kt.utils.NameUtils;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RoleList extends ParentList {

    private final MessageSource messageSource;
    private final Locale locale;
    private final TournamentDTO tournament;
    private final Map<ClubDTO, List<RoleDTO>> rolesByClub;

    public RoleList(MessageSource messageSource, Locale locale, TournamentDTO tournament, Map<ClubDTO, List<RoleDTO>> rolesByClub) {
        this.messageSource = messageSource;
        this.locale = locale;
        this.tournament = tournament;
        this.rolesByClub = rolesByClub;
    }

    @Override
    public void setTableProperties(PdfPTable mainTable) {
        mainTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        mainTable.getDefaultCell().setBorder(TABLE_BORDER);
        mainTable.getDefaultCell().setBorderColor(BaseColor.BLACK);
        mainTable.setWidthPercentage(100);
    }

    @Override
    public void createBodyRows(Document document, PdfPTable mainTable, float width, float height, PdfWriter writer,
                               BaseFont font, int fontSize) throws EmptyPdfBodyException {
        boolean firstClub = true;
        boolean added = false;

        for (final Map.Entry<ClubDTO, List<RoleDTO>> participantsByClub : rolesByClub.entrySet()) {
            if (rolesByClub.keySet().size() > 1 && !participantsByClub.getValue().isEmpty()) {
                if (!firstClub) {
                    mainTable.addCell(getEmptyRow());
                } else {
                    firstClub = false;
                }
                String text = participantsByClub.getKey().getName();
                if (participantsByClub.getKey().getCountry().length() > 1) {
                    text += " (" + participantsByClub.getKey().getCountry() + ")";
                }
                mainTable.addCell(getHeader2(text, 0));
            }

            for (final RoleDTO role : participantsByClub.getValue()) {
                mainTable.addCell(getCell(NameUtils.getLastnameName(role.getParticipant()), PdfTheme.getHandwrittenFont(), 1, Element.ALIGN_CENTER));
                mainTable.addCell(getCell(messageSource.getMessage("competitors.role.name." +
                                role.getRoleType().toString().toLowerCase(locale), null, locale),
                        PdfTheme.getHandwrittenFont(), 1, Element.ALIGN_CENTER));
                added = true;
            }
        }
        if (!added) {
            throw new EmptyPdfBodyException("No clubs in this championship");
        }
    }

    @Override
    public void createHeaderRow(Document document, PdfPTable mainTable, float width, float height, PdfWriter writer,
                                BaseFont font, int fontSize) {
        final PdfPCell cell = new PdfPCell(new Paragraph(tournament.getName(), new Font(font, fontSize)));
        cell.setColspan(getTableWidths().length);
        cell.setBorderWidth(HEADER_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        mainTable.addCell(cell);
    }

    public void createFooterRow(Document document, PdfPTable mainTable, float width, float height, PdfWriter writer,
                                BaseFont font, int fontSize) {
        mainTable.addCell(getEmptyRow());
    }

    @Override
    public float[] getTableWidths() {
        return new float[]{0.60f, 0.30f};
    }

    @Override
    protected Rectangle getPageSize() {
        return PageSize.A4;
    }

    @Override
    protected void addDocumentWriterEvents(PdfWriter writer) {
        // No background.
    }
}