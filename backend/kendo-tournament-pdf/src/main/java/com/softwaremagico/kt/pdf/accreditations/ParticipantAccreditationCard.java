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

import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.logger.KendoTournamentLogger;
import com.softwaremagico.kt.pdf.PdfDocument;
import com.softwaremagico.kt.pdf.PdfTheme;
import com.softwaremagico.kt.pdf.events.TransparentBackgroundCell;
import com.softwaremagico.kt.utils.NameUtils;
import org.springframework.context.MessageSource;

import java.awt.*;
import java.io.IOException;
import java.sql.Time;
import java.util.Date;
import java.util.Locale;

public class ParticipantAccreditationCard extends PdfDocument {
    private static final int BORDER = 0;

    private final MessageSource messageSource;
    private final Locale locale;

    private final TournamentDTO tournament;
    private final ParticipantDTO participant;
    private final byte[] participantImage;
    private final RoleDTO role;
    private final byte[] banner;

    public ParticipantAccreditationCard(MessageSource messageSource, Locale locale,
                                        TournamentDTO tournament, ParticipantDTO participant, RoleDTO role,
                                        byte[] participantImage, byte[] banner) {
        this.messageSource = messageSource;
        this.locale = locale;
        this.tournament = tournament;
        this.participant = participant;
        this.participantImage = participantImage;
        this.role = role;
        this.banner = banner;
    }

    @Override
    protected void createContent(Document document, PdfWriter writer) throws DocumentException {
        final PdfPTable table = pageTable(document.getPageSize().getWidth(), document.getPageSize().getHeight() + 20);
        table.setWidthPercentage(100);
        document.add(table);
    }

    @Override
    protected void addDocumentWriterEvents(PdfWriter writer) {

    }

    private PdfPTable createNameTable() throws BadElementException {
        PdfPCell cell;
        Paragraph p;
        final float[] widths = {0.03f, 0.35f, 0.03f, 0.64f};
        final PdfPTable table = new PdfPTable(widths);
        Image accreditationImage = null;
        try {
            accreditationImage = Image.getInstance(participantImage);
        } catch (IOException | NullPointerException e) {
            KendoTournamentLogger.errorMessage(this.getClass(), e);
        }

        table.addCell(this.getEmptyCell(1));

        cell = new PdfPCell(accreditationImage, true);
        cell.setBorderWidth(BORDER);
        cell.setBackgroundColor(Color.WHITE);
        cell.setColspan(1);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        table.addCell(cell);

        table.addCell(this.getEmptyCell(1));

        final float[] widths2 = {0.90f, 0.10f};
        final PdfPTable table2 = new PdfPTable(widths2);

        p = new Paragraph(NameUtils.getShortName(participant.getName(), 18), new Font(PdfTheme.getLineFont(),
                PdfTheme.ACCREDITATION_NAME_FONT_SIZE, Font.BOLD));
        cell = new PdfPCell(p);
        cell.setBorderWidth(BORDER);
        cell.setColspan(1);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setCellEvent(new TransparentBackgroundCell());
        table2.addCell(cell);

        table2.addCell(this.getEmptyCell(1));

        p = new Paragraph(NameUtils.getShortLastname(participant, 10).toUpperCase(), new Font(PdfTheme.getLineFont(),
                PdfTheme.ACCREDITATION_LASTNAME_FONT_SIZE, Font.BOLD));
        cell = new PdfPCell(p);
        cell.setBorderWidth(BORDER);
        cell.setColspan(1);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setCellEvent(new TransparentBackgroundCell());
        table2.addCell(cell);

        table2.addCell(this.getEmptyCell(1));


        String clubName;
        if (participant.getClub() != null) {
            clubName = participant.getClub().getName();
        } else {
            clubName = "";
        }

        p = new Paragraph(clubName, new Font(PdfTheme.getLineFont(), PdfTheme.ACCREDITATION_CLUBNAME_FONT_SIZE));
        cell = new PdfPCell(p);
        cell.setBorderWidth(BORDER);
        cell.setColspan(1);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setCellEvent(new TransparentBackgroundCell());
        table2.addCell(cell);

        table2.addCell(this.getEmptyCell(1));

        cell = new PdfPCell(table2);
        cell.setColspan(1);
        cell.setBorderWidth(BORDER);
        table.addCell(cell);

        return table;
    }

