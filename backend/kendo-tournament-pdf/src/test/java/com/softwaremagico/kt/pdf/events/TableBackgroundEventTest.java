package com.softwaremagico.kt.pdf.events;

/*-
 * #%L
 * Kendo Tournament Manager (PDF)
 * %%
 * Copyright (C) 2021 - 2026 SoftwareMagico
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
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;

@Test(groups = {"listsUnitTests"})
public class TableBackgroundEventTest {

    private static final float[][] WIDTHS = {{0f, 100f}};
    private static final float[] HEIGHTS = {100f, 0f};

    @Test
    public void nullResource_expectNoBackgroundAndNoException() {
        final TableBackgroundEvent event = new TableBackgroundEvent((String) null);
        event.tableLayout(new PdfPTable(1), WIDTHS, HEIGHTS, 0, 0, null);
    }

    @Test
    public void nonExistingResource_expectNoBackgroundAndNoException() {
        final TableBackgroundEvent event = new TableBackgroundEvent("/does-not-exist-image.png");
        event.tableLayout(new PdfPTable(1), WIDTHS, HEIGHTS, 0, 0, null);
    }

    @Test
    public void existingResourceWithoutDocument_expectCaughtExceptionAndCachedResult() {
        final TableBackgroundEvent event = new TableBackgroundEvent("/kendo-tournament-manager-logo.png");
        // First call initializes (and fails due to missing document), second call reuses the cached value.
        event.tableLayout(new PdfPTable(1), WIDTHS, HEIGHTS, 0, 0, null);
        event.tableLayout(new PdfPTable(1), WIDTHS, HEIGHTS, 0, 0, null);
    }

    @Test
    public void withBackgroundImageAndRealWriter_expectSuccessfulDraw() throws Exception {
        final Document document = new Document(PageSize.A4);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final PdfWriter writer = PdfWriter.getInstance(document, outputStream);
        document.open();
        final PdfContentByte content = writer.getDirectContent();
        document.add(new com.lowagie.text.Paragraph(" "));
        document.close();

        final Image image = Image.getInstance(getClass().getResourceAsStream("/kendo-tournament-manager-logo.png").readAllBytes());
        final TableBackgroundEvent event = new TableBackgroundEvent(image, document);

        final PdfContentByte[] pcbs = new PdfContentByte[]{content, content, content, content};
        event.tableLayout(new PdfPTable(1), WIDTHS, HEIGHTS, 0, 0, pcbs);
    }

    @Test
    public void withBackgroundImageAndNullCanvas_expectCaughtException() {
        final Document document = new Document(PageSize.A4);
        final TableBackgroundEvent event = new TableBackgroundEvent(null, document);
        event.tableLayout(new PdfPTable(1), WIDTHS, HEIGHTS, 0, 0, null);
    }
}


