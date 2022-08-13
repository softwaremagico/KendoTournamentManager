package com.softwaremagico.kt.pdf;


import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.BaseFont;

public abstract class PdfTheme {
    public static final String LOGO_IMAGE = "kendo-tournament-manager-kendo-tournament-manager-logo.png";
    public static final String LINE_FONT_NAME = "DejaVuSansCondensed.ttf";
    public static final String LINE_FONT_ITALIC_NAME = "DejaVuSansCondensed-Oblique.ttf";
    public static final String TABLE_SUBTITLE_FONT_NAME = "DejaVuSansCondensed-Oblique.ttf";
    public static final String LINE_BOLD_FONT_NAME = "DejaVuSansCondensed-Bold.ttf";
    public static final String TITLE_FONT_NAME = "DejaVuSansCondensed-Bold.ttf";
    public static final String HANDWRITTEN_FONT_NAME = "ArchitectsDaughter.ttf";

    public static final int FOOTER_FONT_SIZE = 8;

    public static final int FONT_SIZE = 12;

    public static final int HEADER_FONT_SIZE = FONT_SIZE + 15;

    private static BaseFont footerFont;
    private static BaseFont lineFont;
    private static BaseFont lineItalicFont;
    private static BaseFont lineBoldFont;
    private static BaseFont titleFont;
    private static BaseFont tableSubtitleFont;
    private static BaseFont handwrittenFont;

    private static BaseFont basicFont;

    public static BaseFont getFooterFont() {
        if (footerFont == null) {
            final Font font = FontFactory.getFont("/" + TITLE_FONT_NAME, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 0.5f,
                    Font.NORMAL, BaseColor.BLACK);
            footerFont = font.getBaseFont();
        }
        return footerFont;
    }

    public static BaseFont getLineFont() {
        if (lineFont == null) {
            final Font font = FontFactory.getFont("/" + LINE_FONT_NAME, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 0.8f,
                    Font.NORMAL, BaseColor.BLACK);
            lineFont = font.getBaseFont();
        }
        return lineFont;
    }

    public static BaseFont getLineItalicFont() {
        if (lineItalicFont == null) {
            final Font font = FontFactory.getFont("/" + LINE_FONT_ITALIC_NAME, BaseFont.IDENTITY_H, BaseFont.EMBEDDED,
                    0.8f, Font.ITALIC, BaseColor.BLACK);
            lineItalicFont = font.getBaseFont();
        }
        return lineItalicFont;
    }

    public static BaseFont getLineFontBold() {
        if (lineBoldFont == null) {
            final Font font = FontFactory.getFont("/" + LINE_BOLD_FONT_NAME, BaseFont.IDENTITY_H, BaseFont.EMBEDDED,
                    0.8f, Font.BOLD, BaseColor.BLACK);
            lineBoldFont = font.getBaseFont();
        }
        return lineBoldFont;
    }

    public static BaseFont getTitleFont() {
        if (titleFont == null) {
            final Font font = FontFactory.getFont("/" + TITLE_FONT_NAME, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 0.8f,
                    Font.NORMAL, BaseColor.BLACK);
            titleFont = font.getBaseFont();
        }
        return titleFont;
    }

    public static BaseFont getSubtitleFont() {
        if (tableSubtitleFont == null) {
            final Font font = FontFactory.getFont("/" + TABLE_SUBTITLE_FONT_NAME, BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED, 0.8f, Font.ITALIC, BaseColor.BLACK);
            tableSubtitleFont = font.getBaseFont();
        }
        return tableSubtitleFont;
    }

    public static BaseFont getHandwrittenFont() {
        if (handwrittenFont == null) {
            final Font font = FontFactory.getFont("/" + HANDWRITTEN_FONT_NAME, BaseFont.IDENTITY_H, BaseFont.EMBEDDED,
                    0.8f, Font.NORMAL, BaseColor.BLACK);
            handwrittenFont = font.getBaseFont();
        }
        return handwrittenFont;
    }

    public static BaseFont getBasicFont() {
        if (basicFont == null) {
            final Font font = FontFactory.getFont("/" + LINE_FONT_NAME, BaseFont.IDENTITY_H, BaseFont.EMBEDDED,
                    0.8f, Font.NORMAL, BaseColor.BLACK);
            basicFont = font.getBaseFont();
        }
        return basicFont;
    }

    public static int getHandWrittenFontSize(int originalSize) {
        return originalSize - 1;
    }

}
