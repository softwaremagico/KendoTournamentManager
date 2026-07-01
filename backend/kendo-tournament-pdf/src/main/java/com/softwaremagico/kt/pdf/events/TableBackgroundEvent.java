package com.softwaremagico.kt.pdf.events;

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

import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPTableEvent;
import com.softwaremagico.kt.logger.PdfExporterLog;

import java.io.IOException;
import java.io.InputStream;

public class TableBackgroundEvent implements PdfPTableEvent {

    private final String imageResource;
    private Image defaultBackgroundImage;
    private Image backgroundImage;
    private Document document;

    public TableBackgroundEvent(String imageResource) {
        super();
        this.imageResource = imageResource;
    }

    public TableBackgroundEvent(Image backgroundImage, Document document) {
        this(null);
        this.backgroundImage = backgroundImage;
        this.document = document;
    }

    private void initializeDefaultBackgroundImage() {
        if (this.defaultBackgroundImage == null) {
            try (InputStream inputStream = TableBackgroundEvent.class.getResourceAsStream(this.imageResource)) {
                if (inputStream != null) {
                    this.defaultBackgroundImage = Image.getInstance(inputStream.readAllBytes());
                    this.defaultBackgroundImage.setAlignment(Image.UNDERLYING);
                    this.defaultBackgroundImage.scaleToFit(this.document.getPageSize().getWidth(),
                            this.document.getPageSize().getHeight());
                    this.defaultBackgroundImage.setAbsolutePosition(0, 0);
                }
            } catch (final NullPointerException | BadElementException | IOException ex) {
                PdfExporterLog.severe(TableBackgroundEvent.class, "No background image found!");
            }
        }
    }

    private Image getBackgroundImage() {
        if (this.backgroundImage == null) {
            if (this.imageResource != null) {
                this.initializeDefaultBackgroundImage();
            }
            this.backgroundImage = this.defaultBackgroundImage;
        }
        return this.backgroundImage;
    }

    @Override
    public void tableLayout(PdfPTable ppt, float[][] widths, float[] heights, int headerRows, int rowStart,
            PdfContentByte[] pcbs) {
        try {
            if (this.getBackgroundImage() != null) {
                final int columns = widths[0].length - 1;
                final Rectangle rect = new Rectangle(widths[0][0], heights[0], widths[0][columns], heights[1]);
                pcbs[PdfPTable.BASECANVAS].addImage(this.getBackgroundImage(), rect.getWidth(), 0, 0, -rect.getHeight(),
                        rect.getLeft(), rect.getTop());
            }
        } catch (final Exception e) {
            PdfExporterLog.errorMessage(this.getClass(), e);
        }
    }
}