    private PdfPTable createIdentificationTable(float height) {
        PdfPCell cell;
        Paragraph p;
        final PdfPTable table = new PdfPTable(1);


        final float[] widths = {0.08f, 0.90f, 0.02f};
        final PdfPTable table2 = new PdfPTable(widths);

        table2.addCell(this.getEmptyCell());
        p = new Paragraph(messageSource.getMessage("role.type." +
                role.getRoleType().toString().toLowerCase(locale), null, locale),
                new Font(PdfTheme.getLineFont(), PdfTheme.ACCREDITATION_ROLE_FONT_SIZE, Font.BOLD));

        cell = new PdfPCell(p);
        cell.setColspan(1);
        cell.setBorderWidth(BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        table2.addCell(cell);
        table2.addCell(this.getEmptyCell());

        table2.addCell(getEmptyCell(1));

        final String identification = messageSource.getMessage("role.type." +
                role.getRoleType().toString().toLowerCase(locale) + ".abbreviation", null, locale)
                + "-" + Math.abs(participant.getId());
        p = new Paragraph(identification, new Font(PdfTheme.getLineFont(), PdfTheme.ACCREDITATION_IDENTIFICATION_FONT_SIZE));
        cell = new PdfPCell(p);
        cell.setBorderWidth(BORDER + 2f);
        cell.setColspan(1);
        //cell.setFixedHeight(height);
        cell.setFixedHeight(height * 0.15f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        try {
            switch (role.getRoleType()) {
                case COMPETITOR:
                    cell.setBackgroundColor(new Color(35, 144, 239));
                    break;
                case REFEREE:
                    cell.setBackgroundColor(new Color(255, 102, 0));
                    break;
                case VOLUNTEER:
                    cell.setBackgroundColor(new Color(155, 0, 255));
                    break;
                case PRESS:
                    cell.setBackgroundColor(new Color(255, 0, 127));
                    break;
            }
        } catch (NullPointerException npe) {
            cell.setBackgroundColor(new Color(167, 239, 190));
        }
        table2.addCell(cell);

        table2.addCell(this.getEmptyCell());

        cell = new PdfPCell(table2);
        cell.setColspan(1);
        cell.setBorderWidth(BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        table.addCell(cell);


        return table;
    }

    private PdfPTable createBannerTable(float width) throws BadElementException {
        PdfPCell cell;
        final PdfPTable table = new PdfPTable(1);
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        table.setTotalWidth(width);
        table.getTotalWidth();

        try {
            cell = new PdfPCell(Image.getInstance(banner), true);
        } catch (IOException e) {
            cell = getEmptyCell();
        }

        cell.setBorderWidth(BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        table.addCell(cell);

        return table;
    }

    private PdfPTable mainTable(float width, float height) throws BadElementException {
        PdfPCell cell;
        final float[] widths = {1};
        final PdfPTable mainTable = new PdfPTable(widths);
        mainTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        mainTable.setTotalWidth(width);

        mainTable.addCell(this.getEmptyCell(1));

        cell = new PdfPCell(createNameTable());
        cell.setBorderWidth(BORDER);
        cell.setColspan(1);
        cell.setFixedHeight(height * 0.20f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        mainTable.addCell(cell);

        cell = new PdfPCell(createIdentificationTable(height));
        cell.setBorderWidth(BORDER);
        cell.setColspan(1);
        cell.setFixedHeight(height * 0.20f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        mainTable.addCell(cell);

        cell = new PdfPCell(createBannerTable(width));
        cell.setBorderWidth(BORDER);
        cell.setColspan(1);
        cell.setFixedHeight(height * 0.20f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        mainTable.addCell(cell);

        return mainTable;
    }

    public PdfPTable pageTable(float width, float height) throws BadElementException {
        PdfPCell cell;
        final float[] widths = {0.90f, 0.10f};
        final PdfPTable mainTable = new PdfPTable(widths);
        mainTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        mainTable.setTotalWidth(width + 30);

        cell = new PdfPCell(mainTable(width, height));
        cell.setBorderWidth(BORDER);
        cell.setBorderWidthBottom(1);
        cell.setBorderWidthLeft(1);
        cell.setBorderWidthTop(1);
        cell.setColspan(1);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        mainTable.addCell(cell);

        cell = new PdfPCell(createSignature(PdfTheme.FOOTER_FONT_SIZE));
        cell.setBorderWidth(BORDER);
        cell.setBorderWidthBottom(1);
        cell.setBorderWidthRight(1);
        cell.setBorderWidthTop(1);
        cell.setColspan(1);
        cell.setPaddingBottom(0);
        cell.setPaddingRight(0);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        mainTable.addCell(cell);

        return mainTable;
    }

    private PdfPTable createSignature(int fontSize) {
        final PdfPTable table = new PdfPTable(1);
        Paragraph p;
        PdfPCell cell;

        final Date date = new java.util.Date();
        final long lnMilliseconds = date.getTime();
        final Date sqlDate = new java.sql.Date(lnMilliseconds);
        final Time sqlTime = new java.sql.Time(lnMilliseconds);

        try {
            p = new Paragraph(tournament.getName() + " (" + sqlTime + " " + sqlDate + ")",
                    new Font(PdfTheme.getLineFont(), fontSize));
        } catch (NullPointerException npen) {
            p = new Paragraph("Accreditation Card (" + sqlTime + " " + sqlDate + ")",
                    new Font(PdfTheme.getLineFont(), fontSize));
        }
        cell = new PdfPCell(p);
        cell.setBorderWidth(0);
        cell.setRotation(90);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        table.addCell(cell);

        return table;
    }

    @Override
    protected Rectangle getPageSize() {
        return PageSize.A6;
    }
}
