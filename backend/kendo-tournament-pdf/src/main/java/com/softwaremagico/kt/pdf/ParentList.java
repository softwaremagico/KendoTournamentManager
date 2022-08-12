package com.softwaremagico.kt.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.*;

public abstract class ParentList extends PdfDocument {
    protected int footerBorder = 0;
    protected int headerBorder = 0;


    /**
     * Creates the header of the document.
     *
     * @param document
     * @param mainTable
     * @param width
     * @param height
     * @param writer
     * @param font
     * @param fontSize
     */
    public abstract void createHeaderRow(Document document, PdfPTable mainTable, float width, float height, PdfWriter writer, BaseFont font, int fontSize);

    /**
     * Creates the body of the document.
     *
     * @param document
     * @param mainTable
     * @param width
     * @param height
     * @param writer
     * @param font
     * @param fontSize
     */
    public abstract void createBodyRows(Document document, PdfPTable mainTable, float width, float height, PdfWriter writer, BaseFont font, int fontSize)
            throws EmptyPdfBodyException;

    /**
     * Creates the footer of the document.
     *
     * @param document
     * @param mainTable
     * @param width
     * @param height
     * @param writer
     * @param font
     * @param fontSize
     */
    public abstract void createFooterRow(Document document, PdfPTable mainTable, float width, float height, PdfWriter writer, BaseFont font, int fontSize);

    /**
     * Obtain the width of the main table of the document.
     *
     * @return
     */
    public abstract float[] getTableWidths();

    /**
     * Creates an empty row. It is formed by as many cells as the width of the
     * table.
     *
     * @return
     */
    public PdfPCell getEmptyRow() {
        return getEmptyCell(getTableWidths().length);
    }

    public PdfPCell getEmptyCell(int colspan) {
        final Paragraph p = new Paragraph(" ", new Font(PdfTheme.getBasicFont(), PdfTheme.FONT_SIZE));
        final PdfPCell cell = new PdfPCell(p);
        cell.setColspan(colspan);
        cell.setBorder(0);
        // cell.setBackgroundColor(Color.WHITE);
        return cell;
    }

    public PdfPCell getCell(String text, int colspan, int align) {
        return getCell(text, 0, colspan, align, BaseColor.WHITE, PdfTheme.getBasicFont(), PdfTheme.FONT_SIZE, PdfTheme.getBasicFont().getFontType());
    }

    public PdfPCell getCell(String text, int border, int colspan, int align, Color color,
                            BaseFont font, int fontSize, int fontType) {
        final Paragraph p = new Paragraph(text, new Font(font, fontSize, fontType));
        final PdfPCell cell = new PdfPCell(p);
        cell.setColspan(colspan);
        cell.setBorderWidth(border);
        cell.setHorizontalAlignment(align);
        cell.setBackgroundColor(color);

        return cell;
    }

    /**
     * Creates a cell with a header (similar to html headers).
     *
     * @param text     Text to be putted in the cell.
     * @param border   Number of pixels of the border width.
     * @param align    Text alignment.
     * @param fontSize Size of the font.
     * @return
     */
    public PdfPCell getHeader(String text, int border, int align, int fontSize) {
        return getCell(text, border, getTableWidths().length, align, new Color(255, 255, 255), PdfTheme.getTitleFont(), fontSize, Font.BOLD);
    }

    /**
     * Section header.
     *
     * @param text   Text to be putted in the cell.
     * @param border Number of pixels of the border width.
     * @return
     */
    public PdfPCell getHeader1(String text, int border) {
        return getHeader(text, border, Element.ALIGN_CENTER, PdfTheme.FONT_SIZE + 10);
    }

    /**
     * The bigger header.
     *
     * @param text   Text to be putted in the cell.
     * @param border Number of pixels of the border width.
     * @param align  Text alignment.
     * @return
     */
    public PdfPCell getHeader1(String text, int border, int align) {
        return getHeader(text, border, align, PdfTheme.FONT_SIZE + 10);
    }

    /**
     * Subsection header.
     *
     * @param text   Text to be putted in the cell.
     * @param border Number of pixels of the border width.
     * @return
     */
    public PdfPCell getHeader2(String text, int border) {
        return getHeader(text, border, Element.ALIGN_CENTER, PdfTheme.FONT_SIZE + 6);
    }

    /**
     * Subsection header
     *
     * @param text   Text to be putted in the cell.
     * @param border Number of pixels of the border width.
     * @param align  Text alignment.
     * @return
     */
    public PdfPCell getHeader2(String text, int border, int align) {
        return getHeader(text, border, align, PdfTheme.FONT_SIZE + 6);
    }

    /**
     * Subsubsection header.
     *
     * @param text   Text to be putted in the cell.
     * @param border Number of pixels of the border width.
     * @return
     */
    public PdfPCell getHeader3(String text, int border) {
        return getHeader(text, border, Element.ALIGN_CENTER, PdfTheme.FONT_SIZE + 4);
    }

    /**
     * Subsubsection header.
     *
     * @param text   Text to be putted in the cell.
     * @param border Number of pixels of the border width.
     * @return
     */
    public PdfPCell getHeader4(String text, int border) {
        return getHeader(text, border, Element.ALIGN_CENTER, PdfTheme.FONT_SIZE + 2);
    }

    /**
     * Defines the properties of the main table of the document.
     *
     * @param mainTable
     */
    public abstract void setTableProperties(PdfPTable mainTable);

    /**
     * Creates the main table of the document.
     *
     * @param document
     */
    public void createContent(Document document, PdfWriter writer) throws EmptyPdfBodyException {
        final PdfPCell cellHeader, cellFooter;
        final PdfPTable mainTable = new PdfPTable(getTableWidths());
        setTableProperties(mainTable);

        cellHeader = new PdfPCell();
        cellHeader.setColspan(getTableWidths().length);

        cellFooter = new PdfPCell();
        cellFooter.setColspan(getTableWidths().length);

        mainTable.setHeaderRows(2);
        mainTable.setFooterRows(1);

        mainTable.setWidthPercentage(100);

        createHeaderRow(document, mainTable, document.getPageSize().getWidth(), document.getPageSize().getHeight(), writer, PdfTheme.getBasicFont(),
                PdfTheme.HEADER_FONT_SIZE);
        createFooterRow(document, mainTable, document.getPageSize().getWidth(), document.getPageSize().getHeight(), writer, PdfTheme.getBasicFont(),
                PdfTheme.FOOTER_FONT_SIZE);
        createBodyRows(document, mainTable, document.getPageSize().getWidth(), document.getPageSize().getHeight(), writer, PdfTheme.getBasicFont(),
                PdfTheme.FONT_SIZE);
    }

}
