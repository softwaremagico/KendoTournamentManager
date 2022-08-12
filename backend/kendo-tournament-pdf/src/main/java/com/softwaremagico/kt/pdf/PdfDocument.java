package com.softwaremagico.kt.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;
import com.softwaremagico.kt.logger.PdfExporterLog;
import com.softwaremagico.kt.pdf.events.FooterEvent;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

public abstract class PdfDocument {
    private int rightMargin = 30;
    private int leftMargin = 30;
    private int topMargin = 30;
    private int bottomMargin = 30;

    protected Document addMetaData(Document document) {
        document.addTitle("List Report");
        document.addAuthor("Software Magico");
        document.addCreator("Kendo Tournament Generator v2");
        document.addSubject("Kendo");
        document.addKeywords("Kendo");
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
        final Document document = new Document(getPageSize(), rightMargin, leftMargin, topMargin, bottomMargin);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PdfWriter writer = PdfWriter.getInstance(document, baos);
        addEvent(writer);
        generatePDF(document, writer);
        return baos.toByteArray();

    }

    protected abstract void addDocumentWriterEvents(PdfWriter writer);

    public int createFile(String path) {
        if (!path.endsWith(".pdf")) {
            path += ".pdf";
        }

        // DIN A6 105 x 148 mm
        final Document document = new Document(getPageSize(), rightMargin, leftMargin, topMargin, bottomMargin);

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

    protected abstract Rectangle getPageSize();

}
