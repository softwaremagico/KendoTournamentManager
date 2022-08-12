package com.softwaremagico.kt.pdf.events;


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

    /**
     * Adds a footer to every page
     */
    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        if (writer.getPageNumber() % 2 == 0) {
            final PdfContentByte cb = writer.getDirectContent();
            final Phrase footer = new Phrase("Created using 'Fading Suns Manager v2'",
                    new Font(PdfTheme.getFooterFont(), PdfTheme.FOOTER_FONT_SIZE));
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, footer, (document.right() - document.left()) / 2 + document.leftMargin(),
                    document.bottom() + 20, 0);
        }
    }
}
