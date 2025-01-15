package com.softwaremagico.kt.pdf.events;

/*-
 * #%L
 * Kendo Tournament Manager (PDF)
 * %%
 * Copyright (C) 2021 - 2025 Softwaremagico
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

import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPCellEvent;
import com.lowagie.text.pdf.PdfPTable;

import java.awt.Color;

/**
 * Event for creating a transparent cell.
 */
public class TransparentBackgroundCell implements PdfPCellEvent {
    private static final float OPACITY = 0.6f;

    private final PdfGState documentGs = new PdfGState();

    public TransparentBackgroundCell() {
        documentGs.setFillOpacity(OPACITY);
        documentGs.setStrokeOpacity(1f);
    }

    @Override
    public void cellLayout(PdfPCell cell, Rectangle rect, PdfContentByte[] canvas) {
        final PdfContentByte cb = canvas[PdfPTable.BACKGROUNDCANVAS];
        cb.saveState();
        cb.setGState(documentGs);
        cb.setColorFill(Color.WHITE);
        cb.rectangle(rect.getLeft(), rect.getBottom(), rect.getWidth(), rect.getHeight());
        cb.fill();
        cb.restoreState();
    }
}
