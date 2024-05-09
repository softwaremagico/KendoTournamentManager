package com.softwaremagico.kt.pdf.events;

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
    private static Image defaultBackgroundImage;
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

    private Image getBackgroundImage() {
        if (backgroundImage == null) {
            if (defaultBackgroundImage == null) {
                try (InputStream inputStream = TableBackgroundEvent.class.getResourceAsStream(imageResource)) {
                    if (inputStream != null) {
                        defaultBackgroundImage = Image.getInstance(inputStream.readAllBytes());
                        defaultBackgroundImage.setAlignment(Image.UNDERLYING);
                        defaultBackgroundImage.scaleToFit(document.getPageSize().getWidth(), document.getPageSize().getHeight());
                        defaultBackgroundImage.setAbsolutePosition(0, 0);
                    }
                } catch (NullPointerException | BadElementException | IOException ex) {
                    PdfExporterLog.severe(TableBackgroundEvent.class.getName(), "No background image found!");
                }
            }
            backgroundImage = defaultBackgroundImage;
        }
        return backgroundImage;
    }

    @Override
    public void tableLayout(PdfPTable ppt, float[][] widths, float[] heights, int headerRows, int rowStart,
                            PdfContentByte[] pcbs) {
        try {
            if (getBackgroundImage() != null) {
                final int columns = widths[0].length - 1;
                final Rectangle rect = new Rectangle(widths[0][0], heights[0], widths[0][columns], heights[1]);
                pcbs[PdfPTable.BASECANVAS].addImage(getBackgroundImage(), rect.getWidth(), 0, 0, -rect.getHeight(),
                        rect.getLeft(), rect.getTop());
            }
        } catch (Exception e) {
            PdfExporterLog.errorMessage(this.getClass().getName(), e);
        }
    }
}
