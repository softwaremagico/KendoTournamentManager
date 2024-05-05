package com.softwaremagico.kt.pdf.lists;

/*-
 * #%L
 * Kendo Tournament Manager (PDF)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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
import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.pdf.BaseColor;
import com.softwaremagico.kt.pdf.EmptyPdfBodyException;
import com.softwaremagico.kt.pdf.ParentList;
import com.softwaremagico.kt.pdf.PdfTheme;
import com.softwaremagico.kt.utils.NameUtils;
import org.springframework.context.MessageSource;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RoleList extends ParentList {

    private static final float[] TABLE_WIDTH = {0.60f, 0.30f};
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
        mainTable.setWidthPercentage(TOTAL_WIDTH);
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

            final List<RoleDTO> roles = participantsByClub.getValue();
            roles.sort(Comparator.comparing(o -> NameUtils.getLastnameName(o.getParticipant())));
            for (final RoleDTO role : roles) {
                mainTable.addCell(getCell(NameUtils.getLastnameName(role.getParticipant()), PdfTheme.getHandwrittenFont(), 1, Element.ALIGN_CENTER));
                mainTable.addCell(getCell(messageSource.getMessage("role.type."
                                + role.getRoleType().toString().toLowerCase(locale), null, locale),
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
        cell.setMinimumHeight(MIN_HEADER_HIGH);
        mainTable.addCell(cell);
    }

    @Override
    public void createFooterRow(Document document, PdfPTable mainTable, float width, float height, PdfWriter writer,
                                BaseFont font, int fontSize) {
        mainTable.addCell(getEmptyRow());
    }

    @Override
    public float[] getTableWidths() {
        return TABLE_WIDTH;
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
