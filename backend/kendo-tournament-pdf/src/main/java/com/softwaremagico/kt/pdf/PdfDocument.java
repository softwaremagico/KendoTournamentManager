package com.softwaremagico.kt.pdf;

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
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfWriter;
import com.softwaremagico.kt.logger.PdfExporterLog;
import com.softwaremagico.kt.pdf.events.FooterEvent;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

public abstract class PdfDocument {
    protected static final int TOTAL_WIDTH = 100;
    private static final int RIGHT_MARGIN = 30;
    private static final int LEFT_MARGIN = 30;
    private static final int TOP_MARGIN = 30;
    private static final int BOTTOM_MARGIN = 30;

    protected Document addMetaData(Document document) {
        document.addTitle("List Report");
        document.addAuthor("Software Mágico");
        document.addCreator("Kendo Tournament Generator NG");
        document.addSubject("Kendo");
        document.addKeywords("Kendo, League");
        document.addCreationDate();
        return document;
    }

    private void generatePDF(Document document, PdfWriter writer) throws EmptyPdfBodyException, InvalidXmlElementException, DocumentException {
        addMetaData(document);
        document.open();
        createContent(document, writer);
        document.close();
    }

    protected abstract void createContent(Document document, PdfWriter writer) throws InvalidXmlElementException, DocumentException, EmptyPdfBodyException;

    protected void addEvent(PdfWriter writer) {
        writer.setPageEvent(new FooterEvent());
    }

    /**
     * Pdf as byte array. Be careful with big PDF files.
     *
     * @return
     * @throws EmptyPdfBodyException
     * @throws DocumentException
     * @throws InvalidXmlElementException
     */
    public final byte[] generate() throws EmptyPdfBodyException, DocumentException, InvalidXmlElementException {
        final Document document = new Document(getPageSize(), RIGHT_MARGIN, LEFT_MARGIN, TOP_MARGIN, BOTTOM_MARGIN);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);
        addEvent(writer);
        generatePDF(document, writer);
        return byteArrayOutputStream.toByteArray();

    }

    protected abstract void addDocumentWriterEvents(PdfWriter writer);

    public int createFile(String path) {
        if (!path.endsWith(".pdf")) {
            path += ".pdf";
        }

        try (Document document = new Document(getPageSize(), RIGHT_MARGIN, LEFT_MARGIN, TOP_MARGIN, BOTTOM_MARGIN)) {

            try {
                final PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(path));
                addEvent(writer);
                generatePDF(document, writer);
                return writer.getPageNumber();
            } catch (Exception e) {
                PdfExporterLog.errorMessage(this.getClass().getName(), e);
                return 0;
            }
        }
    }

    protected abstract Rectangle getPageSize();

    public PdfPCell getEmptyCell() {
        return getEmptyCell(1);
    }

    protected PdfPCell getEmptyCell(int colspan) {
        final Paragraph p = new Paragraph(" ", new Font(PdfTheme.getBasicFont(), PdfTheme.FONT_SIZE));
        final PdfPCell cell = new PdfPCell(p);
        cell.setColspan(colspan);
        cell.setBorder(0);
        return cell;
    }

}
