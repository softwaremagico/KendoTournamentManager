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
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPCellEvent;
import com.lowagie.text.pdf.PdfPTable;

import java.awt.Color;

/**
 * Event for creating a transparent cell.
 */
public class FaultTriangleCellEvent implements PdfPCellEvent {

    private static final int TRIANGLE_MARGIN = 6;
    private static final int BOTTOM_MARGIN = 4;


    @Override
    public void cellLayout(PdfPCell cell, Rectangle rect, PdfContentByte[] canvas) {
        final PdfContentByte cb = canvas[PdfPTable.BACKGROUNDCANVAS];
        cb.setColorStroke(Color.BLACK);
        cb.setColorFill(Color.BLACK);
        cb.moveTo(rect.getLeft() + TRIANGLE_MARGIN, rect.getBottom() + BOTTOM_MARGIN);
        cb.lineTo(rect.getRight() - TRIANGLE_MARGIN, rect.getBottom() + BOTTOM_MARGIN);
        cb.lineTo(rect.getLeft() + (rect.getRight() - rect.getLeft()) / 2, rect.getTop() - TRIANGLE_MARGIN
                - (TRIANGLE_MARGIN - BOTTOM_MARGIN));
        cb.closePathFillStroke();
        cb.stroke();
    }
}
