package com.softwaremagico.kt.pdf.accreditations;

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
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantImageDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.logger.KendoTournamentLogger;
import com.softwaremagico.kt.pdf.PdfDocument;
import com.softwaremagico.kt.pdf.events.TableBackgroundEvent;
import org.springframework.context.MessageSource;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;


public class TournamentAccreditationCards extends PdfDocument {

    private static final String BACKGROUND_IMAGE = "/images/accreditation-background.png";

    private static final int BORDER = 1;

    private final MessageSource messageSource;
    private final Locale locale;

    private final TournamentDTO tournament;
    private final Map<ParticipantDTO, RoleDTO> competitorsRoles;
    private final Map<ParticipantDTO, ParticipantImageDTO> participantImages;
    private Image banner;
    private Image background;
    private Image defaultPhoto;

    public static final class ImageAssets {
        private final byte[] banner;
        private final byte[] background;
        private final byte[] defaultPhoto;

        public ImageAssets(byte[] banner, byte[] background, byte[] defaultPhoto) {
            this.banner = banner;
            this.background = background;
            this.defaultPhoto = defaultPhoto;
        }
    }


    public TournamentAccreditationCards(MessageSource messageSource, Locale locale, TournamentDTO tournament,
                                        Map<ParticipantDTO, RoleDTO> competitorsRoles,
                                        Map<ParticipantDTO, ParticipantImageDTO> participantImages,
                                        ImageAssets imageAssets) {
        this.messageSource = messageSource;
        this.locale = locale;
        this.tournament = tournament;
        this.competitorsRoles = competitorsRoles;
        this.banner = toImage(imageAssets.banner, "Invalid banner found");
        this.background = toImage(imageAssets.background, "Invalid background image found");
        this.defaultPhoto = toImage(imageAssets.defaultPhoto, "Invalid default photo found");
        this.participantImages = participantImages;
    }

    private Image toImage(byte[] data, String errorMessage) {
        if (data == null) {
            return null;
        }
        try {
            return Image.getInstance(data);
        } catch (IOException ex) {
            KendoTournamentLogger.severe(this.getClass(), "{}: {}", errorMessage, ex.getMessage());
            return null;
        }
    }

    @Override
    protected void createContent(Document document, PdfWriter writer) {
        final PdfPTable table = pageTable(document);
        table.setWidthPercentage(TOTAL_WIDTH);
        document.add(table);
    }

    @Override
    protected void addDocumentWriterEvents(PdfWriter writer) {
        //Ignored.
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

            final Image participantImage = getParticipantImage(participantImageDTO);

            final ParticipantAccreditationCard competitorPDF = new ParticipantAccreditationCard(messageSource, locale, tournament,
                    entry.getKey(), entry.getValue(), participantImage, banner);
            final PdfPTable competitorTable = competitorPDF.pageTable(document.getPageSize().getWidth() / 2 - 40,
                    document.getPageSize().getHeight() / 2 + 150);
            setBackground(competitorTable, document);
            cell = new PdfPCell(competitorTable);
            cell.setBorderWidth(BORDER);
            cell.setColspan(1);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.addElement(competitorTable);
            mainTable.addCell(cell);
        }
        mainTable.completeRow();
        return mainTable;
    }

    private Image getParticipantImage(ParticipantImageDTO participantImageDTO) {
        if (participantImageDTO == null) {
            return this.defaultPhoto;
        }
        try {
            return Image.getInstance(participantImageDTO.getData());
        } catch (IOException ex) {
            KendoTournamentLogger.warning(this.getClass(), "Invalid participant image used in accreditation ({}).", ex.getMessage());
            return this.defaultPhoto;
        }
    }

    private void setBackground(PdfPTable competitorTable, Document document) {
        if (this.background != null) {
            competitorTable.setTableEvent(new TableBackgroundEvent(this.background, document));
        } else {
            competitorTable.setTableEvent(new TableBackgroundEvent(BACKGROUND_IMAGE));
        }
    }
}
