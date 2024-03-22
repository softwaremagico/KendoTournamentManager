package com.softwaremagico.kt.pdf.qr;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.logger.KendoTournamentLogger;
import com.softwaremagico.kt.pdf.PdfDocument;
import com.softwaremagico.kt.pdf.PdfTheme;
import org.springframework.context.MessageSource;

import java.io.IOException;
import java.util.Locale;

public class TournamentQr extends PdfDocument {

    private static final int BORDER = 0;

    private static final int HEADER_CELL_SIZE = 190;
    private static final int QR_CELL_SIZE = 200;
    private static final int QR_CELL_PADDING = 50;

    private Image backgroundImage;
    private Image qrCode;

    private final MessageSource messageSource;

    private final Locale locale;

    private final TournamentDTO tournamentDTO;

    public TournamentQr(MessageSource messageSource, Locale locale, TournamentDTO tournamentDTO, byte[] qrCode, byte[] backgroundImage) {
        this.tournamentDTO = tournamentDTO;
        this.messageSource = messageSource;
        this.locale = locale;
        try {
            this.qrCode = Image.getInstance(qrCode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (backgroundImage != null) {
            try {
                this.backgroundImage = Image.getInstance(backgroundImage);
            } catch (IOException e) {
                KendoTournamentLogger.severe(this.getClass().getName(), "No background image found");
                this.backgroundImage = null;
            }
        }
    }

    @Override
    protected void createContent(Document document, PdfWriter writer) {
        qrContent(document, writer);
    }

    @Override
    protected void addDocumentWriterEvents(PdfWriter writer) {

    }

    @Override
    protected Rectangle getPageSize() {
        return PageSize.A4;
    }

    @Override
    protected void addEvent(PdfWriter writer) {
        //No Footer on Diplomas.
    }

    public void qrContent(Document document, PdfWriter writer) {
        qrTable(document, writer, tournamentDTO);
    }

    private void qrTable(Document document, PdfWriter writer, TournamentDTO tournamentDTO) {
        final PdfPTable mainTable = new PdfPTable(1);
        mainTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        mainTable.setTotalWidth(document.getPageSize().getWidth());

        addBackGroundImage(document);

        final Paragraph title = new Paragraph(tournamentDTO.getName(), new Font(PdfTheme.getLineFont(), PdfTheme.HEADER_FONT_SIZE));
        final PdfPCell titleCell = new PdfPCell(title);
        titleCell.setBorderWidth(BORDER);
        titleCell.setColspan(1);
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setMinimumHeight(HEADER_CELL_SIZE);
        mainTable.addCell(titleCell);


        final Paragraph content = new Paragraph(messageSource.getMessage("qr.body.content", null, locale), new Font(PdfTheme.getLineFont(), PdfTheme.FONT_SIZE));
        final PdfPCell contentCell = new PdfPCell(content);
        contentCell.setBorderWidth(BORDER);
        contentCell.setColspan(1);
        contentCell.setPadding(QR_CELL_PADDING);
        contentCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        mainTable.addCell(contentCell);


        mainTable.setWidthPercentage(TOTAL_WIDTH);
        document.add(mainTable);

        qrCode.scaleToFit(QR_CELL_SIZE, QR_CELL_SIZE);
        qrCode.setAbsolutePosition((PageSize.A4.getWidth() - qrCode.getScaledWidth()) / 2,
                (PageSize.A4.getHeight() - qrCode.getScaledHeight()) / 2);
        document.add(qrCode);
    }

    void addBackGroundImage(Document document) {
        if (backgroundImage != null) {
            final Image background = backgroundImage;
            background.setAlignment(Image.UNDERLYING);
            background.scaleToFit(document.getPageSize().getWidth(), document.getPageSize().getHeight());
            background.setAbsolutePosition(0, 0);
            document.add(background);
        }
    }

}
