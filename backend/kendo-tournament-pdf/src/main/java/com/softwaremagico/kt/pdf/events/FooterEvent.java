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


import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import com.softwaremagico.kt.pdf.PdfTheme;

public class FooterEvent extends PdfPageEventHelper {
    private static final int BOTTOM_MARGIN = 0;

    /**
     * Adds a footer to every page
     */
    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        //if (writer.getPageNumber() % 2 == 0) {
        final PdfContentByte cb = writer.getDirectContent();
        final Phrase footer = new Phrase("Created using 'Kendo Tournament Manager v2'",
                new Font(PdfTheme.getFooterFont(), PdfTheme.FOOTER_FONT_SIZE));
        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, footer, (document.right() - document.left()) / 2 + document.leftMargin(),
                document.bottom() + BOTTOM_MARGIN, 0);
        //}
    }
}
